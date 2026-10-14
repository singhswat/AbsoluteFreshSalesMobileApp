package com.absolutefresh.sales.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.absolutefresh.sales.data.model.UserRole
import com.absolutefresh.sales.ui.theme.GreenPrimary
import com.absolutefresh.sales.ui.theme.GreenPrimaryFixed
import com.absolutefresh.sales.ui.theme.OnSurfaceVariantText
import com.absolutefresh.sales.ui.theme.SurfaceContainer

@Composable
fun BottomNavBar(
    currentRoute: String,
    userRole: UserRole,
    onNavigateTo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = if (userRole == UserRole.DRIVER) driverTabs else repTabs

    NavigationBar(
        modifier = modifier,
        containerColor = SurfaceContainer,
        tonalElevation = 4.dp
    ) {
        items.forEach { screen ->
            val selected = currentRoute == screen.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigateTo(screen.route) },
                icon = {
                    screen.icon?.let { icon ->
                        Icon(imageVector = icon, contentDescription = screen.title)
                    }
                },
                label = {
                    Text(text = screen.title)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GreenPrimary,
                    selectedTextColor = GreenPrimary,
                    indicatorColor = GreenPrimaryFixed,
                    unselectedIconColor = OnSurfaceVariantText,
                    unselectedTextColor = OnSurfaceVariantText
                )
            )
        }
    }
}
