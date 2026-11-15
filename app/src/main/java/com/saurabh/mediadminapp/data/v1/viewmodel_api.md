# MyViewModel API (v1, ADMIN APP ONLY)

UI work needs only this file + `ui_followups.md`. Types: `dto.` = `com.saurabh.mediadminapp.data.v1.dto`, `PagedData<T>(items: List<T>, pagination: Pagination?)` = `com.saurabh.mediadminapp.repository.PagedData`, `Pagination(limit, offset, total)` and `UiState` = `com.saurabh.mediadminapp.data.v1.core`, `AppError` = `com.saurabh.mediadminapp.data.v1.error.AppError`, `ImagePart` = `com.saurabh.mediadminapp.data.v1.repository.ImagePart`, `OrderStatus` = `com.saurabh.mediadminapp.data.v1.core.OrderStatus`. DTO fields: see `viewmodel_migration.md` "Type reference".

## 1. Hilt
| Item | Value |
|---|---|
| Annotation | `@HiltViewModel class MyViewModel` |
| Constructor | `@Inject constructor(repository: Repository, appEvents: AppEvents, idempotencyKeyProvider: IdempotencyKeyProvider)` |
| Removed deps | `TokenManager` (use `repository.isLoggedIn()/currentAccountId()`), `okhttp3.MultipartBody` (images are `ImagePart`) |

## 2. State flows
All `XState` keep `isLoading: Boolean`, `success: T?`, `error: String?`. `+appError` = `appError: AppError?`. `+list` = `pagination: Pagination?`, `isLoadingMore: Boolean`, `endReached: Boolean`.

| Flow | Type | success type + extra fields |
|---|---|---|
| `isAdminLoggedIn` | `StateFlow<Boolean?>` | null = checking |
| `loggedInAdminId` | `StateFlow<String?>` | account id of the session |
| `upgradeRequiredVersion` | `StateFlow<String?>` | non-null = force update ("latest" when server sent no version) |
| `appConfigError` | `StateFlow<String?>` | non-null = GET /app-config failed and nothing cached (Splash Retry -> getAppConfig()) |
| `createAdminState` | `StateFlow<CreateAdminState>` | `dto.Admin` +appError |
| `loginAdminState` | `StateFlow<LoginAdminState>` | `dto.LoginData` +appError |
| `getAllAdminState` | `StateFlow<GetAllAdminState>` | `PagedData<dto.Admin>` +list (first page limit 100) |
| `specificAdminState` | `StateFlow<AdminState>` | `dto.Admin` (not written by any function yet) |
| `verifyOtpState` | `StateFlow<VerifyOtpState>` | `dto.TokenData` +appError |
| `passwordResetState` | `StateFlow<PasswordResetState>` | `dto.PasswordResetRequestData` +appError |
| `passwordResetOtpState` | `StateFlow<PasswordResetOtpState>` | `Unit` (non-null = success) +appError |
| `getAllUserState` | `StateFlow<GetAllUserState>` | `PagedData<dto.User>` +list |
| `getAllProduct` | `StateFlow<GetAllProductState>` | `PagedData<dto.Product>` +list |
| `addProductState` | `StateFlow<AddProductState>` | `dto.Product` +appError |
| `updateProductState` | `StateFlow<UpdateProductState>` | `dto.Product` +appError |
| `deleteProductState` | `StateFlow<DeleteProductState>` | `dto.Product` (deactivated) |
| `getSpecificProductState` | `StateFlow<GetSpecificProductState>` | `dto.Product` |
| `isApprovedUser` | `StateFlow<Map<String, IsApprovedUserState>>` | key = userId; `dto.User` (approve/block/unblock) |
| `updateOrderState` | `StateFlow<Map<String, UpdateOrderState>>` | key = orderId; `dto.Order` +appError |
| `deleteUserState` | `StateFlow<DeleteUserState>` | `dto.User` |
| `updateUserState` | `StateFlow<UpdateUserState>` | `dto.User` +appError |
| `getAllOrderState` | `StateFlow<GetAllOrdersState>` | `PagedData<dto.Order>` +list |
| `getUsersOrdersState` | `StateFlow<GetUsersOrderState>` | `PagedData<dto.Order>` (first page limit 100) |
| `deleteOrderState` | `StateFlow<DeleteOrderState>` | `dto.Order` (cancelled) +appError |
| `isApproveOrdder` | `StateFlow<Map<String, ApproveOrderState>>` | key = orderId; `dto.Order` +appError |
| `getSellHistory` | `StateFlow<GetSellHistoryState>` | `PagedData<dto.Sale>` +list |
| `getRecordSellHistory` | `StateFlow<Map<String, RecordSellHistoryState>>` | key = orderId (sell); `dto.Order` +appError |
| `returnOrderState` | `StateFlow<Map<String, UpdateOrderState>>` | key = orderId (return); `dto.Order` +appError |
| `verifyPrescriptionState` | `StateFlow<Map<String, UpdateOrderState>>` | key = orderId; `dto.Order` +appError |
| `getProductSellHistory` | `StateFlow<GetProductSellHistoryState>` | `PagedData<dto.Sale>` (not written yet) |
| `getUserSellHistory` | `StateFlow<GetUserSellHistoryState>` | `PagedData<dto.Sale>` (not written yet) |
| `getOrderByIdState` | `StateFlow<GetOrderByIdState>` | `dto.Order` (incl. `sales`) |
| `invoiceState` | `StateFlow<UiState<dto.Invoice>>` | initial `UiState.Empty` |
| `dashboardSummaryState` | `StateFlow<UiState<dto.DashboardSummary>>` | initial `UiState.Empty` |
| `uiEvent` | `Flow<UiEvent>` | `UiEvent.ShowSnackbar(message)` one-shot |

