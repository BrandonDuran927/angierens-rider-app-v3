package com.brandon.angierens_rider.riderMap.presentation

import com.brandon.angierens_rider.riderMap.domain.location.RiderLocation
import com.brandon.angierens_rider.task.domain.model.Delivery
import com.google.android.gms.maps.model.LatLng

data class RiderMapState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val riderLocation: RiderLocation? = null,
    val isTracking: Boolean = false,
    val delivery: Delivery? = null,
    val updatedDeliveryStatus: String = "navigate to store",
    val routePoints: List<LatLng> = emptyList()
)