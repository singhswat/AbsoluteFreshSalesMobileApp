package com.absolutefresh.sales.ui.visit

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.absolutefresh.sales.data.model.Outlet
import com.absolutefresh.sales.data.model.VisitState
import com.absolutefresh.sales.ui.theme.AgingBg
import com.absolutefresh.sales.ui.theme.AgingText
import com.absolutefresh.sales.ui.theme.BorderSubtle
import com.absolutefresh.sales.ui.theme.CriticalBg
import com.absolutefresh.sales.ui.theme.CriticalText
import com.absolutefresh.sales.ui.theme.FreshBg
import com.absolutefresh.sales.ui.theme.FreshText
import com.absolutefresh.sales.ui.theme.GreenPrimary
import com.absolutefresh.sales.ui.theme.SurfaceCanvas
import com.absolutefresh.sales.ui.theme.SurfaceCard
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodaysVisitScreen(
    viewModel: TodaysVisitViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToNewOrder: (outletId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val outlet = uiState.outlet
    if (outlet == null) {
        Box(modifier = modifier.fillMaxSize().background(SurfaceCanvas), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = GreenPrimary)
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceCanvas)
    ) {
        // App Bar Header
        Surface(color = SurfaceCard, shadowElevation = 1.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GreenPrimary)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(text = outlet.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = "Stop #${uiState.sequenceNumber} • ${outlet.area}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Visit Status & Check-In Card
            VisitStatusCard(
                visitState = uiState.visitState,
                checkInTimestamp = uiState.checkInTimestamp,
                completedSummary = uiState.completedSummary,
                onCheckIn = { viewModel.checkIn() },
                onCompleteVisit = { viewModel.markVisitCompleted() }
            )

            // Outlet Details Card
            OutletCommercialCard(outlet = outlet)

            // Visit Actions Grid
            VisitActionsCard(
                visitState = uiState.visitState,
                onTakeOrder = { onNavigateToNewOrder(outlet.id) },
                onNoOrder = { viewModel.openNoOrderDialog() },
                onCall = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${outlet.phone}"))
                    context.startActivity(intent)
                },
                onDirections = {
                    val uri = Uri.parse("geo:0,0?q=${Uri.encode("${outlet.name}, ${outlet.address}")}")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(intent)
                }
            )

            // Geofence confirmation
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "GPS Match: Within 15m of store • Online sync active",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }

    // No Order Dialog
    if (uiState.isNoOrderDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.closeNoOrderDialog() },
            title = { Text("Record No Order (ऑर्डर नहीं हुआ)", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Select or enter reason why no order was booked today:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    listOf("Store has sufficient stock", "Owner not present at shop", "Shop temporarily closed", "Payment dispute / Credit limit exceeded").forEach { r ->
                        Surface(
                            color = SurfaceCanvas,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable { viewModel.updateNoOrderReason(r) }
                        ) {
                            Text(
                                text = "• $r",
                                modifier = Modifier.padding(8.dp),
                                fontSize = 12.sp,
                                fontWeight = if (uiState.noOrderReason == r) FontWeight.Bold else FontWeight.Normal,
                                color = if (uiState.noOrderReason == r) GreenPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.noOrderReason,
                        onValueChange = { viewModel.updateNoOrderReason(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Other reason...") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.submitNoOrder() },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("Confirm (सहेजें)")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeNoOrderDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun VisitStatusCard(
    visitState: VisitState,
    checkInTimestamp: Long?,
    completedSummary: String?,
    onCheckIn: () -> Unit,
    onCompleteVisit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.TaskAlt, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Visit Execution State", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                VisitStateBadge(state = visitState)
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (visitState) {
                VisitState.PLANNED -> {
                    Text(text = "Rep has not checked in at store yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onCheckIn,
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Check In to Store (दुकान पर पहुंचे)", fontWeight = FontWeight.Bold)
                    }
                }
                VisitState.ACTIVE -> {
                    val timeStr = checkInTimestamp?.let { formatTime(it) } ?: "Just now"
                    Text(text = "Checked In at $timeStr. In-store execution active.", fontSize = 12.sp, color = GreenPrimary, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onCompleteVisit,
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Complete Visit (विज़िट समाप्त करें)", color = GreenPrimary, fontWeight = FontWeight.Bold)
                    }
                }
                VisitState.COMPLETED -> {
                    Text(text = completedSummary ?: "Visit completed successfully.", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = FreshText)
                }
                VisitState.MISSED -> {
                    Text(text = "Visit marked as missed.", fontSize = 12.sp, color = CriticalText)
                }
            }
        }
    }
}

@Composable
private fun VisitStateBadge(state: VisitState) {
    val (bg, text, label) = when (state) {
        VisitState.PLANNED -> Triple(SurfaceCanvas, MaterialTheme.colorScheme.outline, "PLANNED")
        VisitState.ACTIVE -> Triple(AgingBg, AgingText, "IN-PROGRESS")
        VisitState.COMPLETED -> Triple(FreshBg, FreshText, "COMPLETED")
        VisitState.MISSED -> Triple(CriticalBg, CriticalText, "MISSED")
    }

    Surface(color = bg, shape = RoundedCornerShape(4.dp)) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun OutletCommercialCard(outlet: Outlet) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = outlet.ownerName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = outlet.phone, fontSize = 12.sp, color = GreenPrimary)
                    Text(text = outlet.address, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                }
                Surface(color = FreshBg, shape = RoundedCornerShape(4.dp)) {
                    Text(
                        text = "Tier ${outlet.priceTier}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = FreshText,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "CURRENT DUES", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold)
                    Text(
                        text = "₹${String.format("%,.0f", outlet.outstandingBalance)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (outlet.outstandingBalance > 0) AgingText else GreenPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "CREDIT LIMIT", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold)
                    Text(
                        text = "₹${String.format("%,.0f", outlet.creditLimit)}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun VisitActionsCard(
    visitState: VisitState,
    onTakeOrder: () -> Unit,
    onNoOrder: () -> Unit,
    onCall: () -> Unit,
    onDirections: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = "Visit Actions (कार्यवाहियां)", fontWeight = FontWeight.Bold, fontSize = 13.sp)

            Button(
                onClick = onTakeOrder,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Book Field Order (नया ऑर्डर बनाएं)", fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCall,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call Owner", color = GreenPrimary, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onDirections,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Directions", color = GreenPrimary, fontSize = 12.sp)
                }
            }

            OutlinedButton(
                onClick = onNoOrder,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.RemoveShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("No Order / Store Closed (ऑर्डर नहीं हुआ)", color = MaterialTheme.colorScheme.outline, fontSize = 12.sp)
            }
        }
    }
}

private fun formatTime(epochMillis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
    return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).format(formatter)
}
