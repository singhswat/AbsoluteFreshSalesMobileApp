package com.absolutefresh.sales

import com.absolutefresh.sales.data.model.Outlet
import com.absolutefresh.sales.data.model.OutletType
import com.absolutefresh.sales.data.model.ProposalStatus
import com.absolutefresh.sales.data.model.SyncStatus
import com.absolutefresh.sales.data.model.VisitFrequency
import com.absolutefresh.sales.data.repository.FakeBeatChangeRepository
import com.absolutefresh.sales.data.repository.FakeOutletRepository
import com.absolutefresh.sales.ui.beatchange.BeatChangeTab
import com.absolutefresh.sales.ui.beatchange.BeatChangeViewModel
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BeatChangeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var outletRepository: FakeOutletRepository
    private lateinit var beatChangeRepository: FakeBeatChangeRepository
    private lateinit var viewModel: BeatChangeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        outletRepository = FakeOutletRepository()
        beatChangeRepository = FakeBeatChangeRepository()
        viewModel = BeatChangeViewModel(outletRepository, beatChangeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialStateLoadsOutletsAndDefaultProposals() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(BeatChangeTab.NEW_PROPOSAL, state.selectedTab)
        assertTrue(state.outlets.isNotEmpty())
        assertNotNull(state.selectedOutlet)
        assertEquals("Sharma Kirana", state.selectedOutlet?.name)
        // Check seeded proposals
        assertEquals(2, state.proposals.size)
        assertEquals(1, state.pendingCount)
    }

    @Test
    fun testSelectOutletUpdatesSelectedOutletAndSchedule() = runTest {
        advanceUntilIdle()

        val testOutlet = Outlet(
            id = "test-o99",
            name = "Test Supermarket",
            ownerName = "Rajesh",
            phone = "9876543299",
            address = "Hazratganj",
            area = "Hazratganj",
            type = OutletType.CHAIN_STORE,
            priceTier = 2
        )

        viewModel.selectOutlet(testOutlet)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(testOutlet, state.selectedOutlet)
        assertNotNull(state.currentDay)
        assertEquals(VisitFrequency.TWICE_WEEKLY, state.currentFrequency)
    }

    @Test
    fun testValidationFailsWhenReasonIsBlankOrTooShort() = runTest {
        advanceUntilIdle()

        // Empty reason
        viewModel.updateReason("")
        viewModel.submitProposal()
        advanceUntilIdle()

        var state = viewModel.uiState.value
        assertNotNull(state.reasonError)
        assertFalse(state.submissionSuccess)
        assertEquals(2, state.proposals.size)

        // Reason shorter than 5 chars
        viewModel.updateReason("bad")
        viewModel.submitProposal()
        advanceUntilIdle()

        state = viewModel.uiState.value
        assertNotNull(state.reasonError)
        assertFalse(state.submissionSuccess)
        assertEquals(2, state.proposals.size)
    }

    @Test
    fun testValidSubmissionCreatesPendingProposalAndSwitchesTab() = runTest {
        advanceUntilIdle()

        viewModel.updateProposedDay("Thursday")
        viewModel.updateProposedFrequency(VisitFrequency.TWICE_WEEKLY)
        viewModel.updateReason("Shopkeeper requested Thursday morning delivery due to weekend rush.")

        viewModel.submitProposal()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.submissionSuccess)
        assertNull(state.reasonError)
        assertEquals("", state.reason)
        assertEquals(BeatChangeTab.PAST_REQUESTS, state.selectedTab)
        assertNotNull(state.snackBarMessage)

        // Proposal count should have increased from 2 to 3
        assertEquals(3, state.proposals.size)
        val submitted = state.proposals.first() // Newly submitted is prepended
        assertEquals("Sharma Kirana", submitted.outletName)
        assertEquals("Thursday", submitted.proposedDay)
        assertEquals(VisitFrequency.TWICE_WEEKLY, submitted.proposedFrequency)
        assertEquals(ProposalStatus.PENDING_APPROVAL, submitted.status)
        assertEquals(SyncStatus.PENDING, submitted.syncStatus)
        assertEquals("Shopkeeper requested Thursday morning delivery due to weekend rush.", submitted.reason)
    }

    @Test
    fun testFilterProposalsByStatus() = runTest {
        advanceUntilIdle()

        // All proposals
        viewModel.setFilterStatus(null)
        assertEquals(2, viewModel.uiState.value.filteredProposals.size)

        // Approved proposals
        viewModel.setFilterStatus(ProposalStatus.APPROVED)
        val approved = viewModel.uiState.value.filteredProposals
        assertEquals(1, approved.size)
        assertEquals(ProposalStatus.APPROVED, approved.first().status)

        // Pending approval proposals
        viewModel.setFilterStatus(ProposalStatus.PENDING_APPROVAL)
        val pending = viewModel.uiState.value.filteredProposals
        assertEquals(1, pending.size)
        assertEquals(ProposalStatus.PENDING_APPROVAL, pending.first().status)

        // Rejected proposals
        viewModel.setFilterStatus(ProposalStatus.REJECTED)
        val rejected = viewModel.uiState.value.filteredProposals
        assertEquals(0, rejected.size)
    }

    @Test
    fun testSelectTabUpdatesTab() = runTest {
        advanceUntilIdle()

        viewModel.selectTab(BeatChangeTab.PAST_REQUESTS)
        assertEquals(BeatChangeTab.PAST_REQUESTS, viewModel.uiState.value.selectedTab)

        viewModel.selectTab(BeatChangeTab.NEW_PROPOSAL)
        assertEquals(BeatChangeTab.NEW_PROPOSAL, viewModel.uiState.value.selectedTab)
    }
}
