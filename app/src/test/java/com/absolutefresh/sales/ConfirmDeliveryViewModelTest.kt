package com.absolutefresh.sales

import androidx.compose.ui.geometry.Offset
import com.absolutefresh.sales.data.model.OrderDeliveryStatus
import com.absolutefresh.sales.data.model.PaymentMode
import com.absolutefresh.sales.data.repository.FakeDeliveryRepository
import com.absolutefresh.sales.ui.driver.confirm.ConfirmDeliveryUiState
import com.absolutefresh.sales.ui.driver.confirm.ConfirmDeliveryViewModel
import com.absolutefresh.sales.ui.driver.confirm.PodMethod
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
class ConfirmDeliveryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeDeliveryRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeDeliveryRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads delivery correctly with full default allocation`() = runTest(testDispatcher) {
        val viewModel = ConfirmDeliveryViewModel("del-03", repository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as ConfirmDeliveryUiState.Ready
        assertEquals("Krishna Sweets", state.delivery.outletName)
        assertEquals(25, state.deliveredPackets)
        assertEquals(25, state.delivery.orderedPackets)
        assertFalse(state.isShortDelivery)
        assertEquals(100, state.fillPercentage)
        assertEquals(2152.5, state.netPayable, 0.01)
        assertTrue(state.isShortfallReasonValid)
    }

    @Test
    fun `adjusting delivered packets updates net payable and fill percentage`() = runTest(testDispatcher) {
        val viewModel = ConfirmDeliveryViewModel("del-03", repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setPackets(20)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as ConfirmDeliveryUiState.Ready
        assertEquals(20, state.deliveredPackets)
        assertTrue(state.isShortDelivery)
        assertEquals(80, state.fillPercentage)
        assertEquals(1722.0, state.netPayable, 0.01)
        assertEquals(0.0, state.amountCollected, 0.01) // del-03 defaults to CREDIT_15D

        // Switching to CASH updates amountCollected to netPayable
        viewModel.setPaymentMode(PaymentMode.CASH)
        testDispatcher.scheduler.advanceUntilIdle()
        val cashState = viewModel.uiState.value as ConfirmDeliveryUiState.Ready
        assertEquals(1722.0, cashState.amountCollected, 0.01)
    }

    @Test
    fun `short delivery requires non-blank shortfall reason to be valid`() = runTest(testDispatcher) {
        val viewModel = ConfirmDeliveryViewModel("del-03", repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setPackets(15)
        testDispatcher.scheduler.advanceUntilIdle()

        var state = viewModel.uiState.value as ConfirmDeliveryUiState.Ready
        assertTrue(state.isShortDelivery)
        assertFalse(state.isShortfallReasonValid)
        assertFalse(state.isValid)

        viewModel.setShortfallReason("Retailer Chiller Full (फ्रिज में जगह नहीं)")
        testDispatcher.scheduler.advanceUntilIdle()

        state = viewModel.uiState.value as ConfirmDeliveryUiState.Ready
        assertTrue(state.isShortfallReasonValid)
        assertTrue(state.isValid)
    }

    @Test
    fun `payment mode updates adjust collected amount and validate cheque number`() = runTest(testDispatcher) {
        val viewModel = ConfirmDeliveryViewModel("del-03", repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Switch to Credit 15D -> amount collected becomes 0.0
        viewModel.setPaymentMode(PaymentMode.CREDIT_15D)
        testDispatcher.scheduler.advanceUntilIdle()

        var state = viewModel.uiState.value as ConfirmDeliveryUiState.Ready
        assertEquals(PaymentMode.CREDIT_15D, state.paymentMode)
        assertEquals(0.0, state.amountCollected, 0.01)
        assertTrue(state.isPaymentValid)

        // Switch to Cheque -> requires valid 6 digit number & bank
        viewModel.setPaymentMode(PaymentMode.CHEQUE)
        testDispatcher.scheduler.advanceUntilIdle()

        state = viewModel.uiState.value as ConfirmDeliveryUiState.Ready
        assertFalse(state.isPaymentValid)

        viewModel.setChequeDetails("123", "HDFC")
        testDispatcher.scheduler.advanceUntilIdle()
        state = viewModel.uiState.value as ConfirmDeliveryUiState.Ready
        assertFalse(state.isPaymentValid) // less than 6 digits

        viewModel.setChequeDetails("123456", "HDFC Bank")
        testDispatcher.scheduler.advanceUntilIdle()
        state = viewModel.uiState.value as ConfirmDeliveryUiState.Ready
        assertTrue(state.isPaymentValid)
        assertEquals("123456", state.chequeNumber)
        assertEquals("HDFC Bank", state.chequeBank)
    }

    @Test
    fun `proof of delivery verifies signature, customer OTP, and photo capture`() = runTest(testDispatcher) {
        val viewModel = ConfirmDeliveryViewModel("del-03", repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // 1. Signature POD
        viewModel.setPodMethod(PodMethod.SIGNATURE)
        viewModel.setReceiverName("Mr. Sharma")
        viewModel.addSignatureStroke(listOf(Offset(10f, 10f), Offset(20f, 20f)))
        testDispatcher.scheduler.advanceUntilIdle()

        var state = viewModel.uiState.value as ConfirmDeliveryUiState.Ready
        assertTrue(state.isPodComplete)
        assertEquals(1, state.signatureStrokes.size)

        // Clear signature
        viewModel.clearSignature()
        viewModel.setReceiverName("")
        testDispatcher.scheduler.advanceUntilIdle()
        state = viewModel.uiState.value as ConfirmDeliveryUiState.Ready
        assertFalse(state.isPodComplete)

        // 2. Customer OTP POD
        viewModel.setPodMethod(PodMethod.OTP)
        viewModel.setOtpCode("4821")
        testDispatcher.scheduler.advanceUntilIdle()
        state = viewModel.uiState.value as ConfirmDeliveryUiState.Ready
        assertTrue(state.isPodComplete)
        assertTrue(state.isOtpVerified)

        // 3. Photo Proof POD
        viewModel.setPodMethod(PodMethod.PHOTO)
        testDispatcher.scheduler.advanceUntilIdle()
        state = viewModel.uiState.value as ConfirmDeliveryUiState.Ready
        assertFalse(state.isPodComplete)

        viewModel.capturePhoto()
        testDispatcher.scheduler.advanceUntilIdle()
        state = viewModel.uiState.value as ConfirmDeliveryUiState.Ready
        assertTrue(state.isPodComplete)
        assertTrue(state.isPhotoCaptured)
        assertNotNull(state.photoTimestamp)
    }

    @Test
    fun `submitDelivery updates repository and triggers success callback`() = runTest(testDispatcher) {
        val viewModel = ConfirmDeliveryViewModel("del-03", repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setPackets(25)
        viewModel.setAmountCollected(2152.5)
        viewModel.setPaymentMode(PaymentMode.CASH)
        viewModel.setPodMethod(PodMethod.SIGNATURE)
        viewModel.setReceiverName("Krishna Gupta")
        viewModel.addSignatureStroke(listOf(Offset(1f, 1f), Offset(5f, 5f)))

        var callbackCalled = false
        viewModel.submitDelivery {
            callbackCalled = true
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(callbackCalled)
        val state = viewModel.uiState.value as ConfirmDeliveryUiState.Ready
        assertTrue(state.isSubmitted)
        assertNotNull(state.snackbarMessage)
        assertTrue(state.snackbarMessage!!.contains("Krishna Sweets"))

        // Check repository was updated
        val updatedItem = repository.getDeliveryById("del-03")
        assertNotNull(updatedItem)
        assertEquals(OrderDeliveryStatus.DELIVERED, updatedItem!!.deliveryStatus)
        assertEquals(25, updatedItem.deliveredPackets)
        assertEquals(2152.5, updatedItem.amountCollected, 0.01)
        assertTrue(updatedItem.podMethod.contains("Signed by Krishna Gupta"))
    }

    @Test
    fun `short delivery submission includes shortfall reason in pod summary`() = runTest(testDispatcher) {
        val viewModel = ConfirmDeliveryViewModel("del-03", repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setPackets(18)
        viewModel.setShortfallReason("Retailer Chiller Full (फ्रिज में जगह नहीं)")
        viewModel.setPodMethod(PodMethod.OTP)
        viewModel.setOtpCode("4821")

        var callbackCalled = false
        viewModel.submitDelivery {
            callbackCalled = true
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(callbackCalled)
        val updatedItem = repository.getDeliveryById("del-03")
        assertNotNull(updatedItem)
        assertEquals(18, updatedItem!!.deliveredPackets)
        assertTrue(updatedItem.podMethod.contains("Shortfall: Retailer Chiller Full"))
    }
}
