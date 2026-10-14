package com.absolutefresh.sales.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.absolutefresh.sales.data.model.FieldOrder
import com.absolutefresh.sales.data.model.OrderDeliveryStatus
import com.absolutefresh.sales.data.model.PaymentMode
import com.absolutefresh.sales.data.model.SyncStatus

data class FieldOrderWithItems(
    @Embedded
    val order: FieldOrderEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "orderId"
    )
    val items: List<OrderItemEntity>
) {
    fun toDomain(): FieldOrder {
        return FieldOrder(
            id = order.id,
            outletId = order.outletId,
            outletName = order.outletName,
            repId = order.repId,
            items = items.map { it.toDomain() },
            subtotal = order.subtotal,
            gstAmount = order.gstAmount,
            totalAmount = order.totalAmount,
            paymentMode = PaymentMode.valueOf(order.paymentMode),
            provisionalInvoiceNumber = order.provisionalInvoiceNumber,
            syncedInvoiceNumber = order.syncedInvoiceNumber,
            syncStatus = SyncStatus.valueOf(order.syncStatus),
            deliveryStatus = OrderDeliveryStatus.valueOf(order.deliveryStatus),
            createdAt = order.createdAt
        )
    }
}
