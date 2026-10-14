package com.absolutefresh.sales.data.model

import java.util.UUID

enum class ProposalStatus(val labelEn: String, val labelHi: String) {
    PENDING_APPROVAL("Pending Approval", "स्वीकृति लंबित"),
    APPROVED("Approved", "स्वीकृत"),
    REJECTED("Rejected", "अस्वीकृत")
}

enum class VisitFrequency(val titleEn: String, val titleHi: String) {
    DAILY("Daily", "दैनिक"),
    ALTERNATE_DAYS("Alternate Days", "एक दिन छोड़कर"),
    TWICE_WEEKLY("Twice Weekly", "सप्ताह में दो बार"),
    WEEKLY("Weekly", "साप्ताहिक")
}

data class BeatChangeProposal(
    val id: String = "bc-${UUID.randomUUID()}",
    val outletId: String,
    val outletName: String,
    val outletArea: String,
    val currentDay: String,
    val proposedDay: String,
    val currentFrequency: VisitFrequency,
    val proposedFrequency: VisitFrequency,
    val reason: String,
    val status: ProposalStatus = ProposalStatus.PENDING_APPROVAL,
    val adminNotes: String? = null,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING
)
