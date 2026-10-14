package com.absolutefresh.sales.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.absolutefresh.sales.data.model.OrderDeliveryStatus
import com.absolutefresh.sales.data.model.PaymentMode
import com.absolutefresh.sales.data.model.SyncStatus

@Entity(tableName = "field_orders")
data class FieldOrderEntity(
    @PrimaryKey
    val id: String,
    val outletId: String,
    val outletName: String,
    val repId: String,
    val subtotal: Double,
    val gstAmount: Double,
    val totalAmount: Double,
    val paymentMode: String,
    val provisionalInvoiceNumber: String,
    val syncedInvoiceNumber: String?,
    val syncStatus: String,
    val deliveryStatus: String,
    val createdAt: Long,
    val updatedAt: Long
)
