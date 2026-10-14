package com.absolutefresh.sales.ui.beat

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.absolutefresh.sales.data.model.BeatOutletItem
import com.absolutefresh.sales.data.model.BeatProgress
import com.absolutefresh.sales.data.model.SyncStatus
import com.absolutefresh.sales.data.model.VisitState
import com.absolutefresh.sales.ui.theme.AgingBg
import com.absolutefresh.sales.ui.theme.AgingText
import com.absolutefresh.sales.ui.theme.BorderSubtle
import com.absolutefresh.sales.ui.theme.CrimsonTertiary
import com.absolutefresh.sales.ui.theme.FreshBg
import com.absolutefresh.sales.ui.theme.FreshText
import com.absolutefresh.sales.ui.theme.GreenPrimary
import com.absolutefresh.sales.ui.theme.OnSurfaceText
import com.absolutefresh.sales.ui.theme.OnSurfaceVariantText
import com.absolutefresh.sales.ui.theme.SaffronSecondary
import com.absolutefresh.sales.ui.theme.SurfaceCanvas
import com.absolutefresh.sales.ui.theme.SurfaceCard
import com.absolutefresh.sales.ui.theme.SurfaceContainer
import com.absolutefresh.sales.ui.theme.SurfaceContainerLow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// Screen entry point
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BeatScreen(
    beatViewModel: BeatViewModel,
    onOutletClick: (outletId: String) -> Unit = {}
) {
    val uiState     by beatViewModel.uiState.collectAsState()
    val selectedDate by beatViewModel.selectedDate.collectAsState()

    val isRefreshing = uiState is BeatUiState.Loading
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh  = { beatViewModel.loadBeat() }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceCanvas)
    ) {
        // ── Top App Bar ───────────────────────────────────────────────────────
        TopAppBar(
            title = {
                Text(
                    text = "Today's Beat",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenPrimary),
            actions = {
                IconButton(onClick = { beatViewModel.loadBeat() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                }
            }
        )

        // ── Date Navigator ────────────────────────────────────────────────────
        DateNavigator(
            date     = selectedDate,
            onPrev   = { beatViewModel.goToPreviousDay() },
            onToday  = { beatViewModel.goToToday() },
            onNext   = { beatViewModel.goToNextDay() }
        )

        // ── Banners ───────────────────────────────────────────────────────────
        if (uiState is BeatUiState.Populated) {
            val populated = uiState as BeatUiState.Populated
            if (populated.isStale) {
                StaleBanner()
            }
            if (populated.isOffline || populated.pendingCount > 0) {
                OfflineBanner(pendingCount = populated.pendingCount)
            }
        }

        // ── Body ──────────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "BeatBodyTransition"
            ) { state ->
                when (state) {
                    is BeatUiState.Loading   -> BeatLoadingSkeleton()
                    is BeatUiState.Empty     -> BeatEmptyState()
                    is BeatUiState.Error     -> BeatErrorState(message = state.message, onRetry = { beatViewModel.loadBeat() })
                    is BeatUiState.Populated -> BeatPopulated(
                        state       = state,
                        onOutletClick = onOutletClick
                    )
                }
            }
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state      = pullRefreshState,
                modifier   = Modifier.align(Alignment.TopCenter),
                contentColor = GreenPrimary
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Date Navigator
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DateNavigator(
    date: LocalDate,
    onPrev: () -> Unit,
    onToday: () -> Unit,
    onNext: () -> Unit
) {
    val today       = LocalDate.now()
    val isToday     = date.isEqual(today)
    val isYesterday = date.isEqual(today.minusDays(1))
    val isTomorrow  = date.isEqual(today.plusDays(1))

    val displayLabel = when {
        isToday     -> "Today, ${date.format(DateTimeFormatter.ofPattern("d MMM"))}"
        isYesterday -> "Yesterday, ${date.format(DateTimeFormatter.ofPattern("d MMM"))}"
        isTomorrow  -> "Tomorrow, ${date.format(DateTimeFormatter.ofPattern("d MMM"))}"
        else        -> date.format(DateTimeFormatter.ofPattern("EEE, d MMM", Locale.ENGLISH))
    }

    Surface(color = SurfaceContainerLow, tonalElevation = 1.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPrev) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous day", tint = GreenPrimary)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(enabled = !isToday) { onToday() }
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = if (isToday) GreenPrimary else OnSurfaceVariantText,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = displayLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    color = if (isToday) GreenPrimary else OnSurfaceText
                )
            }

            IconButton(onClick = onNext) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next day", tint = GreenPrimary)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Banners
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StaleBanner() {
    Surface(color = Color(0xFFFEF9C3)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Beat data may be out of date — refresh when online",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF713F12)
            )
        }
    }
}

@Composable
private fun OfflineBanner(pendingCount: Int) {
    Surface(color = Color(0xFFEFF6FF)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.WifiOff, contentDescription = null, tint = Color(0xFF1D4ED8), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (pendingCount > 0)
                    "Working offline — $pendingCount pending"
                else
                    "Working offline",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF1E3A8A)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Populated state
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BeatPopulated(
    state: BeatUiState.Populated,
    onOutletClick: (outletId: String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            BeatProgressCard(progress = state.progress)
        }
        item {
            LastRefreshedLabel(epochMillis = state.lastRefreshed)
        }
        items(state.items, key = { it.outlet.id }) { item ->
            BeatOutletRow(
                item = item,
                onClick = { onOutletClick(item.outlet.id) }
            )
        }
        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Progress card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BeatProgressCard(progress: BeatProgress) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Beat Progress",
                style = MaterialTheme.typography.labelLarge,
                color = OnSurfaceVariantText,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProgressStat(value = "${progress.done}",     label = "Done",    color = GreenPrimary)
                ProgressStat(value = "${progress.pending}",  label = "Pending", color = SaffronSecondary)
                ProgressStat(value = "${progress.missed}",   label = "Missed",  color = CrimsonTertiary)
                ProgressStat(value = "${progress.planned}",  label = "Total",   color = OnSurfaceVariantText)
            }

            Spacer(modifier = Modifier.height(10.dp))

            val volFraction = (progress.achievedVolumeKg / progress.targetVolumeKg).coerceIn(0.0, 1.0).toFloat()
            LinearProgressIndicator(
                progress = { volFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = GreenPrimary,
                trackColor = BorderSubtle
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${progress.achievedVolumeKg.toInt()} / ${progress.targetVolumeKg.toInt()} kg",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariantText
            )
        }
    }
}

