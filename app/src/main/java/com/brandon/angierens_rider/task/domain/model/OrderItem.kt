package com.brandon.angierens_rider.task.domain.model

data class OrderItem(
    val orderItemId: String,
    val orderId: String,
    val menuId: String,
    val quantity: Long,
    val subtotalPrice: Double,
    val isCompleted: Boolean,
    val addOns: List<OrderItemAddOn> = emptyList(),
    val menu: Menu? = null
)