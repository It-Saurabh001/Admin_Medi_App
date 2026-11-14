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

## network/V1RefreshApi.kt (pkg ...network)
- `interface V1RefreshApi { @POST("auth/refresh") fun refresh(@Header("Authorization") bearerRefreshToken: String): Call<ApiEnvelope<RefreshData>> }`

## network/V1Qualifiers.kt
- `@V1Retrofit` (authenticated Retrofit), `@V1AuthRetrofit` (no token/authenticator: login, verify-otp, password-reset, refresh, signup), `@V1OkHttp`, `@V1AuthOkHttp`

## network interceptors
- `network/RedactingLogger.kt`: `class RedactingLogger(sink: (String) -> Unit = AppLogger) : HttpLoggingInterceptor.Logger`; `companion fun redact(message: String): String`



## Build
- `app/build.gradle.kts`: added `testImplementation` mockwebserver 5.5.0, kotlinx-coroutines-test 1.10.2.

---
# PART 2 (DTOs + APIs). Packages: `...data.v1.dto`, `...data.v1.api`. All DTO props are camelCase with `@SerializedName(snake_case)` (JSON key = snake_case of the name). `Instant?`/`LocalDate?` via V1Gson adapters. Money = Double.

## DTOs (dir dto/)
### dto/UserDtos.kt
- `User(userId, name, email, phoneNumber?, address?, pinCode?, imageUrl?, role, isApproved, isBlocked, isDeleted, createdAt: Instant?)`
- `UserUpdateRequest(name?, email?, phoneNumber?, address?, pinCode?)` (all default null)
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
- `OrdersApi.getOrder(orderId)`; `editOrder(orderId, body: OrderEditRequest)`; `approveOrder(orderId)`; `rejectOrder(orderId, body: RejectOrderRequest = RejectOrderRequest())`; `cancelOrder(orderId, body: CancelOrderRequest = CancelOrderRequest())`; `verifyPrescription(orderId, body: VerifyPrescriptionRequest = VerifyPrescriptionRequest())`; `returnOrder(orderId, body: ReturnOrderRequest = ReturnOrderRequest())` (all `Response<ApiEnvelope<Order>>`)
- `OrdersApi.sellOrder(orderId, idempotencyKey: String? = null): Response<ApiEnvelope<Order>>`
- `OrdersApi.getInvoice(orderId): Response<ApiEnvelope<Invoice>>`
- `SalesApi.listSales(from?, to?, productId?, userId?, orderId?, batchId?, updatedSince?, page): Response<PagedEnvelope<Sale>>`
- `SuppliersApi.listSuppliers(q?, isActive: Boolean?, page): Response<PagedEnvelope<Supplier>>`; `createSupplier(body: SupplierCreateRequest)`; `getSupplier(supplierId)`; `updateSupplier(supplierId, body: SupplierUpdateRequest)`; `deactivateSupplier(supplierId)` (single ones `Response<ApiEnvelope<Supplier>>`)
- `ReportsApi.dashboardSummary(): Response<ApiEnvelope<DashboardSummary>>`
- `ReportsApi.salesReport(from?, to?, groupBy?, page): Response<PagedEnvelope<SalesReportRow>>` (totals in `.summary`)
- `ReportsApi.topProducts(from?, to?, page): Response<PagedEnvelope<TopProduct>>`
- `AuditApi.listAuditLogs(entity?, entityId?, actorId?, action?, from?, to?, page): Response<PagedEnvelope<AuditLog>>`
### api/V1ApiModule.kt (Hilt, SingletonComponent)
- `object V1ApiModule`: provides MetaApi, AuthApi, UsersApi, AdminsApi, DevicesApi, ProductsApi, InventoryApi, OrdersApi, SalesApi, SuppliersApi, ReportsApi, AuditApi (via `@V1Retrofit`) and HealthApi (via `@V1AuthRetrofit`). Inject them directly, no qualifier.

## Notes for Part 3
- All ADMIN `/api/v1` endpoints + `/health`, `/health/ready` are covered (customer-only signup, users/me, POST orders and order prescription upload were removed: ADMIN APP ONLY); `auth/refresh` is `V1RefreshApi` (Part 1).
- PATCH bodies send only non-null fields (Gson omits nulls). Never send `stock` on product PATCH.
- Idempotency-Key: pass `IdempotencyKeyProvider.newKey()` once per user action on sellOrder/purchaseStock; reuse the same key when retrying.
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

