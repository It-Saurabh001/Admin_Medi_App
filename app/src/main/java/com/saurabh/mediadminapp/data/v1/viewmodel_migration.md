# ViewModel migration guide (Repository.kt on API v1)

ADMIN APP ONLY: no customer code exists in this project.

Self-contained: write ViewModels from this file only. Package prefixes used below:
- `dto.` = `com.saurabh.mediadminapp.data.v1.dto`
- `filter.` = `com.saurabh.mediadminapp.data.v1.repository` (filters, `DateRange`, `ImagePart`, `SalesReportResult`)
- `core.` = `com.saurabh.mediadminapp.data.v1.core` (`PageRequest`, `Pagination`, `OrderStatus`, `UiState`, `AppEvents`, `AppEvent`, `IdempotencyKeyProvider`, `HealthResponse`, `loadMore`)
- `err.` = `com.saurabh.mediadminapp.data.v1.error` (`AppError`, `ErrorCode`)
- `repo.` = `com.saurabh.mediadminapp.repository` (`Repository`, `PagedData`, `AppException`)
- `ResultState` = `com.saurabh.mediadminapp.common.ResultState` (unchanged: `Loading`, `Success(data)`, `Error(exception)`)

All Flow methods are plain `fun` (not suspend), cold flows: emit `Loading`, then `Success(T)` or `Error(AppException)`. `page` defaults to `core.PageRequest()` (limit 20, offset 0). Paged methods return `repo.PagedData<T>(items: List<T>, pagination: core.Pagination?)`.

## Repository methods

