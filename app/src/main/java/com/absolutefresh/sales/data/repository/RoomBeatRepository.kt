package com.absolutefresh.sales.data.repository

import com.absolutefresh.sales.data.local.dao.BeatDao
import com.absolutefresh.sales.data.local.dao.OutletDao
import com.absolutefresh.sales.data.model.BeatOutletItem
import com.absolutefresh.sales.data.model.BeatProgress
import com.absolutefresh.sales.data.model.Outlet
import com.absolutefresh.sales.data.model.OutletType
import com.absolutefresh.sales.data.model.SyncStatus
import com.absolutefresh.sales.data.model.VisitState
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Room-backed BeatRepository reading visits from local SQLite database
 * with fallback to associated Outlet entities.
 */
class RoomBeatRepository(
    private val beatDao: BeatDao,
    private val outletDao: OutletDao,
    private val repId: String = "rep-1"
) : BeatRepository {

    private var lastSyncTime: Long? = null

    override suspend fun getBeatForDate(dateEpochDay: Long): List<BeatOutletItem> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val dateStr = dateFormat.format(Date(dateEpochDay * 86400000L))

        val visits = beatDao.getVisitsForDate(dateStr, repId).first()
        if (visits.isEmpty()) {
            return emptyList()
        }

        return visits.map { visit ->
            val outletEntity = outletDao.getOutletById(visit.outletId)
            val outlet = outletEntity?.toDomain() ?: Outlet(
                id = visit.outletId,
                name = "Outlet #${visit.outletId}",
                ownerName = "Shop Owner",
                phone = "9876543210",
                address = "Market Area",
                area = "Lucknow",
                type = OutletType.GENERAL_TRADE,
                priceTier = 4,
                syncStatus = SyncStatus.SYNCED
            )

            BeatOutletItem(
                sequenceNumber = visit.sequenceNumber,
                outlet = outlet,
                visitState = try { VisitState.valueOf(visit.visitState) } catch (_: Exception) { VisitState.PLANNED },
                targetKg = visit.targetKg,
                freshBatchesCount = visit.freshBatchesCount,
                agingBatchesCount = visit.agingBatchesCount,
                lastVisitedTimestamp = visit.lastVisitedTimestamp,
                completedOrderSummary = visit.completedOrderSummary
            )
        }
    }

    override suspend fun getProgressForDate(dateEpochDay: Long): BeatProgress {
        val items = getBeatForDate(dateEpochDay)
        val total = items.size
        val visited = items.count { it.visitState == VisitState.COMPLETED }
        val pending = items.count { it.visitState == VisitState.PLANNED || it.visitState == VisitState.ACTIVE }
        val missed = items.count { it.visitState == VisitState.MISSED }

        val targetVol = items.sumOf { it.targetKg }
        val achievedVol = visited * 15.0

        return BeatProgress(
            planned = total,
            done = visited,
            pending = pending,
            missed = missed,
            targetVolumeKg = targetVol,
            achievedVolumeKg = achievedVol
        )
    }

    override suspend fun lastSyncedAt(): Long? = lastSyncTime

    fun setLastSyncedAt(timestamp: Long) {
        lastSyncTime = timestamp
    }
}
