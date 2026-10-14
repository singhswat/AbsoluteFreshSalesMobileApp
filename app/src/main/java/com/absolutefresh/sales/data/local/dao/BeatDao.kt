package com.absolutefresh.sales.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.absolutefresh.sales.data.local.entity.BeatVisitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BeatDao {

    @Query("SELECT * FROM beat_visits WHERE date = :date AND repId = :repId ORDER BY sequenceNumber ASC")
    fun getVisitsForDate(date: String, repId: String): Flow<List<BeatVisitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisits(visits: List<BeatVisitEntity>)

    @Update
    suspend fun updateVisit(visit: BeatVisitEntity)

    @Query("UPDATE beat_visits SET visitState = :visitState, lastVisitedTimestamp = :timestamp, completedOrderSummary = :orderSummary WHERE id = :visitId")
    suspend fun updateVisitState(
        visitId: String,
        visitState: String,
        timestamp: Long?,
        orderSummary: String?
    )

    @Query("UPDATE beat_visits SET visitState = 'MISSED', missedReason = :reason WHERE id = :visitId")
    suspend fun markVisitMissed(visitId: String, reason: String)

    @Query("SELECT COUNT(*) FROM beat_visits WHERE date = :date AND repId = :repId")
    suspend fun getVisitsCountForDate(date: String, repId: String): Int
}
