package com.absolutefresh.sales.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.absolutefresh.sales.R
import com.absolutefresh.sales.data.model.UserRole
import com.absolutefresh.sales.ui.auth.SignInScreenState
import com.absolutefresh.sales.ui.theme.GreenPrimary
import com.absolutefresh.sales.ui.theme.OnSurfaceText
import com.absolutefresh.sales.ui.theme.OnSurfaceVariantText
import com.absolutefresh.sales.ui.theme.SaffronSecondary
import com.absolutefresh.sales.ui.theme.SurfaceCanvas
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    authState: SignInScreenState,
    onNavigateToSignIn: () -> Unit,
    onNavigateToBeat: () -> Unit,
    onNavigateToDeliveries: () -> Unit
) {
    // Pulse animation for subtle logo breathing effect
    val infiniteTransition = rememberInfiniteTransition(label = "SplashTransition")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LogoScale"
    )

    LaunchedEffect(authState) {
        // Minimum branded display time to avoid jarring flash
        delay(800)
        when (authState) {
            is SignInScreenState.Authenticated -> {
                if (authState.role == UserRole.DRIVER) {
                    onNavigateToDeliveries()
                } else {
                    onNavigateToBeat()
                }
            }
            is SignInScreenState.Form -> {
                onNavigateToSignIn()
            }
            else -> {
                // If in an error / inactive / pending state, show sign-in screen
                onNavigateToSignIn()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceCanvas),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Branded Emblem
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .scale(scale)
                    .background(GreenPrimary, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "AF",
                        color = Color.White,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(3.dp)
                            .background(SaffronSecondary, RoundedCornerShape(2.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Brand Title & Subtitle
            Text(
                text = "Absolute Fresh",
                style = MaterialTheme.typography.headlineLarge,
                color = OnSurfaceText,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Field Sales & Dispatch Ops",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariantText,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Loading / Initializing Session
            CircularProgressIndicator(
                color = GreenPrimary,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(28.dp)
            )
        }

        // Footer at the bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Fresh Batter & Chutney • Lucknow, UP",
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceVariantText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "v1.0.0 (Native Android) • Offline-First",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariantText.copy(alpha = 0.7f)
            )
        }
    }
}
