package com.absolutefresh.sales.data.network

import com.absolutefresh.sales.data.network.dto.BeatVisitDto
import com.absolutefresh.sales.data.network.dto.FieldOrderDto
import com.absolutefresh.sales.data.network.dto.FieldOrderItemDto
import com.absolutefresh.sales.data.network.dto.HolidayDto
import com.absolutefresh.sales.data.network.dto.OutletDto
import com.absolutefresh.sales.data.network.dto.ProductDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Interface contract for remote backend operations.
 */
interface NetworkDataSource {
    suspend fun getBeats(repId: String, date: String): Result<List<BeatVisitDto>>
    suspend fun getOutlets(repId: String? = null): Result<List<OutletDto>>
    suspend fun getProducts(): Result<List<ProductDto>>
    suspend fun getHolidays(): Result<List<HolidayDto>>
    suspend fun pushOrder(order: FieldOrderDto, items: List<FieldOrderItemDto>): Result<FieldOrderDto>
    suspend fun pushOutlet(outlet: OutletDto): Result<OutletDto>
    suspend fun pushBeatVisit(beatVisit: BeatVisitDto): Result<BeatVisitDto>
}

/**
 * Concrete implementation using the official Supabase Kotlin SDK.
 * Respects ADR-001 idempotency client_id side-column and optimistic concurrency sync_version.
 * Includes a read-only safety lock when inspecting live production data.
 */
class SupabaseNetworkDataSource(
    private val client: SupabaseClient = SupabaseClientProvider.getClient(),
    var isReadOnlyMode: Boolean = true // Default to true to protect live production data from accidental writes
) : NetworkDataSource {

    override suspend fun getBeats(repId: String, date: String): Result<List<BeatVisitDto>> = withContext(Dispatchers.IO) {
        runCatching {
            client.postgrest["daily_beat_logs"].select {
                filter {
                    eq("rep", repId)
                    eq("date", date)
                }
            }.decodeList<BeatVisitDto>()
        }
    }

    override suspend fun getOutlets(repId: String?): Result<List<OutletDto>> = withContext(Dispatchers.IO) {
        runCatching {
            client.postgrest["outlets"].select {
                if (!repId.isNullOrBlank()) {
                    filter {
                        eq("assigned_rep", repId)
                    }
                }
            }.decodeList<OutletDto>()
        }
    }

    override suspend fun getProducts(): Result<List<ProductDto>> = withContext(Dispatchers.IO) {
        runCatching {
            client.postgrest["products"].select {
                filter {
                    eq("active", true)
                }
            }.decodeList<ProductDto>()
        }
    }

    override suspend fun getHolidays(): Result<List<HolidayDto>> = withContext(Dispatchers.IO) {
        runCatching {
            client.postgrest["holidays"].select().decodeList<HolidayDto>()
        }
    }

    override suspend fun pushOrder(order: FieldOrderDto, items: List<FieldOrderItemDto>): Result<FieldOrderDto> = withContext(Dispatchers.IO) {
        if (isReadOnlyMode) {
            // Read-only safety guard: do not write to live production environment
            val ym = SimpleDateFormat("yyMM", Locale.ENGLISH).format(Date())
            return@withContext Result.success(
                order.copy(
                    id = "sim-${order.clientId.take(8)}",
                    invoiceNumber = "AFF-$ym-${(100..999).random()}"
                )
            )
        }

        runCatching {
            // 1. Insert order with client_id idempotency key
            val insertedOrder = client.postgrest["orders"].insert(order) {
                select()
            }.decodeSingle<FieldOrderDto>()

            // 2. Insert item lines linked to the order id
            val orderId = insertedOrder.id ?: insertedOrder.clientId
            val itemsWithOrderId = items.map { it.copy(orderId = orderId) }
            if (itemsWithOrderId.isNotEmpty()) {
                client.postgrest["order_items"].insert(itemsWithOrderId)
            }

            insertedOrder
        }
    }

    override suspend fun pushOutlet(outlet: OutletDto): Result<OutletDto> = withContext(Dispatchers.IO) {
        if (isReadOnlyMode) {
            return@withContext Result.success(outlet.copy(id = "sim-${outlet.clientId?.take(8) ?: "out"}"))
        }

        runCatching {
            client.postgrest["outlets"].upsert(outlet) {
                select()
            }.decodeSingle<OutletDto>()
        }
    }

    override suspend fun pushBeatVisit(beatVisit: BeatVisitDto): Result<BeatVisitDto> = withContext(Dispatchers.IO) {
        if (isReadOnlyMode) {
            return@withContext Result.success(beatVisit.copy(id = "sim-bv"))
        }

        runCatching {
            client.postgrest["daily_beat_logs"].upsert(beatVisit) {
                select()
            }.decodeSingle<BeatVisitDto>()
        }
    }
}
