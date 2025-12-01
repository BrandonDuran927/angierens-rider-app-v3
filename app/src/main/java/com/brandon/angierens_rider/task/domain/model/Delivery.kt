package com.brandon.angierens_rider.task.domain.model


data class Delivery(
    val addressId: String,
    val createdAt: String,
    val deliveryFee: Double,
    val deliveryId: String,
    val deliveryTime: String?,
    val riderId: String?,
    val deliveryStatus: String?,
    val orders: List<Order> = emptyList(),
    val address: Address? = null
)