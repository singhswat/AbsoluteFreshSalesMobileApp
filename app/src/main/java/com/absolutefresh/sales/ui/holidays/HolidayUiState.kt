package com.absolutefresh.sales.ui.holidays

import com.absolutefresh.sales.data.model.Holiday
import com.absolutefresh.sales.data.model.HolidayScope

data class HolidayUiState(
    val isLoading: Boolean = false,
    val allHolidays: List<Holiday> = emptyList(),
    val filteredHolidays: List<Holiday> = emptyList(),
    val availableMonths: List<String> = listOf("All", "Oct 2026", "Nov 2026", "Dec 2026", "Jan 2027"),
    val selectedMonth: String = "All",
    val selectedScope: HolidayScope? = null,
    val searchQuery: String = "",
    val isAdvanceSheetOpen: Boolean = false,
    val selectedHoliday: Holiday? = null,
    val outletName: String = "",
    val advancePacketsText: String = "",
    val justification: String = "",
    val isSubmitting: Boolean = false,
    val lastCacheTime: Long = 0L,
    val toastMessage: String? = null
) {
    val isAdvanceValid: Boolean
        get() = outletName.isNotBlank() &&
                (advancePacketsText.toIntOrNull() ?: 0) > 0 &&
                justification.trim().length >= 5

    val upcomingCount: Int
        get() = allHolidays.count { !it.isPast }

    val impactedBeatsCount: Int
        get() = allHolidays.count { it.beatImpact != null }
}
