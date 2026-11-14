# data.v1 contract (Part 1). Root: app/src/main/java/com/saurabh/mediadminapp/data/v1/ ; package prefix `com.saurabh.mediadminapp.data.v1`

Conventions: Gson only. DTO props are camelCase with `@SerializedName("snake_case")`. Use `ApiEnvelope<Unit?>` when `data` is null. Gson instance for any Retrofit/test: `V1Gson.instance`.

## core/ApiEnvelope.kt (pkg ...core)
- `data class ApiEnvelope<T>(val status: Int, val message: String, val data: T?)`
- `data class Pagination(val limit: Int, val offset: Int, val total: Int)`
- `data class PagedEnvelope<T>(val status: Int, val message: String, val data: List<T>, val pagination: Pagination, val summary: JsonObject? = null)`

## core/HealthDtos.kt
- `data class HealthResponse(val status: String? = null, val service: String? = null)`
- `data class HealthReadyResponse(val status: String? = null, val service: String? = null, val checks: Map<String, String>? = null)`

## core/ApiResult.kt
- `sealed class ApiResult<out T>`
  - `data class Success<out T>(val data: T, val pagination: Pagination? = null)`
  - `data class Failure(val error: AppError)`
- `suspend fun <T> safeApiCall(block: suspend () -> Response<ApiEnvelope<T>>): ApiResult<T>` (null data / 204 -> `Success(null)`; declare `T` nullable e.g. `Unit?`)
- `suspend fun <T> safeApiCallPaged(block: suspend () -> Response<PagedEnvelope<T>>): ApiResult<List<T>>` (pagination in `Success.pagination`)
- `fun parseApiError(httpStatus: Int, raw: String?): AppError.Api`
- Mapping: SocketTimeoutException -> `AppError.Timeout`; IOException -> `AppError.Network`; other Exception -> `AppError.Unknown`; CancellationException rethrown; non-JSON/empty error body -> `Api(code=UNKNOWN, message="HTTP <n>")`.

## core/PageRequest.kt
- `data class PageRequest(val limit: Int = 20, val offset: Int = 0, val sort: String? = null, val order: String? = null)`
- `fun PageRequest.toQueryMap(): Map<String, String>` (limit clamped 1..100; sort/order only when non-blank) [member]
- `fun PageRequest.loadMore(pagination: Pagination): PageRequest?` (extension; next page or null)

## core/UiState.kt
- `sealed interface UiState<out T>`: `data object Loading`, `data class Success<out T>(val data: T)`, `data object Empty`, `data class Error(val error: AppError)`

## core/OrderStatus.kt
- `enum class OrderStatus(val wire: String)`: `PENDING("pending")`, `APPROVED("approved")`, `SOLD("sold")`, `CANCELLED("cancelled")`, `RETURNED("returned")`, `UNKNOWN("unknown")`; `companion fun fromWire(raw: String?): OrderStatus`
- `class OrderStatusAdapter : TypeAdapter<OrderStatus>()` (unknown -> UNKNOWN)

## core/V1Gson.kt
- `class InstantAdapter : TypeAdapter<Instant>()` (bad/absent -> null)
- `class LocalDateAdapter : TypeAdapter<LocalDate>()` (bad/absent -> null)
- `object V1Gson { fun builder(): GsonBuilder; val instance: Gson }` registers Instant, LocalDate, OrderStatus, ErrorCode adapters. Use `Instant?` / `LocalDate?` / `OrderStatus` in DTOs. Money = `Double`.

## core/AppEvents.kt
- `sealed interface AppEvent`: `data class UpgradeRequired(val minVersion: String?)`, `data object SessionInvalid`, `data object AccountBlocked`, `data object RateLimited`
- `@Singleton class AppEvents @Inject constructor() { val events: SharedFlow<AppEvent>; fun emit(event: AppEvent): Boolean }`

## core/IdempotencyKeyProvider.kt
- `@Singleton class IdempotencyKeyProvider @Inject constructor() { fun newKey(): String }`

