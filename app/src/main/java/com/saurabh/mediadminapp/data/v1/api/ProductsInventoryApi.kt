package com.saurabh.mediadminapp.data.v1.api

import com.saurabh.mediadminapp.data.v1.core.ApiEnvelope
import com.saurabh.mediadminapp.data.v1.core.PagedEnvelope
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
import com.saurabh.mediadminapp.data.v1.dto.WriteOffRequest
import com.saurabh.mediadminapp.data.v1.dto.WriteOffResult
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface ProductsApi {
    @GET("product-categories")
    suspend fun listCategories(@QueryMap page: Map<String, String> = emptyMap()): Response<PagedEnvelope<ProductCategory>>

    @GET("products")
    suspend fun listProducts(
        @Query("q") q: String? = null,
        @Query("category") category: String? = null,
        @Query("schedule_type") scheduleType: String? = null,
        @Query("requires_prescription") requiresPrescription: Boolean? = null,
        @Query("is_active") isActive: Boolean? = null,
        @Query("in_stock") inStock: Boolean? = null,
        @Query("updated_since") updatedSince: String? = null,
        @QueryMap page: Map<String, String> = emptyMap()
    ): Response<PagedEnvelope<Product>>

    @POST("products")
    suspend fun createProduct(@Body body: ProductCreateRequest): Response<ApiEnvelope<Product>>

    @Multipart
    @POST("products")
    suspend fun createProductMultipart(
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part? = null
    ): Response<ApiEnvelope<Product>>

    @GET("products/by-barcode/{barcode}")
    suspend fun getProductByBarcode(@Path("barcode") barcode: String): Response<ApiEnvelope<Product>>

    @GET("products/{product_id}")
    suspend fun getProduct(@Path("product_id") productId: String): Response<ApiEnvelope<Product>>

    @PATCH("products/{product_id}")
    suspend fun updateProduct(
        @Path("product_id") productId: String,
        @Body body: ProductUpdateRequest
    ): Response<ApiEnvelope<Product>>

    @Multipart
    @PATCH("products/{product_id}")
    suspend fun updateProductMultipart(
        @Path("product_id") productId: String,
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part? = null
    ): Response<ApiEnvelope<Product>>

    @POST("products/{product_id}/deactivate")
    suspend fun deactivateProduct(@Path("product_id") productId: String): Response<ApiEnvelope<Product>>

    @POST("products/{product_id}/reactivate")
    suspend fun reactivateProduct(@Path("product_id") productId: String): Response<ApiEnvelope<Product>>
}

/** Batches, stock operations, movements and alert lists (admin). */
interface InventoryApi {
    @GET("batches/{batch_id}")
    suspend fun getBatch(@Path("batch_id") batchId: String): Response<ApiEnvelope<Batch>>

    @PATCH("batches/{batch_id}")
    suspend fun updateBatch(
        @Path("batch_id") batchId: String,
        @Body body: BatchUpdateRequest
    ): Response<ApiEnvelope<Batch>>

    @POST("batches/{batch_id}/write-off")
    suspend fun writeOffBatch(
        @Path("batch_id") batchId: String,
        @Body body: WriteOffRequest = WriteOffRequest()
    ): Response<ApiEnvelope<WriteOffResult>>

    @GET("products/{product_id}/batches")
    suspend fun listBatches(
        @Path("product_id") productId: String,
        @Query("include_expired") includeExpired: Boolean? = null,
        @Query("in_stock") inStock: Boolean? = null,
        @QueryMap page: Map<String, String> = emptyMap()
    ): Response<PagedEnvelope<Batch>>

    @POST("products/{product_id}/batches")
    suspend fun createBatch(
        @Path("product_id") productId: String,
        @Body body: BatchCreateRequest
    ): Response<ApiEnvelope<StockOperationResult>>

    @POST("products/{product_id}/stock/adjust")
    suspend fun adjustStock(
        @Path("product_id") productId: String,
        @Body body: StockAdjustRequest
    ): Response<ApiEnvelope<StockOperationResult>>

    @POST("products/{product_id}/stock/purchase")
    suspend fun purchaseStock(
        @Path("product_id") productId: String,
        @Body body: StockPurchaseRequest,
        @Header("Idempotency-Key") idempotencyKey: String? = null
    ): Response<ApiEnvelope<StockOperationResult>>

    @GET("stock-movements")
    suspend fun listStockMovements(
        @Query("product_id") productId: String? = null,
        @Query("batch_id") batchId: String? = null,
        @Query("reason") reason: String? = null,
        @Query("performed_by") performedBy: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @QueryMap page: Map<String, String> = emptyMap()
    ): Response<PagedEnvelope<StockMovement>>

    @GET("inventory/expired")
    suspend fun listExpired(@QueryMap page: Map<String, String> = emptyMap()): Response<PagedEnvelope<Batch>> // item shape assumed = Batch (unverified, empty example)

    @GET("inventory/near-expiry")
    suspend fun listNearExpiry(
        @Query("days") days: Int? = null,
        @QueryMap page: Map<String, String> = emptyMap()
    ): Response<PagedEnvelope<Batch>>

    @GET("inventory/low-stock")
    suspend fun listLowStock(@QueryMap page: Map<String, String> = emptyMap()): Response<PagedEnvelope<Product>> // item shape assumed = Product (unverified)

    @GET("inventory/out-of-stock")
    suspend fun listOutOfStock(@QueryMap page: Map<String, String> = emptyMap()): Response<PagedEnvelope<Product>> // item shape assumed = Product (unverified)
}
