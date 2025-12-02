package com.brandon.angierens_rider.task.domain.repository

import com.brandon.angierens_rider.core.CustomResult
import com.brandon.angierens_rider.task.domain.model.Delivery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface TaskRepository {
    fun getDeliveryRider(): Flow<CustomResult<List<Delivery>>>
    fun getDeliveryRider(deliveryId: String): Flow<CustomResult<Delivery>>
    fun observeDeliveryStatus(deliveryId: String): Flow<CustomResult<Delivery>>

    val isRealtimeFetching: StateFlow<Boolean>
}