| Method -> return | Status | Old name / note | Idem. key |
|---|---|---|---|
| `loginAdmin(email: String, password: String) -> Flow<ResultState<dto.LoginData>>` | REWRITTEN | was (email,password) | N |
| `verifyAdminOtp(accountId: String, otp: String) -> Flow<ResultState<dto.TokenData>>` (saves session on success) | REWRITTEN | was (adminId, otp) | N |
| `requestAdminPasswordReset(email: String) -> Flow<ResultState<dto.PasswordResetRequestData>>` | REWRITTEN | | N |
| `resetAdminPasswordWithOtp(email: String, otp: String, newPassword: String) -> Flow<ResultState<Unit?>>` | REWRITTEN | 1st param was adminId, now EMAIL | N |
| `logout() -> Flow<ResultState<dto.LogoutData>>` (unregisters device, revokes tokens, ALWAYS clears session) | NEW | replaces local-only clear | N |
| `isLoggedIn(): Boolean`, `currentRole(): String?`, `currentAccountId(): String?`, `currentAccountType(): String?`, `clearSession()` (no network) | NEW | | N |
| `createAdmin(request: dto.AdminCreateRequest, image: filter.ImagePart? = null) -> Flow<ResultState<dto.Admin>>` | REWRITTEN | was (name,password,email,phoneNumber,MultipartBody.Part?) | N |
| `getAllAdmins(q: String? = null, role: String? = null, page: core.PageRequest) -> Flow<ResultState<repo.PagedData<dto.Admin>>>` | REWRITTEN | | N |
| `getAdmin(adminId: String) -> Flow<ResultState<dto.Admin>>` | NEW | use for own profile (id = `currentAccountId()`) | N |
| `updateAdmin(adminId: String, request: dto.AdminUpdateRequest, image: filter.ImagePart? = null) -> Flow<ResultState<dto.Admin>>` | REWRITTEN | | N |
| `deleteAdmin(adminId: String) -> Flow<ResultState<dto.Admin>>` | REWRITTEN | soft delete | N |
| `getSpecificUser(userId: String) -> Flow<ResultState<dto.User>>` | REWRITTEN | | N |
| `getAllUsers(filter: filter.UserFilter = UserFilter(), page: core.PageRequest) -> Flow<ResultState<repo.PagedData<dto.User>>>` | REWRITTEN | | N |
| `updateUser(userId: String, request: dto.UserUpdateRequest, image: filter.ImagePart? = null) -> Flow<ResultState<dto.User>>` | REWRITTEN | no password/isApproved/block/role anymore | N |
| `isApprovedUser(userId: String, isApproved: Boolean) -> Flow<ResultState<dto.User>>` (true = approve, false = block) | REWRITTEN | | N |
| `unblockUser(userId: String) -> Flow<ResultState<dto.User>>` | NEW | | N |
| `deleteUser(userId: String) -> Flow<ResultState<dto.User>>` | REWRITTEN | soft delete | N |
| `getUserOrders(userId: String, filter: filter.OrderFilter = OrderFilter(), page: core.PageRequest) -> Flow<ResultState<repo.PagedData<dto.Order>>>` | REWRITTEN | | N |
| `getAllProduct(filter: filter.ProductFilter = ProductFilter(), page: core.PageRequest) -> Flow<ResultState<repo.PagedData<dto.Product>>>` | REWRITTEN | | N |
| `getAddProduct(request: dto.ProductCreateRequest, image: filter.ImagePart? = null) -> Flow<ResultState<dto.Product>>` | REWRITTEN | no stock (add via purchaseStock/createBatch) | N |
| `updateProduct(productId: String, request: dto.ProductUpdateRequest, image: filter.ImagePart? = null) -> Flow<ResultState<dto.Product>>` | REWRITTEN | no stock (use adjustStock) | N |
| `deleteProduct(productId: String) -> Flow<ResultState<dto.Product>>` | REWRITTEN | now deactivate (soft) | N |
| `reactivateProduct(productId: String) -> Flow<ResultState<dto.Product>>` | NEW | | N |
| `getSpecificProduct(productId: String) -> Flow<ResultState<dto.Product>>` | REWRITTEN | | N |
| `getProductByBarcode(barcode: String) -> Flow<ResultState<dto.Product>>` | NEW | | N |
| `getProductCategories(page: core.PageRequest) -> Flow<ResultState<repo.PagedData<dto.ProductCategory>>>` | NEW | | N |
| `getAllOrders(filter: filter.OrderFilter = OrderFilter(), page: core.PageRequest) -> Flow<ResultState<repo.PagedData<dto.Order>>>` | REWRITTEN | | N |
| `getOrdersById(orderId: String) -> Flow<ResultState<dto.Order>>` (incl. `sales`) | REWRITTEN | | N |
| `updateOrder(orderId: String, patientName: String? = null, doctorName: String? = null) -> Flow<ResultState<dto.Order>>` | REWRITTEN | only these 2 fields (pending orders); quantity/price/total/product_name/message/isApproved are gone | N |
| `deleteOrder(orderId: String, reason: String? = null) -> Flow<ResultState<dto.Order>>` | REWRITTEN | now cancel (no hard delete) | N |
| `approveOrder(orderId: String, isApproved: Boolean, reason: String? = null) -> Flow<ResultState<dto.Order>>` (true = approve, false = reject) | REWRITTEN | | N |
| `verifyPrescription(orderId: String, verified: Boolean = true) -> Flow<ResultState<dto.Order>>` | NEW | | N |
| `recordSellHistory(orderId: String, idempotencyKey: String) -> Flow<ResultState<dto.Order>>` (approved -> sold) | REWRITTEN | now POST orders/{id}/sell | Y |
| `returnOrder(orderId: String, reason: String? = null) -> Flow<ResultState<dto.Order>>` | NEW | replaces deleteSellHistory | N |
| `getInvoice(orderId: String) -> Flow<ResultState<dto.Invoice>>` | NEW | sold/returned only | N |
| `getAllSellHistory(filter: filter.SalesFilter = SalesFilter(), page: core.PageRequest) -> Flow<ResultState<repo.PagedData<dto.Sale>>>` | REWRITTEN | | N |
| `getUserSellHistory(userId: String, page: core.PageRequest) -> Flow<ResultState<repo.PagedData<dto.Sale>>>` | REWRITTEN | = getAllSellHistory(SalesFilter(userId)) | N |
| `getProductSellHistory(productId: String, page: core.PageRequest) -> Flow<ResultState<repo.PagedData<dto.Sale>>>` | REWRITTEN | = getAllSellHistory(SalesFilter(productId)) | N |
| `deleteSellHistory` | REMOVED (commented, TODO) | use `returnOrder(orderId)` | - |
| `getAppConfig() -> Flow<ResultState<dto.AppConfig>>` (caches on success); `cachedAppConfig(): dto.AppConfig?`; `cachedServerTime(): java.time.Instant?` | NEW | call at app start | N |
| `getApiIndex() -> Flow<ResultState<dto.ApiIndex>>`; `health() -> Flow<ResultState<core.HealthResponse>>` | NEW | | N |
| `registerDevice(request: dto.DeviceRegisterRequest) -> Flow<ResultState<dto.Device>>` (stores device id for logout) | NEW | | N |
| `unregisterDevice(deviceId: String) -> Flow<ResultState<dto.Device>>` | NEW | | N |
| `getBatches(productId: String, includeExpired: Boolean? = null, inStock: Boolean? = null, page: core.PageRequest) -> Flow<ResultState<repo.PagedData<dto.Batch>>>` | NEW | | N |
| `getBatch(batchId: String) -> Flow<ResultState<dto.Batch>>` | NEW | | N |
| `createBatch(productId: String, request: dto.BatchCreateRequest) -> Flow<ResultState<dto.StockOperationResult>>` | NEW | | N |
| `updateBatch(batchId: String, request: dto.BatchUpdateRequest) -> Flow<ResultState<dto.Batch>>` | NEW | | N |
| `writeOffBatch(batchId: String, note: String? = null) -> Flow<ResultState<dto.WriteOffResult>>` | NEW | | N |
| `purchaseStock(productId: String, request: dto.StockPurchaseRequest, idempotencyKey: String) -> Flow<ResultState<dto.StockOperationResult>>` | NEW | | Y |
| `adjustStock(productId: String, request: dto.StockAdjustRequest) -> Flow<ResultState<dto.StockOperationResult>>` | NEW | | N |
| `getStockMovements(filter: filter.StockMovementFilter = StockMovementFilter(), page: core.PageRequest) -> Flow<ResultState<repo.PagedData<dto.StockMovement>>>` | NEW | | N |
| `getNearExpiry(days: Int? = null, page) -> PagedData<dto.Batch>`; `getExpired(page) -> PagedData<dto.Batch>`; `getLowStock(page) -> PagedData<dto.Product>`; `getOutOfStock(page) -> PagedData<dto.Product>` (all `Flow<ResultState<...>>`) | NEW | | N |
| `getSuppliers(q: String? = null, isActive: Boolean? = null, page: core.PageRequest) -> Flow<ResultState<repo.PagedData<dto.Supplier>>>` | NEW | | N |
| `getSupplier(supplierId: String)`, `createSupplier(request: dto.SupplierCreateRequest)`, `updateSupplier(supplierId: String, request: dto.SupplierUpdateRequest)`, `deactivateSupplier(supplierId: String)` -> `Flow<ResultState<dto.Supplier>>` | NEW | | N |
| `getDashboardSummary() -> Flow<ResultState<dto.DashboardSummary>>` | NEW | replaces client-side counting | N |
| `getSalesReport(range: filter.DateRange = DateRange(), groupBy: String? = null, page: core.PageRequest) -> Flow<ResultState<filter.SalesReportResult>>` | NEW | groupBy "day"/"product" | N |
| `getTopProducts(range: filter.DateRange = DateRange(), page: core.PageRequest) -> Flow<ResultState<repo.PagedData<dto.TopProduct>>>` | NEW | | N |
| `getAuditLogs(filter: filter.AuditFilter = AuditFilter(), page: core.PageRequest) -> Flow<ResultState<repo.PagedData<dto.AuditLog>>>` | NEW | | N |

