package com.absolutefresh.sales.ui.outlets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.absolutefresh.sales.data.model.Outlet
import com.absolutefresh.sales.data.model.OutletType
import com.absolutefresh.sales.data.model.SyncStatus
import com.absolutefresh.sales.data.repository.OutletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OutletsViewModel(
    private val repository: OutletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<OutletsUiState>(OutletsUiState.Loading)
    val uiState: StateFlow<OutletsUiState> = _uiState.asStateFlow()

    private var allOutlets: List<Outlet> = emptyList()
    private var searchQuery: String = ""
    private var selectedArea: String = "All"
    private var selectedOutlet: Outlet? = null
    private var formState: AddOutletFormState = AddOutletFormState()

    init {
        observeOutlets()
    }

    private fun observeOutlets() {
        viewModelScope.launch {
            _uiState.value = OutletsUiState.Loading
            try {
                repository.observeOutlets().collectLatest { outlets ->
                    allOutlets = outlets
                    applyFilters()
                }
            } catch (e: Exception) {
                _uiState.value = OutletsUiState.Error(
                    e.message ?: "Failed to load outlets"
                )
            }
        }
    }

    fun setSearchQuery(query: String) {
        searchQuery = query
        applyFilters()
    }

    fun setAreaFilter(area: String) {
        selectedArea = area
        applyFilters()
    }

    fun selectOutlet(outlet: Outlet?) {
        selectedOutlet = outlet
        applyFilters()
    }

    fun openAddOutlet() {
        formState = AddOutletFormState(isOpen = true)
        applyFilters()
    }

    fun closeAddOutlet() {
        formState = AddOutletFormState(isOpen = false)
        applyFilters()
    }

    fun updateFormName(name: String) {
        val error = if (name.isBlank()) "Outlet name is required" else null
        formState = formState.copy(name = name, nameError = error, duplicateWarningDismissed = false)
        applyFilters()
    }

    fun updateFormOwner(owner: String) {
        formState = formState.copy(ownerName = owner)
        applyFilters()
    }

    fun updateFormPhone(phone: String) {
        val clean = phone.filter { it.isDigit() }
        val error = when {
            clean.isEmpty() -> "Phone number is required"
            clean.length < 10 -> "Phone must be at least 10 digits"
            else -> null
        }
        formState = formState.copy(phone = clean, phoneError = error, duplicateWarningDismissed = false)
        applyFilters()
    }

    fun updateFormArea(area: String) {
        val error = if (area.isBlank()) "Area is required" else null
        formState = formState.copy(area = area, areaError = error)
        applyFilters()
    }

    fun updateFormAddress(address: String) {
        formState = formState.copy(address = address)
        applyFilters()
    }

    fun updateFormType(type: OutletType) {
        formState = formState.copy(type = type)
        applyFilters()
    }

    fun updateFormPriceTier(tier: Int) {
        formState = formState.copy(priceTier = tier)
        applyFilters()
    }

    fun updateFormGstin(gstin: String) {
        formState = formState.copy(gstin = gstin)
        applyFilters()
    }

    fun confirmAddAnyway() {
        formState = formState.copy(duplicateWarningDismissed = true)
        submitNewOutlet()
    }

    fun submitNewOutlet() {
        if (!formState.isValid) return

        viewModelScope.launch {
            if (!formState.duplicateWarningDismissed) {
                val duplicates = repository.findPotentialDuplicates(formState.name, formState.phone)
                if (duplicates.isNotEmpty()) {
                    formState = formState.copy(potentialDuplicates = duplicates)
                    applyFilters()
                    return@launch
                }
            }

            formState = formState.copy(isSaving = true)
            applyFilters()

            val newOutlet = Outlet(
                id = "",
                name = formState.name.trim(),
                ownerName = formState.ownerName.trim(),
                phone = formState.phone.trim(),
                address = formState.address.trim().ifBlank { formState.area.trim() },
                area = formState.area.trim(),
                type = formState.type,
                priceTier = formState.priceTier,
                gstin = formState.gstin.trim().ifBlank { null },
                syncStatus = SyncStatus.PENDING
            )

            repository.addOutlet(newOutlet)
            formState = AddOutletFormState(isOpen = false)
            applyFilters()
        }
    }

    private fun applyFilters() {
        if (allOutlets.isEmpty()) {
            _uiState.value = OutletsUiState.Empty
            return
        }

        val areas = listOf("All") + allOutlets.map { it.area }.distinct().sorted()

        val filtered = allOutlets.filter { outlet ->
            val matchesArea = (selectedArea == "All") || (outlet.area.equals(selectedArea, ignoreCase = true))
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                val q = searchQuery.trim().lowercase()
                outlet.name.lowercase().contains(q) ||
                        outlet.ownerName.lowercase().contains(q) ||
                        outlet.area.lowercase().contains(q) ||
                        outlet.phone.contains(q)
            }
            matchesArea && matchesSearch
        }

        val pendingCount = allOutlets.count { it.syncStatus != SyncStatus.SYNCED }

        _uiState.value = OutletsUiState.Populated(
            outlets = allOutlets,
            filteredOutlets = filtered,
            searchQuery = searchQuery,
            selectedArea = selectedArea,
            availableAreas = areas,
            selectedOutlet = selectedOutlet,
            formState = formState,
            pendingSyncCount = pendingCount
        )
    }
}
