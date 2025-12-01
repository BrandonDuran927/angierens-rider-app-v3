package com.brandon.angierens_rider.authentication.domain

import com.brandon.angierens_rider.core.CustomResult
import kotlinx.coroutines.flow.StateFlow

interface AuthenticationRepository {
    val isAuthenticated: StateFlow<Boolean>
    val currentUser: StateFlow<UserSession?>
    suspend fun login(email: String, password: String): CustomResult<Unit>
    suspend fun resetPassword(email: String): CustomResult<Unit>
    suspend fun logout(): CustomResult<Unit>
    suspend fun checkAuthState()
}