---
## UPDATED existing classes (package `com.saurabh.mediadminapp.network`; consolidation: no parallel stack)
- `AuthInterceptor`: v1 branch (path contains `/api/v1/`): adds `Authorization: Bearer <access>` unless the request already has an Authorization header or the path is public (auth/login, auth/verify-otp, auth/password-reset*, auth/refresh, app-config, openapi.json, health*); no 422 handling on v1. Added companion: `fun isV1Path(encodedPath: String): Boolean`, `fun v1RelativePath(encodedPath: String): String`, `fun isV1Excluded(method: String, encodedPath: String): Boolean`. Legacy behavior unchanged.
- `TokenAuthenticator(tokenManager: TokenManager, apiServiceProvider: Lazy<ApiServices>, v1RefreshApi: () -> V1RefreshApi = {throws}, appEvents: AppEvents = AppEvents())` (no longer `@Inject`; provided in ApiProvider). v1 paths: 401 with `code=TOKEN_EXPIRED` -> single-flight refresh via `V1RefreshApi.refresh("Bearer <refresh>")`, saves BOTH tokens + role + account_id/type + expiry via `tokenManager.saveSession`, retries once; any other 401 code / refresh 401-403 / 2nd attempt -> `tokenManager.invalidateSession()` + `AppEvents.SessionInvalid`; 401 on requests without Authorization header and offline/5xx refresh failures leave the session untouched. Legacy paths: old behavior. Both flows use the ONE `TokenManager.refreshLock`.
- `TokenManager` (additive; all old getters/setters kept): `internal constructor(prefs: SharedPreferences)` (tests), `val refreshLock: ReentrantLock`, `fun saveSession(accessToken: String, refreshToken: String, role: String?, accountId: String?, accountType: String?, expiresInSeconds: Long?)`, `fun clear()` (no event), `fun getRole(): String?`, `fun getAccountId(): String?`, `fun getAccountType(): String?`, `fun getExpiresAt(): Long` (epoch ms, 0 = unknown), `getAccessToken()`, `getRefreshToken()`, `isLoggedIn()`. Storage: EncryptedSharedPreferences file `auth_prefs_secure` (falls back to plain `auth_prefs` if Keystore fails) with one-time migration from the plain file; `role` shares the old `admin_role` key and `saveSession` mirrors account_id into the old `admin_id`.
- `ApiProvider` (Hilt): `val V1_BASE_URL: String` (BuildConfig base + `api/v1/`; Retrofit paths are relative, no leading slash). `provideTokenAuthenticator` now also takes `Lazy<V1RefreshApi>` + `AppEvents`; `provideHttpClient` now also takes `AppEvents` and adds `VersionInterceptor` + `ErrorEventInterceptor` (both active on `/api/v1/` paths only). New providers: `@V1OkHttp OkHttpClient` (= main client `.newBuilder().build()`), `@V1AuthOkHttp OkHttpClient` (= main client without authenticator), `@V1Retrofit Retrofit`, `@V1AuthRetrofit Retrofit` (both `GsonConverterFactory.create(V1Gson.instance)`), `V1RefreshApi`. The v1 Gson adapters apply only to v1 Retrofits; old Retrofits keep plain Gson. Provided HttpLoggingInterceptor now uses `RedactingLogger` (Log tag "OkHttp") and redacts the Authorization header.
- `LoggingInterceptor`: request/response bodies pass through `RedactingLogger.redact`; Authorization header logged as `[REDACTED]`.

## NEW
- `data/v1/network/VersionInterceptor.kt`: `class VersionInterceptor(events: AppEvents, versionName: String = BuildConfig.VERSION_NAME) : Interceptor` (v1 paths only: X-App-Version; 426 -> `AppEvent.UpgradeRequired(X-Min-App-Version)`).
- `data/v1/network/ErrorEventInterceptor.kt`: `class ErrorEventInterceptor(events: AppEvents) : Interceptor` (v1 paths only; peeks body: 403 ACCOUNT_BLOCKED -> AccountBlocked; 401 TOKEN_INVALID -> SessionInvalid; 429 -> RateLimited); `internal fun peekCode(response: Response): ErrorCode?`.
- `data/v1/network/RedactingLogger.kt`, `V1Qualifiers.kt`, `V1RefreshApi.kt` (see sections above).
- REMOVED (merged into the classes above): V1AuthInterceptor, V1TokenAuthenticator, V1NetworkModule, V1TokenStore.

