package com.absolutefresh.sales.ui.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.absolutefresh.sales.data.model.FieldOrder
import com.absolutefresh.sales.data.model.OrderDeliveryStatus
import com.absolutefresh.sales.data.model.Outlet
import com.absolutefresh.sales.data.model.SyncStatus
import com.absolutefresh.sales.data.repository.OutletRepository
import com.absolutefresh.sales.ui.orders.OrderFilter
import com.absolutefresh.sales.ui.orders.OrdersUiState
import com.absolutefresh.sales.ui.orders.OrdersViewModel
import com.absolutefresh.sales.ui.theme.AgingBg
import com.absolutefresh.sales.ui.theme.AgingText
import com.absolutefresh.sales.ui.theme.BorderSubtle
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
fun FinanceScreen(
    viewModel: OrdersViewModel,
    outletRepository: OutletRepository,
    modifier: Modifier = Modifier,
    onNavigateToNewInvoice: (String) -> Unit = {},
    onNavigateToInvoice: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var isSearchExpanded by remember { mutableStateOf(false) }
    var isSelectOutletSheetOpen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceCanvas)
    ) {
        // Finance Header
        FinanceHeader(
            isSearchExpanded = isSearchExpanded,
            searchQuery = searchQuery,
            onToggleSearch = { isSearchExpanded = !isSearchExpanded },
            onSearchChange = { viewModel.setSearchQuery(it) },
            onNewInvoiceClick = { isSelectOutletSheetOpen = true }
        )

        // KPI Metric Overview
        val allOrders = when (val s = uiState) {
            is OrdersUiState.Populated -> s.orders
            else -> emptyList()
        }
        val totalInvoiced = allOrders.sumOf { it.totalAmount }
        val totalPaid = allOrders.filter { it.deliveryStatus == OrderDeliveryStatus.DELIVERED }.sumOf { it.totalAmount }
        val totalOutstanding = (totalInvoiced - totalPaid).coerceAtLeast(0.0)

        FinanceKpiOverview(
            totalInvoiced = totalInvoiced,
            totalPaid = totalPaid,
            totalOutstanding = totalOutstanding
        )

        // Filter chips bar
        val currentFilter = (uiState as? OrdersUiState.Populated)?.selectedFilter ?: OrderFilter.ALL
        FinanceFilterBar(
            selectedFilter = currentFilter,
            onSelectFilter = { viewModel.setFilter(it) }
        )

        // Invoices list content
        when (val state = uiState) {
            is OrdersUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            }

            is OrdersUiState.Empty -> {
                FinanceEmptyState(onNewInvoiceClick = { isSelectOutletSheetOpen = true })
            }

            is OrdersUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }

            is OrdersUiState.Populated -> {
                if (state.filteredOrders.isEmpty()) {
                    FinanceNoResultsState(
                        searchQuery = searchQuery,
                        onClearSearch = {
                            viewModel.setSearchQuery("")
                            viewModel.setFilter(OrderFilter.ALL)
                            isSearchExpanded = false
                        }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.filteredOrders, key = { it.id }) { order ->
                            InvoiceSummaryCard(
                                order = order,
                                onClick = { viewModel.selectOrder(order) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                // Invoice detail bottom sheet
                state.selectedOrder?.let { selectedOrder ->
                    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.selectOrder(null) },
                        sheetState = sheetState,
                        containerColor = SurfaceCard
                    ) {
                        InvoiceDetailSheet(
                            order = selectedOrder,
                            onClose = { viewModel.selectOrder(null) },
                            onNavigateToInvoice = { orderId ->
                                viewModel.selectOrder(null)
                                onNavigateToInvoice(orderId)
                            }
                        )
                    }
                }
            }
        }
    }

    // Select Outlet Modal Sheet to Create New Invoice
    if (isSelectOutletSheetOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { isSelectOutletSheetOpen = false },
            sheetState = sheetState,
            containerColor = SurfaceCard
        ) {
            SelectOutletForInvoiceSheet(
                outletRepository = outletRepository,
                onOutletSelected = { outlet ->
                    isSelectOutletSheetOpen = false
                    onNavigateToNewInvoice(outlet.id)
                },
                onClose = { isSelectOutletSheetOpen = false }
            )
        }
    }
}

