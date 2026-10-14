package com.absolutefresh.sales

import com.absolutefresh.sales.ui.more.MoreViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MoreViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: MoreViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = MoreViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has positive cash in hand and sample holidays`() = runTest {
        val state = viewModel.uiState.value
        assertTrue("Cash in hand should be positive", state.cashInHand > 0)
        assertTrue("Should have upcoming holidays", state.upcomingHolidays.isNotEmpty())
        assertEquals("EN", state.selectedLanguage)
    }

    @Test
    fun `language toggle switches between EN and HI`() = runTest {
        assertEquals("EN", viewModel.uiState.value.selectedLanguage)
        viewModel.toggleLanguage()
        assertEquals("HI", viewModel.uiState.value.selectedLanguage)
        viewModel.toggleLanguage()
        assertEquals("EN", viewModel.uiState.value.selectedLanguage)
    }

    @Test
    fun `recording cash receipt increases cash in hand and pending sync count`() = runTest {
        val initialCash = viewModel.uiState.value.cashInHand
        val initialPending = viewModel.uiState.value.syncHealth.pendingCount

        viewModel.recordCashReceipt("o-1", "Test Store", 1000.0, "AFF-2001")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(initialCash + 1000.0, state.cashInHand, 0.01)
        assertEquals(initialPending + 1, state.syncHealth.pendingCount)
        assertEquals("Test Store", state.recentCashTransactions.first().outletName)
    }

    @Test
    fun `manual sync flushes pending outbox to synced`() = runTest {
        viewModel.recordCashReceipt("o-1", "Test Store", 500.0, "INV-1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.triggerManualSync()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0, state.syncHealth.pendingCount)
        assertFalse(state.syncHealth.isSyncing)
    }

    @Test
    fun `modal state helpers open and close sheets`() = runTest {
        assertFalse(viewModel.uiState.value.isRecordCashOpen)
        viewModel.openRecordCash()
        assertTrue(viewModel.uiState.value.isRecordCashOpen)
        viewModel.closeRecordCash()
        assertFalse(viewModel.uiState.value.isRecordCashOpen)

        assertFalse(viewModel.uiState.value.isHolidaysOpen)
        viewModel.openHolidays()
        assertTrue(viewModel.uiState.value.isHolidaysOpen)
        viewModel.closeHolidays()
        assertFalse(viewModel.uiState.value.isHolidaysOpen)
    }
}
