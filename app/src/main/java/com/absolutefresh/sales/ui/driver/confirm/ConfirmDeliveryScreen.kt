package com.absolutefresh.sales.ui.driver.confirm

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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
import com.absolutefresh.sales.ui.theme.SaffronSecondary
import com.absolutefresh.sales.ui.theme.SurfaceCanvas
import com.absolutefresh.sales.ui.theme.SurfaceCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmDeliveryScreen(
    viewModel: ConfirmDeliveryViewModel,
    onNavigateBack: () -> Unit,
    onDeliveryConfirmed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        val state = uiState as? ConfirmDeliveryUiState.Ready
        if (state?.snackbarMessage != null) {
            snackbarHostState.showSnackbar(state.snackbarMessage)
            viewModel.dismissSnackbar()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Confirm Delivery",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = "वितरण पुष्टि • Proof of Delivery",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Offline Ready",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenPrimary)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            val state = uiState as? ConfirmDeliveryUiState.Ready
            if (state != null) {
                Surface(
                    color = SurfaceCard,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "NET INVOICE TOTAL",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = "₹${String.format("%,.2f", state.netPayable)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GreenPrimary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "COLLECTING",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = "₹${String.format("%,.2f", state.amountCollected)} (${state.paymentMode.name})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                viewModel.submitDelivery(onSuccess = onDeliveryConfirmed)
                            },
                            enabled = state.isValid && !state.isSubmitting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                        ) {
                            if (state.isSubmitting) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Logging Delivery...")
                            } else {
                                Icon(imageVector = Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Confirm & Complete Drop (वितरण दर्ज करें)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        if (!state.isValid) {
                            Spacer(modifier = Modifier.height(4.dp))
                            val reasonMsg = when {
                                state.isShortDelivery && state.shortfallReason.isBlank() -> "Shortfall reason required."
                                state.paymentMode == PaymentMode.CHEQUE && (state.chequeNumber.length < 6 || state.chequeBank.isBlank()) -> "Valid 6-digit cheque # & bank name required."
                                !state.isPodComplete -> "Proof of delivery (Signature, OTP, or Photo) required."
                                else -> "Please complete all required fields."
                            }
                            Text(
                                text = "⚠️ $reasonMsg",
                                fontSize = 11.sp,
                                color = CriticalText,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        when (val state = uiState) {
            is ConfirmDeliveryUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            }
            is ConfirmDeliveryUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = state.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(onClick = onNavigateBack) { Text("Back") }
                    }
                }
            }
            is ConfirmDeliveryUiState.Ready -> {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SurfaceCanvas)
                        .padding(innerPadding)
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Stop Header Card
                    StopHeaderCard(
                        delivery = state.delivery,
                        onCall = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${state.delivery.outletPhone}"))
                            context.startActivity(intent)
                        },
                        onOpenMap = {
                            val uri = Uri.parse("geo:0,0?q=${Uri.encode("${state.delivery.outletName}, ${state.delivery.outletAddress}")}")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        }
                    )

                    // Line Items & Delivery Stepper Card
                    DeliveredItemsCard(
                        state = state,
                        onAdjust = { viewModel.adjustPackets(it) },
                        onSetPackets = { viewModel.setPackets(it) }
                    )

                    // Shortfall Alert & Reason Selection (if delivered < ordered)
                    if (state.isShortDelivery) {
                        ShortfallReasonCard(
                            state = state,
                            onSelectReason = { viewModel.setShortfallReason(it) },
                            onCustomReasonChange = { viewModel.setCustomReason(it) }
                        )
                    }

                    // Payment & Billing Settlement Card
                    PaymentSettlementCard(
                        state = state,
                        onSelectMode = { viewModel.setPaymentMode(it) },
                        onAmountChange = { viewModel.setAmountCollected(it) },
                        onChequeChange = { num, bank -> viewModel.setChequeDetails(num, bank) },
                        onUpiTxnChange = { viewModel.setUpiTxnId(it) }
                    )

                    // Proof of Delivery (POD) Interactive Card
                    ProofOfDeliveryCard(
                        state = state,
                        onSelectMethod = { viewModel.setPodMethod(it) },
                        onReceiverNameChange = { viewModel.setReceiverName(it) },
                        onAddStroke = { viewModel.addSignatureStroke(it) },
                        onClearSignature = { viewModel.clearSignature() },
                        onOtpChange = { viewModel.setOtpCode(it) },
                        onVerifyOtp = { viewModel.verifyOtp() },
                        onCapturePhoto = { viewModel.capturePhoto() },
                        onRemovePhoto = { viewModel.removePhoto() }
                    )

                    // Driver Remarks & Notes
                    DriverNotesCard(
                        notes = state.driverNotes,
                        onNotesChange = { viewModel.setDriverNotes(it) }
                    )

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
private fun StopHeaderCard(
    delivery: DeliveryItem,
    onCall: () -> Unit,
    onOpenMap: () -> Unit
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
                        Text(
                            text = "Stop #${delivery.sequence}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FreshText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Territory: Lucknow East",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Surface(color = AgingBg, shape = RoundedCornerShape(4.dp)) {
                    Text(
                        text = "Pending Drop",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AgingText,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = delivery.outletName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = delivery.outletAddress, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onCall,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Call Store", color = GreenPrimary, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onOpenMap,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Directions", color = GreenPrimary, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun DeliveredItemsCard(
    state: ConfirmDeliveryUiState.Ready,
    onAdjust: (Int) -> Unit,
    onSetPackets: (Int) -> Unit
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
                Text(text = "Chilled Batter Line Items", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = "Invoice: ${state.delivery.invoiceNumber}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(color = SurfaceCanvas, shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Classic Idli & Dosa Batter (1 kg)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(
                            text = "Batch AF-2610 • 4°C Cold Chain • Exp: 7 Days",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${state.delivery.orderedPackets} pkts ordered",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${state.delivery.orderedWeightKg} kg total",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stepper and Delivered Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Delivered Packets (वितरित पैकेट)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(
                        text = if (state.isShortDelivery) "${state.delivery.orderedPackets - state.deliveredPackets} pkts short" else "Full allocation",
                        fontSize = 11.sp,
                        color = if (state.isShortDelivery) CriticalText else FreshText
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onAdjust(-1) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SurfaceCanvas)
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", tint = GreenPrimary)
                    }

                    Text(
                        text = "${state.deliveredPackets}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )

                    IconButton(
                        onClick = { onAdjust(1) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SurfaceCanvas)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = GreenPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Selection Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "Full (${state.delivery.orderedPackets})" to state.delivery.orderedPackets,
                    "20 pkts" to 20,
                    "15 pkts" to 15,
                    "10 pkts" to 10,
                    "0 (Refused)" to 0
                ).forEach { (label, count) ->
                    val isSelected = state.deliveredPackets == count
                    Surface(
                        color = if (isSelected) GreenPrimaryFixed else SurfaceCanvas,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSetPackets(count) }
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) GreenOnPrimaryFixed else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 6.dp),
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Delivery Fill Rate Meter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Delivery Fill Rate", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                Text(
                    text = "${state.fillPercentage}% (${state.deliveredPackets}/${state.delivery.orderedPackets})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (state.fillPercentage >= 100) GreenPrimary else AgingText
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { state.fillPercentage / 100f },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = if (state.fillPercentage >= 100) GreenPrimary else AgingText,
                trackColor = SurfaceCanvas
            )
        }
    }
}

@Composable
private fun ShortfallReasonCard(
    state: ConfirmDeliveryUiState.Ready,
    onSelectReason: (String) -> Unit,
    onCustomReasonChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CriticalBg.copy(alpha = 0.5f)),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CriticalText.copy(alpha = 0.4f)))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = CriticalText, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Short Delivery Notice (कम वितरण सूचना)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = CriticalText
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Shortfall of ${state.delivery.orderedPackets - state.deliveredPackets} packets detected. Standard operating procedure requires an audited reason.",
                fontSize = 11.sp,
                color = CriticalText
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Reason Chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                standardShortfallReasons.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        pair.forEach { reason ->
                            val isSelected = state.shortfallReason == reason
                            Surface(
                                color = if (isSelected) CriticalText else Color.White,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSelectReason(reason) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSelected) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = reason,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            if (state.shortfallReason.contains("Other")) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.customReason,
                    onValueChange = onCustomReasonChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Specify Custom Reason *") },
                    placeholder = { Text("e.g. Owner requested delivery tomorrow") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CriticalText)
                )
            }
        }
    }
}

