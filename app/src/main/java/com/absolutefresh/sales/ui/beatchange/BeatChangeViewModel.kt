package com.absolutefresh.sales.ui.beatchange

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.absolutefresh.sales.data.model.BeatChangeProposal
import com.absolutefresh.sales.data.model.Outlet
import com.absolutefresh.sales.data.model.ProposalStatus
import com.absolutefresh.sales.data.model.SyncStatus
import com.absolutefresh.sales.data.model.VisitFrequency
import com.absolutefresh.sales.data.repository.BeatChangeRepository
import com.absolutefresh.sales.data.repository.OutletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class BeatChangeViewModel(
    private val outletRepository: OutletRepository,
    private val beatChangeRepository: BeatChangeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BeatChangeUiState())
    val uiState: StateFlow<BeatChangeUiState> = _uiState.asStateFlow()

    init {
        loadOutlets()
        loadProposals()
    }

    private fun loadOutlets() {
        viewModelScope.launch {
            outletRepository.observeOutlets().collect { list ->
                val currentSelected = _uiState.value.selectedOutlet
                val selected = if (currentSelected == null && list.isNotEmpty()) list.first() else currentSelected
                _uiState.value = _uiState.value.copy(
                    outlets = list,
                    selectedOutlet = selected
                )
            }
        }
    }

    private fun loadProposals() {
        viewModelScope.launch {
            beatChangeRepository.observeProposals().collect { list ->
                _uiState.value = _uiState.value.copy(proposals = list)
            }
        }
    }

    fun selectTab(tab: BeatChangeTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun selectOutlet(outlet: Outlet) {
        // Derive realistic current schedule based on outlet index or ID
        val days = DAYS_OF_WEEK
        val dayIndex = (outlet.id.hashCode() and 0x7FFFFFFF) % days.size
        val currentDay = days[dayIndex]
        val currentFreq = if (outlet.priceTier <= 2) VisitFrequency.TWICE_WEEKLY else VisitFrequency.WEEKLY

        // Pick a default proposed day different from current
        val proposedDayIndex = (dayIndex + 2) % days.size
        val proposedDay = days[proposedDayIndex]

        _uiState.value = _uiState.value.copy(
            selectedOutlet = outlet,
            currentDay = currentDay,
            currentFrequency = currentFreq,
            proposedDay = proposedDay
        )
    }

    fun updateProposedDay(day: String) {
        _uiState.value = _uiState.value.copy(proposedDay = day)
    }

    fun updateProposedFrequency(frequency: VisitFrequency) {
        _uiState.value = _uiState.value.copy(proposedFrequency = frequency)
    }

    fun updateReason(text: String) {
        _uiState.value = _uiState.value.copy(
            reason = text,
            reasonError = if (text.trim().length >= 5) null else _uiState.value.reasonError
        )
    }

    fun setFilterStatus(status: ProposalStatus?) {
        _uiState.value = _uiState.value.copy(filterStatus = status)
    }

    fun clearSnackBarMessage() {
        _uiState.value = _uiState.value.copy(snackBarMessage = null)
    }

    fun submitProposal() {
        val state = _uiState.value
        val outlet = state.selectedOutlet
        if (outlet == null) {
            _uiState.value = state.copy(reasonError = "Please select an outlet / कृपया आउटलेट चुनें")
            return
        }

        val trimmedReason = state.reason.trim()
        if (trimmedReason.length < 5) {
            _uiState.value = state.copy(
                reasonError = "Please provide a reason (at least 5 characters) / कम से कम 5 अक्षरों का कारण लिखें"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true)

            val proposal = BeatChangeProposal(
                id = "bc-${UUID.randomUUID()}",
                outletId = outlet.id,
                outletName = outlet.name,
                outletArea = outlet.area,
                currentDay = state.currentDay,
                proposedDay = state.proposedDay,
                currentFrequency = state.currentFrequency,
                proposedFrequency = state.proposedFrequency,
                reason = trimmedReason,
                status = ProposalStatus.PENDING_APPROVAL,
                adminNotes = null,
                createdAtEpochMs = System.currentTimeMillis(),
                syncStatus = SyncStatus.PENDING
            )

            beatChangeRepository.submitProposal(proposal)

            _uiState.value = _uiState.value.copy(
                isSubmitting = false,
                submissionSuccess = true,
                reason = "",
                reasonError = null,
                selectedTab = BeatChangeTab.PAST_REQUESTS,
                snackBarMessage = "Proposal submitted for ${outlet.name} (प्रस्ताव दर्ज हुआ)"
            )
        }
    }
}
