package com.absolutefresh.sales

import com.absolutefresh.sales.data.model.AuditEventType
import com.absolutefresh.sales.data.repository.FakeOrderHistoryRepository
import com.absolutefresh.sales.data.repository.FakeOrderRepository
import com.absolutefresh.sales.ui.orderhistory.OrderHistoryViewModel
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrderHistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var orderRepository: FakeOrderRepository
    private lateinit var historyRepository: FakeOrderHistoryRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        orderRepository = FakeOrderRepository()
        historyRepository = FakeOrderHistoryRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads order details and audit timeline entries for ord-001`() = runTest(testDispatcher) {
        val viewModel = OrderHistoryViewModel("ord-001", orderRepository, historyRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.order)
        assertEquals("ord-001", state.order?.id)
        assertEquals("Sharma Kirana", state.order?.outletName)
        assertEquals(3, state.timelineEntries.size)
        assertEquals(3, state.filteredEntries.size)
    }

    @Test
    fun `filter by event type filters entries correctly`() = runTest(testDispatcher) {
        val viewModel = OrderHistoryViewModel("ord-001", orderRepository, historyRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Filter by ORDER_CREATED
        viewModel.setFilter(AuditEventType.ORDER_CREATED)
        assertEquals(1, viewModel.uiState.value.filteredEntries.size)
        assertEquals(AuditEventType.ORDER_CREATED, viewModel.uiState.value.filteredEntries.first().eventType)

        // Filter by INVOICE_ISSUED
        viewModel.setFilter(AuditEventType.INVOICE_ISSUED)
        assertEquals(1, viewModel.uiState.value.filteredEntries.size)
        assertEquals(AuditEventType.INVOICE_ISSUED, viewModel.uiState.value.filteredEntries.first().eventType)

        // Clear filter
        viewModel.setFilter(null)
        assertEquals(3, viewModel.uiState.value.filteredEntries.size)
    }

    @Test
    fun `empty or short amendment reason fails validation`() = runTest(testDispatcher) {
        val viewModel = OrderHistoryViewModel("ord-001", orderRepository, historyRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.openAddNoteDialog()
        assertTrue(viewModel.uiState.value.isAddNoteDialogOpen)

        viewModel.onAmendmentReasonChanged("Hi")
        assertFalse(viewModel.uiState.value.isReasonValid)

        viewModel.submitAmendmentNote()
        assertNotNull(viewModel.uiState.value.errorMessage)
        assertEquals(3, viewModel.uiState.value.timelineEntries.size)
    }

    @Test
    fun `valid amendment reason submits successfully and appends entry with UUID`() = runTest(testDispatcher) {
        val viewModel = OrderHistoryViewModel("ord-001", orderRepository, historyRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.openAddNoteDialog()
        viewModel.onAmendmentTypeChanged(AuditEventType.ITEMS_AMENDED)
        viewModel.onAmendmentReasonChanged("Shop owner called requesting delayed dispatch until 4 PM")
        assertTrue(viewModel.uiState.value.isReasonValid)

        viewModel.submitAmendmentNote()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isAddNoteDialogOpen)
        assertNotNull(state.successMessage)
        assertEquals(4, state.timelineEntries.size)

        val latestEntry = state.timelineEntries.first()
        assertEquals(AuditEventType.ITEMS_AMENDED, latestEntry.eventType)
        assertEquals("Shop owner called requesting delayed dispatch until 4 PM", latestEntry.reason)
        assertEquals("Sales Rep (You)", latestEntry.actorName)
        assertTrue(latestEntry.operationUuid.startsWith("op-"))
    }

    @Test
    fun `conflict resolution entries retain local vs server comparison values for ord-003`() = runTest(testDispatcher) {
        val viewModel = OrderHistoryViewModel("ord-003", orderRepository, historyRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val entries = viewModel.uiState.value.timelineEntries
        assertEquals(2, entries.size)

        val conflictEntry = entries.first { it.isConflictResolution }
        assertEquals(AuditEventType.SYNC_RESOLVED, conflictEntry.eventType)
        assertNotNull(conflictEntry.localProposedValue)
        assertNotNull(conflictEntry.serverAuthoritativeValue)
        assertTrue(conflictEntry.localProposedValue!!.contains("Tier 4"))
        assertTrue(conflictEntry.serverAuthoritativeValue!!.contains("Tier 3"))
    }

    @Test
    fun `clearMessages resets error and success messages`() = runTest(testDispatcher) {
        val viewModel = OrderHistoryViewModel("ord-001", orderRepository, historyRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAmendmentReasonChanged("bad")
        viewModel.submitAmendmentNote()
        assertNotNull(viewModel.uiState.value.errorMessage)

        viewModel.clearMessages()
        assertNull(viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.successMessage)
    }
}
