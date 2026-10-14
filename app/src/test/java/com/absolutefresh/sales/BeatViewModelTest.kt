package com.absolutefresh.sales

import com.absolutefresh.sales.data.model.BeatOutletItem
import com.absolutefresh.sales.data.model.BeatProgress
import com.absolutefresh.sales.data.model.Outlet
import com.absolutefresh.sales.data.model.OutletType
import com.absolutefresh.sales.data.model.SyncStatus
import com.absolutefresh.sales.data.model.VisitState
import com.absolutefresh.sales.data.repository.BeatRepository
import com.absolutefresh.sales.ui.beat.BeatUiState
import com.absolutefresh.sales.ui.beat.BeatViewModel
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
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class BeatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: TestBeatRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = TestBeatRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load produces Populated state when items exist`() = runTest {
        val viewModel = BeatViewModel(fakeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected BeatUiState.Populated but got $state", state is BeatUiState.Populated)
        val populated = state as BeatUiState.Populated
        assertEquals(2, populated.items.size)
        assertEquals(2, populated.progress.planned)
    }

    @Test
    fun `empty repository returns BeatUiState Empty`() = runTest {
        fakeRepository.items = emptyList()
        val viewModel = BeatViewModel(fakeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected BeatUiState.Empty but got $state", state is BeatUiState.Empty)
    }

    @Test
    fun `repository error produces BeatUiState Error`() = runTest {
        fakeRepository.shouldThrow = true
        fakeRepository.errorMessage = "Database connection lost"
        val viewModel = BeatViewModel(fakeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected BeatUiState.Error but got $state", state is BeatUiState.Error)
        assertEquals("Database connection lost", (state as BeatUiState.Error).message)
    }

    @Test
    fun `stale threshold marks state as stale when last sync is too old`() = runTest {
        val fourHoursMs = 4 * 60 * 60 * 1000L
        // 5 hours ago
        fakeRepository.lastSynced = System.currentTimeMillis() - (5 * 60 * 60 * 1000L)
        val viewModel = BeatViewModel(fakeRepository, staleThresholdMs = fourHoursMs)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as BeatUiState.Populated
        assertTrue("Expected isStale=true when last sync exceeds threshold", state.isStale)
    }

    @Test
    fun `fresh sync within threshold marks state as not stale`() = runTest {
        val fourHoursMs = 4 * 60 * 60 * 1000L
        // 10 minutes ago
        fakeRepository.lastSynced = System.currentTimeMillis() - (10 * 60 * 1000L)
        val viewModel = BeatViewModel(fakeRepository, staleThresholdMs = fourHoursMs)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as BeatUiState.Populated
        assertFalse("Expected isStale=false when last sync is recent", state.isStale)
    }

    @Test
    fun `date navigation decrements and increments selectedDate correctly`() = runTest {
        val viewModel = BeatViewModel(fakeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val today = LocalDate.now()
        assertEquals(today, viewModel.selectedDate.value)

        viewModel.goToPreviousDay()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(today.minusDays(1), viewModel.selectedDate.value)

        viewModel.goToNextDay()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(today, viewModel.selectedDate.value)

        viewModel.goToNextDay()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(today.plusDays(1), viewModel.selectedDate.value)

        viewModel.goToToday()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(today, viewModel.selectedDate.value)
    }

    private class TestBeatRepository : BeatRepository {
        var items: List<BeatOutletItem> = listOf(
            BeatOutletItem(
                sequenceNumber = 1,
                outlet = Outlet("o1", "Store 1", "Owner 1", "9000000001", "Address 1", "Area 1", OutletType.GENERAL_TRADE, priceTier = 1, syncStatus = SyncStatus.SYNCED),
                visitState = VisitState.PLANNED,
                targetKg = 20.0
            ),
            BeatOutletItem(
                sequenceNumber = 2,
                outlet = Outlet("o2", "Store 2", "Owner 2", "9000000002", "Address 2", "Area 2", OutletType.STANDALONE_SHOP, priceTier = 2, syncStatus = SyncStatus.SYNCED),
                visitState = VisitState.COMPLETED,
                targetKg = 15.0
            )
        )
        var progress: BeatProgress = BeatProgress(planned = 2, done = 1, pending = 1, missed = 0, targetVolumeKg = 35.0, achievedVolumeKg = 15.0)
        var lastSynced: Long? = System.currentTimeMillis()
        var shouldThrow: Boolean = false
        var errorMessage: String = "Test failure"

        override suspend fun getBeatForDate(dateEpochDay: Long): List<BeatOutletItem> {
            if (shouldThrow) throw RuntimeException(errorMessage)
            return items
        }

        override suspend fun getProgressForDate(dateEpochDay: Long): BeatProgress {
            if (shouldThrow) throw RuntimeException(errorMessage)
            return progress
        }

        override suspend fun lastSyncedAt(): Long? = lastSynced
    }
}
