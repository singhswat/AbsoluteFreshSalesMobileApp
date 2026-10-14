package com.absolutefresh.sales.data.model

data class Product(
    val id: String,
    val name: String,
    val sku: String,
    val packWeightGrams: Int,
    val mrp: Double,
    val tier4Price: Double,
    val shelfLifeDays: Int = 10
)

enum class PaymentMode {
    CASH,
    UPI_QR,
    CREDIT_15D
}

data class OrderItem(
    val productId: String,
    val productName: String,
    val quantityPackets: Int,
    val weightKg: Double,
    val unitPrice: Double,
    val lineTotal: Double
)

enum class OrderDeliveryStatus {
    PENDING,
    DISPATCHED,
    DELIVERED,
    CANCELLED
}

data class FieldOrder(
    val id: String,
    val outletId: String,
    val outletName: String = "",
    val repId: String,
    val items: List<OrderItem>,
    val subtotal: Double,
    val gstAmount: Double,
    val totalAmount: Double,
    val paymentMode: PaymentMode,
    val provisionalInvoiceNumber: String,
    val syncedInvoiceNumber: String? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val deliveryStatus: OrderDeliveryStatus = OrderDeliveryStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)

data class CashTransaction(
    val id: String,
    val outletId: String,
    val outletName: String,
    val amount: Double,
    val invoiceRef: String,
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val timestamp: Long = System.currentTimeMillis(),
    val isCleared: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.PENDING
)
