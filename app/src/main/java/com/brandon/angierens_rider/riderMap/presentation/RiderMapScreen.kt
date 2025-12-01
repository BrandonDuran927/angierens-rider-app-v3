package com.brandon.angierens_rider.riderMap.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.MarkUnreadChatAlt
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.brandon.angierens_rider.R
import com.brandon.angierens_rider.ui.theme.AngierensRiderTheme
import androidx.core.graphics.createBitmap
import com.brandon.angierens_rider.core.NavigationHelper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState

@Composable
fun RiderMapScreenCore(
    viewModel: RiderMapViewModel = hiltViewModel(),
    navController: NavController
) {
    val context = LocalContext.current

    // Permission state
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted && coarseLocationGranted) {
            viewModel.startTracking()
        } else {
            Toast.makeText(context, "Location permission required", Toast.LENGTH_SHORT).show()
        }
    }

    // Check permissions on first composition
    LaunchedEffect(Unit) {
        val hasLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            viewModel.startTracking()
        }
    }

    Screen(
        state = viewModel.state,
        onBackPress = { navController.popBackStack() },
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Screen(
    state: RiderMapState,
    onBackPress: () -> Unit,
    onAction: (RiderMapAction) -> Unit
) {
    val context = LocalContext.current

    // 1. Define the persistent sheet state
    val sheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            skipHiddenState = true,
            initialValue = SheetValue.PartiallyExpanded
        )
    )

    // 2. Use BottomSheetScaffold instead of Scaffold
    BottomSheetScaffold(
        scaffoldState = sheetState,
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 15.dp),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        // 3. Define the sheet content using the sheetContent lambda
        sheetContent = {
            RideInfoSheetContent(
                deliveryStatus = deliveryStatusHelper(state.delivery?.deliveryStatus),
                riderLabel = riderLabelHelper(state.delivery?.deliveryStatus),
                name = "${state.delivery?.orders?.first()?.customer?.first_name} ${state.delivery?.orders?.first()?.customer?.last_name}",
                destinationLocation = LatLng(
                    state.delivery?.address?.latitude ?: 0.0,
                    state.delivery?.address?.longitude ?: 0.0
                ),
                pickupAddress = "${state.delivery?.address?.addressLine}, ${state.delivery?.address?.barangay}, ${state.delivery?.address?.city}, ${state.delivery?.address?.region}, ${state.delivery?.address?.postalCode}  ",
                amount = "${state.delivery?.orders?.first()?.totalPrice}",
                onUpdateStatus = { onAction(RiderMapAction.UpdateOrderStatus) },
                onNavigate = {
                    Toast.makeText(context, "Opening navigation...", Toast.LENGTH_SHORT).show()
                }
            )
        },
        // Optional: Customize sheet properties
        sheetContainerColor = Color.White,
        sheetPeekHeight = 100.dp, // Adjust this to control how much is visible in the collapsed state
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetDragHandle = { BottomSheetDefaults.DragHandle() } // Use the default handle
    ) { innerPadding ->
        // 4. Define the map (main content) within the content lambda
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ... (GoogleMap setup remains the same) ...

            val defaultLocation = LatLng(14.5995, 120.9842)
            val currentLocation = state.riderLocation?.let {
                LatLng(it.latitude, it.longitude)
            } ?: defaultLocation

            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(currentLocation, 18f)
            }

            LaunchedEffect(state.riderLocation) {
                state.riderLocation?.let {
                    val newPosition = LatLng(it.latitude, it.longitude)
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(newPosition, 18f),
                        durationMs = 500
                    )
                }
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = state.riderLocation != null,
                    isTrafficEnabled = true
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = true,
                    compassEnabled = true
                )
            ) {
                state.riderLocation?.let { location ->
                    Marker(
                        state = rememberUpdatedMarkerState(
                            position = LatLng(location.latitude, location.longitude)
                        ),
                        title = "Your Location",
                        snippet = "Rider position",
                        icon = remember { context.bitmapDescriptorFromVector(R.drawable.delivery_icon) }
                    )
                }
            }


        }
    }

    if (state.isLoading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.2f)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = Color(0xFF9A501E)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Loading please wait...")
        }
    }


    // 5. Remove RideInfoBottomModal, it's replaced by sheetContent
}

// ---

