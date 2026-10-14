package com.absolutefresh.sales.data.repository

import com.absolutefresh.sales.data.model.AuditEventType
import com.absolutefresh.sales.data.model.FieldDiff
import com.absolutefresh.sales.data.model.OrderAuditEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

interface OrderHistoryRepository {
    fun observeHistoryForOrder(orderId: String): Flow<List<OrderAuditEntry>>
    suspend fun getHistoryForOrder(orderId: String): List<OrderAuditEntry>
    suspend fun addAuditEntry(entry: OrderAuditEntry)
}

class FakeOrderHistoryRepository : OrderHistoryRepository {

    private val now = System.currentTimeMillis()
    private val oneMinute = 60_000L
    private val oneHour = 3600_000L

    private val initialEntries = mutableListOf(
        // Order ord-001 (Sharma Kirana)
        OrderAuditEntry(
            id = "audit-101",
            orderId = "ord-001",
            eventType = AuditEventType.ORDER_CREATED,
            timestamp = now - (2 * oneHour),
            actorName = "Swat (Sales Rep)",
            actorRole = "Sales Rep",
            reason = "Initial order booked from Monday Beat visit",
            operationUuid = "op-ord-001-create",
            diffs = listOf(
                FieldDiff("Items", "None", "2 items (30 pkts, 11.0 kg)"),
                FieldDiff("Subtotal", "₹0.00", "₹1,180.00"),
                FieldDiff("Total Payable", "₹0.00", "₹1,239.00 (inc. 5% GST)"),
                FieldDiff("Payment Mode", "None", "CASH")
            )
        ),
        OrderAuditEntry(
            id = "audit-102",
            orderId = "ord-001",
            eventType = AuditEventType.INVOICE_ISSUED,
            timestamp = now - (90 * oneMinute),
            actorName = "Automated Billing Service",
            actorRole = "System Engine",
            reason = "Server invoice number allocated replacing offline provisional reference",
            operationUuid = "op-ord-001-inv",
            diffs = listOf(
                FieldDiff("Invoice Number", "PROV-2610-001 (Draft)", "AFF-2610-0101 (Official GST)")
            )
        ),
        OrderAuditEntry(
            id = "audit-103",
            orderId = "ord-001",
            eventType = AuditEventType.DELIVERY_STATUS_CHANGED,
            timestamp = now - (30 * oneMinute),
            actorName = "Ramesh Kumar (Driver)",
            actorRole = "Delivery Staff",
            reason = "Delivered all 30 pkts to shop manager. Cash payment verified.",
            operationUuid = "op-ord-001-del",
            diffs = listOf(
                FieldDiff("Delivery Status", "PENDING", "DELIVERED"),
                FieldDiff("Cash Collected", "₹0.00", "₹1,239.00 (Full Amount)")
            )
        ),

        // Order ord-002 (Gupta Dairy & More)
        OrderAuditEntry(
            id = "audit-201",
            orderId = "ord-002",
            eventType = AuditEventType.ORDER_CREATED,
            timestamp = now - (4 * oneHour),
            actorName = "Swat (Sales Rep)",
            actorRole = "Sales Rep",
            reason = "Pre-booking order placed via Outlets directory",
            operationUuid = "op-ord-002-create",
            diffs = listOf(
                FieldDiff("Items", "None", "2 items (30 pkts, 20.0 kg)"),
                FieldDiff("Total Payable", "₹0.00", "₹1,860.00"),
                FieldDiff("Payment Mode", "None", "UPI_QR")
            )
        ),
        OrderAuditEntry(
            id = "audit-202",
            orderId = "ord-002",
            eventType = AuditEventType.ITEMS_AMENDED,
            timestamp = now - (3 * oneHour),
            actorName = "Swat (Sales Rep)",
            actorRole = "Sales Rep",
            reason = "Shop owner called requesting +10 extra 500g batter packets for evening festival rush",
            operationUuid = "op-ord-002-amend",
            diffs = listOf(
                FieldDiff("Batter 500g Qty", "20 pkts (10.0 kg)", "30 pkts (15.0 kg)"),
                FieldDiff("Subtotal", "₹1,770.00", "₹2,200.00"),
                FieldDiff("Total Payable", "₹1,860.00", "₹2,310.00")
            )
        ),
        OrderAuditEntry(
            id = "audit-203",
            orderId = "ord-002",
            eventType = AuditEventType.DELIVERY_STATUS_CHANGED,
            timestamp = now - (1 * oneHour),
            actorName = "Dispatch Logistics Team",
            actorRole = "Central Kitchen",
            reason = "Stock loaded onto van for Hazratganj beat delivery run",
            operationUuid = "op-ord-002-disp",
            diffs = listOf(
                FieldDiff("Delivery Status", "PENDING", "DISPATCHED")
            )
        ),

        // Order ord-003 (Krishna Sweets - Sync Conflict Resolution Demo)
        OrderAuditEntry(
            id = "audit-301",
            orderId = "ord-003",
            eventType = AuditEventType.ORDER_CREATED,
            timestamp = now - (5 * oneHour),
            actorName = "Swat (Sales Rep)",
            actorRole = "Sales Rep",
            reason = "Offline order raised while out of mobile cellular coverage",
            operationUuid = "op-ord-003-create",
            diffs = listOf(
                FieldDiff("Items", "None", "Fresh Batter 1kg (20 pkts, 20.0 kg)"),
                FieldDiff("Unit Price", "None", "₹85.00 / kg (Standard Tier 4)"),
                FieldDiff("Total Payable", "None", "₹1,785.00")
            )
        ),
        OrderAuditEntry(
            id = "audit-302",
            orderId = "ord-003",
            eventType = AuditEventType.SYNC_RESOLVED,
            timestamp = now - (4 * oneHour),
            actorName = "Optimistic Concurrency Engine",
            actorRole = "Sync Engine",
            reason = "Server concurrency version check detected active promotion pricing. Applied server authoritative price tier without losing order items.",
            operationUuid = "op-ord-003-sync-conflict",
            isConflictResolution = true,
            localProposedValue = "₹85.00/kg (Standard Tier 4) • Total: ₹1,785.00",
            serverAuthoritativeValue = "₹80.00/kg (Promotional Tier 3) • Total: ₹1,680.00",
            diffs = listOf(
                FieldDiff("Price Tier Rate", "₹85.00 / kg", "₹80.00 / kg (Promotional)"),
                FieldDiff("Total Payable", "₹1,785.00", "₹1,680.00 (-₹105.00 savings)")
            )
        )
    )

    private val _entriesFlow = MutableStateFlow<List<OrderAuditEntry>>(initialEntries)

    override fun observeHistoryForOrder(orderId: String): Flow<List<OrderAuditEntry>> {
        return _entriesFlow.map { list ->
            list.filter { it.orderId == orderId }.sortedByDescending { it.timestamp }
        }
    }

    override suspend fun getHistoryForOrder(orderId: String): List<OrderAuditEntry> {
        return _entriesFlow.value.filter { it.orderId == orderId }.sortedByDescending { it.timestamp }
    }

    override suspend fun addAuditEntry(entry: OrderAuditEntry) {
        val current = _entriesFlow.value.toMutableList()
        current.add(entry)
        _entriesFlow.value = current
    }
}
