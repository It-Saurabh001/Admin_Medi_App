package com.saurabh.mediadminapp.repository

import com.google.gson.JsonObject
import com.saurabh.mediadminapp.common.ResultState
import com.saurabh.mediadminapp.data.v1.api.AdminsApi
import com.saurabh.mediadminapp.data.v1.api.AuditApi
import com.saurabh.mediadminapp.data.v1.api.AuthApi
import com.saurabh.mediadminapp.data.v1.api.DevicesApi
import com.saurabh.mediadminapp.data.v1.api.HealthApi
import com.saurabh.mediadminapp.data.v1.api.InventoryApi
import com.saurabh.mediadminapp.data.v1.api.MetaApi
import com.saurabh.mediadminapp.data.v1.api.OrdersApi
import com.saurabh.mediadminapp.data.v1.api.ProductsApi
import com.saurabh.mediadminapp.data.v1.api.ReportsApi
import com.saurabh.mediadminapp.data.v1.api.SalesApi
import com.saurabh.mediadminapp.data.v1.api.SuppliersApi
import com.saurabh.mediadminapp.data.v1.api.UsersApi
import com.saurabh.mediadminapp.data.v1.api.textParts
import com.saurabh.mediadminapp.data.v1.core.ApiResult
import com.saurabh.mediadminapp.data.v1.core.HealthResponse
import com.saurabh.mediadminapp.data.v1.core.PageRequest
import com.saurabh.mediadminapp.data.v1.core.V1Gson
import com.saurabh.mediadminapp.data.v1.core.safeApiCall
import com.saurabh.mediadminapp.data.v1.core.safeApiCallPaged
import com.saurabh.mediadminapp.data.v1.dto.AdminCreateRequest
import com.saurabh.mediadminapp.data.v1.dto.AdminRegisterRequest
import com.saurabh.mediadminapp.data.v1.dto.AdminUpdateRequest
import com.saurabh.mediadminapp.data.v1.dto.AppConfig
import com.saurabh.mediadminapp.data.v1.dto.BatchCreateRequest
import com.saurabh.mediadminapp.data.v1.dto.BatchUpdateRequest
import com.saurabh.mediadminapp.data.v1.dto.CancelOrderRequest
import com.saurabh.mediadminapp.data.v1.dto.DeviceRegisterRequest
import com.saurabh.mediadminapp.data.v1.dto.EmailOtpRequest
import com.saurabh.mediadminapp.data.v1.dto.LoginRequest
import com.saurabh.mediadminapp.data.v1.dto.LogoutData
import com.saurabh.mediadminapp.data.v1.dto.LogoutRequest
import com.saurabh.mediadminapp.data.v1.dto.OrderEditRequest
import com.saurabh.mediadminapp.data.v1.dto.PasswordResetConfirmRequest
import com.saurabh.mediadminapp.data.v1.dto.PasswordResetRequest
import com.saurabh.mediadminapp.data.v1.dto.ProductCreateRequest
import com.saurabh.mediadminapp.data.v1.dto.ProductUpdateRequest
import com.saurabh.mediadminapp.data.v1.dto.RejectOrderRequest
import com.saurabh.mediadminapp.data.v1.dto.ReturnOrderRequest
import com.saurabh.mediadminapp.data.v1.dto.SalesReportSummary
import com.saurabh.mediadminapp.data.v1.dto.StockAdjustRequest
import com.saurabh.mediadminapp.data.v1.dto.StockPurchaseRequest
import com.saurabh.mediadminapp.data.v1.dto.SupplierCreateRequest
import com.saurabh.mediadminapp.data.v1.dto.SupplierUpdateRequest
import com.saurabh.mediadminapp.data.v1.dto.UserUpdateRequest
import com.saurabh.mediadminapp.data.v1.dto.VerifyOtpRequest
import com.saurabh.mediadminapp.data.v1.dto.VerifyPrescriptionRequest
import com.saurabh.mediadminapp.data.v1.dto.WriteOffRequest
import com.saurabh.mediadminapp.data.v1.error.AppError
import com.saurabh.mediadminapp.data.v1.repository.AuditFilter
import com.saurabh.mediadminapp.data.v1.repository.DateRange
import com.saurabh.mediadminapp.data.v1.repository.ImagePart
import com.saurabh.mediadminapp.data.v1.repository.OrderFilter
import com.saurabh.mediadminapp.data.v1.repository.ProductFilter
import com.saurabh.mediadminapp.data.v1.repository.SalesFilter
import com.saurabh.mediadminapp.data.v1.repository.SalesReportResult
import com.saurabh.mediadminapp.data.v1.repository.StockMovementFilter
import com.saurabh.mediadminapp.data.v1.repository.UserFilter
import com.saurabh.mediadminapp.network.ApiProvider
import com.saurabh.mediadminapp.network.TokenManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import javax.inject.Inject

