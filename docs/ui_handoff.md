# UI handoff

## Part 0 — foundation | Status: DONE (build OK)

### Files changed (LOCKED)
- app/src/main/java/com/saurabh/mediadminapp/ui/theme/Color.kt
- app/src/main/java/com/saurabh/mediadminapp/ui/theme/Type.kt
- app/src/main/java/com/saurabh/mediadminapp/ui/theme/Shape.kt (new)
- app/src/main/java/com/saurabh/mediadminapp/ui/theme/Theme.kt
- app/src/main/java/com/saurabh/mediadminapp/ui/theme/ClayTokens.kt (legacy Clay colours moved here; delete when no screen imports them)
- app/src/main/java/com/saurabh/mediadminapp/ui/components/StatusChip.kt (new)
- app/src/main/java/com/saurabh/mediadminapp/ui/components/StateComponents.kt (new)
- app/src/main/java/com/saurabh/mediadminapp/ui/screens/nav/NavApp.kt (shell only; graph/routes unchanged)
- app/src/main/java/com/saurabh/mediadminapp/MainActivity.kt (edge-to-edge auto bar styles)
- app/src/main/AndroidManifest.xml (MainActivity theme -> Theme.MediAdminApp.Starting)
- app/src/main/res/values/themes.xml, values-night/themes.xml (new), values/colors.xml
- app/src/main/res/mipmap-anydpi/ic_launcher.xml, ic_launcher_round.xml
- app/src/main/res/drawable-nodpi/ic_logo.png (new), drawable/ic_launcher_foreground_logo.xml (new), drawable/ic_splash_logo.xml (new)

### Theme
- Entry: `MediAdminAppTheme(darkTheme: Boolean = isSystemInDarkTheme(), dynamicColor: Boolean = false /* ignored */, content)` in `com.saurabh.mediadminapp.ui.theme`.
- Colours: `MaterialTheme.colorScheme.*` only (light/dark schemes per spec). Typography = M3 default `Typography()`; Shapes = M3 default `Shapes()`.
- Brand spots only: `BrandColors.ShieldTeal | ShieldTealLight | CapsuleTeal | LeafGreen | Lime | Cream` (ui/theme; never large areas or text).
- Legacy `Clay*` / `State*` / gradient vals still exist in ClayTokens.kt for unrestyled screens. Do not use in restyled code.

### StatusColors API (ui/theme/Theme.kt)
- `data class StatusColor(val container: Color, val content: Color)`
- `data class StatusColors(pending, approved, sold, cancelled, returned: StatusColor) { fun of(status: OrderStatus): StatusColor }` (UNKNOWN -> returned)
- `val MaterialTheme.statusColors: StatusColors` (@Composable getter); `LocalStatusColors`.
- pending = FFDEA8/3D2A00 (dark 5C4000/FFDEA8); approved = primaryContainer; sold = secondaryContainer; cancelled = errorContainer; returned = surfaceVariant (each with its on* role).

### Components (package `com.saurabh.mediadminapp.ui.components`)
- `StatusChip(status: OrderStatus, modifier: Modifier = Modifier)`; `fun OrderStatus.label(): String`
- `EmptyState(title: String, message: String, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null)`
- `ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier)`
- `LoadingState(modifier: Modifier = Modifier)`
- `SectionHeader(text: String, modifier: Modifier = Modifier)` (own 16dp horizontal padding)
- `ConfirmDialog(title: String, message: String, confirmText: String, onConfirm: () -> Unit, onDismiss: () -> Unit, dismissText: String = "Cancel", destructive: Boolean = true)`
- `OrderStatus` = `com.saurabh.mediadminapp.data.v1.core.OrderStatus`.
- Old Clay components (ui/screens/components/Clay*.kt, Glassmorphism.kt) untouched and still compile.

### Brand
- In-app logo drawable: `R.drawable.ic_logo` (768px PNG, transparent, drawable-nodpi).
- Launcher: adaptive icon, background `@color/ic_launcher_background` (FBF9F1), foreground `@drawable/ic_launcher_foreground_logo` (ic_logo inset 9%). Monochrome layer skipped (raster logo).
- Splash: `Theme.MediAdminApp.Starting` (core-splashscreen), background FBF9F1, animated icon `@drawable/ic_splash_logo` (ic_logo inset 16%), postSplashScreenTheme `Theme.MediAdminApp`. `installSplashScreen()` already in MainActivity.

