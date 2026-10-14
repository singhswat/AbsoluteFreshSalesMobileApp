package com.absolutefresh.sales.ui.outlets

import com.absolutefresh.sales.data.model.Outlet
import com.absolutefresh.sales.data.model.OutletType

data class AddOutletFormState(
    val isOpen: Boolean = false,
    val name: String = "",
    val ownerName: String = "",
    val phone: String = "",
    val area: String = "",
    val address: String = "",
    val type: OutletType = OutletType.GENERAL_TRADE,
    val priceTier: Int = 4,
    val gstin: String = "",
    val nameError: String? = null,
    val phoneError: String? = null,
    val areaError: String? = null,
    val potentialDuplicates: List<Outlet> = emptyList(),
    val duplicateWarningDismissed: Boolean = false,
    val isSaving: Boolean = false
) {
    val isValid: Boolean
        get() = name.isNotBlank() &&
                phone.trim().length >= 10 &&
                area.isNotBlank() &&
                nameError == null &&
                phoneError == null &&
                areaError == null
}

sealed class OutletsUiState {
    object Loading : OutletsUiState()
    object Empty : OutletsUiState()

    data class Populated(
        val outlets: List<Outlet>,
        val filteredOutlets: List<Outlet>,
        val searchQuery: String = "",
        val selectedArea: String = "All",
        val availableAreas: List<String> = emptyList(),
        val selectedOutlet: Outlet? = null,
        val formState: AddOutletFormState = AddOutletFormState(),
        val pendingSyncCount: Int = 0
    ) : OutletsUiState()

    data class Error(val message: String) : OutletsUiState()
}
