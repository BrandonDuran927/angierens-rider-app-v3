package com.brandon.angierens_rider.riderMap.data.remote

import android.util.Log
import com.brandon.angierens_rider.BuildConfig
import com.brandon.angierens_rider.core.CustomResult
import com.brandon.angierens_rider.riderMap.domain.RiderMapRepository
import com.brandon.angierens_rider.task.data.remote.respond.DeliveryDto
import com.brandon.angierens_rider.task.domain.model.Delivery
import com.google.android.gms.maps.model.LatLng
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
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
                "confirm pickup" -> "On Delivery" // The previous step was "navigate to customer"
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

            Log.d("RiderMapRepositoryImpl", "Delivery status updated to: $newStatus; updated order status: $updateOrderStatus")
            return CustomResult.Success(newStatus)
        } catch (e: Exception) {
            e.printStackTrace()
            return CustomResult.Failure(e)
        }
    }

    override suspend fun getRoute(
        origin: LatLng,
        destination: LatLng
    ): CustomResult<List<LatLng>> {
        return try {
            val apiKey = BuildConfig.MAPS_API_KEY

            val originStr = "${origin.latitude},${origin.longitude}"
            val destinationStr = "${destination.latitude},${destination.longitude}"

            val url = "https://maps.googleapis.com/maps/api/directions/json" +
                    "?origin=$originStr" +
                    "&destination=$destinationStr" +
                    "&key=$apiKey"

            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .build()

            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }

            if (!response.isSuccessful) {
                return CustomResult.Failure(Exception("Failed to fetch route: ${response.code}"))
            }

            val responseBody = response.body?.string() ?: ""
            val jsonObject = JSONObject(responseBody)

            val status = jsonObject.getString("status")
            if (status != "OK") {
                return CustomResult.Failure(Exception("Directions API error: $status"))
            }

            val routes = jsonObject.getJSONArray("routes")
            if (routes.length() == 0) {
                return CustomResult.Failure(Exception("No routes found"))
            }

            val route = routes.getJSONObject(0)
            val overviewPolyline = route.getJSONObject("overview_polyline")
            val encodedPolyline = overviewPolyline.getString("points")

            // Decode the polyline
            val decodedPath = decodePolyline(encodedPolyline)

            Log.d("RiderMapRepositoryImpl", "Route fetched with ${decodedPath.size} points")
            CustomResult.Success(decodedPath)

        } catch (e: Exception) {
            Log.e("RiderMapRepositoryImpl", "Error fetching route", e)
            CustomResult.Failure(e)
        }
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng(
                lat.toDouble() / 1E5,
                lng.toDouble() / 1E5
            )
            poly.add(latLng)
        }

        return poly
    }
}