package com.absolutefresh.sales.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.absolutefresh.sales.data.model.BeatOutletItem
import com.absolutefresh.sales.data.model.Outlet
import com.absolutefresh.sales.data.model.VisitState

@Entity(
    tableName = "beat_visits",
    foreignKeys = [
        ForeignKey(
            entity = OutletEntity::class,
            parentColumns = ["id"],
            childColumns = ["outletId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["date", "repId"]),
        Index(value = ["outletId"])
    ]
)
data class BeatVisitEntity(
    @PrimaryKey
    val id: String,
    val date: String,
    val repId: String,
    val outletId: String,
    val sequenceNumber: Int,
    val visitState: String,
    val targetKg: Double,
    val freshBatchesCount: Int = 2,
    val agingBatchesCount: Int = 0,
    val lastVisitedTimestamp: Long? = null,
    val completedOrderSummary: String? = null,
    val missedReason: String? = null,
    val syncStatus: String = "PENDING"
) {
    fun toDomain(outlet: Outlet): BeatOutletItem {
        return BeatOutletItem(
            sequenceNumber = sequenceNumber,
            outlet = outlet,
            visitState = try { VisitState.valueOf(visitState) } catch (_: Exception) { VisitState.PLANNED },
            targetKg = targetKg,
            freshBatchesCount = freshBatchesCount,
            agingBatchesCount = agingBatchesCount,
            lastVisitedTimestamp = lastVisitedTimestamp,
            completedOrderSummary = completedOrderSummary
        )
    }
}