@Composable
private fun PaymentSettlementCard(
    state: ConfirmDeliveryUiState.Ready,
    onSelectMode: (PaymentMode) -> Unit,
    onAmountChange: (Double) -> Unit,
    onChequeChange: (String, String) -> Unit,
    onUpiTxnChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "Payment & Settlement", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = "भुगतान विधि एवं राशि संकलन", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)

            Spacer(modifier = Modifier.height(10.dp))

            // Payment Mode FilterChips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf<Pair<PaymentMode, String>>(
                    PaymentMode.CASH to "Cash (नकद)",
                    PaymentMode.UPI_QR to "UPI / QR",
                    PaymentMode.CHEQUE to "Cheque",
                    PaymentMode.CREDIT_15D to "Credit (15D)"
                ).forEach { (mode, label) ->
                    val isSelected = state.paymentMode == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectMode(mode) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dynamic Mode View
            when (state.paymentMode) {
                PaymentMode.CASH -> {
                    Surface(color = FreshBg, shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Payments, contentDescription = null, tint = FreshText, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cash is immediately added to Driver's Handover Bag.",
                                fontSize = 11.sp,
                                color = FreshText
                            )
                        }
                    }
                }
                PaymentMode.UPI_QR -> {
                    Surface(color = SurfaceCanvas, shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.QrCode2, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "VPA: absolutefresh@icici", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Amount: ₹${String.format("%,.2f", state.netPayable)}", fontSize = 11.sp, color = GreenPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = state.upiTxnId,
                                onValueChange = onUpiTxnChange,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("UPI Reference / UTR Number (optional)") },
                                placeholder = { Text("e.g. 428901239842") },
                                singleLine = true
                            )
                        }
                    }
                }
                PaymentMode.CHEQUE -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.chequeNumber,
                            onValueChange = { onChequeChange(it, state.chequeBank) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Cheque Number (6 digits) *") },
                            placeholder = { Text("012345") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = state.chequeBank,
                            onValueChange = { onChequeChange(state.chequeNumber, it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Issuing Bank Name *") },
                            placeholder = { Text("e.g. State Bank of India") },
                            singleLine = true
                        )
                    }
                }
                PaymentMode.CREDIT_15D -> {
                    Surface(color = AgingBg, shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = AgingText, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "15-Day Authorized Credit Ledger. Net payable will be billed on cycle.",
                                fontSize = 11.sp,
                                color = AgingText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Amount Collected input field
            OutlinedTextField(
                value = "${state.amountCollected}",
                onValueChange = { onAmountChange(it.toDoubleOrNull() ?: 0.0) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Amount Collected (संग्रहित राशि ₹) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
            )

            // Quick amount buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = SurfaceCanvas,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.clickable { onAmountChange(state.netPayable) }
                ) {
                    Text(
                        text = "Full: ₹${String.format("%,.0f", state.netPayable)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = SurfaceCanvas,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.clickable { onAmountChange(0.0) }
                ) {
                    Text(
                        text = "Zero (₹0)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProofOfDeliveryCard(
    state: ConfirmDeliveryUiState.Ready,
    onSelectMethod: (PodMethod) -> Unit,
    onReceiverNameChange: (String) -> Unit,
    onAddStroke: (List<Offset>) -> Unit,
    onClearSignature: () -> Unit,
    onOtpChange: (String) -> Unit,
    onVerifyOtp: () -> Unit,
    onCapturePhoto: () -> Unit,
    onRemovePhoto: () -> Unit
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
                Column {
                    Text(text = "Proof of Delivery (POD)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "वितरण सत्यापन प्रमाण *", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                }

                if (state.isPodComplete) {
                    Surface(color = FreshBg, shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = FreshText, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Verified", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FreshText)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Method Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PodMethod.values().forEach { method ->
                    val isSelected = state.podMethod == method
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectMethod(method) },
                        label = { Text(method.labelEn, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (state.podMethod) {
                PodMethod.SIGNATURE -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.receiverName,
                            onValueChange = onReceiverNameChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Receiver / Owner Name (प्राप्तकर्ता का नाम) *") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
                        )

                        Text(
                            text = "Touch and sign inside the box below:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )

                        val currentStroke = remember { mutableStateListOf<Offset>() }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceCanvas)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            currentStroke.clear()
                                            currentStroke.add(offset)
                                        },
                                        onDrag = { change, _ ->
                                            currentStroke.add(change.position)
                                        },
                                        onDragEnd = {
                                            if (currentStroke.isNotEmpty()) {
                                                onAddStroke(currentStroke.toList())
                                                currentStroke.clear()
                                            }
                                        }
                                    )
                                }
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Draw completed strokes
                                state.signatureStrokes.forEach { stroke ->
                                    if (stroke.size > 1) {
                                        val path = Path().apply {
                                            moveTo(stroke.first().x, stroke.first().y)
                                            for (i in 1 until stroke.size) {
                                                lineTo(stroke[i].x, stroke[i].y)
                                            }
                                        }
                                        drawPath(
                                            path = path,
                                            color = Color.Black,
                                            style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                        )
                                    }
                                }
                                // Draw current stroke in progress
                                if (currentStroke.size > 1) {
                                    val path = Path().apply {
                                        moveTo(currentStroke.first().x, currentStroke.first().y)
                                        for (i in 1 until currentStroke.size) {
                                            lineTo(currentStroke[i].x, currentStroke[i].y)
                                        }
                                    }
                                    drawPath(
                                        path = path,
                                        color = Color.Black,
                                        style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                    )
                                }
                            }

                            if (state.signatureStrokes.isEmpty() && currentStroke.isEmpty()) {
                                Row(
                                    modifier = Modifier.align(Alignment.Center),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.Draw, contentDescription = null, tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Retailer Signature Here (दुकानदार हस्ताक्षर)",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            if (state.signatureStrokes.isNotEmpty()) {
                                TextButton(
                                    onClick = onClearSignature,
                                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                                ) {
                                    Text("Clear (मिटाएं)", fontSize = 11.sp, color = CriticalText)
                                }
                            }
                        }
                    }
                }

                PodMethod.OTP -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(color = SurfaceCanvas, shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "OTP sent to retailer mobile (${state.delivery.outletPhone}). Default test OTP is 4821.",
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = state.otpCode,
                                onValueChange = onOtpChange,
                                modifier = Modifier.weight(1f),
                                label = { Text("4-Digit OTP *") },
                                placeholder = { Text("4821") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
                            )

                            Button(
                                onClick = onVerifyOtp,
                                enabled = state.otpCode.length == 4,
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Verify")
                            }
                        }

                        if (state.isOtpVerified) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Customer OTP successfully verified", fontSize = 11.sp, color = GreenPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                PodMethod.PHOTO -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!state.isPhotoCaptured) {
                            Surface(
                                color = SurfaceCanvas,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                    .clickable { onCapturePhoto() }
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = "Tap to Capture Chiller Drop Photo", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text(text = "चिलर में रखी सामग्री की फोटो खींचें", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        } else {
                            Surface(
                                color = FreshBg,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = FreshText, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(text = "Chiller Photo Attached", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = FreshText)
                                            Text(
                                                text = "${state.photoTimestamp} • GPS: Lucknow East",
                                                fontSize = 10.sp,
                                                color = FreshText.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                    TextButton(onClick = onRemovePhoto) {
                                        Text("Retake", fontSize = 11.sp, color = CriticalText)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverNotesCard(
    notes: String,
    onNotesChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "Driver Delivery Remarks", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(text = "वितरण संबंधी टिप्पणी (optional)", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Given to retailer nephew, placed on top chiller rack") },
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
            )
        }
    }
}
