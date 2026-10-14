package com.absolutefresh.sales

import com.absolutefresh.sales.data.model.Outlet
import com.absolutefresh.sales.data.model.OutletType
import com.absolutefresh.sales.data.model.SyncStatus
import com.absolutefresh.sales.data.model.VisitState
import com.absolutefresh.sales.data.repository.OutletRepository
import com.absolutefresh.sales.ui.visit.TodaysVisitViewModel
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
class TodaysVisitViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: TestOutletRepository
    private lateinit var viewModel: TodaysVisitViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = TestOutletRepository()
        viewModel = TodaysVisitViewModel("o1", fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load populates outlet details and planned state`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.outlet)
        assertEquals("Sharma Kirana", state.outlet?.name)
        assertEquals(VisitState.PLANNED, state.visitState)
    }

    @Test
    fun `check-in transitions state to ACTIVE and records timestamp`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.checkIn()
        val state = viewModel.uiState.value
        assertEquals(VisitState.ACTIVE, state.visitState)
        assertNotNull(state.checkInTimestamp)
    }

    @Test
    fun `no order flow records reason and completes visit`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.openNoOrderDialog()
        assertTrue(viewModel.uiState.value.isNoOrderDialogOpen)

        viewModel.updateNoOrderReason("Store has sufficient stock")
        viewModel.submitNoOrder()

        val state = viewModel.uiState.value
        assertFalse(state.isNoOrderDialogOpen)
        assertEquals(VisitState.COMPLETED, state.visitState)
        assertTrue(state.completedSummary?.contains("Store has sufficient stock") == true)
    }

    @Test
    fun `mark visit completed transitions to COMPLETED state`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.markVisitCompleted("Order Booked ₹1,200")
        val state = viewModel.uiState.value
        assertEquals(VisitState.COMPLETED, state.visitState)
        assertEquals("Order Booked ₹1,200", state.completedSummary)
    }

    private class TestOutletRepository : OutletRepository {
        val testOutlet = Outlet(
            id = "o1",
            name = "Sharma Kirana",
            ownerName = "Anil Sharma",
            phone = "9876543201",
            address = "12 Indira Nagar",
            area = "Indira Nagar",
            type = OutletType.GENERAL_TRADE,
            priceTier = 3,
            syncStatus = SyncStatus.SYNCED
        )

        override fun observeOutlets(): Flow<List<Outlet>> = MutableStateFlow(listOf(testOutlet)).asStateFlow()
        override suspend fun getOutlets(): List<Outlet> = listOf(testOutlet)
        override suspend fun getOutletById(id: String): Outlet? = if (id == "o1") testOutlet else null
        override suspend fun addOutlet(outlet: Outlet): Outlet = outlet
        override suspend fun findPotentialDuplicates(name: String, phone: String): List<Outlet> = emptyList()
    }
}
