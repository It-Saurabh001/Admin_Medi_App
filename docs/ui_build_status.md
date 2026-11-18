# UI build status (ledger, ONLY handoff file)

Spec: `app/src/main/java/com/saurabh/mediadminapp/screens and navigation.md` | VM: `data/v1/viewmodel_api.md`, `data/v1/ui_followups.md` | API: `app/src/main/java/com/saurabh/mediadminapp/response.md` | Skill: `.claude/skills/admin-screen-builder/SKILL.md`
Design links: none provided (placeholders). Designs follow M3 + existing ui/theme tokens; drop exports in `docs/design/<screen>.png` to refine.
Nav style: string routes (`Routes.X.route`, `@Serializable` objects); only `VerifyOtpRoutes` is typed.

## Screens
| # | screen | file | route | VM fns/states | design | status | notes |
|---|---|---|---|---|---|---|---|
| 1 | Splash | ui/screens/SplashScreen.kt | SplashRoutes | getAppConfig, upgradeRequiredVersion, isAdminLoggedIn, appConfigError | - | DONE | config fail w/o cache -> error + Retry; device: none |
| 2 | Login | ui/screens/SignIn.kt | SignInRoutes | loginAdmin, loginAdminState, clearLoginState | - | DONE | Clay helpers removed; 'Create account' -> AdminSignUp; client validation, IME Done submits, BLOCKED->AccountBlocked, NOT_APPROVED/RATE_LIMITED msgs |
| 2a | AdminSignUp | ui/screens/AdminSignUpScreen.kt | AdminRegisterRoutes (auth route) | registerAdmin, registerAdminState, clearRegisterAdminState, adminSignupConfig | - | DONE | POST /admins/register; link on Login unless app-config admin_signup.enabled=false; signup-code field when code_required or 403; owner/approved -> Login msg, else 'Awaiting approval'; device: none |
| 3 | Otp | ui/screens/OtpScreen.kt | VerifyOtpRoutes(userId) | verifyAdminOtp, verifyOtpState, cachedAppConfig().otp | - | DONE | boxes (1 hidden field), TTL/len/attempts from app-config (300/6/5 fallback), paste, haptics, auto-verify; resend/RATE_LIMITED/5 fails -> Login; device: clipboard+haptics |
| 4 | ForgotPassword | ui/screens/PasswordResetScreens.kt | ForgotPasswordRoutes | requestPasswordReset, passwordResetState | - | DONE | email format check, IME Done submits, RATE_LIMITED msg, field disabled while loading; device: none |
| 5 | ResetPassword | ui/screens/PasswordResetScreens.kt | ResetPasswordRoutes | resetPasswordOtp(email,otp,pw), passwordResetOtpState, lastResetEmail | - | DONE | email field (prefilled), pw 8-128, IME Done submits, RATE_LIMITED msg + 'Request a new code' -> back; device: none |
| 6 | UpgradeRequired | ui/screens/AccountStatusScreens.kt | UpgradeRequiredRoutes | upgradeRequiredVersion | - | DONE | back disabled; market:// -> https fallback -> toast; device: ACTION_VIEW |
| 7 | AccountBlocked | ui/screens/AccountStatusScreens.kt | AccountBlockedRoutes | setAdminLoggedOut | - | DONE | Log out + back both logout -> SignIn popUpTo(0); device: none |
| 8 | Home | ui/screens/nav/dashboard.kt (DashboardScreen) | HomeRoutes | getDashboardSummary, dashboardSummaryState | - | DONE | AppScreenScaffold+StatCard grid (2 sections, 9 counters), skeleton, keeps last data on refresh/error, tab-style nav; stock tiles -> Products until InventoryAlerts, pending tile has no status filter yet; device: none |
| 9 | Reports | ui/screens/ReportsScreen.kt | ReportsRoutes | loadSalesReport(query?, force), loadMoreSalesReport, salesReportState | - | DONE | range presets 7/30/90 + DateRangePicker, day/product, sort+order, summary StatCards, paged rows w/ share bar; EXPANDED = filter pane + list; entry: Dashboard top-bar Insights; device: none |
| 10 | TopProducts | ui/screens/TopProductsScreen.kt | TopProductsRoutes | loadTopProducts(query?, force), loadMoreTopProducts, topProductsState | - | DONE | shares ReportFilters; ranked rows + Coil thumb, tap -> ProductDetail; DTO fields unverified; entry: Reports top-bar; device: none |
| 11 | ProductList | ui/screens/ProductScreen.kt | ProductRoutes | getAllProduct, loadMoreProducts, productFilter/setProductFilter, productSort, productCategories/loadProductCategories | - | DONE | server-side search (400 ms debounce) + filters (in_stock, is_active tri-state, schedule, category w/ counts), sort menu; adaptive grid 1/2/3 cols (320dp cells), pull-to-refresh, load-more, FAB hides on scroll; scanner action; device: none |
| 12 | BarcodeScanner | ui/screens/BarcodeScannerScreen.kt | BarcodeScannerRoutes | lookupBarcode, clearBarcodeLookup, barcodeLookup | - | DONE | CameraX+ML Kit (lifecycle unbind, torch, haptic, 2.5 s same-code debounce), manual entry; Found -> ProductDetail (pops scanner), 404 -> Add product w/ savedStateHandle `prefill_barcode`; portrait stacked / landscape+EXPANDED side panel; device: CAMERA (asked on entry, rationale, settings) |
| 13 | ProductDetail | ui/screens/SpecificProductScreen.kt | SpecificProductRoutes | getSpecificProduct, deleteProduct, reactivateProduct | - | DONE | PTR; Add stock button removed (stock is added on Add product or the future StockPurchase screen) |
| 14 | AddEditProduct | ui/screens/ProductForm.kt (shared), AddProductScreen.kt, UpdateProductScreen.kt | AddProductRoutes / UpdateProductRoutes | addProduct(ProductCreateRequest, image, openingStock?), updateProduct(id, ProductUpdateRequest, image), cachedAppConfig().enums | - | DONE | all 19 fields; dropdowns from app-config enums (dosage, unit, schedule); Rx auto from schedule; edit sends only changed fields (cleared optional text stays unchanged); camera/gallery+uCrop; Add has optional opening stock + scanner barcode prefill; Edit has Stock section (pick batch -> new qty + reason -> stock/adjust, or receive a new batch -> stock/purchase), runs before the product PATCH; device: camera app via FileProvider |
| 15 | BatchList | ui/screens/BatchListScreen.kt | BatchListRoutes(productId) | loadProductBatches, productBatches | - | DONE | expired red / near-expiry (<=90 d) tertiary, filters, PTR, FAB Receive stock, Adjust action; entry: ProductDetail 'View batches'; device: none |
| 16 | BatchDetail | ui/screens/BatchDetailScreen.kt | BatchDetailRoutes(batchId) | getBatch, batchDetail, updateBatch, writeOffBatch, batchAction, clearBatchAction | - | DONE | edit metadata (batch_no+expiry always sent, others if changed), DateField, write-off w/ note + ConfirmDialog, 'Adjust quantity' -> StockAdjust preselected; device: none |
| 17 | StockPurchase | ui/screens/StockPurchaseScreen.kt | StockPurchaseRoutes(productId) | purchaseStock, stockPurchaseState | - | DONE | StockEntryFields + supplier name + invoice no; Idempotency-Key kept on retry; device: none |
| 18 | StockAdjust | ui/screens/StockAdjustScreen.kt | StockAdjustRoutes(productId) | adjustStock, productBatches | - | DONE | batch dropdown (preselect via savedStateHandle `adjust_batch_id`), add/remove segmented, result preview, no below-zero, quick reasons; device: none |
| 19 | StockMovements | ui/screens/StockMovementsScreen.kt | StockMovementsRoutes(productId?, batchId?) | loadStockMovements, loadMoreStockMovements, stockMovements, stockMovementFilter | - | DONE | reason chips, removable product/batch scope chip, signed qty (Green in / Red out), row -> BatchDetail; entry: drawer, Product details, Batch details |
| 20 | OrderList | ui/screens/OrderListScreen.kt | OrdersRoutes / EachUserOrderRoutes | getAllOrders, loadMoreOrders | - | DONE | M3 list: search, status chips, PagedLazyColumn (PTR, load-more); old NewOrder.kt/OrderDetailsScreen.kt now unused |
| 21 | OrderDetail | ui/screens/SpecificOrderScreen.kt | SpecificOrderRoutes | getOrderById, isApproveOrder, recordSell, returnOrder, deleteOrder, verifyPrescription | - | DONE | GreenShield; approve/reject only |
| 22 | PrescriptionViewer | ui/screens/PrescriptionViewerScreen.kt | PrescriptionRoutes(orderId) | getOrderById, getOrderByIdState, verifyPrescription, verifyPrescriptionState | - | DONE | zoom (pinch/double-tap, Fit), PDF -> ACTION_VIEW, Verify primary, Unverify red link + confirm; entry: Order details "View prescription"; device: ACTION_VIEW |
| 23 | Invoice | ui/screens/InvoiceScreen.kt | InvoiceRoutes(orderId) | getInvoice, invoiceState | - | DONE | header, bill-to, lines (Teal batch pill, expiry, GST), totals; Share = ACTION_SEND text; entry: Order details top bar (SOLD/RETURNED); device: ACTION_SEND |
| 24 | InventoryAlerts | ui/screens/InventoryAlertsScreen.kt | InventoryAlertsRoutes(tab) | loadInventoryAlert, loadMoreInventoryAlert, lowStock, outOfStock, nearExpiry, expiredBatches | - | DONE | PrimaryTabRow 4 tabs with totals, near-expiry 30/60/90 d chips; products -> ProductDetail, batches (shared BatchRow) -> BatchDetail; entry: drawer + Dashboard stock tiles (deep-link tab); bottom-nav tab NOT changed |
| 25 | MoreMenu | nav/NavApp.kt (AppDrawerSheet) | - | setAdminLoggedOut | - | DONE | ModalNavigationDrawer replaces the More sheet: header (admin), Manage (Reports, Top products, Scan, Admins), Account (Profile, Settings, About), Log out; opened by menu button, More tab, edge swipe on tab roots |
| 26 | SalesHistory | ui/screens/SalesHistoryScreen.kt | HistoryRoutes | getAllSellHistory, loadMoreSellHistory | - | DONE | grouped by day with sticky headers, PTR, load-more; old HistroyScreen.kt unused |
| 27 | UserList | ui/screens/UserListScreen.kt | UsersRoutes | getAllUsers, loadMoreUsers | - | DONE | GreenShield; search + status chips; entry: drawer "Customers" |
| 28 | UserDetail | ui/screens/UserDetailsScreen.kt | UserDetailsRoutes | isApprovedUser, unblockUser, deleteUser, getUsersOrders | - | DONE | GreenShield; approve/block/unblock/delete |
| 29 | UserEdit | ui/screens/UpdateUserDetailsScreen.kt | UpdateUserDetailsRoutes | updateUser | - | DONE | GreenShield; profile fields only |
| 30 | AdminList | ui/screens/AdminListScreen.kt | AdminListRoutes | getAllAdmin, loadMoreAdmins, approveAdmin, deleteAdmin, adminActionIds, loggedInAdminId | - | DONE | pending signups (Approve / Reject via ConfirmDialog) + admins; Owner/Pending/Disabled badges; owner & self not removable; entry: More sheet 'Admins'; device: none |
| 31 | AdminDetailEdit | ui/screens/AdminFormScreen.kt | AdminFormRoutes(adminId / "new") | getAllAdmin, updateAdmin, approveAdmin, deleteAdmin, adminActionIds, createAdmin, createAdminState | - | DONE | detail+edit (changed fields only), approve/reject pending, remove (not owner/self), create form; entry: Admins row / FAB; SignUp.kt untouched |
| 32 | SupplierList | ui/screens/SupplierScreens.kt (SupplierListScreen) | SuppliersRoutes | loadSuppliers, loadMoreSuppliers, suppliers, supplierQuery | - | DONE | server search (400 ms), Active/Inactive/All, GsFab; entry: drawer |
| 33 | SupplierDetailEdit | ui/screens/SupplierScreens.kt (SupplierFormScreen) | SupplierFormRoutes(id / "new") | getSupplier, supplierDetail, createSupplier, updateSupplier, deactivateSupplier, supplierAction, clearSupplierAction | - | DONE | GSTIN 15-char Ascii+CAPS check, changed fields only (name always), deactivate + confirm, inactive = read-only |
| 34 | AuditLog | ui/screens/AuditLogScreen.kt | AuditLogRoutes | loadAuditLogs, loadMoreAuditLogs, auditLogs, auditFilter | - | DONE | entity chips, action pills by verb family, tap row expands details; entry: drawer |
| 35 | AdminProfile | ui/screens/ProfileScreen.kt | ProfileRoutes | getAllAdmin, updateAdmin, setAdminLoggedOut | - | DONE | GreenShield; logout confirm |
| 36 | Settings/About | ui/screens/SettingsScreen.kt, AboutScreen.kt | SettingsRoutes, AboutRoutes | - | - | DONE | GreenShield |

