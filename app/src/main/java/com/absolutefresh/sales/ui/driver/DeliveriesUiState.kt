package com.absolutefresh.sales.ui.driver

import com.absolutefresh.sales.data.model.PaymentMode
import com.absolutefresh.sales.data.repository.DeliveryItem

data class ConfirmDeliveryDialogState(
    val isOpen: Boolean = false,
    val delivery: DeliveryItem? = null,
    val deliveredPackets: Int = 0,
    val amountCollected: Double = 0.0,
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val podMethod: String = "OTP Code",
    val shortDeliveryReason: String = "",
    val isSubmitting: Boolean = false
) {
    val isShortDelivery: Boolean
        get() = delivery != null && deliveredPackets < delivery.orderedPackets

    val isValid: Boolean
        get() = delivery != null &&
                deliveredPackets >= 0 &&
                (!isShortDelivery || shortDeliveryReason.isNotBlank())
}

sealed class DeliveriesUiState {
    object Loading : DeliveriesUiState()

    data class Populated(
        val pendingDeliveries: List<DeliveryItem> = emptyList(),
        val completedDeliveries: List<DeliveryItem> = emptyList(),
        val totalPlannedCount: Int = 0,
        val totalCompletedCount: Int = 0,
        val totalCollectedCash: Double = 0.0,
        val confirmState: ConfirmDeliveryDialogState = ConfirmDeliveryDialogState()
    ) : DeliveriesUiState()

    data class Error(val message: String) : DeliveriesUiState()
}