@Composable
private fun FinanceHeader(
    isSearchExpanded: Boolean,
    searchQuery: String,
    onToggleSearch: () -> Unit,
    onSearchChange: (String) -> Unit,
    onNewInvoiceClick: () -> Unit
) {
    Surface(
        color = SurfaceCard,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Finance & Invoices",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "वित्त एवं टैक्स इनवॉइस",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleSearch) {
                        Icon(
                            imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search invoices",
                            tint = GreenPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Button(
                        onClick = onNewInvoiceClick,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = ButtonDefaults.ContentPadding
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "New Invoice", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            if (isSearchExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search outlet or invoice (e.g. Sharma, AFF-2610)...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = GreenPrimary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        cursorColor = GreenPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

@Composable
private fun FinanceKpiOverview(
    totalInvoiced: Double,
    totalPaid: Double,
    totalOutstanding: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        KpiCard(
            title = "Total Invoiced",
            hindiTitle = "कुल इनवॉइस",
            amount = totalInvoiced,
            color = GreenPrimary,
            modifier = Modifier.weight(1f)
        )
        KpiCard(
            title = "Collected",
            hindiTitle = "प्राप्त",
            amount = totalPaid,
            color = Color(0xFF2E7D32),
            modifier = Modifier.weight(1f)
        )
        KpiCard(
            title = "Outstanding",
            hindiTitle = "बकाया",
            amount = totalOutstanding,
            color = if (totalOutstanding > 0) Color(0xFFD97706) else Color.Gray,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun KpiCard(
    title: String,
    hindiTitle: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = hindiTitle, fontSize = 9.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "₹${String.format("%,.0f", amount)}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun FinanceFilterBar(
    selectedFilter: OrderFilter,
    onSelectFilter: (OrderFilter) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OrderFilter.values().forEach { filter ->
            val isSelected = filter == selectedFilter
            val label = when (filter) {
                OrderFilter.ALL -> "All Invoices (सभी)"
                OrderFilter.TODAY -> "Today (आज)"
                OrderFilter.THIS_WEEK -> "This Week (इस सप्ताह)"
                OrderFilter.PENDING -> "Unpaid / Pending (बाकी)"
                OrderFilter.DELIVERED -> "Paid / Cleared (भुगतान)"
            }
            FilterChip(
                selected = isSelected,
                onClick = { onSelectFilter(filter) },
                label = { Text(label, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GreenPrimary.copy(alpha = 0.12f),
                    selectedLabelColor = GreenPrimary
                )
            )
        }
    }
}

@Composable
private fun InvoiceSummaryCard(
    order: FieldOrder,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = order.syncedInvoiceNumber ?: order.provisionalInvoiceNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = GreenPrimary
                    )
                }

                val isPaid = order.deliveryStatus == OrderDeliveryStatus.DELIVERED
                Surface(
                    color = if (isPaid) FreshBg else AgingBg,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (isPaid) "PAID" else "UNPAID",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPaid) FreshText else AgingText,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.outletName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${formatOrderDate(order.createdAt)} • ${order.paymentMode.name}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${String.format("%,.2f", order.totalAmount)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "(incl. 5% GST)",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun InvoiceDetailSheet(
    order: FieldOrder,
    onClose: () -> Unit,
    onNavigateToInvoice: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Tax Invoice Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = order.syncedInvoiceNumber ?: "${order.provisionalInvoiceNumber} (Draft)",
                    fontSize = 12.sp,
                    color = GreenPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(12.dp))

        // Store & Financial Details
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DetailRow("Store / Party:", order.outletName)
                DetailRow("Invoice Ref:", order.syncedInvoiceNumber ?: order.provisionalInvoiceNumber)
                DetailRow("Date:", formatOrderDate(order.createdAt))
                DetailRow("Payment Mode:", order.paymentMode.name)
                DetailRow("Subtotal:", "₹${String.format("%,.2f", order.subtotal)}")
                DetailRow("GST (5%):", "₹${String.format("%,.2f", order.gstAmount)}")
                DetailRow("Grand Total:", "₹${String.format("%,.2f", order.totalAmount)}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Primary Action: Open Full Tax Invoice (Print / PDF / WhatsApp)
        Button(
            onClick = { onNavigateToInvoice(order.id) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
        ) {
            Icon(imageVector = Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("View Tax Invoice (टैक्स इनवॉइस देखें)", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SelectOutletForInvoiceSheet(
    outletRepository: OutletRepository,
    onOutletSelected: (Outlet) -> Unit,
    onClose: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val outletsFlow = remember { outletRepository.observeOutlets() }
    val outlets by outletsFlow.collectAsState(initial = emptyList())

    val filteredOutlets = outlets.filter { outlet ->
        if (searchQuery.isBlank()) true
        else {
            val q = searchQuery.trim().lowercase()
            outlet.name.lowercase().contains(q) ||
                    outlet.area.lowercase().contains(q) ||
                    (outlet.ownerName?.lowercase()?.contains(q) == true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Select Store for New Invoice",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "इनवॉइस के लिए दुकान चुनें",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search by shop name or area...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = GreenPrimary) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenPrimary,
                cursorColor = GreenPrimary
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredOutlets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No stores found matching '$searchQuery'", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredOutlets, key = { it.id }) { outlet ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOutletSelected(outlet) },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = outlet.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    text = "${outlet.area} • Tier ${outlet.priceTier}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { onOutletSelected(outlet) },
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Select", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanceEmptyState(onNewInvoiceClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = null,
                tint = GreenPrimary.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No invoices generated yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Click below to create your first tax invoice for any store.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onNewInvoiceClick,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Create New Invoice (नया इनवॉइस)")
            }
        }
    }
}

@Composable
private fun FinanceNoResultsState(
    searchQuery: String,
    onClearSearch: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No invoices found for \"$searchQuery\"",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Check spelling or clear search filters to view all invoices.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onClearSearch,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Clear Search (सर्च हटाएं)")
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun formatOrderDate(timestamp: Long): String {
    val instant = Instant.ofEpochMilli(timestamp)
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
    return instant.atZone(ZoneId.systemDefault()).format(formatter)
}