## Adaptive helpers (ui/adaptive/Adaptive.kt)
- `enum WindowSize { COMPACT, MEDIUM, EXPANDED }`; `data class WindowInfo(size, isLandscape, widthDp)` with `isCompact`, `isExpanded`
- `@Composable rememberWindowSize(): WindowInfo` (LocalConfiguration; no adaptive dependency)
- `AdaptiveWidths.Form = 560.dp`, `AdaptiveWidths.List = 840.dp`
- `AdaptiveContent(modifier, maxWidth = List, content: BoxScope)` centered, capped width
- `adaptiveGridCells(minCellWidth = 160.dp): GridCells`
- `TwoPane(showDetail, list, detail, modifier, emptyDetail)` (Row 42/58 on EXPANDED; single pane otherwise; caller hoists selection with rememberSaveable)

## Navigation decisions
- NavApp shell: COMPACT bottom NavigationBar, MEDIUM/EXPANDED NavigationRail (tab roots only). Tabs today: Dashboard, Products, Orders, History, More (opens navigation drawer). Inventory tab replaces History when InventoryAlerts exists; History moves into MoreMenu.
- Tab switch: `popUpTo(Home){saveState}; launchSingleTop; restoreState`; re-tap inside a tab pops to its root; BackHandler on Products/Orders/History roots -> Dashboard; back on Dashboard exits.
- Scaffold bottom padding applied to the NavHost and consumed (`consumeWindowInsets`) so the nav bar never covers the last item.
- Transitions: enter fade+slide 250 ms, exit fade 200 ms (pop mirrored).
- Guards (NavApp only): `isAdminLoggedIn` null -> nothing (system splash), false -> SignIn `popUpTo(0)`, true on auth route -> Home `popUpTo(0)`; `upgradeRequiredVersion` -> UpgradeRequired `popUpTo(0)`. Snackbars from `uiEvent` (AccountBlocked, RateLimited, SessionInvalid via VM).
- Predictive back: `android:enableOnBackInvokedCallback="true"` already in manifest.

