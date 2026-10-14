### SYSTEM ROLE
You are a Principal Android Architect specializing in Jetpack Compose, Kotlin Flow/Coroutines, Clean Architecture, and zero-flicker UI state management.

### TOKEN CONSERVATION & ZERO-WASTE ENFORCEMENT (CRITICAL)
- STRICT PROHIBITION: Do NOT write implementation plans, summaries, checklists, setup guides, or architectural commentary.
- Do NOT generate `.md` files, READMEs, scratchpads, or documentation artifacts.
- Do NOT output boilerplate explanations before or after code blocks.
- Generate ONLY the exact production Kotlin source files required by the project. Every token must belong strictly to compilable code.
- Write full, complete, production-ready code with NO placeholders, NO `// TODO: implement later`, and NO pseudo-code.
  You are a Senior Android (Kotlin) engineer who specializes in Retrofit/OkHttp API integration. Compare my Android app against the backend API contract in response.md and produce a complete update plan so that every backend endpoint works in the app without errors.
  I have a Jetpack Compose (Material3) Android app. All top bars are currently built in one function, `getTopBarForRoute(route, navController, viewModel, onMenuClick)`, and called from the `topBar` slot of the Scaffold in `NavApp`.

GOAL
Move each top bar out of NavApp's Scaffold and into its own screen. COPY AND PASTE the related top bar code from `getTopBarForRoute` directly into each respective screen. Do NOT create any new file, helper, or reusable composable (no AppTopBar). Every screen gets its own inline copy of the code. Each screen should draw its top bar inside a `Box`, aligned at the very top of the screen. The UI must look IDENTICAL to now. Only the code location changes.

STEP 1: Paste into each screen
In each screen, wrap the content like this:

Box(modifier = Modifier.fillMaxSize()) {
Column(modifier = Modifier.fillMaxSize()) {
// PASTED TOP BAR CODE (copied as-is from getTopBarForRoute)
Column(modifier = Modifier.background(brush = Brush.horizontalGradient(listOf(ClayPrimary, ClayPrimary)))) {
TopAppBar(
title = { Text(text = "...", fontSize = 24.sp, fontWeight = FontWeight.Bold,
modifier = Modifier.padding(vertical = 16.dp).background(Color.Transparent)) },
navigationIcon = { ... }, // or actions = { ... }, exactly as in the original
colors = TopAppBarDefaults.topAppBarColors(
containerColor = Color.Transparent,
scrolledContainerColor = Color.Transparent,
titleContentColor = Color.White,
navigationIconContentColor = Color.White,
actionIconContentColor = Color.White
)
)
}
// EXISTING SCREEN CONTENT, moved into Box(Modifier.weight(1f)) { ... }
}
}

Rules:
- Copy the exact title text, fontSize, fontWeight, padding, icons, contentDescription, and onClick code from the original block. Do not rewrite or "improve" them.
- Define the `transparentTopBarColors` value (the topAppBarColors preset) inside each screen, or inline it in each TopAppBar call.
- Add `@OptIn(ExperimentalMaterial3Api::class)` to every screen that uses TopAppBar.
- The top bar must stay fixed at the top and must not scroll with LazyColumn/scrollable content. Put the existing content in `Box(Modifier.weight(1f))` so it starts right below the bar, where Scaffold's innerPadding used to put it.
- Drop the commented-out HorizontalDivider blocks.
- Add all required imports in each file.

STEP 2: Mapping (route → screen → title → icon and click behavior)
Preserve each behavior exactly:

1. HomeRoutes → HomeScreen → "Admin Management" (title vertical padding 5.dp, NOT 16.dp). Action: Menu icon (Icons.Default.Menu, "Open Drawer") → onMenuClick.
2. productroutes → ProductScreen → "All Product". Action: Menu icon → onMenuClick.
3. HistoryRoutes → HistoryScreen → "History". Action: Menu icon → onMenuClick.
4. ordersRoutes → OrderDetailsScreen → "Orders Screen". Action: Menu icon → onMenuClick.
5. UserSettingsRoutes → UserDetailsScreen (and UpdateUserDetailsScreen if it used this bar) → "User Detail Management". Nav icon: ArrowBack (Icons.AutoMirrored.Filled.ArrowBack, "Home") → navController.popBackStack().
6. AddProductRoutes → AddProductScreen → "Add Product". Nav icon: ArrowBack → navController.popBackStack().
7. specificProductRoutes → SpecificProductScreen → "ProductDetail". Nav icon: ArrowBack → navController.navigate(Routes.ProductRoutes()) { popUpTo(Routes.ProductRoutes()) { inclusive = true } } (keep this exact code and its comment).
8. updateProductRoutes → UpdateProductScreen → "Update Product Details". Nav icon: ArrowBack → navController.popBackStack().
9. orderDetailRoutes → the "Each User Orders" screen → title "Each User Orders". Nav icon: ArrowBack → navController.popBackStack() AND viewModel.clearGetUsersOrdersState().
10. specificOrderRoutes → SpecificOrderScreen → "Specific Order". Nav icon: ArrowBack → navController.popBackStack() AND viewModel.clearGetOrderByIdState().

Profile, Settings, About, Splash, SignIn, SignUp, VerifyOtp have NO top bar (currently null). Leave them untouched.

Check which screen composable actually owns each route in my NavHost. If a route in getTopBarForRoute has no matching screen, tell me instead of guessing.

STEP 3: Drawer
The drawer state stays in NavApp (ModalNavigationDrawer). Do NOT move it. Add an `onMenuClick: () -> Unit` parameter to HomeScreen, ProductScreen, HistoryScreen, and OrderDetailsScreen. In NavApp create:

val openDrawer = {
Log.i("TAG", "DRAWER_DEBUG TopBar menu icon CLICKED on route: $currentRoute")
if (isHomeScreen) coroutineScope.launch { drawerState.open() }
}

and pass `onMenuClick = openDrawer` at those four composable() call sites in the NavHost. Keep the current behavior (the drawer opens only on the Home screen).

STEP 4: Clean up NavApp
- Remove the whole `topBar = { ... }` parameter from the Scaffold.
- Delete `getTopBarForRoute` (and its file, since nothing uses it anymore) and the unused imports, including `android.R`.
- Keep bottomBar, the gradient Modifier.background, containerColor = Transparent, the drawer, and all LaunchedEffects unchanged.
- The NavHost uses Modifier.padding(innerPadding). With no top bar, innerPadding.top becomes 0, so bottom padding for the bottom bar still works. Verify that nothing gets double padding and nothing is hidden under the status bar. The TopAppBar's default window insets should still handle the status bar, and the gradient must still show behind it.

CONSTRAINTS
- No UI redesign: same colors, sizes, fonts, spacing, icons, gradient, behavior.
- Do not change ViewModel logic, routes, navigation graph, or the bottom bar.
- Screen signatures may only gain `onMenuClick` where needed. Update every call site.
- The code must compile. Show the complete updated NavApp and the changed top section of each screen file, not unchanged code below.