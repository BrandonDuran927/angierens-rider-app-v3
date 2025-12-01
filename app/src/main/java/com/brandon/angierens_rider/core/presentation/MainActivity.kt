package com.brandon.angierens_rider.core.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.brandon.angierens_rider.account.presentation.InfoScreenCore
import com.brandon.angierens_rider.authentication.domain.User
import com.brandon.angierens_rider.authentication.presentation.AuthenticationAction
import com.brandon.angierens_rider.authentication.presentation.AuthenticationScreenCore
import com.brandon.angierens_rider.authentication.presentation.AuthenticationViewModel
import com.brandon.angierens_rider.notification.presentation.NotificationScreenCore
import com.brandon.angierens_rider.riderMap.presentation.RiderMapScreenCore
import com.brandon.angierens_rider.task.presentation.TaskScreenCore
import com.brandon.angierens_rider.ui.theme.AngierensRiderTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

enum class ModalContent {
    TASK, INFO
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AngierensRiderTheme {
                val navController = rememberNavController()
                Navigation(navController = navController)
            }
        }
    }
}

@Composable
fun Navigation(navController: NavHostController) {
    val authViewModel: AuthenticationViewModel = hiltViewModel()
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val isCheckingAuth by authViewModel.isCheckingAuth.collectAsState()

    val startDest = if (isAuthenticated) ModalTaskRoute else LoginRoute

    NavHost(
        navController = navController,
        startDestination = startDest
    ) {
        composable<LoginRoute> {
            AuthenticationScreenCore(
                navController = navController,
                viewModel = authViewModel
            )
        }

        composable<ModalTaskRoute> {
            Log.d("AUTH_DEBUG", "Displaying ModalTaskRoute: $isCheckingAuth")
            ProtectedRoute(isAuthenticated = isAuthenticated) {
                ModalScreen(
                    currentModalContent = ModalContent.TASK,
                    navController = navController
                )
            }
        }

        composable<ModalInfoRoute> {
            ProtectedRoute(isAuthenticated = isAuthenticated) {
                ModalScreen(
                    currentModalContent = ModalContent.INFO,
                    navController = navController
                )
            }
        }

        composable<NotificationRoute> {
            ProtectedRoute(isAuthenticated = isAuthenticated) {
                NotificationScreenCore(navController = navController)
            }
        }

        composable<RiderMapRoute> {
            ProtectedRoute(isAuthenticated = isAuthenticated) {
                RiderMapScreenCore(navController = navController)
            }
        }
    }
}

@Composable
fun ProtectedRoute(
    isAuthenticated: Boolean,
    content: @Composable () -> Unit
) {
    if (isAuthenticated) {
        content()
    } else {
        // This will trigger navigation back to login
        LaunchedEffect(Unit) {
            // Navigation is handled by NavHost's startDestination
        }
    }
}


@Composable
fun ModalScreen(
    currentModalContent: ModalContent,
    navController: NavController,
    authViewModel: AuthenticationViewModel = hiltViewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = Color(0xFFFFD700)) {
                DrawerContent(
                    currentModalContent = currentModalContent,
                    navController = navController,
                    onLogout = {
                        authViewModel.onAction(AuthenticationAction.OnLogout)
                        scope.launch {
                            drawerState.close()
                            delay(150)
                            navController.navigate(LoginRoute) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    },
                    onCloseDrawer = {
                        scope.launch { drawerState.close() }
                    },
                    user = authViewModel.currentUser.collectAsState().value?.user
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    title = when (currentModalContent) {
                        ModalContent.TASK -> "TASK"
                        ModalContent.INFO -> "ACCOUNT"
                    },
                    onOpenDrawer = {
                        scope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    },
                    navController = navController
                )
            }
        ) { padding ->
            when (currentModalContent) {
                ModalContent.TASK -> TaskScreenCore(
                    modifier = Modifier.padding(padding),
                    navController = navController
                )
                ModalContent.INFO -> InfoScreenCore(modifier = Modifier.padding(padding))
            }
        }
    }
}


@Composable
fun DrawerContent(
    modifier: Modifier = Modifier,
    currentModalContent: ModalContent,
    navController: NavController,
    onCloseDrawer: () -> Unit,
    onLogout: () -> Unit,
    user: User?
) {
    // Header
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF9A501E))
            .padding(horizontal = 10.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Hello, Penny!",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )
    }

    // User info
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
    ) {
        Text(text = user?.first_name + " " + user?.last_name)
        Text(text = user?.phone_number ?: " ")
        Text(text = user?.email ?: " ")
    }

    HorizontalDivider(
        Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
        color = Color.Black.copy(alpha = 0.4f)
    )

    // Task item
    NavigationDrawerItem(
        modifier = Modifier.padding(horizontal = 20.dp),
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Default.List,
                contentDescription = "Task",
                modifier = Modifier.size(32.dp)
            )
        },
        label = {
            Text(
                text = "Task",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        },
        selected = currentModalContent == ModalContent.TASK,
        onClick = {
            navController.navigate(ModalTaskRoute) {
                popUpTo(navController.graph.startDestinationId) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
            onCloseDrawer()
        },
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = Color(0xFF9A501E),
            selectedTextColor = Color(0xFFFFD700),
            unselectedIconColor = Color.Black,
            selectedIconColor = Color(0xFFFFD700)
        )
    )

    // Account/Info item
    NavigationDrawerItem(
        modifier = Modifier.padding(horizontal = 20.dp),
        icon = {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Account",
                modifier = Modifier.size(32.dp)
            )
        },
        label = {
            Text(
                text = "Account",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        selected = currentModalContent == ModalContent.INFO,
        onClick = {
            navController.navigate(ModalInfoRoute) {
                popUpTo(navController.graph.startDestinationId) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
            onCloseDrawer()
        },
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = Color(0xFF9A501E),
            selectedTextColor = Color(0xFFFFD700),
            unselectedIconColor = Color.Black,
            selectedIconColor = Color(0xFFFFD700)
        )
    )

    HorizontalDivider(
        Modifier.padding(20.dp),
        color = Color.Black.copy(alpha = 0.4f)
    )

    NavigationDrawerItem(
        modifier = Modifier.padding(horizontal = 20.dp),
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = "Logout Icon",
                modifier = Modifier.size(32.dp)
            )
        },
        label = {
            Text(
                text = "Logout",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        },
        selected = false,
        onClick = {
            onLogout()
            onCloseDrawer()
        },
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = Color(0xFF9A501E),
            selectedTextColor = Color(0xFFFFD700),
            unselectedIconColor = Color.Black,
            selectedIconColor = Color(0xFFFFD700)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    onOpenDrawer: () -> Unit,
    navController: NavController
) {
    TopAppBar(
        navigationIcon = {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                modifier = Modifier
                    .clickable { onOpenDrawer() }
                    .size(40.dp)
                    .padding(start = 10.dp),
                tint = Color(0xFFFFD700)
            )
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.padding(start = 10.dp),
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp
                )
            }
        },
        actions = {
            // Notifications button
            IconButton(
                onClick = {
                    navController.navigate(NotificationRoute) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.padding(end = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFFFD700), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        modifier = Modifier.size(30.dp),
                        tint = Color.Black
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF9A501E)
        )
    )
}