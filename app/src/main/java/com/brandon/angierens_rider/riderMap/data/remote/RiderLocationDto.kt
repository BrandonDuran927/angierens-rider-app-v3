package com.brandon.angierens_rider.riderMap.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class RiderLocationDto(
    val rider_id: String,
    val latitude: Double,
    val longitude: Double,

)
