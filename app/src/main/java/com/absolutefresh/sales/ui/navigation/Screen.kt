package com.absolutefresh.sales.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Splash : Screen("splash", "Splash")
    object SignIn : Screen("sign_in", "Sign In")
    
    // Rep Navigation Tabs
    object Beat : Screen("beat", "Today's Beat", Icons.Default.AltRoute)
    object Orders : Screen("orders", "Orders", Icons.Default.ShoppingCart)
    object Cash : Screen("cash", "Collections", Icons.Default.Payments)
    object Stock : Screen("stock", "Van Stock", Icons.Default.Inventory)
    
    // Driver Navigation Tabs
    object Deliveries : Screen("deliveries", "Deliveries", Icons.Default.LocalShipping)
    object DoneToday : Screen("done_today", "Completed", Icons.Default.CheckCircle)
}
