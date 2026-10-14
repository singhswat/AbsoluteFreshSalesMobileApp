package com.absolutefresh.sales.data.repository

import com.absolutefresh.sales.data.model.BeatChangeProposal
import com.absolutefresh.sales.data.model.ProposalStatus
import com.absolutefresh.sales.data.model.SyncStatus
import com.absolutefresh.sales.data.model.VisitFrequency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

interface BeatChangeRepository {
    fun observeProposals(): Flow<List<BeatChangeProposal>>
    suspend fun getProposals(): List<BeatChangeProposal>
    suspend fun submitProposal(proposal: BeatChangeProposal): BeatChangeProposal
    suspend fun markSynced(proposalId: String)
}

class FakeBeatChangeRepository : BeatChangeRepository {

    private val initialProposals = mutableListOf(
        BeatChangeProposal(
            id = "bc-seed-01",
            outletId = "o3",
            outletName = "Krishna Sweets",
            outletArea = "Hazratganj",
            currentDay = "Monday",
            proposedDay = "Wednesday",
            currentFrequency = VisitFrequency.WEEKLY,
            proposedFrequency = VisitFrequency.TWICE_WEEKLY,
            reason = "Shop owner requested mid-week replenishment due to high batter demand on Wednesdays.",
            status = ProposalStatus.APPROVED,
            adminNotes = "Approved by Admin on 12-Oct. Beat plan updated.",
            createdAtEpochMs = System.currentTimeMillis() - (48 * 3600 * 1000L),
            syncStatus = SyncStatus.SYNCED
        ),
        BeatChangeProposal(
            id = "bc-seed-02",
            outletId = "o8",
            outletName = "Taste of Lucknow",
            outletArea = "Chowk",
            currentDay = "Tuesday",
            proposedDay = "Friday",
            currentFrequency = VisitFrequency.WEEKLY,
            proposedFrequency = VisitFrequency.WEEKLY,
            reason = "Chowk market remains heavily congested on Tuesdays due to local weekly bazaar.",
            status = ProposalStatus.PENDING_APPROVAL,
            adminNotes = null,
            createdAtEpochMs = System.currentTimeMillis() - (4 * 3600 * 1000L),
            syncStatus = SyncStatus.PENDING
        )
    )

    private val proposalsFlow = MutableStateFlow<List<BeatChangeProposal>>(initialProposals)

    override fun observeProposals(): Flow<List<BeatChangeProposal>> = proposalsFlow.asStateFlow()

    override suspend fun getProposals(): List<BeatChangeProposal> = proposalsFlow.value

    override suspend fun submitProposal(proposal: BeatChangeProposal): BeatChangeProposal {
        val assigned = if (proposal.id.isBlank()) {
            proposal.copy(id = "bc-${UUID.randomUUID()}", syncStatus = SyncStatus.PENDING)
        } else {
            proposal.copy(syncStatus = SyncStatus.PENDING)
        }
        val updated = listOf(assigned) + proposalsFlow.value
        proposalsFlow.value = updated
        return assigned
    }

    override suspend fun markSynced(proposalId: String) {
        val updated = proposalsFlow.value.map {
            if (it.id == proposalId) it.copy(syncStatus = SyncStatus.SYNCED) else it
        }
        proposalsFlow.value = updated
    }
}
