package com.absolutefresh.sales

import com.absolutefresh.sales.data.model.Outlet
import com.absolutefresh.sales.data.model.OutletType
import com.absolutefresh.sales.data.model.PaymentMode
import com.absolutefresh.sales.data.model.Product
import com.absolutefresh.sales.data.model.SyncStatus
import com.absolutefresh.sales.data.repository.FakeOrderRepository
import com.absolutefresh.sales.data.repository.FakeOutletRepository
import com.absolutefresh.sales.ui.orders.NewOrderUiState
import com.absolutefresh.sales.ui.orders.NewOrderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class NewOrderViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var outletRepo: FakeOutletRepository
    private lateinit var orderRepo: FakeOrderRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        outletRepo = FakeOutletRepository()
        orderRepo = FakeOrderRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load populates outlet details and available products with zero qty`() = runTest {
        val viewModel = NewOrderViewModel("o1", outletRepo, orderRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("State should be Populated", state is NewOrderUiState.Populated)
        val populated = state as NewOrderUiState.Populated

        assertEquals("Sharma Kirana", populated.outlet.name)
        assertEquals(3, populated.outlet.priceTier)
        assertEquals(4, populated.items.size)
        assertEquals(0, populated.totalQuantity)
        assertEquals(0.0, populated.totalAmount, 0.001)
        assertFalse(populated.isValidOrder)
    }

    @Test
    fun `pricing calculates tier discounts correctly`() = runTest {
        // o1 has priceTier = 3 (5% off tier 4 price)
        val viewModelTier3 = NewOrderViewModel("o1", outletRepo, orderRepo)
        testDispatcher.scheduler.advanceUntilIdle()
        val populatedTier3 = viewModelTier3.uiState.value as NewOrderUiState.Populated
        val itemTier3 = populatedTier3.items.first { it.product.id == "p1" } // tier4 is 45.0, 5% off = 42.75
        assertEquals(42.75, itemTier3.unitPrice, 0.01)

        // o2 has priceTier = 4 (base price)
        val viewModelTier4 = NewOrderViewModel("o2", outletRepo, orderRepo)
        testDispatcher.scheduler.advanceUntilIdle()
        val populatedTier4 = viewModelTier4.uiState.value as NewOrderUiState.Populated
        val itemTier4 = populatedTier4.items.first { it.product.id == "p1" }
        assertEquals(45.0, itemTier4.unitPrice, 0.01)

        // o3 has priceTier = 2 (10% off tier 4 price)
        val viewModelTier2 = NewOrderViewModel("o3", outletRepo, orderRepo)
        testDispatcher.scheduler.advanceUntilIdle()
        val populatedTier2 = viewModelTier2.uiState.value as NewOrderUiState.Populated
        val itemTier2 = populatedTier2.items.first { it.product.id == "p1" } // 45.0 * 0.9 = 40.5
        assertEquals(40.5, itemTier2.unitPrice, 0.01)
    }

    @Test
    fun `incrementing and decrementing quantity calculates subtotals and 5 percent GST`() = runTest {
        val viewModel = NewOrderViewModel("o2", outletRepo, orderRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        // Increment p1 (500g batter, 45.0/unit) by 2 packets -> 1.0 kg, subtotal 90.0
        viewModel.onQuantityChanged("p1", 2)
        var populated = viewModel.uiState.value as NewOrderUiState.Populated
        assertEquals(2, populated.totalQuantity)
        assertEquals(1.0, populated.totalWeightKg, 0.01)
        assertEquals(90.0, populated.subtotal, 0.01)
        assertEquals(4.5, populated.gstAmount, 0.01) // 5% GST
        assertEquals(94.5, populated.totalAmount, 0.01)
        assertTrue(populated.isValidOrder)

        // Increment p2 (1kg batter, 85.0/unit) by 1 packet -> 1.0 kg, total 2.0 kg, subtotal 175.0
        viewModel.onQuantityChanged("p2", 1)
        populated = viewModel.uiState.value as NewOrderUiState.Populated
        assertEquals(3, populated.totalQuantity)
        assertEquals(2.0, populated.totalWeightKg, 0.01)
        assertEquals(175.0, populated.subtotal, 0.01)
        assertEquals(8.75, populated.gstAmount, 0.01)
        assertEquals(183.75, populated.totalAmount, 0.01)

        // Decrement p1 by 1 packet
        viewModel.onQuantityChanged("p1", -1)
        populated = viewModel.uiState.value as NewOrderUiState.Populated
        assertEquals(2, populated.totalQuantity)
        assertEquals(130.0, populated.subtotal, 0.01) // 45 + 85
        assertEquals(6.5, populated.gstAmount, 0.01)
        assertEquals(136.5, populated.totalAmount, 0.01)
    }

    @Test
    fun `submitting with zero quantity does not submit and shows validation message`() = runTest {
        val viewModel = NewOrderViewModel("o1", outletRepo, orderRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        var callbackInvoked = false
        viewModel.submitOrder { callbackInvoked = true }

        assertFalse(callbackInvoked)
        val populated = viewModel.uiState.value as NewOrderUiState.Populated
        assertNotNull(populated.errorMessage)
    }

    @Test
    fun `successful order submission stores order with provisional invoice and client UUID`() = runTest {
        val viewModel = NewOrderViewModel("o1", outletRepo, orderRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onQuantityChanged("p1", 10)
        viewModel.onPaymentModeSelected(PaymentMode.UPI_QR)
        viewModel.onNotesChanged("Deliver by 7 AM")

        var placedOrder: com.absolutefresh.sales.data.model.FieldOrder? = null
        viewModel.submitOrder { order ->
            placedOrder = order
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(placedOrder)
        assertEquals("o1", placedOrder?.outletId)
        assertEquals("Sharma Kirana", placedOrder?.outletName)
        assertEquals(PaymentMode.UPI_QR, placedOrder?.paymentMode)
        assertTrue(placedOrder?.provisionalInvoiceNumber?.startsWith("PROV-") == true)
        assertEquals(SyncStatus.PENDING, placedOrder?.syncStatus)
        assertEquals(1, placedOrder?.items?.size)

        // Verify order was also persisted to order repository
        val inRepo = orderRepo.getOrderById(placedOrder!!.id)
        assertNotNull(inRepo)
        assertEquals(placedOrder?.id, inRepo?.id)
    }

    @Test
    fun `non existent outlet ID transitions to Error state`() = runTest {
        val viewModel = NewOrderViewModel("non-existent-id", outletRepo, orderRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("State should be Error", state is NewOrderUiState.Error)
        assertEquals("Outlet not found for ID: non-existent-id", (state as NewOrderUiState.Error).message)
    }
}
