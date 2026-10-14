package com.absolutefresh.sales.ui.visit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.absolutefresh.sales.data.model.Outlet
import com.absolutefresh.sales.data.model.VisitState
import com.absolutefresh.sales.data.repository.OutletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TodaysVisitViewModel(
    private val outletId: String,
    private val outletRepository: OutletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodaysVisitUiState())
    val uiState: StateFlow<TodaysVisitUiState> = _uiState.asStateFlow()

    init {
        loadOutlet()
    }

    private fun loadOutlet() {
        viewModelScope.launch {
            val outlet = outletRepository.getOutletById(outletId)
            _uiState.value = _uiState.value.copy(
                outlet = outlet,
                targetKg = if ((outlet?.priceTier ?: 4) <= 2) 25.0 else 15.0
            )
        }
    }

    fun checkIn() {
        _uiState.value = _uiState.value.copy(
            visitState = VisitState.ACTIVE,
            checkInTimestamp = System.currentTimeMillis()
        )
    }

    fun openNoOrderDialog() {
        _uiState.value = _uiState.value.copy(isNoOrderDialogOpen = true)
    }

    fun closeNoOrderDialog() {
        _uiState.value = _uiState.value.copy(isNoOrderDialogOpen = false)
    }

    fun updateNoOrderReason(reason: String) {
        _uiState.value = _uiState.value.copy(noOrderReason = reason)
    }

    fun submitNoOrder() {
        val reason = _uiState.value.noOrderReason.ifBlank { "No order required today" }
        _uiState.value = _uiState.value.copy(
            visitState = VisitState.COMPLETED,
            completedSummary = "No Order: $reason",
            isNoOrderDialogOpen = false
        )
    }

    fun markVisitCompleted(summary: String = "Visit Completed") {
        _uiState.value = _uiState.value.copy(
            visitState = VisitState.COMPLETED,
            completedSummary = summary
        )
    }
}
