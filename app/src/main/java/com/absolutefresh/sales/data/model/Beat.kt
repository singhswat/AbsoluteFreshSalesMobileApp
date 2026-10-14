package com.absolutefresh.sales.data.model

enum class VisitState {
    PLANNED,
    ACTIVE,
    COMPLETED,
    MISSED
}

enum class OutletType {
    STANDALONE_SHOP,
    GENERAL_TRADE,
    CHAIN_STORE,
    DARK_STORE
}

enum class SyncStatus {
    PENDING,
    SYNCING,
    SYNCED,
    FAILED
}

data class Outlet(
    val id: String,
    val name: String,
    val ownerName: String,
    val phone: String,
    val address: String,
    val area: String,
    val type: OutletType = OutletType.GENERAL_TRADE,
    val priceTier: Int = 4,
    val gstin: String? = null,
    val outstandingBalance: Double = 0.0,
    val creditLimit: Double = 10000.0,
    val paymentTermsDays: Int = 15,
    val avgKgPerDay: Double = 10.0,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)

data class BeatOutletItem(
    val sequenceNumber: Int,
    val outlet: Outlet,
    val visitState: VisitState = VisitState.PLANNED,
    val targetKg: Double = 15.0,
    val freshBatchesCount: Int = 2,
    val agingBatchesCount: Int = 0,
    val lastVisitedTimestamp: Long? = null,
    val completedOrderSummary: String? = null
)

data class BeatProgress(
    val planned: Int = 12,
    val done: Int = 7,
    val pending: Int = 4,
    val missed: Int = 1,
    val targetVolumeKg: Double = 180.0,
    val achievedVolumeKg: Double = 115.0
)
