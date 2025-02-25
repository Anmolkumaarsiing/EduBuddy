package com.anmol.e_learning

interface PaymentCallback {
    fun onPaymentSuccess(paymentId: String?)
    fun onPaymentError(errorCode: Int, errorMessage: String?)
}
