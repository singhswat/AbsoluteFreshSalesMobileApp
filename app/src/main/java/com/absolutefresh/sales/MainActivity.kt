package com.absolutefresh.sales

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.absolutefresh.sales.data.model.UserRole
import com.absolutefresh.sales.data.repository.FakeAuthRepository
import com.absolutefresh.sales.ui.auth.SignInScreen
import com.absolutefresh.sales.ui.auth.SignInScreenState
import com.absolutefresh.sales.ui.auth.SignInViewModel
import com.absolutefresh.sales.ui.navigation.BottomNavBar
import com.absolutefresh.sales.ui.navigation.Screen
import com.absolutefresh.sales.ui.splash.SplashScreen
import com.absolutefresh.sales.ui.theme.AbsoluteFreshSalesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install Android 12+ Core Splash Screen before super.onCreate
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            AbsoluteFreshSalesTheme {
                MainAppScaffold()
            }
        }
    }
}

@Composable
fun MainAppScaffold() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Splash.route

    // Auth state and session management
    val fakeRepo = FakeAuthRepository()
    val authViewModel = viewModel { SignInViewModel(fakeRepo) }
    val authState by authViewModel.uiState.collectAsState()

    val userRole = (authState as? SignInScreenState.Authenticated)?.role ?: UserRole.REP
    val showBottomBar = currentRoute != Screen.Splash.route && currentRoute != Screen.SignIn.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    userRole = userRole,
                    onNavigateTo = { route ->
                        navController.navigate(route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    authState = authState,
                    onNavigateToSignIn = {
                        navController.navigate(Screen.SignIn.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToBeat = {
                        navController.navigate(Screen.Beat.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToDeliveries = {
                        navController.navigate(Screen.Deliveries.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.SignIn.route) {
                SignInScreen(
                    viewModel = authViewModel,
                    onNavigateToBeat = {
                        navController.navigate(Screen.Beat.route) {
                            popUpTo(Screen.SignIn.route) { inclusive = true }
                        }
                    },
                    onNavigateToDeliveries = {
                        navController.navigate(Screen.Deliveries.route) {
                            popUpTo(Screen.SignIn.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Beat.route) {
                // Beat Screen destination (FS-01/02)
                androidx.compose.material3.Text("Today's Beat (Lucknow East)")
            }
            composable(Screen.Orders.route) {
                androidx.compose.material3.Text("My Orders & Drafts")
            }
            composable(Screen.Cash.route) {
                androidx.compose.material3.Text("Cash Collections Register")
            }
            composable(Screen.Stock.route) {
                androidx.compose.material3.Text("Van Stock & Freshness Settlement")
            }
            composable(Screen.Deliveries.route) {
                androidx.compose.material3.Text("Driver Deliveries Worklist")
            }
            composable(Screen.DoneToday.route) {
                androidx.compose.material3.Text("Deliveries Completed Today")
            }
        }
    }
}
