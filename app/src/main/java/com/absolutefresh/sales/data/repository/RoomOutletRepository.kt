package com.absolutefresh.sales.data.repository

import com.absolutefresh.sales.data.local.dao.OutletDao
import com.absolutefresh.sales.data.local.dao.OutboxDao
import com.absolutefresh.sales.data.local.entity.OutletEntity
import com.absolutefresh.sales.data.local.entity.OutboxEntity
import com.absolutefresh.sales.data.model.Outlet
import com.absolutefresh.sales.data.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

class RoomOutletRepository(
    private val outletDao: OutletDao,
    private val outboxDao: OutboxDao
) : OutletRepository {

    override fun observeOutlets(): Flow<List<Outlet>> {
        return outletDao.getAllOutlets().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getOutlets(): List<Outlet> {
        return observeOutlets().first()
    }

    override suspend fun getOutletById(id: String): Outlet? {
        return outletDao.getOutletById(id)?.toDomain()
    }

    override suspend fun addOutlet(outlet: Outlet): Outlet {
        val assignedId = if (outlet.id.isBlank()) "out-${UUID.randomUUID()}" else outlet.id
        val pendingOutlet = outlet.copy(
            id = assignedId,
            syncStatus = SyncStatus.PENDING
        )

        val entity = OutletEntity.fromDomain(pendingOutlet)
        outletDao.insertOutlet(entity)

        // Transactional outbox entry with idempotent client UUID
        val operationUuid = UUID.randomUUID().toString()
        outboxDao.enqueue(
            OutboxEntity(
                operationUuid = operationUuid,
                entityType = "OUTLET",
                entityId = assignedId,
                action = "CREATE",
                payloadJson = """{"id":"$assignedId","name":"${outlet.name}","phone":"${outlet.phone}","area":"${outlet.area}"}"""
            )
        )

        return pendingOutlet
    }

    override suspend fun findPotentialDuplicates(name: String, phone: String): List<Outlet> {
        return outletDao.findPossibleDuplicates(phone, name).map { it.toDomain() }
    }
}
