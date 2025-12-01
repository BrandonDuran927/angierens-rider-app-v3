package com.brandon.angierens_rider.task.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brandon.angierens_rider.core.statusHelper
import com.brandon.angierens_rider.task.domain.model.Address
import com.brandon.angierens_rider.task.domain.model.Delivery
import com.brandon.angierens_rider.task.domain.model.Order
import com.brandon.angierens_rider.task.domain.model.OrderItem
import com.brandon.angierens_rider.task.domain.model.OrderItemAddOn
import com.brandon.angierens_rider.ui.theme.AngierensRiderTheme

@Composable
fun DeliveryCard(
    delivery: Delivery,
    onClick: () -> Unit,
    onMapClick: () -> Unit
) {
    val primaryOrder = delivery.orders.firstOrNull()
    val orderCount = delivery.orders.size
    val totalOrderAmount = delivery.orders.sumOf { it.totalPrice }
    val totalAmount = totalOrderAmount + delivery.deliveryFee

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .shadow(5.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { onClick() }
    ) {
        // Header with status and order number
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF9A501E))
                .padding(15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (primaryOrder != null) {
                    "${statusHelper(primaryOrder.orderStatus)}${if (orderCount > 1) " ($orderCount orders)" else ""}"
                } else {
                    "No orders"
                },
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (primaryOrder != null) "#${primaryOrder.orderNumber}" else "",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        // Address
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(imageVector = Icons.Outlined.LocationOn, contentDescription = "Location")
            Text(
                text = delivery.address?.getFullAddress() ?: "Address not available",
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Delivery time
        delivery.deliveryTime?.let { time ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(imageVector = Icons.Default.MailOutline, contentDescription = "Time")
                Text(
                    text = "Must be delivered by $time",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(10.dp))

        // Total amount and action button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Total Paid Amount", fontSize = 14.sp)
                Text(text = "₱${"%.2f".format(totalAmount)}", fontSize = 32.sp)
                Text(
                    text = "(${orderCount} order${if (orderCount > 1) "s" else ""})",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Button(
                onClick = { onMapClick() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700)
                )
            ) {
                Text(text = "View Map", color = Color.Black)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsModal(
    delivery: Delivery,
    onDismiss: () -> Unit,
    onMapClick: () -> Unit
) {
    val totalOrderAmount = delivery.orders.sumOf { it.totalPrice }
    val grandTotal = totalOrderAmount + delivery.deliveryFee

    BasicAlertDialog(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        onDismissRequest = { onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF9A501E))
                    .padding(15.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Delivery Details",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "${delivery.orders.size} order(s) in this delivery",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            // Address
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(imageVector = Icons.Outlined.LocationOn, contentDescription = "Location")
                Column {
                    Text(text = "Delivery Address", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        text = delivery.address?.getFullAddress() ?: "Address not available",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Delivery time
            delivery.deliveryTime?.let { time ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.MailOutline, contentDescription = "Time")
                    Column {
                        Text(text = "Delivery Time", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            text = time,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Track address button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .background(Color(0xFFEDEDED))
                    .padding(10.dp)
                    .clickable { onMapClick() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Track Customer Address", fontWeight = FontWeight.Bold)
                Icon(imageVector = Icons.Outlined.LocationOn, contentDescription = "Location")
            }

            Text(
                modifier = Modifier.padding(start = 10.dp, top = 10.dp),
                text = "Orders in this delivery",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider(
                modifier = Modifier.padding(
                    start = 10.dp,
                    end = 10.dp,
                    bottom = 10.dp
                )
            )

            // Orders list with items
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .padding(horizontal = 10.dp)
            ) {
                items(delivery.orders) { order ->
                    OrderSection(order = order)

                    if (order != delivery.orders.last()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            thickness = 2.dp,
                            color = Color(0xFF9A501E).copy(alpha = 0.3f)
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                // Summary
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F5F5))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Subtotal (Orders):")
                            Text(text = "₱${"%.2f".format(totalOrderAmount)}")
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Delivery Fee:")
                            Text(text = "₱${"%.2f".format(delivery.deliveryFee)}")
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Paid Amount:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "₱${"%.2f".format(grandTotal)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF9A501E)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(10.dp))

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = { onDismiss() },
                    border = BorderStroke(2.dp, Color(0xFF9A501E))
                ) {
                    Text(text = "Close", color = Color(0xFF9A501E))
                }

                Button(
                    onClick = { onMapClick() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700)
                    )
                ) {
                    Text(text = "Start Delivery", color = Color.Black)
                }
            }
        }
    }
}

@Composable
private fun OrderSection(order: Order) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFAFAFA))
            .padding(12.dp)
    ) {
        // Order header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Order #${order.orderNumber}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = statusHelper(order.orderStatus),
                    fontSize = 12.sp,
                    color = when (order.orderStatus.lowercase()) {
                        "pending" -> Color(0xFFFFA500)
                        "preparing" -> Color(0xFF2196F3)
                        "ready" -> Color(0xFF4CAF50)
                        "completed" -> Color(0xFF4CAF50)
                        else -> Color.Gray
                    }
                )
            }
            Text(
                text = "₱${"%.2f".format(order.totalPrice)}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF9A501E)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Order type and payment
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Type: ${order.orderType}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        // Special instructions
        order.additionalInformation?.let { info ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "📝 Note: $info",
                fontSize = 13.sp,
                color = Color(0xFF666666),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF8DC))
                    .padding(8.dp)
            )
        }

        // Order items
        if (order.items.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Items:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            order.items.forEach { item ->
                OrderItemRow(item = item)
            }
        }
    }
}

