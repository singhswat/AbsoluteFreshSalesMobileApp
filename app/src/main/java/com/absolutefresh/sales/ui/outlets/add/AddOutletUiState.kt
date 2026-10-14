package com.absolutefresh.sales.ui.outlets.add

import com.absolutefresh.sales.data.model.Outlet
import com.absolutefresh.sales.data.model.OutletType

data class AddOutletUiState(
    val name: String = "",
    val ownerName: String = "",
    val phone: String = "",
    val area: String = "",
    val address: String = "",
    val type: OutletType = OutletType.GENERAL_TRADE,
    val priceTier: Int = 4,
    val gstin: String = "",
    val avgKgPerDay: String = "10.0",
    val creditLimit: String = "10000",
    val paymentTermsDays: Int = 15,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isGpsCaptured: Boolean = false,

    // Validation errors
    val nameError: String? = null,
    val phoneError: String? = null,
    val areaError: String? = null,
    val gstinError: String? = null,

    // Duplicate detection
    val potentialDuplicates: List<Outlet> = emptyList(),
    val duplicateWarningDismissed: Boolean = false,

    // Submission status
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    val isValid: Boolean
        get() = name.isNotBlank() &&
                nameError == null &&
                phone.trim().length >= 10 &&
                phoneError == null &&
                area.isNotBlank() &&
                areaError == null &&
                gstinError == null
}
