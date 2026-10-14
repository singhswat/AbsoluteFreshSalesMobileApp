package com.absolutefresh.sales

import com.absolutefresh.sales.data.model.FieldOrder
import com.absolutefresh.sales.data.model.OrderDeliveryStatus
import com.absolutefresh.sales.data.model.OrderItem
import com.absolutefresh.sales.data.model.PaymentMode
import com.absolutefresh.sales.data.model.Product
import com.absolutefresh.sales.data.model.SyncStatus
import com.absolutefresh.sales.data.repository.OrderRepository
import com.absolutefresh.sales.ui.orders.OrderFilter
import com.absolutefresh.sales.ui.orders.OrdersUiState
import com.absolutefresh.sales.ui.orders.OrdersViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrdersViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: TestOrderRepository
    private lateinit var viewModel: OrdersViewModel

    private val now = System.currentTimeMillis()
    private val oneDay = 86400_000L

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = TestOrderRepository()
        viewModel = OrdersViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load populates orders and calculates pending sync count`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected Populated state but was $state", state is OrdersUiState.Populated)
        val populated = state as OrdersUiState.Populated
        assertEquals(3, populated.orders.size)
        assertEquals(3, populated.filteredOrders.size)
        // 1 Synced, 1 Pending, 1 Failed -> 2 pendingSyncCount
        assertEquals(2, populated.pendingSyncCount)
    }

    @Test
    fun `filter by PENDING returns only pending delivery orders`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setFilter(OrderFilter.PENDING)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as OrdersUiState.Populated
        assertEquals(1, state.filteredOrders.size)
        assertEquals("o1", state.filteredOrders.first().outletId)
    }

    @Test
    fun `filter by DELIVERED returns only delivered orders`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setFilter(OrderFilter.DELIVERED)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as OrdersUiState.Populated
        assertEquals(2, state.filteredOrders.size)
        assertTrue(state.filteredOrders.all { it.deliveryStatus == OrderDeliveryStatus.DELIVERED })
    }

    @Test
    fun `search query filters orders by outlet name`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setSearchQuery("Sharma")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as OrdersUiState.Populated
        assertEquals(1, state.filteredOrders.size)
        assertEquals("Sharma Kirana", state.filteredOrders.first().outletName)
    }

    @Test
    fun `search query filters orders by provisional or synced invoice`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setSearchQuery("AFF-2610-0101")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as OrdersUiState.Populated
        assertEquals(1, state.filteredOrders.size)
        assertEquals("AFF-2610-0101", state.filteredOrders.first().syncedInvoiceNumber)
    }

    @Test
    fun `selecting and clearing order updates selectedOrder state`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val order = (viewModel.uiState.value as OrdersUiState.Populated).orders.first()
        viewModel.selectOrder(order)
        testDispatcher.scheduler.advanceUntilIdle()

        var state = viewModel.uiState.value as OrdersUiState.Populated
        assertNotNull(state.selectedOrder)
        assertEquals(order.id, state.selectedOrder?.id)

        viewModel.selectOrder(null)
        testDispatcher.scheduler.advanceUntilIdle()
        state = viewModel.uiState.value as OrdersUiState.Populated
        assertNull(state.selectedOrder)
    }

    private class TestOrderRepository : OrderRepository {
        val now = System.currentTimeMillis()
        val testOrders = listOf(
            FieldOrder(
                id = "ord-1",
                outletId = "o1",
                outletName = "Sharma Kirana",
                repId = "rep-1",
                items = listOf(OrderItem("p1", "Batter 500g", 10, 5.0, 45.0, 450.0)),
                subtotal = 450.0,
                gstAmount = 22.5,
                totalAmount = 472.5,
                paymentMode = PaymentMode.CASH,
                provisionalInvoiceNumber = "PROV-001",
                syncedInvoiceNumber = null,
                syncStatus = SyncStatus.PENDING,
                deliveryStatus = OrderDeliveryStatus.PENDING,
                createdAt = now
            ),
            FieldOrder(
                id = "ord-2",
                outletId = "o2",
                outletName = "Gupta Dairy",
                repId = "rep-1",
                items = listOf(OrderItem("p1", "Batter 500g", 20, 10.0, 45.0, 900.0)),
                subtotal = 900.0,
                gstAmount = 45.0,
                totalAmount = 945.0,
                paymentMode = PaymentMode.UPI_QR,
                provisionalInvoiceNumber = "PROV-002",
                syncedInvoiceNumber = "AFF-2610-0101",
                syncStatus = SyncStatus.SYNCED,
                deliveryStatus = OrderDeliveryStatus.DELIVERED,
                createdAt = now
            ),
            FieldOrder(
                id = "ord-3",
                outletId = "o3",
                outletName = "Krishna Sweets",
                repId = "rep-1",
                items = listOf(OrderItem("p2", "Batter 1kg", 15, 15.0, 85.0, 1275.0)),
                subtotal = 1275.0,
                gstAmount = 63.75,
                totalAmount = 1338.75,
                paymentMode = PaymentMode.CREDIT_15D,
                provisionalInvoiceNumber = "PROV-003",
                syncedInvoiceNumber = null,
                syncStatus = SyncStatus.FAILED,
                deliveryStatus = OrderDeliveryStatus.DELIVERED,
                createdAt = now - (2 * 86400_000L)
            )
        )

        private val flow = MutableStateFlow(testOrders)

        override fun observeOrders(): Flow<List<FieldOrder>> = flow.asStateFlow()
        override suspend fun getOrders(): List<FieldOrder> = flow.value
        override suspend fun getOrderById(id: String): FieldOrder? = flow.value.find { it.id == id }
        override suspend fun createOrder(order: FieldOrder): FieldOrder = order
        override suspend fun getAvailableProducts(): List<Product> = emptyList()
    }
}
