# UI build status (ledger, ONLY handoff file)

Spec: `app/src/main/java/com/saurabh/mediadminapp/screens and navigation.md` | VM: `data/v1/viewmodel_api.md`, `data/v1/ui_followups.md` | API: `app/src/main/java/com/saurabh/mediadminapp/response.md` | Skill: `.claude/skills/admin-screen-builder/SKILL.md`
Design links: none provided (placeholders). Designs follow M3 + existing ui/theme tokens; drop exports in `docs/design/<screen>.png` to refine.
Nav style: string routes (`Routes.X.route`, `@Serializable` objects); only `VerifyOtpRoutes` is typed.

## Screens
| # | screen | file | route | VM fns/states | design | status | notes |
|---|---|---|---|---|---|---|---|
| 1 | Splash | ui/screens/SplashScreen.kt | SplashRoutes | getAppConfig, upgradeRequiredVersion, isAdminLoggedIn, appConfigError | - | DONE | config fail w/o cache -> error + Retry; device: none |
| 2 | Login | ui/screens/SignIn.kt | SignInRoutes | loginAdmin, loginAdminState, clearLoginState | - | DONE | sign-up link + Clay helpers removed; client validation, IME Done submits, BLOCKED->AccountBlocked, NOT_APPROVED/RATE_LIMITED msgs |
| 3 | Otp | ui/screens/OtpScreen.kt | VerifyOtpRoutes(userId) | verifyAdminOtp, verifyOtpState, cachedAppConfig().otp | - | DONE | boxes (1 hidden field), TTL/len/attempts from app-config (300/6/5 fallback), paste, haptics, auto-verify; resend/RATE_LIMITED/5 fails -> Login; device: clipboard+haptics |
| 4 | ForgotPassword | ui/screens/PasswordResetScreens.kt | ForgotPasswordRoutes | requestPasswordReset, passwordResetState | - | DONE | email format check, IME Done submits, RATE_LIMITED msg, field disabled while loading; device: none |
| 5 | ResetPassword | ui/screens/PasswordResetScreens.kt | ResetPasswordRoutes | resetPasswordOtp(email,otp,pw), passwordResetOtpState, lastResetEmail | - | DONE | email field (prefilled), pw 8-128, IME Done submits, RATE_LIMITED msg + 'Request a new code' -> back; device: none |
| 6 | UpgradeRequired | ui/screens/AccountStatusScreens.kt | UpgradeRequiredRoutes | upgradeRequiredVersion | - | DONE | back disabled; market:// -> https fallback -> toast; device: ACTION_VIEW |
| 7 | AccountBlocked | ui/screens/AccountStatusScreens.kt | AccountBlockedRoutes | setAdminLoggedOut | - | DONE | Log out + back both logout -> SignIn popUpTo(0); device: none |
| 8 | Home | ui/screens/nav/dashboard.kt (DashboardScreen) | HomeRoutes | getDashboardSummary, dashboardSummaryState | - | DONE | AppScreenScaffold+StatCard grid (2 sections, 9 counters), skeleton, keeps last data on refresh/error, tab-style nav; stock tiles -> Products until InventoryAlerts, pending tile has no status filter yet; device: none |
| 9 | Reports | - | - | (VM missing; repo salesReport) | - | TODO | MISSING |
| 10 | TopProducts | - | - | (VM missing; repo topProducts) | - | TODO | MISSING |
| 11 | ProductList | ui/screens/ProductScreen.kt | ProductRoutes | getAllProduct, loadMoreProducts | - | TODO | EXISTS (Part 2) |
| 12 | BarcodeScanner | - | - | (VM missing; repo by-barcode?) | - | TODO | MISSING; deps added |
| 13 | ProductDetail | ui/screens/SpecificProductScreen.kt | SpecificProductRoutes | getSpecificProduct, deleteProduct, reactivateProduct | - | TODO | EXISTS (Part 2) |
| 14 | AddEditProduct | ui/screens/AddProductScreen.kt + UpdateProductScreen.kt | AddProductRoutes / UpdateProductRoutes | addProduct, updateProduct | - | TODO | EXISTS; VM sends only name/price/category/image |
| 15 | BatchList | - | - | VM missing | - | TODO | MISSING |
| 16 | BatchDetail | - | - | VM missing | - | TODO | MISSING |
| 17 | StockPurchase | - | - | VM missing | - | TODO | MISSING |
| 18 | StockAdjust | - | - | VM missing | - | TODO | MISSING |
| 19 | StockMovements | - | - | VM missing | - | TODO | MISSING |
| 20 | OrderList | ui/screens/NewOrder.kt (+ OrderDetailsScreen.kt) | OrdersRoutes / EachUserOrderRoutes | getAllOrders, loadMoreOrders | - | TODO | RENAME (Clay) |
| 21 | OrderDetail | ui/screens/SpecificOrderScreen.kt | SpecificOrderRoutes | getOrderById, isApproveOrder, recordSell, returnOrder, deleteOrder, verifyPrescription | - | TODO | EXISTS (Clay) |
| 22 | PrescriptionViewer | - | - | verifyPrescription | - | TODO | MISSING |
| 23 | Invoice | - | - | getInvoice, invoiceState | - | TODO | MISSING |
| 24 | InventoryAlerts | - | - | VM missing | - | TODO | MISSING; nav tab slot pending |
| 25 | MoreMenu | nav/NavApp.kt (MoreSheet) | - | - | - | TODO | sheet today; screen needed |
| 26 | SalesHistory | ui/screens/HistroyScreen.kt | HistoryRoutes | getAllSellHistory, loadMoreSellHistory | - | TODO | RENAME (Clay) |
| 27 | UserList | ui/screens/HomeScreen.kt (UserListScreen1) | none (was HomeRoutes) | getAllUsers, loadMoreUsers | - | TODO | RENAME; unrouted since Part 2 |
| 28 | UserDetail | ui/screens/UserDetailsScreen.kt | UserDetailsRoutes | isApprovedUser, unblockUser, deleteUser, getUsersOrders | - | TODO | EXISTS (Clay) |
| 29 | UserEdit | ui/screens/UpdateUserDetailsScreen.kt | UpdateUserDetailsRoutes | updateUser | - | TODO | EXISTS (Clay) |
| 30 | AdminList | - | - | getAllAdmin, loadMoreAdmins | - | TODO | MISSING |
| 31 | AdminDetailEdit | ui/screens/SignUp.kt (create only) | SignUpRoutes | createAdmin, updateAdmin (delete VM missing) | - | TODO | RENAME; move out of auth graph |
| 32 | SupplierList | - | - | VM missing | - | TODO | MISSING |
| 33 | SupplierDetailEdit | - | - | VM missing | - | TODO | MISSING |
| 34 | AuditLog | - | - | VM missing | - | TODO | MISSING |
| 35 | AdminProfile | ui/screens/ProfileScreen.kt | ProfileRoutes | getAllAdmin, updateAdmin, setAdminLoggedOut | - | TODO | EXISTS (Clay) |
| 36 | Settings/About | ui/screens/SettingsScreen.kt, AboutScreen.kt | SettingsRoutes, AboutRoutes | - | - | TODO | EXISTS (Clay) |

