package com.absolutefresh.sales

import com.absolutefresh.sales.data.model.Outlet
import com.absolutefresh.sales.data.model.OutletType
import com.absolutefresh.sales.data.model.SyncStatus
import com.absolutefresh.sales.data.repository.OutletRepository
import com.absolutefresh.sales.ui.outlets.OutletsUiState
import com.absolutefresh.sales.ui.outlets.OutletsViewModel
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
class OutletsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: TestOutletRepository
    private lateinit var viewModel: OutletsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = TestOutletRepository()
        viewModel = OutletsViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load populates outlets and available areas`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected Populated state but was $state", state is OutletsUiState.Populated)
        val populated = state as OutletsUiState.Populated
        assertEquals(2, populated.outlets.size)
        assertTrue(populated.availableAreas.contains("All"))
        assertTrue(populated.availableAreas.contains("Indira Nagar"))
        assertTrue(populated.availableAreas.contains("Gomti Nagar"))
    }

    @Test
    fun `area filter returns only outlets in that area`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setAreaFilter("Indira Nagar")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as OutletsUiState.Populated
        assertEquals(1, state.filteredOutlets.size)
        assertEquals("Indira Nagar", state.filteredOutlets.first().area)
    }

    @Test
    fun `search filters by store name or phone`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setSearchQuery("Sharma")
        testDispatcher.scheduler.advanceUntilIdle()
        var state = viewModel.uiState.value as OutletsUiState.Populated
        assertEquals(1, state.filteredOutlets.size)
        assertEquals("Sharma Kirana", state.filteredOutlets.first().name)

        viewModel.setSearchQuery("9876543202")
        testDispatcher.scheduler.advanceUntilIdle()
        state = viewModel.uiState.value as OutletsUiState.Populated
        assertEquals(1, state.filteredOutlets.size)
        assertEquals("Gupta Dairy", state.filteredOutlets.first().name)
    }

    @Test
    fun `form validation enforces required fields and phone length`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.openAddOutlet()
        var state = viewModel.uiState.value as OutletsUiState.Populated
        assertFalse(state.formState.isValid)

        viewModel.updateFormName("New Store")
        viewModel.updateFormPhone("123") // too short
        viewModel.updateFormArea("Aliganj")
        state = viewModel.uiState.value as OutletsUiState.Populated
        assertFalse("Phone is too short", state.formState.isValid)
        assertNotNull(state.formState.phoneError)

        viewModel.updateFormPhone("9876543210")
        state = viewModel.uiState.value as OutletsUiState.Populated
        assertTrue("Form should be valid now", state.formState.isValid)
    }

    @Test
    fun `duplicate detection warns user before saving`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.openAddOutlet()
        viewModel.updateFormName("Sharma Kirana")
        viewModel.updateFormPhone("9876543201")
        viewModel.updateFormArea("Indira Nagar")
        viewModel.submitNewOutlet()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as OutletsUiState.Populated
        // Modal remains open with potential duplicates shown
        assertTrue(state.formState.isOpen)
        assertEquals(1, state.formState.potentialDuplicates.size)
        assertFalse(state.formState.duplicateWarningDismissed)

        // Confirm add anyway
        viewModel.confirmAddAnyway()
        testDispatcher.scheduler.advanceUntilIdle()

        val stateAfter = viewModel.uiState.value as OutletsUiState.Populated
        assertFalse("Sheet should close after confirmation", stateAfter.formState.isOpen)
        assertEquals(3, stateAfter.outlets.size)
    }

    private class TestOutletRepository : OutletRepository {
        val list = mutableListOf(
            Outlet("o1", "Sharma Kirana", "Anil", "9876543201", "Addr 1", "Indira Nagar", OutletType.GENERAL_TRADE, priceTier = 3, syncStatus = SyncStatus.SYNCED),
            Outlet("o2", "Gupta Dairy", "Ramesh", "9876543202", "Addr 2", "Gomti Nagar", OutletType.GENERAL_TRADE, priceTier = 4, syncStatus = SyncStatus.SYNCED)
        )
        val flow = MutableStateFlow<List<Outlet>>(list)

        override fun observeOutlets(): Flow<List<Outlet>> = flow.asStateFlow()
        override suspend fun getOutlets(): List<Outlet> = flow.value
        override suspend fun getOutletById(id: String): Outlet? = flow.value.find { it.id == id }

        override suspend fun addOutlet(outlet: Outlet): Outlet {
            val assigned = outlet.copy(id = "o-new", syncStatus = SyncStatus.PENDING)
            val updated = listOf(assigned) + flow.value
            flow.value = updated
            return assigned
        }

        override suspend fun findPotentialDuplicates(name: String, phone: String): List<Outlet> {
            val cleanName = name.trim().lowercase()
            val cleanPhone = phone.trim()
            return flow.value.filter { it.phone == cleanPhone || it.name.lowercase().contains(cleanName) }
        }
    }
}
