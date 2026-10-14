package com.absolutefresh.sales.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.absolutefresh.sales.data.model.FieldOrder
import com.absolutefresh.sales.data.model.OrderDeliveryStatus
import com.absolutefresh.sales.data.model.OrderItem
import com.absolutefresh.sales.data.model.PaymentMode
import com.absolutefresh.sales.data.model.Product
import com.absolutefresh.sales.data.model.SyncStatus
import com.absolutefresh.sales.data.repository.OrderRepository
import com.absolutefresh.sales.data.repository.OutletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.round

class NewOrderViewModel(
    private val outletId: String,
    private val outletRepository: OutletRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NewOrderUiState>(NewOrderUiState.Loading)
    val uiState: StateFlow<NewOrderUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = NewOrderUiState.Loading
            val outlet = outletRepository.getOutletById(outletId)
            if (outlet == null) {
                _uiState.value = NewOrderUiState.Error("Outlet not found for ID: $outletId")
                return@launch
            }

            val products = orderRepository.getAvailableProducts()
            val items = products.map { product ->
                val unitPrice = calculateTierPrice(product, outlet.priceTier)
                OrderProductItemUiState(
                    product = product,
                    quantity = 0,
                    unitPrice = unitPrice,
                    lineTotal = 0.0,
                    lineWeightKg = 0.0
                )
            }

            _uiState.value = NewOrderUiState.Populated(
                outlet = outlet,
                items = items,
                paymentMode = PaymentMode.CASH,
                deliveryDateTimestamp = System.currentTimeMillis() + 86400_000L
            )
        }
    }

    fun onQuantityChanged(productId: String, delta: Int) {
        val currentState = _uiState.value as? NewOrderUiState.Populated ?: return
        val updatedItems = currentState.items.map { item ->
            if (item.product.id == productId) {
                val newQty = (item.quantity + delta).coerceAtLeast(0)
                calculateItemState(item, newQty)
            } else {
                item
            }
        }
        recalculateTotals(currentState, updatedItems)
    }

    fun onSetQuantity(productId: String, newQty: Int) {
        val currentState = _uiState.value as? NewOrderUiState.Populated ?: return
        val clampedQty = newQty.coerceAtLeast(0)
        val updatedItems = currentState.items.map { item ->
            if (item.product.id == productId) {
                calculateItemState(item, clampedQty)
            } else {
                item
            }
        }
        recalculateTotals(currentState, updatedItems)
    }

    fun onPaymentModeSelected(mode: PaymentMode) {
        _uiState.update { current ->
            if (current is NewOrderUiState.Populated) {
                current.copy(paymentMode = mode)
            } else current
        }
    }

    fun onNotesChanged(notes: String) {
        _uiState.update { current ->
            if (current is NewOrderUiState.Populated) {
                current.copy(notes = notes)
            } else current
        }
    }

    fun onDeliveryDateChanged(timestamp: Long) {
        _uiState.update { current ->
            if (current is NewOrderUiState.Populated) {
                current.copy(deliveryDateTimestamp = timestamp)
            } else current
        }
    }

    fun submitOrder(onSuccess: (FieldOrder) -> Unit) {
        val currentState = _uiState.value as? NewOrderUiState.Populated ?: return
        if (!currentState.isValidOrder) {
            _uiState.update {
                currentState.copy(errorMessage = "Please add at least 1 item to the order.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { currentState.copy(isSubmitting = true, errorMessage = null) }

            val orderItems = currentState.items
                .filter { it.quantity > 0 }
                .map { item ->
                    OrderItem(
                        productId = item.product.id,
                        productName = item.product.name,
                        quantityPackets = item.quantity,
                        weightKg = item.lineWeightKg,
                        unitPrice = item.unitPrice,
                        lineTotal = item.lineTotal
                    )
                }

            val dateFormat = SimpleDateFormat("yyMM", Locale.ENGLISH)
            val monthYear = dateFormat.format(Date())
            val randomSeq = (100..999).random()
            val provInvoice = "PROV-$monthYear-$randomSeq"
            val orderId = "ord-${UUID.randomUUID().toString().take(8)}"

            val newOrder = FieldOrder(
                id = orderId,
                outletId = currentState.outlet.id,
                outletName = currentState.outlet.name,
                repId = "rep-01",
                items = orderItems,
                subtotal = currentState.subtotal,
                gstAmount = currentState.gstAmount,
                totalAmount = currentState.totalAmount,
                paymentMode = currentState.paymentMode,
                provisionalInvoiceNumber = provInvoice,
                syncedInvoiceNumber = null,
                syncStatus = SyncStatus.PENDING,
                deliveryStatus = OrderDeliveryStatus.PENDING,
                createdAt = System.currentTimeMillis()
            )

            val created = orderRepository.createOrder(newOrder)
            _uiState.value = NewOrderUiState.Submitted(created)
            onSuccess(created)
        }
    }

    private fun calculateTierPrice(product: Product, tier: Int): Double {
        val rawPrice = when (tier) {
            1 -> product.tier4Price * 0.85
            2 -> product.tier4Price * 0.90
            3 -> product.tier4Price * 0.95
            else -> product.tier4Price
        }
        return round(rawPrice * 100.0) / 100.0
    }

    private fun calculateItemState(item: OrderProductItemUiState, qty: Int): OrderProductItemUiState {
        val lineTotal = round((qty * item.unitPrice) * 100.0) / 100.0
        val lineWeight = round((qty * item.product.packWeightGrams / 1000.0) * 100.0) / 100.0
        return item.copy(
            quantity = qty,
            lineTotal = lineTotal,
            lineWeightKg = lineWeight
        )
    }

    private fun recalculateTotals(
        currentState: NewOrderUiState.Populated,
        items: List<OrderProductItemUiState>
    ) {
        val subtotal = round(items.sumOf { it.lineTotal } * 100.0) / 100.0
        val gstAmount = round((subtotal * 0.05) * 100.0) / 100.0
        val totalAmount = round((subtotal + gstAmount) * 100.0) / 100.0
        val totalWeight = round(items.sumOf { it.lineWeightKg } * 100.0) / 100.0
        val totalQty = items.sumOf { it.quantity }

        _uiState.update {
            currentState.copy(
                items = items,
                subtotal = subtotal,
                gstAmount = gstAmount,
                totalAmount = totalAmount,
                totalWeightKg = totalWeight,
                totalQuantity = totalQty,
                isValidOrder = totalQty > 0,
                errorMessage = null
            )
        }
    }
}