## 3. Functions
| fn(params) | Status | State written | Side effects | Guards |
|---|---|---|---|---|
| `checkLoginStatus()` | UPDATED | isAdminLoggedIn, loggedInAdminId | reads repository session | - |
| `setAdminLoggedIn()` | UPDATED | same | - | - |
| `setAdminLoggedOut()` | UPDATED | all reset | `repository.logout()` (device unregister + token revoke), always clears | - |
| `getAppConfig()` | NEW | upgradeRequiredVersion | called in init; compares min_app_version with `BuildConfig.VERSION_NAME` | - |
| `createAdmin(name, email, password, phoneNumber, image: ImagePart? = null)` | UPDATED | createAdminState | blank phone sent as null | isLoading |
| `loginAdmin(email, password)` | UPDATED | loginAdminState | - | isLoading |
| `verifyAdminOtp(accountId, otp)` | UPDATED | verifyOtpState | session saved by repository; role != admin -> logout + error "Admin account required"; else setAdminLoggedIn() | isLoading |
| `requestPasswordReset(email)` | UPDATED | passwordResetState | remembers email | isLoading |
| `lastResetEmail(): String?` | NEW | - | email remembered by requestPasswordReset (prefill) | - |
| `resetPasswordOtp(email: String? = null, otp, newPassword)` | UPDATED | passwordResetOtpState | email defaults to the remembered one | isLoading |
| `getAllAdmin(force = false)` | UPDATED | getAllAdminState | resets admin page | cached unless force |
| `loadMoreAdmins()` | NEW | getAllAdminState (append) | - | busy / endReached |
| `updateAdmin(adminId, name? = null, email? = null, phoneNumber? = null, image: ImagePart? = null)` | UPDATED | - | snackbar; getAllAdmin(force) | - |
| `getAllSellHistory(force = false)` | UPDATED | getSellHistory | resets page | cached unless force |
| `loadMoreSellHistory()` | NEW | getSellHistory (append) | - | busy / endReached |
| `getAllUsers(force = false)` | UPDATED | getAllUserState | resets page | cached unless force |
| `loadMoreUsers()` | NEW | getAllUserState (append) | - | busy / endReached |
| `deleteUser(userId)` | UPDATED | getAllUserState (optimistic remove/rollback), deleteUserState | snackbar | - |
| `updateUser(userId, name? , address?, email?, phonenumber?, pincode?, image: ImagePart? = null)` | UPDATED | updateUserState; row replaced in getAllUserState | snackbar | - |
| `isApprovedUser(userId, isApproveds)` | UPDATED | isApprovedUser[userId]; row replaced | true = approve, false = BLOCK; snackbar | - |
| `unblockUser(userId)` | NEW | isApprovedUser[userId]; row replaced | snackbar | - |
| `getAllOrders(force = false)` | UPDATED | getAllOrderState | resets page | cached unless force |
| `loadMoreOrders()` | NEW | getAllOrderState (append) | - | busy / endReached |
| `getUsersOrders(userId, force = false)` | UPDATED | getUsersOrdersState | - | same id cached |
| `getOrderById(orderId, force = false)` | UPDATED | getOrderByIdState | - | same id cached |
| `deleteOrder(orderId, reason: String? = null)` | UPDATED | deleteOrderState; row replaced (status CANCELLED) | invalidates sales; refreshes products; snackbar | isLoading |
| `updateOrder(orderId, patientName? = null, doctorName? = null)` | UPDATED | updateOrderState[orderId]; row replaced | snackbar | - |
| `isApproveOrder(orderId, isApproved, reason: String? = null)` | UPDATED | isApproveOrdder[orderId]; row replaced | true = approve, false = reject; invalidates sales; refreshes products; snackbar | - |
| `recordSell(orderId)` | NEW | getRecordSellHistory[orderId]; row replaced | Idempotency-Key per order (reused on retry, dropped on success); invalidates sales; refreshes orders + products | in-flight per order |
| `returnOrder(orderId, reason: String? = null)` | NEW | returnOrderState[orderId]; row replaced | invalidates sales; refreshes products | in-flight per order |
| `verifyPrescription(orderId, verified = true)` | NEW | verifyPrescriptionState[orderId]; row replaced | snackbar | in-flight per order |
| `getInvoice(orderId)` | NEW | invoiceState | - | - |
| `getDashboardSummary()` | NEW | dashboardSummaryState | - | - |
| `getAllProduct(force = false)` | UPDATED | getAllProduct | resets page | cached unless force |
| `loadMoreProducts()` | NEW | getAllProduct (append) | - | busy / endReached |
| `addProduct(name, price: Double, category, image: ImagePart? = null)` | UPDATED | addProductState | refreshes products | isLoading |
| `updateProduct(productId, name? = null, price: Double? = null, category? = null, image: ImagePart? = null)` | UPDATED | updateProductState | refreshes products; invalidates detail cache | isLoading |
| `deleteProduct(productId)` | UPDATED | getAllProduct (optimistic remove/rollback), deleteProductState | deactivate; snackbar | - |
| `reactivateProduct(productId)` | NEW | getSpecificProductState (if same id) | refreshes products; snackbar | - |
| `getSpecificProduct(productId, force = false)` | UPDATED | getSpecificProductState | - | same id cached |
| `clearApprovalState(userId? = null)`, `resetDeleteUserState()`, `clearAddProductState()`, `clearUpdateProductState()`, `clearGetSpecificProductState()`, `clearDeleteProductState()`, `clearGetAllProductState()`, `clearGetUsersOrdersState()`, `clearGetOrderByIdState()`, `clearUpdateOrderSteate()`, `clearGetAllOrdersState()`, `clearLoginState()`, `clearVerifyOtpState()` | unchanged | reset the named state | - | - |