### Nav shell (ui/screens/nav/NavApp.kt)
- `Scaffold` (background colour, SnackbarHost, contentWindowInsets 0; screens keep handling their own insets and bottom padding) + M3 `NavigationBar` on top-level routes: Dashboard (HomeRoutes), Products (productRoutes), Orders (OrdersRoutes), History (HistoryRoutes), More.
- Drawer removed from the visual layer; `More` tab and every screen's `onMenuClick` open a `ModalBottomSheet` with Profile / Settings / About / Logout (same routes and `viewModel.setAdminLoggedOut()`).
- Screens still own their TopAppBars and receive `scrollBehavior` as before; top bars are restyled per screen in later parts.
- Hide-on-scroll and custom bar animations removed (U13).

### Screens restyled
- none (Part 0 is foundation only)

### MISSING screens
- MISSING: Inventory (no route/screen exists; nav tab slot 3 kept as History until one is added)

### Skipped / notes
- Old `res/drawable/glossy_shield_pharmacy_app_icon.png` (~1 MB, unreferenced), `drawable/ic_launcher_foreground.xml`, `drawable/ic_launcher_background.xml` and legacy `mipmap-*/ic_launcher*.webp` are now unused; deleting them was not permitted in this session. Remove manually to shrink the APK.
- ui/auth package (Cura theme) untouched.
- Stitch project link not fetched; the exact palette values from the Part 0 spec were used.

## Part 1 — auth screens | Status: DONE (build OK)

