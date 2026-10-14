package com.absolutefresh.sales.ui.stock

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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.absolutefresh.sales.data.model.PaymentMode
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
import com.absolutefresh.sales.ui.theme.SaffronSecondary
import com.absolutefresh.sales.ui.theme.SurfaceCanvas
import com.absolutefresh.sales.ui.theme.SurfaceCard

@Composable
fun StockScreen(
    viewModel: StockViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Box(modifier = modifier.fillMaxSize().background(SurfaceCanvas)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Batch Banner
            FreshBatchHeroBanner(uiState = uiState)

            // Van Metric Bento 3-Grid
            VanMetricBentoGrid(
                loaded = uiState.totalMorningLoaded,
                delivered = uiState.totalDelivered,
                balance = uiState.totalBalance
            )

            // Live Chilled Stock SKU Breakdown
            LiveChilledSkuCard(skus = uiState.skuBreakdown)

            // FIFO Shelf-Life Urgent Notice
            FifoShelfLifeBanner(urgentBatch = uiState.fifoUrgentBatch)

            // Store Settlement Action Card
            StoreSettlementCard(
                state = uiState.settlementState,
                onDropQtyChange = { viewModel.adjustDropQty(it) },
                onExpiredReturnChange = { viewModel.adjustExpiredReturnQty(it) },
                onSelectPaymentMode = { viewModel.setPaymentMode(it) },
                onExecuteSettlement = { viewModel.executeSettlement() },
                onShareWhatsApp = {
                    val settle = uiState.settlementState
                    val shareMsg = "Absolute Fresh Invoice ${settle.generatedInvoiceNumber ?: "AFF-2610-018"}\n" +
                            "Store: ${settle.outletName}\n" +
                            "Delivered: ${settle.packetsDropped} pkts Chilled Batter\n" +
                            "Expiry Returns: ${settle.expiredReturned} pkts (Credit ₹${settle.expiryCreditAmount.toInt()})\n" +
                            "Total Payable: ₹${String.format("%,.0f", settle.netPayable)}\n" +
                            "Thank you for choosing freshly fermented batter!"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(shareMsg)}"))
                    context.startActivity(intent)
                }
            )

            // Geofence Pill
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "GPS Geofence: 12m from ${uiState.settlementState.outletName} • 100% Signal",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        // Delight Toast Notification on Settlement
        if (uiState.showSuccessToast) {
            Surface(
                color = MaterialTheme.colorScheme.inverseSurface,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = GreenPrimaryFixed, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Settlement Logged", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.inverseOnSurface, fontSize = 13.sp)
                        Text(text = uiState.toastMessage, fontSize = 11.sp, color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.8f))
                    }
                    TextButton(onClick = { viewModel.dismissToast() }) {
                        Text("OK", color = GreenPrimaryFixed, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FreshBatchHeroBanner(uiState: StockUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GreenPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, tint = GreenPrimaryFixed, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${uiState.vanRouteId.uppercase()} • ${uiState.territory.uppercase()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimaryFixed,
                        letterSpacing = 0.5.sp
                    )
                }

                Surface(
                    color = GreenPrimaryFixed,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(GreenPrimary)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Freshness 10/10 Days",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenOnPrimaryFixed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = uiState.batchCode,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Active Load",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "Mfd: ${uiState.mfdDate} • Exp: ${uiState.expDate} (Natural Fermentation)",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Inventory Depletion Meter
            val total = (uiState.totalMorningLoaded).coerceAtLeast(1)
            val deliveredPct = ((uiState.totalDelivered.toFloat() / total) * 100).toInt()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Van Inventory Depletion", fontSize = 11.sp, color = Color.White)
                Text(text = "$deliveredPct% Dispatched", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { uiState.totalDelivered.toFloat() / total },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = GreenPrimaryFixed,
                trackColor = Color.White.copy(alpha = 0.25f)
            )
        }
    }
}

@Composable
private fun VanMetricBentoGrid(
    loaded: Int,
    delivered: Int,
    balance: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BentoMetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Inventory2,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = "LOADED",
            value = "$loaded",
            subtitle = "Morning Gate"
        )
        BentoMetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.TaskAlt,
            iconColor = GreenPrimary,
            title = "DELIVERED",
            value = "$delivered",
            subtitle = "7 Stores Done"
        )
        BentoMetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Inbox,
            iconColor = SaffronSecondary,
            title = "BALANCE",
            value = "$balance",
            subtitle = "In Crates"
        )
    }
}

