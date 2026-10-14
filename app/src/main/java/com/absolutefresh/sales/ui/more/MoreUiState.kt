package com.absolutefresh.sales.ui.more

import com.absolutefresh.sales.data.model.CashTransaction

data class Holiday(
    val date: String,
    val nameEn: String,
    val nameHi: String,
    val type: String = "Gazetted Holiday"
)

data class SyncHealth(
    val lastSyncTime: Long = System.currentTimeMillis() - (15 * 60_000L),
    val pendingCount: Int = 0,
    val failedCount: Int = 0,
    val isSyncing: Boolean = false
)

data class MoreUiState(
    val repName: String = "Anand Verma",
    val repPhone: String = "+91 98765 43210",
    val repTerritory: String = "Lucknow East",
    val selectedLanguage: String = "EN",
    val cashInHand: Double = 3450.0,
    val recentCashTransactions: List<CashTransaction> = emptyList(),
    val syncHealth: SyncHealth = SyncHealth(),
    val upcomingHolidays: List<Holiday> = emptyList(),
    val isRecordCashOpen: Boolean = false,
    val isSyncDetailsOpen: Boolean = false,
    val isHolidaysOpen: Boolean = false
)
