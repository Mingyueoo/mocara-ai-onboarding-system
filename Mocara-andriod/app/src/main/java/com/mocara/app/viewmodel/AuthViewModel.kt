package com.mocara.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mocara.app.data.repository.AuthRepository
import com.mocara.app.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val isLoggingOut: Boolean = false,
    val logoutError: String? = null
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository(AppContainer.tokenManager)
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun updateEmail(value: String) {
        _uiState.value = _uiState.value.copy(email = value, error = null)
    }

    fun updatePassword(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null)
    }

    fun login(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "Email and password are required")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(loading = true, error = null)
            runCatching { repository.login(state.email.trim(), state.password) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(loading = false, isAuthenticated = true)
                    onSuccess()
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(loading = false, error = "Login failed")
                }
        }
    }

    fun register(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.password.length < 8) {
            _uiState.value = state.copy(error = "Password must be at least 8 characters")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(loading = true, error = null)
            runCatching { repository.register(state.email.trim(), state.password) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(loading = false, isAuthenticated = true)
                    onSuccess()
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(loading = false, error = "Registration failed")
                }
        }
    }

    fun logout(onComplete: () -> Unit) {
        if (_uiState.value.isLoggingOut) {
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoggingOut = true, logoutError = null)
            runCatching { repository.logout() }
                .onSuccess {
                    _uiState.value = AuthUiState(isAuthenticated = false, isLoggingOut = false)
                    onComplete()
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoggingOut = false,
                        logoutError = "Logout failed, please try again"
                    )
                }
        }
    }

    fun clearLogoutError() {
        _uiState.value = _uiState.value.copy(logoutError = null)
    }
}