## 4. Removed / changed params
| Function | Old | New |
|---|---|---|
| createAdmin / updateAdmin / addProduct / updateProduct / updateUser | `image: MultipartBody.Part?` | `image: ImagePart?` (`Uri.toImagePart(context)`) |
| verifyAdminOtp | `adminId` | `accountId` (= `LoginData.accountId`) |
| resetPasswordOtp | `userId: String?` (admin id) | `email: String?` |
| updateAdmin | `password` | removed |
| updateUser | `password`, `isApproved`, `block` | removed (use isApprovedUser / unblockUser) |
| addProduct / updateProduct | `stock` | removed |
| updateOrder | `isApproved, quantity, price` | `patientName, doctorName` |
| isApproveOrder | `(orderId, isApproved)` | `+ reason: String? = null` |
| deleteOrder | `(orderId)` | `+ reason: String? = null` (cancel, not delete) |

## 5. UI field map (old -> new)
| Old | New |
|---|---|
| `success?.users / .products / .orders / .admins / .sell_history` | `success?.items` |
| `GetOrderByIdState.success?.order`, `GetSpecificProductState.success?.product` | `success` |
| `Product_id`, `product_id` | `productId` |
| `user_id`, `order_id`/`Order_id`, `Sell_id`, `admin_id` | `userId`, `orderId`, `saleId`, `adminId` |
| `product_name`, `user_name`, `image_url`, `phone_number`, `pin_code`, `total_amount`, `remaining_stock` | `productName`, `userName`, `imageUrl`, `phoneNumber`, `pinCode`, `totalAmount`, `remainingStock` |
| `UserItem.block` | `User.isBlocked` |
| `Order.isApproved` / `Order.sold` | `status` (`OrderStatus.PENDING/APPROVED/SOLD/CANCELLED/RETURNED/UNKNOWN`) |
| `SellHistory.isApproved` | `Sale.orderStatus` |
| `date_of_sell` / `date_of_order_creation` / `date_of_account_creation` | `soldAt` / `createdAt` (`java.time.Instant?`) |
| `AdminLoginResponse.admin_id/status/message` | `LoginData.accountId/otpRequired` |
| `VerifyOtpResponse.access_token/status/message` | `TokenData.accessToken/role` |
| `XResponse.message` (toasts) | not returned; UI shows its own text |

