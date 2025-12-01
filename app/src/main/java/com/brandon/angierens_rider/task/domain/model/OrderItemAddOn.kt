package com.brandon.angierens_rider.task.domain.model

data class OrderItemAddOn(
    val orderItemAddOnId: String,
    val orderItemId: String,
    val addOnId: String,
    val quantity: Long,
    val subtotalPrice: Double,
    val isCompleted: Boolean,
    val addOn: AddOn? = null
)
