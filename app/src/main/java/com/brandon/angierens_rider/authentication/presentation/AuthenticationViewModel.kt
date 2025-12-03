package com.brandon.angierens_rider.authentication.presentation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brandon.angierens_rider.authentication.domain.AuthenticationRepository
import com.brandon.angierens_rider.core.CustomResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val authRepository: AuthenticationRepository
) : ViewModel() {

    var state by mutableStateOf(AuthenticationState())
        private set

    val isAuthenticated = authRepository.isAuthenticated
    val currentUser = authRepository.currentUser


    private val _isCheckingAuth = MutableStateFlow(true)
    val isCheckingAuth = _isCheckingAuth.asStateFlow()

    init {
        checkAuthState()
    }

    fun onAction(action: AuthenticationAction) {
        when (action) {
            is AuthenticationAction.OnEmailChange -> state = state.copy(email = action.email)
            is AuthenticationAction.OnPasswordChange -> state = state.copy(password = action.password)
            AuthenticationAction.OnLogin -> login()
            AuthenticationAction.OnLogout -> logout()
            AuthenticationAction.OnResetPassword -> TODO()
            AuthenticationAction.ClearError -> clearError()
        }
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            _isCheckingAuth.value = true

            authRepository.checkAuthState()

            _isCheckingAuth.value = false
        }
    }

    private fun login() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)

            authRepository.login(state.email, state.password).let { result ->
                when (result) {
                    is CustomResult.Success -> {
                        state = state.copy(isLoading = false)
                    }

                    is CustomResult.Failure -> {
                        state = state.copy(
                            isLoading = false,
                            error = result.exception.message ?: "Login failed"
                        )
                    }
                }
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)

            authRepository.logout()

            state = state.copy(isLoading = false)
        }
    }

    private fun onResetPassword() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            authRepository.resetPassword(state.email).let { result ->
                when (result) {
                    is CustomResult.Success -> {
                        state = state.copy(isLoading = false)
                    }

                    is CustomResult.Failure -> {
                        state = state.copy(
                            isLoading = false,
                            error = result.exception.message ?: "Password reset failed"
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        state = state.copy(error = null)
    }
}