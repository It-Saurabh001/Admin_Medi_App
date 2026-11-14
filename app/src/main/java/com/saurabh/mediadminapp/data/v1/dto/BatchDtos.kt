package com.saurabh.mediadminapp.data.v1.dto

import com.google.gson.annotations.SerializedName
import java.time.Instant
import java.time.LocalDate

data class Batch(
    @SerializedName("batch_id") val batchId: String,
    @SerializedName("product_id") val productId: String,
    @SerializedName("product_name") val productName: String,
    @SerializedName("batch_no") val batchNo: String,
    @SerializedName("expiry_date") val expiryDate: LocalDate?,
    @SerializedName("mfg_date") val mfgDate: LocalDate?,
    @SerializedName("received_date") val receivedDate: LocalDate?,
    val quantity: Int,
    @SerializedName("purchase_price") val purchasePrice: Double,
    val mrp: Double,
    @SerializedName("supplier_id") val supplierId: String?,
    @SerializedName("supplier_name") val supplierName: String,
    @SerializedName("days_to_expiry") val daysToExpiry: Int?, // unverified: null possible for placeholder batches
    @SerializedName("is_expired") val isExpired: Boolean,
    @SerializedName("is_legacy_placeholder") val isLegacyPlaceholder: Boolean,
    @SerializedName("created_at") val createdAt: Instant?,
    @SerializedName("updated_at") val updatedAt: Instant?
)

/**
 * Stock ledger row. Fields present depend on the endpoint:
 * list (GET stock-movements) has batch_no + product_name, no product_stock;
 * write-off/adjust/purchase have product_stock, no batch_no/product_name.
 */
data class StockMovement(
    @SerializedName("movement_id") val movementId: String,
    @SerializedName("product_id") val productId: String,
    @SerializedName("batch_id") val batchId: String,
    @SerializedName("change_qty") val changeQty: Int,
    val reason: String, // PURCHASE|SALE|ADJUSTMENT|EXPIRED_WRITEOFF|RETURN|CANCEL_RESTOCK
    @SerializedName("reference_id") val referenceId: String?,
    @SerializedName("performed_by") val performedBy: String,
    @SerializedName("created_at") val createdAt: Instant?,
    @SerializedName("batch_no") val batchNo: String?,
    @SerializedName("product_name") val productName: String?,
    @SerializedName("product_stock") val productStock: Int?
)

/** Result of POST batches (movement null unless opening quantity), stock/adjust and stock/purchase. */
data class StockOperationResult(
    val batch: Batch,
    val movement: StockMovement?,
    @SerializedName("product_stock") val productStock: Int?,
    @SerializedName("batch_created") val batchCreated: Boolean?
)

/** Result of POST batches/{batch_id}/write-off. */
data class WriteOffResult(
    val batch: Batch,
    val movement: StockMovement
)

/** POST products/{id}/batches. */
data class BatchCreateRequest(
    @SerializedName("batch_no") val batchNo: String,
    @SerializedName("expiry_date") val expiryDate: LocalDate,
    @SerializedName("mfg_date") val mfgDate: LocalDate? = null,
    @SerializedName("received_date") val receivedDate: LocalDate? = null,
    val quantity: Int? = null,
    @SerializedName("purchase_price") val purchasePrice: Double? = null,
    val mrp: Double? = null,
    @SerializedName("supplier_id") val supplierId: String? = null,
    @SerializedName("supplier_name") val supplierName: String? = null
)

/** PATCH batches/{batch_id}. */
data class BatchUpdateRequest(
    @SerializedName("batch_no") val batchNo: String? = null,
    @SerializedName("expiry_date") val expiryDate: LocalDate? = null,
    @SerializedName("mfg_date") val mfgDate: LocalDate? = null,
    @SerializedName("received_date") val receivedDate: LocalDate? = null,
    val quantity: Int? = null,
    @SerializedName("purchase_price") val purchasePrice: Double? = null,
    val mrp: Double? = null,
    @SerializedName("supplier_id") val supplierId: String? = null,
    @SerializedName("supplier_name") val supplierName: String? = null
)

/** POST products/{id}/stock/purchase (send Idempotency-Key). */
data class StockPurchaseRequest(
    @SerializedName("batch_no") val batchNo: String,
    @SerializedName("expiry_date") val expiryDate: LocalDate,
    val quantity: Int,
    @SerializedName("mfg_date") val mfgDate: LocalDate? = null,
    @SerializedName("received_date") val receivedDate: LocalDate? = null,
    @SerializedName("purchase_price") val purchasePrice: Double? = null,
    val mrp: Double? = null,
    @SerializedName("supplier_id") val supplierId: String? = null,
    @SerializedName("supplier_name") val supplierName: String? = null,
    @SerializedName("invoice_no") val invoiceNo: String? = null
)

/** POST products/{id}/stock/adjust. changeQty is signed and non-zero; reason min 3 chars. */
data class StockAdjustRequest(
    @SerializedName("batch_id") val batchId: String,
    @SerializedName("change_qty") val changeQty: Int,
    val reason: String
)

/** POST batches/{batch_id}/write-off. */
data class WriteOffRequest(
    val note: String? = null
)
