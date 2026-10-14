package com.absolutefresh.sales.ui.driver

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.absolutefresh.sales.data.model.PaymentMode
import com.absolutefresh.sales.data.repository.DeliveryItem
import com.absolutefresh.sales.ui.theme.AgingBg
import com.absolutefresh.sales.ui.theme.AgingText
import com.absolutefresh.sales.ui.theme.BorderSubtle
import com.absolutefresh.sales.ui.theme.CriticalBg
import com.absolutefresh.sales.ui.theme.CriticalText
import com.absolutefresh.sales.ui.theme.FreshBg
import com.absolutefresh.sales.ui.theme.FreshText
import com.absolutefresh.sales.ui.theme.GreenOnPrimaryFixed
import com.absolutefresh.sales.ui.theme.GreenPrimary
import com.absolutefresh.sales.ui.theme.GreenPrimaryFixed
import com.absolutefresh.sales.ui.theme.SurfaceCanvas
import com.absolutefresh.sales.ui.theme.SurfaceCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveriesScreen(
    viewModel: DeliveriesViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceCanvas)
    ) {
        when (val state = uiState) {
            is DeliveriesUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            }
            is DeliveriesUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is DeliveriesUiState.Populated -> {
                // Driver Route Header
                DriverRouteHeader(
                    completedCount = state.totalCompletedCount,
                    totalCount = state.totalPlannedCount,
                    totalCash = state.totalCollectedCash
                )

                if (state.pendingDeliveries.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                            Icon(imageVector = Icons.Default.TaskAlt, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "All Deliveries Completed!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(text = "Check 'Done Today' tab for completed delivery logs.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.pendingDeliveries, key = { it.id }) { delivery ->
                            PendingDeliveryCard(
                                delivery = delivery,
                                onConfirmClick = { viewModel.openConfirmDelivery(delivery) },
                                onCallClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${delivery.outletPhone}"))
                                    context.startActivity(intent)
                                },
                                onDirectionsClick = {
                                    val uri = Uri.parse("geo:0,0?q=${Uri.encode("${delivery.outletName}, ${delivery.outletAddress}")}")
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    context.startActivity(intent)
                                }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                // Confirm Delivery Bottom Sheet
                if (state.confirmState.isOpen) {
                    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeConfirmDelivery() },
                        sheetState = sheetState,
                        containerColor = SurfaceCard
                    ) {
                        ConfirmDeliverySheet(
                            state = state.confirmState,
                            onUpdatePackets = { viewModel.updateDeliveredPackets(it) },
                            onUpdateAmount = { viewModel.updateAmountCollected(it) },
                            onUpdatePaymentMode = { viewModel.updatePaymentMode(it) },
                            onUpdatePodMethod = { viewModel.updatePodMethod(it) },
                            onUpdateShortReason = { viewModel.updateShortDeliveryReason(it) },
                            onSubmit = { viewModel.submitConfirmDelivery() },
                            onClose = { viewModel.closeConfirmDelivery() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverRouteHeader(
    completedCount: Int,
    totalCount: Int,
    totalCash: Double
) {
    Surface(color = SurfaceCard, shadowElevation = 1.dp) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Driver Deliveries", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(text = "Route #04 • Lucknow East Territory", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }

                Surface(color = FreshBg, shape = RoundedCornerShape(12.dp)) {
                    Text(
                        text = "Cash: ₹${String.format("%,.0f", totalCash)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = FreshText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val progressPct = if (totalCount > 0) ((completedCount.toFloat() / totalCount) * 100).toInt() else 0
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Route Progress ($completedCount of $totalCount Stops)", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                Text(text = "$progressPct%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { if (totalCount > 0) completedCount.toFloat() / totalCount else 0f },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = GreenPrimary,
                trackColor = SurfaceCanvas
            )
        }
    }
}

@Composable
private fun PendingDeliveryCard(
    delivery: DeliveryItem,
    onConfirmClick: () -> Unit,
    onCallClick: () -> Unit,
    onDirectionsClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = FreshBg, shape = RoundedCornerShape(4.dp)) {
                        Text(text = "Stop #${delivery.sequence}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FreshText, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = delivery.outletName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Surface(color = AgingBg, shape = RoundedCornerShape(4.dp)) {
                    Text(text = "Pending Drop", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AgingText, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = delivery.outletAddress, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "${delivery.orderedPackets} pkts (${delivery.orderedWeightKg} kg)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(text = "Inv: ${delivery.invoiceNumber}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "₹${String.format("%,.2f", delivery.invoiceAmount)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                    Text(text = delivery.paymentMode.name, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onCallClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call", color = GreenPrimary, fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onDirectionsClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Maps", color = GreenPrimary, fontSize = 12.sp)
                }
                Button(
                    onClick = onConfirmClick,
                    modifier = Modifier.weight(2f),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Confirm Drop", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ConfirmDeliverySheet(
    state: ConfirmDeliveryDialogState,
    onUpdatePackets: (Int) -> Unit,
    onUpdateAmount: (Double) -> Unit,
    onUpdatePaymentMode: (PaymentMode) -> Unit,
    onUpdatePodMethod: (String) -> Unit,
    onUpdateShortReason: (String) -> Unit,
    onSubmit: () -> Unit,
    onClose: () -> Unit
) {
    val delivery = state.delivery ?: return
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Confirm Delivery", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = delivery.outletName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Order Summary Box
        Surface(color = SurfaceCanvas, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Ordered Quantity: ${delivery.orderedPackets} pkts (${delivery.orderedWeightKg} kg)", fontSize = 12.sp)
                Text(text = "Invoice Amount: ₹${String.format("%,.2f", delivery.invoiceAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Delivered Packets Input
        OutlinedTextField(
            value = "${state.deliveredPackets}",
            onValueChange = { onUpdatePackets(it.toIntOrNull() ?: 0) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Delivered Packets (वितरित पैकेट) *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
        )

        // Short delivery warning & reason field (FS-06 AC2)
        if (state.isShortDelivery) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(color = AgingBg, shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Short delivery detected (${state.deliveredPackets} < ${delivery.orderedPackets} ordered). Reason required by field policy.",
                    fontSize = 11.sp,
                    color = AgingText,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.shortDeliveryReason,
                onValueChange = onUpdateShortReason,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Short Delivery Reason *") },
                placeholder = { Text("e.g. Retailer chiller full / 5 pkts rejected") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Amount Collected
        OutlinedTextField(
            value = "${state.amountCollected}",
            onValueChange = { onUpdateAmount(it.toDoubleOrNull() ?: 0.0) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Amount Collected (₹) *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Payment Mode
        Text(text = "Payment Mode", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(PaymentMode.CASH, PaymentMode.UPI_QR, PaymentMode.CREDIT_15D).forEach { mode ->
                val selected = state.paymentMode == mode
                FilterChip(
                    selected = selected,
                    onClick = { onUpdatePaymentMode(mode) },
                    label = { Text(mode.name) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GreenPrimary, selectedLabelColor = Color.White)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Proof of Delivery (POD) Slot
        Text(text = "Proof of Delivery (POD)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("OTP Code", "Signature", "Photo").forEach { method ->
                val selected = state.podMethod == method
                FilterChip(
                    selected = selected,
                    onClick = { onUpdatePodMethod(method) },
                    label = { Text(method) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GreenPrimary, selectedLabelColor = Color.White)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onSubmit,
            enabled = state.isValid && !state.isSubmitting,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
            } else {
                Text("Confirm & Log Delivery (पुष्टि करें)", fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
