package com.absolutefresh.sales.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OutletDto(
    @SerialName("id") val id: String? = null,
    @SerialName("client_id") val clientId: String? = null,
    @SerialName("name") val name: String,
    @SerialName("area") val area: String,
    @SerialName("owner") val owner: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("assigned_rep") val assignedRep: String? = null,
    @SerialName("average_kg_per_day") val averageKgPerDay: Double = 0.0,
    @SerialName("status") val status: String = "active",
    @SerialName("price_tier") val priceTier: Int = 4,
    @SerialName("outlet_type") val outletType: String = "general_trade",
    @SerialName("gstin") val gstin: String? = null,
    @SerialName("address") val address: String? = null,
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
    @SerialName("sync_version") val syncVersion: Long = 1L
)

@Serializable
data class BeatVisitDto(
    @SerialName("id") val id: String? = null,
    @SerialName("rep") val rep: String = "",
    @SerialName("date") val date: String = "",
    @SerialName("outlet_id") val outletId: String = "",
    @SerialName("outlet_name") val outletName: String? = null,
    @SerialName("area") val area: String? = null,
    @SerialName("sequence") val sequence: Int = 1,
    @SerialName("target_kg") val targetKg: Double = 0.0,
    @SerialName("status") val status: String = "PLANNED",
    @SerialName("linked_order") val linkedOrder: String? = null,
    @SerialName("visited_time") val visitedTime: String? = null,
    @SerialName("missed_reason") val missedReason: String? = null
)

@Serializable
data class ProductDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("unit") val unit: String = "kg",
    @SerialName("base_price") val basePrice: Double = 0.0,
    @SerialName("mrp") val mrp: Double = 0.0,
    @SerialName("shelf_life") val shelfLife: Int = 10,
    @SerialName("pack_weight") val packWeight: String? = null,
    @SerialName("code") val code: String? = null,
    @SerialName("active") val active: Boolean = true
)

@Serializable
data class FieldOrderDto(
    @SerialName("id") val id: String? = null,
    @SerialName("client_id") val clientId: String,
    @SerialName("outlet_id") val outletId: String,
    @SerialName("outlet_name") val outletName: String? = null,
    @SerialName("rep_id") val repId: String,
    @SerialName("order_date") val orderDate: String,
    @SerialName("delivery_date") val deliveryDate: String? = null,
    @SerialName("status") val status: String = "PENDING",
    @SerialName("notes") val notes: String? = null,
    @SerialName("invoice_number") val invoiceNumber: String? = null,
    @SerialName("subtotal") val subtotal: Double = 0.0,
    @SerialName("tax_amount") val taxAmount: Double = 0.0,
    @SerialName("total_amount") val totalAmount: Double = 0.0,
    @SerialName("payment_mode") val paymentMode: String = "CASH",
    @SerialName("payment_status") val paymentStatus: String = "UNPAID",
    @SerialName("sync_version") val syncVersion: Long = 1L
)

@Serializable
data class FieldOrderItemDto(
    @SerialName("id") val id: String? = null,
    @SerialName("order_id") val orderId: String? = null,
    @SerialName("product_id") val productId: String,
    @SerialName("sku_name") val skuName: String,
    @SerialName("quantity") val quantity: Int,
    @SerialName("kg_ordered") val kgOrdered: Double,
    @SerialName("unit_price") val unitPrice: Double,
    @SerialName("discount_amount") val discountAmount: Double = 0.0,
    @SerialName("subtotal") val subtotal: Double = 0.0,
    @SerialName("tax_rate") val taxRate: Double = 0.05
)

@Serializable
data class HolidayDto(
    @SerialName("id") val id: String? = null,
    @SerialName("date") val date: String,
    @SerialName("name") val name: String
)
