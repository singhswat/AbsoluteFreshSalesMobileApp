package com.absolutefresh.sales.ui.more

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.absolutefresh.sales.data.model.CashTransaction
import com.absolutefresh.sales.data.model.SyncStatus
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
fun MoreScreen(
    viewModel: MoreViewModel,
    modifier: Modifier = Modifier,
    onSignOut: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceCanvas)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // User Profile Header
        ProfileCard(
            name = uiState.repName,
            phone = uiState.repPhone,
            territory = uiState.repTerritory,
            language = uiState.selectedLanguage,
            onToggleLanguage = { viewModel.toggleLanguage() }
        )

        // Sync Health Widget (GAC 11)
        SyncHealthCard(
            syncHealth = uiState.syncHealth,
            onSyncClick = { viewModel.triggerManualSync() },
            onDetailsClick = { viewModel.openSyncDetails() }
        )

        // Cash Currently Held Card (FS-03)
        CashHeldCard(
            cashInHand = uiState.cashInHand,
            recentTransactions = uiState.recentCashTransactions,
            onRecordCashClick = { viewModel.openRecordCash() }
        )

        // Operations & Shortcuts Card
        OperationsCard(
            onOpenHolidays = { viewModel.openHolidays() },
            onOpenBeatChange = { /* Suggest Beat Change stub */ }
        )

        // Sign Out Section
        OutlinedButton(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CriticalText)
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = CriticalText)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out (लॉग आउट)", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Record Cash Bottom Sheet
    if (uiState.isRecordCashOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeRecordCash() },
            sheetState = sheetState,
            containerColor = SurfaceCard
        ) {
            RecordCashSheet(
                onSave = { outletId, outletName, amount, invoiceRef ->
                    viewModel.recordCashReceipt(outletId, outletName, amount, invoiceRef)
                },
                onClose = { viewModel.closeRecordCash() }
            )
        }
    }

    // Holiday Calendar Bottom Sheet
    if (uiState.isHolidaysOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeHolidays() },
            sheetState = sheetState,
            containerColor = SurfaceCard
        ) {
            HolidayCalendarSheet(
                holidays = uiState.upcomingHolidays,
                onClose = { viewModel.closeHolidays() }
            )
        }
    }

    // Sync Details Bottom Sheet
    if (uiState.isSyncDetailsOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeSyncDetails() },
            sheetState = sheetState,
            containerColor = SurfaceCard
        ) {
            SyncDetailsSheet(
                syncHealth = uiState.syncHealth,
                onRetryNow = { viewModel.triggerManualSync() },
                onClose = { viewModel.closeSyncDetails() }
            )
        }
    }
}

@Composable
private fun ProfileCard(
    name: String,
    phone: String,
    territory: String,
    language: String,
    onToggleLanguage: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(FreshBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(36.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = "$territory • $phone", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
            }

            Surface(
                color = SurfaceCanvas,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.clickable(onClick = onToggleLanguage)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = language, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GreenPrimary)
                }
            }
        }
    }
}

@Composable
private fun SyncHealthCard(
    syncHealth: SyncHealth,
    onSyncClick: () -> Unit,
    onDetailsClick: () -> Unit
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
                    Icon(
                        imageVector = if (syncHealth.pendingCount == 0) Icons.Default.CloudDone else Icons.Default.Sync,
                        contentDescription = null,
                        tint = if (syncHealth.pendingCount == 0) GreenPrimary else AgingText,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "Sync & Offline Health", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = if (syncHealth.pendingCount == 0) "All field data synced" else "${syncHealth.pendingCount} record(s) queued offline",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                if (syncHealth.isSyncing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = GreenPrimary)
                } else {
                    OutlinedButton(
                        onClick = onSyncClick,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = ButtonDefaults.ContentPadding
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp), tint = GreenPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync Now", fontSize = 11.sp, color = GreenPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onDetailsClick),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Last synced: ${formatTime(syncHealth.lastSyncTime)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "View Outbox", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(14.dp), tint = GreenPrimary)
                }
            }
        }
    }
}

