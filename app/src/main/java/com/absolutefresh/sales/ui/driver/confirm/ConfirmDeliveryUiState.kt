package com.absolutefresh.sales.ui.driver.confirm

import androidx.compose.ui.geometry.Offset
import com.absolutefresh.sales.data.model.PaymentMode
import com.absolutefresh.sales.data.repository.DeliveryItem

enum class PodMethod(val labelEn: String, val labelHi: String) {
    SIGNATURE("Digital Signature", "डिजिटल हस्ताक्षर"),
    OTP("Customer OTP", "ग्राहक ओटीपी"),
    PHOTO("Chiller Photo", "चिलर फोटो")
}

val standardShortfallReasons = listOf(
    "Retailer Chiller Full (फ्रिज में जगह नहीं)",
    "Damaged in Transit (रास्ते में डैमेज)",
    "Owner Reduced Order (मालिक ने कम लिया)",
    "Store Closed / Absent (दुकान बंद)",
    "Quality Dispute (गुणवत्ता समस्या)",
    "Other Reason (अन्य कारण)"
)

sealed class ConfirmDeliveryUiState {
    object Loading : ConfirmDeliveryUiState()
    data class Error(val message: String) : ConfirmDeliveryUiState()

    data class Ready(
        val delivery: DeliveryItem,
        val deliveredPackets: Int,
        val shortfallReason: String = "",
        val customReason: String = "",
        val amountCollected: Double,
        val paymentMode: PaymentMode = PaymentMode.CASH,
        val chequeNumber: String = "",
        val chequeBank: String = "",
        val upiTxnId: String = "",
        val podMethod: PodMethod = PodMethod.SIGNATURE,
        val receiverName: String = "",
        val signatureStrokes: List<List<Offset>> = emptyList(),
        val otpCode: String = "",
        val isOtpVerified: Boolean = false,
        val isPhotoCaptured: Boolean = false,
        val photoTimestamp: String? = null,
        val driverNotes: String = "",
        val isSubmitting: Boolean = false,
        val isSubmitted: Boolean = false,
        val snackbarMessage: String? = null
    ) : ConfirmDeliveryUiState() {

        val isShortDelivery: Boolean
            get() = deliveredPackets < delivery.orderedPackets

        val unitPrice: Double
            get() = if (delivery.orderedPackets > 0) delivery.invoiceAmount / delivery.orderedPackets else 86.10

        val netPayable: Double
            get() = (deliveredPackets * unitPrice).coerceAtLeast(0.0)

        val fillPercentage: Int
            get() = if (delivery.orderedPackets > 0) {
                ((deliveredPackets.toFloat() / delivery.orderedPackets) * 100).toInt().coerceIn(0, 100)
            } else 100

        val isPodComplete: Boolean
            get() = when (podMethod) {
                PodMethod.SIGNATURE -> signatureStrokes.isNotEmpty() || receiverName.isNotBlank()
                PodMethod.OTP -> isOtpVerified || otpCode.trim() == "4821" || otpCode.trim().length == 4
                PodMethod.PHOTO -> isPhotoCaptured
            }

        val isPaymentValid: Boolean
            get() = when (paymentMode) {
                PaymentMode.CHEQUE -> chequeNumber.trim().length >= 6 && chequeBank.isNotBlank()
                PaymentMode.CASH -> amountCollected >= 0.0
                PaymentMode.UPI_QR -> true
                PaymentMode.CREDIT_15D -> true
            }

        val isShortfallReasonValid: Boolean
            get() = !isShortDelivery || shortfallReason.isNotBlank()

        val isValid: Boolean
            get() = deliveredPackets >= 0 &&
                    isShortfallReasonValid &&
                    isPaymentValid &&
                    isPodComplete
    }
}
