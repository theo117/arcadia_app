package com.teodordevtech.arcadiatourism.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.teodordevtech.arcadiatourism.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationUiState(
    val isConfigured: Boolean = false,
    val configuredUserId: String? = null,
    val message: String? = null
)

class NotificationViewModel(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    fun initializeForUser(userId: String, role: String) {
        if (_uiState.value.isConfigured && _uiState.value.configuredUserId == userId) return

        viewModelScope.launch {
            runCatching {
                notificationRepository.syncFcmTokenForUser(userId)
                notificationRepository.subscribeToRoleTopic(role)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isConfigured = true,
                        configuredUserId = userId
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(message = error.message ?: "Notification setup failed.") }
            }
        }
    }

    fun createNotification(
        topicId: String,
        title: String,
        message: String,
        eventType: String,
        createdBy: String
    ) {
        viewModelScope.launch {
            notificationRepository.createNotification(
                topicId = topicId,
                title = title,
                message = message,
                eventType = eventType,
                createdBy = createdBy
            )
        }
    }
}

class NotificationViewModelFactory(
    private val notificationRepository: NotificationRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NotificationViewModel(notificationRepository) as T
    }
}