---
# PART 3 Repository interfaces (dir data/v1/repository/, package `...data.v1.repository`). No implementations, no @Binds yet. All return `ApiResult<T>`; lists are `ApiResult<List<T>>` with pagination in `ApiResult.Success.pagination`.

## Filters.kt
- `DateRange(from: LocalDate? = null, to: LocalDate? = null)`
- `ImagePart(fileName: String, mimeType: String, bytes: ByteArray)` (no OkHttp types)
- `ProductFilter(q?, category?, scheduleType?, requiresPrescription: Boolean?, isActive: Boolean?, inStock: Boolean?, updatedSince: Instant?)`
- `OrderFilter(status: OrderStatus?, productId?, userId?, dateRange: DateRange = DateRange(), updatedSince: Instant?)`
- `SalesFilter(dateRange: DateRange, productId?, userId?, orderId?, batchId?, updatedSince: Instant?)`
- `UserFilter(q?, isApproved: Boolean?, isBlocked: Boolean?, includeDeleted: Boolean?, updatedSince: Instant?)`
- `StockMovementFilter(productId?, batchId?, reason?, performedBy?, dateRange: DateRange)`
- `AuditFilter(entity?, entityId?, actorId?, action?, dateRange: DateRange)`
## AppConfigAuthRepositories.kt
- `AppConfigRepository`: `getAppConfig(): ApiResult<AppConfig>`; `getApiIndex(): ApiResult<ApiIndex>`; `health(): ApiResult<HealthResponse>`
- `AuthRepository` (account_type fixed to admin): `login(email, password): ApiResult<LoginData>`; `verifyOtp(accountId, otp): ApiResult<TokenData>`; `logout(): ApiResult<LogoutData>`; `requestPasswordReset(email): ApiResult<PasswordResetRequestData>`; `confirmPasswordReset(email, otp, newPassword): ApiResult<Unit?>`; non-suspend: `isLoggedIn(): Boolean`, `currentRole(): String?`, `currentAccountId(): String?`, `currentAccountType(): String?`, `clearSession()`
## UserAdminDeviceRepositories.kt
- `UserRepository`: `list(filter: UserFilter, page: PageRequest): ApiResult<List<User>>`; `get(userId): ApiResult<User>`; `adminUpdate(userId, request: UserUpdateRequest, image: ImagePart? = null)`; `delete(userId)`; `approve(userId)`; `block(userId)`; `unblock(userId)` (all `ApiResult<User>`); `ordersOf(userId, filter: OrderFilter, page: PageRequest): ApiResult<List<Order>>`
- `AdminRepository`: `list(q?, role?, page): ApiResult<List<Admin>>`; `get(adminId)`; `create(request: AdminCreateRequest, image?)`; `update(adminId, request: AdminUpdateRequest, image?)`; `delete(adminId)` (`ApiResult<Admin>`)
- `DeviceRepository`: `register(request: DeviceRegisterRequest): ApiResult<Device>`; `unregister(deviceId): ApiResult<Device>`
## ProductInventoryRepositories.kt
- `ProductRepository`: `list(filter: ProductFilter, page): ApiResult<List<Product>>`; `get(productId)`; `byBarcode(barcode)`; `create(request: ProductCreateRequest, image?)`; `update(productId, request: ProductUpdateRequest, image?)`; `deactivate(productId)`; `reactivate(productId)` (`ApiResult<Product>`); `categories(page): ApiResult<List<ProductCategory>>`
- `InventoryRepository`: `batches(productId, includeExpired: Boolean?, inStock: Boolean?, page): ApiResult<List<Batch>>`; `batch(batchId): ApiResult<Batch>`; `createBatch(productId, request: BatchCreateRequest): ApiResult<StockOperationResult>`; `updateBatch(batchId, request: BatchUpdateRequest): ApiResult<Batch>`; `writeOff(batchId, note: String? = null): ApiResult<WriteOffResult>`; `purchase(productId, request: StockPurchaseRequest, idempotencyKey: String): ApiResult<StockOperationResult>`; `adjust(productId, request: StockAdjustRequest): ApiResult<StockOperationResult>`; `movements(filter: StockMovementFilter, page): ApiResult<List<StockMovement>>`; `nearExpiry(days: Int?, page): ApiResult<List<Batch>>`; `expired(page): ApiResult<List<Batch>>`; `lowStock(page): ApiResult<List<Product>>`; `outOfStock(page): ApiResult<List<Product>>`
## OrderSalesRepositories.kt
- `OrderRepository`: `list(filter: OrderFilter, page): ApiResult<List<Order>>`; `get(orderId)`; `edit(orderId, request: OrderEditRequest)`; `approve(orderId)`; `reject(orderId, reason: String? = null)`; `cancel(orderId, reason: String? = null)`; `verifyPrescription(orderId, verified: Boolean = true)`; `sell(orderId, idempotencyKey: String)`; `returnOrder(orderId, reason: String? = null)` (all `ApiResult<Order>`); `invoice(orderId): ApiResult<Invoice>`
- `SalesRepository`: `list(filter: SalesFilter, page): ApiResult<List<Sale>>`
## SupplierReportAuditRepositories.kt
- `SalesReportResult(rows: List<SalesReportRow>, summary: SalesReportSummary?, pagination: Pagination?)`
- `SupplierRepository`: `list(q?, isActive: Boolean?, page): ApiResult<List<Supplier>>`; `get(supplierId)`; `create(request: SupplierCreateRequest)`; `update(supplierId, request: SupplierUpdateRequest)`; `deactivate(supplierId)` (`ApiResult<Supplier>`)
- `ReportRepository`: `dashboardSummary(): ApiResult<DashboardSummary>`; `salesReport(range: DateRange, groupBy: String?, page): ApiResult<SalesReportResult>`; `topProducts(range: DateRange, page): ApiResult<List<TopProduct>>`
- `AuditRepository`: `list(filter: AuditFilter, page): ApiResult<List<AuditLog>>`

