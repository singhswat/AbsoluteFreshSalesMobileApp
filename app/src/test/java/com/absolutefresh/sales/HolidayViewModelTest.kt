package com.absolutefresh.sales

import com.absolutefresh.sales.data.model.HolidayScope
import com.absolutefresh.sales.data.repository.FakeHolidayRepository
import com.absolutefresh.sales.ui.holidays.HolidayViewModel
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
class HolidayViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var holidayRepository: FakeHolidayRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        holidayRepository = FakeHolidayRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads all holidays from repository`() = runTest(testDispatcher) {
        val viewModel = HolidayViewModel(holidayRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.allHolidays.isNotEmpty())
        assertEquals(state.allHolidays.size, state.filteredHolidays.size)
        assertEquals("All", state.selectedMonth)
        assertNull(state.selectedScope)
        assertTrue(state.upcomingCount > 0)
        assertTrue(state.impactedBeatsCount > 0)
    }

    @Test
    fun `filter by month updates filtered list accurately`() = runTest(testDispatcher) {
        val viewModel = HolidayViewModel(holidayRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Filter for "Oct 2026"
        viewModel.setMonth("Oct 2026")
        testDispatcher.scheduler.advanceUntilIdle()

        val octState = viewModel.uiState.value
        assertEquals("Oct 2026", octState.selectedMonth)
        assertTrue(octState.filteredHolidays.isNotEmpty())
        assertTrue(octState.filteredHolidays.all { it.monthYear == "Oct 2026" })

        // Filter for "Nov 2026"
        viewModel.setMonth("Nov 2026")
        testDispatcher.scheduler.advanceUntilIdle()

        val novState = viewModel.uiState.value
        assertEquals("Nov 2026", novState.selectedMonth)
        assertTrue(novState.filteredHolidays.all { it.monthYear == "Nov 2026" })
    }

    @Test
    fun `filter by scope filters pan-india and regional correctly`() = runTest(testDispatcher) {
        val viewModel = HolidayViewModel(holidayRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setScope(HolidayScope.REGIONAL)
        testDispatcher.scheduler.advanceUntilIdle()

        val regionalState = viewModel.uiState.value
        assertEquals(HolidayScope.REGIONAL, regionalState.selectedScope)
        assertTrue(regionalState.filteredHolidays.all { it.scope == HolidayScope.REGIONAL })

        // Toggling the same scope clears the filter
        viewModel.setScope(HolidayScope.REGIONAL)
        testDispatcher.scheduler.advanceUntilIdle()

        val resetScopeState = viewModel.uiState.value
        assertNull(resetScopeState.selectedScope)
        assertEquals(resetScopeState.allHolidays.size, resetScopeState.filteredHolidays.size)
    }

    @Test
    fun `search query filters by festival name, description, and affected routes`() = runTest(testDispatcher) {
        val viewModel = HolidayViewModel(holidayRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Search by festival name
        viewModel.setSearchQuery("Diwali")
        testDispatcher.scheduler.advanceUntilIdle()

        val diwaliState = viewModel.uiState.value
        assertEquals(2, diwaliState.filteredHolidays.size)
        assertTrue(diwaliState.filteredHolidays.any { it.id == "hol-2026-11-08" })

        // Search by unique Hindi title
        viewModel.setSearchQuery("दीपावली")
        testDispatcher.scheduler.advanceUntilIdle()

        val deepawaliState = viewModel.uiState.value
        assertEquals(1, deepawaliState.filteredHolidays.size)
        assertEquals("hol-2026-11-08", deepawaliState.filteredHolidays.first().id)

        // Search by route name
        viewModel.setSearchQuery("Patrakarpuram")
        testDispatcher.scheduler.advanceUntilIdle()

        val routeState = viewModel.uiState.value
        assertEquals(1, routeState.filteredHolidays.size)
        assertEquals("Dussehra / Vijayadashami", routeState.filteredHolidays.first().nameEn)

        // Non-matching search query yields empty list
        viewModel.setSearchQuery("NonExistentFestival123")
        testDispatcher.scheduler.advanceUntilIdle()

        val emptyState = viewModel.uiState.value
        assertTrue(emptyState.filteredHolidays.isEmpty())
    }

    @Test
    fun `pre-holiday advance validation checks outlet, positive packets, and justification length`() = runTest(testDispatcher) {
        val viewModel = HolidayViewModel(holidayRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val holiday = viewModel.uiState.value.allHolidays.first()
        viewModel.openAdvanceSheet(holiday)

        var state = viewModel.uiState.value
        assertTrue(state.isAdvanceSheetOpen)
        assertNotNull(state.selectedHoliday)
        assertFalse("Blank outlet should be invalid", state.isAdvanceValid)

        viewModel.updateOutletName("Kapoor General Store")
        viewModel.updateAdvancePackets("0")
        viewModel.updateJustification("abc")
        state = viewModel.uiState.value
        assertFalse("Zero packets should be invalid", state.isAdvanceValid)

        viewModel.updateAdvancePackets("30")
        state = viewModel.uiState.value
        assertFalse("Less than 5 chars reason required", state.isAdvanceValid)

        viewModel.updateJustification("Extra morning requirement for festival shoppers")
        state = viewModel.uiState.value
        assertTrue("Valid inputs should enable submission", state.isAdvanceValid)
    }

    @Test
    fun `submit pre-holiday advance records request and updates state with toast`() = runTest(testDispatcher) {
        val viewModel = HolidayViewModel(holidayRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val holiday = viewModel.uiState.value.allHolidays.first()
        viewModel.openAdvanceSheet(holiday)
        viewModel.updateOutletName("Vipin Traders")
        viewModel.updateAdvancePackets("40")
        viewModel.updateJustification("High demand expected on Dussehra morning")

        viewModel.submitAdvanceRequest()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isAdvanceSheetOpen)
        assertNull(state.selectedHoliday)
        assertNotNull(state.toastMessage)
        assertTrue(state.toastMessage!!.contains("Vipin Traders"))

        val recorded = holidayRepository.getSubmittedRequests()
        assertEquals(1, recorded.size)
        assertEquals("Vipin Traders", recorded.first().outletName)
        assertEquals(40, recorded.first().advancePackets)
        assertEquals(holiday.id, recorded.first().holidayId)

        viewModel.clearToast()
        assertNull(viewModel.uiState.value.toastMessage)
    }
}
