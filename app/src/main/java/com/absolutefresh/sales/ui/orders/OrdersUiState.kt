package com.absolutefresh.sales.ui.orders

import com.absolutefresh.sales.data.model.FieldOrder

enum class OrderFilter(val labelEn: String, val labelHi: String) {
    ALL("All", "सभी"),
    TODAY("Today", "आज"),
    THIS_WEEK("This Week", "इस सप्ताह"),
    PENDING("Pending", "लंबित"),
    DELIVERED("Delivered", "वितरित")
}

sealed class OrdersUiState {
    object Loading : OrdersUiState()

    object Empty : OrdersUiState()

    data class Populated(
        val orders: List<FieldOrder>,
        val filteredOrders: List<FieldOrder>,
        val selectedFilter: OrderFilter = OrderFilter.ALL,
        val searchQuery: String = "",
        val selectedOrder: FieldOrder? = null,
        val pendingSyncCount: Int = 0
    ) : OrdersUiState()

    data class Error(val message: String) : OrdersUiState()
}
