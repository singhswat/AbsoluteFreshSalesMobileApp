package com.absolutefresh.sales

import com.absolutefresh.sales.data.local.dao.OrderDao
import com.absolutefresh.sales.data.local.dao.OutletDao
import com.absolutefresh.sales.data.local.dao.OutboxDao
import com.absolutefresh.sales.data.local.entity.FieldOrderEntity
import com.absolutefresh.sales.data.local.entity.FieldOrderWithItems
import com.absolutefresh.sales.data.local.entity.OrderItemEntity
import com.absolutefresh.sales.data.local.entity.OutletEntity
import com.absolutefresh.sales.data.local.entity.OutboxEntity
import com.absolutefresh.sales.data.model.FieldOrder
import com.absolutefresh.sales.data.model.OrderItem
import com.absolutefresh.sales.data.model.Outlet
import com.absolutefresh.sales.data.model.OutletType
import com.absolutefresh.sales.data.model.PaymentMode
import com.absolutefresh.sales.data.model.SyncStatus
import com.absolutefresh.sales.data.repository.RoomOrderRepository
import com.absolutefresh.sales.data.repository.RoomOutletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RoomRepositoryTest {

    private lateinit var fakeOutletDao: FakeOutletDao
    private lateinit var fakeOrderDao: FakeOrderDao
    private lateinit var fakeOutboxDao: FakeOutboxDao

    private lateinit var outletRepository: RoomOutletRepository
    private lateinit var orderRepository: RoomOrderRepository

    @Before
    fun setUp() {
        fakeOutletDao = FakeOutletDao()
        fakeOrderDao = FakeOrderDao()
        fakeOutboxDao = FakeOutboxDao()

        outletRepository = RoomOutletRepository(fakeOutletDao, fakeOutboxDao)
        orderRepository = RoomOrderRepository(fakeOrderDao, fakeOutboxDao)
    }

    @Test
    fun addOutlet_insertsEntityAndEnqueuesOutboxMutationAtomically() = runTest {
        val newOutlet = Outlet(
            id = "",
            name = "Aman Superstore",
            ownerName = "Aman Kumar",
            phone = "9876500001",
            address = "Shop 12, Indiranagar",
            area = "Indiranagar",
            type = OutletType.GENERAL_TRADE,
            priceTier = 4
        )

        val result = outletRepository.addOutlet(newOutlet)

        // Verifications
        assertNotNull(result.id)
        assertTrue(result.id.startsWith("out-"))
        assertEquals(SyncStatus.PENDING, result.syncStatus)

        // Verify entity in OutletDao
        val inserted = fakeOutletDao.insertedOutlets.find { it.id == result.id }
        assertNotNull(inserted)
        assertEquals("Aman Superstore", inserted?.name)

        // Verify Outbox mutation enqueued
        val enqueued = fakeOutboxDao.enqueuedMutations.find { it.entityId == result.id }
        assertNotNull(enqueued)
        assertEquals("OUTLET", enqueued?.entityType)
        assertEquals("CREATE", enqueued?.action)
        assertEquals("QUEUED", enqueued?.status)
    }

    @Test
    fun createOrder_insertsOrderWithItemsAndEnqueuesOutboxMutation() = runTest {
        val testOrder = FieldOrder(
            id = "ord-test-101",
            outletId = "o1",
            outletName = "Sharma Kirana",
            repId = "rep-01",
            items = listOf(
                OrderItem("p1", "Fresh Idli Dosa Batter 500g", 10, 5.0, 45.0, 450.0),
                OrderItem("p3", "Podi Chutney 100g", 4, 0.4, 28.0, 112.0)
            ),
            subtotal = 562.0,
            gstAmount = 28.10,
            totalAmount = 590.10,
            paymentMode = PaymentMode.CASH,
            provisionalInvoiceNumber = "PROV-2610-101"
        )

        val result = orderRepository.createOrder(testOrder)

        assertEquals("ord-test-101", result.id)

        // Verify order inserted in OrderDao
        val insertedOrder = fakeOrderDao.insertedOrders.find { it.id == "ord-test-101" }
        assertNotNull(insertedOrder)
        assertEquals(590.10, insertedOrder!!.totalAmount, 0.01)

        // Verify items inserted
        val insertedItems = fakeOrderDao.insertedItems.filter { it.orderId == "ord-test-101" }
        assertEquals(2, insertedItems.size)

        // Verify Outbox mutation
        val enqueued = fakeOutboxDao.enqueuedMutations.find { it.entityId == "ord-test-101" }
        assertNotNull(enqueued)
        assertEquals("ORDER", enqueued?.entityType)
        assertEquals("CREATE", enqueued?.action)
    }

    @Test
    fun outboxDao_fifoOrderProcessingAndRetryTracking() = runTest {
        fakeOutboxDao.enqueue(
            OutboxEntity(
                operationUuid = "uuid-1",
                entityType = "ORDER",
                entityId = "ord-1",
                action = "CREATE",
                payloadJson = "{}"
            )
        )
        fakeOutboxDao.enqueue(
            OutboxEntity(
                operationUuid = "uuid-2",
                entityType = "OUTLET",
                entityId = "out-1",
                action = "CREATE",
                payloadJson = "{}"
            )
        )

        val pending = fakeOutboxDao.getPendingMutations()
        assertEquals(2, pending.size)
        assertEquals("ord-1", pending[0].entityId) // FIFO order check

        // Mark processing
        fakeOutboxDao.markProcessing(pending[0].id)
        assertEquals("PROCESSING", fakeOutboxDao.enqueuedMutations.find { it.id == pending[0].id }?.status)

        // Mark synced
        fakeOutboxDao.markSynced(pending[0].id)
        assertEquals("SYNCED", fakeOutboxDao.enqueuedMutations.find { it.id == pending[0].id }?.status)
    }

    // --- In-Memory Fakes for DAO Testing ---

    private class FakeOutletDao : OutletDao {
        val insertedOutlets = mutableListOf<OutletEntity>()
        private val flow = MutableStateFlow<List<OutletEntity>>(emptyList())

        override fun getAllOutlets(): Flow<List<OutletEntity>> = flow.asStateFlow()

        override suspend fun getOutletById(id: String): OutletEntity? =
            insertedOutlets.find { it.id == id }

        override fun getOutletsByArea(area: String): Flow<List<OutletEntity>> = flow.asStateFlow()

        override fun searchOutlets(query: String): Flow<List<OutletEntity>> = flow.asStateFlow()

        override suspend fun findPossibleDuplicates(phone: String, name: String): List<OutletEntity> =
            insertedOutlets.filter { it.phone == phone || it.name.equals(name, ignoreCase = true) }

        override suspend fun insertOutlet(outlet: OutletEntity) {
            insertedOutlets.add(outlet)
            flow.value = insertedOutlets.toList()
        }

        override suspend fun insertOutlets(outlets: List<OutletEntity>) {
            insertedOutlets.addAll(outlets)
            flow.value = insertedOutlets.toList()
        }

        override suspend fun updateOutlet(outlet: OutletEntity) {
            val idx = insertedOutlets.indexOfFirst { it.id == outlet.id }
            if (idx >= 0) insertedOutlets[idx] = outlet
            flow.value = insertedOutlets.toList()
        }

        override suspend fun deleteOutlet(outlet: OutletEntity) {
            insertedOutlets.removeAll { it.id == outlet.id }
            flow.value = insertedOutlets.toList()
        }

        override suspend fun getOutletCount(): Int = insertedOutlets.size
    }

    private class FakeOrderDao : OrderDao {
        val insertedOrders = mutableListOf<FieldOrderEntity>()
        val insertedItems = mutableListOf<OrderItemEntity>()
        private val flow = MutableStateFlow<List<FieldOrderWithItems>>(emptyList())

        override fun getAllOrders(): Flow<List<FieldOrderWithItems>> = flow.asStateFlow()

        override suspend fun getOrderById(orderId: String): FieldOrderWithItems? {
            val order = insertedOrders.find { it.id == orderId } ?: return null
            val items = insertedItems.filter { it.orderId == orderId }
            return FieldOrderWithItems(order, items)
        }

        override fun getOrdersByOutlet(outletId: String): Flow<List<FieldOrderWithItems>> = flow.asStateFlow()
        override fun getOrdersByDeliveryStatus(status: String): Flow<List<FieldOrderWithItems>> = flow.asStateFlow()

        override suspend fun getPendingSyncOrders(): List<FieldOrderWithItems> {
            return insertedOrders.filter { it.syncStatus == "PENDING" }.map { order ->
                FieldOrderWithItems(order, insertedItems.filter { it.orderId == order.id })
            }
        }

        override suspend fun insertOrders(orders: List<FieldOrderEntity>) {
            insertedOrders.addAll(orders)
            flow.value = insertedOrders.map { order ->
                FieldOrderWithItems(order, insertedItems.filter { it.orderId == order.id })
            }
        }

        override suspend fun insertOrderEntity(order: FieldOrderEntity) {
            insertedOrders.add(order)
            flow.value = insertedOrders.map { o ->
                FieldOrderWithItems(o, insertedItems.filter { it.orderId == o.id })
            }
        }

        override suspend fun insertOrderItemEntities(items: List<OrderItemEntity>) {
            insertedItems.addAll(items)
            flow.value = insertedOrders.map { o ->
                FieldOrderWithItems(o, insertedItems.filter { it.orderId == o.id })
            }
        }

        override suspend fun insertOrderItems(items: List<OrderItemEntity>) {
            insertedItems.addAll(items)
            flow.value = insertedOrders.map { o ->
                FieldOrderWithItems(o, insertedItems.filter { it.orderId == o.id })
            }
        }

        override suspend fun updateSyncStatus(
            orderId: String,
            syncStatus: String,
            syncedInvoiceNumber: String?,
            updatedAt: Long
        ) {
            val idx = insertedOrders.indexOfFirst { it.id == orderId }
            if (idx >= 0) {
                insertedOrders[idx] = insertedOrders[idx].copy(
                    syncStatus = syncStatus,
                    syncedInvoiceNumber = syncedInvoiceNumber,
                    updatedAt = updatedAt
                )
            }
        }

        override suspend fun updateDeliveryStatus(
            orderId: String,
            deliveryStatus: String,
            updatedAt: Long
        ) {
            val idx = insertedOrders.indexOfFirst { it.id == orderId }
            if (idx >= 0) {
                insertedOrders[idx] = insertedOrders[idx].copy(
                    deliveryStatus = deliveryStatus,
                    updatedAt = updatedAt
                )
            }
        }

        override suspend fun getOrderCount(): Int = insertedOrders.size
    }

    private class FakeOutboxDao : OutboxDao {
        val enqueuedMutations = mutableListOf<OutboxEntity>()
        private var idCounter = 1L

        override suspend fun enqueue(mutation: OutboxEntity): Long {
            val assigned = mutation.copy(id = idCounter++)
            enqueuedMutations.add(assigned)
            return assigned.id
        }

        override suspend fun getPendingMutations(): List<OutboxEntity> {
            return enqueuedMutations.filter { it.status in listOf("QUEUED", "FAILED") && it.retryCount < 5 }
        }

        override suspend fun markProcessing(id: Long, timestamp: Long) {
            val idx = enqueuedMutations.indexOfFirst { it.id == id }
            if (idx >= 0) enqueuedMutations[idx] = enqueuedMutations[idx].copy(status = "PROCESSING", lastAttemptAt = timestamp)
        }

        override suspend fun markSynced(id: Long, timestamp: Long) {
            val idx = enqueuedMutations.indexOfFirst { it.id == id }
            if (idx >= 0) enqueuedMutations[idx] = enqueuedMutations[idx].copy(status = "SYNCED", lastAttemptAt = timestamp)
        }

        override suspend fun markFailed(
            id: Long,
            errorMessage: String,
            retryCount: Int,
            timestamp: Long
        ) {
            val idx = enqueuedMutations.indexOfFirst { it.id == id }
            if (idx >= 0) enqueuedMutations[idx] = enqueuedMutations[idx].copy(
                status = "FAILED",
                errorMessage = errorMessage,
                retryCount = retryCount,
                lastAttemptAt = timestamp
            )
        }

        override fun getPendingCountFlow(): Flow<Int> = MutableStateFlow(enqueuedMutations.count { it.status in listOf("QUEUED", "PROCESSING") }).asStateFlow()

        override suspend fun getPendingCount(): Int = enqueuedMutations.count { it.status in listOf("QUEUED", "PROCESSING") }

        override suspend fun pruneSynced(beforeTimestamp: Long) {
            enqueuedMutations.removeAll { it.status == "SYNCED" && it.createdAt < beforeTimestamp }
        }
    }
}
