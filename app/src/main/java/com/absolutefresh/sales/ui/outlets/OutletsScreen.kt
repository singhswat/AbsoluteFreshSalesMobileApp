package com.absolutefresh.sales.ui.outlets

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.absolutefresh.sales.data.model.Outlet
import com.absolutefresh.sales.data.model.OutletType
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutletsScreen(
    viewModel: OutletsViewModel,
    modifier: Modifier = Modifier,
    onNavigateToNewOrder: (outletId: String) -> Unit = {},
    onNavigateToAddOutlet: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var isSearchExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceCanvas)
    ) {
        // Header
        OutletsHeader(
            isSearchExpanded = isSearchExpanded,
            searchQuery = (uiState as? OutletsUiState.Populated)?.searchQuery ?: "",
            pendingSyncCount = (uiState as? OutletsUiState.Populated)?.pendingSyncCount ?: 0,
            onToggleSearch = { isSearchExpanded = !isSearchExpanded },
            onSearchChange = { viewModel.setSearchQuery(it) },
            onAddOutletClick = {
                onNavigateToAddOutlet()
            }
        )

        // Area Filter Chips Bar
        val populated = uiState as? OutletsUiState.Populated
        if (populated != null && populated.availableAreas.isNotEmpty()) {
            OutletsAreaFilterBar(
                areas = populated.availableAreas,
                selectedArea = populated.selectedArea,
                onSelectArea = { viewModel.setAreaFilter(it) }
            )
        }

        // Body Content
        when (val state = uiState) {
            is OutletsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            }

            is OutletsUiState.Empty -> {
                OutletsEmptyState(onAddOutlet = { onNavigateToAddOutlet() })
            }

            is OutletsUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }

            is OutletsUiState.Populated -> {
                if (state.filteredOutlets.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No outlets found for '${state.searchQuery.ifBlank { state.selectedArea }}'",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = {
                                viewModel.setSearchQuery("")
                                viewModel.setAreaFilter("All")
                                isSearchExpanded = false
                            }) {
                                Text("Reset filters", color = GreenPrimary)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.filteredOutlets, key = { it.id }) { outlet ->
                            OutletCard(
                                outlet = outlet,
                                onClick = { viewModel.selectOutlet(outlet) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                // Outlet Detail Modal
                state.selectedOutlet?.let { selected ->
                    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.selectOutlet(null) },
                        sheetState = sheetState,
                        containerColor = SurfaceCard
                    ) {
                        OutletDetailSheet(
                            outlet = selected,
                            onClose = { viewModel.selectOutlet(null) },
                            onStartOrder = {
                                viewModel.selectOutlet(null)
                                onNavigateToNewOrder(selected.id)
                            }
                        )
                    }
                }

                // Add Outlet Modal Form
                if (state.formState.isOpen) {
                    val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeAddOutlet() },
                        sheetState = addSheetState,
                        containerColor = SurfaceCard
                    ) {
                        AddOutletSheet(
                            formState = state.formState,
                            onUpdateName = { viewModel.updateFormName(it) },
                            onUpdateOwner = { viewModel.updateFormOwner(it) },
                            onUpdatePhone = { viewModel.updateFormPhone(it) },
                            onUpdateArea = { viewModel.updateFormArea(it) },
                            onUpdateAddress = { viewModel.updateFormAddress(it) },
                            onUpdateType = { viewModel.updateFormType(it) },
                            onUpdatePriceTier = { viewModel.updateFormPriceTier(it) },
                            onUpdateGstin = { viewModel.updateFormGstin(it) },
                            onConfirmAddAnyway = { viewModel.confirmAddAnyway() },
                            onSubmit = { viewModel.submitNewOutlet() },
                            onClose = { viewModel.closeAddOutlet() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OutletsHeader(
    isSearchExpanded: Boolean,
    searchQuery: String,
    pendingSyncCount: Int,
    onToggleSearch: () -> Unit,
    onSearchChange: (String) -> Unit,
    onAddOutletClick: () -> Unit
) {
    Surface(color = SurfaceCard, shadowElevation = 1.dp) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "My Outlets",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "दुकानें / आउटलेट्स",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (pendingSyncCount > 0) {
                        Surface(
                            color = AgingBg,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = AgingText,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "$pendingSyncCount pending",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AgingText
                                )
                            }
                        }
                    }

                    IconButton(onClick = onToggleSearch) {
                        Icon(
                            imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search",
                            tint = GreenPrimary
                        )
                    }

                    Button(
                        onClick = onAddOutletClick,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = ButtonDefaults.ContentPadding
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (isSearchExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by name, owner, area, phone...") },
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
private fun OutletsAreaFilterBar(
    areas: List<String>,
    selectedArea: String,
    onSelectArea: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        areas.forEach { area ->
            val isSelected = area == selectedArea
            FilterChip(
                selected = isSelected,
                onClick = { onSelectArea(area) },
                label = { Text(area, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GreenPrimary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun OutletCard(
    outlet: Outlet,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Name & Tier
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = outlet.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = FreshBg,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Tier ${outlet.priceTier}",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = FreshText
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Owner & Area
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${outlet.ownerName} • ${outlet.area}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = outlet.type.name.replace("_", " "),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Phone, Balance & Sync status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${outlet.phone}"))
                        context.startActivity(intent)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = GreenPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = outlet.phone,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = GreenPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (outlet.outstandingBalance > 0) {
                        Text(
                            text = "Due: ₹${String.format("%,.0f", outlet.outstandingBalance)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AgingText,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    OutletSyncBadge(outlet.syncStatus)
                }
            }
        }
    }
}

@Composable
private fun OutletSyncBadge(status: SyncStatus) {
    val (icon, label, color, bg) = when (status) {
        SyncStatus.SYNCED -> TripleColor(Icons.Default.CheckCircle, "Synced", FreshText, FreshBg)
        SyncStatus.SYNCING -> TripleColor(Icons.Default.Refresh, "Syncing", GreenPrimary, FreshBg)
        SyncStatus.PENDING -> TripleColor(Icons.Default.Sync, "Pending", AgingText, AgingBg)
        SyncStatus.FAILED -> TripleColor(Icons.Default.Error, "Failed", CriticalText, CriticalBg)
    }

    Surface(color = bg, shape = RoundedCornerShape(4.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(10.dp), tint = color)
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

private data class TripleColor(val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String, val color: Color, val bg: Color)

@Composable
private fun OutletDetailSheet(
    outlet: Outlet,
    onClose: () -> Unit,
    onStartOrder: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = outlet.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = outlet.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
            OutletSyncBadge(outlet.syncStatus)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Commercial Details Box
        Surface(color = SurfaceCanvas, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                OutletDetailRow("Owner:", outlet.ownerName)
                OutletDetailRow("Phone:", outlet.phone)
                OutletDetailRow("Area:", outlet.area)
                OutletDetailRow("Category:", outlet.type.name.replace("_", " "))
                OutletDetailRow("Price Tier:", "Tier ${outlet.priceTier}")
                outlet.gstin?.let { OutletDetailRow("GSTIN:", it) }
                OutletDetailRow("Payment Terms:", "${outlet.paymentTermsDays} Days")
                OutletDetailRow("Credit Limit:", "₹${String.format("%,.0f", outlet.creditLimit)}")
                OutletDetailRow("Current Balance:", "₹${String.format("%,.0f", outlet.outstandingBalance)}")
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Action Buttons
        Button(
            onClick = onStartOrder,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Take Order (नया ऑर्डर लें)", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${outlet.phone}"))
                    context.startActivity(intent)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp), tint = GreenPrimary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Call Owner", color = GreenPrimary)
            }

            OutlinedButton(
                onClick = {
                    val uri = Uri.parse("geo:0,0?q=${Uri.encode("${outlet.name}, ${outlet.address}")}")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(intent)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = GreenPrimary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Maps", color = GreenPrimary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
            Text("Close", color = GreenPrimary, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun OutletDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AddOutletSheet(
    formState: AddOutletFormState,
    onUpdateName: (String) -> Unit,
    onUpdateOwner: (String) -> Unit,
    onUpdatePhone: (String) -> Unit,
    onUpdateArea: (String) -> Unit,
    onUpdateAddress: (String) -> Unit,
    onUpdateType: (OutletType) -> Unit,
    onUpdatePriceTier: (Int) -> Unit,
    onUpdateGstin: (String) -> Unit,
    onConfirmAddAnyway: () -> Unit,
    onSubmit: () -> Unit,
    onClose: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Add New Outlet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = "नयी दुकान जोड़ें (Offline Ready)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Duplicate warning banner if potential duplicates were detected
        if (formState.potentialDuplicates.isNotEmpty() && !formState.duplicateWarningDismissed) {
            Surface(
                color = AgingBg,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = AgingText, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Possible Duplicate Found (${formState.potentialDuplicates.size})",
                            fontWeight = FontWeight.Bold,
                            color = AgingText,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Field sales policy (FS-04 AC5): Existing store found with similar name/phone:",
                        fontSize = 12.sp,
                        color = AgingText
                    )
                    formState.potentialDuplicates.take(2).forEach { dup ->
                        Text(
                            text = "• ${dup.name} (${dup.area}) - ${dup.phone}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = AgingText
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onConfirmAddAnyway,
                        colors = ButtonDefaults.buttonColors(containerColor = AgingText),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Confirm & Add Anyway (नयी शाखा जोड़ें)", fontSize = 12.sp)
                    }
                }
            }
        }

        // Form Fields
        OutlinedTextField(
            value = formState.name,
            onValueChange = onUpdateName,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Store / Business Name (दुकान का नाम) *") },
            isError = formState.nameError != null,
            supportingText = formState.nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = formState.ownerName,
            onValueChange = onUpdateOwner,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Owner / Contact Person (मालिक का नाम)") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = formState.phone,
            onValueChange = onUpdatePhone,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Mobile Phone (मोबाइल नंबर) *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = formState.phoneError != null,
            supportingText = formState.phoneError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = formState.area,
            onValueChange = onUpdateArea,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Area / Territory (क्षेत्र/इलाका) *") },
            isError = formState.areaError != null,
            supportingText = formState.areaError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = formState.address,
            onValueChange = onUpdateAddress,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Full Address (पता)") },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Price Tier selector (1 to 4)
        Text(text = "Price Tier (मूल्य श्रेणी)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            (1..4).forEach { tier ->
                val selected = formState.priceTier == tier
                FilterChip(
                    selected = selected,
                    onClick = { onUpdatePriceTier(tier) },
                    label = { Text("Tier $tier") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = formState.gstin,
            onValueChange = onUpdateGstin,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("GSTIN (Optional)") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onSubmit,
            enabled = formState.isValid && !formState.isSaving,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (formState.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
            } else {
                Text("Save Outlet (दुकान सहेजें)", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun OutletsEmptyState(onAddOutlet: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(imageVector = Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(56.dp), tint = GreenPrimary.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "No outlets registered yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onAddOutlet, colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)) {
                Text("+ Add First Outlet")
            }
        }
    }
}
