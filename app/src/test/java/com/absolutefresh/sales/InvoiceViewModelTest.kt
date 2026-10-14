package com.absolutefresh.sales

import com.absolutefresh.sales.data.model.FieldOrder
import com.absolutefresh.sales.data.model.OrderItem
import com.absolutefresh.sales.data.model.Outlet
import com.absolutefresh.sales.data.model.OutletType
import com.absolutefresh.sales.data.model.PaymentMode
import com.absolutefresh.sales.data.model.SyncStatus
import com.absolutefresh.sales.data.repository.FakeOrderRepository
import com.absolutefresh.sales.data.repository.FakeOutletRepository
import com.absolutefresh.sales.ui.invoice.InvoiceUiState
import com.absolutefresh.sales.ui.invoice.InvoiceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
class InvoiceViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var orderRepository: FakeOrderRepository
    private lateinit var outletRepository: FakeOutletRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        orderRepository = FakeOrderRepository()
        outletRepository = FakeOutletRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadInvoice_existingProvisionalOrder_emitsSuccessWithExactTaxBreakdown() = runTest {
        // Create an order with known figures: 2x 500g @ ₹40.50 = ₹81.00 taxable
        val testOrder = FieldOrder(
            id = "test-ord-001",
            outletId = "o4", // New Delhi Canteen
            outletName = "New Delhi Canteen",
            repId = "rep-01",
            items = listOf(
                OrderItem(
                    productId = "p1",
                    productName = "Fresh Idli Dosa Batter 500g",
                    quantityPackets = 2,
                    weightKg = 1.0,
                    unitPrice = 40.50,
                    lineTotal = 81.00
                )
            ),
            subtotal = 81.00,
            gstAmount = 4.05,
            totalAmount = 85.05,
            paymentMode = PaymentMode.CASH,
            provisionalInvoiceNumber = "PROV-2610-674",
            syncedInvoiceNumber = null,
            syncStatus = SyncStatus.PENDING
        )
        orderRepository.createOrder(testOrder)

        val viewModel = InvoiceViewModel(
            orderId = "test-ord-001",
            orderRepository = orderRepository,
            outletRepository = outletRepository
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("State should be Success", state is InvoiceUiState.Success)
        val success = state as InvoiceUiState.Success

        assertEquals("New Delhi Canteen", success.outletName)
        assertEquals("PROV-2610-674", success.invoiceNumber)
        assertTrue("Should be provisional", success.isProvisional)
        assertEquals(81.00, success.subtotal, 0.01)
        assertEquals(2.03, success.cgstTotal, 0.01)
        assertEquals(2.02, success.sgstTotal, 0.01)
        assertEquals(85.05, success.grandTotal, 0.01)
        assertEquals(1, success.items.size)
        assertEquals("190590", success.items[0].hsnCode)
        assertEquals(2, success.items[0].quantityPackets)
        assertEquals(1.0, success.items[0].weightKg, 0.01)
    }

    @Test
    fun loadInvoice_syncedOrder_emitsNonProvisionalStateWithSyncedInvoiceNumber() = runTest {
        val syncedOrder = FieldOrder(
            id = "test-ord-002",
            outletId = "o1",
            outletName = "Sharma Kirana",
            repId = "rep-01",
            items = listOf(
                OrderItem(
                    productId = "p1",
                    productName = "Fresh Idli Dosa Batter 500g",
                    quantityPackets = 10,
                    weightKg = 5.0,
                    unitPrice = 45.0,
                    lineTotal = 450.0
                )
            ),
            subtotal = 450.0,
            gstAmount = 22.50,
            totalAmount = 472.50,
            paymentMode = PaymentMode.UPI_QR,
            provisionalInvoiceNumber = "PROV-2610-001",
            syncedInvoiceNumber = "AFF-2610-0101",
            syncStatus = SyncStatus.SYNCED
        )
        orderRepository.createOrder(syncedOrder)

        val viewModel = InvoiceViewModel(
            orderId = "test-ord-002",
            orderRepository = orderRepository,
            outletRepository = outletRepository
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("State should be Success", state is InvoiceUiState.Success)
        val success = state as InvoiceUiState.Success

        assertFalse("Should NOT be provisional when synced", success.isProvisional)
        assertEquals("AFF-2610-0101", success.invoiceNumber)
        assertEquals(SyncStatus.SYNCED, success.syncStatus)
    }

    @Test
    fun loadInvoice_nonExistentOrder_emitsError() = runTest {
        val viewModel = InvoiceViewModel(
            orderId = "non-existent-order-id",
            orderRepository = orderRepository,
            outletRepository = outletRepository
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("State should be Error", state is InvoiceUiState.Error)
        val error = state as InvoiceUiState.Error
        assertTrue("Error message should mention order", error.message.contains("Order not found"))
    }

    @Test
    fun loadInvoice_shareText_containsEssentialGstAndReceiptDetails() = runTest {
        val testOrder = FieldOrder(
            id = "test-ord-003",
            outletId = "o4",
            outletName = "New Delhi Canteen",
            repId = "rep-01",
            items = listOf(
                OrderItem(
                    productId = "p2",
                    productName = "Fresh Idli Dosa Batter 1kg",
                    quantityPackets = 4,
                    weightKg = 4.0,
                    unitPrice = 76.50,
                    lineTotal = 306.00
                )
            ),
            subtotal = 306.00,
            gstAmount = 15.30,
            totalAmount = 321.30,
            paymentMode = PaymentMode.CREDIT_15D,
            provisionalInvoiceNumber = "PROV-2610-888",
            syncedInvoiceNumber = null
        )
        orderRepository.createOrder(testOrder)

        val viewModel = InvoiceViewModel(
            orderId = "test-ord-003",
            orderRepository = orderRepository,
            outletRepository = outletRepository
        )
        advanceUntilIdle()

        val success = viewModel.uiState.value as InvoiceUiState.Success
        val share = success.shareText

        assertTrue("Share text must mention Absolute Fresh", share.contains("ABSOLUTE FRESH"))
        assertTrue("Share text must mention FSSAI", share.contains("12723999000123"))
        assertTrue("Share text must mention GSTIN", share.contains("09AAHCA1234F1Z5"))
        assertTrue("Share text must state Provisional", share.contains("PROVISIONAL INVOICE"))
        assertTrue("Share text must mention Outlet", share.contains("New Delhi Canteen"))
        assertTrue("Share text must contain subtotal", share.contains("306.00"))
        assertTrue("Share text must contain grand total", share.contains("321.30"))
        assertTrue("Share text must mention payment mode", share.contains("15D Credit"))
    }
}
