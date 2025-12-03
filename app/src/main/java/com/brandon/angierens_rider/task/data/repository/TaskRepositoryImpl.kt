package com.brandon.angierens_rider.task.data.repository

import android.util.Log
import androidx.core.app.PendingIntentCompat.send
import com.brandon.angierens_rider.authentication.data.UserSessionManager
import com.brandon.angierens_rider.core.CustomResult
import com.brandon.angierens_rider.task.data.mappers.toDomain
import com.brandon.angierens_rider.task.data.remote.respond.AddOnDto
import com.brandon.angierens_rider.task.data.remote.respond.AddressDto
import com.brandon.angierens_rider.task.data.remote.respond.CustomerDto
import com.brandon.angierens_rider.task.data.remote.respond.DeliveryDto
import com.brandon.angierens_rider.task.data.remote.respond.MenuDto
import com.brandon.angierens_rider.task.data.remote.respond.OrderDto
import com.brandon.angierens_rider.task.data.remote.respond.OrderItemAddOnDto
import com.brandon.angierens_rider.task.data.remote.respond.OrderItemDto
import com.brandon.angierens_rider.task.domain.model.Delivery
import com.brandon.angierens_rider.task.domain.repository.TaskRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.PostgresChangeFilter
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject


class TaskRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val realtime: Realtime,
    private val userSessionManager: UserSessionManager,
) : TaskRepository {
    private val _isRealtimeFetching = MutableStateFlow(false)
    override val isRealtimeFetching: StateFlow<Boolean> = _isRealtimeFetching

    override fun getDeliveryRider(): Flow<CustomResult<List<Delivery>>> = flow {
        try {
            val riderId = userSessionManager.getCurrentUserId()
                ?: throw Exception("User not authenticated")

            Log.d("TaskRepositoryImpl", "riderId: $riderId")

            val deliveryDtos = postgrest
                .from("delivery")
                .select()
                .decodeList<DeliveryDto>()
                .filter { it.rider_id == riderId }

            Log.d("TaskRepositoryImpl", "Found ${deliveryDtos.size} deliveries")

            if (deliveryDtos.isEmpty()) {
                emit(CustomResult.Success(emptyList()))
                return@flow
            }

            val deliveryIds = deliveryDtos.map { it.delivery_id }

            val orders = postgrest
                .from("order")
                .select {
                    filter {
                        isIn("delivery_id", deliveryIds)
                    }
                }
                .decodeList<OrderDto>()

            Log.d("TaskRepositoryImpl", "Found ${orders.size} orders for these deliveries")

            val orderIds = orders.map { it.order_id }

            // Fetch customer data
            val customerUids = orders.map { it.customer_uid }.distinct()
            val customers = if (customerUids.isNotEmpty()) {
                postgrest
                    .from("users")
                    .select {
                        filter {
                            isIn("user_uid", customerUids)
                        }
                    }
                    .decodeList<CustomerDto>()
            } else {
                emptyList()
            }

            val orderItems = if (orderIds.isNotEmpty()) {
                postgrest
                    .from("order_item")
                    .select {
                        filter {
                            isIn("order_id", orderIds)
                        }
                    }
                    .decodeList<OrderItemDto>()
            } else {
                emptyList()
            }

            Log.d("TaskRepositoryImpl", "Found ${orderItems.size} order items")

            val menuIds = orderItems.map { it.menu_id }.distinct()

            val menus = if (menuIds.isNotEmpty()) {
                postgrest
                    .from("menu")
                    .select {
                        filter {
                            isIn("menu_id", menuIds)
                        }
                    }
                    .decodeList<MenuDto>()
            } else {
                emptyList()
            }

            val orderItemIds = orderItems.map { it.order_item_id }

            val orderItemAddOns = if (orderItemIds.isNotEmpty()) {
                postgrest
                    .from("order_item_add_on")
                    .select {
                        filter {
                            isIn("order_item_id", orderItemIds)
                        }
                    }
                    .decodeList<OrderItemAddOnDto>()
            } else {
                emptyList()
            }

            val addOnIds = orderItemAddOns.map { it.add_on_id }.distinct()

            val addOns = if (addOnIds.isNotEmpty()) {
                postgrest
                    .from("add_on")
                    .select {
                        filter {
                            isIn("add_on", addOnIds)
                        }
                    }
                    .decodeList<AddOnDto>()
            } else {
                emptyList()
            }

            val addressIds = deliveryDtos.map { it.address_id }
            val addresses = postgrest
                .from("address")
                .select {
                    filter {
                        isIn("address_id", addressIds)
                    }
                }
                .decodeList<AddressDto>()

            val addOnDetailsById = addOns.associateBy { it.add_on }
            val menuDetailsById = menus.associateBy { it.menu_id }
            val addOnsByOrderItemId = orderItemAddOns.groupBy { it.order_item_id }
            val itemsByOrderId = orderItems.groupBy { it.order_id }
            val ordersByDeliveryId = orders.groupBy { it.delivery_id }
            val addressesById = addresses.associateBy { it.address_id }
            val customersById = customers.associateBy { it.user_uid }

            val deliveries = deliveryDtos.map { deliveryDto ->
                val address = addressesById[deliveryDto.address_id]

                val deliveryOrders = ordersByDeliveryId[deliveryDto.delivery_id] ?: emptyList()

                val ordersWithItems = deliveryOrders.map { orderDto ->
                    val orderItemsForThisOrder = itemsByOrderId[orderDto.order_id] ?: emptyList()

                    val itemsWithAddOns = orderItemsForThisOrder.map { orderItem ->
                        val addOnsForThisItem =
                            addOnsByOrderItemId[orderItem.order_item_id] ?: emptyList()

                        val addOnsWithDetails = addOnsForThisItem.map { addOnItem ->
                            addOnItem.copy(addOn = addOnDetailsById[addOnItem.add_on_id])
                        }

                        orderItem.copy(
                            addOns = addOnsWithDetails,
                            menu = menuDetailsById[orderItem.menu_id]
                        )
                    }

                    orderDto.copy(
                        items = itemsWithAddOns,
                        customer = customersById[orderDto.customer_uid]
                    )
                }

                deliveryDto.copy(
                    order = ordersWithItems,
                    address = address
                ).toDomain()
            }

            emit(CustomResult.Success(deliveries))
        } catch (e: Exception) {
            Log.e("TaskRepositoryImpl", "Error fetching deliveries", e)
            e.printStackTrace()
            emit(CustomResult.Failure(e))
        }
    }

    override fun getDeliveryRider(deliveryId: String): Flow<CustomResult<Delivery>> = flow {
        try {
            val deliveryDto = postgrest
                .from("delivery")
                .select {
                    filter {
                        eq("delivery_id", deliveryId)
                    }
                }
                .decodeSingle<DeliveryDto>()

            val orders = postgrest
                .from("order")
                .select {
                    filter {
                        eq("delivery_id", deliveryId)
                    }
                }
                .decodeList<OrderDto>()

            val orderIds = orders.map { it.order_id }

            // Fetch customer data
            val customerUids = orders.map { it.customer_uid }.distinct()
            val customers = if (customerUids.isNotEmpty()) {
                postgrest
                    .from("users")
                    .select {
                        filter {
                            isIn("user_uid", customerUids)
                        }
                    }
                    .decodeList<CustomerDto>()
            } else {
                emptyList()
            }

            Log.d("TaskRepositoryImpl", "Found ${customers} customers")

            val orderItems = if (orderIds.isNotEmpty()) {
                postgrest
                    .from("order_item")
                    .select {
                        filter {
                            isIn("order_id", orderIds)
                        }
                    }
                    .decodeList<OrderItemDto>()
            } else {
                emptyList()
            }

            val menuIds = orderItems.map { it.menu_id }.distinct()

            val menus = if (menuIds.isNotEmpty()) {
                postgrest
                    .from("menu")
                    .select {
                        filter {
                            isIn("menu_id", menuIds)
                        }
                    }
                    .decodeList<MenuDto>()
            } else {
                emptyList()
            }

            val orderItemIds = orderItems.map { it.order_item_id }

            val orderItemAddOns = if (orderItemIds.isNotEmpty()) {
                postgrest
                    .from("order_item_add_on")
                    .select {
                        filter {
                            isIn("order_item_id", orderItemIds)
                        }
                    }
                    .decodeList<OrderItemAddOnDto>()
            } else {
                emptyList()
            }

            val addOnIds = orderItemAddOns.map { it.add_on_id }.distinct()

            val addOns = if (addOnIds.isNotEmpty()) {
                postgrest
                    .from("add_on")
                    .select {
                        filter {
                            isIn("add_on", addOnIds)
                        }
                    }
                    .decodeList<AddOnDto>()
            } else {
                emptyList()
            }

            val address = postgrest
                .from("address")
                .select {
                    filter {
                        eq("address_id", deliveryDto.address_id)
                    }
                }
                .decodeSingleOrNull<AddressDto>()

            val addOnDetailsById = addOns.associateBy { it.add_on }
            val menuDetailsById = menus.associateBy { it.menu_id }
            val addOnsByOrderItemId = orderItemAddOns.groupBy { it.order_item_id }
            val itemsByOrderId = orderItems.groupBy { it.order_id }
            val customersById = customers.associateBy { it.user_uid }

            val ordersWithItems = orders.map { orderDto ->
                val orderItemsForThisOrder = itemsByOrderId[orderDto.order_id] ?: emptyList()

                val itemsWithAddOns = orderItemsForThisOrder.map { orderItem ->
                    val addOnsForThisItem = addOnsByOrderItemId[orderItem.order_item_id] ?: emptyList()

                    val addOnsWithDetails = addOnsForThisItem.map { addOnItem ->
                        addOnItem.copy(addOn = addOnDetailsById[addOnItem.add_on_id])
                    }

                    orderItem.copy(
                        addOns = addOnsWithDetails,
                        menu = menuDetailsById[orderItem.menu_id]
                    )
                }


                orderDto.copy(
                    items = itemsWithAddOns,
                    customer = customersById[orderDto.customer_uid]
                )
            }

            val delivery = deliveryDto.copy(
                order = ordersWithItems,
                address = address
            ).toDomain()

            emit(CustomResult.Success(delivery))
        } catch (e: Exception) {
            Log.e("TaskRepositoryImpl", "Error fetching delivery", e)
            e.printStackTrace()
            emit(CustomResult.Failure(e))
        }
    }

    override fun observeDeliveryStatus(deliveryId: String): Flow<CustomResult<Delivery>> = callbackFlow {
        val channel = realtime.channel("delivery_$deliveryId")

        channel.postgresChangeFlow<PostgresAction>(
            schema = "public",
        ) {
            table = "delivery"
            filter("delivery_id", FilterOperator.EQ, deliveryId)
        }.onEach { action ->
            _isRealtimeFetching.value = true

            Log.d("TaskRepositoryImpl", "Delivery changed: $action")
            getDeliveryRider(deliveryId).collect { result ->
                Log.d("TaskRepositoryImpl", "Initial delivery status: $result")
                send(result)
            }

            _isRealtimeFetching.value = false
        }.launchIn(this)

        channel.subscribe(blockUntilSubscribed = true)

        getDeliveryRider(deliveryId).collect { result ->
            Log.d("TaskRepositoryImpl", "Initial delivery status: $result")
            send(result)
        }

        awaitClose {
            Log.d("TaskRepositoryImpl", "Unsubscribing from delivery updates")
            launch {
                channel.unsubscribe()
            }
        }
    }
}