## Theme tokens
- `MediAdminAppTheme(darkTheme, dynamicColor ignored)` in ui/theme/Theme.kt; light + dark schemes from Part 0 (teal brand); M3 default Typography + Shapes.
- `MaterialTheme.statusColors.of(OrderStatus)` -> `StatusColor(container, content)`.
- `BrandColors.*` small accents only. Legacy Clay* in ClayTokens.kt (delete when unused).
- No design exports provided, so no new tokens added.

## Shared components
- ui/components/StateComponents.kt: `LoadingState(modifier)`, `EmptyState(title, message, modifier, action?)`, `ErrorState(message, onRetry, modifier)`, `SectionHeader(text, modifier)`, `ConfirmDialog(title, message, confirmText, onConfirm, onDismiss, dismissText = "Cancel", destructive = true)`
- ui/components/StatusChip.kt: `StatusChip(status: OrderStatus, modifier)`, `OrderStatus.label()`
- ui/components/AppScaffolds.kt:
  - `AppScreenScaffold(title, modifier, onBack?, navigationIcon?, actions, scrollBehavior = pinned, floatingActionButton, bottomBar, snackbarHostState?, content: (PaddingValues))` (TopAppBar + nestedScroll + ConnectivityBanner)
  - `ConnectivityBanner(modifier, onRetry?)`
  - `ScreenStateHost(isLoading, error, isEmpty, onRetry, modifier, emptyTitle, emptyMessage, content)` (skeleton / error+Retry / empty / content)
  - `SkeletonList(rows = 6, modifier)`
  - `PagedLazyColumn(isRefreshing, onRefresh, isLoadingMore, endReached, onLoadMore, modifier, state, contentPadding (bottom 96dp), verticalArrangement, content: LazyListScope)` (pull-to-refresh, snapshotFlow load-more, footer spinner, 840dp cap)
  - `FormScaffold(modifier, content: ColumnScope)` (verticalScroll + imePadding + navigationBarsPadding, 560dp cap)
  - `Map<String,String>?.fieldError(key)`, `FieldError(text)`
  - `StatCard(label, value, icon, onClick?, modifier, highlight = false)`
  - `ImageSourceSheet(onDismiss, onCamera, onGallery, onRemove? = null, title = "Add image")` (camera row hidden without camera)
