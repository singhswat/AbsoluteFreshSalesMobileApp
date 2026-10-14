package com.absolutefresh.sales.data.repository

import com.absolutefresh.sales.data.model.FieldOrder
import com.absolutefresh.sales.data.model.OrderDeliveryStatus
import com.absolutefresh.sales.data.model.OrderItem
import com.absolutefresh.sales.data.model.PaymentMode
import com.absolutefresh.sales.data.model.Product
import com.absolutefresh.sales.data.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

interface OrderRepository {
    fun observeOrders(): Flow<List<FieldOrder>>
    suspend fun getOrders(): List<FieldOrder>
    suspend fun getOrderById(id: String): FieldOrder?
    suspend fun createOrder(order: FieldOrder): FieldOrder
    suspend fun getAvailableProducts(): List<Product>
}

class FakeOrderRepository : OrderRepository {

    private val availableProducts = listOf(
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

    private val now = System.currentTimeMillis()
    private val oneHour = 3600_000L
    private val oneDay = 86400_000L

    private val initialOrders = listOf(
        FieldOrder(
            id = "ord-001",
            outletId = "o1",
            outletName = "Sharma Kirana",
            repId = "rep-01",
            items = listOf(
                OrderItem("p1", "Fresh Idli Dosa Batter 500g", 20, 10.0, 45.0, 900.0),
                OrderItem("p3", "Special Gunpowder Podi Chutney 100g", 10, 1.0, 28.0, 280.0)
            ),
            subtotal = 1180.0,
            gstAmount = 59.0,
            totalAmount = 1239.0,
            paymentMode = PaymentMode.CASH,
            provisionalInvoiceNumber = "PROV-2610-001",
            syncedInvoiceNumber = "AFF-2610-0101",
            syncStatus = SyncStatus.SYNCED,
            deliveryStatus = OrderDeliveryStatus.DELIVERED,
            createdAt = now - (2 * oneHour)
        ),
        FieldOrder(
            id = "ord-002",
            outletId = "o2",
            outletName = "Gupta Dairy & More",
            repId = "rep-01",
            items = listOf(
                OrderItem("p1", "Fresh Idli Dosa Batter 500g", 30, 15.0, 45.0, 1350.0),
                OrderItem("p2", "Fresh Idli Dosa Batter 1kg", 10, 10.0, 85.0, 850.0)
            ),
            subtotal = 2200.0,
            gstAmount = 110.0,
            totalAmount = 2310.0,
            paymentMode = PaymentMode.UPI_QR,
            provisionalInvoiceNumber = "PROV-2610-002",
            syncedInvoiceNumber = "AFF-2610-0102",
            syncStatus = SyncStatus.SYNCED,
            deliveryStatus = OrderDeliveryStatus.DISPATCHED,
            createdAt = now - (4 * oneHour)
        ),
        FieldOrder(
            id = "ord-003",
            outletId = "o3",
            outletName = "Krishna Sweets",
            repId = "rep-01",
            items = listOf(
                OrderItem("p2", "Fresh Idli Dosa Batter 1kg", 25, 25.0, 82.0, 2050.0)
            ),
            subtotal = 2050.0,
            gstAmount = 102.5,
            totalAmount = 2152.5,
            paymentMode = PaymentMode.CREDIT_15D,
            provisionalInvoiceNumber = "PROV-2610-003",
            syncedInvoiceNumber = null,
            syncStatus = SyncStatus.PENDING,
            deliveryStatus = OrderDeliveryStatus.PENDING,
            createdAt = now - (30 * 60_000L)
        ),
        FieldOrder(
            id = "ord-004",
            outletId = "o5",
            outletName = "Om Sai Provision",
            repId = "rep-01",
            items = listOf(
                OrderItem("p1", "Fresh Idli Dosa Batter 500g", 15, 7.5, 45.0, 675.0),
                OrderItem("p4", "Traditional Filter Coffee Decoction 200ml", 5, 1.0, 62.0, 310.0)
            ),
            subtotal = 985.0,
            gstAmount = 49.25,
            totalAmount = 1034.25,
            paymentMode = PaymentMode.CASH,
            provisionalInvoiceNumber = "PROV-2610-004",
            syncedInvoiceNumber = null,
            syncStatus = SyncStatus.FAILED,
            deliveryStatus = OrderDeliveryStatus.PENDING,
            createdAt = now - (1 * oneHour)
        ),
        FieldOrder(
            id = "ord-005",
            outletId = "o4",
            outletName = "New Delhi Canteen",
            repId = "rep-01",
            items = listOf(
                OrderItem("p1", "Fresh Idli Dosa Batter 500g", 50, 25.0, 42.0, 2100.0),
                OrderItem("p2", "Fresh Idli Dosa Batter 1kg", 20, 20.0, 80.0, 1600.0)
            ),
            subtotal = 3700.0,
            gstAmount = 185.0,
            totalAmount = 3885.0,
            paymentMode = PaymentMode.CREDIT_15D,
            provisionalInvoiceNumber = "PROV-2610-005",
            syncedInvoiceNumber = "AFF-2610-0098",
            syncStatus = SyncStatus.SYNCED,
            deliveryStatus = OrderDeliveryStatus.DELIVERED,
            createdAt = now - (2 * oneDay)
        )
    )

    private val ordersFlow = MutableStateFlow(initialOrders)

    override fun observeOrders(): Flow<List<FieldOrder>> = ordersFlow.asStateFlow()

    override suspend fun getOrders(): List<FieldOrder> = ordersFlow.value

    override suspend fun getOrderById(id: String): FieldOrder? =
        ordersFlow.value.find { it.id == id }

    override suspend fun createOrder(order: FieldOrder): FieldOrder {
        val newOrder = if (order.id.isBlank()) {
            order.copy(id = "ord-${UUID.randomUUID()}")
        } else {
            order
        }
        ordersFlow.value = listOf(newOrder) + ordersFlow.value
        return newOrder
    }

    override suspend fun getAvailableProducts(): List<Product> = availableProducts
}
