package com.absolutefresh.sales.ui.invoice

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.absolutefresh.sales.data.model.PaymentMode
import com.absolutefresh.sales.ui.theme.AgingBg
import com.absolutefresh.sales.ui.theme.AgingText
import com.absolutefresh.sales.ui.theme.BorderSubtle
import com.absolutefresh.sales.ui.theme.FreshBg
import com.absolutefresh.sales.ui.theme.FreshText
import com.absolutefresh.sales.ui.theme.GreenPrimary
import com.absolutefresh.sales.ui.theme.SurfaceCanvas
import com.absolutefresh.sales.ui.theme.SurfaceCard
import com.absolutefresh.sales.ui.theme.TextPrimary
import com.absolutefresh.sales.ui.theme.TextSecondary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    viewModel: InvoiceViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Tax Invoice",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "कर चालान (GST बिल)",
                            fontSize = 12.sp,
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary
                )
            )
        },
        bottomBar = {
            if (uiState is InvoiceUiState.Success) {
                val success = uiState as InvoiceUiState.Success
                Surface(
                    color = SurfaceCard,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "Close", fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, success.shareText)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share Invoice Receipt")
                                context.startActivity(shareIntent)
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(50.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Share Receipt",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SurfaceCanvas)
        ) {
            when (val state = uiState) {
                is InvoiceUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                }
                is InvoiceUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "Unable to load invoice",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = state.message, color = TextSecondary, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadInvoice() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is InvoiceUiState.Success -> {
                    InvoiceSuccessContent(state = state)
                }
            }
        }
    }
}

@Composable
private fun InvoiceSuccessContent(state: InvoiceUiState.Success) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Status Alert Card
        if (state.isProvisional) {
            Surface(
                color = AgingBg,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AgingText.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = AgingText,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Provisional Invoice (अस्थायी चालान)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AgingText
                        )
                        Text(
                            text = "Ref: ${state.invoiceNumber} • Final invoice number AFF-YYMM-NNN will be assigned upon server sync.",
                            fontSize = 12.sp,
                            color = AgingText.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        } else {
            Surface(
                color = FreshBg,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, FreshText.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = FreshText,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Verified GST Invoice (सत्यापित चालान)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = FreshText
                        )
                        Text(
                            text = "Issued by Absolute Fresh ERP • Invoice ${state.invoiceNumber}",
                            fontSize = 12.sp,
                            color = FreshText.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        // Paper-Style Invoice Document Card
        Surface(
            color = SurfaceCard,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Seller Brand Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = GreenPrimary,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Receipt,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Absolute Fresh",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = GreenPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.sellerName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "FSSAI Lic. No: ${state.sellerFssai}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "GSTIN: ${state.sellerGstin}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            color = if (state.isProvisional) AgingBg else FreshBg,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (state.isProvisional) "PROVISIONAL" else "ORIGINAL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (state.isProvisional) AgingText else FreshText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = state.invoiceNumber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = state.invoiceDateFormatted,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = BorderSubtle)
                Spacer(modifier = Modifier.height(14.dp))

                // Billed To Section
                Text(
                    text = "Billed To (खरीदार विवरण)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.outletName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "${state.outletOwner} • ${state.outletPhone}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "${state.outletAddress}, ${state.outletArea}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        if (state.outletGstin != null) {
                            Text(
                                text = "Buyer GSTIN: ${state.outletGstin}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Surface(
                        color = SurfaceCanvas,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                    ) {
                        Text(
                            text = "Tier ${state.priceTier}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = BorderSubtle)
                Spacer(modifier = Modifier.height(14.dp))

                // Itemized Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceCanvas, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Item (उत्पाद)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.weight(2f)
                    )
                    Text(
                        text = "Qty",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(0.8f)
                    )
                    Text(
                        text = "Rate",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Amount",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1.2f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Line Items
                state.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(2f)) {
                            Text(
                                text = item.productName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "HSN: ${item.hsnCode} • ${item.weightKg} kg",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                        Text(
                            text = "${item.quantityPackets}",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(0.8f),
                            color = TextPrimary
                        )
                        Text(
                            text = "₹${String.format(Locale.ENGLISH, "%.2f", item.unitRate)}",
                            fontSize = 12.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f),
                            color = TextPrimary
                        )
                        Text(
                            text = "₹${String.format(Locale.ENGLISH, "%.2f", item.taxableAmount)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1.2f),
                            color = TextPrimary
                        )
                    }
                    HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Financial Breakdown
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Taxable Subtotal (${state.totalPackets} pkts • ${state.totalWeightKg} kg)",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "₹${String.format(Locale.ENGLISH, "%.2f", state.subtotal)}",
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "CGST (2.5%)",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "₹${String.format(Locale.ENGLISH, "%.2f", state.cgstTotal)}",
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "SGST (2.5%)",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "₹${String.format(Locale.ENGLISH, "%.2f", state.sgstTotal)}",
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = BorderSubtle, thickness = 1.5.dp)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Grand Total (कुल राशि)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Incl. all taxes",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Text(
                            text = "₹${String.format(Locale.ENGLISH, "%.2f", state.grandTotal)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = GreenPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Payment Mode Tag
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceCanvas, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Payment Mode:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Text(
                            text = state.paymentStatusText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state.paymentMode == PaymentMode.CREDIT_15D) AgingText else GreenPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BorderSubtle)
                Spacer(modifier = Modifier.height(12.dp))

                // Footer Notice
                Text(
                    text = "Pure Natural Fermentation • Zero Preservatives • Keep Chilled 0-4°C\nThis is a system generated document issued by Absolute Fresh Field Operations.",
                    fontSize = 10.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    lineHeight = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
