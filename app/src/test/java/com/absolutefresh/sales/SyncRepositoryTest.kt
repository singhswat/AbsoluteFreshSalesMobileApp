package com.absolutefresh.sales

import com.absolutefresh.sales.data.local.dao.BeatDao
import com.absolutefresh.sales.data.local.dao.OutboxDao
import com.absolutefresh.sales.data.local.dao.OutletDao
import com.absolutefresh.sales.data.local.entity.BeatVisitEntity
import com.absolutefresh.sales.data.local.entity.OutboxEntity
import com.absolutefresh.sales.data.local.entity.OutletEntity
import com.absolutefresh.sales.data.network.NetworkDataSource
import com.absolutefresh.sales.data.network.dto.BeatVisitDto
import com.absolutefresh.sales.data.network.dto.FieldOrderDto
import com.absolutefresh.sales.data.network.dto.FieldOrderItemDto
import com.absolutefresh.sales.data.network.dto.HolidayDto
import com.absolutefresh.sales.data.network.dto.OutletDto
import com.absolutefresh.sales.data.network.dto.ProductDto
import com.absolutefresh.sales.data.sync.SyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class FakeTestNetworkDataSource : NetworkDataSource {
    val outletsList = mutableListOf(
        OutletDto(
            id = "srv-out-1",
            clientId = "c-1",
            name = "Lucknow Supermart",
            area = "Hazratganj",
            phone = "9876543200",
            priceTier = 2,
            outletType = "CHAIN_STORE"
        ),
        OutletDto(
            id = "srv-out-2",
            clientId = "c-2",
            name = "Verma General Store",
            area = "Aliganj",
            phone = "9876543201",
            priceTier = 4,
            outletType = "GENERAL_TRADE"
        )
    )

    val beatVisitsList = mutableListOf(
        BeatVisitDto(
            id = "srv-bv-1",
            rep = "rep-1",
            date = "2026-10-14",
            outletId = "srv-out-1",
            outletName = "Lucknow Supermart",
            area = "Hazratganj",
            sequence = 1,
            targetKg = 25.0,
            status = "PLANNED"
        )
    )

    override suspend fun getBeats(repId: String, date: String): Result<List<BeatVisitDto>> {
        return Result.success(beatVisitsList.filter { it.rep == repId && it.date == date })
    }

    override suspend fun getOutlets(repId: String?): Result<List<OutletDto>> {
        return Result.success(outletsList)
    }

    override suspend fun getProducts(): Result<List<ProductDto>> {
        return Result.success(emptyList())
    }

    override suspend fun getHolidays(): Result<List<HolidayDto>> {
        return Result.success(emptyList())
    }

    override suspend fun pushOrder(order: FieldOrderDto, items: List<FieldOrderItemDto>): Result<FieldOrderDto> {
        return Result.success(order.copy(id = "srv-order-created", invoiceNumber = "AFF-2610-777"))
    }

    override suspend fun pushOutlet(outlet: OutletDto): Result<OutletDto> {
        return Result.success(outlet.copy(id = "srv-out-created"))
    }

    override suspend fun pushBeatVisit(beatVisit: BeatVisitDto): Result<BeatVisitDto> {
        return Result.success(beatVisit.copy(id = "srv-bv-created"))
    }
}

class FakeTestOutletDao : OutletDao {
    val insertedOutlets = mutableListOf<OutletEntity>()
    private val flow = MutableStateFlow<List<OutletEntity>>(emptyList())

    override fun getAllOutlets(): Flow<List<OutletEntity>> = flow.asStateFlow()

    override suspend fun getOutletById(id: String): OutletEntity? =
        insertedOutlets.find { it.id == id }

    override fun getOutletsByArea(area: String): Flow<List<OutletEntity>> = flow.asStateFlow()
    override fun searchOutlets(query: String): Flow<List<OutletEntity>> = flow.asStateFlow()
    override suspend fun findPossibleDuplicates(phone: String, name: String): List<OutletEntity> =
        insertedOutlets.filter { it.phone == phone || it.name.equals(name, ignoreCase = true) }

