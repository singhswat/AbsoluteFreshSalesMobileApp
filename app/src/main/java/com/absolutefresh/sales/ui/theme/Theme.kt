package com.absolutefresh.sales.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = GreenOnPrimary,
    primaryContainer = GreenPrimaryContainer,
    onPrimaryContainer = GreenPrimaryFixed,
    secondary = SaffronSecondary,
    onSecondary = GreenOnPrimary,
    secondaryContainer = SaffronSecondaryContainer,
    tertiary = CrimsonTertiary,
    onTertiary = GreenOnPrimary,
    tertiaryContainer = CrimsonTertiaryContainer,
    background = SurfaceCanvas,
    onBackground = OnSurfaceText,
    surface = SurfaceCanvas,
    onSurface = OnSurfaceText,
    surfaceVariant = SurfaceContainerHigh,
    onSurfaceVariant = OnSurfaceVariantText,
    outline = BorderSubtle
)

@Composable
fun AbsoluteFreshSalesTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.primary.toArgb() // pre-35 compat; migrate to WindowInsetsController when minSdk >= 35
            insetsController.isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
