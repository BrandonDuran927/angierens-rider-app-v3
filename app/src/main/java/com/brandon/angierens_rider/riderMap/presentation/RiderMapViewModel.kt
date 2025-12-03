package com.brandon.angierens_rider.riderMap.presentation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brandon.angierens_rider.core.CustomResult
import com.brandon.angierens_rider.riderMap.domain.RiderMapRepository
import com.brandon.angierens_rider.riderMap.domain.location.LocationRepository
import com.brandon.angierens_rider.riderMap.domain.location.RiderLocation
import com.brandon.angierens_rider.task.domain.repository.TaskRepository
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RiderMapViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val taskRepository: TaskRepository,
    private val riderMapRepository: RiderMapRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    var state by mutableStateOf(RiderMapState())
        private set

    private val deliveryId = savedStateHandle.getStateFlow("deliveryId", "")
    private val riderId = savedStateHandle.getStateFlow("riderId", "")

    private var locationJob: Job? = null

    init {
        Log.d("RiderMapViewModel", "Delivery ID: ${deliveryId.value}")
        viewModelScope.launch {
            getDelivery()

            taskRepository.isRealtimeFetching.onEach { isFetching ->
                state = state.copy(isLoading = isFetching)
            }.launchIn(viewModelScope)

            observeDeliveryChanges()
            fetchRoute()
        }
    }

    @OptIn(FlowPreview::class)
    fun startTracking() {
        if (state.isTracking) return
        state = state.copy(isTracking = true, isLoading = true)

        locationJob?.cancel()

        try {
            locationRepository.startLocationUpdates { latitude, longitude ->
                state = state.copy(
                    riderLocation = RiderLocation(latitude, longitude),
                    isLoading = false
                )
            }

            locationJob = viewModelScope.launch {
                snapshotFlow { state.riderLocation }
                    .filterNotNull()
                    .debounce(5000L)
                    .collect { location ->
                        val result = riderMapRepository.saveRiderLocation(location, riderId.value)

                        if (result is CustomResult.Failure) {
                            Log.e("RiderMapViewModel", "Failed to save rider location", result.exception)
                        }
                    }
            }
        } catch (e: SecurityException) {
            Log.e("RiderMapViewModel", "Failed to start location updates", e)
            state = state.copy(isTracking = false, isLoading = false)
        }
    }

    fun onAction(action: RiderMapAction) {
        when (action) {
            is RiderMapAction.UpdateOrderStatus -> {
                state = state.copy(isLoading = true)

                viewModelScope.launch {
                    val status = riderMapRepository.updateDeliveryStatus(
                        deliveryId = deliveryId.value
                    )

                    when (status) {
                        is CustomResult.Success -> {
                            Log.d("RiderMapViewModel", "Delivery status updated: ${status.data}")
                            state = state.copy(updatedDeliveryStatus = status.data, isLoading = false)
                        }
                        is CustomResult.Failure -> {
                            state = state.copy(error = status.exception.message, isLoading = false)
                        }
                    }
                }
            }
        }
    }

    private suspend fun getDelivery() {
        state = state.copy(isLoading = true)

        taskRepository.getDeliveryRider(deliveryId.value).collect { result ->
            when (result) {
                is CustomResult.Success -> {
                    state = state.copy(delivery = result.data, isLoading = false)
                }

                is CustomResult.Failure -> {
                    state = state.copy(error = result.exception.message, isLoading = false)
                    throw result.exception
                }
            }
        }
    }

    private fun observeDeliveryChanges() {
        viewModelScope.launch {
            taskRepository.observeDeliveryStatus(deliveryId.value).collect { result ->
                when (result) {
                    is CustomResult.Success -> {
                        state = state.copy(delivery = result.data)
                    }

                    is CustomResult.Failure -> {
                        state = state.copy(error = result.exception.message, isLoading = false)
                    }
                }
            }
        }
    }

    private fun fetchRoute() {
        viewModelScope.launch {
            val storeLocation = LatLng(14.818589037203248, 121.05753223366108)
            val customerLocation = state.delivery?.address?.let {
                LatLng(it.latitude, it.longitude)
            } ?: return@launch

            when (val result = riderMapRepository.getRoute(storeLocation, customerLocation)) {
                is CustomResult.Success -> {
                    state = state.copy(routePoints = result.data)
                }
                is CustomResult.Failure -> {
                    Log.e("RiderMapViewModel", "Failed to fetch route", result.exception)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        state = state.copy(isTracking = false)
    }
}