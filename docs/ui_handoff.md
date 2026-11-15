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