- ui/screens/AuthCommon.kt (internal): `AppError?.fieldError(key)`, `AuthFormColumn`, `SupportingText`, `PasswordField(..., imeAction, onDone?)`, `AuthHeader`, `AuthFooterLink`, `AuthFormError`, `AuthPrimaryButton`, `AUTH_MESSAGE_KEY`
- ui/screens/AddProductScreen.kt (internal): `ProductFormScaffold(...)`
- ui/components/FormParts.kt: `DateField(label, iso, onIsoChange, enabled, error)`, `SaveBar(text, loading, onClick, enabled)`, `LocalDate?.showDate()`
- ui/components/StockEntry.kt: `rememberStockDraft()`, `StockEntryFields(draft, enabled)`, `ReceiveStockSheet(productName, isLoading, error, serverFields, success, onSubmit, onDismiss)`; ui/components/ListParts.kt: `ThumbImage(url, fallback, size)`, `Instant?.dayLabel()/dateTimeLabel()`
- ui/screens/ReportsScreen.kt (internal): `ReportFilters(query: ReportQuery, onQueryChange, showGroupBy, sortOptions: List<Pair<key,label>>)`, `rememberInrFormat()`

## Device access
| feature | screen | mechanism | permission | why |
|---|---|---|---|---|
| API calls | all | Retrofit | INTERNET (existed) | network |
| Offline banner | all (AppScreenScaffold) | `ConnectivityObserver.observe(context)` network callback, `rememberIsOnline()` | ACCESS_NETWORK_STATE (added) | read network state |
| Barcode scan | BarcodeScanner | CameraX 1.4.2 + ML Kit barcode-scanning 17.3.0 (added) | CAMERA (added), requested on tap | live camera preview |
| Photo capture | AddEditProduct, UserEdit, AdminDetailEdit, AdminProfile, OrderDetail Rx | `TakePicture` + FileProvider `com.saurabh.mediadminapp.provider` (cache-path, existed) | none extra | system camera app writes to our Uri |
| Gallery image | same | Photo Picker `PickVisualMedia(ImageOnly)` | none | no storage permission needed |
| Prescription file | OrderDetail, PrescriptionViewer | `OpenDocument(allowed_mime_types)` | none | SAF |
| Invoice share/print | Invoice | ACTION_SEND / PrintManager + FileProvider | none | |
| OTP paste / haptics | Otp | Clipboard / LocalHapticFeedback | none | |
| Push token | (device register) | FCM | BLOCKED | no google-services.json / firebase-messaging in project |
- Manifest: added ACCESS_NETWORK_STATE, CAMERA, `<uses-feature android.hardware.camera required=false>`; existing FileProvider + `res/xml/file_paths.xml` (cache-path) reused; uCrop activity kept.
- ui/device/DeviceAccess.kt: `enum PermissionStatus { Granted, NotRequested, Denied, PermanentlyDenied }`; `rememberPermissionState(permission, onResult = {}): PermissionHandle` (`status`, `isGranted`, `request()`, `openSettings()`); `Context.openAppSettings()`; `Context.hasCamera()`; `notificationPermissionOrNull()`; `CAMERA_PERMISSION`; `ConnectivityObserver.observe(context): Flow<Boolean>`; `rememberIsOnline(): State<Boolean>`.
- ui/device/UploadGuard.kt: `suspend prepareUpload(context, uri, uploads: AppConfigUploads?, imageOnly = true, maxPx = 2048, quality = 85): UploadResult` (`Ready(part: ImagePart)` | `Rejected(message)`); `AppError?.uploadMessage(): String?` (413/415).
- MyViewModel (additive): `cachedAppConfig(): dto.AppConfig?` (uploads limits/enums/otp).
- Device registration BLOCKED: add `app/google-services.json` + Firebase messaging to enable `POST /devices`; POST_NOTIFICATIONS not declared until then.

## Open issues
- Stock*, InventoryAlerts, Suppliers, AuditLog, AdminList/Delete need VM fns (repo methods to verify per screen).
- UserList has no route since Part 2 (HomeRoutes = Dashboard).
- Dashboard tiles: repoint stock tiles to InventoryAlerts and pass a pending filter to OrderList once those screens exist.

- PTR rule: VM list/detail loaders keep old data while reloading (`update{copy(isLoading=true)}`); screens pass `isRefreshing = isLoading && data != null`. Done for admins, history, users, orders, order-by-id, product-by-id, products.
- Still old Clay styling: SpecificOrderScreen, UserDetails, UpdateUser, Profile, Settings, About, HomeScreen(users, unrouted), SignUp.kt (dead). Restyle needs Stitch exports in docs/design/.
