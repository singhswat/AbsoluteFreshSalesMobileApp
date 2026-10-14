package com.absolutefresh.sales.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.absolutefresh.sales.data.model.FieldOrder
import com.absolutefresh.sales.data.model.OrderDeliveryStatus
import com.absolutefresh.sales.data.model.SyncStatus
import com.absolutefresh.sales.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class OrdersViewModel(
    private val repository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<OrdersUiState>(OrdersUiState.Loading)
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    private var allOrders: List<FieldOrder> = emptyList()
    private var currentFilter: OrderFilter = OrderFilter.ALL
    private var currentSearch: String = ""
    private var activeSelectedOrder: FieldOrder? = null

    init {
        observeOrders()
    }

    private fun observeOrders() {
        viewModelScope.launch {
            _uiState.value = OrdersUiState.Loading
            try {
                repository.observeOrders().collectLatest { orders ->
                    allOrders = orders
                    applyFilterAndSearch()
                }
            } catch (e: Exception) {
                _uiState.value = OrdersUiState.Error(
                    e.message ?: "Could not load orders. Pull down to refresh."
                )
            }
        }
    }

    fun setFilter(filter: OrderFilter) {
        currentFilter = filter
        applyFilterAndSearch()
    }

    fun setSearchQuery(query: String) {
        currentSearch = query
        applyFilterAndSearch()
    }

    fun selectOrder(order: FieldOrder?) {
        activeSelectedOrder = order
        applyFilterAndSearch()
    }

    private fun applyFilterAndSearch() {
        if (allOrders.isEmpty()) {
            _uiState.value = OrdersUiState.Empty
            return
        }

        val today = LocalDate.now()
        val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)

        val filtered = allOrders.filter { order ->
            val matchesFilter = when (currentFilter) {
                OrderFilter.ALL -> true
                OrderFilter.TODAY -> {
                    val orderDate = Instant.ofEpochMilli(order.createdAt)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    orderDate == today
                }
                OrderFilter.THIS_WEEK -> order.createdAt >= oneWeekAgo
                OrderFilter.PENDING -> order.deliveryStatus == OrderDeliveryStatus.PENDING
                OrderFilter.DELIVERED -> order.deliveryStatus == OrderDeliveryStatus.DELIVERED
            }

            val matchesSearch = if (currentSearch.isBlank()) {
                true
            } else {
                val q = currentSearch.trim().lowercase()
                order.outletName.lowercase().contains(q) ||
                        order.provisionalInvoiceNumber.lowercase().contains(q) ||
                        (order.syncedInvoiceNumber?.lowercase()?.contains(q) == true)
            }

            matchesFilter && matchesSearch
        }

        val pendingSync = allOrders.count { it.syncStatus != SyncStatus.SYNCED }

        _uiState.value = OrdersUiState.Populated(
            orders = allOrders,
            filteredOrders = filtered,
            selectedFilter = currentFilter,
            searchQuery = currentSearch,
            selectedOrder = activeSelectedOrder,
            pendingSyncCount = pendingSync
        )
    }
}
