package com.absolutefresh.sales.ui.orders

import com.absolutefresh.sales.data.model.FieldOrder
import com.absolutefresh.sales.data.model.Outlet
import com.absolutefresh.sales.data.model.PaymentMode
import com.absolutefresh.sales.data.model.Product

data class OrderProductItemUiState(
    val product: Product,
    val quantity: Int = 0,
    val unitPrice: Double,
    val lineTotal: Double = 0.0,
    val lineWeightKg: Double = 0.0
)

sealed class NewOrderUiState {
    object Loading : NewOrderUiState()

    data class Populated(
        val outlet: Outlet,
        val items: List<OrderProductItemUiState>,
        val paymentMode: PaymentMode = PaymentMode.CASH,
        val deliveryDateTimestamp: Long = System.currentTimeMillis() + 86400_000L,
        val notes: String = "",
        val subtotal: Double = 0.0,
        val gstAmount: Double = 0.0,
        val totalAmount: Double = 0.0,
        val totalWeightKg: Double = 0.0,
        val totalQuantity: Int = 0,
        val isValidOrder: Boolean = false,
        val isSubmitting: Boolean = false,
        val errorMessage: String? = null
    ) : NewOrderUiState()

    data class Submitted(val order: FieldOrder) : NewOrderUiState()

    data class Error(val message: String) : NewOrderUiState()
}