@Composable
private fun ProgressStat(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariantText)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Last-refreshed label (GAC 15)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LastRefreshedLabel(epochMillis: Long) {
    val minutesAgo = ((System.currentTimeMillis() - epochMillis) / 60_000).coerceAtLeast(0)
    val label = when {
        minutesAgo < 1   -> "Last refreshed: just now"
        minutesAgo < 60  -> "Last refreshed: ${minutesAgo}m ago"
        else             -> "Last refreshed: ${minutesAgo / 60}h ago"
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = OnSurfaceVariantText,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Outlet row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BeatOutletRow(
    item: BeatOutletItem,
    onClick: () -> Unit
) {
    val isReassigned = item.visitState == VisitState.MISSED
    val cardBg = when {
        isReassigned                      -> Color(0xFFFFF7ED) // amber tint for reassigned / missed
        item.visitState == VisitState.COMPLETED -> SurfaceContainerLow
        else                              -> SurfaceCard
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (item.visitState == VisitState.ACTIVE) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Sequence circle
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(visitStateColor(item.visitState).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${item.sequenceNumber}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = visitStateColor(item.visitState)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.outlet.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurfaceText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    VisitStateBadge(state = item.visitState)
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${item.outlet.area} • Tier ${item.outlet.priceTier}",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariantText
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Target volume
                    Text(
                        text = "Target: ${item.targetKg.toInt()} kg",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariantText
                    )

                    // Fresh / aging batch chips
                    if (item.freshBatchesCount > 0) {
                        FreshnessChip(count = item.freshBatchesCount, isFresh = true)
                    }
                    if (item.agingBatchesCount > 0) {
                        FreshnessChip(count = item.agingBatchesCount, isFresh = false)
                    }

                    // Sync badge
                    SyncBadge(syncStatus = item.outlet.syncStatus)
                }

                if (item.completedOrderSummary != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Order: ${item.completedOrderSummary}",
                        style = MaterialTheme.typography.labelSmall,
                        color = GreenPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (isReassigned) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.SyncProblem,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reassigned — review",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFD97706),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VisitStateBadge(state: VisitState) {
    val (label, bg, fg) = when (state) {
        VisitState.COMPLETED -> Triple("Done",    Color(0xFFDCFCE7), GreenPrimary)
        VisitState.ACTIVE    -> Triple("Active",  Color(0xFFDEF7FF), Color(0xFF0369A1))
        VisitState.PLANNED   -> Triple("Planned", SurfaceContainer,  OnSurfaceVariantText)
        VisitState.MISSED    -> Triple("Missed",  Color(0xFFFFF7ED), Color(0xFFD97706))
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = fg, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun FreshnessChip(count: Int, isFresh: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isFresh) FreshBg else AgingBg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "$count ${if (isFresh) "fresh" else "aging"}",
            style = MaterialTheme.typography.labelSmall,
            color = if (isFresh) FreshText else AgingText,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SyncBadge(syncStatus: SyncStatus) {
    val (icon, tint, label) = when (syncStatus) {
        SyncStatus.SYNCED   -> Triple(Icons.Default.CheckCircle,        GreenPrimary,         "Synced")
        SyncStatus.PENDING  -> Triple(Icons.Default.Schedule,           SaffronSecondary,     "Pending")
        SyncStatus.SYNCING  -> Triple(Icons.Default.Refresh,            Color(0xFF0369A1),    "Syncing")
        SyncStatus.FAILED   -> Triple(Icons.Default.ErrorOutline,       CrimsonTertiary,      "Failed")
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(2.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = tint, fontSize = 10.sp)
    }
}

private fun visitStateColor(state: VisitState) = when (state) {
    VisitState.COMPLETED -> Color(0xFF166534)
    VisitState.ACTIVE    -> Color(0xFF0369A1)
    VisitState.PLANNED   -> Color(0xFF6B7280)
    VisitState.MISSED    -> Color(0xFFD97706)
}

// ─────────────────────────────────────────────────────────────────────────────
// Loading skeleton
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BeatLoadingSkeleton() {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(6) {
            SkeletonRow()
        }
    }
}

@Composable
private fun SkeletonRow() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(BorderSubtle))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Box(modifier = Modifier.height(14.dp).fillMaxWidth(0.55f).clip(RoundedCornerShape(4.dp)).background(BorderSubtle))
                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier.height(10.dp).fillMaxWidth(0.35f).clip(RoundedCornerShape(4.dp)).background(SurfaceContainer))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty state
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BeatEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = BorderSubtle,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No beat assigned for this date",
            style = MaterialTheme.typography.titleMedium,
            color = OnSurfaceVariantText,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Check back later or contact your manager",
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariantText
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Error state
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BeatErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = CrimsonTertiary,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariantText
        )
        Spacer(modifier = Modifier.height(20.dp))
        androidx.compose.material3.OutlinedButton(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Retry")
        }
    }
}
