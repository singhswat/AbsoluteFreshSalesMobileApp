package com.absolutefresh.sales.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.absolutefresh.sales.data.local.AppDatabase
import com.absolutefresh.sales.data.network.NetworkDataSource
import com.absolutefresh.sales.data.network.SupabaseNetworkDataSource
import com.absolutefresh.sales.data.network.dto.FieldOrderDto
import com.absolutefresh.sales.data.network.dto.FieldOrderItemDto
import com.absolutefresh.sales.data.network.dto.OutletDto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Background WorkManager worker that automatically replays queued offline outbox
 * mutations in FIFO order upon network connectivity restoration against Supabase backend.
 */
class OutboxSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    var networkDataSource: NetworkDataSource = defaultNetworkDataSource

    override suspend fun doWork(): Result {
        val database = AppDatabase.getInstance(applicationContext)
        val outboxDao = database.outboxDao()
        val orderDao = database.orderDao()
        val outletDao = database.outletDao()

        val pendingMutations = outboxDao.getPendingMutations()
        if (pendingMutations.isEmpty()) {
            return Result.success()
        }

        for (mutation in pendingMutations) {
            try {
                outboxDao.markProcessing(mutation.id)

                when (mutation.entityType) {
                    "ORDER" -> {
                        val orderWithItems = orderDao.getOrderById(mutation.entityId)
                        if (orderWithItems != null) {
                            val order = orderWithItems.toDomain()
                            val orderDto = FieldOrderDto(
                                id = null,
                                clientId = order.id,
                                outletId = order.outletId,
                                outletName = order.outletName,
                                repId = order.repId,
                                orderDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date(order.createdAt)),
                                deliveryDate = null,
                                status = "PENDING",
                                notes = null,
                                invoiceNumber = order.provisionalInvoiceNumber,
                                subtotal = order.subtotal,
                                taxAmount = order.gstAmount,
                                totalAmount = order.totalAmount,
                                paymentMode = order.paymentMode.name,
                                paymentStatus = "UNPAID",
                                syncVersion = 1L
                            )

                            val itemsDto = order.items.map { item ->
                                FieldOrderItemDto(
                                    productId = item.productId,
                                    skuName = item.productName,
                                    quantity = item.quantityPackets,
                                    kgOrdered = item.weightKg,
                                    unitPrice = item.unitPrice,
                                    discountAmount = 0.0,
                                    subtotal = item.lineTotal,
                                    taxRate = 0.05
                                )
                            }

                            val pushResult = networkDataSource.pushOrder(orderDto, itemsDto)
                            if (pushResult.isSuccess) {
                                val serverOrder = pushResult.getOrThrow()
                                val serverInvoiceNo = serverOrder.invoiceNumber ?: run {
                                    val dateFormat = SimpleDateFormat("yyMM", Locale.ENGLISH)
                                    val yearMonth = dateFormat.format(Date())
                                    "AFF-$yearMonth-${(100..999).random()}"
                                }

                                orderDao.updateSyncStatus(
                                    orderId = mutation.entityId,
                                    syncStatus = "SYNCED",
                                    syncedInvoiceNumber = serverInvoiceNo
                                )
                                outboxDao.markSynced(mutation.id)
                            } else {
                                throw pushResult.exceptionOrNull() ?: Exception("Failed to push order")
                            }
                        } else {
                            outboxDao.markSynced(mutation.id)
                        }
                    }
                    "OUTLET" -> {
                        val outlet = outletDao.getOutletById(mutation.entityId)
                        if (outlet != null) {
                            val outletDto = OutletDto(
                                id = null,
                                clientId = outlet.id,
                                name = outlet.name,
                                area = outlet.area,
                                owner = outlet.ownerName,
                                phone = outlet.phone,
                                assignedRep = null,
                                averageKgPerDay = outlet.avgKgPerDay,
                                status = "active",
                                priceTier = outlet.priceTier,
                                outletType = outlet.type,
                                gstin = outlet.gstin,
                                address = outlet.address,
                                latitude = outlet.latitude,
                                longitude = outlet.longitude,
                                syncVersion = 1L
                            )

                            val pushResult = networkDataSource.pushOutlet(outletDto)
                            if (pushResult.isSuccess) {
                                outletDao.updateOutlet(outlet.copy(syncStatus = "SYNCED"))
                                outboxDao.markSynced(mutation.id)
                            } else {
                                throw pushResult.exceptionOrNull() ?: Exception("Failed to push outlet")
                            }
                        } else {
                            outboxDao.markSynced(mutation.id)
                        }
                    }
                    else -> {
                        outboxDao.markSynced(mutation.id)
                    }
                }
            } catch (e: Exception) {
                val nextRetry = mutation.retryCount + 1
                outboxDao.markFailed(
                    id = mutation.id,
                    errorMessage = e.message ?: "Sync failure",
                    retryCount = nextRetry
                )
                if (nextRetry >= 5) {
                    // Maximum retry reached
                } else {
                    return Result.retry()
                }
            }
        }

        return Result.success()
    }

    companion object {
        private const val SYNC_WORK_NAME = "absolute_fresh_outbox_sync"

        @Volatile
        var defaultNetworkDataSource: NetworkDataSource = SupabaseNetworkDataSource()

        /**
         * Enqueues automatic background sync constrained to active network connectivity.
         */
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<OutboxSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                SYNC_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                syncRequest
            )
        }
    }
}
