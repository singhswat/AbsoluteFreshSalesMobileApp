package com.absolutefresh.sales.data.repository

import com.absolutefresh.sales.data.model.OrderDeliveryStatus
import com.absolutefresh.sales.data.model.PaymentMode
import com.absolutefresh.sales.data.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DeliveryItem(
    val id: String,
    val orderId: String,
    val outletId: String,
    val outletName: String,
    val outletAddress: String,
    val outletPhone: String,
    val sequence: Int,
    val orderedPackets: Int,
    val orderedWeightKg: Double,
    val invoiceNumber: String,
    val invoiceAmount: Double,
    val deliveredPackets: Int = 0,
    val amountCollected: Double = 0.0,
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val deliveryStatus: OrderDeliveryStatus = OrderDeliveryStatus.PENDING,
    val completedTimestamp: Long? = null,
    val podMethod: String = "Digital OTP / Signature",
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)

interface DeliveryRepository {
    fun observeDeliveries(): Flow<List<DeliveryItem>>
    suspend fun getDeliveries(): List<DeliveryItem>
    suspend fun getDeliveryById(deliveryId: String): DeliveryItem?
    suspend fun confirmDelivery(
        deliveryId: String,
        deliveredPackets: Int,
        amountCollected: Double,
        paymentMode: PaymentMode,
        podMethod: String
    ): DeliveryItem
}

class FakeDeliveryRepository : DeliveryRepository {

    private val now = System.currentTimeMillis()
    private val oneHour = 3600_000L

    private val initialDeliveries = listOf(
        DeliveryItem(
            id = "del-01",
            orderId = "ord-001",
            outletId = "o1",
            outletName = "Sharma Kirana",
            outletAddress = "12 Indira Nagar, Lucknow",
            outletPhone = "9876543201",
            sequence = 1,
            orderedPackets = 30,
            orderedWeightKg = 15.0,
            invoiceNumber = "AFF-2610-0101",
            invoiceAmount = 1239.0,
            deliveredPackets = 30,
            amountCollected = 1239.0,
            paymentMode = PaymentMode.CASH,
            deliveryStatus = OrderDeliveryStatus.DELIVERED,
            completedTimestamp = now - (3 * oneHour),
            syncStatus = SyncStatus.SYNCED
        ),
        DeliveryItem(
            id = "del-02",
            orderId = "ord-002",
            outletId = "o2",
            outletName = "Gupta Dairy & More",
            outletAddress = "45 Vikas Nagar, Lucknow",
            outletPhone = "9876543202",
            sequence = 2,
            orderedPackets = 40,
            orderedWeightKg = 25.0,
            invoiceNumber = "AFF-2610-0102",
            invoiceAmount = 2310.0,
            deliveredPackets = 40,
            amountCollected = 2310.0,
            paymentMode = PaymentMode.UPI_QR,
            deliveryStatus = OrderDeliveryStatus.DELIVERED,
            completedTimestamp = now - (1 * oneHour),
            syncStatus = SyncStatus.SYNCED
        ),
        DeliveryItem(
            id = "del-03",
            orderId = "ord-003",
            outletId = "o3",
            outletName = "Krishna Sweets",
            outletAddress = "7 Hazratganj, Lucknow",
            outletPhone = "9876543203",
            sequence = 3,
            orderedPackets = 25,
            orderedWeightKg = 25.0,
            invoiceNumber = "PROV-2610-003",
            invoiceAmount = 2152.5,
            deliveredPackets = 0,
            amountCollected = 0.0,
            paymentMode = PaymentMode.CREDIT_15D,
            deliveryStatus = OrderDeliveryStatus.PENDING,
            syncStatus = SyncStatus.PENDING
        ),
        DeliveryItem(
            id = "del-04",
            orderId = "ord-004",
            outletId = "o5",
            outletName = "Om Sai Provision",
            outletAddress = "3 Alambagh, Lucknow",
            outletPhone = "9876543205",
            sequence = 4,
            orderedPackets = 20,
            orderedWeightKg = 8.5,
            invoiceNumber = "PROV-2610-004",
            invoiceAmount = 1034.25,
            deliveredPackets = 0,
            amountCollected = 0.0,
            paymentMode = PaymentMode.CASH,
            deliveryStatus = OrderDeliveryStatus.PENDING,
            syncStatus = SyncStatus.PENDING
        ),
        DeliveryItem(
            id = "del-05",
            orderId = "ord-005",
            outletId = "o6",
            outletName = "Fresh Bite Café",
            outletAddress = "88 Aliganj, Lucknow",
            outletPhone = "9876543206",
            sequence = 5,
            orderedPackets = 15,
            orderedWeightKg = 7.5,
            invoiceNumber = "PROV-2610-005",
            invoiceAmount = 850.0,
            deliveredPackets = 0,
            amountCollected = 0.0,
            paymentMode = PaymentMode.UPI_QR,
            deliveryStatus = OrderDeliveryStatus.PENDING,
            syncStatus = SyncStatus.PENDING
        )
    )

    private val deliveriesFlow = MutableStateFlow(initialDeliveries)

    override fun observeDeliveries(): Flow<List<DeliveryItem>> = deliveriesFlow.asStateFlow()

    override suspend fun getDeliveries(): List<DeliveryItem> = deliveriesFlow.value
    override suspend fun getDeliveryById(deliveryId: String): DeliveryItem? =
        deliveriesFlow.value.find { it.id == deliveryId }

    override suspend fun confirmDelivery(
        deliveryId: String,
        deliveredPackets: Int,
        amountCollected: Double,
        paymentMode: PaymentMode,
        podMethod: String
    ): DeliveryItem {
        val current = deliveriesFlow.value
        val target = current.find { it.id == deliveryId } ?: throw IllegalArgumentException("Delivery not found")
        val updated = target.copy(
            deliveredPackets = deliveredPackets,
            amountCollected = amountCollected,
            paymentMode = paymentMode,
            podMethod = podMethod,
            deliveryStatus = OrderDeliveryStatus.DELIVERED,
            completedTimestamp = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING
        )
        deliveriesFlow.value = current.map { if (it.id == deliveryId) updated else it }
        return updated
    }
}
