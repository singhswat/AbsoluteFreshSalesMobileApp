package com.absolutefresh.sales

import com.absolutefresh.sales.data.model.PaymentMode
import com.absolutefresh.sales.ui.stock.StockViewModel
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
class StockViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: StockViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = StockViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has positive van stock and sku breakdown`() = runTest {
        val state = viewModel.uiState.value
        assertEquals(120, state.totalMorningLoaded)
        assertEquals(85, state.totalDelivered)
        assertEquals(35, state.totalBalance)
        assertEquals(3, state.skuBreakdown.size)
        assertFalse(state.settlementState.isSettled)
    }

    @Test
    fun `adjusting drop quantity updates net payable calculation`() = runTest {
        val initialDrop = viewModel.uiState.value.settlementState.packetsDropped
        val initialNet = viewModel.uiState.value.settlementState.netPayable

        viewModel.adjustDropQty(5)
        var state = viewModel.uiState.value
        assertEquals(initialDrop + 5, state.settlementState.packetsDropped)
        assertTrue(state.settlementState.netPayable > initialNet)

        viewModel.adjustDropQty(-10)
        state = viewModel.uiState.value
        assertEquals(initialDrop - 5, state.settlementState.packetsDropped)
    }

    @Test
    fun `adjusting expired return calculates credit and reduces net payable`() = runTest {
        val initialExpired = viewModel.uiState.value.settlementState.expiredReturned
        val initialNet = viewModel.uiState.value.settlementState.netPayable

        viewModel.adjustExpiredReturnQty(2)
        val state = viewModel.uiState.value
        assertEquals(initialExpired + 2, state.settlementState.expiredReturned)
        // 2 extra expired pkts * ₹50 = ₹100 less net payable
        assertEquals(initialNet - 100.0, state.settlementState.netPayable, 0.01)
    }

    @Test
    fun `payment mode selection updates state`() = runTest {
        viewModel.setPaymentMode(PaymentMode.CASH)
        assertEquals(PaymentMode.CASH, viewModel.uiState.value.settlementState.selectedPaymentMode)

        viewModel.setPaymentMode(PaymentMode.CREDIT_15D)
        assertEquals(PaymentMode.CREDIT_15D, viewModel.uiState.value.settlementState.selectedPaymentMode)
    }

    @Test
    fun `execute settlement generates invoice number and reduces van balance`() = runTest {
        val initialBalance = viewModel.uiState.value.totalBalance
        val dropped = viewModel.uiState.value.settlementState.packetsDropped

        viewModel.executeSettlement()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.settlementState.isSettled)
        assertNotNull(state.settlementState.generatedInvoiceNumber)
        assertEquals(initialBalance - dropped, state.totalBalance)
        assertTrue(state.showSuccessToast)

        viewModel.dismissToast()
        assertFalse(viewModel.uiState.value.showSuccessToast)
    }
}
