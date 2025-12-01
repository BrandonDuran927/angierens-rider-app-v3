package com.brandon.angierens_rider.task.data.remote.respond

import kotlinx.serialization.Serializable

@Serializable
data class DeliveryDto(
    val address_id: String,
    val created_at: String,
    val delivery_fee: Double,
    val delivery_id: String,
    val delivery_time: String?,
    val rider_id: String?,
    val delivery_status: String?,

    val order: List<OrderDto>? = null,
    val address: AddressDto? = null
)