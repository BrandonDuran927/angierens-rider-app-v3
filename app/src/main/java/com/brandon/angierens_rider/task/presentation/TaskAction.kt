package com.brandon.angierens_rider.task.presentation

sealed class TaskAction {
    data object OnLogout : TaskAction()
    data class OnSearchQueryChange(val query: String) : TaskAction()
    data class OnStatusFilterChange(val status: String) : TaskAction()
    data object OnRefresh : TaskAction()
}