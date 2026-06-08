package com.teodordevtech.arcadiatourism.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.teodordevtech.arcadiatourism.auth.AuthCallbackRegistry
import com.teodordevtech.arcadiatourism.auth.errorDescription
import com.teodordevtech.arcadiatourism.auth.isSignUpConfirmation
import com.teodordevtech.arcadiatourism.data.model.User
import com.teodordevtech.arcadiatourism.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val grade: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val shouldReturnToLogin: Boolean = false,
    val currentUser: User? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                authRepository.getCurrentUserProfile()
            }.onSuccess { currentUser ->
                _uiState.update { it.copy(currentUser = currentUser) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        currentUser = null,
                        errorMessage = error.message ?: "Failed to load user profile."
                    )
                }
            }
        }

        viewModelScope.launch {
            AuthCallbackRegistry.event.collect { event ->
                event ?: return@collect

                val callbackError = event.errorDescription()
                when {
                    callbackError != null -> {
                        _uiState.update {
                            it.copy(
                                currentUser = null,
                                isLoading = false,
                                errorMessage = callbackError,
                                successMessage = null,
                                shouldReturnToLogin = true
                            )
                        }
                    }

                    event.isSignUpConfirmation() -> {
                        runCatching { authRepository.logout() }
                        _uiState.update {
                            it.copy(
                                password = "",
                                currentUser = null,
                                isLoading = false,
                                errorMessage = null,
                                successMessage = "Thank you for confirming your email",
                                shouldReturnToLogin = true
                            )
                        }
                    }
                }

                AuthCallbackRegistry.clear(event.id)
            }
        }
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null, successMessage = null) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null, successMessage = null) }
    }

    fun updateFullName(fullName: String) {
        _uiState.update { it.copy(fullName = fullName, errorMessage = null, successMessage = null) }
    }

    fun updateGrade(grade: String) {
        _uiState.update { it.copy(grade = grade, errorMessage = null, successMessage = null) }
    }

    fun login() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            val result = authRepository.login(
                email = uiState.value.email.trim(),
                password = uiState.value.password
            )

            result.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUser = user,
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Login failed."
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState()
        }
    }

    fun signUp() {
        viewModelScope.launch {
            val fullName = uiState.value.fullName.trim()
            val email = uiState.value.email.trim()
            val password = uiState.value.password
            val grade = uiState.value.grade.trim()

            if (fullName.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Enter a full name.", successMessage = null) }
                return@launch
            }
            if (email.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Enter an email address.", successMessage = null) }
                return@launch
            }
            if (password.length < 6) {
                _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters.", successMessage = null) }
                return@launch
            }
            if (grade.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Enter a grade for student accounts.", successMessage = null) }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            val result = authRepository.signUp(
                fullName = fullName,
                email = email,
                password = password,
                grade = grade
            )

            result.onSuccess { user ->
                if (user != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentUser = user,
                            errorMessage = null,
                            successMessage = "Account created."
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            fullName = "",
                            password = "",
                            grade = "",
                            isLoading = false,
                            errorMessage = null,
                            successMessage = "Account created. Check your email, then log in.",
                            shouldReturnToLogin = true
                        )
                    }
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Sign up failed.",
                        successMessage = null
                    )
                }
            }
        }
    }

    fun clearFeedback() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                successMessage = null,
                shouldReturnToLogin = false
            )
        }
    }

    fun onReturnedToLogin() {
        _uiState.update { it.copy(shouldReturnToLogin = false) }
    }
}

class AuthViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(authRepository) as T
    }
}
