package com.absolutefresh.sales

import com.absolutefresh.sales.data.model.OrderDeliveryStatus
import com.absolutefresh.sales.data.model.PaymentMode
import com.absolutefresh.sales.data.model.SyncStatus
import com.absolutefresh.sales.data.repository.DeliveryItem
import com.absolutefresh.sales.data.repository.DeliveryRepository
import com.absolutefresh.sales.ui.driver.DeliveriesUiState
import com.absolutefresh.sales.ui.driver.DeliveriesViewModel
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeliveriesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: TestDeliveryRepository
    private lateinit var viewModel: DeliveriesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = TestDeliveryRepository()
        viewModel = DeliveriesViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load separates pending and completed deliveries`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is DeliveriesUiState.Populated)
        val populated = state as DeliveriesUiState.Populated
        assertEquals(2, populated.completedDeliveries.size)
        assertEquals(1, populated.pendingDeliveries.size)
        assertEquals(3, populated.totalPlannedCount)
        assertEquals(2, populated.totalCompletedCount)
        assertEquals(1239.0, populated.totalCollectedCash, 0.01)
    }

    @Test
    fun `open confirm delivery sets default order values`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val pending = (viewModel.uiState.value as DeliveriesUiState.Populated).pendingDeliveries.first()
        viewModel.openConfirmDelivery(pending)

        val confirmState = (viewModel.uiState.value as DeliveriesUiState.Populated).confirmState
        assertTrue(confirmState.isOpen)
        assertEquals(pending.orderedPackets, confirmState.deliveredPackets)
        assertEquals(pending.invoiceAmount, confirmState.amountCollected, 0.01)
        assertFalse(confirmState.isShortDelivery)
        assertTrue(confirmState.isValid)
    }

    @Test
    fun `short delivery requires reason before becoming valid`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val pending = (viewModel.uiState.value as DeliveriesUiState.Populated).pendingDeliveries.first()
        viewModel.openConfirmDelivery(pending)

        // Drop fewer than ordered
        viewModel.updateDeliveredPackets(pending.orderedPackets - 5)
        var confirmState = (viewModel.uiState.value as DeliveriesUiState.Populated).confirmState
        assertTrue(confirmState.isShortDelivery)
        assertFalse("Short delivery without reason must be invalid", confirmState.isValid)

        viewModel.updateShortDeliveryReason("Retailer chiller full")
        confirmState = (viewModel.uiState.value as DeliveriesUiState.Populated).confirmState
        assertTrue("Short delivery with reason must be valid", confirmState.isValid)
    }

    @Test
    fun `confirming delivery transitions delivery to completed list and updates cash`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val pending = (viewModel.uiState.value as DeliveriesUiState.Populated).pendingDeliveries.first()
        viewModel.openConfirmDelivery(pending)
        viewModel.updatePaymentMode(PaymentMode.CASH)
        viewModel.updateAmountCollected(2000.0)
        viewModel.submitConfirmDelivery()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as DeliveriesUiState.Populated
        assertEquals(0, state.pendingDeliveries.size)
        assertEquals(3, state.completedDeliveries.size)
        assertEquals(1239.0 + 2000.0, state.totalCollectedCash, 0.01)
        assertFalse(state.confirmState.isOpen)
    }

    private class TestDeliveryRepository : DeliveryRepository {
        val testDeliveries = mutableListOf(
            DeliveryItem("d1", "ord-1", "o1", "Store 1", "Addr 1", "9000000001", 1, 30, 15.0, "AFF-101", 1239.0, deliveredPackets = 30, amountCollected = 1239.0, paymentMode = PaymentMode.CASH, deliveryStatus = OrderDeliveryStatus.DELIVERED, completedTimestamp = System.currentTimeMillis()),
            DeliveryItem("d2", "ord-2", "o2", "Store 2", "Addr 2", "9000000002", 2, 40, 20.0, "AFF-102", 2310.0, deliveredPackets = 40, amountCollected = 2310.0, paymentMode = PaymentMode.UPI_QR, deliveryStatus = OrderDeliveryStatus.DELIVERED, completedTimestamp = System.currentTimeMillis()),
            DeliveryItem("d3", "ord-3", "o3", "Store 3", "Addr 3", "9000000003", 3, 25, 12.5, "PROV-103", 2152.5, deliveredPackets = 0, amountCollected = 0.0, paymentMode = PaymentMode.CASH, deliveryStatus = OrderDeliveryStatus.PENDING)
        )

        val flow = MutableStateFlow<List<DeliveryItem>>(testDeliveries)

        override fun observeDeliveries(): Flow<List<DeliveryItem>> = flow.asStateFlow()
        override suspend fun getDeliveries(): List<DeliveryItem> = flow.value
        override suspend fun getDeliveryById(deliveryId: String): DeliveryItem? = flow.value.find { it.id == deliveryId }

        override suspend fun confirmDelivery(
            deliveryId: String,
            deliveredPackets: Int,
            amountCollected: Double,
            paymentMode: PaymentMode,
            podMethod: String
        ): DeliveryItem {
            val target = flow.value.find { it.id == deliveryId }!!
            val updated = target.copy(
                deliveredPackets = deliveredPackets,
                amountCollected = amountCollected,
                paymentMode = paymentMode,
                podMethod = podMethod,
                deliveryStatus = OrderDeliveryStatus.DELIVERED,
                completedTimestamp = System.currentTimeMillis(),
                syncStatus = SyncStatus.PENDING
            )
            flow.value = flow.value.map { if (it.id == deliveryId) updated else it }
            return updated
        }
    }
}
