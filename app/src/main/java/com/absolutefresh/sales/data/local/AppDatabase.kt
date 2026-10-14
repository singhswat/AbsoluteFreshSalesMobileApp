package com.absolutefresh.sales.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.absolutefresh.sales.data.local.dao.BeatDao
import com.absolutefresh.sales.data.local.dao.OrderDao
import com.absolutefresh.sales.data.local.dao.OutletDao
import com.absolutefresh.sales.data.local.dao.OutboxDao
import com.absolutefresh.sales.data.local.dao.VanStockDao
import com.absolutefresh.sales.data.local.entity.BeatVisitEntity
import com.absolutefresh.sales.data.local.entity.FieldOrderEntity
import com.absolutefresh.sales.data.local.entity.OrderItemEntity
import com.absolutefresh.sales.data.local.entity.OutletEntity
import com.absolutefresh.sales.data.local.entity.OutboxEntity
import com.absolutefresh.sales.data.local.entity.VanStockEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        OutletEntity::class,
        FieldOrderEntity::class,
        OrderItemEntity::class,
        BeatVisitEntity::class,
        VanStockEntity::class,
        OutboxEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun outletDao(): OutletDao
    abstract fun orderDao(): OrderDao
    abstract fun beatDao(): BeatDao
    abstract fun vanStockDao(): VanStockDao
    abstract fun outboxDao(): OutboxDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "absolute_fresh_sales.db"
                )
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        fun createInMemory(context: Context): AppDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                AppDatabase::class.java
            )
            .allowMainThreadQueries()
            .build()
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    seedDatabase(database)
                }
            }
        }

        private suspend fun seedDatabase(database: AppDatabase) {
            // Seed 12 Outlets across Lucknow East
            val seedOutlets = listOf(
                OutletEntity("o1", "Sharma Kirana",       "Anil Sharma",  "9876543201", "12 Indira Nagar", "Indira Nagar",   "GENERAL_TRADE", priceTier = 3, gstin = null, outstandingBalance = 1200.0, creditLimit = 15000.0, paymentTermsDays = 15, avgKgPerDay = 12.0, latitude = 26.8833, longitude = 80.9933, syncStatus = "SYNCED"),
                OutletEntity("o2", "Gupta Dairy & More",  "Ramesh Gupta", "9876543202", "45 Vikas Nagar",  "Vikas Nagar",    "GENERAL_TRADE", priceTier = 4, gstin = null, outstandingBalance = 450.0, creditLimit = 10000.0, paymentTermsDays = 15, avgKgPerDay = 8.0, latitude = 26.8912, longitude = 80.9543, syncStatus = "SYNCED"),
                OutletEntity("o3", "Krishna Sweets",      "K. Prasad",    "9876543203", "7 Hazratganj",    "Hazratganj",     "STANDALONE_SHOP", priceTier = 2, gstin = "09AABCK3321D1Z1", outstandingBalance = 0.0, creditLimit = 25000.0, paymentTermsDays = 15, avgKgPerDay = 15.0, latitude = 26.8522, longitude = 80.9467, syncStatus = "SYNCED"),
                OutletEntity("o4", "New Delhi Canteen",   "S. Malhotra",  "9876543204", "22 Gomti Nagar",  "Gomti Nagar",    "CHAIN_STORE", priceTier = 2, gstin = "09AABCN5543D1ZT", outstandingBalance = 3885.0, creditLimit = 30000.0, paymentTermsDays = 15, avgKgPerDay = 25.0, latitude = 26.8489, longitude = 81.0125, syncStatus = "SYNCED"),
                OutletEntity("o5", "Om Sai Provision",    "D. Yadav",     "9876543205", "3 Alambagh",      "Alambagh",       "GENERAL_TRADE", priceTier = 4, gstin = null, outstandingBalance = 1034.25, creditLimit = 10000.0, paymentTermsDays = 15, avgKgPerDay = 10.0, latitude = 26.8123, longitude = 80.9123, syncStatus = "PENDING"),
                OutletEntity("o6", "Fresh Bite Café",     "M. Verma",     "9876543206", "88 Aliganj",      "Aliganj",        "STANDALONE_SHOP", priceTier = 3, gstin = null, outstandingBalance = 0.0, creditLimit = 15000.0, paymentTermsDays = 15, avgKgPerDay = 12.0, latitude = 26.8845, longitude = 80.9389, syncStatus = "SYNCED"),
                OutletEntity("o7", "Laxmi General Store", "P. Singh",     "9876543207", "55 Rajajipuram",  "Rajajipuram",    "GENERAL_TRADE", priceTier = 4, gstin = null, outstandingBalance = 2400.0, creditLimit = 10000.0, paymentTermsDays = 15, avgKgPerDay = 9.0, latitude = 26.8345, longitude = 80.8912, syncStatus = "SYNCED"),
                OutletEntity("o8", "Taste of Lucknow",    "A. Khan",      "9876543208", "19 Chowk",        "Chowk",          "STANDALONE_SHOP", priceTier = 3, gstin = null, outstandingBalance = 0.0, creditLimit = 20000.0, paymentTermsDays = 15, avgKgPerDay = 14.0, latitude = 26.8678, longitude = 80.9123, syncStatus = "SYNCED"),
                OutletEntity("o9", "Sunrise Mart",        "V. Tiwari",    "9876543209", "71 Mahanagar",    "Mahanagar",      "GENERAL_TRADE", priceTier = 4, gstin = null, outstandingBalance = 5600.0, creditLimit = 10000.0, paymentTermsDays = 15, avgKgPerDay = 11.0, latitude = 26.8789, longitude = 80.9567, syncStatus = "FAILED"),
                OutletEntity("o10","Quick Fresh Hub",     "R. Mishra",    "9876543210", "30 Jankipuram",   "Jankipuram",     "DARK_STORE", priceTier = 2, gstin = "09AABCQ8876D1Z9", outstandingBalance = 0.0, creditLimit = 50000.0, paymentTermsDays = 15, avgKgPerDay = 35.0, latitude = 26.9123, longitude = 80.9456, syncStatus = "SYNCED"),
                OutletEntity("o11","Sai Blessings Kirana","H. Dubey",     "9876543211", "14 Chinhat",      "Chinhat",        "GENERAL_TRADE", priceTier = 4, gstin = null, outstandingBalance = 900.0, creditLimit = 10000.0, paymentTermsDays = 15, avgKgPerDay = 7.0, latitude = 26.8712, longitude = 81.0456, syncStatus = "SYNCED"),
                OutletEntity("o12","Metro Grocery",       "S. Bajpai",    "9876543212", "99 Telibagh",     "Telibagh",       "CHAIN_STORE", priceTier = 2, gstin = "09AABCM1122D1Z3", outstandingBalance = 150.0, creditLimit = 25000.0, paymentTermsDays = 15, avgKgPerDay = 20.0, latitude = 26.7912, longitude = 80.9412, syncStatus = "SYNCED")
            )
            database.outletDao().insertOutlets(seedOutlets)

            // Seed Van Stock SKUs
            val seedStock = listOf(
                VanStockEntity("p1", "Idli & Dosa Batter 500g", "500g", 25, 45.0, "BATCH-LKO-2610-01", "10 Days (Fresh)"),
                VanStockEntity("p2", "Idli & Dosa Batter 1kg", "1000g", 15, 85.0, "BATCH-LKO-2610-01", "10 Days (Fresh)"),
                VanStockEntity("p3", "Podi Chutney 30g", "30g", 10, 28.0, "BATCH-LKO-2610-P1", "15 Days (Fresh)")
            )
            database.vanStockDao().insertStock(seedStock)
        }
    }
}