@Composable
private fun CashHeldCard(
    cashInHand: Double,
    recentTransactions: List<CashTransaction>,
    onRecordCashClick: () -> Unit
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
                Column {
                    Text(text = "Cash Currently Held", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = "₹${String.format("%,.2f", cashInHand)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                }

                Button(
                    onClick = onRecordCashClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Collect Cash", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (recentTransactions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Recent Cash Receipts", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(6.dp))

                recentTransactions.take(3).forEach { tx ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = tx.outletName, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text(text = "${tx.invoiceRef} • ${formatTime(tx.timestamp)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        }
                        Text(
                            text = "+₹${String.format("%,.0f", tx.amount)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OperationsCard(
    onOpenHolidays: () -> Unit,
    onOpenBeatChange: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            OperationRow(
                icon = Icons.Default.CalendarMonth,
                title = "Holiday Calendar (अवकाश कैलेंडर)",
                subtitle = "Official field sales holidays & closures",
                onClick = onOpenHolidays
            )
            HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
            OperationRow(
                icon = Icons.Default.Route,
                title = "Suggest Beat Change (रूट सुझाव)",
                subtitle = "Propose store reassignment or route tweak",
                onClick = onOpenBeatChange
            )
            HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
            OperationRow(
                icon = Icons.Default.Info,
                title = "App Info & Diagnostics",
                subtitle = "v1.0.0 (Native Android Compose • Offline Durable)",
                onClick = {}
            )
        }
    }
}

@Composable
private fun OperationRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(imageVector = icon, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
            }
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun RecordCashSheet(
    onSave: (outletId: String, outletName: String, amount: Double, invoiceRef: String) -> Unit,
    onClose: () -> Unit
) {
    var outletName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var invoiceRef by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
        Text(text = "Record Cash Received", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = "नकद भुगतान दर्ज करें (Immediate Receipt)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = outletName,
            onValueChange = { outletName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Outlet Name (दुकान का नाम) *") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Amount Collected (₹) *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
        )

        // Quick amount chips
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(500, 1000, 2000, 5000).forEach { amt ->
                Surface(
                    color = SurfaceCanvas,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.clickable { amountText = amt.toString() }
                ) {
                    Text(
                        text = "+₹$amt",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = invoiceRef,
            onValueChange = { invoiceRef = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Invoice Reference (optional)") },
            placeholder = { Text("e.g. AFF-2610-0101") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
        )

        Spacer(modifier = Modifier.height(18.dp))

        val amountVal = amountText.toDoubleOrNull() ?: 0.0
        val isValid = outletName.isNotBlank() && amountVal > 0.0

        Button(
            onClick = {
                onSave("o-manual", outletName.trim(), amountVal, invoiceRef.trim())
            },
            enabled = isValid,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Confirm Receipt (भुगतान सहेजें)", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun HolidayCalendarSheet(
    holidays: List<Holiday>,
    onClose: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
        Text(text = "Holiday Calendar 2026", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = "लखनऊ क्षेत्र अवकाश सूची", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(holidays) { holiday ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCanvas)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = holiday.nameEn, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = holiday.nameHi, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text(text = holiday.date, fontSize = 10.sp, color = GreenPrimary, fontWeight = FontWeight.SemiBold)
                        }
                        Surface(color = FreshBg, shape = RoundedCornerShape(4.dp)) {
                            Text(text = holiday.type, fontSize = 10.sp, color = FreshText, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        TextButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
            Text("Close", color = GreenPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SyncDetailsSheet(
    syncHealth: SyncHealth,
    onRetryNow: () -> Unit,
    onClose: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
        Text(text = "Offline Outbox & Sync Health", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = "GAC-11 Sync Diagnostics (Room Outbox Engine)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)

        Spacer(modifier = Modifier.height(14.dp))

        Surface(color = SurfaceCanvas, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SyncDiagRow("Connection State:", "Online (Local Mock/Staging)")
                SyncDiagRow("Queued Offline Records:", "${syncHealth.pendingCount}")
                SyncDiagRow("Failed Transactions:", "${syncHealth.failedCount}")
                SyncDiagRow("Last Successful Sync:", formatTime(syncHealth.lastSyncTime))
                SyncDiagRow("Sync Protocol:", "Client UUID Idempotent Replay")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRetryNow,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Flush & Sync All Now", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
            Text("Close", color = GreenPrimary)
        }
    }
}

@Composable
private fun SyncDiagRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun formatTime(epochMillis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
    return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).format(formatter)
}
