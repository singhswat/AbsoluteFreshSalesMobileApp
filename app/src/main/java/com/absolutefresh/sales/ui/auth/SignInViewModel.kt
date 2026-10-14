package com.absolutefresh.sales.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.absolutefresh.sales.data.model.UserRole
import com.absolutefresh.sales.data.repository.AuthRepository
import com.absolutefresh.sales.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class SignInScreenState {
    data class Form(
        val identifier: String = "",
        val password: String = "",
        val isSubmitEnabled: Boolean = false,
        val isLoading: Boolean = false,
        val inlineError: String? = null
    ) : SignInScreenState()

    data class Authenticated(val role: UserRole, val userName: String, val userId: String = "") : SignInScreenState()
    data class AccountInactive(val message: String) : SignInScreenState()
    data class PendingApproval(val message: String) : SignInScreenState()
    data class OfflineDeadEnd(val message: String) : SignInScreenState()
}

class SignInViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignInScreenState>(SignInScreenState.Form())
    val uiState: StateFlow<SignInScreenState> = _uiState.asStateFlow()

    init {
        checkCachedSession()
    }

    fun checkCachedSession() {
        viewModelScope.launch {
            val cachedUser = authRepository.getCachedUser()
            if (cachedUser != null) {
                _uiState.value = SignInScreenState.Authenticated(
                    role = cachedUser.role,
                    userName = cachedUser.name,
                    userId = cachedUser.id
                )
            } else {
                _uiState.value = SignInScreenState.Form()
            }
        }
    }

    fun onIdentifierChanged(newIdentifier: String) {
        val currentState = _uiState.value
        if (currentState is SignInScreenState.Form) {
            val enabled = newIdentifier.isNotBlank() && currentState.password.isNotBlank()
            _uiState.update {
                currentState.copy(
                    identifier = newIdentifier,
                    isSubmitEnabled = enabled,
                    inlineError = null
                )
            }
        }
    }

    fun onPasswordChanged(newPassword: String) {
        val currentState = _uiState.value
        if (currentState is SignInScreenState.Form) {
            val enabled = currentState.identifier.isNotBlank() && newPassword.isNotBlank()
            _uiState.update {
                currentState.copy(
                    password = newPassword,
                    isSubmitEnabled = enabled,
                    inlineError = null
                )
            }
        }
    }

    fun signIn() {
        val currentState = _uiState.value as? SignInScreenState.Form ?: return
        if (!currentState.isSubmitEnabled) return

        _uiState.update { currentState.copy(isLoading = true, inlineError = null) }

        viewModelScope.launch {
            when (val result = authRepository.signIn(currentState.identifier, currentState.password)) {
                is AuthResult.Success -> {
                    authRepository.saveCachedUser(result.user)
                    _uiState.value = SignInScreenState.Authenticated(
                        role = result.user.role,
                        userName = result.user.name,
                        userId = result.user.id
                    )
                }
                is AuthResult.InvalidCredentials -> {
                    _uiState.value = SignInScreenState.Form(
                        identifier = currentState.identifier,
                        password = "",
                        isSubmitEnabled = false,
                        isLoading = false,
                        inlineError = result.message
                    )
                }
                is AuthResult.AccountInactive -> {
                    _uiState.value = SignInScreenState.AccountInactive(result.message)
                }
                is AuthResult.PendingApproval -> {
                    _uiState.value = SignInScreenState.PendingApproval(result.message)
                }
                is AuthResult.OfflineNoCache -> {
                    _uiState.value = SignInScreenState.OfflineDeadEnd(result.message)
                }
                is AuthResult.NetworkError -> {
                    _uiState.value = SignInScreenState.Form(
                        identifier = currentState.identifier,
                        password = currentState.password,
                        isSubmitEnabled = true,
                        isLoading = false,
                        inlineError = result.message
                    )
                }
            }
        }
    }

    fun retryFromOffline() {
        _uiState.value = SignInScreenState.Form()
    }

    fun signOut() {
        _uiState.value = SignInScreenState.Form()
    }
}
