package com.saurabh.mediadminapp.data.v1.repository

import com.saurabh.mediadminapp.data.v1.core.ApiResult
import com.saurabh.mediadminapp.data.v1.core.PageRequest
import com.saurabh.mediadminapp.data.v1.dto.Batch
import com.saurabh.mediadminapp.data.v1.dto.BatchCreateRequest
import com.saurabh.mediadminapp.data.v1.dto.BatchUpdateRequest
import com.saurabh.mediadminapp.data.v1.dto.Product
import com.saurabh.mediadminapp.data.v1.dto.ProductCategory
import com.saurabh.mediadminapp.data.v1.dto.ProductCreateRequest
import com.saurabh.mediadminapp.data.v1.dto.ProductUpdateRequest
import com.saurabh.mediadminapp.data.v1.dto.StockAdjustRequest
import com.saurabh.mediadminapp.data.v1.dto.StockMovement
import com.saurabh.mediadminapp.data.v1.dto.StockOperationResult
import com.saurabh.mediadminapp.data.v1.dto.StockPurchaseRequest
import com.saurabh.mediadminapp.data.v1.dto.WriteOffResult

interface ProductRepository {
    /** GET products (paginated; customers see active products only, purchasePrice null). Both. */
    suspend fun list(filter: ProductFilter = ProductFilter(), page: PageRequest = PageRequest()): ApiResult<List<Product>>

    /** GET products/{product_id}. Both. */
    suspend fun get(productId: String): ApiResult<Product>

    /** GET products/by-barcode/{barcode}. Both. */
    suspend fun byBarcode(barcode: String): ApiResult<Product>

    /** POST products (multipart when [image] != null; stock starts at 0). Admin. */
    suspend fun create(request: ProductCreateRequest, image: ImagePart? = null): ApiResult<Product>

    /** PATCH products/{product_id} (multipart when [image] != null); never sends stock. Admin. */
    suspend fun update(productId: String, request: ProductUpdateRequest, image: ImagePart? = null): ApiResult<Product>

    /** POST products/{product_id}/deactivate (soft delete). Admin. */
    suspend fun deactivate(productId: String): ApiResult<Product>

    /** POST products/{product_id}/reactivate. Admin. */
    suspend fun reactivate(productId: String): ApiResult<Product>

    /** GET product-categories. Both. */
    suspend fun categories(page: PageRequest = PageRequest()): ApiResult<List<ProductCategory>>
}

interface InventoryRepository {
    /** GET products/{product_id}/batches. Admin. */
    suspend fun batches(
        productId: String,
        includeExpired: Boolean? = null,
        inStock: Boolean? = null,
        page: PageRequest = PageRequest()
    ): ApiResult<List<Batch>>

    /** GET batches/{batch_id}. Admin. */
    suspend fun batch(batchId: String): ApiResult<Batch>

    /** POST products/{product_id}/batches (optional opening quantity). Admin. */
    suspend fun createBatch(productId: String, request: BatchCreateRequest): ApiResult<StockOperationResult>

    /** PATCH batches/{batch_id} (metadata; fixing a legacy expiry clears the placeholder flag). Admin. */
    suspend fun updateBatch(batchId: String, request: BatchUpdateRequest): ApiResult<Batch>

    /** POST batches/{batch_id}/write-off (whole remaining quantity). Admin. */
    suspend fun writeOff(batchId: String, note: String? = null): ApiResult<WriteOffResult>

    /** POST products/{product_id}/stock/purchase (idempotent via [idempotencyKey]). Admin. */
    suspend fun purchase(productId: String, request: StockPurchaseRequest, idempotencyKey: String): ApiResult<StockOperationResult>

    /** POST products/{product_id}/stock/adjust (signed change on one batch). Admin. */
    suspend fun adjust(productId: String, request: StockAdjustRequest): ApiResult<StockOperationResult>

    /** GET stock-movements. Admin. */
    suspend fun movements(filter: StockMovementFilter = StockMovementFilter(), page: PageRequest = PageRequest()): ApiResult<List<StockMovement>>

    /** GET inventory/near-expiry (default 90 days). Admin. */
    suspend fun nearExpiry(days: Int? = null, page: PageRequest = PageRequest()): ApiResult<List<Batch>>

    /** GET inventory/expired. Admin. */
    suspend fun expired(page: PageRequest = PageRequest()): ApiResult<List<Batch>>

    /** GET inventory/low-stock (active products with stock <= reorder level). Admin. */
    suspend fun lowStock(page: PageRequest = PageRequest()): ApiResult<List<Product>>

    /** GET inventory/out-of-stock. Admin. */
    suspend fun outOfStock(page: PageRequest = PageRequest()): ApiResult<List<Product>>
}
