package com.brandon.angierens_rider.riderMap.domain

import com.brandon.angierens_rider.core.CustomResult
import com.brandon.angierens_rider.riderMap.domain.location.RiderLocation
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.StateFlow

interface RiderMapRepository {
    suspend fun updateDeliveryStatus(deliveryId: String) : CustomResult<String>
    suspend fun getRoute(origin: LatLng, destination: LatLng): CustomResult<List<LatLng>>
    suspend fun saveRiderLocation(location: RiderLocation, riderId: String): CustomResult<Unit>
}