## Adaptive helpers (ui/adaptive/Adaptive.kt)
- `enum WindowSize { COMPACT, MEDIUM, EXPANDED }`; `data class WindowInfo(size, isLandscape, widthDp)` with `isCompact`, `isExpanded`
- `@Composable rememberWindowSize(): WindowInfo` (LocalConfiguration; no adaptive dependency)
- `AdaptiveWidths.Form = 560.dp`, `AdaptiveWidths.List = 840.dp`
- `AdaptiveContent(modifier, maxWidth = List, content: BoxScope)` centered, capped width
- `adaptiveGridCells(minCellWidth = 160.dp): GridCells`
- `TwoPane(showDetail, list, detail, modifier, emptyDetail)` (Row 42/58 on EXPANDED; single pane otherwise; caller hoists selection with rememberSaveable)

## Navigation decisions
- NavApp shell: COMPACT bottom NavigationBar, MEDIUM/EXPANDED NavigationRail (tab roots only). Tabs today: Dashboard, Products, Orders, History, More(sheet). Inventory tab replaces History when InventoryAlerts exists; History moves into MoreMenu.
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
- Reports, TopProducts, BarcodeScanner, Batch*, Stock*, InventoryAlerts, Suppliers, AuditLog, AdminList/Delete need VM fns (repo methods to verify per screen).
- AddEditProduct full form needs VM params (dosage form, schedule, unit, Rx, etc.).
- UserList has no route since Part 2 (HomeRoutes = Dashboard).
- Dashboard tiles: repoint stock tiles to InventoryAlerts and pass a pending filter to OrderList once those screens exist.
