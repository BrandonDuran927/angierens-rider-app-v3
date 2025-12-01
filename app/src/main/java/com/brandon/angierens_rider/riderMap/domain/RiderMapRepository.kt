package com.brandon.angierens_rider.riderMap.domain

import com.brandon.angierens_rider.core.CustomResult

interface RiderMapRepository {
    suspend fun updateDeliveryStatus(deliveryId: String) : CustomResult<String>
}