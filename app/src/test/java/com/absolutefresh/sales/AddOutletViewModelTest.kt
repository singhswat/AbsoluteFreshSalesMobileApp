package com.absolutefresh.sales

import com.absolutefresh.sales.data.model.OutletType
import com.absolutefresh.sales.data.model.SyncStatus
import com.absolutefresh.sales.data.repository.FakeOutletRepository
import com.absolutefresh.sales.ui.outlets.add.AddOutletViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddOutletViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeOutletRepository
    private lateinit var viewModel: AddOutletViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeOutletRepository()
        viewModel = AddOutletViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty fields and is invalid with default tier 4`() {
        val state = viewModel.uiState.value
        assertEquals("", state.name)
        assertEquals("", state.phone)
        assertEquals("", state.area)
        assertEquals(4, state.priceTier)
        assertEquals(OutletType.GENERAL_TRADE, state.type)
        assertEquals("10.0", state.avgKgPerDay)
        assertEquals("10000", state.creditLimit)
        assertEquals(15, state.paymentTermsDays)
        assertFalse(state.isGpsCaptured)
        assertFalse(state.isValid)
        assertFalse(state.isSuccess)
    }

    @Test
    fun `name validation requires non blank and at least 2 characters`() {
        viewModel.updateName("")
        assertNotNull(viewModel.uiState.value.nameError)
        assertFalse(viewModel.uiState.value.isValid)

        viewModel.updateName("A")
        assertNotNull(viewModel.uiState.value.nameError)
        assertFalse(viewModel.uiState.value.isValid)

        viewModel.updateName("Verma Kirana")
        assertNull(viewModel.uiState.value.nameError)
    }

    @Test
    fun `phone validation requires at least 10 digits and strips non-digits`() {
        viewModel.updatePhone("12345")
        assertNotNull(viewModel.uiState.value.phoneError)

        viewModel.updatePhone("+91-98765-43210")
        assertEquals("919876543210", viewModel.uiState.value.phone)
        assertNull(viewModel.uiState.value.phoneError)

        viewModel.updatePhone("9876543210")
        assertEquals("9876543210", viewModel.uiState.value.phone)
        assertNull(viewModel.uiState.value.phoneError)
    }

    @Test
    fun `area validation enforces required non-blank`() {
        viewModel.updateArea("")
        assertNotNull(viewModel.uiState.value.areaError)

        viewModel.updateArea("Indira Nagar")
        assertNull(viewModel.uiState.value.areaError)
    }

    @Test
    fun `optional GSTIN validation allows blank but rejects invalid format`() {
        viewModel.updateGstin("")
        assertNull(viewModel.uiState.value.gstinError)

        viewModel.updateGstin("INVALID123")
        assertNotNull(viewModel.uiState.value.gstinError)
        assertFalse(viewModel.uiState.value.isValid)
    }

    @Test
    fun `valid GSTIN format passes validation and converts to uppercase`() {
        viewModel.updateGstin("09abcde1234f1z5")
        assertEquals("09ABCDE1234F1Z5", viewModel.uiState.value.gstin)
        assertNull(viewModel.uiState.value.gstinError)
    }

    @Test
    fun `price tier and channel type update correctly`() {
        viewModel.updatePriceTier(2)
        assertEquals(2, viewModel.uiState.value.priceTier)

        viewModel.updatePriceTier(99)
        assertEquals(4, viewModel.uiState.value.priceTier) // Coerced to 4

        viewModel.updateType(OutletType.CHAIN_STORE)
        assertEquals(OutletType.CHAIN_STORE, viewModel.uiState.value.type)
    }

    @Test
    fun `GPS coordinates capture and clear correctly`() {
        viewModel.captureGpsLocation(26.8467, 80.9462)
        assertTrue(viewModel.uiState.value.isGpsCaptured)
        assertEquals(26.8467, viewModel.uiState.value.latitude)
        assertEquals(80.9462, viewModel.uiState.value.longitude)

        viewModel.clearGpsLocation()
        assertFalse(viewModel.uiState.value.isGpsCaptured)
        assertNull(viewModel.uiState.value.latitude)
        assertNull(viewModel.uiState.value.longitude)
    }

    @Test
    fun `duplicate detection triggers warning when existing outlet name or phone matches`() = runTest {
        viewModel.updateName("Sharma Kirana")
        viewModel.updatePhone("9876543201")
        viewModel.updateArea("Indira Nagar")

        assertTrue(viewModel.uiState.value.isValid)

        viewModel.submitOutlet()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected duplicate warnings to be populated", state.potentialDuplicates.isNotEmpty())
        assertFalse(state.isSuccess)
    }

    @Test
    fun `confirm add anyway bypasses duplicate warning and saves outlet`() = runTest {
        viewModel.updateName("Sharma Kirana")
        viewModel.updatePhone("9876543201")
        viewModel.updateArea("Indira Nagar")

        viewModel.submitOutlet()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.potentialDuplicates.isNotEmpty())
        assertFalse(viewModel.uiState.value.isSuccess)

        // User overrides and confirms add anyway
        viewModel.confirmAddAnyway()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun `submit valid new outlet persists to repository with client UUID and PENDING status`() = runTest {
        viewModel.updateName("Awadh Organic Mart")
        viewModel.updateOwner("Deepak Mishra")
        viewModel.updatePhone("9876500000")
        viewModel.updateArea("Vikas Nagar")
        viewModel.updateAddress("Shop 14, Ring Road")
        viewModel.updateType(OutletType.STANDALONE_SHOP)
        viewModel.updatePriceTier(3)
        viewModel.updateAvgKgPerDay("25.0")
        viewModel.updateCreditLimit("15000")
        viewModel.updatePaymentTermsDays(7)
        viewModel.captureGpsLocation(26.85, 80.95)

        assertTrue(viewModel.uiState.value.isValid)

        viewModel.submitOutlet()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isSuccess)

        val allOutlets = fakeRepository.getOutlets()
        val created = allOutlets.find { it.name == "Awadh Organic Mart" }
        assertNotNull("Outlet should be saved in repository", created)
        assertEquals("Deepak Mishra", created?.ownerName)
        assertEquals("9876500000", created?.phone)
        assertEquals("Vikas Nagar", created?.area)
        assertEquals(OutletType.STANDALONE_SHOP, created?.type)
        assertEquals(3, created?.priceTier)
        assertEquals(25.0, created?.avgKgPerDay)
        assertEquals(15000.0, created?.creditLimit)
        assertEquals(7, created?.paymentTermsDays)
        assertEquals(26.85, created?.latitude)
        assertEquals(80.95, created?.longitude)
        assertEquals(SyncStatus.PENDING, created?.syncStatus)
        assertTrue(created?.id?.startsWith("out-") == true)
    }
}
