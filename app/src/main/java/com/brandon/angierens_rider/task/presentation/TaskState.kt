package com.brandon.angierens_rider.task.presentation

import com.brandon.angierens_rider.task.domain.model.Delivery

data class TaskState(
    val error: String? = null,
    val isLoading: Boolean = false,
    val deliveries: List<Delivery> = emptyList(),
    val searchQuery: String = "",
    val selectedStatus: String = "All"
)
