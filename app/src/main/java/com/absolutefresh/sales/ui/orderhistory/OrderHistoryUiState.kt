package com.absolutefresh.sales.ui.orderhistory

import com.absolutefresh.sales.data.model.AuditEventType
import com.absolutefresh.sales.data.model.FieldOrder
import com.absolutefresh.sales.data.model.OrderAuditEntry

data class OrderHistoryUiState(
    val isLoading: Boolean = true,
    val order: FieldOrder? = null,
    val timelineEntries: List<OrderAuditEntry> = emptyList(),
    val selectedFilter: AuditEventType? = null,
    val isAddNoteDialogOpen: Boolean = false,
    val amendmentReason: String = "",
    val amendmentType: AuditEventType = AuditEventType.ITEMS_AMENDED,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val filteredEntries: List<OrderAuditEntry>
        get() = if (selectedFilter == null) {
            timelineEntries
        } else {
            timelineEntries.filter { it.eventType == selectedFilter }
        }

    val isReasonValid: Boolean
        get() = amendmentReason.trim().length >= 5
}
