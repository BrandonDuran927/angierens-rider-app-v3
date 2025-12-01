package com.brandon.angierens_rider.task.data.remote.respond

import kotlinx.serialization.Serializable

@Serializable
data class OrderDto(
    val order_id: String,
    val additional_information: String?,
    val completed_date: String?,
    val created_at: String,
    val customer_uid: String,
    val delivery_id: String?,
    val failed_delivery_reason: String?,
    val order_cooked: String?,
    val order_number: Int,
    val order_status: String,
    val order_type: String,
    val payment_id: String?,
    val schedule_id: String,
    val status_updated_at: String,
    val total_price: Double,
    val customer: CustomerDto? = null,
    val items: List<OrderItemDto>? = null
)