## 6. UI call sites changed
| File | Change |
|---|---|
| AboutScreen.kt | counts from `.items` |
| AddProductScreen.kt | toast uses product name; `Uri.toImagePart`; stock no longer passed |
| HistroyScreen.kt | `.items`; day/month/year grouping on `soldAt` (ISO date); status from `orderStatus == SOLD`; field renames |
| HomeScreen.kt | `.items`; `isBlocked`; field renames |
| NewOrder.kt | `.items`; approved = status APPROVED or SOLD; field renames |
| OrderDetailsScreen.kt | same as NewOrder |
| OtpScreen.kt | success = `accessToken.isNotBlank()`; fixed error text |
| ProductScreen.kt | `.items`; `productId`, `imageUrl` |
| ProfileScreen.kt | admin from `.items` by `adminId`; `imageUrl`, `phoneNumber`, `createdAt` |
| SignIn.kt | navigate when `otpRequired` with `accountId` |
| SignUp.kt | `Uri.toImagePart` |
| SpecificOrderScreen.kt | `success` is the order; status from `OrderStatus`; `createdAt`; field renames |
| SpecificProductScreen.kt | `success` is the product; `imageUrl`, `productId` |
| UpdateProductScreen.kt | `success` is the product; `imageUrl`; `Uri.toImagePart`; stock not sent |
| UpdateUserDetailsScreen.kt | `.items`/`userId`; `isBlocked` init; updateUser without isApproved/block |
| UserDetailsScreen.kt | `.items`/`userId`; `isBlocked`; `createdAt`; fixed delete toast |
| nav/NavApp.kt | admin from `.items` by `adminId` (`dto.Admin`) |
| components/EachOrderCard.kt | `dto.Order`; approved from `status` |
| components/SalesCard.kt | `dto.Sale`; `orderStatus`, `remainingStock`, `soldAt` |

## 7. Events
| Topic | Behavior |
|---|---|
| `upgradeRequiredVersion` | set by app-config check at init or `AppEvent.UpgradeRequired` (426); UI should block with an update screen |
| SessionInvalid | `resetAllStatesOnLogout()` -> `isAdminLoggedIn = false` (NavApp goes to SignIn) |
| AccountBlocked | snackbar "Account blocked, contact the pharmacy" + `setAdminLoggedOut()` |
| RateLimited | snackbar "Too many attempts, please wait" |
| Role gate | verifyAdminOtp: role != "admin" -> logout + `verifyOtpState.error = "Admin account required"` |
| Idempotency | `recordSell(orderId)`: one key per order from `IdempotencyKeyProvider`, reused on retry, removed on success |
| Form errors | `appError` on form states: `(appError as? AppError.Api)?.fields` (snake_case keys) and `.code` (`ErrorCode`) |

## 8. Helpers
| Name | File | Signature |
|---|---|---|
| collectInto | MyViewModel.kt (private) | `suspend fun <T> Flow<ResultState<T>>.collectInto(onLoading: () -> Unit = {}, onError: suspend (String?, AppError?) -> Unit, onSuccess: suspend (T) -> Unit)` |
| replaceOrderRow | MyViewModel.kt (private) | `fun replaceOrderRow(order: dto.Order)` (all orders, user orders, detail; keeps detail `sales`) |
| toImagePart | utils/utilityFunctions/UriExtensions.kt | `fun Uri.toImagePart(context: Context): ImagePart?` |
| removeItem / restoreItem / updateItem | utils/ListExtensions.kt | unchanged |

## 9. Remaining errors
none (`:app:compileDebugKotlin` clean; unit tests 33/33 pass)
