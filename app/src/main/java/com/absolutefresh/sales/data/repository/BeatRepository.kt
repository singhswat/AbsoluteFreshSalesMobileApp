package com.absolutefresh.sales.data.repository

import com.absolutefresh.sales.data.model.BeatOutletItem
import com.absolutefresh.sales.data.model.BeatProgress
import com.absolutefresh.sales.data.model.Outlet
import com.absolutefresh.sales.data.model.OutletType
import com.absolutefresh.sales.data.model.SyncStatus
import com.absolutefresh.sales.data.model.VisitState

/** Contract for beat data access. Implementations swap in Room + Supabase later. */
interface BeatRepository {
    /** Returns the outlet visit list for [dateEpochDay] (days since epoch). */
    suspend fun getBeatForDate(dateEpochDay: Long): List<BeatOutletItem>

    /** Returns aggregate progress for [dateEpochDay]. */
    suspend fun getProgressForDate(dateEpochDay: Long): BeatProgress

    /** Epoch-millis of the last successful sync, or null if never synced. */
    suspend fun lastSyncedAt(): Long?
}

/**
 * Fake implementation with realistic synthetic data for Lucknow East territory.
 * Replace with Room-backed implementation once the schema is in place.
 */
class FakeBeatRepository : BeatRepository {

    private val outlets = listOf(
        Outlet("o1", "Sharma Kirana",       "Anil Sharma",  "9876543201", "12 Indira Nagar", "Indira Nagar",   OutletType.GENERAL_TRADE, priceTier = 3, syncStatus = SyncStatus.SYNCED),
        Outlet("o2", "Gupta Dairy & More",  "Ramesh Gupta", "9876543202", "45 Vikas Nagar",  "Vikas Nagar",    OutletType.GENERAL_TRADE, priceTier = 4, syncStatus = SyncStatus.SYNCED),
        Outlet("o3", "Krishna Sweets",      "K. Prasad",    "9876543203", "7 Hazratganj",    "Hazratganj",     OutletType.STANDALONE_SHOP, priceTier = 2, syncStatus = SyncStatus.SYNCED),
        Outlet("o4", "New Delhi Canteen",   "S. Malhotra",  "9876543204", "22 Gomti Nagar",  "Gomti Nagar",    OutletType.CHAIN_STORE, priceTier = 2, syncStatus = SyncStatus.SYNCED),
        Outlet("o5", "Om Sai Provision",    "D. Yadav",     "9876543205", "3 Alambagh",      "Alambagh",       OutletType.GENERAL_TRADE, priceTier = 4, syncStatus = SyncStatus.PENDING),
        Outlet("o6", "Fresh Bite Café",     "M. Verma",     "9876543206", "88 Aliganj",      "Aliganj",        OutletType.STANDALONE_SHOP, priceTier = 3, syncStatus = SyncStatus.SYNCED),
        Outlet("o7", "Laxmi General Store", "P. Singh",     "9876543207", "55 Rajajipuram",  "Rajajipuram",    OutletType.GENERAL_TRADE, priceTier = 4, syncStatus = SyncStatus.SYNCED),
        Outlet("o8", "Taste of Lucknow",    "A. Khan",      "9876543208", "19 Chowk",        "Chowk",          OutletType.STANDALONE_SHOP, priceTier = 3, syncStatus = SyncStatus.SYNCED),
        Outlet("o9", "Sunrise Mart",        "V. Tiwari",    "9876543209", "71 Mahanagar",    "Mahanagar",      OutletType.GENERAL_TRADE, priceTier = 4, syncStatus = SyncStatus.FAILED),
        Outlet("o10","Quick Fresh Hub",     "R. Mishra",    "9876543210", "30 Jankipuram",   "Jankipuram",     OutletType.DARK_STORE, priceTier = 2, syncStatus = SyncStatus.SYNCED),
        Outlet("o11","Sai Blessings Kirana","H. Dubey",     "9876543211", "14 Chinhat",      "Chinhat",        OutletType.GENERAL_TRADE, priceTier = 4, syncStatus = SyncStatus.SYNCED),
        Outlet("o12","Metro Grocery",       "S. Bajpai",    "9876543212", "99 Telibagh",     "Telibagh",       OutletType.CHAIN_STORE, priceTier = 2, syncStatus = SyncStatus.SYNCED),
    )

    override suspend fun getBeatForDate(dateEpochDay: Long): List<BeatOutletItem> {
        // Today's beat — realistic mixed state to exercise all UI states.
        return outlets.mapIndexed { index, outlet ->
            BeatOutletItem(
                sequenceNumber  = index + 1,
                outlet          = outlet,
                visitState      = when (index) {
                    0, 1, 2 -> VisitState.COMPLETED
                    3       -> VisitState.ACTIVE
                    11      -> VisitState.MISSED
                    else    -> VisitState.PLANNED
                },
                targetKg        = if (outlet.priceTier <= 2) 25.0 else 15.0,
                freshBatchesCount   = (2..4).random(),
                agingBatchesCount   = if (index % 4 == 0) 1 else 0,
                completedOrderSummary = if (index < 3) "₹${(800..2400).random()} • 12 kg" else null
            )
        }
    }

    override suspend fun getProgressForDate(dateEpochDay: Long): BeatProgress =
        BeatProgress(planned = 12, done = 3, pending = 8, missed = 1,
            targetVolumeKg = 180.0, achievedVolumeKg = 42.0)

    override suspend fun lastSyncedAt(): Long =
        System.currentTimeMillis() - (23 * 60 * 60 * 1000L) // 23 h ago → triggers stale banner
}
