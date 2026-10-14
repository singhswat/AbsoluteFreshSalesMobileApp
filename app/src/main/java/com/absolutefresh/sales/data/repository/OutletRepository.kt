package com.absolutefresh.sales.data.repository

import com.absolutefresh.sales.data.model.Outlet
import com.absolutefresh.sales.data.model.OutletType
import com.absolutefresh.sales.data.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

interface OutletRepository {
    fun observeOutlets(): Flow<List<Outlet>>
    suspend fun getOutlets(): List<Outlet>
    suspend fun getOutletById(id: String): Outlet?
    suspend fun addOutlet(outlet: Outlet): Outlet
    suspend fun findPotentialDuplicates(name: String, phone: String): List<Outlet>
}

class FakeOutletRepository : OutletRepository {

    private val initialOutlets = mutableListOf(
        Outlet("o1", "Sharma Kirana",       "Anil Sharma",  "9876543201", "12 Indira Nagar", "Indira Nagar",   OutletType.GENERAL_TRADE, priceTier = 3, outstandingBalance = 1200.0, creditLimit = 15000.0, syncStatus = SyncStatus.SYNCED),
        Outlet("o2", "Gupta Dairy & More",  "Ramesh Gupta", "9876543202", "45 Vikas Nagar",  "Vikas Nagar",    OutletType.GENERAL_TRADE, priceTier = 4, outstandingBalance = 450.0, creditLimit = 10000.0, syncStatus = SyncStatus.SYNCED),
        Outlet("o3", "Krishna Sweets",      "K. Prasad",    "9876543203", "7 Hazratganj",    "Hazratganj",     OutletType.STANDALONE_SHOP, priceTier = 2, outstandingBalance = 0.0, creditLimit = 25000.0, syncStatus = SyncStatus.SYNCED),
        Outlet("o4", "New Delhi Canteen",   "S. Malhotra",  "9876543204", "22 Gomti Nagar",  "Gomti Nagar",    OutletType.CHAIN_STORE, priceTier = 2, outstandingBalance = 3885.0, creditLimit = 30000.0, syncStatus = SyncStatus.SYNCED),
        Outlet("o5", "Om Sai Provision",    "D. Yadav",     "9876543205", "3 Alambagh",      "Alambagh",       OutletType.GENERAL_TRADE, priceTier = 4, outstandingBalance = 1034.25, creditLimit = 10000.0, syncStatus = SyncStatus.PENDING),
        Outlet("o6", "Fresh Bite Café",     "M. Verma",     "9876543206", "88 Aliganj",      "Aliganj",        OutletType.STANDALONE_SHOP, priceTier = 3, outstandingBalance = 0.0, creditLimit = 15000.0, syncStatus = SyncStatus.SYNCED),
        Outlet("o7", "Laxmi General Store", "P. Singh",     "9876543207", "55 Rajajipuram",  "Rajajipuram",    OutletType.GENERAL_TRADE, priceTier = 4, outstandingBalance = 2400.0, creditLimit = 10000.0, syncStatus = SyncStatus.SYNCED),
        Outlet("o8", "Taste of Lucknow",    "A. Khan",      "9876543208", "19 Chowk",        "Chowk",          OutletType.STANDALONE_SHOP, priceTier = 3, outstandingBalance = 0.0, creditLimit = 20000.0, syncStatus = SyncStatus.SYNCED),
        Outlet("o9", "Sunrise Mart",        "V. Tiwari",    "9876543209", "71 Mahanagar",    "Mahanagar",      OutletType.GENERAL_TRADE, priceTier = 4, outstandingBalance = 5600.0, creditLimit = 10000.0, syncStatus = SyncStatus.FAILED),
        Outlet("o10","Quick Fresh Hub",     "R. Mishra",    "9876543210", "30 Jankipuram",   "Jankipuram",     OutletType.DARK_STORE, priceTier = 2, outstandingBalance = 0.0, creditLimit = 50000.0, syncStatus = SyncStatus.SYNCED),
        Outlet("o11","Sai Blessings Kirana","H. Dubey",     "9876543211", "14 Chinhat",      "Chinhat",        OutletType.GENERAL_TRADE, priceTier = 4, outstandingBalance = 900.0, creditLimit = 10000.0, syncStatus = SyncStatus.SYNCED),
        Outlet("o12","Metro Grocery",       "S. Bajpai",    "9876543212", "99 Telibagh",     "Telibagh",       OutletType.CHAIN_STORE, priceTier = 2, outstandingBalance = 150.0, creditLimit = 25000.0, syncStatus = SyncStatus.SYNCED)
    )

    private val outletsFlow = MutableStateFlow<List<Outlet>>(initialOutlets)

    override fun observeOutlets(): Flow<List<Outlet>> = outletsFlow.asStateFlow()

    override suspend fun getOutlets(): List<Outlet> = outletsFlow.value

    override suspend fun getOutletById(id: String): Outlet? =
        outletsFlow.value.find { it.id == id }

    override suspend fun addOutlet(outlet: Outlet): Outlet {
        val assigned = if (outlet.id.isBlank()) {
            outlet.copy(
                id = "out-${UUID.randomUUID()}",
                syncStatus = SyncStatus.PENDING
            )
        } else {
            outlet.copy(syncStatus = SyncStatus.PENDING)
        }
        val updated = listOf(assigned) + outletsFlow.value
        outletsFlow.value = updated
        return assigned
    }

    override suspend fun findPotentialDuplicates(name: String, phone: String): List<Outlet> {
        val cleanName = name.trim().lowercase()
        val cleanPhone = phone.trim()
        return outletsFlow.value.filter { outlet ->
            (cleanPhone.isNotBlank() && outlet.phone == cleanPhone) ||
                    (cleanName.isNotBlank() && outlet.name.lowercase().contains(cleanName))
        }
    }
}
