package com.brandon.angierens_rider.task.data.remote.respond

import kotlinx.serialization.Serializable

@Serializable
data class MenuDto(
    val menu_id: String,
    val name: String,
    val inclusion: String?,
    val description: String,
    val price: String,
    val is_available: Boolean?,
    val category: String?,
    val size: String?,
    val image_url: String?
)
