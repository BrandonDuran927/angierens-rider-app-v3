package com.brandon.angierens_rider.task.presentation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brandon.angierens_rider.authentication.domain.AuthenticationRepository
import com.brandon.angierens_rider.core.CustomResult
import com.brandon.angierens_rider.task.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val authRepository: AuthenticationRepository
) : ViewModel() {
    var state by mutableStateOf(TaskState())
        private set

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { session ->
                if (session != null) {
                    getDeliveries()
                }
            }
        }
    }

    fun onAction(action: TaskAction) {
        when (action) {
            is TaskAction.OnLogout -> {
                // Handle logout if needed
            }
            is TaskAction.OnSearchQueryChange -> {
                state = state.copy(searchQuery = action.query)
            }
            is TaskAction.OnStatusFilterChange -> {
                state = state.copy(selectedStatus = action.status)
            }
            is TaskAction.OnRefresh -> {
                getDeliveries()
            }
        }
    }

    private fun getDeliveries() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)

            repository.getDeliveryRider().collect { result ->
                when (result) {
                    is CustomResult.Success -> {
                        state = state.copy(
                            deliveries = result.data,
                            isLoading = false,
                            error = null
                        )

                        Log.d("TaskViewModel", "Deliveries: ${result.data}")
                        result.data.forEach { delivery ->
                            Log.d("TaskViewModel", "Delivery ID: ${delivery.deliveryId}, Orders: ${delivery.orders.size}")
                        }
                    }
                    is CustomResult.Failure -> {
                        state = state.copy(
                            isLoading = false,
                            error = result.exception.message ?: "An unexpected error occurred"
                        )
                        Log.e("TaskViewModel", "Error: ${result.exception.message}", result.exception)
                    }
                }
            }
        }
    }



    fun clearError() {
        state = state.copy(error = null)
    }
}