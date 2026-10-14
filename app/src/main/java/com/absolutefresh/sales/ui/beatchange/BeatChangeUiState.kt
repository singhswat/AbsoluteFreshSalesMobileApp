package com.absolutefresh.sales.ui.beatchange

import com.absolutefresh.sales.data.model.BeatChangeProposal
import com.absolutefresh.sales.data.model.Outlet
import com.absolutefresh.sales.data.model.ProposalStatus
import com.absolutefresh.sales.data.model.VisitFrequency

enum class BeatChangeTab(val labelEn: String, val labelHi: String) {
    NEW_PROPOSAL("New Proposal", "नया प्रस्ताव"),
    PAST_REQUESTS("Past Requests", "पिछले अनुरोध")
}

val DAYS_OF_WEEK = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

data class BeatChangeUiState(
    val selectedTab: BeatChangeTab = BeatChangeTab.NEW_PROPOSAL,
    val outlets: List<Outlet> = emptyList(),
    val selectedOutlet: Outlet? = null,
    val currentDay: String = "Monday",
    val currentFrequency: VisitFrequency = VisitFrequency.WEEKLY,
    val proposedDay: String = "Wednesday",
    val proposedFrequency: VisitFrequency = VisitFrequency.TWICE_WEEKLY,
    val reason: String = "",
    val reasonError: String? = null,
    val isSubmitting: Boolean = false,
    val submissionSuccess: Boolean = false,
    val proposals: List<BeatChangeProposal> = emptyList(),
    val filterStatus: ProposalStatus? = null,
    val snackBarMessage: String? = null
) {
    val filteredProposals: List<BeatChangeProposal>
        get() = if (filterStatus == null) proposals else proposals.filter { it.status == filterStatus }

    val pendingCount: Int
        get() = proposals.count { it.status == ProposalStatus.PENDING_APPROVAL }
}
