package com.absolutefresh.sales.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.absolutefresh.sales.data.local.entity.OutletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OutletDao {

    @Query("SELECT * FROM outlets ORDER BY name ASC")
    fun getAllOutlets(): Flow<List<OutletEntity>>

    @Query("SELECT * FROM outlets WHERE id = :id")
    suspend fun getOutletById(id: String): OutletEntity?

    @Query("SELECT * FROM outlets WHERE area = :area ORDER BY name ASC")
    fun getOutletsByArea(area: String): Flow<List<OutletEntity>>

    @Query("SELECT * FROM outlets WHERE name LIKE '%' || :query || '%' OR ownerName LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchOutlets(query: String): Flow<List<OutletEntity>>

    @Query("SELECT * FROM outlets WHERE phone = :phone OR LOWER(name) = LOWER(:name) LIMIT 5")
    suspend fun findPossibleDuplicates(phone: String, name: String): List<OutletEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutlet(outlet: OutletEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutlets(outlets: List<OutletEntity>)

    @Update
    suspend fun updateOutlet(outlet: OutletEntity)

    @Delete
    suspend fun deleteOutlet(outlet: OutletEntity)

    @Query("SELECT COUNT(*) FROM outlets")
    suspend fun getOutletCount(): Int
}
