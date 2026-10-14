package com.absolutefresh.sales.ui.holidays

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.absolutefresh.sales.data.model.BeatImpact
import com.absolutefresh.sales.data.model.Holiday
import com.absolutefresh.sales.data.model.HolidayScope
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HolidayCalendarScreen(
    viewModel: HolidayViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Holiday Calendar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "अवकाश कैलेंडर • Route & Beat Planning",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
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
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = FreshText,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Cached Offline",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = FreshText
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceCard)
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceCanvas)
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    HolidayOverviewBento(
                        upcomingCount = uiState.upcomingCount,
                        impactedBeatsCount = uiState.impactedBeatsCount
                    )
                }

                item {
                    // Month Tabs Row
                    MonthFilterBar(
                        months = uiState.availableMonths,
                        selectedMonth = uiState.selectedMonth,
                        onSelectMonth = { viewModel.setMonth(it) }
                    )
                }

                item {
                    // Scope Filters & Search
                    ScopeAndSearchBar(
                        selectedScope = uiState.selectedScope,
                        onSelectScope = { viewModel.setScope(it) },
                        searchQuery = uiState.searchQuery,
                        onQueryChange = { viewModel.setSearchQuery(it) }
                    )
                }

                if (uiState.filteredHolidays.isEmpty()) {
                    item {
                        EmptyHolidaysCard(
                            month = uiState.selectedMonth,
                            onReset = {
                                viewModel.setMonth("All")
                                viewModel.setScope(null)
                                viewModel.setSearchQuery("")
                            }
                        )
                    }
                } else {
                    items(uiState.filteredHolidays, key = { it.id }) { holiday ->
                        HolidayCard(
                            holiday = holiday,
                            onAdvanceStockClick = { viewModel.openAdvanceSheet(holiday) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Pre-Holiday Advance Stock Bottom Sheet
        if (uiState.isAdvanceSheetOpen && uiState.selectedHoliday != null) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeAdvanceSheet() },
                sheetState = sheetState,
                containerColor = SurfaceCard
            ) {
                PreHolidayAdvanceSheet(
                    holiday = uiState.selectedHoliday!!,
                    outletName = uiState.outletName,
                    advancePacketsText = uiState.advancePacketsText,
                    justification = uiState.justification,
                    isSubmitting = uiState.isSubmitting,
                    isValid = uiState.isAdvanceValid,
                    onOutletNameChange = { viewModel.updateOutletName(it) },
                    onPacketsChange = { viewModel.updateAdvancePackets(it) },
                    onJustificationChange = { viewModel.updateJustification(it) },
                    onSubmit = { viewModel.submitAdvanceRequest() },
                    onClose = { viewModel.closeAdvanceSheet() }
                )
            }
        }
    }
}

@Composable
private fun HolidayOverviewBento(
    upcomingCount: Int,
    impactedBeatsCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Field Operations Schedule",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Surface(color = SurfaceCanvas, shape = RoundedCornerShape(6.dp)) {
                    Text(
                        text = "Lucknow East & Central",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                BentoMetricItem(
                    title = "Upcoming Holidays",
                    value = "$upcomingCount",
                    color = GreenPrimary
                )
                BentoMetricItem(
                    title = "Affected Beat Days",
                    value = "$impactedBeatsCount",
                    color = SaffronSecondary
                )
                BentoMetricItem(
                    title = "Offline Status",
                    value = "Durable",
                    color = FreshText
                )
            }
        }
    }
}

