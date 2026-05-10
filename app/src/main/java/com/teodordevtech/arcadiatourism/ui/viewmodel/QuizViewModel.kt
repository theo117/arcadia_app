package com.teodordevtech.arcadiatourism.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.teodordevtech.arcadiatourism.data.model.Quiz
import com.teodordevtech.arcadiatourism.data.model.QuizQuestion
import com.teodordevtech.arcadiatourism.data.model.StudentQuizResult
import com.teodordevtech.arcadiatourism.data.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuizUiState(
    val quizzes: List<Quiz> = emptyList(),
    val questions: List<QuizQuestion> = emptyList(),
    val results: List<StudentQuizResult> = emptyList(),
    val isLoading: Boolean = false,
    val quizTitleInput: String = "",
    val questionTextInput: String = "",
    val optionAInput: String = "",
    val optionBInput: String = "",
    val optionCInput: String = "",
    val optionDInput: String = "",
    val correctAnswerInput: String = "A",
    val selectedAnswers: Map<String, String> = emptyMap(),
    val currentQuizId: String? = null,
    val latestScore: Int = 0,
    val latestTotal: Int = 0,
    val latestQuizTitle: String = "",
    val message: String? = null
)

class QuizViewModel(
    private val quizRepository: QuizRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    fun updateQuizTitle(title: String) {
        _uiState.update { it.copy(quizTitleInput = title) }
    }

    fun updateQuestionText(value: String) {
        _uiState.update { it.copy(questionTextInput = value) }
    }

    fun updateOptionA(value: String) {
        _uiState.update { it.copy(optionAInput = value) }
    }

    fun updateOptionB(value: String) {
        _uiState.update { it.copy(optionBInput = value) }
    }

    fun updateOptionC(value: String) {
        _uiState.update { it.copy(optionCInput = value) }
    }

    fun updateOptionD(value: String) {
        _uiState.update { it.copy(optionDInput = value) }
    }

    fun updateCorrectAnswer(value: String) {
        _uiState.update { it.copy(correctAnswerInput = value.uppercase()) }
    }

    fun selectAnswer(questionId: String, answer: String) {
        _uiState.update {
            it.copy(selectedAnswers = it.selectedAnswers + (questionId to answer))
        }
    }

    fun loadQuizzes(topicId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            val result = quizRepository.getQuizzesForTopic(topicId)
            result.onSuccess { quizzes ->
                _uiState.update { it.copy(isLoading = false, quizzes = quizzes) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, message = error.message ?: "Could not load quizzes.")
                }
            }
        }
    }

    fun createQuiz(topicId: String, createdBy: String, onSuccess: (String) -> Unit) {
        val title = uiState.value.quizTitleInput.trim()
        if (title.isBlank()) {
            _uiState.update { it.copy(message = "Enter a quiz title.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            val result = quizRepository.createQuiz(topicId, title, createdBy)
            result.onSuccess { quizId ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        quizTitleInput = "",
                        currentQuizId = quizId,
                        message = "Quiz created."
                    )
                }
                onSuccess(quizId)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, message = error.message ?: "Could not create quiz.")
                }
            }
        }
    }

    fun deleteQuiz(topicId: String, quizId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            val result = quizRepository.deleteQuiz(quizId)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        quizzes = it.quizzes.filterNot { quiz -> quiz.quizId == quizId },
                        message = "Quiz deleted."
                    )
                }
                loadQuizzes(topicId)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, message = error.message ?: "Could not delete quiz.")
                }
            }
        }
    }

    fun addQuestionToQuiz(quizId: String) {
        val state = uiState.value
        if (
            state.questionTextInput.isBlank() ||
            state.optionAInput.isBlank() ||
            state.optionBInput.isBlank() ||
            state.optionCInput.isBlank() ||
            state.optionDInput.isBlank()
        ) {
            _uiState.update { it.copy(message = "Complete all question fields.") }
            return
        }

        if (state.correctAnswerInput !in listOf("A", "B", "C", "D")) {
            _uiState.update { it.copy(message = "Correct answer must be A, B, C or D.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            val result = quizRepository.addQuestionToQuiz(
                quizId = quizId,
                questionText = state.questionTextInput.trim(),
                optionA = state.optionAInput.trim(),
                optionB = state.optionBInput.trim(),
                optionC = state.optionCInput.trim(),
                optionD = state.optionDInput.trim(),
                correctAnswer = state.correctAnswerInput
            )
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        questionTextInput = "",
                        optionAInput = "",
                        optionBInput = "",
                        optionCInput = "",
                        optionDInput = "",
                        correctAnswerInput = "A",
                        message = "Question added."
                    )
                }
                loadQuestionsForQuiz(quizId, shuffle = false)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, message = error.message ?: "Could not add question.")
                }
            }
        }
    }

    fun loadQuestionsForQuiz(quizId: String, shuffle: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    message = null,
                    currentQuizId = quizId,
                    selectedAnswers = emptyMap(),
                    results = emptyList()
                )
            }
            val questionResult = quizRepository.getQuestionsForQuiz(quizId)
            val resultsResult = quizRepository.getQuizResults(quizId)

            questionResult.onSuccess { questions ->
                val readyQuestions = if (shuffle) questions.shuffled() else questions
                val grades = resultsResult.getOrElse { emptyList() }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        questions = readyQuestions,
                        results = grades
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, message = error.message ?: "Could not load questions.")
                }
            }
        }
    }

    fun submitQuiz(studentId: String, quizId: String, quizTitle: String, onSuccess: () -> Unit) {
        val questions = uiState.value.questions
        val selectedAnswers = uiState.value.selectedAnswers

        if (questions.isEmpty()) {
            _uiState.update { it.copy(message = "This quiz has no questions yet.") }
            return
        }

        if (selectedAnswers.size < questions.size) {
            _uiState.update { it.copy(message = "Answer all questions before submitting.") }
            return
        }

        val score = questions.count { question ->
            selectedAnswers[question.questionId] == question.correctAnswer
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            val result = quizRepository.submitQuizResult(
                studentId = studentId,
                quizId = quizId,
                score = score,
                totalQuestions = questions.size
            )
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        latestScore = score,
                        latestTotal = questions.size,
                        latestQuizTitle = quizTitle,
                        message = "Quiz submitted."
                    )
                }
                onSuccess()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, message = error.message ?: "Could not submit quiz.")
                }
            }
        }
    }
}

class QuizViewModelFactory(
    private val quizRepository: QuizRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return QuizViewModel(quizRepository) as T
    }
}
