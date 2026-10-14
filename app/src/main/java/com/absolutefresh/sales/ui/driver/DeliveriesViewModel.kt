package com.absolutefresh.sales.ui.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.absolutefresh.sales.data.model.OrderDeliveryStatus
import com.absolutefresh.sales.data.model.PaymentMode
import com.absolutefresh.sales.data.repository.DeliveryItem
import com.absolutefresh.sales.data.repository.DeliveryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DeliveriesViewModel(
    private val repository: DeliveryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DeliveriesUiState>(DeliveriesUiState.Loading)
    val uiState: StateFlow<DeliveriesUiState> = _uiState.asStateFlow()

    private var allDeliveries: List<DeliveryItem> = emptyList()
    private var confirmState: ConfirmDeliveryDialogState = ConfirmDeliveryDialogState()

    init {
        observeDeliveries()
    }

    private fun observeDeliveries() {
        viewModelScope.launch {
            _uiState.value = DeliveriesUiState.Loading
            try {
                repository.observeDeliveries().collectLatest { deliveries ->
                    allDeliveries = deliveries
                    updateState()
                }
            } catch (e: Exception) {
                _uiState.value = DeliveriesUiState.Error(
                    e.message ?: "Failed to load deliveries"
                )
            }
        }
    }

    fun openConfirmDelivery(delivery: DeliveryItem) {
        confirmState = ConfirmDeliveryDialogState(
            isOpen = true,
            delivery = delivery,
            deliveredPackets = delivery.orderedPackets,
            amountCollected = delivery.invoiceAmount,
            paymentMode = delivery.paymentMode,
            podMethod = "OTP Code",
            shortDeliveryReason = ""
        )
        updateState()
    }

    fun closeConfirmDelivery() {
        confirmState = ConfirmDeliveryDialogState(isOpen = false)
        updateState()
    }

    fun updateDeliveredPackets(packets: Int) {
        confirmState = confirmState.copy(deliveredPackets = packets.coerceAtLeast(0))
        updateState()
    }

    fun updateAmountCollected(amount: Double) {
        confirmState = confirmState.copy(amountCollected = amount.coerceAtLeast(0.0))
        updateState()
    }

    fun updatePaymentMode(mode: PaymentMode) {
        confirmState = confirmState.copy(paymentMode = mode)
        updateState()
    }

    fun updatePodMethod(method: String) {
        confirmState = confirmState.copy(podMethod = method)
        updateState()
    }

    fun updateShortDeliveryReason(reason: String) {
        confirmState = confirmState.copy(shortDeliveryReason = reason)
        updateState()
    }

    fun submitConfirmDelivery() {
        val delivery = confirmState.delivery ?: return
        if (!confirmState.isValid) return

        viewModelScope.launch {
            confirmState = confirmState.copy(isSubmitting = true)
            updateState()

            val podFinal = if (confirmState.isShortDelivery) {
                "${confirmState.podMethod} • Short: ${confirmState.shortDeliveryReason}"
            } else {
                confirmState.podMethod
            }

            repository.confirmDelivery(
                deliveryId = delivery.id,
                deliveredPackets = confirmState.deliveredPackets,
                amountCollected = confirmState.amountCollected,
                paymentMode = confirmState.paymentMode,
                podMethod = podFinal
            )

            confirmState = ConfirmDeliveryDialogState(isOpen = false)
            updateState()
        }
    }

    private fun updateState() {
        val pending = allDeliveries.filter { it.deliveryStatus == OrderDeliveryStatus.PENDING }
        val completed = allDeliveries.filter { it.deliveryStatus == OrderDeliveryStatus.DELIVERED }
        val totalCash = completed.filter { it.paymentMode == PaymentMode.CASH }.sumOf { it.amountCollected }

        _uiState.value = DeliveriesUiState.Populated(
            pendingDeliveries = pending,
            completedDeliveries = completed,
            totalPlannedCount = allDeliveries.size,
            totalCompletedCount = completed.size,
            totalCollectedCash = totalCash,
            confirmState = confirmState
        )
    }
}
