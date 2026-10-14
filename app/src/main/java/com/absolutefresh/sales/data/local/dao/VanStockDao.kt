package com.absolutefresh.sales.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.absolutefresh.sales.data.local.entity.VanStockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VanStockDao {

    @Query("SELECT * FROM van_stock ORDER BY productId ASC")
    fun getAllStock(): Flow<List<VanStockEntity>>

    @Query("SELECT * FROM van_stock WHERE productId = :productId")
    suspend fun getStockByProductId(productId: String): VanStockEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(items: List<VanStockEntity>)

    @Update
    suspend fun updateStock(item: VanStockEntity)

    @Query("UPDATE van_stock SET loadedPackets = loadedPackets - :droppedPackets WHERE productId = :productId")
    suspend fun deductStock(productId: String, droppedPackets: Int)
}