/**
 * Single repository for the app, backed by the v1 API. Implements every function of the
 * data/v1/repository interfaces (catalog). Full method table: data/v1/viewmodel_migration.md.
 */
class Repository @Inject constructor(
    private val authApi: AuthApi,
    private val metaApi: MetaApi,
    private val healthApi: HealthApi,
    private val usersApi: UsersApi,
    private val adminsApi: AdminsApi,
    private val devicesApi: DevicesApi,
    private val productsApi: ProductsApi,
    private val inventoryApi: InventoryApi,
    private val ordersApi: OrdersApi,
    private val salesApi: SalesApi,
    private val suppliersApi: SuppliersApi,
    private val reportsApi: ReportsApi,
    private val auditApi: AuditApi,
    private val tokenManager: TokenManager
) {

    // ----------------------------
    // AUTH
    // ----------------------------

    /** POST auth/login (emails an OTP). Both. */
    fun loginAdmin(email: String, password: String) =
        v1Flow { authApi.login(LoginRequest(email, password, ADMIN)) }

    /** POST auth/verify-otp; persists the session on success. */
    fun verifyAdminOtp(accountId: String, otp: String) =
        v1Flow(onSuccess = { t ->
            tokenManager.saveSession(t.accessToken, t.refreshToken, t.role, t.accountId, t.accountType, t.expiresIn.toLong())
        }) { authApi.verifyOtp(VerifyOtpRequest(accountId, otp, ADMIN)) }

    /** POST auth/password-reset/request. */
    /** POST auth/verify-otp {email, otp}: signup email verification (no tokens issued). */
    fun verifyAdminEmail(email: String, otp: String) =
        v1Flow { authApi.verifyEmailOtp(EmailOtpRequest(email, otp, ADMIN)) }

    /** POST auth/resend-otp (60 s cooldown -> RATE_LIMITED). */
    fun resendAdminEmailOtp(email: String) =
        v1Flow { authApi.resendEmailOtp(EmailOtpRequest(email, null, ADMIN)) }

    fun requestAdminPasswordReset(email: String) =
        v1Flow { authApi.passwordResetRequest(PasswordResetRequest(email, ADMIN)) }

    /** POST auth/password-reset/confirm (newPassword min 8). */
    fun resetAdminPasswordWithOtp(email: String, otp: String, newPassword: String) =
        v1Flow { authApi.passwordResetConfirm(PasswordResetConfirmRequest(email, otp, newPassword, ADMIN)) }

    /** NEW. DELETE devices/{id} (if registered, failure ignored) -> POST auth/logout -> always clears the session. Both. */
    fun logout(): Flow<ResultState<LogoutData>> = flow {
        emit(ResultState.Loading)
        try {
            tokenManager.getDeviceId()?.let { id -> safeApiCall { devicesApi.unregisterDevice(id) } }
            when (val r = safeApiCall { authApi.logout(LogoutRequest(tokenManager.getRefreshToken())) }) {
                is ApiResult.Success -> emit(ResultState.Success(r.data))
                is ApiResult.Failure -> emit(ResultState.Error(AppException(r.error)))
            }
        } finally {
            tokenManager.clear()
        }
    }

    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()
    fun currentRole(): String? = tokenManager.getRole()
    fun currentAccountId(): String? = tokenManager.getAccountId()
    fun currentAccountType(): String? = tokenManager.getAccountType()
    fun clearSession() = tokenManager.clear()

    // ----------------------------
    // ADMINS
    // ----------------------------

    /** POST admins (multipart when image != null). Admin. */
    fun createAdmin(request: AdminCreateRequest, image: ImagePart? = null) = v1Flow {
        if (image == null) adminsApi.createAdmin(request)
        else adminsApi.createAdminMultipart(request.parts(), image.toPart())
    }

    /** GET admins (paged). Admin. */
    fun getAllAdmins(q: String? = null, role: String? = null, page: PageRequest = PageRequest(), isApproved: Boolean? = null) =
        v1Paged { adminsApi.listAdmins(q, role, page.toQueryMap(), isApproved) }

    /** POST admins/register (public; multipart when image != null). */
    fun registerAdmin(request: AdminRegisterRequest, image: ImagePart? = null) = v1Flow {
        if (image == null) adminsApi.registerAdmin(request)
        else adminsApi.registerAdminMultipart(request.parts(), image.toPart())
    }

    /** POST admins/{admin_id}/approve. Admin. Idempotent. */
    fun approveAdmin(adminId: String) = v1Flow { adminsApi.approveAdmin(adminId) }

    /** NEW. GET admins/{admin_id}. Admin. */
    fun getAdmin(adminId: String) = v1Flow { adminsApi.getAdmin(adminId) }

    /** PATCH admins/{admin_id} (multipart when image != null). Admin. */
    fun updateAdmin(adminId: String, request: AdminUpdateRequest, image: ImagePart? = null) = v1Flow {
        if (image == null) adminsApi.updateAdmin(adminId, request)
        else adminsApi.updateAdminMultipart(adminId, request.parts(), image.toPart())
    }

    /** DELETE admins/{admin_id} (soft). Admin. */
    fun deleteAdmin(adminId: String) = v1Flow { adminsApi.deleteAdmin(adminId) }

    // ----------------------------
    // USERS
    // ----------------------------

    /** GET users/{user_id}. Both (customer: own id). */
    fun getSpecificUser(userId: String) = v1Flow { usersApi.getUser(userId) }

    /** GET users (paged). Admin. */
    fun getAllUsers(filter: UserFilter = UserFilter(), page: PageRequest = PageRequest()) = v1Paged {
        val q = filter.toQuery()
        usersApi.listUsers(
            q = q["q"], isApproved = filter.isApproved, isBlocked = filter.isBlocked,
            includeDeleted = filter.includeDeleted, updatedSince = q["updated_since"], page = page.toQueryMap()
        )
    }

    /** PATCH users/{user_id} (admin edit; multipart when image != null). Admin. */
    fun updateUser(userId: String, request: UserUpdateRequest, image: ImagePart? = null) = v1Flow {
        if (image == null) usersApi.updateUser(userId, request)
        else usersApi.updateUserMultipart(userId, request.parts(), image.toPart())
    }

    /** true -> POST users/{id}/approve; false -> POST users/{id}/block. Admin. */
    fun isApprovedUser(userId: String, isApproved: Boolean) = v1Flow {
        if (isApproved) usersApi.approveUser(userId) else usersApi.blockUser(userId)
    }

    /** NEW. POST users/{user_id}/unblock. Admin. */
    fun unblockUser(userId: String) = v1Flow { usersApi.unblockUser(userId) }

    /** DELETE users/{user_id} (soft). Admin. */
    fun deleteUser(userId: String) = v1Flow { usersApi.deleteUser(userId) }

    /** GET users/{user_id}/orders (paged). Both (customer: own id). */
    fun getUserOrders(userId: String, filter: OrderFilter = OrderFilter(), page: PageRequest = PageRequest()) = v1Paged {
        val q = filter.toQuery()
        usersApi.listUserOrders(
            userId, status = q["status"], productId = q["product_id"], from = q["from"], to = q["to"],
            updatedSince = q["updated_since"], page = page.toQueryMap()
        )
    }

    // ----------------------------
    // PRODUCTS
    // ----------------------------

    /** GET products (paged). Both. */
    fun getAllProduct(filter: ProductFilter = ProductFilter(), page: PageRequest = PageRequest()) = v1Paged {
        val q = filter.toQuery()
        productsApi.listProducts(
            q = q["q"], category = q["category"], scheduleType = q["schedule_type"],
            requiresPrescription = filter.requiresPrescription, isActive = filter.isActive,
            inStock = filter.inStock, updatedSince = q["updated_since"], page = page.toQueryMap()
        )
    }

    /** POST products (multipart when image != null; stock starts at 0). Admin. */
    fun getAddProduct(request: ProductCreateRequest, image: ImagePart? = null) = v1Flow {
        if (image == null) productsApi.createProduct(request)
        else productsApi.createProductMultipart(request.parts(), image.toPart())
    }

    /** PATCH products/{product_id} (no stock; multipart when image != null). Admin. */
    fun updateProduct(productId: String, request: ProductUpdateRequest, image: ImagePart? = null) = v1Flow {
        if (image == null) productsApi.updateProduct(productId, request)
        else productsApi.updateProductMultipart(productId, request.parts(), image.toPart())
    }

    /** POST products/{product_id}/deactivate (soft delete). Admin. */
    fun deleteProduct(productId: String) = v1Flow { productsApi.deactivateProduct(productId) }

    /** NEW. POST products/{product_id}/reactivate. Admin. */
    fun reactivateProduct(productId: String) = v1Flow { productsApi.reactivateProduct(productId) }

    /** GET products/{product_id}. Both. */
    fun getSpecificProduct(productId: String) = v1Flow { productsApi.getProduct(productId) }

    /** NEW. GET products/by-barcode/{barcode}. Both. */
    fun getProductByBarcode(barcode: String) = v1Flow { productsApi.getProductByBarcode(barcode) }

    /** NEW. GET product-categories (paged). Both. */
    fun getProductCategories(page: PageRequest = PageRequest()) =
        v1Paged { productsApi.listCategories(page.toQueryMap()) }

    // ----------------------------
    // ORDERS
    // ----------------------------

    /** GET orders (paged; customer: own only). Both. */
    fun getAllOrders(filter: OrderFilter = OrderFilter(), page: PageRequest = PageRequest()) = v1Paged {
        val q = filter.toQuery()
        ordersApi.listOrders(
            status = q["status"], productId = q["product_id"], userId = q["user_id"], from = q["from"],
            to = q["to"], updatedSince = q["updated_since"], page = page.toQueryMap()
        )
    }

    /** GET orders/{order_id} (with sales). Both. */
    fun getOrdersById(orderId: String) = v1Flow { ordersApi.getOrder(orderId) }

    /** PATCH orders/{order_id}: only patient_name / doctor_name (pending orders, ep.50). Both. */
    fun updateOrder(orderId: String, patientName: String? = null, doctorName: String? = null) =
        v1Flow { ordersApi.editOrder(orderId, OrderEditRequest(patientName, doctorName)) }

    /** POST orders/{order_id}/cancel (restocks if deducted). Both. */
    fun deleteOrder(orderId: String, reason: String? = null) =
        v1Flow { ordersApi.cancelOrder(orderId, CancelOrderRequest(reason)) }

    /** true -> POST orders/{id}/approve; false -> POST orders/{id}/reject with reason. Admin. */
    fun approveOrder(orderId: String, isApproved: Boolean, reason: String? = null) = v1Flow {
        if (isApproved) ordersApi.approveOrder(orderId) else ordersApi.rejectOrder(orderId, RejectOrderRequest(reason))
    }

    /** NEW. POST orders/{order_id}/verify-prescription. Admin. */
    fun verifyPrescription(orderId: String, verified: Boolean = true) =
        v1Flow { ordersApi.verifyPrescription(orderId, VerifyPrescriptionRequest(verified)) }

    /** POST orders/{order_id}/sell (approved -> sold, FEFO; send Idempotency-Key). Admin. */
    fun recordSellHistory(orderId: String, idempotencyKey: String) =
        v1Flow { ordersApi.sellOrder(orderId, idempotencyKey) }

    /** NEW. POST orders/{order_id}/return (sold -> returned). Admin. */
    fun returnOrder(orderId: String, reason: String? = null) =
        v1Flow { ordersApi.returnOrder(orderId, ReturnOrderRequest(reason)) }

    /** NEW. GET orders/{order_id}/invoice (sold/returned only). */
    fun getInvoice(orderId: String) = v1Flow { ordersApi.getInvoice(orderId) }

    // ----------------------------
    // SALES
    // ----------------------------

    /** GET sales (paged ledger). Admin. */
    fun getAllSellHistory(filter: SalesFilter = SalesFilter(), page: PageRequest = PageRequest()) = v1Paged {
        val q = filter.toQuery()
        salesApi.listSales(
            from = q["from"], to = q["to"], productId = q["product_id"], userId = q["user_id"],
            orderId = q["order_id"], batchId = q["batch_id"], updatedSince = q["updated_since"], page = page.toQueryMap()
        )
    }

    /** GET sales?user_id= (paged). Admin. */
    fun getUserSellHistory(userId: String, page: PageRequest = PageRequest()) =
        getAllSellHistory(SalesFilter(userId = userId), page)

    /** GET sales?product_id= (paged). Admin. */
    fun getProductSellHistory(productId: String, page: PageRequest = PageRequest()) =
        getAllSellHistory(SalesFilter(productId = productId), page)

    // TODO v1: replace by returnOrder(orderId) (return is by order_id)
    // fun deleteSellHistory(sellId: String) = apiServices.deleteSellHistory(sellId)

    // ----------------------------
    // NEW: APP CONFIG
    // ----------------------------

    /** NEW. GET app-config (cached in memory on success). Both. */
    fun getAppConfig() = v1Flow(onSuccess = { c: AppConfig -> appConfigCache = c }) { metaApi.appConfig() }

    /** Last successfully fetched app-config, or null. */
    fun cachedAppConfig(): AppConfig? = appConfigCache

    /** server_time of the last fetched app-config, or null. */
    fun cachedServerTime(): Instant? = appConfigCache?.serverTime

    /** NEW. GET api/v1 index. Both. */
    fun getApiIndex() = v1Flow { metaApi.index() }

    /** NEW. GET /health (not enveloped). Both. */
    fun health(): Flow<ResultState<HealthResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val r = healthApi.health(ApiProvider.BASE_URL1.trimEnd('/') + "/health")
            val body = r.body()
            if (r.isSuccessful && body != null) emit(ResultState.Success(body))
            else emit(ResultState.Error(AppException(com.saurabh.mediadminapp.data.v1.core.parseApiError(r.code(), r.errorBody()?.string()))))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(AppException(if (e is java.io.IOException) AppError.Network else AppError.Unknown(e))))
        }
    }

    // ----------------------------
    // NEW: DEVICES
    // ----------------------------

    /** NEW. POST devices (stores the device id for logout). Both. */
    fun registerDevice(request: DeviceRegisterRequest) =
        v1Flow(onSuccess = { d -> tokenManager.saveDeviceId(d.deviceId) }) { devicesApi.registerDevice(request) }

    /** NEW. DELETE devices/{device_id}. Both. */
    fun unregisterDevice(deviceId: String) = v1Flow { devicesApi.unregisterDevice(deviceId) }

    // ----------------------------
    // NEW: INVENTORY
    // ----------------------------

    fun getBatches(productId: String, includeExpired: Boolean? = null, inStock: Boolean? = null, page: PageRequest = PageRequest()) =
        v1Paged { inventoryApi.listBatches(productId, includeExpired, inStock, page.toQueryMap()) }

    fun getBatch(batchId: String) = v1Flow { inventoryApi.getBatch(batchId) }

    fun createBatch(productId: String, request: BatchCreateRequest) = v1Flow { inventoryApi.createBatch(productId, request) }

    fun updateBatch(batchId: String, request: BatchUpdateRequest) = v1Flow { inventoryApi.updateBatch(batchId, request) }

    fun writeOffBatch(batchId: String, note: String? = null) = v1Flow { inventoryApi.writeOffBatch(batchId, WriteOffRequest(note)) }

    /** Send Idempotency-Key (same key on retry). */
    fun purchaseStock(productId: String, request: StockPurchaseRequest, idempotencyKey: String) =
        v1Flow { inventoryApi.purchaseStock(productId, request, idempotencyKey) }

    fun adjustStock(productId: String, request: StockAdjustRequest) = v1Flow { inventoryApi.adjustStock(productId, request) }

    fun getStockMovements(filter: StockMovementFilter = StockMovementFilter(), page: PageRequest = PageRequest()) = v1Paged {
        val q = filter.toQuery()
        inventoryApi.listStockMovements(
            productId = q["product_id"], batchId = q["batch_id"], reason = q["reason"], performedBy = q["performed_by"],
            from = q["from"], to = q["to"], page = page.toQueryMap()
        )
    }

    fun getNearExpiry(days: Int? = null, page: PageRequest = PageRequest()) = v1Paged { inventoryApi.listNearExpiry(days, page.toQueryMap()) }
    fun getExpired(page: PageRequest = PageRequest()) = v1Paged { inventoryApi.listExpired(page.toQueryMap()) }
    fun getLowStock(page: PageRequest = PageRequest()) = v1Paged { inventoryApi.listLowStock(page.toQueryMap()) }
    fun getOutOfStock(page: PageRequest = PageRequest()) = v1Paged { inventoryApi.listOutOfStock(page.toQueryMap()) }

    // ----------------------------
    // NEW: SUPPLIERS
    // ----------------------------

    fun getSuppliers(q: String? = null, isActive: Boolean? = null, page: PageRequest = PageRequest()) =
        v1Paged { suppliersApi.listSuppliers(q, isActive, page.toQueryMap()) }

    fun getSupplier(supplierId: String) = v1Flow { suppliersApi.getSupplier(supplierId) }
    fun createSupplier(request: SupplierCreateRequest) = v1Flow { suppliersApi.createSupplier(request) }
    fun updateSupplier(supplierId: String, request: SupplierUpdateRequest) = v1Flow { suppliersApi.updateSupplier(supplierId, request) }
    fun deactivateSupplier(supplierId: String) = v1Flow { suppliersApi.deactivateSupplier(supplierId) }

    // ----------------------------
    // NEW: REPORTS
    // ----------------------------

    fun getDashboardSummary() = v1Flow { reportsApi.dashboardSummary() }

    /** GET reports/sales: rows + summary (envelope `summary`) + pagination. */
    fun getSalesReport(range: DateRange = DateRange(), groupBy: String? = null, page: PageRequest = PageRequest()): Flow<ResultState<SalesReportResult>> = flow {
        emit(ResultState.Loading)
        var summary: JsonObject? = null
        val q = range.toQuery()
        when (val r = safeApiCallPaged {
            reportsApi.salesReport(q["from"], q["to"], groupBy, page.toQueryMap()).also { summary = it.body()?.summary }
        }) {
            is ApiResult.Success -> emit(ResultState.Success(SalesReportResult(
                rows = r.data,
                summary = summary?.let { runCatching { V1Gson.instance.fromJson(it, SalesReportSummary::class.java) }.getOrNull() },
                pagination = r.pagination
            )))
            is ApiResult.Failure -> emit(ResultState.Error(AppException(r.error)))
        }
    }

    fun getTopProducts(range: DateRange = DateRange(), page: PageRequest = PageRequest()) = v1Paged {
        val q = range.toQuery()
        reportsApi.topProducts(q["from"], q["to"], page.toQueryMap())
    }

    // ----------------------------
    // NEW: AUDIT
    // ----------------------------

    fun getAuditLogs(filter: AuditFilter = AuditFilter(), page: PageRequest = PageRequest()) = v1Paged {
        val q = filter.toQuery()
        auditApi.listAuditLogs(
            entity = q["entity"], entityId = q["entity_id"], actorId = q["actor_id"], action = q["action"],
            from = q["from"], to = q["to"], page = page.toQueryMap()
        )
    }

    // ----------------------------
    // multipart text fields (snake_case keys, nulls skipped)
    // ----------------------------

    private fun AdminCreateRequest.parts() =
        textParts("name" to name, "email" to email, "password" to password, "phone_number" to phoneNumber)

    private fun AdminRegisterRequest.parts() = textParts(
        "name" to name, "email" to email, "password" to password,
        "phone_number" to phoneNumber, "signup_code" to signupCode
    )

    private fun AdminUpdateRequest.parts() = textParts(
        "name" to name, "email" to email, "phone_number" to phoneNumber, "password" to password,
        "role" to role, "current_password" to currentPassword
    )

    private fun UserUpdateRequest.parts() = textParts(
        "name" to name, "email" to email, "phone_number" to phoneNumber, "address" to address, "pin_code" to pinCode
    )

    private fun ProductCreateRequest.parts() = textParts(
        "name" to name, "price" to price, "generic_name" to genericName, "brand_name" to brandName,
        "manufacturer" to manufacturer, "category" to category, "dosage_form" to dosageForm, "strength" to strength,
        "pack_size" to packSize, "unit" to unit, "hsn_code" to hsnCode, "gst_percent" to gstPercent, "mrp" to mrp,
        "purchase_price" to purchasePrice, "schedule_type" to scheduleType, "requires_prescription" to requiresPrescription,
        "storage_conditions" to storageConditions, "reorder_level" to reorderLevel, "barcode" to barcode
    )

    private fun ProductUpdateRequest.parts() = textParts(
        "name" to name, "price" to price, "generic_name" to genericName, "brand_name" to brandName,
        "manufacturer" to manufacturer, "category" to category, "dosage_form" to dosageForm, "strength" to strength,
        "pack_size" to packSize, "unit" to unit, "hsn_code" to hsnCode, "gst_percent" to gstPercent, "mrp" to mrp,
        "purchase_price" to purchasePrice, "schedule_type" to scheduleType, "requires_prescription" to requiresPrescription,
        "storage_conditions" to storageConditions, "reorder_level" to reorderLevel, "barcode" to barcode
    )

    private companion object {
        /** This is the admin app: every auth call uses account_type=admin. */
        private const val ADMIN = "admin"

        /** Process-wide cache of the last successful app-config. */
        @Volatile
        private var appConfigCache: AppConfig? = null
    }
}
