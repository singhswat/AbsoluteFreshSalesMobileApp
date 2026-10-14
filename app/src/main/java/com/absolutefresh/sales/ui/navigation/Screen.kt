package com.absolutefresh.sales.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Splash : Screen("splash", "Splash")
    object SignIn : Screen("sign_in", "Sign In")

    // ── Rep Navigation Tabs (5 tabs per design spec) ─────────────────────────
    object Beat       : Screen("beat",    "Beat",    Icons.AutoMirrored.Filled.AltRoute)
    object Orders     : Screen("orders",  "Orders",  Icons.Default.ShoppingCart)
    object Outlets    : Screen("outlets", "Outlets", Icons.Default.Store)
    object Stock      : Screen("stock",   "Stock",   Icons.Default.Inventory)
    object More       : Screen("more",    "More",    Icons.Default.MoreHoriz)

    // ── Rep Sub-screens (not in bottom nav) ──────────────────────────────────
    object TodaysVisit   : Screen("todays_visit/{outletId}", "Today's Visit")
    object NewOrder      : Screen("new_order/{outletId}",    "New Order")
    object AddOutlet     : Screen("add_outlet",              "Add Outlet")
    object CreateInvoice : Screen("create_invoice/{orderId}", "Tax Invoice") {
        fun createRoute(orderId: String) = "create_invoice/$orderId"
    }
    object Cash          : Screen("cash",                    "Cash")
    object BeatChange    : Screen("beat_change",             "Suggest Change")
    object OrderHistory  : Screen("order_history/{orderId}", "Order History") {
        fun createRoute(orderId: String) = "order_history/$orderId"
    }
    object Holidays      : Screen("holidays",                "Holiday Calendar")

    // ── Driver Navigation Tabs (2 tabs) ───────────────────────────────────────
    object Deliveries   : Screen("deliveries", "Deliveries", Icons.Default.LocalShipping)
    object DoneToday    : Screen("done_today", "Done Today", Icons.Default.CheckCircle)

    // ── Driver Sub-screens ────────────────────────────────────────────────────
    object ConfirmDelivery : Screen("confirm_delivery/{deliveryId}", "Confirm Delivery") {
        fun createRoute(deliveryId: String) = "confirm_delivery/$deliveryId"
    }
}

/** Bottom-nav tab sets for each role. */
val repTabs    = listOf(Screen.Beat, Screen.Orders, Screen.Outlets, Screen.Stock, Screen.More)
val driverTabs = listOf(Screen.Deliveries, Screen.DoneToday)

