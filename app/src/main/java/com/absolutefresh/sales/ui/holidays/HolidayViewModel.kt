package com.absolutefresh.sales.ui.holidays

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.absolutefresh.sales.data.model.Holiday
import com.absolutefresh.sales.data.model.HolidayScope
import com.absolutefresh.sales.data.model.PreHolidayStockRequest
import com.absolutefresh.sales.data.repository.HolidayRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HolidayViewModel(
    private val repository: HolidayRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HolidayUiState(
            isLoading = true,
            lastCacheTime = repository.getLastCacheTime()
        )
    )
    val uiState: StateFlow<HolidayUiState> = _uiState.asStateFlow()

    init {
        loadHolidays()
    }

    private fun loadHolidays() {
        viewModelScope.launch {
            repository.getHolidays().collect { list ->
                _uiState.update { current ->
                    val filtered = applyFilters(
                        all = list,
                        month = current.selectedMonth,
                        scope = current.selectedScope,
                        query = current.searchQuery
                    )
                    current.copy(
                        isLoading = false,
                        allHolidays = list,
                        filteredHolidays = filtered,
                        lastCacheTime = repository.getLastCacheTime()
                    )
                }
            }
        }
    }

    fun setMonth(month: String) {
        _uiState.update { current ->
            val filtered = applyFilters(
                all = current.allHolidays,
                month = month,
                scope = current.selectedScope,
                query = current.searchQuery
            )
            current.copy(selectedMonth = month, filteredHolidays = filtered)
        }
    }

    fun setScope(scope: HolidayScope?) {
        _uiState.update { current ->
            val newScope = if (current.selectedScope == scope) null else scope
            val filtered = applyFilters(
                all = current.allHolidays,
                month = current.selectedMonth,
                scope = newScope,
                query = current.searchQuery
            )
            current.copy(selectedScope = newScope, filteredHolidays = filtered)
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { current ->
            val filtered = applyFilters(
                all = current.allHolidays,
                month = current.selectedMonth,
                scope = current.selectedScope,
                query = query
            )
            current.copy(searchQuery = query, filteredHolidays = filtered)
        }
    }

    fun openAdvanceSheet(holiday: Holiday) {
        _uiState.update {
            it.copy(
                isAdvanceSheetOpen = true,
                selectedHoliday = holiday,
                outletName = "",
                advancePacketsText = "20",
                justification = ""
            )
        }
    }

    fun closeAdvanceSheet() {
        _uiState.update {
            it.copy(
                isAdvanceSheetOpen = false,
                selectedHoliday = null
            )
        }
    }

    fun updateOutletName(name: String) {
        _uiState.update { it.copy(outletName = name) }
    }

    fun updateAdvancePackets(packets: String) {
        val filtered = packets.filter { ch -> ch.isDigit() }
        _uiState.update { it.copy(advancePacketsText = filtered) }
    }

    fun updateJustification(reason: String) {
        _uiState.update { it.copy(justification = reason) }
    }

    fun submitAdvanceRequest() {
        val state = _uiState.value
        val holiday = state.selectedHoliday ?: return
        if (!state.isAdvanceValid) return

        val qty = state.advancePacketsText.toIntOrNull() ?: 0

        val request = PreHolidayStockRequest(
            holidayId = holiday.id,
            holidayName = holiday.nameEn,
            outletName = state.outletName.trim(),
            advancePackets = qty,
            justification = state.justification.trim()
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            repository.submitPreHolidayAdvance(request)
            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    isAdvanceSheetOpen = false,
                    selectedHoliday = null,
                    toastMessage = "Pre-holiday advance of $qty pkts recorded for ${request.outletName} (अग्रिम स्टॉक दर्ज किया गया)"
                )
            }
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    private fun applyFilters(
        all: List<Holiday>,
        month: String,
        scope: HolidayScope?,
        query: String
    ): List<Holiday> {
        return all.filter { holiday ->
            val matchesMonth = month == "All" || holiday.monthYear.equals(month, ignoreCase = true)
            val matchesScope = scope == null || holiday.scope == scope
            val matchesQuery = query.isBlank() ||
                    holiday.nameEn.contains(query, ignoreCase = true) ||
                    holiday.nameHi.contains(query, ignoreCase = true) ||
                    holiday.description.contains(query, ignoreCase = true) ||
                    (holiday.beatImpact?.beatName?.contains(query, ignoreCase = true) == true) ||
                    (holiday.beatImpact?.affectedRoutes?.any { it.contains(query, ignoreCase = true) } == true)

            matchesMonth && matchesScope && matchesQuery
        }
    }
}
