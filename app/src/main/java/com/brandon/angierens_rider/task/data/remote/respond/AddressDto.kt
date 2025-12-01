package com.brandon.angierens_rider.task.data.remote.respond

import kotlinx.serialization.Serializable

@Serializable
data class AddressDto(
    val address_id: String,
    val address_type: String,
    val address_line: String,
    val region: String,
    val city: String,
    val barangay: String,
    val postal_code: String,
    val customer_id: String,
    val latitude: Double,
    val longitude: Double
)