## Type reference
Timestamps `java.time.Instant?`, dates `java.time.LocalDate?`, money `Double`. Request DTO fields default to null unless shown without `?`.
- `dto.LoginData`: accountId:String, accountType:String, otpRequired:Boolean, otpExpiresIn:Int
- `dto.TokenData`: accessToken:String, refreshToken:String, tokenType:String, expiresIn:Int, role:String, accountType:String, accountId:String
- `dto.LogoutData`: revoked:List<String>
- `dto.PasswordResetRequestData`: otpExpiresIn:Int
- `dto.User`: userId:String, name:String, email:String, phoneNumber:String?, address:String?, pinCode:String?, imageUrl:String?, role:String, isApproved:Boolean, isBlocked:Boolean, isDeleted:Boolean, createdAt:Instant?
- `dto.UserUpdateRequest`: name?, email?, phoneNumber?, address?, pinCode? (all String?)
- `dto.Admin`: adminId:String, name:String, email:String, phoneNumber:String?, imageUrl:String?, role:String (admin|disabled|deleted), isActive:Boolean, createdAt:Instant?
- `dto.AdminCreateRequest`: name:String, email:String, password:String (min 8), phoneNumber:String?
- `dto.AdminUpdateRequest`: name?, email?, phoneNumber?, password?, role? ("admin"|"disabled"), currentPassword? (all String?)
- `dto.Device`: deviceId:String, userId:String?, platform:String, appVersion:String?, isActive:Boolean, createdAt:Instant?, updatedAt:Instant?
- `dto.DeviceRegisterRequest`: token:String, platform:String="android", appVersion:String?
- `dto.Product`: productId:String, name:String, genericName:String, brandName:String, manufacturer:String, category:String, dosageForm:String, strength:String, packSize:String, unit:String, hsnCode:String, gstPercent:Double, price:Double, mrp:Double, purchasePrice:Double? (null for customers), scheduleType:String (OTC|H|H1|X|NARCOTIC), requiresPrescription:Boolean, storageConditions:String, reorderLevel:Int, barcode:String, imageUrl:String?, isActive:Boolean, stock:Int, sellableStock:Int, inStock:Boolean, createdAt:Instant?, updatedAt:Instant?
- `dto.ProductCategory`: name:String, productCount:Int
- `dto.ProductCreateRequest`: name:String, price:Double, genericName:String?, brandName:String?, manufacturer:String?, category:String?, dosageForm:String?, strength:String?, packSize:String?, unit:String?, hsnCode:String? (4-8 digits), gstPercent:Double? (0-28), mrp:Double?, purchasePrice:Double?, scheduleType:String?, requiresPrescription:Boolean?, storageConditions:String?, reorderLevel:Int?, barcode:String?
- `dto.ProductUpdateRequest`: same fields as create, all nullable (name:String?, price:Double?, ...)
- `dto.Batch`: batchId:String, productId:String, productName:String, batchNo:String, expiryDate:LocalDate?, mfgDate:LocalDate?, receivedDate:LocalDate?, quantity:Int, purchasePrice:Double, mrp:Double, supplierId:String?, supplierName:String, daysToExpiry:Int?, isExpired:Boolean, isLegacyPlaceholder:Boolean, createdAt:Instant?, updatedAt:Instant?
- `dto.BatchCreateRequest`: batchNo:String, expiryDate:LocalDate, mfgDate:LocalDate?, receivedDate:LocalDate?, quantity:Int?, purchasePrice:Double?, mrp:Double?, supplierId:String?, supplierName:String?
- `dto.BatchUpdateRequest`: batchNo?, expiryDate:LocalDate?, mfgDate:LocalDate?, receivedDate:LocalDate?, quantity:Int?, purchasePrice:Double?, mrp:Double?, supplierId?, supplierName?
- `dto.StockMovement`: movementId:String, productId:String, batchId:String, changeQty:Int, reason:String, referenceId:String?, performedBy:String, createdAt:Instant?, batchNo:String?, productName:String?, productStock:Int?
- `dto.StockOperationResult`: batch:dto.Batch, movement:dto.StockMovement?, productStock:Int?, batchCreated:Boolean?
- `dto.WriteOffResult`: batch:dto.Batch, movement:dto.StockMovement
- `dto.StockPurchaseRequest`: batchNo:String, expiryDate:LocalDate, quantity:Int (min 1), mfgDate:LocalDate?, receivedDate:LocalDate?, purchasePrice:Double?, mrp:Double?, supplierId:String?, supplierName:String?, invoiceNo:String?
- `dto.StockAdjustRequest`: batchId:String, changeQty:Int (signed, non-zero), reason:String (min 3)
- `dto.Order`: orderId:String, userId:String, userName:String, productId:String, productName:String, category:String, imageUrl:String?, quantity:Int, price:Double, totalAmount:Double, status:core.OrderStatus (PENDING|APPROVED|SOLD|CANCELLED|RETURNED|UNKNOWN), message:String, doctorName:String, patientName:String, prescriptionUrl:String?, prescriptionVerified:Boolean, requiresPrescription:Boolean, cancelReason:String, createdAt:Instant?, updatedAt:Instant?, sales:List<dto.Sale>?, restocked:List<dto.RestockedLine>?, notRestocked:List<dto.RestockedLine>?
- `dto.RestockedLine`: batchId:String, batchNo:String, qty:Int
- `dto.Sale`: saleId:String, orderId:String, orderStatus:core.OrderStatus, userId:String, userName:String, productId:String, productName:String, imageUrl:String?, batchId:String, batchNo:String, quantity:Int, price:Double, gstPercent:Double, totalAmount:Double, remainingStock:Int, soldBy:String, soldAt:Instant?
- `dto.Invoice`: invoiceNumber:String, currency:String, issuedAt:Instant?, generatedAt:Instant?, pricesIncludeGst:Boolean, subtotal:Double, gstTotal:Double, grandTotal:Double, items:List<dto.InvoiceItem>, order:dto.Order, user:dto.InvoiceUser
- `dto.InvoiceItem`: productId, productName, batchId, batchNo:String, expiryDate:LocalDate?, quantity:Int, unitPrice, gstPercent, taxableAmount, gstAmount, lineTotal:Double
- `dto.InvoiceUser`: userId:String, name:String, email:String, phoneNumber:String?, address:String?, pinCode:String?
- `dto.Supplier`: supplierId:String, name:String, contactPhone:String?, email:String?, gstNumber:String?, address:String?, isActive:Boolean, createdAt:Instant?, updatedAt:Instant?
- `dto.SupplierCreateRequest`: name:String, contactPhone?, email?, gstNumber?, address?; `dto.SupplierUpdateRequest`: all String?
- `dto.DashboardSummary`: date:LocalDate?, products:Int, pendingOrders:Int, approvedOrders:Int, todaySalesCount:Int, todaySalesTotal:Double, lowStock:Int, outOfStock:Int, expiredBatches:Int, nearExpiryBatches:Int, nearExpiryDays:Int
- `dto.SalesReportRow` (unverified): key:String?, quantity:Int?, revenue:Double?, orders:Int?, productName:String?
- `dto.SalesReportSummary`: from:LocalDate?, to:LocalDate?, groupBy:String?, orders:Int, quantity:Int, revenue:Double
- `filter.SalesReportResult`: rows:List<dto.SalesReportRow>, summary:dto.SalesReportSummary?, pagination:core.Pagination?
- `dto.TopProduct` (unverified): productId:String?, productName:String?, quantity:Int?, revenue:Double?, imageUrl:String?
- `dto.AuditLog`: auditId:String, actorId:String, actorRole:String, action:String, entity:String, entityId:String, details:com.google.gson.JsonObject?, createdAt:Instant?
- `dto.AppConfig`: apiPrefix:String, apiVersion:String, docsEnabled:Boolean, enums:dto.AppConfigEnums, errorCodes:List<String>, minAppVersion:String?, otp:dto.AppConfigOtp, pagination:dto.AppConfigPagination, serverTime:Instant?, uploads:dto.AppConfigUploads
- `dto.AppConfigEnums`: accountTypes, devicePlatforms, dosageForms, orderStatuses, prescriptionSchedules, scheduleTypes, stockMovementReasons, units (all List<String>)
- `dto.AppConfigOtp`: expiresIn:Int, length:Int, maxAttempts:Int; `dto.AppConfigPagination`: defaultLimit:Int, maxLimit:Int; `dto.AppConfigUploads`: allowedMimeTypes:List<String>, imageMimeTypes:List<String>, maxRequestBytes:Long, maxUploadMb:Int
- `dto.ApiIndex`: name, version, apiVersion:String, links:List<dto.ApiLink(href:String, methods:List<String>, rel:String)>; `core.HealthResponse`: status:String?, service:String?
- `filter.UserFilter`: q:String?, isApproved:Boolean?, isBlocked:Boolean?, includeDeleted:Boolean?, updatedSince:Instant?
- `filter.ProductFilter`: q:String?, category:String?, scheduleType:String?, requiresPrescription:Boolean?, isActive:Boolean?, inStock:Boolean?, updatedSince:Instant?
- `filter.OrderFilter`: status:core.OrderStatus?, productId:String?, userId:String? (admin only), dateRange:filter.DateRange, updatedSince:Instant?
- `filter.SalesFilter`: dateRange:filter.DateRange, productId:String?, userId:String?, orderId:String?, batchId:String?, updatedSince:Instant?
- `filter.StockMovementFilter`: productId:String?, batchId:String?, reason:String?, performedBy:String?, dateRange:filter.DateRange
- `filter.AuditFilter`: entity:String?, entityId:String?, actorId:String?, action:String?, dateRange:filter.DateRange
- `filter.DateRange`: from:LocalDate?, to:LocalDate?
- `filter.ImagePart(fileName:String, mimeType:String, bytes:ByteArray)` (jpeg/png/webp); max size `cachedAppConfig()?.uploads?.maxUploadMb` (5 MB)
- `core.PageRequest(limit:Int = 20, offset:Int = 0, sort:String?, order:String?)`; `core.Pagination(limit:Int, offset:Int, total:Int)`; `fun core.PageRequest.loadMore(p: core.Pagination): core.PageRequest?` (next page or null)
- `repo.PagedData<T>(items:List<T>, pagination:core.Pagination?)`; `repo.AppException(error: err.AppError)`

