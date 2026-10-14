package com.absolutefresh.sales.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.absolutefresh.sales.data.local.entity.OutboxEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OutboxDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(mutation: OutboxEntity): Long

    @Query("SELECT * FROM outbox_mutations WHERE status IN ('QUEUED', 'FAILED') AND retryCount < 5 ORDER BY createdAt ASC")
    suspend fun getPendingMutations(): List<OutboxEntity>

    @Query("UPDATE outbox_mutations SET status = 'PROCESSING', lastAttemptAt = :timestamp WHERE id = :id")
    suspend fun markProcessing(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE outbox_mutations SET status = 'SYNCED', lastAttemptAt = :timestamp WHERE id = :id")
    suspend fun markSynced(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE outbox_mutations SET status = 'FAILED', retryCount = :retryCount, errorMessage = :errorMessage, lastAttemptAt = :timestamp WHERE id = :id")
    suspend fun markFailed(
        id: Long,
        errorMessage: String,
        retryCount: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("SELECT COUNT(*) FROM outbox_mutations WHERE status IN ('QUEUED', 'PROCESSING')")
    fun getPendingCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM outbox_mutations WHERE status IN ('QUEUED', 'PROCESSING')")
    suspend fun getPendingCount(): Int

    @Query("DELETE FROM outbox_mutations WHERE status = 'SYNCED' AND createdAt < :beforeTimestamp")
    suspend fun pruneSynced(beforeTimestamp: Long)
}
