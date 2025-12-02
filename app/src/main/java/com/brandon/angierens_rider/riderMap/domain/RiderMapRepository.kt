package com.brandon.angierens_rider.riderMap.domain

import com.brandon.angierens_rider.core.CustomResult
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.StateFlow

interface RiderMapRepository {
    suspend fun updateDeliveryStatus(deliveryId: String) : CustomResult<String>
    suspend fun getRoute(origin: LatLng, destination: LatLng): CustomResult<List<LatLng>>
}