@Composable
private fun OrderItemRow(item: OrderItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.menu?.name ?: "Unknown Menu",
                modifier = Modifier.weight(1f),
                fontSize = 13.sp
            )
            Text(
                text = "x${item.quantity}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "₱${"%.2f".format(item.subtotalPrice)}",
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Add-ons
        if (item.addOns.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            ) {
                item.addOns.forEach { addOn ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "  + ${addOn.addOn?.name ?: "Unknown Add-on"}",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "x${addOn.quantity}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "₱${"%.2f".format(addOn.subtotalPrice)}",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        // Completion status
        if (item.isCompleted) {
            Text(
                text = "✓ Completed",
                fontSize = 11.sp,
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

//@Preview(showBackground = true, backgroundColor = 0xFFEFEFEF)
//@Composable
//private fun ComponentPrev() {
//    AngierensRiderTheme {
//        val mockDelivery = Delivery(
//            addressId = "addr123",
//            createdAt = "2024-01-01",
//            deliveryFee = 50.0,
//            deliveryId = "del123",
//            deliveryTime = "13:30",
//            riderId = "rider123",
//            address = Address(
//                addressId = "addr123",
//                addressType = "Home",
//                addressLine = "123 Mango St.",
//                region = "NCR",
//                city = "Quezon City",
//                barangay = "Brgy. Central",
//                postalCode = "1100",
//                customerId = "cust123",
//                latitude = 14.6760,
//                longitude = 121.0437
//            ),
//            orders = listOf(
//                Order(
//                    orderId = "order1",
//                    additionalInformation = "Extra spicy please, no onions",
//                    completedDate = null,
//                    createdAt = "2024-01-01",
//                    customerUid = "customer123",
//                    deliveryId = "del123",
//                    failedDeliveryReason = null,
//                    orderCooked = null,
//                    orderNumber = 101,
//                    orderStatus = "Preparing",
//                    orderType = "Delivery",
//                    paymentId = "pay123",
//                    scheduleId = "sched123",
//                    statusUpdatedAt = "2024-01-01",
//                    totalPrice = 250.0,
//                    items = listOf(
//                        OrderItem(
//                            orderItemId = "item1",
//                            orderId = "order1",
//                            menuId = "menu1",
//                            quantity = 2,
//                            subtotalPrice = 200.0,
//                            isCompleted = true,
//                            addOns = listOf(
//                                OrderItemAddOn(
//                                    orderItemAddOnId = "addon1",
//                                    orderItemId = "item1",
//                                    addOnId = "addon_extra_cheese",
//                                    quantity = 2,
//                                    subtotalPrice = 50.0,
//                                    isCompleted = true
//                                )
//                            )
//                        )
//                    )
//                ),
//                Order(
//                    orderId = "order2",
//                    additionalInformation = null,
//                    completedDate = null,
//                    createdAt = "2024-01-01",
//                    customerUid = "customer123",
//                    deliveryId = "del123",
//                    failedDeliveryReason = null,
//                    orderCooked = null,
//                    orderNumber = 102,
//                    orderStatus = "Ready",
//                    orderType = "Delivery",
//                    paymentId = "pay124",
//                    scheduleId = "sched124",
//                    statusUpdatedAt = "2024-01-01",
//                    totalPrice = 180.0,
//                    items = listOf(
//                        OrderItem(
//                            orderItemId = "item2",
//                            orderId = "order2",
//                            menuId = "menu2",
//                            quantity = 1,
//                            subtotalPrice = 180.0,
//                            isCompleted = false,
//                            addOns = emptyList()
//                        )
//                    )
//                )
//            )
//        )
//
//        Column(modifier = Modifier.fillMaxSize()) {
//            DeliveryCard(
//                delivery = mockDelivery,
//                onClick = {},
//                onMapClick = {}
//            )
//        }
//    }
//}