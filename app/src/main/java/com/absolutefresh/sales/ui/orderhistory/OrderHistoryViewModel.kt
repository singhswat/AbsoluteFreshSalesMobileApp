package com.absolutefresh.sales.ui.orderhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.absolutefresh.sales.data.model.AuditEventType
import com.absolutefresh.sales.data.model.FieldDiff
import com.absolutefresh.sales.data.model.OrderAuditEntry
import com.absolutefresh.sales.data.repository.OrderHistoryRepository
import com.absolutefresh.sales.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class OrderHistoryViewModel(
    private val orderId: String,
    private val orderRepository: OrderRepository,
    private val orderHistoryRepository: OrderHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderHistoryUiState())
    val uiState: StateFlow<OrderHistoryUiState> = _uiState.asStateFlow()

    init {
        loadOrderAndHistory()
    }

    private fun loadOrderAndHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val order = orderRepository.getOrderById(orderId)
            _uiState.update { it.copy(order = order) }

            orderHistoryRepository.observeHistoryForOrder(orderId).collect { entries ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        timelineEntries = entries
                    )
                }
            }
        }
    }

    fun setFilter(type: AuditEventType?) {
        _uiState.update { it.copy(selectedFilter = type) }
    }

    fun openAddNoteDialog() {
        _uiState.update {
            it.copy(
                isAddNoteDialogOpen = true,
                amendmentReason = "",
                amendmentType = AuditEventType.ITEMS_AMENDED,
                errorMessage = null
            )
        }
    }

    fun closeAddNoteDialog() {
        _uiState.update { it.copy(isAddNoteDialogOpen = false, errorMessage = null) }
    }

    fun onAmendmentReasonChanged(reason: String) {
        _uiState.update { it.copy(amendmentReason = reason, errorMessage = null) }
    }

    fun onAmendmentTypeChanged(type: AuditEventType) {
        _uiState.update { it.copy(amendmentType = type) }
    }

    fun submitAmendmentNote() {
        val state = _uiState.value
        if (!state.isReasonValid) {
            _uiState.update { it.copy(errorMessage = "Reason must be at least 5 characters (कारण अनिवार्य है)") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }

            val newEntry = OrderAuditEntry(
                id = "audit-rep-${UUID.randomUUID().toString().take(8)}",
                orderId = orderId,
                eventType = state.amendmentType,
                timestamp = System.currentTimeMillis(),
                actorName = "Sales Rep (You)",
                actorRole = "Field Staff",
                reason = state.amendmentReason.trim(),
                operationUuid = "op-${UUID.randomUUID()}",
                diffs = listOf(
                    FieldDiff("Note / Revision", "None", state.amendmentReason.trim())
                )
            )

            orderHistoryRepository.addAuditEntry(newEntry)

            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    isAddNoteDialogOpen = false,
                    amendmentReason = "",
                    successMessage = "Revision note logged into audit trail (ऑडिट ट्रेल में दर्ज हुआ)"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