    override suspend fun insertOutlet(outlet: OutletEntity) {
        insertedOutlets.add(outlet)
        flow.value = insertedOutlets.toList()
    }

    override suspend fun insertOutlets(outlets: List<OutletEntity>) {
        insertedOutlets.addAll(outlets)
        flow.value = insertedOutlets.toList()
    }

    override suspend fun updateOutlet(outlet: OutletEntity) {
        val idx = insertedOutlets.indexOfFirst { it.id == outlet.id }
        if (idx >= 0) insertedOutlets[idx] = outlet
        flow.value = insertedOutlets.toList()
    }

    override suspend fun deleteOutlet(outlet: OutletEntity) {
        insertedOutlets.removeAll { it.id == outlet.id }
        flow.value = insertedOutlets.toList()
    }

    override suspend fun getOutletCount(): Int = insertedOutlets.size
}

class FakeTestBeatDao : BeatDao {
    val visits = mutableListOf<BeatVisitEntity>()
    private val visitsFlow = MutableStateFlow<List<BeatVisitEntity>>(emptyList())

    override fun getVisitsForDate(date: String, repId: String): Flow<List<BeatVisitEntity>> {
        return visitsFlow.asStateFlow()
    }

    override suspend fun insertVisits(newVisits: List<BeatVisitEntity>) {
        visits.removeAll { existing -> newVisits.any { it.id == existing.id } }
        visits.addAll(newVisits)
        visitsFlow.value = visits.toList()
    }

    override suspend fun updateVisit(visit: BeatVisitEntity) {
        val idx = visits.indexOfFirst { it.id == visit.id }
        if (idx >= 0) visits[idx] = visit
        visitsFlow.value = visits.toList()
    }

    override suspend fun updateVisitState(visitId: String, visitState: String, timestamp: Long?, orderSummary: String?) {
        val idx = visits.indexOfFirst { it.id == visitId }
        if (idx >= 0) {
            visits[idx] = visits[idx].copy(visitState = visitState, lastVisitedTimestamp = timestamp, completedOrderSummary = orderSummary)
            visitsFlow.value = visits.toList()
        }
    }

    override suspend fun markVisitMissed(visitId: String, reason: String) {
        val idx = visits.indexOfFirst { it.id == visitId }
        if (idx >= 0) {
            visits[idx] = visits[idx].copy(visitState = "MISSED", missedReason = reason)
            visitsFlow.value = visits.toList()
        }
    }

    override suspend fun getVisitsCountForDate(date: String, repId: String): Int {
        return visits.count { it.date == date && it.repId == repId }
    }
}

class FakeTestOutboxDao : OutboxDao {
    val enqueuedMutations = mutableListOf<OutboxEntity>()
    private var idCounter = 1L
    private val countFlow = MutableStateFlow(0)

    override suspend fun enqueue(mutation: OutboxEntity): Long {
        val assigned = mutation.copy(id = idCounter++)
        enqueuedMutations.add(assigned)
        countFlow.value = enqueuedMutations.count { it.status in listOf("QUEUED", "PROCESSING") }
        return assigned.id
    }

    override suspend fun getPendingMutations(): List<OutboxEntity> {
        return enqueuedMutations.filter { it.status in listOf("QUEUED", "FAILED") && it.retryCount < 5 }
    }

    override suspend fun markProcessing(id: Long, timestamp: Long) {
        val idx = enqueuedMutations.indexOfFirst { it.id == id }
        if (idx >= 0) enqueuedMutations[idx] = enqueuedMutations[idx].copy(status = "PROCESSING", lastAttemptAt = timestamp)
    }

