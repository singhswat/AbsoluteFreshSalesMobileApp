package com.absolutefresh.sales.ui.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.absolutefresh.sales.data.model.PaymentMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StockViewModel : ViewModel() {

    private val initialSkus = listOf(
        SkuStockItem("p1", "500g Batter", "500g", 20, 45.0),
        SkuStockItem("p2", "1kg Family", "1000g", 10, 85.0),
        SkuStockItem("p3", "Podi Chutney", "100g", 5, 28.0)
    )

    private val _uiState = MutableStateFlow(
        StockUiState(
            skuBreakdown = initialSkus
        )
    )
    val uiState: StateFlow<StockUiState> = _uiState.asStateFlow()

    fun adjustDropQty(delta: Int) {
        val current = _uiState.value.settlementState.packetsDropped
        val updated = (current + delta).coerceIn(1, 100)
        _uiState.value = _uiState.value.copy(
            settlementState = _uiState.value.settlementState.copy(packetsDropped = updated)
        )
    }

    fun adjustExpiredReturnQty(delta: Int) {
        val current = _uiState.value.settlementState.expiredReturned
        val updated = (current + delta).coerceIn(0, 50)
        _uiState.value = _uiState.value.copy(
            settlementState = _uiState.value.settlementState.copy(expiredReturned = updated)
        )
    }

    fun setPaymentMode(mode: PaymentMode) {
        _uiState.value = _uiState.value.copy(
            settlementState = _uiState.value.settlementState.copy(selectedPaymentMode = mode)
        )
    }

    fun executeSettlement() {
        val settlement = _uiState.value.settlementState
        if (settlement.isProcessing || settlement.isSettled) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                settlementState = settlement.copy(isProcessing = true)
            )

            delay(800) // Simulated settlement calculation

            val invoiceNo = "AFF-2610-018"
            val newDelivered = _uiState.value.totalDelivered + settlement.packetsDropped
            val newBalance = (_uiState.value.totalBalance - settlement.packetsDropped).coerceAtLeast(0)

            _uiState.value = _uiState.value.copy(
                totalDelivered = newDelivered,
                totalBalance = newBalance,
                settlementState = settlement.copy(
                    isProcessing = false,
                    isSettled = true,
                    generatedInvoiceNumber = invoiceNo
                ),
                showSuccessToast = true,
                toastMessage = "Invoice $invoiceNo Logged. Crate inventory updated."
            )
        }
    }

    fun dismissToast() {
        _uiState.value = _uiState.value.copy(showSuccessToast = false)
    }
}
