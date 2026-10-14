package com.absolutefresh.sales.ui.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.absolutefresh.sales.data.model.CashTransaction
import com.absolutefresh.sales.data.model.PaymentMode
import com.absolutefresh.sales.data.model.SyncStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class MoreViewModel : ViewModel() {

    private val sampleHolidays = listOf(
        Holiday("2026-10-20", "Diwali / Deepavali", "दीपावली / लक्ष्मी पूजा", "Gazetted"),
        Holiday("2026-10-21", "Govardhan Puja", "गोवर्धन पूजा", "Restricted"),
        Holiday("2026-10-23", "Bhai Dooj", "भाई दूज", "Restricted"),
        Holiday("2026-10-27", "Chhath Puja", "छठ पूजा (सायं अर्घ्य)", "Gazetted"),
        Holiday("2026-11-15", "Guru Nanak Jayanti", "गुरु नानक जयंती", "Gazetted")
    )

    private val initialCashTransactions = listOf(
        CashTransaction(
            id = "cash-01",
            outletId = "o1",
            outletName = "Sharma Kirana",
            amount = 1200.0,
            invoiceRef = "AFF-2610-0101",
            paymentMode = PaymentMode.CASH,
            timestamp = System.currentTimeMillis() - (2 * 3600_000L),
            isCleared = false,
            syncStatus = SyncStatus.SYNCED
        ),
        CashTransaction(
            id = "cash-02",
            outletId = "o2",
            outletName = "Gupta Dairy & More",
            amount = 2250.0,
            invoiceRef = "PROV-2610-002",
            paymentMode = PaymentMode.CASH,
            timestamp = System.currentTimeMillis() - (4 * 3600_000L),
            isCleared = false,
            syncStatus = SyncStatus.PENDING
        )
    )

    private val _uiState = MutableStateFlow(
        MoreUiState(
            cashInHand = initialCashTransactions.filter { !it.isCleared }.sumOf { it.amount },
            recentCashTransactions = initialCashTransactions,
            upcomingHolidays = sampleHolidays,
            syncHealth = SyncHealth(
                lastSyncTime = System.currentTimeMillis() - (12 * 60_000L),
                pendingCount = 1,
                failedCount = 0
            )
        )
    )
    val uiState: StateFlow<MoreUiState> = _uiState.asStateFlow()

    fun toggleLanguage() {
        val next = if (_uiState.value.selectedLanguage == "EN") "HI" else "EN"
        _uiState.value = _uiState.value.copy(selectedLanguage = next)
    }

    fun openRecordCash() {
        _uiState.value = _uiState.value.copy(isRecordCashOpen = true)
    }

    fun closeRecordCash() {
        _uiState.value = _uiState.value.copy(isRecordCashOpen = false)
    }

    fun openSyncDetails() {
        _uiState.value = _uiState.value.copy(isSyncDetailsOpen = true)
    }

    fun closeSyncDetails() {
        _uiState.value = _uiState.value.copy(isSyncDetailsOpen = false)
    }

    fun openHolidays() {
        _uiState.value = _uiState.value.copy(isHolidaysOpen = true)
    }

    fun closeHolidays() {
        _uiState.value = _uiState.value.copy(isHolidaysOpen = false)
    }

    fun recordCashReceipt(outletId: String, outletName: String, amount: Double, invoiceRef: String) {
        if (amount <= 0.0) return

        val newTransaction = CashTransaction(
            id = "cash-${UUID.randomUUID()}",
            outletId = outletId,
            outletName = outletName,
            amount = amount,
            invoiceRef = invoiceRef.ifBlank { "PROV-CASH" },
            paymentMode = PaymentMode.CASH,
            timestamp = System.currentTimeMillis(),
            isCleared = false,
            syncStatus = SyncStatus.PENDING
        )

        val updatedList = listOf(newTransaction) + _uiState.value.recentCashTransactions
        val newCashInHand = updatedList.filter { !it.isCleared }.sumOf { it.amount }

        _uiState.value = _uiState.value.copy(
            cashInHand = newCashInHand,
            recentCashTransactions = updatedList,
            syncHealth = _uiState.value.syncHealth.copy(
                pendingCount = _uiState.value.syncHealth.pendingCount + 1
            ),
            isRecordCashOpen = false
        )
    }

    fun triggerManualSync() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                syncHealth = _uiState.value.syncHealth.copy(isSyncing = true)
            )
            delay(1200) // Simulated background sync
            val syncedTransactions = _uiState.value.recentCashTransactions.map {
                it.copy(syncStatus = SyncStatus.SYNCED)
            }
            _uiState.value = _uiState.value.copy(
                recentCashTransactions = syncedTransactions,
                syncHealth = SyncHealth(
                    lastSyncTime = System.currentTimeMillis(),
                    pendingCount = 0,
                    failedCount = 0,
                    isSyncing = false
                )
            )
        }
    }
}
