package com.brandon.angierens_rider.core.presentation

import kotlinx.serialization.Serializable

@Serializable
object ModalTaskRoute

@Serializable
object ModalInfoRoute

@Serializable
data class RiderMapRoute(val deliveryId: String)

@Serializable
data object NotificationRoute

@Serializable
data object LoginRoute