    override suspend fun markSynced(id: Long, timestamp: Long) {
        val idx = enqueuedMutations.indexOfFirst { it.id == id }
        if (idx >= 0) enqueuedMutations[idx] = enqueuedMutations[idx].copy(status = "SYNCED", lastAttemptAt = timestamp)
        countFlow.value = enqueuedMutations.count { it.status in listOf("QUEUED", "PROCESSING") }
    }

    override suspend fun markFailed(id: Long, errorMessage: String, retryCount: Int, timestamp: Long) {
        val idx = enqueuedMutations.indexOfFirst { it.id == id }
        if (idx >= 0) enqueuedMutations[idx] = enqueuedMutations[idx].copy(status = "FAILED", errorMessage = errorMessage, retryCount = retryCount, lastAttemptAt = timestamp)
    }

    override fun getPendingCountFlow(): Flow<Int> = countFlow.asStateFlow()
    override suspend fun getPendingCount(): Int = countFlow.value
    override suspend fun pruneSynced(beforeTimestamp: Long) {
        enqueuedMutations.removeAll { it.status == "SYNCED" && it.createdAt < beforeTimestamp }
    }
}

class SyncRepositoryTest {

    private lateinit var fakeOutletDao: FakeTestOutletDao
    private lateinit var fakeBeatDao: FakeTestBeatDao
    private lateinit var fakeOutboxDao: FakeTestOutboxDao
    private lateinit var fakeNetwork: FakeTestNetworkDataSource
    private lateinit var syncRepository: SyncRepository

    @Before
    fun setup() {
        fakeOutletDao = FakeTestOutletDao()
        fakeBeatDao = FakeTestBeatDao()
        fakeOutboxDao = FakeTestOutboxDao()
        fakeNetwork = FakeTestNetworkDataSource()

        syncRepository = SyncRepository(
            outletDao = fakeOutletDao,
            beatDao = fakeBeatDao,
            outboxDao = fakeOutboxDao,
            networkDataSource = fakeNetwork
        )
    }

    @Test
    fun refreshMasterData_populatesOutletsAndBeatsIntoRoom() = runTest {
        val result = syncRepository.refreshMasterData("rep-1", "2026-10-14")
        assertEquals(true, result.isSuccess)

        val outlets = fakeOutletDao.getAllOutlets().first()
        assertEquals(2, outlets.size)
        assertEquals("Lucknow Supermart", outlets[0].name)
        assertEquals("SYNCED", outlets[0].syncStatus)

        val beatCount = fakeBeatDao.getVisitsCountForDate("2026-10-14", "rep-1")
        assertEquals(1, beatCount)

        val visits = fakeBeatDao.getVisitsForDate("2026-10-14", "rep-1").first()
        assertEquals("srv-out-1", visits[0].outletId)
        assertEquals(25.0, visits[0].targetKg, 0.001)
    }

    @Test
    fun localPendingOutlet_isPreservedWhenNetworkRefreshes() = runTest {
        // 1. Rep adds an outlet offline
        fakeOutletDao.insertOutlet(
            OutletEntity(
                id = "local-uuid-1",
                name = "New Offline Stall",
                ownerName = "Suresh",
                phone = "9811223344",
                address = "Gomti Nagar Gate",
                area = "Gomti Nagar",
                type = "GENERAL_TRADE",
                priceTier = 4,
                gstin = null,
                outstandingBalance = 0.0,
                creditLimit = 10000.0,
                paymentTermsDays = 7,
                avgKgPerDay = 10.0,
                latitude = 26.85,
                longitude = 80.95,
                syncStatus = "PENDING"
            )
        )

        // 2. Network refresh occurs
        syncRepository.refreshMasterData("rep-1", "2026-10-14")

        // 3. Both network outlets and the local pending outlet are present
        val allOutlets = fakeOutletDao.getAllOutlets().first()
        assertEquals(3, allOutlets.size)

        val localPending = fakeOutletDao.getOutletById("local-uuid-1")
        assertNotNull(localPending)
        assertEquals("PENDING", localPending?.syncStatus)
    }
}
