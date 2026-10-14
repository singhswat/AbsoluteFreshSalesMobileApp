package com.absolutefresh.sales.ui.visit

import com.absolutefresh.sales.data.model.Outlet
import com.absolutefresh.sales.data.model.VisitState

data class TodaysVisitUiState(
    val outlet: Outlet? = null,
    val sequenceNumber: Int = 1,
    val visitState: VisitState = VisitState.PLANNED,
    val targetKg: Double = 25.0,
    val achievedKg: Double = 0.0,
    val checkInTimestamp: Long? = null,
    val isNoOrderDialogOpen: Boolean = false,
    val noOrderReason: String = "",
    val completedSummary: String? = null,
    val isSaving: Boolean = false
)