## error/ErrorCode.kt (pkg ...error)
- `enum class ErrorCode`: VALIDATION_ERROR, PRESCRIPTION_REQUIRED, UNAUTHORIZED, TOKEN_EXPIRED, TOKEN_INVALID, FORBIDDEN, ACCOUNT_BLOCKED, ACCOUNT_NOT_APPROVED, NOT_FOUND, METHOD_NOT_ALLOWED, CONFLICT, OUT_OF_STOCK, PRESCRIPTION_NOT_VERIFIED, INVALID_TRANSITION, ALREADY_SOLD, BATCH_EXPIRED, PAYLOAD_TOO_LARGE, UNSUPPORTED_MEDIA_TYPE, UPGRADE_REQUIRED, RATE_LIMITED, INTERNAL_ERROR, UNKNOWN; `companion fun from(raw: String?): ErrorCode`
- `class ErrorCodeAdapter : TypeAdapter<ErrorCode>()`

## error/ErrorBody.kt
- `data class ErrorBody(val status: Int? = null, val message: String? = null, val code: ErrorCode? = null, val fields: Map<String, String>? = null)`

## error/AppError.kt
- `sealed class AppError`: `data class Api(val httpStatus: Int, val code: ErrorCode, val message: String, val fields: Map<String, String>?)`, `data object Network`, `data object Timeout`, `data class Unknown(val cause: Throwable?)`; `fun userMessage(): String`

## dto/AuthDtos.kt (pkg ...dto) all snake_case via @SerializedName
- `LoginRequest(email: String, password: String, accountType: String = "user")` -> POST `auth/login`
- `LoginData(accountId: String, accountType: String, otpRequired: Boolean, otpExpiresIn: Int)`
- `VerifyOtpRequest(accountId: String, otp: String, accountType: String = "user")` -> POST `auth/verify-otp`
- `TokenData(accessToken, refreshToken, tokenType: String, expiresIn: Int, role: String, accountType: String, accountId: String)` (verify-otp response)
- `RefreshData(` same fields as TokenData `)` (auth/refresh response)
- `LogoutRequest(refreshToken: String? = null)` -> POST `auth/logout`
- `LogoutData(revoked: List<String>)`
- `PasswordResetRequest(email: String, accountType: String = "user")` -> POST `auth/password-reset/request`
- `PasswordResetRequestData(otpExpiresIn: Int)`
- `PasswordResetConfirmRequest(email: String, otp: String, newPassword: String, accountType: String = "user")` -> POST `auth/password-reset/confirm`; response `ApiEnvelope<Unit?>`

## network/V1TokenStore.kt (pkg ...network)
- `interface V1TokenStore { fun getAccessToken(): String?; fun getRefreshToken(): String?; fun saveSession(accessToken: String, refreshToken: String, role: String?, accountId: String?, accountType: String?, expiresInSeconds: Long?); fun clear() }`
- `interface V1RefreshApi { @POST("auth/refresh") fun refresh(@Header("Authorization") bearerRefreshToken: String): Call<ApiEnvelope<RefreshData>> }`

## network/V1Qualifiers.kt
- `@V1Retrofit` (authenticated Retrofit), `@V1AuthRetrofit` (no token/authenticator: login, verify-otp, password-reset, refresh, signup), `@V1OkHttp`, `@V1AuthOkHttp`

## network interceptors
- `network/VersionInterceptor.kt`: `class VersionInterceptor(events: AppEvents, versionName: String = BuildConfig.VERSION_NAME) : Interceptor` (adds X-App-Version; 426 -> `UpgradeRequired(X-Min-App-Version)`)
- `network/V1AuthInterceptor.kt`: `class V1AuthInterceptor(store: V1TokenStore) : Interceptor`; `companion fun relativePath(encodedPath: String): String`, `companion fun isExcluded(method: String, encodedPath: String): Boolean`. Skips: auth/login, auth/verify-otp, auth/password-reset*, auth/refresh, app-config, openapi.json, health*, POST users, and any request already carrying `Authorization`.
- `network/ErrorEventInterceptor.kt`: `class ErrorEventInterceptor(events: AppEvents) : Interceptor` (403 ACCOUNT_BLOCKED -> AccountBlocked; 401 TOKEN_INVALID -> SessionInvalid; 429 -> RateLimited); `internal fun peekCode(response: Response): ErrorCode?`
- `network/V1TokenAuthenticator.kt`: `class V1TokenAuthenticator(store: V1TokenStore, refreshApi: () -> V1RefreshApi, events: AppEvents) : Authenticator` (401 + TOKEN_EXPIRED -> single-flight refresh, saves both tokens; other 401 code or refresh 401/403 or 2nd attempt -> `store.clear()` + SessionInvalid; 401 on requests without Authorization header and offline/5xx refresh failures leave the session untouched)
- `network/RedactingLogger.kt`: `class RedactingLogger(sink: (String) -> Unit = AppLogger) : HttpLoggingInterceptor.Logger`; `companion fun redact(message: String): String`

