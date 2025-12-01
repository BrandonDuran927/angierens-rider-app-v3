package com.brandon.angierens_rider.task.data.remote.respond

import kotlinx.serialization.Serializable

@Serializable
data class OrderItemAddOnDto(
    val order_item_add_on_id: String,
    val order_item_id: String,
    val add_on_id: String,
    val quantity: Long,
    val subtotal_price: Double,
    val is_completed: Boolean,
    val addOn: AddOnDto? = null
)