package com.absolutefresh.sales.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.absolutefresh.sales.data.model.OrderItem

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = FieldOrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["orderId"])]
)
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: String,
    val productId: String,
    val productName: String,
    val quantityPackets: Int,
    val weightKg: Double,
    val unitPrice: Double,
    val lineTotal: Double
) {
    fun toDomain(): OrderItem {
        return OrderItem(
            productId = productId,
            productName = productName,
            quantityPackets = quantityPackets,
            weightKg = weightKg,
            unitPrice = unitPrice,
            lineTotal = lineTotal
        )
    }

    companion object {
        fun fromDomain(orderId: String, item: OrderItem): OrderItemEntity {
            return OrderItemEntity(
                orderId = orderId,
                productId = item.productId,
                productName = item.productName,
                quantityPackets = item.quantityPackets,
                weightKg = item.weightKg,
                unitPrice = item.unitPrice,
                lineTotal = item.lineTotal
            )
        }
    }
}