## network/V1NetworkModule.kt (Hilt, SingletonComponent)
- `object V1NetworkModule`; `val V1_BASE_URL: String` (= `BuildConfig.WirelessPhysicalDevice` trimmed + `/api/v1/`; Retrofit paths are RELATIVE with no leading slash, e.g. `@GET("products")`, `@POST("auth/login")`)
- Provides: `V1TokenStore` (TokenManager), `@V1AuthOkHttp OkHttpClient`, `@V1AuthRetrofit Retrofit`, `V1RefreshApi`, `V1TokenAuthenticator`, `@V1OkHttp OkHttpClient`, `@V1Retrofit Retrofit`. Both Retrofits use `GsonConverterFactory.create(V1Gson.instance)`, 90 s timeouts, redacted logging. Create APIs with `retrofit.create(XApi::class.java)` from `@V1Retrofit` (or `@V1AuthRetrofit` for token-less calls).

## TokenManager (existing file network/TokenManager.kt, additive; implements V1TokenStore)
- New: `override fun saveSession(accessToken, refreshToken, role, accountId, accountType, expiresInSeconds)`, `override fun clear()` (no event), `fun getRole(): String?`, `fun getAccountId(): String?`, `fun getAccountType(): String?`, `fun getExpiresAt(): Long` (epoch ms, 0 = unknown), `override fun getAccessToken(): String?`, `override fun getRefreshToken(): String?`
- Unchanged/compatible: `saveTokens`, `updateTokens`, `saveAdminId`, `saveRole`, `getUserRole`, `getAdminId`, `isLoggedIn()`, `clearTokens()`, `invalidateSession()`, `sessionExpiredEvent`, `getInstance`
- Storage: `EncryptedSharedPreferences` file `auth_prefs_secure` (fallback to plain `auth_prefs` if Keystore fails); one-time migration from plain `auth_prefs`; `role` shares the old `admin_role` key; `saveSession` also mirrors `account_id` into old `admin_id`.

## Build
- `app/build.gradle.kts`: added `testImplementation` mockwebserver 5.5.0, kotlinx-coroutines-test 1.10.2.
- Tests: `app/src/test/java/com/saurabh/mediadminapp/data/v1/{SafeApiCallTest,EnumsAndPagingTest,V1TokenAuthenticatorTest}.kt`

---
# PART 2 (DTOs + APIs). Packages: `...data.v1.dto`, `...data.v1.api`. All DTO props are camelCase with `@SerializedName(snake_case)` (JSON key = snake_case of the name). `Instant?`/`LocalDate?` via V1Gson adapters. Money = Double.

