package com.brandon.angierens_rider.authentication.domain

data class UserSession(
    val userId: String,
    val email: String,
    val user: User
)