@Composable
private fun BentoMetricItem(
    title: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = color
        )
        Text(
            text = title,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun MonthFilterBar(
    months: List<String>,
    selectedMonth: String,
    onSelectMonth: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        months.forEach { month ->
            val isSelected = selectedMonth == month
            FilterChip(
                selected = isSelected,
                onClick = { onSelectMonth(month) },
                label = {
                    Text(
                        text = month,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GreenPrimary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun ScopeAndSearchBar(
    selectedScope: HolidayScope?,
    onSelectScope: (HolidayScope) -> Unit,
    searchQuery: String,
    onQueryChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search festivals, routes, or dates...", fontSize = 12.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenPrimary,
                unfocusedContainerColor = SurfaceCard,
                focusedContainerColor = SurfaceCard
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            HolidayScope.values().forEach { scope ->
                val isSelected = selectedScope == scope
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectScope(scope) },
                    label = {
                        Text(
                            text = scope.labelEn,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenPrimaryFixed,
                        selectedLabelColor = GreenOnPrimaryFixed
                    )
                )
            }
        }
    }
}

@Composable
private fun HolidayCard(
    holiday: Holiday,
    onAdvanceStockClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Date badge
                DateBadge(dateString = holiday.date, dayOfWeek = holiday.dayOfWeek)

                Spacer(modifier = Modifier.width(14.dp))

                // Name and Scope
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = holiday.nameEn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        ScopeTag(scope = holiday.scope)
                    }

                    Text(
                        text = holiday.nameHi,
                        fontSize = 12.sp,
                        color = GreenPrimary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = holiday.description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline,
                        lineHeight = 15.sp
                    )
                }
            }

            // Beat Impact Section (if applicable)
            holiday.beatImpact?.let { impact ->
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(10.dp))

                BeatImpactNotice(impact = impact)

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onAdvanceStockClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = GreenPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Request Advance Stocking (अग्रिम स्टॉक अनुरोध)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DateBadge(dateString: String, dayOfWeek: String) {
    // Parse "YYYY-MM-DD"
    val parts = dateString.split("-")
    val day = parts.getOrNull(2) ?: "01"
    val monthName = try {
        val localDate = LocalDate.parse(dateString)
        localDate.format(DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH)).uppercase()
    } catch (e: Exception) {
        "OCT"
    }

    Surface(
        color = FreshBg,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.size(width = 64.dp, height = 72.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = monthName,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary
            )
            Text(
                text = day,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GreenPrimary
            )
            Text(
                text = dayOfWeek.take(3).uppercase(),
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun ScopeTag(scope: HolidayScope) {
    val (bgColor, textColor) = when (scope) {
        HolidayScope.PAN_INDIA -> Pair(FreshBg, FreshText)
        HolidayScope.REGIONAL -> Pair(AgingBg, AgingText)
        HolidayScope.MARKET_CLOSURE -> Pair(CriticalBg, CriticalText)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = scope.labelEn,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun BeatImpactNotice(impact: BeatImpact) {
    Surface(
        color = AgingBg.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = AgingText,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Beat Impact: ${impact.beatName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = AgingText
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = impact.advisoryNotice,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                impact.affectedRoutes.take(3).forEach { route ->
                    Surface(
                        color = SurfaceCard,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = route,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHolidaysCard(
    month: String,
    onReset: () -> Unit
) {
    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No Holidays Found",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "No scheduled operational closures match for '$month'",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = onReset) {
                Text("Reset Filters", fontSize = 12.sp, color = GreenPrimary)
            }
        }
    }
}

@Composable
private fun PreHolidayAdvanceSheet(
    holiday: Holiday,
    outletName: String,
    advancePacketsText: String,
    justification: String,
    isSubmitting: Boolean,
    isValid: Boolean,
    onOutletNameChange: (String) -> Unit,
    onPacketsChange: (String) -> Unit,
    onJustificationChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Pre-Holiday Buffer Advance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "अग्रिम स्टॉक अनुरोध • ${holiday.nameEn}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            TextButton(onClick = onClose) {
                Text("Close", color = GreenPrimary)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = outletName,
            onValueChange = onOutletNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Store / Outlet Name (दुकान का नाम) *") },
            placeholder = { Text("e.g. Sharma Kirana Store") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = advancePacketsText,
            onValueChange = onPacketsChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Advance Packets (अतिरिक्त पैकेट) *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
        )

        // Quick packet increments
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("10", "20", "40", "60").forEach { qty ->
                Surface(
                    color = FreshBg,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.clip(RoundedCornerShape(6.dp))
                ) {
                    TextButton(
                        onClick = { onPacketsChange(qty) },
                        contentPadding = ButtonDefaults.TextButtonContentPadding
                    ) {
                        Text("+$qty pkts", fontSize = 11.sp, color = GreenPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = justification,
            onValueChange = onJustificationChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Justification Reason (कारण) *") },
            placeholder = { Text("e.g. High morning footfall on Dussehra, requests +20 extra pkts batter on Monday evening.") },
            minLines = 3,
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = isValid && !isSubmitting,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary,
                disabledContainerColor = Color.LightGray
            )
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Submit Advance Request (अग्रिम अनुरोध दर्ज करें)",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
