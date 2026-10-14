package com.absolutefresh.sales.ui.outlets.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.absolutefresh.sales.data.model.Outlet
import com.absolutefresh.sales.data.model.OutletType
import com.absolutefresh.sales.data.model.SyncStatus
import com.absolutefresh.sales.data.repository.OutletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class AddOutletViewModel(
    private val repository: OutletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddOutletUiState())
    val uiState: StateFlow<AddOutletUiState> = _uiState.asStateFlow()

    private val gstinRegex = Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$")

    fun updateName(name: String) {
        val error = when {
            name.isBlank() -> "Outlet name is required (दुकान का नाम आवश्यक है)"
            name.trim().length < 2 -> "Name must be at least 2 characters"
            else -> null
        }
        _uiState.update {
            it.copy(
                name = name,
                nameError = error,
                duplicateWarningDismissed = false
            )
        }
    }

    fun updateOwner(owner: String) {
        _uiState.update { it.copy(ownerName = owner) }
    }

    fun updatePhone(phone: String) {
        val digits = phone.filter { it.isDigit() }
        val error = when {
            digits.isEmpty() -> "Phone number is required (मोबाइल नंबर आवश्यक है)"
            digits.length < 10 -> "Phone must be at least 10 digits"
            else -> null
        }
        _uiState.update {
            it.copy(
                phone = digits,
                phoneError = error,
                duplicateWarningDismissed = false
            )
        }
    }

    fun updateArea(area: String) {
        val error = if (area.isBlank()) "Area is required (इलाका आवश्यक है)" else null
        _uiState.update { it.copy(area = area, areaError = error) }
    }

    fun updateAddress(address: String) {
        _uiState.update { it.copy(address = address) }
    }

    fun updateType(type: OutletType) {
        _uiState.update { it.copy(type = type) }
    }

    fun updatePriceTier(tier: Int) {
        _uiState.update { it.copy(priceTier = tier.coerceIn(1, 4)) }
    }

    fun updateGstin(gstinInput: String) {
        val upper = gstinInput.trim().uppercase()
        val error = if (upper.isNotEmpty() && !gstinRegex.matches(upper)) {
            "Invalid GSTIN format (e.g. 09ABCDE1234F1Z5)"
        } else {
            null
        }
        _uiState.update { it.copy(gstin = upper, gstinError = error) }
    }

    fun updateAvgKgPerDay(kg: String) {
        _uiState.update { it.copy(avgKgPerDay = kg) }
    }

    fun updateCreditLimit(limit: String) {
        _uiState.update { it.copy(creditLimit = limit) }
    }

    fun updatePaymentTermsDays(days: Int) {
        _uiState.update { it.copy(paymentTermsDays = days) }
    }

    fun captureGpsLocation(lat: Double = 26.8467, lng: Double = 80.9462) {
        _uiState.update {
            it.copy(
                latitude = lat,
                longitude = lng,
                isGpsCaptured = true
            )
        }
    }

    fun clearGpsLocation() {
        _uiState.update {
            it.copy(
                latitude = null,
                longitude = null,
                isGpsCaptured = false
            )
        }
    }

    fun dismissDuplicateWarning() {
        _uiState.update {
            it.copy(
                potentialDuplicates = emptyList(),
                duplicateWarningDismissed = true
            )
        }
    }

    fun confirmAddAnyway() {
        _uiState.update { it.copy(duplicateWarningDismissed = true) }
        submitOutlet()
    }

    fun submitOutlet() {
        val state = _uiState.value

        // Validate mandatory fields
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Outlet name is required (दुकान का नाम आवश्यक है)") }
            return
        }
        if (state.phone.trim().length < 10) {
            _uiState.update { it.copy(phoneError = "Phone must be at least 10 digits") }
            return
        }
        if (state.area.isBlank()) {
            _uiState.update { it.copy(areaError = "Area is required (इलाका आवश्यक है)") }
            return
        }
        if (state.gstinError != null) return

        viewModelScope.launch {
            // Check duplicates if not already dismissed
            if (!state.duplicateWarningDismissed) {
                val duplicates = repository.findPotentialDuplicates(state.name, state.phone)
                if (duplicates.isNotEmpty()) {
                    _uiState.update { it.copy(potentialDuplicates = duplicates) }
                    return@launch
                }
            }

            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

            try {
                val parsedKg = state.avgKgPerDay.toDoubleOrNull() ?: 10.0
                val parsedLimit = state.creditLimit.toDoubleOrNull() ?: 10000.0

                val newOutlet = Outlet(
                    id = "out-${UUID.randomUUID()}",
                    name = state.name.trim(),
                    ownerName = state.ownerName.trim(),
                    phone = state.phone.trim(),
                    address = state.address.trim().ifBlank { state.area.trim() },
                    area = state.area.trim(),
                    type = state.type,
                    priceTier = state.priceTier,
                    gstin = state.gstin.ifBlank { null },
                    outstandingBalance = 0.0,
                    creditLimit = parsedLimit,
                    paymentTermsDays = state.paymentTermsDays,
                    avgKgPerDay = parsedKg,
                    latitude = state.latitude,
                    longitude = state.longitude,
                    syncStatus = SyncStatus.PENDING
                )

                repository.addOutlet(newOutlet)
                _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = e.message ?: "Failed to save outlet"
                    )
                }
            }
        }
    }
}
