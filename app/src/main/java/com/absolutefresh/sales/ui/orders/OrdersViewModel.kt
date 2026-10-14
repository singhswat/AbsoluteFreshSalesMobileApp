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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var allOrders: List<FieldOrder> = emptyList()
    private var currentFilter: OrderFilter = OrderFilter.ALL
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
        _searchQuery.value = query
        applyFilterAndSearch()
    }

    fun selectOrder(order: FieldOrder?) {
        activeSelectedOrder = order
        applyFilterAndSearch()
    }

    private fun applyFilterAndSearch() {
        val query = _searchQuery.value.trim().lowercase()
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

            val matchesSearch = if (query.isBlank()) {
                true
            } else {
                order.outletName.lowercase().contains(query) ||
                        order.provisionalInvoiceNumber.lowercase().contains(query) ||
                        (order.syncedInvoiceNumber?.lowercase()?.contains(query) == true) ||
                        order.outletId.lowercase().contains(query)
            }

            matchesFilter && matchesSearch
        }

        val pendingSync = allOrders.count { it.syncStatus != SyncStatus.SYNCED }

        _uiState.value = if (allOrders.isEmpty() && query.isBlank()) {
            OrdersUiState.Empty
        } else {
            OrdersUiState.Populated(
                orders = allOrders,
                filteredOrders = filtered,
                selectedFilter = currentFilter,
                searchQuery = _searchQuery.value,
                selectedOrder = activeSelectedOrder,
                pendingSyncCount = pendingSync
            )
        }
    }
}
