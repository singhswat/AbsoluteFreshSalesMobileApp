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
            if (list.isEmpty()) {
                seedInitialOrders()
                orderDao.getAllOrders().first().map { it.toDomain() }
            } else {
                list.map { it.toDomain() }
            }
        }
    }

    private suspend fun seedInitialOrders() {
        val now = System.currentTimeMillis()
        val seedOrders = listOf(
            FieldOrderEntity("ord-1", "o1", "Sharma Kirana", "rep-1", 1142.86, 57.14, 1200.0, "CASH", "AFF-2610-001", "AFF-2610-001", "SYNCED", "DELIVERED", now - 86400000L, now - 86400000L),
            FieldOrderEntity("ord-2", "o2", "Gupta Dairy & More", "rep-1", 428.57, 21.43, 450.0, "UPI", "AFF-2610-002", "AFF-2610-002", "SYNCED", "DELIVERED", now - 43200000L, now - 43200000L),
            FieldOrderEntity("ord-3", "o4", "New Delhi Canteen", "rep-1", 3700.0, 185.0, 3885.0, "CREDIT", "AFF-2610-003", null, "PENDING", "PENDING", now - 7200000L, now - 7200000L),
            FieldOrderEntity("ord-4", "o7", "Laxmi General Store", "rep-1", 2285.71, 114.29, 2400.0, "CASH", "AFF-2610-004", "AFF-2610-004", "SYNCED", "DELIVERED", now - 3600000L, now - 3600000L)
        )
        val seedItems = listOf(
            OrderItemEntity(orderId = "ord-1", productId = "p2", productName = "Fresh Idli Dosa Batter 1kg", quantityPackets = 12, weightKg = 12.0, unitPrice = 85.0, lineTotal = 1020.0),
            OrderItemEntity(orderId = "ord-1", productId = "p3", productName = "Podi Chutney 30g", quantityPackets = 6, weightKg = 0.18, unitPrice = 28.0, lineTotal = 168.0),
            OrderItemEntity(orderId = "ord-2", productId = "p1", productName = "Fresh Idli Dosa Batter 500g", quantityPackets = 10, weightKg = 5.0, unitPrice = 45.0, lineTotal = 450.0),
            OrderItemEntity(orderId = "ord-3", productId = "p2", productName = "Fresh Idli Dosa Batter 1kg", quantityPackets = 40, weightKg = 40.0, unitPrice = 85.0, lineTotal = 3400.0),
            OrderItemEntity(orderId = "ord-4", productId = "p1", productName = "Fresh Idli Dosa Batter 500g", quantityPackets = 40, weightKg = 20.0, unitPrice = 45.0, lineTotal = 1800.0)
        )
        orderDao.insertOrders(seedOrders)
        orderDao.insertOrderItems(seedItems)
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