## DTOs (dir dto/)
### dto/UserDtos.kt
- `User(userId, name, email, phoneNumber?, address?, pinCode?, imageUrl?, role, isApproved, isBlocked, isDeleted, createdAt: Instant?)`
- `UserCreateRequest(name, email, password, phoneNumber?=null, address?=null, pinCode?=null)`
- `UserUpdateRequest(name?, email?, phoneNumber?, address?, pinCode?)` (all default null)
- `ProfileUpdateRequest(name?, email?, phoneNumber?, address?, pinCode?, password?, currentPassword?)` (all default null)
### dto/AdminDtos.kt
- `Admin(adminId, name, email, phoneNumber?, imageUrl?, role, isActive, createdAt: Instant?)`
- `AdminCreateRequest(name, email, password, phoneNumber?=null)`
- `AdminUpdateRequest(name?, email?, phoneNumber?, password?, role?, currentPassword?)` (all default null)
### dto/DeviceDtos.kt
- `Device(deviceId, userId?, platform, appVersion?, isActive, createdAt: Instant?, updatedAt: Instant?)`
- `DeviceRegisterRequest(token, platform="android", appVersion?=null)`
### dto/ProductDtos.kt
- `Product(productId, name, genericName, brandName, manufacturer, category, dosageForm, strength, packSize, unit, hsnCode, gstPercent: Double, price: Double, mrp: Double, purchasePrice: Double? (null for customers), scheduleType: String, requiresPrescription, storageConditions, reorderLevel: Int, barcode, imageUrl?, isActive, stock: Int, sellableStock: Int, inStock, createdAt: Instant?, updatedAt: Instant?)`
- `ProductCategory(name, productCount: Int)`
- `ProductCreateRequest(name, price: Double, genericName?, brandName?, manufacturer?, category?, dosageForm?, strength?, packSize?, unit?, hsnCode?, gstPercent: Double?, mrp: Double?, purchasePrice: Double?, scheduleType?, requiresPrescription: Boolean?, storageConditions?, reorderLevel: Int?, barcode?)`
- `ProductUpdateRequest(` same fields, ALL nullable/default null (incl. name?, price?) `)`
### dto/BatchDtos.kt
- `Batch(batchId, productId, productName, batchNo, expiryDate: LocalDate?, mfgDate: LocalDate?, receivedDate: LocalDate?, quantity: Int, purchasePrice: Double, mrp: Double, supplierId?, supplierName, daysToExpiry: Int?, isExpired, isLegacyPlaceholder, createdAt: Instant?, updatedAt: Instant?)`
- `StockMovement(movementId, productId, batchId, changeQty: Int, reason: String, referenceId?, performedBy, createdAt: Instant?, batchNo?, productName?, productStock: Int?)` (batchNo/productName only in GET stock-movements; productStock only in adjust/purchase/write-off)
- `StockOperationResult(batch: Batch, movement: StockMovement?, productStock: Int?, batchCreated: Boolean?)` (create batch: movement null; adjust: productStock; purchase: productStock + batchCreated)
- `WriteOffResult(batch: Batch, movement: StockMovement)`
- `BatchCreateRequest(batchNo, expiryDate: LocalDate, mfgDate?, receivedDate?, quantity: Int?, purchasePrice: Double?, mrp: Double?, supplierId?, supplierName?)`
- `BatchUpdateRequest(batchNo?, expiryDate?, mfgDate?, receivedDate?, quantity?, purchasePrice?, mrp?, supplierId?, supplierName?)` (all default null)
- `StockPurchaseRequest(batchNo, expiryDate: LocalDate, quantity: Int, mfgDate?, receivedDate?, purchasePrice?, mrp?, supplierId?, supplierName?, invoiceNo?)`
- `StockAdjustRequest(batchId, changeQty: Int, reason: String)`
- `WriteOffRequest(note?=null)`
### dto/OrderDtos.kt
- `Order(orderId, userId, userName, productId, productName, category, imageUrl?, quantity: Int, price: Double, totalAmount: Double, status: OrderStatus, message, doctorName, patientName, prescriptionUrl?, prescriptionVerified, requiresPrescription, cancelReason, createdAt: Instant?, updatedAt: Instant?, sales: List<Sale>?, restocked: List<RestockedLine>?, notRestocked: List<RestockedLine>?)` (sales: get/sell; restocked: cancel/reject/return; notRestocked: return)
- `RestockedLine(batchId, batchNo, qty: Int)`
- `OrderCreateRequest(productId, quantity: Int, message?, doctorName?, patientName?, prescriptionUrl?)`
- `OrderEditRequest(patientName?, doctorName?)`
- `CancelOrderRequest(cancelReason?=null)`, `RejectOrderRequest(reason?=null)`, `ReturnOrderRequest(reason?=null)`, `VerifyPrescriptionRequest(verified: Boolean=true)`
### dto/SaleDtos.kt
- `Sale(saleId, orderId, orderStatus: OrderStatus, userId, userName, productId, productName, imageUrl?, batchId, batchNo, quantity: Int, price: Double, gstPercent: Double, totalAmount: Double, remainingStock: Int, soldBy, soldAt: Instant?)`
### dto/InvoiceDtos.kt
- `Invoice(invoiceNumber, currency, issuedAt: Instant?, generatedAt: Instant?, pricesIncludeGst, subtotal, gstTotal, grandTotal: Double, items: List<InvoiceItem>, order: Order, user: InvoiceUser)`
- `InvoiceItem(productId, productName, batchId, batchNo, expiryDate: LocalDate?, quantity: Int, unitPrice, gstPercent, taxableAmount, gstAmount, lineTotal: Double)`
- `InvoiceUser(userId, name, email, phoneNumber?, address?, pinCode?)`
### dto/SupplierDtos.kt
- `Supplier(supplierId, name, contactPhone?, email?, gstNumber?, address?, isActive, createdAt: Instant?, updatedAt: Instant?)`
- `SupplierCreateRequest(name, contactPhone?, email?, gstNumber?, address?)`; `SupplierUpdateRequest(name?, contactPhone?, email?, gstNumber?, address?)`
### dto/ReportDtos.kt
- `DashboardSummary(date: LocalDate?, products, pendingOrders, approvedOrders, todaySalesCount: Int, todaySalesTotal: Double, lowStock, outOfStock, expiredBatches, nearExpiryBatches, nearExpiryDays: Int)`
- `SalesReportRow(key?, quantity: Int?, revenue: Double?, orders: Int?, productName?)` (all unverified)
- `SalesReportSummary(from: LocalDate?, to: LocalDate?, groupBy?, orders: Int, quantity: Int, revenue: Double)` (parse with `V1Gson.instance.fromJson(envelope.summary, SalesReportSummary::class.java)`)
- `TopProduct(productId?, productName?, quantity: Int?, revenue: Double?, imageUrl?)` (all unverified)
### dto/AuditDtos.kt
- `AuditLog(auditId, actorId, actorRole, action, entity, entityId, details: JsonObject?, createdAt: Instant?)`
### dto/MetaDtos.kt
- `AppConfig(apiPrefix, apiVersion, docsEnabled, enums: AppConfigEnums, errorCodes: List<String>, minAppVersion?, otp: AppConfigOtp, pagination: AppConfigPagination, serverTime: Instant?, uploads: AppConfigUploads)`
- `AppConfigEnums(accountTypes, devicePlatforms, dosageForms, orderStatuses, prescriptionSchedules, scheduleTypes, stockMovementReasons, units: List<String>)`
- `AppConfigOtp(expiresIn, length, maxAttempts: Int)`; `AppConfigPagination(defaultLimit, maxLimit: Int)`; `AppConfigUploads(allowedMimeTypes, imageMimeTypes: List<String>, maxRequestBytes: Long, maxUploadMb: Int)`
- `ApiIndex(name, version, apiVersion, links: List<ApiLink>)`; `ApiLink(href, methods: List<String>, rel)`

