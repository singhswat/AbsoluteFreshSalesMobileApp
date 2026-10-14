package com.absolutefresh.sales.ui.outlets.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Storefront
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.absolutefresh.sales.data.model.OutletType
import com.absolutefresh.sales.ui.theme.BorderSubtle
import com.absolutefresh.sales.ui.theme.CrimsonErrorContainer
import com.absolutefresh.sales.ui.theme.CrimsonTertiary
import com.absolutefresh.sales.ui.theme.FreshBg
import com.absolutefresh.sales.ui.theme.FreshText
import com.absolutefresh.sales.ui.theme.GreenOnPrimary
import com.absolutefresh.sales.ui.theme.GreenPrimary
import com.absolutefresh.sales.ui.theme.GreenPrimaryFixed
import com.absolutefresh.sales.ui.theme.SaffronOnSecondaryFixed
import com.absolutefresh.sales.ui.theme.SaffronSecondary
import com.absolutefresh.sales.ui.theme.SaffronSecondaryFixed
import com.absolutefresh.sales.ui.theme.SurfaceCard
import com.absolutefresh.sales.ui.theme.TextPrimary
import com.absolutefresh.sales.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddOutletScreen(
    viewModel: AddOutletViewModel,
    onNavigateBack: () -> Unit,
    onOutletSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onOutletSaved()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Add Outlet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "नया आउटलेट जोड़ें (फील्ड सेल्स)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Surface(
                        color = FreshBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = "Offline Ready",
                                tint = FreshText,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Offline Ready",
                                style = MaterialTheme.typography.labelSmall,
                                color = FreshText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceCard,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            Surface(
                color = SurfaceCard,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (!state.isValid) {
                        Text(
                            text = "Please fill mandatory fields (* Outlet name, phone, area)",
                            style = MaterialTheme.typography.labelSmall,
                            color = CrimsonTertiary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Button(
                        onClick = { viewModel.submitOutlet() },
                        enabled = state.isValid && !state.isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenPrimary,
                            contentColor = GreenOnPrimary
                        )
                    ) {
                        if (state.isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = GreenOnPrimary,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Save Outlet / आउटलेट सुरक्षित करें",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Potential Duplicate Warning Banner
            if (state.potentialDuplicates.isNotEmpty() && !state.duplicateWarningDismissed) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SaffronSecondaryFixed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Duplicate Warning",
                                tint = SaffronOnSecondaryFixed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Possible Duplicate Detected (संभावित डुप्लिकेट)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SaffronOnSecondaryFixed
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Similar existing outlet(s) found in the database:",
                            style = MaterialTheme.typography.bodySmall,
                            color = SaffronOnSecondaryFixed
                        )
                        state.potentialDuplicates.take(2).forEach { dup ->
                            Text(
                                text = "• ${dup.name} (${dup.area}) - Ph: ${dup.phone}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = SaffronOnSecondaryFixed,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.dismissDuplicateWarning() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Edit Form")
                            }
                            Button(
                                onClick = { viewModel.confirmAddAnyway() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronSecondary)
                            ) {
                                Text("Add Anyway", color = Color.White)
                            }
                        }
                    }
                }
            }

            // Error banner if submit failed
            state.errorMessage?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CrimsonErrorContainer),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = msg,
                        color = CrimsonTertiary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // 1. Outlet Identity Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(GreenPrimaryFixed, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Outlet Identity / आउटलेट विवरण",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Basic business information",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Business Name
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = { viewModel.updateName(it) },
                        label = { Text("Business Name * (दुकान का नाम)") },
                        placeholder = { Text("e.g. Verma Kirana Store") },
                        leadingIcon = {
                            Icon(Icons.Default.Store, contentDescription = null, tint = GreenPrimary)
                        },
                        isError = state.nameError != null,
                        supportingText = {
                            state.nameError?.let {
                                Text(it, color = CrimsonTertiary)
                            } ?: Text("Official sign-board or trade name", color = TextSecondary)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Owner / Contact Name
                    OutlinedTextField(
                        value = state.ownerName,
                        onValueChange = { viewModel.updateOwner(it) },
                        label = { Text("Owner / Contact Person (मालिक का नाम)") },
                        placeholder = { Text("e.g. Rajesh Verma") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary)
                        },
                        supportingText = {
                            Text("Person who signs delivery & makes payments", color = TextSecondary)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mobile Phone
                    OutlinedTextField(
                        value = state.phone,
                        onValueChange = { viewModel.updatePhone(it) },
                        label = { Text("Mobile Phone * (मोबाइल नंबर)") },
                        placeholder = { Text("9876543210") },
                        prefix = { Text("+91 ", fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = GreenPrimary)
                        },
                        isError = state.phoneError != null,
                        supportingText = {
                            state.phoneError?.let {
                                Text(it, color = CrimsonTertiary)
                            } ?: Text("10-digit number for order SMS & payments", color = TextSecondary)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Area / Market
                    OutlinedTextField(
                        value = state.area,
                        onValueChange = { viewModel.updateArea(it) },
                        label = { Text("Area / Market * (इलाका / बाजार)") },
                        placeholder = { Text("e.g. Indira Nagar") },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = GreenPrimary)
                        },
                        isError = state.areaError != null,
                        supportingText = {
                            state.areaError?.let {
                                Text(it, color = CrimsonTertiary)
                            } ?: Text("Locality or beat cluster", color = TextSecondary)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quick Select Area Chips
                    val popularAreas = listOf("Indira Nagar", "Gomti Nagar", "Aliganj", "Alambagh", "Hazratganj", "Chowk")
                    Text(
                        text = "Quick Select Area / त्वरित इलाका चुनें:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        popularAreas.forEach { areaName ->
                            val isSelected = state.area.equals(areaName, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateArea(areaName) },
                                label = { Text(areaName, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GreenPrimaryFixed,
                                    selectedLabelColor = GreenPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Full Address / Landmark
                    OutlinedTextField(
                        value = state.address,
                        onValueChange = { viewModel.updateAddress(it) },
                        label = { Text("Address / Landmark (दुकान का पूरा पता / लैंडमार्क)") },
                        placeholder = { Text("Shop #4, Near City Hospital, Main Road") },
                        leadingIcon = {
                            Icon(Icons.Default.AddLocation, contentDescription = null, tint = TextSecondary)
                        },
                        supportingText = {
                            Text("Helps delivery drivers locate the store easily", color = TextSecondary)
                        },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 2. Field GPS Location Pin Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFFE0F2FE), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint = Color(0xFF0369A1),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "GPS Coordinates / स्थान रिकॉर्ड",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Geotag shop location for audit",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        if (state.isGpsCaptured) {
                            Surface(
                                color = FreshBg,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = FreshText,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Tagged",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = FreshText,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (state.isGpsCaptured && state.latitude != null && state.longitude != null) {
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Captured Coordinates:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "📍 ${"%.4f".format(state.latitude)}° N, ${"%.4f".format(state.longitude)}° E",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                OutlinedButton(
                                    onClick = { viewModel.clearGpsLocation() },
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Clear")
                                }
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.captureGpsLocation(26.8467, 80.9462) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tag Shop Location (जीपीएस स्थान दर्ज करें)")
                        }
                    }
                }
            }

            // 3. Channel Type & Price Tier Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFFEF3C7), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Channel & Price Tier / श्रेणी व मूल्य",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Outlet trade type and pricing group",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Outlet Trade Type (दुकान का प्रकार):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val types = listOf(
                            OutletType.GENERAL_TRADE to "General Trade (किराना)",
                            OutletType.STANDALONE_SHOP to "Dairy / Sweet (डेयरी)",
                            OutletType.CHAIN_STORE to "Supermarket (सुपरमार्केट)",
                            OutletType.DARK_STORE to "Dark Store (क्विक कॉमर्स)"
                        )
                        types.forEach { (type, label) ->
                            FilterChip(
                                selected = state.type == type,
                                onClick = { viewModel.updateType(type) },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GreenPrimaryFixed,
                                    selectedLabelColor = GreenPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = BorderSubtle)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Price Tier (मूल्य स्तर - BRD F2.5):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Default for new field outlets is Tier 4 (MRP)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val tiers = listOf(
                        1 to "Tier 1: Wholesale (थोक) - ₹85/kg",
                        2 to "Tier 2: Distributor (वितरक) - ₹90/kg",
                        3 to "Tier 3: Retailer (खुदरा) - ₹95/kg",
                        4 to "Tier 4: Standard / MRP (मानक) - ₹100/kg [Default]"
                    )
                    tiers.forEach { (tierNum, tierLabel) ->
                        val isSelected = state.priceTier == tierNum
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) GreenPrimaryFixed.copy(alpha = 0.35f) else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) GreenPrimary else BorderSubtle
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable { viewModel.updatePriceTier(tierNum) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .border(
                                            2.dp,
                                            if (isSelected) GreenPrimary else Color.Gray,
                                            CircleShape
                                        )
                                        .padding(3.dp)
                                        .background(
                                            if (isSelected) GreenPrimary else Color.Transparent,
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = tierLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) GreenPrimary else TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // 4. Commercial & Compliance Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFEDE9FE), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = Color(0xFF6D28D9),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Commercial & Terms / व्यापारिक शर्तें",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "GSTIN, daily volume & credit terms",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // GSTIN (Optional with validation)
                    OutlinedTextField(
                        value = state.gstin,
                        onValueChange = { viewModel.updateGstin(it) },
                        label = { Text("GSTIN (जीएसटी नंबर - यदि उपलब्ध हो)") },
                        placeholder = { Text("e.g. 09ABCDE1234F1Z5") },
                        isError = state.gstinError != null,
                        supportingText = {
                            state.gstinError?.let {
                                Text(it, color = CrimsonTertiary)
                            } ?: Text("Optional 15-character alphanumeric format", color = TextSecondary)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            keyboardType = KeyboardType.Ascii
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Estimated Daily Sales (kg/day)
                        OutlinedTextField(
                            value = state.avgKgPerDay,
                            onValueChange = { viewModel.updateAvgKgPerDay(it) },
                            label = { Text("Avg kg/day (औसत बिक्री)") },
                            suffix = { Text("kg") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        // Credit Limit (₹)
                        OutlinedTextField(
                            value = state.creditLimit,
                            onValueChange = { viewModel.updateCreditLimit(it) },
                            label = { Text("Credit Limit (उधार सीमा)") },
                            prefix = { Text("₹") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Payment Terms (भुगतान की अवधि):",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    val terms = listOf(
                        0 to "COD (नकद)",
                        7 to "7 Days",
                        15 to "15 Days (Default)",
                        30 to "30 Days"
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        terms.forEach { (days, label) ->
                            FilterChip(
                                selected = state.paymentTermsDays == days,
                                onClick = { viewModel.updatePaymentTermsDays(days) },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GreenPrimaryFixed,
                                    selectedLabelColor = GreenPrimary
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
