package com.brandon.angierens_rider.task.domain.model

data class Menu(
    val menuId: String,
    val name: String,
    val inclusion: String?,
    val description: String,
    val price: String,
    val isAvailable: Boolean?,
    val category: String?,
    val size: String?,
    val imageUrl: String?
)
