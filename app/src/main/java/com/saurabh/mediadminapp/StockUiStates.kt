package com.saurabh.mediadminapp

import com.saurabh.mediadminapp.data.v1.dto.Batch
import com.saurabh.mediadminapp.data.v1.dto.StockOperationResult
import com.saurabh.mediadminapp.data.v1.error.AppError
import java.time.LocalDate

/** POST products/{id}/stock/purchase. */
data class StockPurchaseState(
    val isLoading: Boolean = false,
    val success: StockOperationResult? = null,
    val error: String? = null,
    val appError: AppError? = null,
)

/** Optional first batch sent right after a product is created. */
data class OpeningStock(
    val quantity: Int,
    val batchNo: String,
    val expiryDate: LocalDate,
    val purchasePrice: Double? = null,
    val mrp: Double? = null,
)

/** PATCH batches/{id} or POST batches/{id}/write-off. [wroteOff] distinguishes the two on success. */
data class BatchActionState(
    val isLoading: Boolean = false,
    val success: Batch? = null,
    val wroteOff: Boolean = false,
    val error: String? = null,
    val appError: AppError? = null,
)
