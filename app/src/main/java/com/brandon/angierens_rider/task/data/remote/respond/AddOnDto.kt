package com.brandon.angierens_rider.task.data.remote.respond

import kotlinx.serialization.Serializable

@Serializable
data class AddOnDto(
    val add_on: String,
    val name: String,
    val price: Double
)
