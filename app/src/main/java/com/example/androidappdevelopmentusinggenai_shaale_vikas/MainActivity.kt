package com.example.androidappdevelopmentusinggenai_shaale_vikas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.androidappdevelopmentusinggenai_shaale_vikas.ui.screens.*
import com.example.androidappdevelopmentusinggenai_shaale_vikas.ui.theme.AndroidAppDevelopmentUsingGenAIShaaleVikasTheme
import com.example.androidappdevelopmentusinggenai_shaale_vikas.viewmodel.SchoolViewModel
import com.example.androidappdevelopmentusinggenai_shaale_vikas.viewmodel.UserRole
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val label: String = "", val icon: @Composable (() -> Unit)? = null) {
    data object Landing : Screen("landing")
    data object Dashboard : Screen("dashboard", "Needs", { Icon(Icons.Default.Home, contentDescription = null) })
    data object Chat : Screen("chat", "Chat", { Icon(Icons.Default.Chat, contentDescription = null) })
    data object Impact : Screen("impact", "Impact", { Icon(Icons.Default.Info, contentDescription = null) })
    data object HallOfFame : Screen("hall_of_fame", "Donors", { Icon(Icons.Default.Star, contentDescription = null) })
    data object Profile : Screen("profile", "Profile", { Icon(Icons.Default.Person, contentDescription = null) })
    data object Admin : Screen("admin", "Headmaster", { Icon(Icons.Default.Settings, contentDescription = null) })
    data object Login : Screen("login")
    data object Signup : Screen("signup")
    data object Notifications : Screen("notifications")
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidAppDevelopmentUsingGenAIShaaleVikasTheme {
                val navController = rememberNavController()
                val viewModel: SchoolViewModel = viewModel()
                val userRole by viewModel.userRole.collectAsState()
                val currentUserName by viewModel.currentUserName.collectAsState()
                val currentUserBatch by viewModel.currentUserBatch.collectAsState()
                val notifications by viewModel.notifications.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                
                val navItems = remember(userRole) {
                    val items = mutableListOf<Screen>(Screen.Dashboard, Screen.Chat, Screen.Impact, Screen.HallOfFame, Screen.Profile)
                    if (userRole == UserRole.HEADMASTER) {
                        items.add(Screen.Admin)
                    }
                    items
                }

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val currentRoute = currentDestination?.route
                
                val isLanding = currentRoute == Screen.Landing.route
                val isLogin = currentRoute == Screen.Login.route
                val isSignup = currentRoute == Screen.Signup.route
                val isPayment = currentRoute?.startsWith("payment/") == true
                
                val showBars = currentRoute != null && !isLanding && !isLogin && !isSignup && !isPayment

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        if (showBars) {
                            CenterAlignedTopAppBar(
                                title = { 
                                    Text(
                                        "Shaale-Vikas", 
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    ) 
                                },
                                navigationIcon = {
                                    IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
                                        BadgedBox(
                                            badge = { 
                                                if (notifications.isNotEmpty()) {
                                                    Badge { Text(notifications.size.toString()) }
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                                        }
                                    }
                                },
                                actions = {
                                    if (currentUserName == "Guest Alumnus") {
                                        TextButton(onClick = { navController.navigate(Screen.Login.route) }) {
                                            Text("Admin Login")
                                        }
                                    } else {
                                        IconButton(onClick = {
                                            viewModel.logout()
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Logged out successfully")
                                            }
                                            navController.navigate(Screen.Landing.route) {
                                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                            }
                                        }) {
                                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                            )
                        }
                    },
                    bottomBar = {
                        val isTopLevel = currentRoute != null && navItems.any { it.route == currentRoute }
                        if (isTopLevel) {
                            NavigationBar {
                                navItems.forEach { screen ->
                                    NavigationBarItem(
                                        icon = screen.icon ?: {},
                                        label = { Text(screen.label) },
                                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                        onClick = {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Landing.route,
                        modifier = Modifier.padding(if (showBars) innerPadding else PaddingValues(0.dp))
                    ) {
                        composable(Screen.Landing.route) {
                            LandingScreen(
                                isLoggedIn = currentUserName != "Guest Alumnus",
                                onGetStarted = {
                                    navController.navigate(Screen.Signup.route)
                                },
                                onLoginClick = {
                                    navController.navigate(Screen.Login.route)
                                },
                                onContinueClick = {
                                    navController.navigate(Screen.Dashboard.route)
                                }
                            )
                        }
                        composable(Screen.Login.route) {
                            LoginScreen(
                                viewModel = viewModel,
                                onLoginSuccess = {
                                    val dest = if (viewModel.userRole.value == UserRole.HEADMASTER) Screen.Admin.route else Screen.Dashboard.route
                                    navController.navigate(dest) {
                                        popUpTo(Screen.Landing.route) { inclusive = true }
                                    }
                                },
                                onSignupClick = {
                                    navController.navigate(Screen.Signup.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Screen.Signup.route) {
                            SignupScreen(
                                viewModel = viewModel,
                                onSignupSuccess = {
                                    navController.navigate(Screen.Dashboard.route) {
                                        popUpTo(Screen.Landing.route) { inclusive = true }
                                    }
                                },
                                onLoginClick = {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.Signup.route) { inclusive = true }
                                    }
                                },
                                onBack = {
                                    navController.navigate(Screen.Dashboard.route) {
                                        popUpTo(Screen.Landing.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(Screen.Dashboard.route) { 
                            NeedsDashboardScreen(
                                viewModel = viewModel,
                                onNeedClick = { needId ->
                                    navController.navigate("detail/$needId")
                                }
                            ) 
                        }
                        composable(
                            route = "detail/{needId}",
                            arguments = listOf(navArgument("needId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val needId = backStackEntry.arguments?.getString("needId") ?: ""
                            NeedDetailScreen(
                                needId = needId,
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onPledgeInitiated = { amount, title ->
                                    navController.navigate("payment/$needId/$amount/$title")
                                },
                                onDiscussClick = { id ->
                                    navController.navigate("chat/$id")
                                }
                            )
                        }
                        composable(
                            route = "payment/{needId}/{amount}/{title}",
                            arguments = listOf(
                                navArgument("needId") { type = NavType.StringType },
                                navArgument("amount") { type = NavType.FloatType },
                                navArgument("title") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val needId = backStackEntry.arguments?.getString("needId") ?: ""
                            val amount = backStackEntry.arguments?.getFloat("amount")?.toDouble() ?: 0.0
                            val title = backStackEntry.arguments?.getString("title") ?: ""
                            
                            PaymentScreen(
                                amount = amount,
                                needTitle = title,
                                onPaymentSuccess = {
                                    viewModel.pledge(needId, currentUserName, currentUserBatch, amount)
                                    navController.popBackStack("detail/$needId", inclusive = false)
                                }
                            )
                        }
                        composable(Screen.Chat.route) { ChatScreen(viewModel) }
                        composable(
                            route = "chat/{needId}",
                            arguments = listOf(navArgument("needId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val needId = backStackEntry.arguments?.getString("needId")
                            ChatScreen(
                                viewModel = viewModel,
                                needId = needId,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Screen.Impact.route) { ImpactScreen(viewModel) }
                        composable(Screen.HallOfFame.route) { HallOfFameScreen(viewModel) }
                        composable(Screen.Profile.route) { 
                            ProfileScreen(
                                viewModel = viewModel,
                                onLogout = {
                                    navController.navigate(Screen.Landing.route) {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    }
                                }
                            ) 
                        }
                        composable(Screen.Admin.route) { AdminScreen(viewModel) }
                        composable(Screen.Notifications.route) {
                            NotificationScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
