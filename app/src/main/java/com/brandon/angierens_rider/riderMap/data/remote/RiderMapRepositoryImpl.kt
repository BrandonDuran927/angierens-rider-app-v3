package com.brandon.angierens_rider.riderMap.data.remote

import android.util.Log
import com.brandon.angierens_rider.core.CustomResult
import com.brandon.angierens_rider.riderMap.domain.RiderMapRepository
import com.brandon.angierens_rider.task.data.remote.respond.DeliveryDto
import com.brandon.angierens_rider.task.domain.model.Delivery
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject

class RiderMapRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest
) : RiderMapRepository {
    override suspend fun updateDeliveryStatus(
        deliveryId: String
    ): CustomResult<String> {
        try {
            // 1. Fetch ONLY the row with the matching deliveryId (PostgREST filtering)
            val deliveryDto = postgrest
                .from("delivery")
                .select {
                    filter {
                        eq("delivery_id", deliveryId)
                    }
                }
                .decodeSingleOrNull<DeliveryDto>() // Gets one DTO or null

            val currentStatus = deliveryDto?.delivery_status ?: ""

            Log.d(
                "RiderMapRepositoryImpl",
                "Current status from DB: $currentStatus, deliveryId: $deliveryId"
            )

            val newStatus = when (currentStatus.lowercase()) {
                "" -> "navigate to store"
                "navigate to store" -> "arrived at store"
                "arrived at store" -> "confirm pickup"
                "confirm pickup" -> "navigate to customer"
                "navigate to customer" -> "arrived at customer"
                "arrived at customer" -> "complete order"
                else -> throw IllegalStateException("Invalid status for update: $currentStatus")
            }

            val updateOrderStatus = when (newStatus.lowercase()) {
                "arrived at customer" -> "On Delivery" // The previous step was "navigate to customer"
                "complete order" -> "Completed"
                else -> null
            }

            postgrest
                .from("delivery")
                .update(
                    mapOf("delivery_status" to newStatus, "status_updated_at" to "now()")
                ) {
                    filter {
                        eq("delivery_id", deliveryId)
                    }
                }

            if (updateOrderStatus != null) {
                postgrest
                    .from("order")
                    .update(
                        mapOf("order_status" to updateOrderStatus)
                    ) {
                        filter {
                            eq("delivery_id", deliveryId)
                        }
                    }
            }

            Log.d("RiderMapRepositoryImpl", "Delivery status updated to: $newStatus")
            return CustomResult.Success(newStatus)
        } catch (e: Exception) {
            e.printStackTrace()
            return CustomResult.Failure(e)
        }
    }
}