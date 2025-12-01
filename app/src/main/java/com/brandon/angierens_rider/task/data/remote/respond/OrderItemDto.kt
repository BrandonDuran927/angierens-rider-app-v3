package com.brandon.angierens_rider.task.data.remote.respond

import kotlinx.serialization.Serializable

@Serializable
data class OrderItemDto(
    val order_item_id: String,
    val order_id: String,
    val menu_id: String,
    val quantity: Long,
    val subtotal_price: Double,
    val is_completed: Boolean,
    val addOns: List<OrderItemAddOnDto>? = null,
    val menu: MenuDto? = null
)
