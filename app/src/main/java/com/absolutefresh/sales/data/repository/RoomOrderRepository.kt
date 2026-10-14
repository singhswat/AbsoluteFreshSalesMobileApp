package com.absolutefresh.sales.data.repository

import com.absolutefresh.sales.data.local.dao.OrderDao
import com.absolutefresh.sales.data.local.dao.OutboxDao
import com.absolutefresh.sales.data.local.entity.FieldOrderEntity
import com.absolutefresh.sales.data.local.entity.OrderItemEntity
import com.absolutefresh.sales.data.local.entity.OutboxEntity
import com.absolutefresh.sales.data.model.FieldOrder
import com.absolutefresh.sales.data.model.Product
import com.absolutefresh.sales.data.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

class RoomOrderRepository(
    private val orderDao: OrderDao,
    private val outboxDao: OutboxDao
) : OrderRepository {

    private val catalogProducts = listOf(
        Product(
            id = "p1",
            name = "Fresh Idli Dosa Batter 500g",
            sku = "BAT-500",
            packWeightGrams = 500,
            mrp = 60.0,
            tier4Price = 45.0,
            shelfLifeDays = 10
        ),
        Product(
            id = "p2",
            name = "Fresh Idli Dosa Batter 1kg",
            sku = "BAT-1000",
            packWeightGrams = 1000,
            mrp = 110.0,
            tier4Price = 85.0,
            shelfLifeDays = 10
        ),
        Product(
            id = "p3",
            name = "Special Gunpowder Podi Chutney 100g",
            sku = "PODI-100",
            packWeightGrams = 100,
            mrp = 40.0,
            tier4Price = 28.0,
            shelfLifeDays = 30
        ),
        Product(
            id = "p4",
            name = "Traditional Filter Coffee Decoction 200ml",
            sku = "COF-200",
            packWeightGrams = 200,
            mrp = 80.0,
            tier4Price = 62.0,
            shelfLifeDays = 15
        )
    )

    override fun observeOrders(): Flow<List<FieldOrder>> {
        return orderDao.getAllOrders().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getOrders(): List<FieldOrder> {
        return observeOrders().first()
    }

    override suspend fun getOrderById(id: String): FieldOrder? {
        return orderDao.getOrderById(id)?.toDomain()
    }

    override suspend fun createOrder(order: FieldOrder): FieldOrder {
        val now = System.currentTimeMillis()
        val orderEntity = FieldOrderEntity(
            id = order.id,
            outletId = order.outletId,
            outletName = order.outletName,
            repId = order.repId,
            subtotal = order.subtotal,
            gstAmount = order.gstAmount,
            totalAmount = order.totalAmount,
            paymentMode = order.paymentMode.name,
            provisionalInvoiceNumber = order.provisionalInvoiceNumber,
            syncedInvoiceNumber = order.syncedInvoiceNumber,
            syncStatus = order.syncStatus.name,
            deliveryStatus = order.deliveryStatus.name,
            createdAt = order.createdAt,
            updatedAt = now
        )

        val itemEntities = order.items.map { item ->
            OrderItemEntity.fromDomain(order.id, item)
        }

        // Atomic Room transaction
        orderDao.insertOrderWithItems(orderEntity, itemEntities)

        // Atomic Outbox enqueue
        val operationUuid = UUID.randomUUID().toString()
        outboxDao.enqueue(
            OutboxEntity(
                operationUuid = operationUuid,
                entityType = "ORDER",
                entityId = order.id,
                action = "CREATE",
                payloadJson = """{"orderId":"${order.id}","outletId":"${order.outletId}","total":${order.totalAmount},"invoice":"${order.provisionalInvoiceNumber}"}"""
            )
        )

        return order
    }

    override suspend fun getAvailableProducts(): List<Product> = catalogProducts
}
