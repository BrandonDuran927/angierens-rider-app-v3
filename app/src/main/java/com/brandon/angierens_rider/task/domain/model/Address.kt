package com.brandon.angierens_rider.task.domain.model

data class Address(
    val addressId: String,
    val addressType: String,
    val addressLine: String,
    val region: String,
    val city: String,
    val barangay: String,
    val postalCode: String,
    val customerId: String,
    val latitude: Double,
    val longitude: Double
) {
    fun getFullAddress(): String {
        return "$addressLine, $barangay, $city, $region $postalCode"
    }
}
