package com.brandon.angierens_rider.task.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.brandon.angierens_rider.core.presentation.RiderMapRoute
import com.brandon.angierens_rider.task.domain.model.Address
import com.brandon.angierens_rider.task.domain.model.Delivery
import com.brandon.angierens_rider.task.domain.model.Order
import com.brandon.angierens_rider.task.domain.model.OrderItem
import com.brandon.angierens_rider.task.domain.model.OrderItemAddOn
import com.brandon.angierens_rider.task.presentation.component.DeliveryCard
import com.brandon.angierens_rider.task.presentation.component.OrderDetailsModal
import com.brandon.angierens_rider.ui.theme.AngierensRiderTheme

@Composable
fun TaskScreenCore(
    viewModel: TaskViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    navController: NavController
) {
    Screen(
        state = viewModel.state,
        modifier = modifier,
        onAction = viewModel::onAction,
        onMapClick = { deliveryId ->
            // Navigate to map with delivery ID
            navController.navigate(RiderMapRoute(deliveryId))
        }
    )
}

@Composable
private fun Screen(
    state: TaskState,
    modifier: Modifier = Modifier,
    onAction: (TaskAction) -> Unit = {},
    onMapClick: (String) -> Unit = {}
) {
    var selectedDelivery by remember { mutableStateOf<Delivery?>(null) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.deliveries) {
        Log.d("TaskScreen", "Deliveries updated: ${state.deliveries.size}")
        state.deliveries.forEach { delivery ->
            Log.d("TaskScreen", "Delivery ${delivery.deliveryId}: ${delivery.orders.size} orders")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 8.dp, end = 8.dp, top = 12.dp, bottom = 8.dp)
    ) {
        // Search and Filter Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Search Box
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onAction(TaskAction.OnSearchQueryChange(it)) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search orders...") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Filter Button
            IconButton(
                onClick = { /* Handle additional filters */ },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF8B4513), RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Filter",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Status Dropdown
            Box {
                Button(
                    onClick = { expanded = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                ) {
                    Text(text = state.selectedStatus, color = Color.Black)
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dropdown Arrow",
                        tint = Color.Black
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    listOf(
                        "All",
                        "Pending",
                        "Preparing",
                        "Cooking",
                        "Ready",
                        "In Progress",
                        "Completed"
                    ).forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status) },
                            onClick = {
                                onAction(TaskAction.OnStatusFilterChange(status))
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // Content Area
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF9A501E)
                        )
                        Text(
                            text = "Loading deliveries...",
                            color = Color.Gray
                        )
                    }
                }
            }

            state.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "⚠️ Error",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                        Text(
                            text = state.error,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Button(
                            onClick = { onAction(TaskAction.OnRefresh) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF9A501E)
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }

            state.deliveries.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "📦",
                            fontSize = 64.sp
                        )
                        Text(
                            text = "No deliveries assigned",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "You don't have any deliveries at the moment",
                            color = Color.Gray
                        )
                        Button(
                            onClick = { onAction(TaskAction.OnRefresh) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFD700)
                            )
                        ) {
                            Text("Refresh", color = Color.Black)
                        }
                    }
                }
            }

            else -> {
                // Filter deliveries based on search and status
                val filteredDeliveries = state.deliveries.filter { delivery ->
                    // Search filter
                    val matchesSearch = if (state.searchQuery.isBlank()) {
                        true
                    } else {
                        val query = state.searchQuery.lowercase()

                        // Search in delivery ID
                        delivery.deliveryId.lowercase().contains(query) ||

                                // Search in address
                                delivery.address?.getFullAddress()?.lowercase()?.contains(query) == true ||

                                // Search in order numbers
                                delivery.orders.any { order ->
                                    order.orderNumber.toString().contains(query) ||
                                            order.additionalInformation?.lowercase()?.contains(query) == true ||
                                            order.customerUid.lowercase().contains(query)
                                } ||

                                // Search in order items
                                delivery.orders.any { order ->
                                    order.items.any { item ->
                                        item.menuId.lowercase().contains(query)
                                    }
                                }
                    }

                    // Status filter
                    val matchesStatus = if (state.selectedStatus == "All") {
                        true
                    } else {
                        delivery.orders.any { order ->
                            order.orderStatus.equals(state.selectedStatus, ignoreCase = true)
                        }
                    }

                    matchesSearch && matchesStatus
                }

                if (filteredDeliveries.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "🔍",
                                fontSize = 64.sp
                            )
                            Text(
                                text = "No matching deliveries",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Try adjusting your search or filter",
                                color = Color.Gray
                            )
                            Button(
                                onClick = {
                                    onAction(TaskAction.OnSearchQueryChange(""))
                                    onAction(TaskAction.OnStatusFilterChange("All"))
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFD700)
                                )
                            ) {
                                Text("Clear Filters", color = Color.Black)
                            }
                        }
                    }
                } else {
                    // Show deliveries count
                    Text(
                        text = "${filteredDeliveries.size} delivery(ies) found",
                        modifier = Modifier.padding(start = 20.dp, bottom = 8.dp),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    // Deliveries List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = filteredDeliveries,
                            key = { it.deliveryId }
                        ) { delivery ->
                            DeliveryCard(
                                delivery = delivery,
                                onClick = {
                                    selectedDelivery = delivery
                                    Log.d("TaskScreen", "Selected delivery: ${delivery.deliveryId}")
                                },
                                onMapClick = {
                                    onMapClick(delivery.deliveryId)
                                    Log.d("TaskScreen", "Navigate to map for: ${delivery.deliveryId}")
                                }
                            )
                        }

                        // Bottom spacing
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }

    // Order Details Modal
    selectedDelivery?.let { delivery ->
        OrderDetailsModal(
            delivery = delivery,
            onDismiss = {
                selectedDelivery = null
                Log.d("TaskScreen", "Dismissed modal")
            },
            onMapClick = {
                onMapClick(delivery.deliveryId)
                selectedDelivery = null
                Log.d("TaskScreen", "Navigate to map from modal: ${delivery.deliveryId}")
            },
            isRiderMap = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ScreenPreviewLoading() {
    AngierensRiderTheme {
        Screen(
            state = TaskState(isLoading = true),
            modifier = Modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ScreenPreviewError() {
    AngierensRiderTheme {
        Screen(
            state = TaskState(
                error = "Failed to load deliveries. Please check your connection."
            ),
            modifier = Modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ScreenPreviewEmpty() {
    AngierensRiderTheme {
        Screen(
            state = TaskState(
                deliveries = emptyList()
            ),
            modifier = Modifier
        )
    }
}

//@Preview(showBackground = true)
//@Composable
//private fun ScreenPreviewWithData() {
//    AngierensRiderTheme {
//        Screen(
//            state = TaskState(
//                deliveries = listOf(
//                    Delivery(
//                        addressId = "addr1",
//                        createdAt = "2024-01-01T10:00:00",
//                        deliveryFee = 50.0,
//                        deliveryId = "del1",
//                        deliveryTime = "2024-01-01T13:30:00",
//                        riderId = "rider1",
//                        address = Address(
//                            addressId = "addr1",
//                            addressType = "Home",
//                            addressLine = "123 Mango St.",
//                            region = "NCR",
//                            city = "Quezon City",
//                            barangay = "Brgy. Central",
//                            postalCode = "1100",
//                            customerId = "cust1",
//                            latitude = 14.6760,
//                            longitude = 121.0437
//                        ),
//                        orders = listOf(
//                            Order(
//                                orderId = "order1",
//                                additionalInformation = "Extra spicy please",
//                                completedDate = null,
//                                createdAt = "2024-01-01T10:00:00",
//                                customerUid = "cust1",
//                                deliveryId = "del1",
//                                failedDeliveryReason = null,
//                                orderCooked = null,
//                                orderNumber = 101,
//                                orderStatus = "Preparing",
//                                orderType = "Delivery",
//                                paymentId = "pay1",
//                                scheduleId = "sched1",
//                                statusUpdatedAt = "2024-01-01T10:00:00",
//                                totalPrice = 250.0,
//                                items = listOf(
//                                    OrderItem(
//                                        orderItemId = "item1",
//                                        orderId = "order1",
//                                        menuId = "menu1",
//                                        quantity = 2,
//                                        subtotalPrice = 200.0,
//                                        isCompleted = true,
//                                        addOns = listOf(
//                                            OrderItemAddOn(
//                                                orderItemAddOnId = "addon1",
//                                                orderItemId = "item1",
//                                                addOnId = "extra_cheese",
//                                                quantity = 2,
//                                                subtotalPrice = 50.0,
//                                                isCompleted = true
//                                            )
//                                        )
//                                    )
//                                )
//                            )
//                        )
//                    ),
//                    Delivery(
//                        addressId = "addr2",
//                        createdAt = "2024-01-01T11:00:00",
//                        deliveryFee = 50.0,
//                        deliveryId = "del2",
//                        deliveryTime = "2024-01-01T14:00:00",
//                        riderId = "rider1",
//                        address = Address(
//                            addressId = "addr2",
//                            addressType = "Office",
//                            addressLine = "456 Acacia Ave.",
//                            region = "NCR",
//                            city = "Makati",
//                            barangay = "Brgy. Poblacion",
//                            postalCode = "1200",
//                            customerId = "cust2",
//                            latitude = 14.5547,
//                            longitude = 121.0244
//                        ),
//                        orders = listOf(
//                            Order(
//                                orderId = "order2",
//                                additionalInformation = null,
//                                completedDate = null,
//                                createdAt = "2024-01-01T11:00:00",
//                                customerUid = "cust2",
//                                deliveryId = "del2",
//                                failedDeliveryReason = null,
//                                orderCooked = null,
//                                orderNumber = 102,
//                                orderStatus = "Ready",
//                                orderType = "Delivery",
//                                paymentId = "pay2",
//                                scheduleId = "sched2",
//                                statusUpdatedAt = "2024-01-01T11:00:00",
//                                totalPrice = 320.0,
//                                items = listOf(
//                                    OrderItem(
//                                        orderItemId = "item2",
//                                        orderId = "order2",
//                                        menuId = "menu2",
//                                        quantity = 3,
//                                        subtotalPrice = 320.0,
//                                        isCompleted = false,
//                                        addOns = emptyList()
//                                    )
//                                )
//                            ),
//                            Order(
//                                orderId = "order3",
//                                additionalInformation = "Call when arriving",
//                                completedDate = null,
//                                createdAt = "2024-01-01T11:15:00",
//                                customerUid = "cust2",
//                                deliveryId = "del2",
//                                failedDeliveryReason = null,
//                                orderCooked = null,
//                                orderNumber = 103,
//                                orderStatus = "Ready",
//                                orderType = "Delivery",
//                                paymentId = "pay3",
//                                scheduleId = "sched3",
//                                statusUpdatedAt = "2024-01-01T11:15:00",
//                                totalPrice = 180.0,
//                                items = listOf(
//                                    OrderItem(
//                                        orderItemId = "item3",
//                                        orderId = "order3",
//                                        menuId = "menu3",
//                                        quantity = 1,
//                                        subtotalPrice = 180.0,
//                                        isCompleted = true,
//                                        addOns = emptyList()
//                                    )
//                                )
//                            )
//                        )
//                    )
//                )
//            ),
//            modifier = Modifier
//        )
//    }
//}

//@Preview(showBackground = true)
//@Composable
//private fun ScreenPreviewFiltered() {
//    AngierensRiderTheme {
//        Screen(
//            state = TaskState(
//                deliveries = listOf(
//                    Delivery(
//                        addressId = "addr1",
//                        createdAt = "2024-01-01T10:00:00",
//                        deliveryFee = 50.0,
//                        deliveryId = "del1",
//                        deliveryTime = "2024-01-01T13:30:00",
//                        riderId = "rider1",
//                        address = Address(
//                            addressId = "addr1",
//                            addressType = "Home",
//                            addressLine = "123 Mango St.",
//                            region = "NCR",
//                            city = "Quezon City",
//                            barangay = "Brgy. Central",
//                            postalCode = "1100",
//                            customerId = "cust1",
//                            latitude = 14.6760,
//                            longitude = 121.0437
//                        ),
//                        orders = listOf(
//                            Order(
//                                orderId = "order1",
//                                additionalInformation = "Extra spicy please",
//                                completedDate = null,
//                                createdAt = "2024-01-01T10:00:00",
//                                customerUid = "cust1",
//                                deliveryId = "del1",
//                                failedDeliveryReason = null,
//                                orderCooked = null,
//                                orderNumber = 101,
//                                orderStatus = "Completed",
//                                orderType = "Delivery",
//                                paymentId = "pay1",
//                                scheduleId = "sched1",
//                                statusUpdatedAt = "2024-01-01T10:00:00",
//                                totalPrice = 250.0,
//                                items = emptyList()
//                            )
//                        )
//                    )
//                ),
//                searchQuery = "nonexistent",
//                selectedStatus = "Preparing"
//            ),
//            modifier = Modifier
//        )
//    }
//}