## Old -> new call map
Old repo methods took no paging; old DTO wrappers (`GetAllXResponse` with `.users/.products/.orders/...`) are gone: data is now the object itself or `PagedData.items`.
- `vm.loginAdmin(email,password)` : `loginAdmin(email,password)` -> same (account_type admin is fixed in the repository); `AdminLoginResponse.admin_id` -> `LoginData.accountId` (pass to OTP screen); OTP never returned.
- `vm.verifyAdminOtp(adminId,otp)` : -> `verifyAdminOtp(accountId, otp)`; `VerifyOtpResponse.status/access_token/refresh_token/message` -> `TokenData.accessToken/refreshToken/role/accountId`; REMOVE manual `tokenManager.saveTokens(...)` (repository saves the session); then role gate (see Events & session).
- `vm.requestPasswordReset(email)` : -> `requestAdminPasswordReset(email)`; `PasswordResetResponse.user_id` -> none (keep the email for the confirm step); data = `PasswordResetRequestData.otpExpiresIn`.
- `vm.resetPasswordOtp(userId, otp, newPassword)` : `resetAdminPasswordWithOtp(adminId, ...)` -> `resetAdminPasswordWithOtp(email, otp, newPassword)` (NOTE: compiles today but the first arg must now be the EMAIL); success data `Unit?` (show `message` text yourself).
- `vm.getAllAdmin()` : `getAllAdmins()` -> `getAdmin(currentAccountId()!!)` for the logged-in profile, or `getAllAdmins(page=...)` for lists; `GetAllAdminResponse.admins` -> `PagedData<dto.Admin>.items`; `admin_id/phone_number/date_of_account_creation/image_url` -> `adminId/phoneNumber/createdAt/imageUrl`.
- `vm.updateAdmin(adminId,name,password,email,phoneNumber,image)` : -> `updateAdmin(adminId, dto.AdminUpdateRequest(name,email,phoneNumber,password,currentPassword=...), image)`; result `dto.Admin`.
- `vm.getAllSellHistory()` : -> `getAllSellHistory(filter.SalesFilter(), page)`; `GetSellHistoryResponse.sell_history: List<SellHistory>` -> `PagedData<dto.Sale>.items`; `Sell_id/Order_id/date_of_sell/isApproved` -> `saleId/orderId/soldAt/orderStatus`.
- `vm.getAllUsers()` : -> `getAllUsers(filter.UserFilter(), page)`; `GetAllUserResponse.users: List<UserItem>` -> `PagedData<dto.User>.items`; `isApproved/block/date_of_account_creation/phone_number/pin_code/user_id` -> `isApproved/isBlocked/createdAt/phoneNumber/pinCode/userId`; `password` gone.
- `vm.deleteUser(userId)` : same name -> result `dto.User` (soft delete, `isDeleted=true`).
- `vm.getAllOrders()` : -> `getAllOrders(filter.OrderFilter(), page)`; `GetAllOrdersResponse.orders: List<Order(old)>` -> `PagedData<dto.Order>.items`; `isApproved/sold` -> `status: OrderStatus`; `date_of_order_creation` -> `createdAt`; `id: Int` gone.
- `vm.getUsersOrders(userId)` : `getUserOrders(userId)` -> `getUserOrders(userId, filter.OrderFilter(), page)`; -> `PagedData<dto.Order>`.
- `vm.getOrderById(orderId)` : `getOrdersById(orderId)` -> same; `GetOrderByIdResponse.order` -> `dto.Order` (plus `sales`).
- `vm.deleteOrder(orderId)` : -> `deleteOrder(orderId, reason)` = cancel; result `dto.Order` (`status=CANCELLED`, `restocked`).
- `vm.getAllProduct()` : -> `getAllProduct(filter.ProductFilter(), page)`; `GetAllProductResponse.products: List<ProductItem>` -> `PagedData<dto.Product>.items`; `Product_id` -> `productId`; `stock` (read-only) + `sellableStock`, `inStock`, many new fields.
- `vm.addProduct(name,price,category,stock,image)` : `getAddProduct(name,price,category,stock,image)` -> `getAddProduct(dto.ProductCreateRequest(name=,price=,category=,...), filter.ImagePart?)`; DROP stock (use `purchaseStock`/`createBatch` after create); result `dto.Product`.
- `vm.updateProduct(productId,name,price,category,stock,image)` : -> `updateProduct(productId, dto.ProductUpdateRequest(name,price,category=...), image)`; DROP stock (use `adjustStock`); result `dto.Product`.
- `vm.deleteProduct(productId)` : same name, now deactivate; result `dto.Product` (`isActive=false`); offer `reactivateProduct`.
- `vm.getSpecificProduct(productId)` : same; `GetSpecificProductResponse.product` -> `dto.Product`.
- `vm.updateUser(userId,name,password,isApproved,block,address,email,phonenumber,pincode,role)` : -> `updateUser(userId, dto.UserUpdateRequest(name,email,phoneNumber,address,pinCode), image)`; approval/block -> `isApprovedUser(userId, true/false)` / `unblockUser(userId)`; password/role no longer editable by admin.
- `vm.updateOrder(orderId,isApproved,quantity,price,total_amount,product_name,message)` : -> `updateOrder(orderId, patientName, doctorName)` only; approval -> `approveOrder`; quantity/price edits are not supported by v1.
- `vm.isApprovedUser(userId, isApproveds)` : same; true = approve, false = block; result `dto.User`; add an Unblock action via `unblockUser`.
- `vm.isApproveOrder(orderId, isApproved)` : `approveOrder(orderId, isApproved)` -> `approveOrder(orderId, isApproved, reason)`; result `dto.Order` (`status`).
- `vm.setAdminLoggedOut()` : local `tokenManager.clearTokens()` -> collect `logout()` (always clears), then reset states.
- `vm.checkLoginStatus()` : `tokenManager.isLoggedIn()/getAdminId()` -> `repository.isLoggedIn()` / `currentAccountId()` / `currentRole()`.
- Not called by MyViewModel today but available: `recordSellHistory(orderId, key)` (sell), `returnOrder`, `verifyPrescription`, `getInvoice`, `getDashboardSummary` (replace Home's client-side counts).

## State rules
- Hold screen state as `core.UiState<T>`: `Loading`, `Success(data)`, `Empty` (list with no items), `Error(err.AppError)`.
- Map flows: `ResultState.Loading -> UiState.Loading`; `ResultState.Success(d) -> UiState.Success(d)` (or `Empty` if `d.items.isEmpty()`); `ResultState.Error(e) -> UiState.Error((e.exception as? repo.AppException)?.error ?: err.AppError.Unknown(e.exception))`.
- `err.AppError` = `Api(httpStatus:Int, code:err.ErrorCode, message:String, fields:Map<String,String>?)` | `Network` | `Timeout` | `Unknown(cause)`; show `error.userMessage()`.
- Branch on `ErrorCode` (never on message text): `OUT_OF_STOCK` (show `fields["quantity"]`), `PRESCRIPTION_NOT_VERIFIED` (admin: verify before sell), `INVALID_TRANSITION` / `ALREADY_SOLD` (reload order), `BATCH_EXPIRED` (pick another batch), `ACCOUNT_BLOCKED` (message + logout), `ACCOUNT_NOT_APPROVED` (awaiting approval message on login), `VALIDATION_ERROR` (form errors), `CONFLICT` (duplicate; reload), `NOT_FOUND` (resource missing or not yours).
- Form errors: `(error as AppError.Api).fields` maps field name (snake_case, e.g. `email`, `phone_number`, `new_password`) -> message; show next to the input.
- Lists: state = `items: List<T>`, `pagination: core.Pagination?`, `isLoadingMore: Boolean`, `endReached: Boolean`, `request: core.PageRequest`. First page: `PageRequest()`. Load more: `next = request.loadMore(pagination)`; `null` => `endReached = true`; else call the same method with `page = next` and append `items`.
- Order UI uses `dto.Order.status` (`core.OrderStatus`) for buttons: PENDING -> approve/reject/cancel; APPROVED -> verify prescription (if `requiresPrescription && !prescriptionVerified`), sell, cancel; SOLD -> return, invoice; RETURNED -> invoice; CANCELLED -> none.
- One-time effects: keep `Channel<UiEvent>` (`UiEvent.ShowSnackbar`) for toasts.

## Events & session
- Collect `core.AppEvents.events` (SharedFlow<core.AppEvent>) once in `NavApp` (inject `AppEvents` into the ViewModel and expose it):
  - `AppEvent.UpgradeRequired(minVersion)` -> blocking "update the app" screen.
  - `AppEvent.SessionInvalid` -> reset state, navigate to SignIn (tokens are already cleared). The legacy `TokenManager.sessionExpiredEvent` also fires.
  - `AppEvent.AccountBlocked` -> show "account blocked, contact the pharmacy", call `logout()`.
  - `AppEvent.RateLimited` -> snackbar "too many attempts, wait or request a new code".
- Role gate after `verifyAdminOtp` success: if `tokenData.role != "admin"` -> collect `logout()` and show "admin account required" (this app is admin-only).
- Login with `ACCOUNT_NOT_APPROVED` / `ACCOUNT_BLOCKED` comes as `AppError.Api.code` on `loginAdmin` / `verifyAdminOtp`.
- Startup: collect `getAppConfig()`; if `minAppVersion` > `BuildConfig.VERSION_NAME` show the update screen.
- Idempotency: for `recordSellHistory` (sell) and `purchaseStock`, call `core.IdempotencyKeyProvider.newKey()` ONCE when the user taps the button, keep it in state, reuse the same key on retry, clear it after success.

## Breakages (historical: all fixed in Part 5, see viewmodel_api.md)
Compile output (`./gradlew :app:compileDebugKotlin`), all in `app/src/main/java/com/saurabh/mediadminapp/MyViewModel.kt`:
- MyViewModel.kt:233  `repository.createAdmin(name,password,email,phoneNumber,image)` -> `createAdmin(AdminCreateRequest(...), ImagePart?)`
- MyViewModel.kt:242  `CreateAdminState(success = CreateAdminResponse)` -> `dto.Admin`
- MyViewModel.kt:261  `LoginAdminState(success = AdminLoginResponse)` -> `dto.LoginData`
- MyViewModel.kt:281  `result.data.status / access_token` (verify OTP) -> `TokenData.accessToken`; drop manual saveTokens
- MyViewModel.kt:289  `VerifyOtpState(success = VerifyOtpResponse)` -> `dto.TokenData` (line 291 `.message` gone)
- MyViewModel.kt:312  `PasswordResetState(success = PasswordResetResponse)` -> `dto.PasswordResetRequestData`
- MyViewModel.kt:332  `PasswordResetOtpState(success = PasswordResetOtpResponse)` -> `Unit?`
- MyViewModel.kt:352  `GetAllAdminState(success = GetAllAdminResponse)` -> `PagedData<dto.Admin>` (or `getAdmin`)
- MyViewModel.kt:367  `repository.updateAdmin(adminId,name,password,email,phone,image)` -> `updateAdmin(adminId, AdminUpdateRequest(...), ImagePart?)`
- MyViewModel.kt:403  `GetSellHistoryState(success = GetSellHistoryResponse)` -> `PagedData<dto.Sale>`
- MyViewModel.kt:423  `GetAllUserState(success = GetAllUserResponse)` -> `PagedData<dto.User>`
- MyViewModel.kt:465  `DeleteUserState(success = DeleteUserResponse)` -> `dto.User`
- MyViewModel.kt:487  `GetAllOrdersState(success = GetAllOrdersResponse)` -> `PagedData<dto.Order>`
- MyViewModel.kt:508  `GetUsersOrderState(success = GetUsersOrdersResponse)` -> `PagedData<dto.Order>`
- MyViewModel.kt:529  `GetOrderByIdState(success = GetOrderByIdResponse)` -> `dto.Order`
- MyViewModel.kt:571  `DeleteOrderState(success = DeleteOrderResponse)` -> `dto.Order` (cancel)
- MyViewModel.kt:593  `GetAllProductState(success = GetAllProductResponse)` -> `PagedData<dto.Product>`
- MyViewModel.kt:609  `repository.getAddProduct(name,price,category,stock,image)` -> `getAddProduct(ProductCreateRequest(...), ImagePart?)`
- MyViewModel.kt:618  `AddProductState(success = GetAddProductResponse)` -> `dto.Product`
- MyViewModel.kt:638  `repository.updateProduct(id,name,price,category,stock,image)` -> `updateProduct(id, ProductUpdateRequest(...), ImagePart?)`
- MyViewModel.kt:647  `UpdateProductState(success = UpdateProductResponse)` -> `dto.Product`
- MyViewModel.kt:695  `DeleteProductState(success = DeleteProductResponse)` -> `dto.Product` (deactivate)
- MyViewModel.kt:718  `GetSpecificProductState(success = GetSpecificProductResponse)` -> `dto.Product`
- MyViewModel.kt:738  `repository.updateUser(userId,name,password,isApproved,block,address,email,phone,pincode,role)` -> `updateUser(userId, UserUpdateRequest(...), ImagePart?)` + `isApprovedUser`/`unblockUser`
- MyViewModel.kt:778  `UpdateUserState(success = UpdateUserResponse)` -> `dto.User`
- MyViewModel.kt:797  `repository.updateOrder(orderId,isApproved,quantity,price,total,product_name,message)` -> `updateOrder(orderId, patientName, doctorName)`
- MyViewModel.kt:830  `UpdateOrderState(success = UpdateOrderResponse)` -> `dto.Order`
- MyViewModel.kt:865  `IsApprovedUserState(success = IsApproveUserResponse)` -> `dto.User`
- MyViewModel.kt:906  `ApproveOrderState(success = ApproveOrderResponse)` -> `dto.Order`
Screens reading the old DTO fields (state `.success?.users`, `Product_id`, `isApproved`, `sold`, ...) will break next, once MyViewModel compiles; migrate them with the field map in "Old -> new call map".

## Hilt
- `repo.Repository @Inject constructor(authApi, metaApi, healthApi, usersApi, adminsApi, devicesApi, productsApi, inventoryApi, ordersApi, salesApi, suppliersApi, reportsApi, auditApi: com.saurabh.mediadminapp.data.v1.api.*, tokenManager: com.saurabh.mediadminapp.network.TokenManager)`. All APIs come from `V1ApiModule` (no qualifier at the injection site); TokenManager from `ApiProvider`. Not `@Singleton` (app-config cache is process-wide in a companion).
- ViewModel injects: `repo.Repository`, `core.AppEvents` (@Singleton), `core.IdempotencyKeyProvider` (@Singleton). `TokenManager` only if still needed for legacy screens; prefer `repository.isLoggedIn()/currentRole()/currentAccountId()`.
