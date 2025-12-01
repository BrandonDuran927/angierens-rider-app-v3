package com.brandon.angierens_rider.task.domain.model

data class Order(
    val orderId: String,
    val additionalInformation: String?,
    val completedDate: String?,
    val createdAt: String,
    val customerUid: String,
    val deliveryId: String?,
    val failedDeliveryReason: String?,
    val orderCooked: String?,
    val orderNumber: Int,
    val orderStatus: String,
    val orderType: String,
    val paymentId: String?,
    val scheduleId: String,
    val statusUpdatedAt: String,
    val totalPrice: Double,
    val customer: Customer? = null,
    val items: List<OrderItem> = emptyList()
)