@Composable
private fun BentoMetricCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    value: String,
    subtitle: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun LiveChilledSkuCard(skus: List<SkuStockItem>) {
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
                Text(text = "Live Chilled Stock SKU Breakdown", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Surface(color = SurfaceCanvas, shape = RoundedCornerShape(12.dp)) {
                    Text(
                        text = "4°C Chiller",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                skus.forEach { sku ->
                    Surface(
                        color = SurfaceCanvas,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = sku.name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "${sku.packetsRemaining}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                            Text(text = "pkts left", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FifoShelfLifeBanner(urgentBatch: String?) {
    urgentBatch ?: return

    Surface(
        color = CriticalBg,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(CriticalText.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.HourglassTop, contentDescription = null, tint = CriticalText, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "FIFO Shelf-Life Urgent Notice", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CriticalText)
                    Surface(color = CriticalText, shape = RoundedCornerShape(4.dp)) {
                        Text(text = "Day 8 Decay", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                    }
                }
                Text(
                    text = "Store shelf contains $urgentBatch requiring urgent return or swap.",
                    fontSize = 11.sp,
                    color = CriticalText
                )
            }
        }
    }
}

@Composable
private fun StoreSettlementCard(
    state: StoreSettlementState,
    onDropQtyChange: (Int) -> Unit,
    onExpiredReturnChange: (Int) -> Unit,
    onSelectPaymentMode: (PaymentMode) -> Unit,
    onExecuteSettlement: () -> Unit,
    onShareWhatsApp: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(FreshBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Storefront, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = state.outletName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(color = FreshBg, shape = RoundedCornerShape(4.dp)) {
                                Text(text = "Stop #${state.stopNumber}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FreshText, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                        }
                        Text(text = "${state.outletAddress} • GSTIN: ${state.gstin}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }

                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${state.phone}"))
                        context.startActivity(intent)
                    }
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = GreenPrimary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Key-Value Settlement Ledger
            Surface(
                color = SurfaceCanvas,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Dropped
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(GreenPrimary))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Packets Dropped to Chiller", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { onDropQtyChange(-5) }, modifier = Modifier.size(24.dp)) {
                                Text("-", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Text(text = "${state.packetsDropped}", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 6.dp))
                            IconButton(onClick = { onDropQtyChange(5) }, modifier = Modifier.size(24.dp)) {
                                Text("+", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }

                    // Expired return
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(CriticalText))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(text = "Expired Stock Pickup", fontSize = 12.sp, color = CriticalText, fontWeight = FontWeight.Medium)
                                Text(text = "2x 500g (Batch AF-011026)", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                        Text(text = "- ${state.expiredReturned} pkts", fontWeight = FontWeight.Bold, color = CriticalText, fontSize = 12.sp)
                    }

                    HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)

                    // Net Payable
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "NET PAYABLE FOR SHOP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                            Text(text = "Gross ₹${String.format("%,.0f", state.grossAmount)} − ₹${String.format("%,.0f", state.expiryCreditAmount)} Expiry Credit", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        }
                        Text(
                            text = "₹${String.format("%,.0f", state.netPayable)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Payment Mode Selector
            Text(text = "PAYMENT MODE COLLECTED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PaymentChip(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.QrCodeScanner,
                    label = "UPI / QR",
                    isSelected = state.selectedPaymentMode == PaymentMode.UPI_QR,
                    onClick = { onSelectPaymentMode(PaymentMode.UPI_QR) }
                )
                PaymentChip(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Payments,
                    label = "Cash",
                    isSelected = state.selectedPaymentMode == PaymentMode.CASH,
                    onClick = { onSelectPaymentMode(PaymentMode.CASH) }
                )
                PaymentChip(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    label = "Ledger 15D",
                    isSelected = state.selectedPaymentMode == PaymentMode.CREDIT_15D,
                    onClick = { onSelectPaymentMode(PaymentMode.CREDIT_15D) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Settle Button
            Button(
                onClick = onExecuteSettlement,
                enabled = !state.isProcessing && !state.isSettled,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                if (state.isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Processing GST Invoice...")
                } else if (state.isSettled) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = GreenPrimaryFixed, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Settled (${state.generatedInvoiceNumber})", fontWeight = FontWeight.Bold)
                } else {
                    Icon(imageVector = Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Settle & Generate GST Invoice", fontWeight = FontWeight.Bold)
                }
            }

            // WhatsApp Share Button
            if (state.isSettled) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onShareWhatsApp,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share E-Receipt to WhatsApp", color = GreenPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun PaymentChip(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) GreenPrimaryFixed else SurfaceCanvas,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) GreenOnPrimaryFixed else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) GreenOnPrimaryFixed else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
