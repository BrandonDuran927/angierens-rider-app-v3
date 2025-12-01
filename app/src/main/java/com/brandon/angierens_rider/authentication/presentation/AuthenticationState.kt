package com.brandon.angierens_rider.authentication.presentation

data class AuthenticationState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val email: String = "",
    val password: String = ""
)
