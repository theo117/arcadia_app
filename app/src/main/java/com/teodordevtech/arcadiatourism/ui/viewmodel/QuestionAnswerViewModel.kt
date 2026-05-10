package com.teodordevtech.arcadiatourism.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.teodordevtech.arcadiatourism.data.model.QuestionWithAnswer
import com.teodordevtech.arcadiatourism.data.repository.QuestionAnswerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuestionAnswerUiState(
    val questions: List<QuestionWithAnswer> = emptyList(),
    val isLoading: Boolean = false,
    val askQuestionInput: String = "",
    val draftAnswers: Map<String, String> = emptyMap(),
    val message: String? = null
)

class QuestionAnswerViewModel(
    private val questionAnswerRepository: QuestionAnswerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuestionAnswerUiState())
    val uiState: StateFlow<QuestionAnswerUiState> = _uiState.asStateFlow()

    fun updateAskQuestionInput(value: String) {
        _uiState.update { it.copy(askQuestionInput = value) }
    }

    fun updateDraftAnswer(questionId: String, value: String) {
        _uiState.update {
            it.copy(draftAnswers = it.draftAnswers + (questionId to value))
        }
    }

    fun loadQuestions(topicId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            val result = questionAnswerRepository.getQuestionsForTopic(topicId)
            result.onSuccess { questions ->
                _uiState.update { it.copy(isLoading = false, questions = questions) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, message = error.message ?: "Could not load questions.")
                }
            }
        }
    }

    fun askQuestion(topicId: String, studentId: String, onSuccess: () -> Unit) {
        val questionText = uiState.value.askQuestionInput.trim()
        if (questionText.isBlank()) {
            _uiState.update { it.copy(message = "Enter your question.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            val result = questionAnswerRepository.askQuestion(topicId, studentId, questionText)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        askQuestionInput = "",
                        message = "Question sent."
                    )
                }
                loadQuestions(topicId)
                onSuccess()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, message = error.message ?: "Could not send question.")
                }
            }
        }
    }

    fun answerQuestion(
        topicId: String,
        questionId: String,
        teacherId: String,
        onSuccess: () -> Unit = {}
    ) {
        val answerText = uiState.value.draftAnswers[questionId].orEmpty().trim()
        if (answerText.isBlank()) {
            _uiState.update { it.copy(message = "Enter an answer before submitting.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            val result = questionAnswerRepository.answerQuestion(questionId, teacherId, answerText)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        draftAnswers = it.draftAnswers - questionId,
                        message = "Answer saved."
                    )
                }
                loadQuestions(topicId)
                onSuccess()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, message = error.message ?: "Could not save answer.")
                }
            }
        }
    }
}

class QuestionAnswerViewModelFactory(
    private val questionAnswerRepository: QuestionAnswerRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return QuestionAnswerViewModel(questionAnswerRepository) as T
    }
}
