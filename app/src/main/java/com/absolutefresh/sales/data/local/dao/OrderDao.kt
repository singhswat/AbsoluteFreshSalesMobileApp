package com.absolutefresh.sales.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.absolutefresh.sales.data.local.entity.FieldOrderEntity
import com.absolutefresh.sales.data.local.entity.FieldOrderWithItems
import com.absolutefresh.sales.data.local.entity.OrderItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Transaction
    @Query("SELECT * FROM field_orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<FieldOrderWithItems>>

    @Transaction
    @Query("SELECT * FROM field_orders WHERE id = :orderId")
    suspend fun getOrderById(orderId: String): FieldOrderWithItems?

    @Transaction
    @Query("SELECT * FROM field_orders WHERE outletId = :outletId ORDER BY createdAt DESC")
    fun getOrdersByOutlet(outletId: String): Flow<List<FieldOrderWithItems>>

    @Transaction
    @Query("SELECT * FROM field_orders WHERE deliveryStatus = :status ORDER BY createdAt DESC")
    fun getOrdersByDeliveryStatus(status: String): Flow<List<FieldOrderWithItems>>

    @Transaction
    @Query("SELECT * FROM field_orders WHERE syncStatus = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingSyncOrders(): List<FieldOrderWithItems>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<FieldOrderEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderEntity(order: FieldOrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItemEntities(items: List<OrderItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Transaction
    suspend fun insertOrderWithItems(order: FieldOrderEntity, items: List<OrderItemEntity>) {
        insertOrderEntity(order)
        insertOrderItemEntities(items)
    }

    @Query("UPDATE field_orders SET syncStatus = :syncStatus, syncedInvoiceNumber = :syncedInvoiceNumber, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateSyncStatus(
        orderId: String,
        syncStatus: String,
        syncedInvoiceNumber: String?,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE field_orders SET deliveryStatus = :deliveryStatus, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateDeliveryStatus(
        orderId: String,
        deliveryStatus: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("SELECT COUNT(*) FROM field_orders")
    suspend fun getOrderCount(): Int
}
