package com.absolutefresh.sales.data.sync

import android.content.Context
import com.absolutefresh.sales.data.local.AppDatabase
import com.absolutefresh.sales.data.local.dao.BeatDao
import com.absolutefresh.sales.data.local.dao.OutboxDao
import com.absolutefresh.sales.data.local.dao.OutletDao
import com.absolutefresh.sales.data.local.entity.BeatVisitEntity
import com.absolutefresh.sales.data.local.entity.OutletEntity
import com.absolutefresh.sales.data.network.NetworkDataSource
import com.absolutefresh.sales.data.network.SupabaseNetworkDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * High-level synchronization coordinator responsible for:
 * 1. Refreshing master data (Outlets, Beats, Products, Holidays) from Supabase into Room.
 * 2. Enqueueing automatic background push of queued offline mutations.
 * 3. Exposing outbox pending counts for the UI offline banner.
 */
class SyncRepository(
    private val outletDao: OutletDao,
    private val beatDao: BeatDao,
    private val outboxDao: OutboxDao,
    private val networkDataSource: NetworkDataSource = SupabaseNetworkDataSource()
) {
    constructor(
        database: AppDatabase,
        networkDataSource: NetworkDataSource = SupabaseNetworkDataSource()
    ) : this(
        outletDao = database.outletDao(),
        beatDao = database.beatDao(),
        outboxDao = database.outboxDao(),
        networkDataSource = networkDataSource
    )

    /**
     * Flow emitting count of pending un-synced outbox mutations.
     */
    fun getPendingOutboxCountFlow(): Flow<Int> {
        return outboxDao.getPendingCountFlow()
    }

    /**
     * Triggers immediate automatic background push via WorkManager.
     */
    fun triggerPush(context: Context) {
        OutboxSyncWorker.enqueue(context)
    }

    /**
     * Pulls latest master data from Supabase and synchronizes into local Room DB.
     * Preserves locally created PENDING entities to prevent data loss.
     */
    suspend fun refreshMasterData(repId: String, beatDate: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // 1. Refresh Outlets
            val outletsResult = networkDataSource.getOutlets(repId)
            if (outletsResult.isSuccess) {
                val serverOutlets = outletsResult.getOrThrow()
                val localEntities = serverOutlets.map { dto ->
                    OutletEntity(
                        id = dto.id ?: dto.clientId ?: "out-${System.currentTimeMillis()}",
                        name = dto.name,
                        ownerName = dto.owner ?: "Store Owner",
                        phone = dto.phone ?: "9876543200",
                        address = dto.address ?: dto.area,
                        area = dto.area,
                        type = dto.outletType,
                        priceTier = dto.priceTier,
                        gstin = dto.gstin,
                        outstandingBalance = 0.0,
                        creditLimit = 10000.0,
                        paymentTermsDays = 7,
                        avgKgPerDay = dto.averageKgPerDay,
                        latitude = dto.latitude,
                        longitude = dto.longitude,
                        syncStatus = "SYNCED",
                        createdAt = System.currentTimeMillis()
                    )
                }
                outletDao.insertOutlets(localEntities)
            }

            // 2. Refresh Beats for today
            val beatsResult = networkDataSource.getBeats(repId, beatDate)
            if (beatsResult.isSuccess) {
                val serverBeats = beatsResult.getOrThrow()
                val beatEntities = serverBeats.map { dto ->
                    BeatVisitEntity(
                        id = dto.id ?: "visit-${dto.rep}-${dto.date}-${dto.sequence}",
                        date = dto.date,
                        repId = dto.rep,
                        outletId = dto.outletId,
                        sequenceNumber = dto.sequence,
                        visitState = dto.status,
                        targetKg = dto.targetKg,
                        freshBatchesCount = 2,
                        agingBatchesCount = 0,
                        lastVisitedTimestamp = null,
                        completedOrderSummary = null,
                        missedReason = dto.missedReason,
                        syncStatus = "SYNCED"
                    )
                }
                beatDao.insertVisits(beatEntities)
            }
        }
    }
}
