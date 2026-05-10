package com.teodordevtech.arcadiatourism.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.teodordevtech.arcadiatourism.data.model.Topic
import com.teodordevtech.arcadiatourism.data.repository.TopicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TopicUiState(
    val topics: List<Topic> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null,
    val usedStorageBytes: Long = 0L,
    val storageLimitBytes: Long = TopicRepository.STORAGE_LIMIT_BYTES
)

class TopicViewModel(
    private val topicRepository: TopicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TopicUiState())
    val uiState: StateFlow<TopicUiState> = _uiState.asStateFlow()

    fun loadTopics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }

            val result = topicRepository.getTopics()
            val usageResult = topicRepository.getStorageUsageBytes()
            result.onSuccess { topics ->
                val usedBytes = usageResult.getOrDefault(_uiState.value.usedStorageBytes)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        topics = topics,
                        usedStorageBytes = usedBytes,
                        message = usageResult.exceptionOrNull()?.message
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = error.message ?: "Could not load topics."
                    )
                }
            }
        }
    }

    fun createTopic(title: String, description: String, createdBy: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }

            val result = topicRepository.createTopic(title, description, createdBy)
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, message = "Topic created.") }
                loadTopics()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = error.message ?: "Could not create topic."
                    )
                }
            }
        }
    }

    fun deleteTopic(topicId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }

            val result = topicRepository.deleteTopic(topicId)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        topics = it.topics.filterNot { topic -> topic.topicId == topicId },
                        message = "Topic deleted."
                    )
                }
                loadTopics()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = error.message ?: "Could not delete topic."
                    )
                }
            }
        }
    }
}

class TopicViewModelFactory(
    private val topicRepository: TopicRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TopicViewModel(topicRepository) as T
    }
}
