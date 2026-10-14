package com.absolutefresh.sales.ui.orders

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Storefront
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.absolutefresh.sales.data.model.FieldOrder
import com.absolutefresh.sales.data.model.Outlet
import com.absolutefresh.sales.data.model.PaymentMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BrandGreen = Color(0xFF1B5E20)
private val BrandGreenLight = Color(0xFFE8F5E9)
private val BrandAmber = Color(0xFFE65100)
private val SurfaceGray = Color(0xFFF8F9FA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewOrderScreen(
    viewModel: NewOrderViewModel,
    onNavigateBack: () -> Unit,
    onOrderPlaced: (FieldOrder) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Book Order",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "नया ऑर्डर दर्ज करें",
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
                    containerColor = BrandGreen
                )
            )
        },
        bottomBar = {
            if (state is NewOrderUiState.Populated) {
                OrderBottomBar(
                    state = state as NewOrderUiState.Populated,
                    onSubmit = {
                        viewModel.submitOrder(onSuccess = onOrderPlaced)
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(SurfaceGray)
        ) {
            when (val currentState = state) {
                is NewOrderUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = BrandGreen
                    )
                }
                is NewOrderUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentState.message,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadData() },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                        ) {
                            Text("Retry / पुनः प्रयास")
                        }
                    }
                }
                is NewOrderUiState.Submitted -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = BrandGreen,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Order Placed Successfully!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandGreen
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Provisional Ref: ${currentState.order.provisionalInvoiceNumber}",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { onOrderPlaced(currentState.order) },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                        ) {
                            Text("View Orders / ऑर्डर देखें")
                        }
                    }
                }
                is NewOrderUiState.Populated -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            OutletHeaderCard(outlet = currentState.outlet)
                        }

                        item {
                            Text(
                                text = "Select Products & Quantities / उत्पाद चुनें",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF263238),
                                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                            )
                        }

                        items(currentState.items, key = { it.product.id }) { item ->
                            ProductOrderItemCard(
                                item = item,
                                onIncrement = { viewModel.onQuantityChanged(item.product.id, 1) },
                                onDecrement = { viewModel.onQuantityChanged(item.product.id, -1) }
                            )
                        }

                        item {
                            OrderOptionsCard(
                                state = currentState,
                                onPaymentModeSelected = { viewModel.onPaymentModeSelected(it) },
                                onNotesChanged = { viewModel.onNotesChanged(it) }
                            )
                        }

                        // Extra bottom spacing to scroll past bottom bar
                        item {
                            Spacer(modifier = Modifier.height(90.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OutletHeaderCard(outlet: Outlet) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = BrandGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = outlet.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${outlet.area} • Ph: ${outlet.phone}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BrandGreenLight
                ) {
                    Text(
                        text = "Tier ${outlet.priceTier}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandGreen,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Outstanding Balance", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = "₹${"%.2f".format(outlet.outstandingBalance)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (outlet.outstandingBalance > 0) BrandAmber else Color(0xFF2E7D32)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Credit Limit", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = "₹${"%.2f".format(outlet.creditLimit)}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color(0xFF37474F)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductOrderItemCard(
    item: OrderProductItemUiState,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val isSelected = item.quantity > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) {
                    Modifier.border(1.5.dp, BrandGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFF9FDF9) else Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1F2937)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "₹${"%.2f".format(item.unitPrice)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = BrandGreen
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MRP ₹${"%.0f".format(item.product.mrp)}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textDecoration = TextDecoration.LineThrough
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFF0F4F8)
                    ) {
                        Text(
                            text = "${item.product.packWeightGrams}g",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (isSelected) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Subtotal: ₹${"%.2f".format(item.lineTotal)} (${"%.1f".format(item.lineWeightKg)} kg)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = BrandGreen
                    )
                }
            }

            // Stepper controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onDecrement,
                    enabled = item.quantity > 0,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (item.quantity > 0) Color(0xFFEEEEEE) else Color(0xFFF5F5F5)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease",
                        tint = if (item.quantity > 0) Color(0xFF333333) else Color.LightGray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "${item.quantity}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (item.quantity > 0) BrandGreen else Color.Gray,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                IconButton(
                    onClick = onIncrement,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BrandGreen)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderOptionsCard(
    state: NewOrderUiState.Populated,
    onPaymentModeSelected: (PaymentMode) -> Unit,
    onNotesChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Payment Mode / भुगतान का तरीका",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PaymentChip(
                    label = "Cash",
                    icon = Icons.Default.Payments,
                    isSelected = state.paymentMode == PaymentMode.CASH,
                    onSelect = { onPaymentModeSelected(PaymentMode.CASH) }
                )
                PaymentChip(
                    label = "UPI QR",
                    icon = Icons.Default.QrCode,
                    isSelected = state.paymentMode == PaymentMode.UPI_QR,
                    onSelect = { onPaymentModeSelected(PaymentMode.UPI_QR) }
                )
                PaymentChip(
                    label = "15D Credit",
                    icon = Icons.Default.CreditCard,
                    isSelected = state.paymentMode == PaymentMode.CREDIT_15D,
                    onSelect = { onPaymentModeSelected(PaymentMode.CREDIT_15D) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(14.dp))

            // Delivery schedule
            val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date(state.deliveryDateTimestamp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = BrandGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Delivery Date: $dateStr (Next Morning)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF37474F)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.notes,
                onValueChange = onNotesChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Special delivery notes (optional)", fontSize = 13.sp) },
                shape = RoundedCornerShape(8.dp),
                maxLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandGreen,
                    unfocusedBorderColor = Color(0xFFDDDDDD)
                )
            )

            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun PaymentChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onSelect,
        label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) BrandGreen else Color.Gray
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = BrandGreenLight,
            selectedLabelColor = BrandGreen
        )
    )
}

@Composable
private fun OrderBottomBar(
    state: NewOrderUiState.Populated,
    onSubmit: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${state.totalQuantity} items • ${"%.1f".format(state.totalWeightKg)} kg",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "₹${"%.2f".format(state.totalAmount)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = BrandGreen
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(incl. 5% GST ₹${"%.2f".format(state.gstAmount)})",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                Button(
                    onClick = onSubmit,
                    enabled = state.isValidOrder && !state.isSubmitting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandGreen,
                        disabledContainerColor = Color(0xFFCCCCCC)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Place Order",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
