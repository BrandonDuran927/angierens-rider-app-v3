package com.brandon.angierens_rider.authentication.data

import android.util.Log
import com.brandon.angierens_rider.authentication.data.mappers.toDomain
import com.brandon.angierens_rider.authentication.data.respond.UserDto
import com.brandon.angierens_rider.authentication.domain.AuthenticationRepository
import com.brandon.angierens_rider.authentication.domain.User
import com.brandon.angierens_rider.authentication.domain.UserSession
import com.brandon.angierens_rider.core.CustomResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject


class AuthenticationRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val postgrest: Postgrest
) : AuthenticationRepository {

    private val _isAuthenticated = MutableStateFlow(false)
    override val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _currentUser = MutableStateFlow<UserSession?>(null)
    override val currentUser: StateFlow<UserSession?> = _currentUser.asStateFlow()

    override suspend fun checkAuthState() {
        supabaseClient.auth.sessionStatus.collect { status ->
            when (status) {

                is SessionStatus.Authenticated -> {
                    val userAuth = status.session.user
                    _isAuthenticated.value = true

                    _currentUser.value = UserSession(
                        userId = userAuth?.id ?: "",
                        email = userAuth?.email ?: "",
                        user = retrieveUserFromTable(userAuth?.id)
                    )


                    Log.d("AuthRepo", "Restored session successfully!: ${_currentUser.value}")
                    return@collect
                }

                is SessionStatus.NotAuthenticated -> {
                    _isAuthenticated.value = false
                    _currentUser.value = null

                    Log.d("AuthRepo", "No session found.")
                    return@collect
                }

                SessionStatus.Initializing -> {
                    Log.d("AuthRepo", "Loading saved session…")
                }
                is SessionStatus.RefreshFailure -> {
                    Log.d("AuthRepo", "Loading saved session…2")
                }
            }
        }
    }

    override suspend fun login(email: String, password: String): CustomResult<Unit> {
        return try {
            supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }


            val user = supabaseClient.auth.currentUserOrNull()
            Log.d("AuthenticationRepositoryImpl", "User: $user")


            user?.let {
                _currentUser.value = UserSession(
                    userId = it.id,
                    email = it.email ?: "",
                    user = retrieveUserFromTable(it.id)
                )
            }

            _isAuthenticated.value = true

            CustomResult.Success(Unit)
        } catch (e: Exception) {
            CustomResult.Failure(e)
        }
    }

    override suspend fun logout(): CustomResult<Unit> {
        return try {
            supabaseClient.auth.signOut()
            _isAuthenticated.value = false
            _currentUser.value = null
            CustomResult.Success(Unit)
        } catch (e: Exception) {
            CustomResult.Failure(e)
        }
    }

    override suspend fun resetPassword(email: String): CustomResult<Unit> {
        return try {
            supabaseClient.auth.resetPasswordForEmail(email)
            CustomResult.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            CustomResult.Failure(e)
        }
    }

    private suspend fun retrieveUserFromTable(userAuthId: String?): User {
        val userFromTable = postgrest
            .from("users")
            .select()
            .decodeList<UserDto>()
            .filter { it.user_uid == userAuthId }


        return userFromTable.first().toDomain()
    }
}