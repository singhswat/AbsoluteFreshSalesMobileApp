package com.absolutefresh.sales.ui.beatchange

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.absolutefresh.sales.data.model.BeatChangeProposal
import com.absolutefresh.sales.data.model.Outlet
import com.absolutefresh.sales.data.model.ProposalStatus
import com.absolutefresh.sales.data.model.SyncStatus
import com.absolutefresh.sales.data.model.VisitFrequency
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
import com.absolutefresh.sales.ui.theme.TextPrimary
import com.absolutefresh.sales.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeatChangeScreen(
    viewModel: BeatChangeViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var isOutletPickerOpen by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.snackBarMessage) {
        uiState.snackBarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackBarMessage()
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
                            text = "Suggest Beat Change",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "बीट परिवर्तन सुझाव • FS-05",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    if (uiState.pendingCount > 0) {
                        Surface(
                            color = AgingBg,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = AgingText,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "${uiState.pendingCount} Pending",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AgingText
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceCard)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SurfaceCanvas)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = uiState.selectedTab.ordinal,
                containerColor = SurfaceCard,
                contentColor = GreenPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab.ordinal]),
                        color = GreenPrimary
                    )
                }
            ) {
                BeatChangeTab.entries.forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = tab.labelEn,
                                    fontWeight = if (uiState.selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                                if (tab == BeatChangeTab.PAST_REQUESTS && uiState.proposals.isNotEmpty()) {
                                    Surface(
                                        color = if (uiState.selectedTab == tab) GreenPrimary else BorderSubtle,
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            text = uiState.proposals.size.toString(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (uiState.selectedTab == tab) Color.White else TextSecondary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }

            // Tab Content
            when (uiState.selectedTab) {
                BeatChangeTab.NEW_PROPOSAL -> {
                    NewProposalForm(
                        uiState = uiState,
                        onOpenOutletPicker = { isOutletPickerOpen = true },
                        onSelectProposedDay = { viewModel.updateProposedDay(it) },
                        onSelectProposedFrequency = { viewModel.updateProposedFrequency(it) },
                        onReasonChange = { viewModel.updateReason(it) },
                        onSubmit = { viewModel.submitProposal() }
                    )
                }
                BeatChangeTab.PAST_REQUESTS -> {
                    PastRequestsList(
                        uiState = uiState,
                        onFilterSelected = { viewModel.setFilterStatus(it) }
                    )
                }
            }
        }
    }

    // Outlet Selection Modal Sheet
    if (isOutletPickerOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { isOutletPickerOpen = false },
            sheetState = sheetState,
            containerColor = SurfaceCard
        ) {
            OutletPickerSheet(
                outlets = uiState.outlets,
                selectedOutlet = uiState.selectedOutlet,
                onOutletSelected = { outlet ->
                    viewModel.selectOutlet(outlet)
                    isOutletPickerOpen = false
                },
                onClose = { isOutletPickerOpen = false }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// New Proposal Tab Form
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NewProposalForm(
    uiState: BeatChangeUiState,
    onOpenOutletPicker: () -> Unit,
    onSelectProposedDay: (String) -> Unit,
    onSelectProposedFrequency: (VisitFrequency) -> Unit,
    onReasonChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Info Banner
        Surface(
            color = FreshBg,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = FreshText,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "Beat Change Rules (बीट नियम)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = FreshText
                    )
                    Text(
                        text = "Proposals are queued offline and submitted for Admin review. Your daily visit schedule remains authoritative until approved.",
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                }
            }
        }

        // Outlet Selector Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenOutletPicker() }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SELECT OUTLET (आउटलेट चुनें) *",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = FreshBg,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = uiState.selectedOutlet?.name ?: "Tap to select outlet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = uiState.selectedOutlet?.let { "${it.ownerName} • ${it.area}" } ?: "No outlet chosen",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = TextSecondary
                    )
                }
            }
        }

        // Current Schedule Card (Read-only reference)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CURRENT SCHEDULE (वर्तमान शेड्यूल)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Visit Day", fontSize = 12.sp, color = TextSecondary)
                        Text(text = uiState.currentDay, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Frequency", fontSize = 12.sp, color = TextSecondary)
                        Text(text = uiState.currentFrequency.titleEn, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    }
                }
            }
        }

        // Proposed Day Selection
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PROPOSED DAY (प्रस्तावित दिन) *",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = uiState.proposedDay,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DAYS_OF_WEEK.forEach { day ->
                        val isSelected = uiState.proposedDay == day
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectProposedDay(day) },
                            label = { Text(text = day, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FreshBg,
                                selectedLabelColor = GreenPrimary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) GreenPrimary else BorderSubtle
                            )
                        )
                    }
                }
            }
        }

        // Proposed Frequency Selection
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PROPOSED FREQUENCY (प्रस्तावित आवृत्ति) *",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = uiState.proposedFrequency.titleEn,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VisitFrequency.entries.forEach { freq ->
                        val isSelected = uiState.proposedFrequency == freq
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectProposedFrequency(freq) },
                            label = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = freq.titleEn, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                    Text(text = freq.titleHi, fontSize = 10.sp, color = if (isSelected) GreenPrimary else TextSecondary)
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FreshBg,
                                selectedLabelColor = GreenPrimary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) GreenPrimary else BorderSubtle
                            )
                        )
                    }
                }
            }
        }

        // Reason / Justification Field
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "REASON / JUSTIFICATION (परिवर्तन का कारण) *",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.reason,
                    onValueChange = onReasonChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "e.g., Shop owner available only on Wednesday afternoons; market closed Mondays.",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    },
                    minLines = 3,
                    maxLines = 5,
                    isError = uiState.reasonError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = BorderSubtle
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                if (uiState.reasonError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.reasonError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Submit Button
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            enabled = !uiState.isSubmitting
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.AltRoute,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Submit Proposal (प्रस्ताव भेजें)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Past Requests Tab List
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PastRequestsList(
    uiState: BeatChangeUiState,
    onFilterSelected: (ProposalStatus?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.filterStatus == null,
                onClick = { onFilterSelected(null) },
                label = { Text("All (${uiState.proposals.size})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GreenPrimary,
                    selectedLabelColor = Color.White
                )
            )
            ProposalStatus.entries.forEach { status ->
                val count = uiState.proposals.count { it.status == status }
                val isSelected = uiState.filterStatus == status
                FilterChip(
                    selected = isSelected,
                    onClick = { onFilterSelected(status) },
                    label = { Text("${status.labelEn} ($count)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when (status) {
                            ProposalStatus.PENDING_APPROVAL -> AgingBg
                            ProposalStatus.APPROVED -> FreshBg
                            ProposalStatus.REJECTED -> CriticalBg
                        },
                        selectedLabelColor = when (status) {
                            ProposalStatus.PENDING_APPROVAL -> AgingText
                            ProposalStatus.APPROVED -> FreshText
                            ProposalStatus.REJECTED -> CriticalText
                        }
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.filteredProposals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.EditCalendar,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No beat change proposals found",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Switch to 'New Proposal' tab to suggest a schedule change.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.filteredProposals, key = { it.id }) { proposal ->
                    ProposalItemCard(proposal = proposal)
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ProposalItemCard(proposal: BeatChangeProposal) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(proposal.createdAtEpochMs))

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Outlet & Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = proposal.outletName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${proposal.outletArea} • $formattedDate",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                StatusPill(status = proposal.status)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(12.dp))

            // Schedule Change Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "CURRENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Text(text = "${proposal.currentDay} • ${proposal.currentFrequency.titleEn}", fontSize = 13.sp, color = TextPrimary)
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Changes to",
                    tint = GreenPrimary,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .size(18.dp)
                )

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(text = "PROPOSED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                    Text(
                        text = "${proposal.proposedDay} • ${proposal.proposedFrequency.titleEn}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                }
            }

            // Reason
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = SurfaceCanvas,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = "Reason (कारण):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Text(text = "\"${proposal.reason}\"", fontSize = 12.sp, color = TextPrimary, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                }
            }

            // Admin Review Note if available
            if (!proposal.adminNotes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = FreshBg,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
                        Text(text = proposal.adminNotes, fontSize = 11.sp, color = FreshText, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Sync Status
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (proposal.syncStatus == SyncStatus.PENDING) {
                    Icon(imageVector = Icons.Default.Sync, contentDescription = null, tint = AgingText, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Offline Draft (Pending Sync)", fontSize = 10.sp, color = AgingText)
                } else {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Synced", fontSize = 10.sp, color = GreenPrimary)
                }
            }
        }
    }
}

@Composable
private fun StatusPill(status: ProposalStatus) {
    val (bgColor, textColor, icon) = when (status) {
        ProposalStatus.PENDING_APPROVAL -> Triple(AgingBg, AgingText, Icons.Default.Schedule)
        ProposalStatus.APPROVED -> Triple(FreshBg, FreshText, Icons.Default.CheckCircle)
        ProposalStatus.REJECTED -> Triple(CriticalBg, CriticalText, Icons.Default.Close)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = textColor, modifier = Modifier.size(12.dp))
            Text(text = status.labelEn, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Outlet Picker Modal Sheet
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OutletPickerSheet(
    outlets: List<Outlet>,
    selectedOutlet: Outlet?,
    onOutletSelected: (Outlet) -> Unit,
    onClose: () -> Unit
) {
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
            Text(
                text = "Choose Outlet (आउटलेट चुनें)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(outlets, key = { it.id }) { outlet ->
                val isCurrent = outlet.id == selectedOutlet?.id
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) FreshBg else SurfaceCard
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOutletSelected(outlet) }
                        .border(
                            width = 1.dp,
                            color = if (isCurrent) GreenPrimary else BorderSubtle,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = outlet.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "${outlet.ownerName} • ${outlet.area}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        if (isCurrent) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = GreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
