package com.absolutefresh.sales.ui.stock

import com.absolutefresh.sales.data.model.PaymentMode

data class SkuStockItem(
    val productId: String,
    val name: String,
    val packSize: String,
    val packetsRemaining: Int,
    val unitPrice: Double
)

data class StoreSettlementState(
    val outletId: String = "o3",
    val outletName: String = "Krishna Sweets & Dairy",
    val stopNumber: Int = 8,
    val outletAddress: String = "Aliganj Sector C, Lucknow",
    val gstin: String = "09AAAFK1208",
    val phone: String = "+91 98765 43203",
    val packetsDropped: Int = 35,
    val transitDamaged: Int = 0,
    val expiredReturned: Int = 2,
    val unitRate: Double = 55.0,
    val expiryCreditPerPacket: Double = 50.0,
    val selectedPaymentMode: PaymentMode = PaymentMode.UPI_QR,
    val isSettled: Boolean = false,
    val generatedInvoiceNumber: String? = null,
    val isProcessing: Boolean = false
) {
    val grossAmount: Double
        get() = packetsDropped * unitRate

    val expiryCreditAmount: Double
        get() = expiredReturned * expiryCreditPerPacket

    val netPayable: Double
        get() = (grossAmount - expiryCreditAmount).coerceAtLeast(0.0)
}

data class StockUiState(
    val vanRouteId: String = "Van Route Stock #04",
    val territory: String = "Lucknow East",
    val batchCode: String = "AF-IDB-101026-01",
    val mfdDate: String = "10 Oct, 04:00 AM",
    val expDate: String = "20 Oct 2026",
    val shelfLifeRemainingDays: Int = 10,
    val totalMorningLoaded: Int = 120,
    val totalDelivered: Int = 85,
    val totalBalance: Int = 35,
    val skuBreakdown: List<SkuStockItem> = emptyList(),
    val fifoUrgentBatch: String? = "AF-011026 (2 pkts, Day 8)",
    val settlementState: StoreSettlementState = StoreSettlementState(),
    val showSuccessToast: Boolean = false,
    val toastMessage: String = ""
)
