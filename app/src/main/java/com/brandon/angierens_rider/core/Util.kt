package com.brandon.angierens_rider.core

fun statusHelper(status: String) : String {
    return when (status.lowercase()) {
        "queueing" -> "Order is still in queue"
        "preparing" -> "Order is being prepared"
        "ready" -> "Order is ready for pickup"
        "cooking" -> "Order is being cooked"
        "in progress" -> "Order is in progress"
        "completed" -> "Order has been completed"
        else -> "Order has been cancelled"
    }
}