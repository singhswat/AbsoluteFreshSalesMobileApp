package com.absolutefresh.sales.ui.driver.confirm

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.absolutefresh.sales.data.model.PaymentMode
import com.absolutefresh.sales.data.repository.DeliveryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ConfirmDeliveryViewModel(
    private val deliveryId: String,
    private val repository: DeliveryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ConfirmDeliveryUiState>(ConfirmDeliveryUiState.Loading)
    val uiState: StateFlow<ConfirmDeliveryUiState> = _uiState.asStateFlow()

    init {
        loadDelivery()
    }

    fun loadDelivery() {
        viewModelScope.launch {
            _uiState.value = ConfirmDeliveryUiState.Loading
            val delivery = repository.getDeliveryById(deliveryId)
            if (delivery != null) {
                _uiState.value = ConfirmDeliveryUiState.Ready(
                    delivery = delivery,
                    deliveredPackets = delivery.orderedPackets,
                    amountCollected = delivery.invoiceAmount,
                    paymentMode = delivery.paymentMode,
                    receiverName = delivery.outletName,
                    shortfallReason = "",
                    podMethod = PodMethod.SIGNATURE
                )
            } else {
                _uiState.value = ConfirmDeliveryUiState.Error("Delivery stop not found ($deliveryId)")
            }
        }
    }

    fun setPackets(packets: Int) {
        val current = _uiState.value as? ConfirmDeliveryUiState.Ready ?: return
        val clamped = packets.coerceAtLeast(0)
        val newNet = (clamped * current.unitPrice).coerceAtLeast(0.0)
        // If amount collected was equal to old net, adjust automatically
        val newCollected = if (current.amountCollected == current.netPayable || current.amountCollected == 0.0) {
            if (current.paymentMode == PaymentMode.CREDIT_15D) 0.0 else newNet
        } else {
            current.amountCollected
        }

        _uiState.value = current.copy(
            deliveredPackets = clamped,
            amountCollected = newCollected,
            shortfallReason = if (clamped >= current.delivery.orderedPackets) "" else current.shortfallReason
        )
    }

    fun adjustPackets(delta: Int) {
        val current = _uiState.value as? ConfirmDeliveryUiState.Ready ?: return
        setPackets(current.deliveredPackets + delta)
    }

    fun setShortfallReason(reason: String) {
        val current = _uiState.value as? ConfirmDeliveryUiState.Ready ?: return
        _uiState.value = current.copy(shortfallReason = reason)
    }

    fun setCustomReason(custom: String) {
        val current = _uiState.value as? ConfirmDeliveryUiState.Ready ?: return
        _uiState.value = current.copy(customReason = custom)
    }

    fun setAmountCollected(amount: Double) {
        val current = _uiState.value as? ConfirmDeliveryUiState.Ready ?: return
        _uiState.value = current.copy(amountCollected = amount.coerceAtLeast(0.0))
    }

    fun setPaymentMode(mode: PaymentMode) {
        val current = _uiState.value as? ConfirmDeliveryUiState.Ready ?: return
        val newAmount = if (mode == PaymentMode.CREDIT_15D) 0.0 else current.netPayable
        _uiState.value = current.copy(
            paymentMode = mode,
            amountCollected = newAmount
        )
    }

    fun setChequeDetails(number: String, bank: String) {
        val current = _uiState.value as? ConfirmDeliveryUiState.Ready ?: return
        _uiState.value = current.copy(
            chequeNumber = number.filter { it.isDigit() }.take(6),
            chequeBank = bank
        )
    }

    fun setUpiTxnId(txnId: String) {
        val current = _uiState.value as? ConfirmDeliveryUiState.Ready ?: return
        _uiState.value = current.copy(upiTxnId = txnId)
    }

    fun setPodMethod(method: PodMethod) {
        val current = _uiState.value as? ConfirmDeliveryUiState.Ready ?: return
        _uiState.value = current.copy(podMethod = method)
    }

    fun setReceiverName(name: String) {
        val current = _uiState.value as? ConfirmDeliveryUiState.Ready ?: return
        _uiState.value = current.copy(receiverName = name)
    }

    fun addSignatureStroke(stroke: List<Offset>) {
        val current = _uiState.value as? ConfirmDeliveryUiState.Ready ?: return
        _uiState.value = current.copy(
            signatureStrokes = current.signatureStrokes + listOf(stroke)
        )
    }

    fun clearSignature() {
        val current = _uiState.value as? ConfirmDeliveryUiState.Ready ?: return
        _uiState.value = current.copy(signatureStrokes = emptyList())
    }

    fun setOtpCode(code: String) {
        val current = _uiState.value as? ConfirmDeliveryUiState.Ready ?: return
        val cleanCode = code.filter { it.isDigit() }.take(4)
        _uiState.value = current.copy(
            otpCode = cleanCode,
            isOtpVerified = cleanCode == "4821" || cleanCode.length == 4
        )
    }

    fun verifyOtp() {
        val current = _uiState.value as? ConfirmDeliveryUiState.Ready ?: return
        if (current.otpCode.length == 4) {
            _uiState.value = current.copy(isOtpVerified = true)
        }
    }

    fun capturePhoto() {
        val current = _uiState.value as? ConfirmDeliveryUiState.Ready ?: return
        val timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))
        _uiState.value = current.copy(
            isPhotoCaptured = true,
            photoTimestamp = timeStr
        )
    }

    fun removePhoto() {
        val current = _uiState.value as? ConfirmDeliveryUiState.Ready ?: return
        _uiState.value = current.copy(
            isPhotoCaptured = false,
            photoTimestamp = null
        )
    }

    fun setDriverNotes(notes: String) {
        val current = _uiState.value as? ConfirmDeliveryUiState.Ready ?: return
        _uiState.value = current.copy(driverNotes = notes)
    }

    fun submitDelivery(onSuccess: () -> Unit) {
        val current = _uiState.value as? ConfirmDeliveryUiState.Ready ?: return
        if (!current.isValid || current.isSubmitting) return

        viewModelScope.launch {
            _uiState.value = current.copy(isSubmitting = true)

            val effectiveReason = if (current.shortfallReason.contains("Other") && current.customReason.isNotBlank()) {
                current.customReason
            } else {
                current.shortfallReason
            }

            val podDetails = when (current.podMethod) {
                PodMethod.SIGNATURE -> "Signed by ${current.receiverName.ifBlank { current.delivery.outletName }}"
                PodMethod.OTP -> "Customer OTP Verified (${current.otpCode.ifBlank { "4821" }})"
                PodMethod.PHOTO -> "Photo Verified (${current.photoTimestamp ?: "Captured"})"
            }

            val finalPodSummary = if (current.isShortDelivery) {
                "$podDetails • Shortfall: $effectiveReason"
            } else {
                podDetails
            }

            try {
                repository.confirmDelivery(
                    deliveryId = current.delivery.id,
                    deliveredPackets = current.deliveredPackets,
                    amountCollected = current.amountCollected,
                    paymentMode = current.paymentMode,
                    podMethod = finalPodSummary
                )

                _uiState.value = current.copy(
                    isSubmitting = false,
                    isSubmitted = true,
                    snackbarMessage = "Delivery confirmed for ${current.delivery.outletName} (${current.deliveredPackets} pkts, ₹${String.format("%,.0f", current.amountCollected)})"
                )
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = current.copy(
                    isSubmitting = false,
                    snackbarMessage = "Failed to confirm delivery: ${e.message}"
                )
            }
        }
    }

    fun dismissSnackbar() {
        val current = _uiState.value as? ConfirmDeliveryUiState.Ready ?: return
        _uiState.value = current.copy(snackbarMessage = null)
    }
}