## APIs (dir api/). Paths relative to `.../api/v1/`. `page: Map<String,String>` = `PageRequest.toQueryMap()`. Dates / updatedSince are String? (`YYYY-MM-DD` / ISO `...Z`). Multipart variants take `fields = textParts("name" to value, ...)` (null values skipped) + optional file part.
### api/MultipartUtil.kt
- `fun textParts(vararg pairs: Pair<String, Any?>): Map<String, RequestBody>`
### api/MetaAndAuthApi.kt
- `MetaApi.index(): Response<ApiEnvelope<ApiIndex>>`
- `MetaApi.appConfig(): Response<ApiEnvelope<AppConfig>>`
- `MetaApi.openApi(): Response<JsonObject>` (not enveloped)
- `HealthApi.health(url: String): Response<HealthResponse>`; `HealthApi.ready(url: String): Response<HealthReadyResponse>` (pass full URL, e.g. `BuildConfig.WirelessPhysicalDevice.trimEnd('/') + "/health"`; 503 is a normal HTTP error)
- `AuthApi.login(body: LoginRequest): Response<ApiEnvelope<LoginData>>`
- `AuthApi.verifyOtp(body: VerifyOtpRequest): Response<ApiEnvelope<TokenData>>`
- `AuthApi.logout(body: LogoutRequest): Response<ApiEnvelope<LogoutData>>`
- `AuthApi.passwordResetRequest(body: PasswordResetRequest): Response<ApiEnvelope<PasswordResetRequestData>>`
- `AuthApi.passwordResetConfirm(body: PasswordResetConfirmRequest): Response<ApiEnvelope<Unit?>>`
- NOTE: refresh is ONLY `V1RefreshApi.refresh` (Part 1); it is not in AuthApi.
### api/UsersAdminsDevicesApi.kt
- `UsersApi.listUsers(q?, isApproved: Boolean?, isBlocked: Boolean?, includeDeleted: Boolean?, updatedSince?, page): Response<PagedEnvelope<User>>`
- `UsersApi.createUser(body: UserCreateRequest)`; `createUserMultipart(fields, image: MultipartBody.Part?)` (public signup; `Response<ApiEnvelope<User>>`)
- `UsersApi.getMe()`; `updateMe(body: ProfileUpdateRequest)`; `updateMeMultipart(fields, image?)` (`Response<ApiEnvelope<User>>`)
- `UsersApi.getUser(userId)`; `updateUser(userId, body: UserUpdateRequest)`; `updateUserMultipart(userId, fields, image?)`; `deleteUser(userId)`; `approveUser(userId)`; `blockUser(userId)`; `unblockUser(userId)` (all `Response<ApiEnvelope<User>>`)
- `UsersApi.listUserOrders(userId, status?, productId?, from?, to?, updatedSince?, page): Response<PagedEnvelope<Order>>`
- `AdminsApi.listAdmins(q?, role?, page): Response<PagedEnvelope<Admin>>`
- `AdminsApi.createAdmin(body: AdminCreateRequest)`; `createAdminMultipart(fields, image?)`; `getAdmin(adminId)`; `updateAdmin(adminId, body: AdminUpdateRequest)`; `updateAdminMultipart(adminId, fields, image?)`; `deleteAdmin(adminId)` (all `Response<ApiEnvelope<Admin>>`)
- `DevicesApi.registerDevice(body: DeviceRegisterRequest): Response<ApiEnvelope<Device>>` (201 new / 200 existing token)
- `DevicesApi.unregisterDevice(deviceId): Response<ApiEnvelope<Device>>`
### api/ProductsInventoryApi.kt
- `ProductsApi.listCategories(page): Response<PagedEnvelope<ProductCategory>>`
- `ProductsApi.listProducts(q?, category?, scheduleType?, requiresPrescription: Boolean?, isActive: Boolean?, inStock: Boolean?, updatedSince?, page): Response<PagedEnvelope<Product>>`
- `ProductsApi.createProduct(body: ProductCreateRequest)`; `createProductMultipart(fields, image?)`; `getProductByBarcode(barcode)`; `getProduct(productId)`; `updateProduct(productId, body: ProductUpdateRequest)`; `updateProductMultipart(productId, fields, image?)`; `deactivateProduct(productId)`; `reactivateProduct(productId)` (all `Response<ApiEnvelope<Product>>`)
- `InventoryApi.getBatch(batchId): Response<ApiEnvelope<Batch>>`; `updateBatch(batchId, body: BatchUpdateRequest): Response<ApiEnvelope<Batch>>`
- `InventoryApi.writeOffBatch(batchId, body: WriteOffRequest = WriteOffRequest()): Response<ApiEnvelope<WriteOffResult>>`
- `InventoryApi.listBatches(productId, includeExpired: Boolean?, inStock: Boolean?, page): Response<PagedEnvelope<Batch>>`
- `InventoryApi.createBatch(productId, body: BatchCreateRequest): Response<ApiEnvelope<StockOperationResult>>`
- `InventoryApi.adjustStock(productId, body: StockAdjustRequest): Response<ApiEnvelope<StockOperationResult>>`
- `InventoryApi.purchaseStock(productId, body: StockPurchaseRequest, idempotencyKey: String? = null): Response<ApiEnvelope<StockOperationResult>>` (header Idempotency-Key)
- `InventoryApi.listStockMovements(productId?, batchId?, reason?, performedBy?, from?, to?, page): Response<PagedEnvelope<StockMovement>>`
- `InventoryApi.listExpired(page): Response<PagedEnvelope<Batch>>`; `listNearExpiry(days: Int?, page): Response<PagedEnvelope<Batch>>`; `listLowStock(page): Response<PagedEnvelope<Product>>`; `listOutOfStock(page): Response<PagedEnvelope<Product>>`
### api/OrdersSalesApi.kt
- `OrdersApi.listOrders(status?, productId?, userId?, from?, to?, updatedSince?, page): Response<PagedEnvelope<Order>>`
- `OrdersApi.createOrder(body: OrderCreateRequest, idempotencyKey: String? = null): Response<ApiEnvelope<Order>>`
- `OrdersApi.createOrderMultipart(fields, prescription: MultipartBody.Part? = null, idempotencyKey: String? = null): Response<ApiEnvelope<Order>>` (text fields: product_id, quantity, message, doctor_name, patient_name)
- `OrdersApi.getOrder(orderId)`; `editOrder(orderId, body: OrderEditRequest)`; `approveOrder(orderId)`; `rejectOrder(orderId, body: RejectOrderRequest = RejectOrderRequest())`; `cancelOrder(orderId, body: CancelOrderRequest = CancelOrderRequest())`; `verifyPrescription(orderId, body: VerifyPrescriptionRequest = VerifyPrescriptionRequest())`; `returnOrder(orderId, body: ReturnOrderRequest = ReturnOrderRequest())` (all `Response<ApiEnvelope<Order>>`)
- `OrdersApi.sellOrder(orderId, idempotencyKey: String? = null): Response<ApiEnvelope<Order>>`
- `OrdersApi.getInvoice(orderId): Response<ApiEnvelope<Invoice>>`
- `OrdersApi.uploadPrescription(orderId, prescription: MultipartBody.Part, fields: Map<String, RequestBody> = emptyMap()): Response<ApiEnvelope<Order>>` (text parts: doctor_name, patient_name)
- `SalesApi.listSales(from?, to?, productId?, userId?, orderId?, batchId?, updatedSince?, page): Response<PagedEnvelope<Sale>>`
- `SuppliersApi.listSuppliers(q?, isActive: Boolean?, page): Response<PagedEnvelope<Supplier>>`; `createSupplier(body: SupplierCreateRequest)`; `getSupplier(supplierId)`; `updateSupplier(supplierId, body: SupplierUpdateRequest)`; `deactivateSupplier(supplierId)` (single ones `Response<ApiEnvelope<Supplier>>`)
- `ReportsApi.dashboardSummary(): Response<ApiEnvelope<DashboardSummary>>`
- `ReportsApi.salesReport(from?, to?, groupBy?, page): Response<PagedEnvelope<SalesReportRow>>` (totals in `.summary`)
- `ReportsApi.topProducts(from?, to?, page): Response<PagedEnvelope<TopProduct>>`
- `AuditApi.listAuditLogs(entity?, entityId?, actorId?, action?, from?, to?, page): Response<PagedEnvelope<AuditLog>>`
### api/V1ApiModule.kt (Hilt, SingletonComponent)
- `object V1ApiModule`: provides MetaApi, AuthApi, UsersApi, AdminsApi, DevicesApi, ProductsApi, InventoryApi, OrdersApi, SalesApi, SuppliersApi, ReportsApi, AuditApi (via `@V1Retrofit`) and HealthApi (via `@V1AuthRetrofit`). Inject them directly, no qualifier.

