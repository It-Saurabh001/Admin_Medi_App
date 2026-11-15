package com.saurabh.mediadminapp

import com.saurabh.mediadminapp.data.v1.dto.Product
import com.saurabh.mediadminapp.data.v1.error.AppError

/** Result of GET /products/by-barcode/{barcode}. */
sealed interface BarcodeLookupState {
    data object Idle : BarcodeLookupState
    data class Loading(val barcode: String) : BarcodeLookupState
    data class Found(val barcode: String, val product: Product) : BarcodeLookupState
    data class NotFound(val barcode: String) : BarcodeLookupState
    data class Failed(val barcode: String, val message: String, val error: AppError?) : BarcodeLookupState
}
