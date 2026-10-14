package com.absolutefresh.sales.ui.invoice

import com.absolutefresh.sales.data.model.PaymentMode
import com.absolutefresh.sales.data.model.SyncStatus

data class InvoiceLineItemUiState(
    val productName: String,
    val hsnCode: String = "190590",
    val quantityPackets: Int,
    val weightKg: Double,
    val unitRate: Double,
    val taxableAmount: Double,
    val gstRatePercent: Double = 5.0,
    val cgstAmount: Double,
    val sgstAmount: Double,
    val lineTotal: Double
)

sealed interface InvoiceUiState {
    object Loading : InvoiceUiState
    data class Error(val message: String) : InvoiceUiState
    data class Success(
        val orderId: String,
        val outletId: String,
        val outletName: String,
        val outletOwner: String,
        val outletPhone: String,
        val outletAddress: String,
        val outletArea: String,
        val outletGstin: String?,
        val priceTier: Int,
        // Seller info
        val sellerName: String = "Absolute Fresh Pvt. Ltd.",
        val sellerFssai: String = "12723999000123",
        val sellerGstin: String = "09AAHCA1234F1Z5",
        val sellerAddress: String = "B-12, Transport Nagar, Lucknow, UP 226012",
        val sellerContact: String = "+91 80099 22334 • orders@absolutefresh.com",
        // Invoice details
        val invoiceNumber: String,
        val isProvisional: Boolean,
        val invoiceDateFormatted: String,
        val syncStatus: SyncStatus,
        val paymentMode: PaymentMode,
        val paymentStatusText: String,
        val items: List<InvoiceLineItemUiState>,
        val subtotal: Double,
        val cgstTotal: Double,
        val sgstTotal: Double,
        val grandTotal: Double,
        val totalWeightKg: Double,
        val totalPackets: Int,
        val shareText: String
    ) : InvoiceUiState
}
