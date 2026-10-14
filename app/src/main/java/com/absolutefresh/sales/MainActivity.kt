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
import com.absolutefresh.sales.data.repository.FakeBeatRepository
import com.absolutefresh.sales.data.repository.FakeBeatChangeRepository
import com.absolutefresh.sales.data.repository.FakeDeliveryRepository
import com.absolutefresh.sales.data.repository.FakeHolidayRepository
import com.absolutefresh.sales.data.repository.FakeOrderHistoryRepository
import com.absolutefresh.sales.data.repository.FakeOrderRepository
import com.absolutefresh.sales.data.repository.FakeOutletRepository
import com.absolutefresh.sales.ui.holidays.HolidayCalendarScreen
import com.absolutefresh.sales.ui.holidays.HolidayViewModel
import com.absolutefresh.sales.ui.orderhistory.OrderHistoryScreen
import com.absolutefresh.sales.ui.orderhistory.OrderHistoryViewModel
import com.absolutefresh.sales.ui.auth.SignInScreen
import com.absolutefresh.sales.ui.auth.SignInScreenState
import com.absolutefresh.sales.ui.auth.SignInViewModel
import com.absolutefresh.sales.ui.beat.BeatScreen
import com.absolutefresh.sales.ui.beat.BeatViewModel
import com.absolutefresh.sales.ui.beatchange.BeatChangeScreen
import com.absolutefresh.sales.ui.beatchange.BeatChangeViewModel
import com.absolutefresh.sales.ui.invoice.InvoiceScreen
import com.absolutefresh.sales.ui.invoice.InvoiceViewModel
import com.absolutefresh.sales.ui.navigation.BottomNavBar
import com.absolutefresh.sales.ui.navigation.Screen
import com.absolutefresh.sales.ui.navigation.repTabs
import com.absolutefresh.sales.ui.more.MoreScreen
import com.absolutefresh.sales.ui.more.MoreViewModel
import com.absolutefresh.sales.ui.driver.DeliveriesScreen
import com.absolutefresh.sales.ui.driver.DeliveriesViewModel
import com.absolutefresh.sales.ui.driver.DoneTodayScreen
import com.absolutefresh.sales.ui.orders.NewOrderScreen
import com.absolutefresh.sales.ui.orders.NewOrderViewModel
import com.absolutefresh.sales.ui.orders.OrdersScreen
import com.absolutefresh.sales.ui.orders.OrdersViewModel
import com.absolutefresh.sales.ui.outlets.OutletsScreen
import com.absolutefresh.sales.ui.outlets.OutletsViewModel
import com.absolutefresh.sales.ui.splash.SplashScreen
import com.absolutefresh.sales.ui.stock.StockScreen
import com.absolutefresh.sales.ui.stock.StockViewModel
import com.absolutefresh.sales.ui.theme.AbsoluteFreshSalesTheme
import com.absolutefresh.sales.ui.visit.TodaysVisitScreen
import com.absolutefresh.sales.ui.visit.TodaysVisitViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
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
    val navController     = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute      = navBackStackEntry?.destination?.route ?: Screen.Splash.route

    val fakeRepo      = FakeAuthRepository()
    val authViewModel = viewModel { SignInViewModel(fakeRepo) }
    val authState     by authViewModel.uiState.collectAsState()

    val deliveryRepo    = androidx.compose.runtime.remember { FakeDeliveryRepository() }
    val driverViewModel = viewModel { DeliveriesViewModel(deliveryRepo) }

    val outletRepo = androidx.compose.runtime.remember { FakeOutletRepository() }
    val orderRepo  = androidx.compose.runtime.remember { FakeOrderRepository() }
    val beatChangeRepo = androidx.compose.runtime.remember { FakeBeatChangeRepository() }
    val orderHistoryRepo = androidx.compose.runtime.remember { FakeOrderHistoryRepository() }
    val holidayRepo = androidx.compose.runtime.remember { FakeHolidayRepository() }

    val userRole = (authState as? SignInScreenState.Authenticated)?.role ?: UserRole.REP

    // Bottom bar visible only on top-level tab destinations.
    val bottomBarRoutes = repTabs.map { it.route } +
            listOf(Screen.Deliveries.route, Screen.DoneToday.route)
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    userRole     = userRole,
                    onNavigateTo = { route ->
                        val homeRoute = if (userRole == UserRole.DRIVER) Screen.Deliveries.route else Screen.Beat.route
                        navController.navigate(route) {
                            popUpTo(homeRoute) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Splash.route,
            modifier         = Modifier.padding(innerPadding)
        ) {
            // ── Auth ──────────────────────────────────────────────────────────
            composable(Screen.Splash.route) {
                SplashScreen(
                    authState              = authState,
                    onNavigateToSignIn     = {
                        navController.navigate(Screen.SignIn.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToBeat       = {
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
                    viewModel              = authViewModel,
                    onNavigateToBeat       = {
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

            // ── Rep top-level tabs ────────────────────────────────────────────
            composable(Screen.Beat.route) {
                val beatViewModel = viewModel { BeatViewModel(FakeBeatRepository()) }
                BeatScreen(
                    beatViewModel = beatViewModel,
                    onOutletClick = { outletId ->
                        navController.navigate("todays_visit/$outletId")
                    }
                )
            }

            composable(Screen.Orders.route) {
                val ordersViewModel = viewModel { OrdersViewModel(orderRepo) }
                OrdersScreen(
                    viewModel = ordersViewModel,
                    onNavigateToNewOrder = {
                        navController.navigate("new_order/o1")
                    },
                    onNavigateToInvoice = { orderId ->
                        navController.navigate(Screen.CreateInvoice.createRoute(orderId))
                    },
                    onNavigateToHistory = { orderId ->
                        navController.navigate(Screen.OrderHistory.createRoute(orderId))
                    }
                )
            }

            composable(Screen.Outlets.route) {
                val outletsViewModel = viewModel { OutletsViewModel(outletRepo) }
                OutletsScreen(
                    viewModel = outletsViewModel,
                    onNavigateToNewOrder = { outletId ->
                        navController.navigate("new_order/$outletId")
                    }
                )
            }

            composable(Screen.Stock.route) {
                val stockViewModel = viewModel { StockViewModel() }
                StockScreen(viewModel = stockViewModel)
            }

            composable(Screen.More.route) {
                val moreViewModel = viewModel { MoreViewModel() }
                MoreScreen(
                    viewModel = moreViewModel,
                    onSignOut = {
                        authViewModel.signOut()
                        navController.navigate(Screen.SignIn.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToBeatChange = {
                        navController.navigate(Screen.BeatChange.route)
                    },
                    onNavigateToHolidays = {
                        navController.navigate(Screen.Holidays.route)
                    }
                )
            }

            // ── Rep sub-screens (no bottom bar) ───────────────────────────────
            composable(Screen.Holidays.route) {
                val holidayViewModel = viewModel {
                    HolidayViewModel(holidayRepo)
                }
                HolidayCalendarScreen(
                    viewModel = holidayViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("todays_visit/{outletId}") { backStackEntry ->
                val outletId = backStackEntry.arguments?.getString("outletId") ?: ""
                val visitViewModel = viewModel(key = outletId) {
                    TodaysVisitViewModel(outletId, outletRepo)
                }
                TodaysVisitScreen(
                    viewModel = visitViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToNewOrder = { id ->
                        navController.navigate("new_order/$id")
                    }
                )
            }

            composable("new_order/{outletId}") { backStackEntry ->
                val outletId = backStackEntry.arguments?.getString("outletId") ?: "o1"
                val newOrderViewModel = viewModel(key = "new_order_$outletId") {
                    NewOrderViewModel(outletId, outletRepo, orderRepo)
                }
                NewOrderScreen(
                    viewModel = newOrderViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onOrderPlaced = {
                        navController.navigate(Screen.Orders.route) {
                            popUpTo(Screen.Beat.route)
                        }
                    }
                )
            }

            composable(Screen.CreateInvoice.route) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                val invoiceViewModel = viewModel(key = "invoice_$orderId") {
                    InvoiceViewModel(orderId, orderRepo, outletRepo)
                }
                InvoiceScreen(
                    viewModel = invoiceViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.BeatChange.route) {
                val beatChangeViewModel = viewModel {
                    BeatChangeViewModel(outletRepo, beatChangeRepo)
                }
                BeatChangeScreen(
                    viewModel = beatChangeViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.OrderHistory.route) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                val historyViewModel = viewModel(key = "history_$orderId") {
                    OrderHistoryViewModel(orderId, orderRepo, orderHistoryRepo)
                }
                OrderHistoryScreen(
                    viewModel = historyViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // ── Driver top-level tabs ─────────────────────────────────────────
            composable(Screen.Deliveries.route) {
                DeliveriesScreen(viewModel = driverViewModel)
            }

            composable(Screen.DoneToday.route) {
                DoneTodayScreen(viewModel = driverViewModel)
            }
        }
    }
}