### Files changed (LOCKED)
- app/src/main/java/com/saurabh/mediadminapp/ui/screens/SignIn.kt (SignIn composable restyled; legacy internal ClayTextField/ClayBlob/ClayGradientButton kept at file bottom because SignUp/AddProduct/UpdateProduct/UpdateUserDetails/Home still use them; later parts may need them removed: ask before editing)
- app/src/main/java/com/saurabh/mediadminapp/ui/screens/OtpScreen.kt
- app/src/main/java/com/saurabh/mediadminapp/ui/screens/SplashScreen.kt
- app/src/main/java/com/saurabh/mediadminapp/ui/screens/AuthCommon.kt (new, internal helpers)
- app/src/main/java/com/saurabh/mediadminapp/ui/screens/PasswordResetScreens.kt (new)
- app/src/main/java/com/saurabh/mediadminapp/ui/screens/AccountStatusScreens.kt (new)
- app/src/main/java/com/saurabh/mediadminapp/ui/screens/nav/Routes.kt (4 auth routes added; later parts may add routes but must not change these)
- app/src/main/java/com/saurabh/mediadminapp/ui/screens/nav/NavApp.kt (LOCK exception for the Part 1 nav recheck: 4 composables, isAuthRoute entries, forced-update redirect)
- DELETED: app/src/main/java/com/saurabh/mediadminapp/ui/auth/** (25 files: Cura theme, mock AuthRepository, duplicate Login/Otp/Forgot/Reset/Splash/Success screens + VMs, AuthNavGraph). Zero references before removal.

### Screens restyled (old name -> file)
- Splash -> ui/screens/SplashScreen.kt (`SplashScreen(navController, viewModel)`)
- Login -> ui/screens/SignIn.kt (`SignIn(viewModel, navController)`)
- Otp -> ui/screens/OtpScreen.kt (`OtpScreen(adminId, viewModel, navController)`)
- ForgotPassword -> ui/screens/PasswordResetScreens.kt (`ForgotPasswordScreen(viewModel, navController)`, new; `Routes.ForgotPasswordRoutes`)
- ResetPassword -> ui/screens/PasswordResetScreens.kt (`ResetPasswordScreen(viewModel, navController)`, new; `Routes.ResetPasswordRoutes`)
- UpgradeRequired -> ui/screens/AccountStatusScreens.kt (`UpgradeRequiredScreen(viewModel)`, new; `Routes.UpgradeRequiredRoutes`)
- AccountBlocked -> ui/screens/AccountStatusScreens.kt (`AccountBlockedScreen(viewModel, navController)`, new; `Routes.AccountBlockedRoutes`)

### Shared auth helpers (ui/screens/AuthCommon.kt, internal, reusable)
- `AppError?.fieldError(key: String): String?` (backend `fields`)
- `AuthFormColumn(modifier, verticalArrangement = Arrangement.Center, content)` (scroll + imePadding + 16dp margins)
- `SupportingText(error: String?, hint: String? = null)`
- `PasswordField(value, onValueChange, label, error, modifier, hint = null, imeAction = ImeAction.Done)` (visibility toggle)
- `AuthPrimaryButton(text, isLoading, onClick, modifier)` (full-width Button, 48dp min)
- `AUTH_MESSAGE_KEY` (savedStateHandle key on the SignIn back-stack entry for one-line messages)

### Navigation (U16 recheck)
- Start: system splash holds while `isAdminLoggedIn == null`; NavHost then starts at Home or SignIn (no flicker). The `SplashRoutes` screen routes UpgradeRequired > Home > SignIn.
- Login -> Otp -> Home (popUpTo graph, inclusive). Non-admin role (VM error "Admin account required") or 5 failed OTP tries -> pop back to SignIn with a message.
- Login -> Forgot -> Reset -> pop to SignIn with "Password updated" message.
- Login 403 `ACCOUNT_BLOCKED` -> AccountBlocked -> Log out -> SignIn (popUpTo 0).
- `upgradeRequiredVersion != null` -> UpgradeRequired (popUpTo 0, back disabled); the session guard is skipped on that route.
- Logout / invalid token (`isAdminLoggedIn = false`) -> SignIn with the whole back stack cleared (existing guard).
- No SignUp link and no PendingApproval route in the auth flow.

### MISSING screens
- none (Forgot/Reset/UpgradeRequired/AccountBlocked had no real screens and were created)

### Skipped / notes
- Splash ErrorState + Retry not wired: `MyViewModel.getAppConfig()` swallows network errors and exposes no state (needs a VM state; out of UI scope).
- Global `AppEvent.AccountBlocked` (403 on any call) is handled in the VM as snackbar + logout -> Login; routing it to AccountBlocked needs a VM event.
- OTP resend only restarts the 60 s timer + snackbar (unchanged wiring); a real resend (= login again) needs the credentials (VM change). Timer not switched to `otp_expires_in` (not passed through the route).
- `SignUpRoutes` / `SignUp.kt` kept untouched: it is the create-admin form and needs an admin token; now unreachable (link removed from Login). Rewire under More > Admins in a later part, then drop it from `isAuthRoute`.
- Password-reset states have no VM clear fns; screens use a local `submitted` flag so stale success/error is ignored.
- Stitch project not fetched; layouts follow the Part 1 spec + U5-U14.

## Part 1b — auth follow-up (user request) | Status: DONE (build OK)
- SignUp -> ui/screens/SignUp.kt restyled to M3 (TopAppBar back, tonal photo picker, OutlinedTextFields with `fields` errors, min 8 password, `AuthFormError`), `createAdmin(...)` wiring unchanged; success pops to SignIn with "Account created" message. LOCKED.
- SignIn <-> SignUp links restored at the user's request ("Don't have an account? Create account" / "Already have an account? Sign in"). This overrides the admin-only "no signup link" rule. Note: `POST /admins` needs an admin token, so creating an account while signed out will be rejected by the server.
- AuthCommon.kt additions: `AuthHeader(icon, title, subtitle, modifier)`, `AuthFooterLink(prompt, action, onClick, modifier)`, `AuthFormError(text)` (errorContainer banner).
- Otp / Forgot / Reset now open with `AuthHeader`; form errors use `AuthFormError`; Login: "Welcome back" header, right-aligned "Forgot password?", divider + footer link.

## Part 2 — Dashboard and Products | Status: DONE (build OK)

### Files changed (LOCKED)
- app/src/main/java/com/saurabh/mediadminapp/ui/screens/nav/dashboard.kt (mock Dashboard replaced by `DashboardScreen(viewModel, navController, onMenuClick, scrollBehavior)`)
- app/src/main/java/com/saurabh/mediadminapp/ui/screens/nav/NavApp.kt (one line: HomeRoutes now renders DashboardScreen)
- app/src/main/java/com/saurabh/mediadminapp/ui/screens/ProductScreen.kt
- app/src/main/java/com/saurabh/mediadminapp/ui/screens/SpecificProductScreen.kt
- app/src/main/java/com/saurabh/mediadminapp/ui/screens/AddProductScreen.kt (also `internal ProductFormScaffold(...)`, reusable form)
- app/src/main/java/com/saurabh/mediadminapp/ui/screens/UpdateProductScreen.kt
- DELETED: ui/screens/HomeScreen1.kt (empty)

### Screens restyled (old name -> file)
- Home/Dashboard -> nav/dashboard.kt `DashboardScreen`: 2-col adaptive grid of Cards, pull-to-refresh, pending tile in primaryContainer. Tap: pending -> OrdersRoutes, today sales -> HistoryRoutes, low stock / near expiry / expired / products -> ProductRoutes (no InventoryAlerts route).
- ProductList -> ProductScreen.kt: DockedSearchBar, FilterChips (category, schedule, in stock, active; client-side), OutlinedCard+ListItem rows, stock chip, Rx chip, load-more (`loadMoreProducts`), pull-to-refresh, one FAB -> AddProductRoutes.
- ProductDetail -> SpecificProductScreen.kt: Details/Pricing (purchase price)/Stock sections, Edit (FilledTonalButton), Deactivate/Reactivate (TextButton, ConfirmDialog).
- AddEditProduct -> AddProductScreen.kt + UpdateProductScreen.kt via `ProductFormScaffold` (bottom Save button, supportingText from `fields`, image picker; Edit keeps camera/gallery + UCrop).

### MISSING screens
- MISSING: Reports, TopProducts (no screen/route/VM state; repo has salesReport/topProducts)
- MISSING: BarcodeScanner (no screen/route; ProductList barcode IconButton not added)
- MISSING: BatchList, StockPurchase, StockMovements routes (ProductDetail "Batches" button not added)
- MISSING: InventoryAlerts route

### Skipped / notes
- NEEDS_DATA: Add/Edit form only sends name, price, category, image (`addProduct/updateProduct` VM signature). Dosage form, schedule, unit, Rx switch, strength, etc. need VM params; add dropdowns when VM is extended.
- HomeScreen.kt (users list) is no longer on any route; the Users part must re-home it. Left untouched.
- Dashboard needs `getDashboardSummary()`; Reports date-range/SegmentedButton skipped (see MISSING).
- Legacy Clay* components still used by other screens; not deleted.

## Part 6 — GreenShield rules rollout (batch 1: auth + shell) | Status: IN PROGRESS
Files changed: ui/theme/Color.kt (GS palette, Shades, light scheme remap), ui/theme/Theme.kt (ripple off via `LocalRippleConfiguration provides null`), ui/screens/OtpScreen.kt, ui/screens/AuthCommon.kt (tap-outside dismiss, pressed shade 600->700 + 0.97 scale on `AuthPrimaryButton`, disabled = Green 300), ui/screens/nav/NavApp.kt (directional NavHost transitions per Part 3 §2, tab switches crossfade; OTP call sites).
OTP consolidation: ONE screen `OtpScreen(flow: OtpFlow, viewModel, navController)`; `OtpFlow.SignIn(adminId)` (verifyOtpState, expiry timer) and `OtpFlow.EmailVerify(email)` (emailVerifyState, 60 s resend cooldown). Routes (`VerifyOtpRoutes`, `VerifyEmailRoutes`) and ViewModel untouched. `EmailVerifyScreen.kt` removed (no remaining references).
LOCK note: OtpScreen/AuthCommon/NavApp/Color/Theme are earlier-part LOCKED files; edited because the request requires it.
Remaining (later batches): per-screen shade-on-press for rows/cards/chips/icon buttons, StatusChip family mapping, medicine image fallback tiles, predictive back, keyboard types per field.

## Part 7 — GreenShield rollout (batch 2: order detail, user detail, user edit, admin profile) | Status: DONE (build OK)
Files changed (LOCKED): ui/components/DetailParts.kt (new), ui/screens/SpecificOrderScreen.kt, ui/screens/UserDetailsScreen.kt, ui/screens/UpdateUserDetailsScreen.kt, ui/screens/ProfileScreen.kt.
Screens restyled (old name -> file): OrderDetail -> SpecificOrderScreen.kt; UserDetail -> UserDetailsScreen.kt; UserEdit -> UpdateUserDetailsScreen.kt; AdminProfile -> ProfileScreen.kt. All on `AppScreenScaffold`, data-first loading/error/empty, Form-width adaptive, no Clay* usage.
New shared components (ui.components): `DetailCard(modifier, title?, icon?, content)`, `InfoTile(label, value, modifier, icon?)`, `KeyValueRow(label, value, modifier, emphasize)`, `ToneChip(text, Tone.Success|Warning|Danger|Info)`, `PersonAvatar(url, name, modifier, size)`, `MedicineImage(url, modifier, size)` (1:1, Fit, Teal fallback), `ShadeButton(text, onClick, modifier, shade, enabled, isLoading, icon)`, `ShadeOutlinedButton(text, onClick, modifier, enabled, icon, contentColor)`, `Modifier.clearFocusOnTap()`.
Behaviour notes: Order: PENDING -> Approve + Reject, APPROVED -> Reject (ConfirmDialog); SOLD/CANCELLED/RETURNED have no action (old screen toggled SOLD too); order reloads (force) after approve/reject. User detail: Approve/Unblock/Block(confirm)/Delete(confirm) via existing VM fns; status switches removed from User edit (per ui_followups). User edit sends all profile fields, shows backend `fields` errors, pops only after its own request succeeds.
MISSING: none. Skipped: hold (2-step) shade on buttons, predictive back, Mark sold / Return / Verify prescription on order (needs UI-only wiring decision), ClayNetworkImage still used by HomeScreen.kt.

## Part 8 — GreenShield audit of all existing screens | Status: DONE (build OK)
Files changed (LOCKED): ui/components/DetailParts.kt (+NavRow, +Modifier.shadeRow), ui/components/AppScaffolds.kt (StatCard: White/Outline, press shade, 0.97 scale), ui/components/FormParts.kt (SaveBar -> ShadeButton), ui/components/StockEntry.kt, ui/screens/SettingsScreen.kt, AboutScreen.kt, UserListScreen.kt (new), SpecificOrderScreen.kt, ProfileScreen.kt, ProductScreen.kt, OrderListScreen.kt, SalesHistoryScreen.kt, BatchListScreen.kt, BatchDetailScreen.kt, AdminListScreen.kt, BarcodeScannerScreen.kt, SplashScreen.kt, ProductForm.kt, SpecificProductScreen.kt, nav/NavApp.kt (2 dead imports removed only).
DELETED (unrouted Clay duplicates, zero references): screens/HomeScreen.kt (replaced by UserListScreen.kt), HistroyScreen.kt, NewOrder.kt, OrderDetailsScreen.kt, EmailVerifyScreen.kt, screens/components/* (ClayButton, ClayCard, ClayMorphic, ClayScreenShell, ClayTextField, Glassmorphism, SalesCard, SalesFilter, EachOrderCard, CircularAnalyticsChart), theme/ClayTokens.kt.
Screens restyled (old name -> file): Settings -> SettingsScreen.kt (fake toggles removed; Account/Reports/App rows + logout confirm); About -> AboutScreen.kt (logo, real versionName, counts); UserList (HomeScreen.kt UserListScreen1) -> UserListScreen.kt `UserListScreen(viewModel, navController, onMenuClick? = null, scrollBehavior)`.
Rule fixes: list rows (orders, sales, batches, products, users) use `Modifier.shadeRow` (White->Green50 press, Green100 hold, Outline border, no ripple/scale; batches use Red/Amber card shades); medicine thumbs -> `MedicineImage` (1:1, Fit, Teal fallback); product chips -> ToneChip families; product form image Fit not Crop; fixed `.height(48.dp)` -> `heightIn(min = 48.dp)`; all primary buttons -> ShadeButton (600->700, 0.97); write-off -> red outlined + confirm.
Update options added (existing VM fns): Order detail: edit patient/doctor (pending, `updateOrder`), approve/reject with reason, Mark sold (`recordSell`), Cancel with reason (`deleteOrder`), Return with reason (`returnOrder`), prescription image + Mark verified (`verifyPrescription`); order reloads after each. Profile: Edit name/email/phone (`updateAdmin`, changed fields only).
MISSING: Customers list has no route (UserListScreen exists; UserDetails/UserEdit reachable only through it). Needs a route + drawer entry (nav change, out of U1 scope).
Skipped: StockMovements / InventoryAlerts / Suppliers / AuditLog / Invoice screens (no VM data); predictive-back shrink.

## Part 9 — GreenShield full pass on every screen (theme + touch + styling) | Status: DONE (build OK)
Theme (ui/theme): `secondaryContainer` = Green 100 / `onSecondaryContainer` = Green 600 (M3 "selected" for chips, segmented buttons, nav indicator, tonal buttons). Status colours now U14 families (pending Amber, approved Teal, sold Green, cancelled Red, returned neutral). New `MaterialTheme.isLight` and `object GsUi { rowShade(), secondaryButtonShade(), iconButtonShade(), familyShade(light), fieldColors(), topBarColors() }` — dark-mode safe (GS shades are light-only). NOTE: named `GsUi`, not `Gs`: `Gs`/`GS` collide on Windows' case-insensitive file system.
New components (ui/components): Press.kt `InteractionSource.pressLevel()` (None/Pressed/Held; held after long-press timeout, resets on release), `shadeColor(shade)`, `pressScale()`; Controls.kt `GsTextField` (same params as OutlinedTextField: White fill, Outline 1dp, focus Green 500 2dp, error Red 600), `GsFilterChip`, `LinkButton` (600/700/800 text), `NeutralTextButton`, `GsFab`, `ChipLabel`; DetailParts `TileIconButton` (Icon-fill tile, 48dp); AppScaffolds `HeroCard` (only gradient: Green 800->600->400); `StatCard(tone = Tone?)`.
Global: every OutlinedTextField -> GsTextField, every FilterChip -> GsFilterChip; ShadeButton/ShadeOutlinedButton/rows/chips/nav now use real press + hold shades; AppScreenScaffold = White top bar + Outline divider + back arrow in tile; FormScaffold clears focus on outside tap; ConfirmDialog White, Red/Green confirm link + neutral Cancel; StatusChip bold for Amber/Red; sheet rows shade (danger row Red 50/100).
Screens touched: SignIn (links, email no-autocorrect), AuthCommon (AuthTopBar, AuthPrimaryButton -> ShadeButton, footer LinkButton, error text Red 600), OTP (top bar, animated 120ms Green 500 box border, White boxes, Paste/Resend links), Forgot/Reset (top bar, link), AdminSignUp + SignUp (top bar, footer link, photo button), AccountStatus (family icon tile), Splash (Green 200 track), Dashboard (Hero today-sales card; tiles toned: pending/low/near = Amber, out/expired = Red, approved = Teal; tile icon buttons), NavApp (custom bottom-nav item with shades, White bar/rail/drawer, avatar), Products (tile icons, GsFab, white sort menu), ProductDetail (rewritten: 220dp 1:1 Fit image, semantic pills, Field-fill tile grid with ALL fields incl. HSN/storage/unit/dates, teal "View batches" row, Edit primary bottom bar, deactivate red / reactivate green), ProductForm (Ascii+CAPS for barcode/HSN, Words for name/brand/manufacturer, photo button), BatchList (Teal batch pill + Expired/Near/Available pills, GsFab, tile icon), BatchDetail / StockPurchase / StockEntry / Barcode (Ascii+CAPS batch/invoice/barcode, shade buttons), AdminList (ToneChips, PersonAvatar, Reject outlined red, Remove red link), Reports (white bordered rows, Green 200 progress track, picker buttons), TopProducts (shade rows, Fit image), Orders/Sales/Users/UserDetail/Order/Profile (tile icons, dialog buttons).
Skipped: AlertDialog/DatePicker enter motion (M3 internal, no API for 0.92 scale); predictive-back shrink; SectionHeader inside dashboard grid keeps its own 16dp inset.

## Part 10 — next 4 screens (Invoice, PrescriptionViewer, AdminDetail/Create, Customers route) | Status: DONE (build OK)
Files changed (LOCKED): ui/screens/InvoiceScreen.kt (new), PrescriptionViewerScreen.kt (new), AdminFormScreen.kt (new), nav/Routes.kt (+UsersRoutes, InvoiceRoutes(orderId), PrescriptionRoutes(orderId), AdminFormRoutes(adminId); existing routes unchanged), nav/NavApp.kt (4 composables + drawer "Customers"), AdminListScreen.kt (row tap -> detail via shadeRow, "Add admin" GsFab), SpecificOrderScreen.kt (Invoice tile action for SOLD/RETURNED; prescription card -> teal "View prescription" row).
Screens: Invoice -> `InvoiceScreen(orderId, vm, nav)`; PrescriptionViewer -> `PrescriptionViewerScreen(orderId, vm, nav)`; AdminDetailEdit -> `AdminFormScreen(adminId, vm, nav)` (`NEW_ADMIN_ID = "new"` = create); UserList -> `UserListScreen` now routed (UsersRoutes).
Reused only existing components/VM fns (DetailCard, InfoTile, KeyValueRow, ToneChip, StatusChip, PersonAvatar, ShadeButton/Outlined, LinkButton, TileIconButton, GsFab, GsTextField, PasswordField, AuthFormError, FormScaffold, ConfirmDialog, shadeRow, rememberInrFormat-style money). No VM / repository / DTO changes.
Notes: invoiceState / createAdminState are never cleared by the VM -> screens filter by orderId / use submitted+sawLoading. updateAdmin has no state -> save completes when the reloaded list shows the new values (15 s fallback; errors come as VM snackbar). Deleting a user from UserDetails still navigates to Dashboard (existing nav logic, not changed). SignUp.kt (old create-admin in auth graph) left untouched.
MISSING (need VM fns; repository already has them): StockMovements (getStockMovements), InventoryAlerts (getLowStock/getOutOfStock/getNearExpiry/getExpired), Suppliers (getSuppliers/create/update/deactivate), AuditLog (getAuditLogs).

## Part 11 — VM wiring + Inventory alerts, Stock movements, Suppliers, Audit log | Status: DONE (build OK, unit tests 33/33)
User allowed ViewModel changes for this part (U1 exception). Repository / DTO / API untouched.
Verified against response.md: #44 stock-movements (StockMovement), #45 expired + #47 near-expiry (Batch; near-expiry example matches exactly, days default 90), #46 low-stock + #48 out-of-stock (Product: examples empty, shape assumed = "active products"), #63-67 suppliers (Supplier; deactivate twice -> 409; GSTIN 15 chars; no reactivate endpoint), #71 audit-logs (AuditLog; details free-form JSON, secrets stripped).
VM (MyViewModel.kt, additive, end of class): private generic `PagedSource<T>` + `load(key, force, sort, order, fetch)` / `more()` (keeps items while refreshing, clears on filter change, ignores stale load-more). Public:
- `stockMovements: StateFlow<PagedListState<StockMovement>>`, `stockMovementFilter`, `loadStockMovements(filter?, force)`, `loadMoreStockMovements()` (created_at desc)
- `lowStock`, `outOfStock: StateFlow<PagedListState<Product>>`, `nearExpiry`, `expiredBatches: StateFlow<PagedListState<Batch>>`, `loadInventoryAlert(type, force, nearExpiryDays?)`, `loadMoreInventoryAlert(type)`
- `suppliers`, `supplierQuery`, `loadSuppliers(query?, force)`, `loadMoreSuppliers()`, `supplierDetail: StateFlow<UiState<Supplier>>`, `getSupplier(id, force)`, `supplierAction: StateFlow<SupplierActionState>` (seq-tagged), `createSupplier(req)`, `updateSupplier(id, req)`, `deactivateSupplier(id)`, `clearSupplierAction()` (list + detail patched in place, snackbars)
- `auditLogs`, `auditFilter`, `loadAuditLogs(filter?, force)`, `loadMoreAuditLogs()`
- all reset in `resetAllStatesOnLogout()` via `resetInventoryAdminData()`.
New state file: app/.../InventoryState.kt (`PagedListState<T>`, `InventoryAlertType`, `SupplierQuery`, `SupplierActionState`).
Files changed (LOCKED): MyViewModel.kt, InventoryState.kt (new), ui/screens/InventoryAlertsScreen.kt, StockMovementsScreen.kt, SupplierScreens.kt, AuditLogScreen.kt (new), nav/Routes.kt (+InventoryAlertsRoutes(tab), StockMovementsRoutes(productId?, batchId?), SuppliersRoutes, SupplierFormRoutes(id), AuditLogRoutes), nav/NavApp.kt (5 composables + drawer: Inventory alerts, Stock movements, Suppliers, Audit log), nav/dashboard.kt (stock tiles -> Inventory alerts tab, plain forward nav), SpecificProductScreen.kt (+ "Stock movements" teal row), BatchDetailScreen.kt (+ link to batch movements).
Not changed: bottom-nav tabs (History still in slot 3; swapping in Inventory alters tab structure, ask first). Stock purchase still takes a free-text supplier name (no supplier picker; StockPurchaseRequest has supplier_id? check before wiring).

## Part 12 - Bottom nav layout fix | Status: DONE (build OK)
Bug: GsNavBarItem used fillMaxHeight() inside NavigationBar; the Scaffold bottomBar slot is unbounded, so the bar filled the whole screen (tabs in the middle, screen content hidden). Fix: item height fixed to 80.dp (M3 NavigationBar height). Other fillMaxHeight uses checked: all inside bounded screen content, no change.
Files changed: nav/NavApp.kt.
