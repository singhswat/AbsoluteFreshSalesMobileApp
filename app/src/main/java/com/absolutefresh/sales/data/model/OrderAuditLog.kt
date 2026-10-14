package com.absolutefresh.sales.data.model

enum class AuditEventType(val labelEn: String, val labelHi: String) {
    ORDER_CREATED("Order Created", "ऑर्डर सृजित"),
    ITEMS_AMENDED("Items Amended", "सामान संशोधित"),
    PAYMENT_UPDATED("Payment Updated", "भुगतान अद्यतन"),
    DELIVERY_STATUS_CHANGED("Delivery Status", "वितरण स्थिति"),
    INVOICE_ISSUED("Invoice Issued", "चालान जारी"),
    SYNC_RESOLVED("Sync Conflict Resolved", "सिंक समाधान")
}

data class FieldDiff(
    val fieldName: String,
    val previousValue: String,
    val newValue: String
)

data class OrderAuditEntry(
    val id: String,
    val orderId: String,
    val eventType: AuditEventType,
    val timestamp: Long,
    val actorName: String,
    val actorRole: String, // "Sales Rep", "Admin", "Delivery Driver", "Sync Engine"
    val reason: String,
    val operationUuid: String,
    val diffs: List<FieldDiff> = emptyList(),
    val isConflictResolution: Boolean = false,
    val serverAuthoritativeValue: String? = null,
    val localProposedValue: String? = null
)