// ✨ REFACTORED: The card content is now the sheet content
@Composable
fun RideInfoSheetContent(
    modifier: Modifier = Modifier,
    deliveryStatus: String,
    riderLabel: String,
    name: String,
    destinationLocation: LatLng? = null,
    pickupAddress: String,
    amount: String,
    onUpdateStatus: () -> Unit,
    onNavigate: () -> Unit
) {
    val context = LocalContext.current

    // Removed Card wrapper as ModalBottomSheet provides the container
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp) // Added horizontal padding here
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFF4CAF50)
                )
                Text(
                    text = riderLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4CAF50)
                )
            }

            if (riderLabel != "Well done! Order Completed") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            destinationLocation?.let {
                                NavigationHelper.navigateWithGoogleMaps(
                                    context = context,
                                    destinationLat = it.latitude,
                                    destinationLng = it.longitude,
                                    destinationLabel = pickupAddress
                                )
                                onNavigate()
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color(0xFFE8F5E9),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Directions,
                            contentDescription = "Navigate with Google Maps",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Waze button
                    if (NavigationHelper.isWazeInstalled(context)) {
                        IconButton(
                            onClick = {
                                destinationLocation?.let {
                                    NavigationHelper.navigateWithWaze(
                                        context = context,
                                        destinationLat = it.latitude,
                                        destinationLng = it.longitude
                                    )
                                    onNavigate()
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = Color(0xFFE3F2FD),
                                    shape = CircleShape
                                )
                        ) {
                            // Using a placeholder icon, ideally use a custom Waze icon
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = "Navigate with Waze",
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // Address section
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Text(
            text = pickupAddress,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "₱",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = amount,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Text(
                text = "•",
                color = Color.Gray
            )

            Chip(
                text = "Paid",
                backgroundColor = Color(0xFFF5F5F5)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons row - IMPROVED UX
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Arrived button (primary) - Color corrected for clear visibility
            Button(
                onClick = onUpdateStatus,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9A501E), // Use a strong primary color
                    contentColor = Color.White
                ),
                enabled = if (deliveryStatus == "Order Completed") false else true
            ) {
                Text(
                    text = deliveryStatus,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) // Added separator

        // Bottom action buttons - RESTRUCTURED
        FlowRow( // Use FlowRow for better wrapping on smaller screens
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalArrangement = Arrangement.Center
        ) {
            ActionTextButton(
                onClick = { /* Open chat */ },
                icon = Icons.Default.MarkUnreadChatAlt,
                text = "Chat",
            )
            ActionTextButton(
                onClick = { /* Free call */ },
                icon = Icons.Default.Call,
                text = "Free Call",
            )
            ActionTextButton(
                onClick = { /* Help centre */ },
                icon = Icons.AutoMirrored.Filled.Help,
                text = "Help",
            )
            ActionTextButton(
                onClick = { /* More actions */ },
                icon = Icons.Default.MoreVert,
                text = "More",
            ) // Used MoreVert
        }
    }
}

// ---

// Helper Composable for clean action buttons
@Composable
fun ActionTextButton(
    onClick: () -> Unit,
    icon: ImageVector,
    text: String,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            contentColor = contentColor
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun Chip(
    text: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

@Composable
fun RideInfoContent(
    modifier: Modifier = Modifier,
    onCancel: () -> Unit,
    onGoNow: () -> Unit
) {
    // FIX: Changed to Column content as the ModalBottomSheet provides the card/surface
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Time and distance row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "23 min",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "5 km",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Route description
        Column {
            Text(
                text = "Via Susano Rd and Quirino Hwy",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                text = "Best route, despite heavier traffic than usual",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cancel button
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(2.dp, Color(0xFF8B4513)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF8B4513)
                )
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Go now button
            Button(
                onClick = onGoNow,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B4513)
                )
            ) {
                Text(
                    text = "Go now",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        // Important: Add padding for the system navigation bar (if present)
        Spacer(modifier = Modifier.height(32.dp))
    }
}

private fun deliveryStatusHelper(status: String?): String {
    if (status == null) return "navigate to store"

    val status = when (status.lowercase()) {
        "navigate to store" -> "Arrived at Store"
        "arrived at store" -> "Confirm Pickup"
        "confirm pickup" -> "Navigate to Customer"
        "navigate to customer" -> "Arrived at Customer"
        "arrived at customer" -> "Complete Order"
        "complete order" -> "Order Completed"
        else -> "Cancelled"
    }
    return status
}

private fun riderLabelHelper(status: String?): String {
    val status = when (status?.lowercase()) {
        null -> "Navigate to Store"
        "navigate to store" -> "Go to Store"
        "arrived at store" -> "Pick up food"
        "confirm pickup" -> "Confirm Pickup"
        "navigate to customer" -> "Go to Customer Location"
        "arrived at customer" -> "Waiting for the Customer"
        "complete order" -> "Well done! Order Completed"
        else -> "Order is Cancelled"
    }

    return status
}

// Add this function at the bottom of your file
private fun Context.bitmapDescriptorFromVector(vectorResId: Int): BitmapDescriptor {
    val vectorDrawable = ContextCompat.getDrawable(this, vectorResId)!!

    // Set desired size in pixels
    val sizePx = (48 * resources.displayMetrics.density).toInt()

    vectorDrawable.setBounds(0, 0, sizePx, sizePx)
    val bitmap = createBitmap(sizePx, sizePx)
    val canvas = Canvas(bitmap)
    vectorDrawable.draw(canvas)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

@Preview(showBackground = true)
@Composable
private fun ScreenPreview() {
    AngierensRiderTheme {
        RideInfoContent(
            modifier = Modifier.padding(16.dp),
            onCancel = { /* Handle cancel */ },
            onGoNow = { /* Handle go now */ }
        )
    }
}