package com.brandon.angierens_rider.authentication.data

import com.brandon.angierens_rider.authentication.domain.AuthenticationRepository
import com.brandon.angierens_rider.authentication.domain.UserSession
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSessionManager @Inject constructor(
    private val authRepository: AuthenticationRepository
) {
    val currentUser: StateFlow<UserSession?> = authRepository.currentUser

    fun getCurrentUserId(): String? = currentUser.value?.userId
    fun getCurrentUserEmail(): String? = currentUser.value?.email
}

