package com.absolutefresh.sales.ui.beat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.absolutefresh.sales.data.repository.BeatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * ViewModel for the Beat (Rep Home) screen — FS-01.
 *
 * Stale threshold: open item #5 from screen-map. Currently set to a conservative 4-hour
 * placeholder so the stale banner is testable; the real value must come from
 * Architect/BizAnalyst before the acceptance criterion is complete.
 */
class BeatViewModel(
    private val repository: BeatRepository,
    private val staleThresholdMs: Long = STALE_THRESHOLD_MS_PLACEHOLDER
) : ViewModel() {

    private val _uiState = MutableStateFlow<BeatUiState>(BeatUiState.Loading)
    val uiState: StateFlow<BeatUiState> = _uiState.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    init {
        loadBeat()
    }

    fun loadBeat() {
        viewModelScope.launch {
            _uiState.value = BeatUiState.Loading
            try {
                val epochDay = _selectedDate.value.toEpochDay()
                val items    = repository.getBeatForDate(epochDay)
                val progress = repository.getProgressForDate(epochDay)
                val lastSync = repository.lastSyncedAt() ?: 0L
                val isStale  = (System.currentTimeMillis() - lastSync) > staleThresholdMs

                _uiState.value = if (items.isEmpty()) {
                    BeatUiState.Empty
                } else {
                    BeatUiState.Populated(
                        items        = items,
                        progress     = progress,
                        lastRefreshed = lastSync,
                        isStale      = isStale
                    )
                }
            } catch (e: Exception) {
                _uiState.value = BeatUiState.Error(
                    e.message ?: "Could not load beat. Pull to refresh when online."
                )
            }
        }
    }

    fun goToPreviousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
        loadBeat()
    }

    fun goToNextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
        loadBeat()
    }

    fun goToToday() {
        _selectedDate.value = LocalDate.now()
        loadBeat()
    }

    companion object {
        /** Placeholder — real value to come from Architect/BizAnalyst (open item #5). */
        const val STALE_THRESHOLD_MS_PLACEHOLDER = 4 * 60 * 60 * 1000L // 4 hours
    }
}
