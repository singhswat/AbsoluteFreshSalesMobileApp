package com.absolutefresh.sales.ui.invoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.absolutefresh.sales.data.model.FieldOrder
import com.absolutefresh.sales.data.model.Outlet
import com.absolutefresh.sales.data.model.PaymentMode
import com.absolutefresh.sales.data.model.SyncStatus
import com.absolutefresh.sales.data.repository.OrderRepository
import com.absolutefresh.sales.data.repository.OutletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InvoiceViewModel(
    private val orderId: String,
    private val orderRepository: OrderRepository,
    private val outletRepository: OutletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<InvoiceUiState>(InvoiceUiState.Loading)
    val uiState: StateFlow<InvoiceUiState> = _uiState.asStateFlow()

    init {
        loadInvoice()
    }

    fun loadInvoice() {
        viewModelScope.launch {
            _uiState.value = InvoiceUiState.Loading
            try {
                val order = orderRepository.getOrderById(orderId)
                if (order == null) {
                    _uiState.value = InvoiceUiState.Error("Order not found: $orderId")
                    return@launch
                }

                val outlet = outletRepository.getOutletById(order.outletId)
                val successState = buildSuccessState(order, outlet)
                _uiState.value = successState
            } catch (e: Exception) {
                _uiState.value = InvoiceUiState.Error(e.message ?: "Failed to generate invoice")
            }
        }
    }

    private fun buildSuccessState(order: FieldOrder, outlet: Outlet?): InvoiceUiState.Success {
        val isProvisional = order.syncedInvoiceNumber == null
        val invoiceNumber = order.syncedInvoiceNumber ?: order.provisionalInvoiceNumber

        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
        val formattedDate = dateFormat.format(Date(order.createdAt))

        var runningSubtotal = 0.0
        var runningCgst = 0.0
        var runningSgst = 0.0
        var totalWeight = 0.0
        var totalPkts = 0

        val lineItems = order.items.map { item ->
            val taxable = BigDecimal(item.lineTotal).setScale(2, RoundingMode.HALF_UP).toDouble()
            val totalGst = BigDecimal(taxable * 0.05).setScale(2, RoundingMode.HALF_UP).toDouble()
            val cgst = BigDecimal(totalGst / 2.0).setScale(2, RoundingMode.HALF_UP).toDouble()
            val sgst = BigDecimal(totalGst - cgst).setScale(2, RoundingMode.HALF_UP).toDouble()
            val itemLineTotal = BigDecimal(taxable + totalGst).setScale(2, RoundingMode.HALF_UP).toDouble()

            runningSubtotal += taxable
            runningCgst += cgst
            runningSgst += sgst
            totalWeight += item.weightKg
            totalPkts += item.quantityPackets

            InvoiceLineItemUiState(
                productName = item.productName,
                hsnCode = "190590",
                quantityPackets = item.quantityPackets,
                weightKg = BigDecimal(item.weightKg).setScale(2, RoundingMode.HALF_UP).toDouble(),
                unitRate = item.unitPrice,
                taxableAmount = taxable,
                gstRatePercent = 5.0,
                cgstAmount = cgst,
                sgstAmount = sgst,
                lineTotal = itemLineTotal
            )
        }

        val subtotal = BigDecimal(runningSubtotal).setScale(2, RoundingMode.HALF_UP).toDouble()
        val cgstTotal = BigDecimal(runningCgst).setScale(2, RoundingMode.HALF_UP).toDouble()
        val sgstTotal = BigDecimal(runningSgst).setScale(2, RoundingMode.HALF_UP).toDouble()
        val grandTotal = BigDecimal(subtotal + cgstTotal + sgstTotal).setScale(2, RoundingMode.HALF_UP).toDouble()

        val paymentStatusText = when (order.paymentMode) {
            PaymentMode.CASH -> "Collected via Cash (नकद)"
            PaymentMode.UPI_QR -> "Paid via UPI QR (यूपीआई)"
            PaymentMode.CHEQUE -> "Paid via Cheque (चेक)"
            PaymentMode.CREDIT_15D -> "15-Day Credit Line (उधार)"
        }

        val outletName = outlet?.name ?: order.outletName.ifEmpty { "Store Outlet" }
        val outletOwner = outlet?.ownerName ?: "Store Manager"
        val outletPhone = outlet?.phone ?: "—"
        val outletAddress = outlet?.address ?: "Lucknow, UP"
        val outletArea = outlet?.area ?: "Lucknow"
        val outletGstin = if (outlet?.type?.name == "CHAIN_STORE") "09AABCN5543D1ZT" else null
        val priceTier = outlet?.priceTier ?: 4

        val shareText = buildShareText(
            isProvisional = isProvisional,
            invoiceNumber = invoiceNumber,
            date = formattedDate,
            outletName = outletName,
            outletArea = outletArea,
            items = lineItems,
            subtotal = subtotal,
            cgst = cgstTotal,
            sgst = sgstTotal,
            grandTotal = grandTotal,
            paymentMode = order.paymentMode
        )

        return InvoiceUiState.Success(
            orderId = order.id,
            outletId = order.outletId,
            outletName = outletName,
            outletOwner = outletOwner,
            outletPhone = outletPhone,
            outletAddress = outletAddress,
            outletArea = outletArea,
            outletGstin = outletGstin,
            priceTier = priceTier,
            invoiceNumber = invoiceNumber,
            isProvisional = isProvisional,
            invoiceDateFormatted = formattedDate,
            syncStatus = order.syncStatus,
            paymentMode = order.paymentMode,
            paymentStatusText = paymentStatusText,
            items = lineItems,
            subtotal = subtotal,
            cgstTotal = cgstTotal,
            sgstTotal = sgstTotal,
            grandTotal = grandTotal,
            totalWeightKg = BigDecimal(totalWeight).setScale(1, RoundingMode.HALF_UP).toDouble(),
            totalPackets = totalPkts,
            shareText = shareText
        )
    }

    private fun buildShareText(
        isProvisional: Boolean,
        invoiceNumber: String,
        date: String,
        outletName: String,
        outletArea: String,
        items: List<InvoiceLineItemUiState>,
        subtotal: Double,
        cgst: Double,
        sgst: Double,
        grandTotal: Double,
        paymentMode: PaymentMode
    ): String {
        val provisionalHeader = if (isProvisional) {
            "⚠️ PROVISIONAL INVOICE (PENDING SERVER SYNC)\nOfficial AFF Invoice will be issued upon server sync.\n"
        } else {
            "✅ OFFICIAL TAX INVOICE\n"
        }

        val itemsSection = items.joinToString("\n") { item ->
            "• ${item.quantityPackets}x ${item.productName} (${item.weightKg} kg) @ ₹${String.format(Locale.ENGLISH, "%.2f", item.unitRate)} = ₹${String.format(Locale.ENGLISH, "%.2f", item.taxableAmount)}"
        }

        val modeLabel = when (paymentMode) {
            PaymentMode.CASH -> "Cash"
            PaymentMode.UPI_QR -> "UPI QR"
            PaymentMode.CHEQUE -> "Cheque"
            PaymentMode.CREDIT_15D -> "15D Credit"
        }

        return """
🌿 ABSOLUTE FRESH PVT. LTD.
FSSAI: 12723999000123 | GSTIN: 09AAHCA1234F1Z5
------------------------------------------------
$provisionalHeader
Invoice No: $invoiceNumber
Date: $date
Billed To: $outletName ($outletArea)
------------------------------------------------
ITEMS:
$itemsSection
------------------------------------------------
Subtotal: ₹${String.format(Locale.ENGLISH, "%.2f", subtotal)}
CGST (2.5%): ₹${String.format(Locale.ENGLISH, "%.2f", cgst)}
SGST (2.5%): ₹${String.format(Locale.ENGLISH, "%.2f", sgst)}
GRAND TOTAL: ₹${String.format(Locale.ENGLISH, "%.2f", grandTotal)}
Payment Mode: $modeLabel
------------------------------------------------
Pure Fermented Batter • Keep Refrigerated 0-4°C
Thank you for partnering with Absolute Fresh!
        """.trimIndent()
    }
}
