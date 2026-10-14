package com.absolutefresh.sales.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.absolutefresh.sales.data.model.Outlet
import com.absolutefresh.sales.data.model.OutletType
import com.absolutefresh.sales.data.model.SyncStatus

@Entity(tableName = "outlets")
data class OutletEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val ownerName: String,
    val phone: String,
    val address: String,
    val area: String,
    val type: String,
    val priceTier: Int,
    val gstin: String?,
    val outstandingBalance: Double,
    val creditLimit: Double,
    val paymentTermsDays: Int,
    val avgKgPerDay: Double,
    val latitude: Double?,
    val longitude: Double?,
    val syncStatus: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Outlet {
        return Outlet(
            id = id,
            name = name,
            ownerName = ownerName,
            phone = phone,
            address = address,
            area = area,
            type = try { OutletType.valueOf(type) } catch (_: Exception) { OutletType.GENERAL_TRADE },
            priceTier = priceTier,
            gstin = gstin,
            outstandingBalance = outstandingBalance,
            creditLimit = creditLimit,
            paymentTermsDays = paymentTermsDays,
            avgKgPerDay = avgKgPerDay,
            latitude = latitude,
            longitude = longitude,
            syncStatus = try { SyncStatus.valueOf(syncStatus) } catch (_: Exception) { SyncStatus.SYNCED }
        )
    }

    companion object {
        fun fromDomain(outlet: Outlet): OutletEntity {
            return OutletEntity(
                id = outlet.id,
                name = outlet.name,
                ownerName = outlet.ownerName,
                phone = outlet.phone,
                address = outlet.address,
                area = outlet.area,
                type = outlet.type.name,
                priceTier = outlet.priceTier,
                gstin = outlet.gstin,
                outstandingBalance = outlet.outstandingBalance,
                creditLimit = outlet.creditLimit,
                paymentTermsDays = outlet.paymentTermsDays,
                avgKgPerDay = outlet.avgKgPerDay,
                latitude = outlet.latitude,
                longitude = outlet.longitude,
                syncStatus = outlet.syncStatus.name
            )
        }
    }
}