## Tests (src/test/java/com/saurabh/mediadminapp/data/v1/)
- `SafeApiCallTest`, `EnumsAndPagingTest`, `TokenAuthenticatorTest` (consolidated TokenAuthenticator: 5 parallel v1 401 -> one refresh; legacy path still uses `admin/refreshToken`), `DtoParsingTest` (response.md examples). Build: `testOptions { unitTests.isReturnDefaultValues = true }` added.

---
## PART 4 Repository.kt (updated in place)
File `app/src/main/java/com/saurabh/mediadminapp/repository/Repository.kt` (package `com.saurabh.mediadminapp.repository`) now implements every function of the data/v1/repository interfaces (catalog) on the v1 APIs; old private safeApiCall/parseErrorMessage and old-DTO imports removed; `deleteSellHistory` commented out (TODO: use returnOrder). Constructor: the 13 v1 API interfaces + TokenManager.
Helpers in `repository/RepositorySupport.kt`:
- `class AppException(val error: AppError) : Exception(error.userMessage())`
- `data class PagedData<T>(val items: List<T>, val pagination: Pagination?)`
- `internal fun <T> v1Flow(onSuccess: suspend (T) -> Unit = {}, block: suspend () -> Response<ApiEnvelope<T>>): Flow<ResultState<T>>`
- `internal fun <T> v1Paged(block: suspend () -> Response<PagedEnvelope<T>>): Flow<ResultState<PagedData<T>>>`
- `fun queryOf(vararg pairs: Pair<String, Any?>): Map<String, String>`; `fun DateRange?.toQuery(): Map<String, String>`
- `fun ProductFilter.toQuery()`, `OrderFilter.toQuery()`, `SalesFilter.toQuery()`, `UserFilter.toQuery()`, `StockMovementFilter.toQuery()`, `AuditFilter.toQuery()` (keys = @Query names)
- `fun ImagePart.toPart(field: String = "image"): MultipartBody.Part`
- TokenManager additive: `saveDeviceId(deviceId: String)`, `getDeviceId(): String?` (cleared with the session).
Full method table: data/v1/viewmodel_migration.md


## PART 5 prune (ADMIN APP ONLY)
- Removed: Repository.getMe/updateMe/signup/createOrder/uploadPrescription; auth methods lost `accountType` (fixed "admin"); UserRepository.signup/me/updateMe; OrderRepository.create/uploadPrescription; FilePart (+toPart); UsersApi.createUser/createUserMultipart/getMe/updateMe/updateMeMultipart; OrdersApi.createOrder/createOrderMultipart/uploadPrescription; DTOs UserCreateRequest, ProfileUpdateRequest, OrderCreateRequest; AuthInterceptor no longer exempts POST users.
- `fun Uri.toImagePart(context: Context): ImagePart?` added in `utils/utilityFunctions/UriExtensions.kt`.
- ViewModel API: data/v1/viewmodel_api.md; UI follow-ups: data/v1/ui_followups.md