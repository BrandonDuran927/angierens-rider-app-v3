package com.brandon.angierens_rider.authentication.presentation

sealed class AuthenticationAction {
    data class OnEmailChange(val email: String) : AuthenticationAction()
    data class OnPasswordChange(val password: String) : AuthenticationAction()
    data object OnLogin : AuthenticationAction()
    data object OnResetPassword : AuthenticationAction()
    data object OnLogout : AuthenticationAction()
}