## Notes for Part 3
- All 69 `/api/v1` endpoints + `/health`, `/health/ready` are covered; `auth/refresh` is `V1RefreshApi` (Part 1).
- PATCH bodies send only non-null fields (Gson omits nulls). Never send `stock` on product PATCH.
- Idempotency-Key: pass `IdempotencyKeyProvider.newKey()` once per user action on createOrder/createOrderMultipart/sellOrder/purchaseStock; reuse the same key when retrying.
- `PRESCRIPTION_REQUIRED` -> use createOrderMultipart with a `prescription` part (or uploadPrescription after create).
- Rows of `data` that are null (e.g. password-reset confirm) use `ApiEnvelope<Unit?>`.

## `// unverified` fields (Class.field)
- Device.userId (value for admin devices)
- Batch.daysToExpiry (nullability)
- Order.notRestocked (element shape)
- Supplier.contactPhone, Supplier.email, Supplier.gstNumber, Supplier.address (nullability)
- SalesReportRow.key, .quantity, .revenue, .orders, .productName (response.md has no row example)
- TopProduct.productId, .productName, .quantity, .revenue, .imageUrl (no row example)
- AuditLog.details (free-form shape)
- InventoryApi.listExpired element = Batch; listLowStock / listOutOfStock element = Product (examples empty, shapes assumed)
