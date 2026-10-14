package com.absolutefresh.sales.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "van_stock")
data class VanStockEntity(
    @PrimaryKey
    val productId: String,
    val name: String,
    val packSize: String,
    val loadedPackets: Int,
    val unitPrice: Double,
    val batchNumber: String = "BATCH-LKO-2610-A",
    val expiryDate: String = "10 Days (Fresh)"
)
