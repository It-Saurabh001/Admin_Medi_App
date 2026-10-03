# 🎨 MediAdminApp — Complete UI Architecture & Design Documentation

Comprehensive technical documentation of the user interface architecture, design tokens, component hierarchy, animations, and end-to-end data connections for the **MediAdminApp** Android Jetpack Compose codebase.

---

## 📑 Table of Contents
1. [Design Philosophy & Core Principles](#1-design-philosophy--core-principles)
2. [Design Tokens & Theming System](#2-design-tokens--theming-system)
   - [Color Palette & Semantic Roles](#color-palette--semantic-roles)
   - [Gradient Specifications](#gradient-specifications)
   - [Hardware Canvas Shadows & 5-Layer Pillow Lighting](#hardware-canvas-shadows--5-layer-pillow-lighting)
   - [Typography Scale](#typography-scale)
   - [Geometry, Radii & Insets](#geometry-radii--insets)
3. [Global Navigation & App Shell Architecture](#3-global-navigation--app-shell-architecture)
   - [Navigation Architecture Graph](#navigation-architecture-graph)
   - [Route Registry (`Routes.kt`)](#route-registry-routeskt)
   - [Navigation Shell (`NavApp.kt`)](#navigation-shell-navappkt)
   - [Contextual Top App Bar (`getTopBarForRoute`)](#contextual-top-app-bar-gettopbarforroute)
   - [Tactile Bottom Navigation Bar](#tactile-bottom-navigation-bar)
   - [Navigation Drawer (`ModalNavigationDrawer`)](#navigation-drawer-modalnavigationdrawer)
4. [Comprehensive Screen-by-Screen Breakdown](#4-comprehensive-screen-by-screen-breakdown)
   - [1. Splash Screen](#1-splash-screen)
   - [2. Sign In Screen](#2-sign-in-screen)
   - [3. Sign Up Screen](#3-sign-up-screen)
   - [4. OTP Verification Screen](#4-otp-verification-screen)
   - [5. Home Dashboard / User Management](#5-home-dashboard--user-management)
   - [6. Product Catalog Showcase](#6-product-catalog-showcase)
   - [7. Product Detail Screen](#7-product-detail-screen)
   - [8. Add Product Screen](#8-add-product-screen)
   - [9. Update Product Screen](#9-update-product-screen)
   - [10. Orders Fulfillment Hub](#10-orders-fulfillment-hub)
   - [11. Specific Order Receipt Screen](#11-specific-order-receipt-screen)
   - [12. History Transaction Ledger](#12-history-transaction-ledger)
   - [13. User Details Screen](#13-user-details-screen)
   - [14. Update User Details Screen](#14-update-user-details-screen)
   - [15. Admin Profile Screen](#15-admin-profile-screen)
   - [16. Settings Screen](#16-settings-screen)
   - [17. About Screen](#17-about-screen)
5. [Reusable UI Component Ecosystem](#5-reusable-ui-component-ecosystem)
6. [Motion, Physics & Animation Matrix](#6-motion-physics--animation-matrix)
7. [Data Flow, State Management & Networking](#7-data-flow-state-management--networking)

---

## 1. Design Philosophy & Core Principles

The UI of **MediAdminApp** is designed around tactile physics, high-contrast usability, and purpose-built screens.

```
┌────────────────────────────────────────────────────────────────────────┐
│                        CORE DESIGN TENETS                             │
├────────────────────────────────────────────────────────────────────────┤
│ 1. ANTI-COOKIE-CUTTER ARCHITECTURE                                     │
│    Every screen possesses a bespoke structural layout tailored to its  │
│    domain (e.g., Timeline for history, Radar Deck for orders, Bento    │
│    Grid for settings, Asymmetric Showcase for catalog).                │
│                                                                        │
│ 2. TWO-TIER SCANNABLE HIERARCHY                                        │
│    List cards present minimal, high-scannability data + spring "Details"│
│    button. Deep specifications reside exclusively on detail screens.   │
│                                                                        │
│ 3. HARDWARE-CANVAS CLAYMORPHISM (ZERO NAIVE ELEVATION)                 │
│    Modifier.shadow() and standard elevation are avoided on clay items  │
│    to eliminate RenderNode white-corner artifacts. All shadows are     │
│    rendered via BlurMaskFilter on hardware canvas.                     │
│                                                                        │
│ 4. MULTI-LAYER PILLOW LIGHTING                                         │
│    Tactile realism is simulated with a 5-layer shader model: Top       │
│    Specular Highlight, Bottom Contact Shadow, Side Curvature Shading,  │
│    Top Rim Highlight, and Bottom Rim Shadow.                           │
│                                                                        │
│ 5. ZERO TEXT WASHOUT                                                   │
│    Custom input wrappers enforce high-contrast charcoal (#1E293B)      │
│    across focused, unfocused, and error states.                        │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Design Tokens & Theming System

### Color Palette & Semantic Roles
Source files: `ui/theme/Color.kt` and `ui/theme/ClayTokens.kt`.

| Token Name | Hex Value | Primary Application |
|---|---|---|
| `ClayPrimary` | `#6C63FF` | Brand Electric Violet: Primary buttons, active indicators, focused outlines |
| `ClayPrimaryLight` | `#9D97FF` | Highlight accents, secondary glow borders |
| `ClayPrimaryDark` | `#4B44CC` | Pressed button states, deep edge shading |
| `ClaySecondary` | `#48CAE4` | Sky Cyan: Gradient accent, complementary badges |
| `ClaySecondaryLight` | `#80DFEF` | Light specular wash on hero headers |
| `ClayAccent` | `#FF6584` | Coral Pink: Ambient backdrop blobs, blocked badges, destructive tags |
| `ClaySuccess` / `StateApproved` | `#10B981` | Emerald: Verified accounts, fulfilled orders, in-stock pills |
| `ClayWarning` / `StatePending` | `#F59E0B` | Amber: In-progress tasks, pending reviews, low-stock warnings |
| `ClayError` | `#EF4444` | Crimson Red: Out-of-stock indicators, delete buttons, error alerts |
| `ClayTextPrimary` | `#1E293B` | High-contrast deep slate for typography, values, input text |
| `ClayTextSecondary` | `#64748B` | Medium slate for captions, metadata, resting icons |
| `ClayTextOnDark` | `#FFFFFF` | Crisp white for surfaces over gradients and primary buttons |
| `ClayTextMuted` | `#94A3B8` | Subdued slate for inactive placeholders and disabled labels |
| `ClayCardBg` | `#FAF9FF` | Lavender-tinted white base for raised clay cards |
| `ClayFieldBg` | `#F0EEFF` | Soft recessed lavender for inset containers and input backgrounds |
| `ClayScreenBg` | `#F5F3FF` | Background color for root screen scaffolding |
| `ClayBorder` | `#D0C8FF` | Default hairline border for cards and unselected inputs |
| `ClayCardShadow` | `#6C63FF` (25%-30% α) | Tinted colored shadow replacing standard grey drop shadows |

### Gradient Specifications

```kotlin
// Global Background & Auth Surfaces
val ClayGradient = Brush.verticalGradient(listOf(Color(0xFF6C63FF), Color(0xFF48CAE4)))

// Interactive Elements & Primary Buttons
val ClayButtonGradient = Brush.horizontalGradient(listOf(Color(0xFF6C63FF), Color(0xFF48CAE4)))
val FilterPillGradientActive = Brush.horizontalGradient(listOf(Color(0xFF6C63FF), Color(0xFF48CAE4)))
val FilterPillGradientInactive = Brush.horizontalGradient(listOf(Color.White, Color(0xFFFAF9FF)))

// Domain-Specific Canvas Backdrops
val ClayProductGradient = Brush.verticalGradient(listOf(Color(0xFF0EA5E9), Color(0xFF6C63FF)))
val ClayOrderGradient   = Brush.verticalGradient(listOf(Color(0xFF10B981), Color(0xFF48CAE4)))
val ClayHistoryGradient = Brush.verticalGradient(listOf(Color(0xFFFF6584), Color(0xFF6C63FF)))
val SignUpGradient      = Brush.verticalGradient(listOf(Color(0xFFFF6584), Color(0xFF6C63FF)))
val OtpGradient         = Brush.verticalGradient(listOf(Color(0xFF48CAE4), Color(0xFF6C63FF)))
```

### Hardware Canvas Shadows & 5-Layer Pillow Lighting

#### 1. Hardware Canvas Blur Shadow (Eliminates RenderNode artifacts)
```kotlin
Modifier.drawBehind {
    val cr = cornerRadius.toPx()
    drawIntoCanvas { canvas ->
        canvas.nativeCanvas.drawRoundRect(
            4.dp.toPx(), 6.dp.toPx(),
            size.width - 4.dp.toPx(), size.height + 4.dp.toPx(),
            cr, cr,
            android.graphics.Paint().apply {
                isAntiAlias = true
                color = android.graphics.Color.argb((60 * shadowAlpha).toInt(), 108, 99, 255)
                maskFilter = android.graphics.BlurMaskFilter(
                    18.dp.toPx(),
                    android.graphics.BlurMaskFilter.Blur.NORMAL
                )
            }
        )
    }
}
```

#### 2. Five-Layer Pillow Lighting Model
```kotlin
Modifier.drawWithContent {
    drawContent()
    val cr = CornerRadius(cornerRadius.toPx())
    val rimWidth = 1.8.dp.toPx()

    // Layer 1: Top Specular Highlight
    drawRoundRect(
        brush = Brush.verticalGradient(
            0.00f to Color.White.copy(alpha = 0.72f),
            0.28f to Color.White.copy(alpha = 0.15f),
            0.50f to Color.White.copy(alpha = 0.00f)
        ),
        size = size, cornerRadius = cr
    )
    // Layer 2: Bottom Contact Shadow
    drawRoundRect(
        brush = Brush.verticalGradient(
            0.50f to Color.Black.copy(alpha = 0.00f),
            0.80f to Color.Black.copy(alpha = 0.05f),
            1.00f to Color.Black.copy(alpha = 0.18f)
        ),
        size = size, cornerRadius = cr
    )
    // Layer 3: Side Curvature Shading
    drawRoundRect(
        brush = Brush.horizontalGradient(
            0.00f to Color.Black.copy(alpha = 0.06f),
            0.12f to Color.Black.copy(alpha = 0.00f),
            0.88f to Color.Black.copy(alpha = 0.00f),
            1.00f to Color.Black.copy(alpha = 0.06f)
        ),
        size = size, cornerRadius = cr
    )
    // Layer 4: Top Rim Highlight (Stroke)
    drawRoundRect(
        brush = Brush.verticalGradient(
            0.00f to Color.White.copy(alpha = 1.00f),
            0.35f to Color.White.copy(alpha = 0.28f),
            0.60f to Color.White.copy(alpha = 0.00f)
        ),
        size = size, cornerRadius = cr, style = Stroke(width = rimWidth)
    )
    // Layer 5: Bottom Rim Shadow (Stroke)
    drawRoundRect(
        brush = Brush.verticalGradient(
            0.42f to Color.Black.copy(alpha = 0.00f),
            0.80f to Color.Black.copy(alpha = 0.07f),
            1.00f to Color.Black.copy(alpha = 0.20f)
        ),
        size = size, cornerRadius = cr, style = Stroke(width = rimWidth)
    )
}
```

### Typography Scale
* `ClayH1`: `30.sp` / `ExtraBold` / `#FFFFFF`
* `ClayH2`: `22.sp` / `ExtraBold` / `#1E1B4B`
* `ClayH3`: `20.sp` / `Bold` / `#1E1B4B`
* `ClayCardTitle`: `16.sp` / `Bold` / `#1E1B4B`
* `ClayButtonText`: `15.sp` / `Bold` / `#FFFFFF` (Letter-spacing: `0.5.sp`)
* `ClayBody`: `14.sp` / `Medium` / `#1E293B` (Cards) or `#FFFFFF` (Gradients)
* `ClayInputText`: `15.sp` / `Medium` / `#1E293B`
* `ClayMetadata`: `12.sp` / `Medium` / `#6B6893`
* `ClayMicroBadge`: `11.sp` / `ExtraBold` / Uppercase, letter spacing `1.sp`

### Geometry, Radii & Insets
* **Cards & Containers:** `28.dp` primary corner radius; `22.dp` secondary tile radius.
* **Buttons & Inputs:** `54.dp` primary height with `50.dp` pill radius; `16.dp`-`18.dp` corner radius on inputs.
* **Canvas Paddings:** `20.dp` horizontal margins, `16.dp` top spacing, `90.dp`-`100.dp` bottom inset (clears floating navigation bar).

---

## 3. Global Navigation & App Shell Architecture

### Navigation Architecture Graph

```
                                [ SplashScreen ]
                                       │
                      ┌────────────────┴────────────────┐
               (Session Expired)                  (Session Active)
                      ▼                                 ▼
               [ SignIn Screen ]                 [ Home Dashboard ]
                      │                                 │
           ┌──────────┴──────────┐                      ├─► [ Products Catalog ]
           ▼                     ▼                      │     ├─► [ Specific Product ]
     [ SignUp Screen ]    [ OtpScreen ]                 │     ├─► [ Add Product ]
           │                     │                      │     └─► [ Update Product ]
           └─────────►───────────┘                      │
                                                        ├─► [ Orders Fulfillment ]
                                                        │     └─► [ Specific Order ]
                                                        │
                                                        ├─► [ History Ledger ]
                                                        │     └─► [ Specific Order ]
                                                        │
                                                        └─► [ Modal Navigation Drawer ]
                                                              ├─► [ Profile Screen ]
                                                              ├─► [ Settings Screen ]
                                                              ├─► [ About Screen ]
                                                              └─► [ Session Logout ]
```

### Route Registry (`Routes.kt`)
The navigation routes are declared in [`ui/screens/nav/Routes.kt`](file:///c:/Users/09sau/AndroidStudioProjects/MediAdminApp/app/src/main/java/com/saurabh/mediadminapp/ui/screens/nav/Routes.kt):
* `Routes.SplashRoutes`: Entry gate checking persistent session.
* `Routes.SignInRoutes`: Admin login screen.
* `Routes.SignUpRoutes`: Account registration.
* `Routes.VerifyOtpRoutes(userId)`: 2-Factor OTP verification.
* `Routes.HomeRoutes`: Primary dashboard (User Management).
* `Routes.ProductRoutes`: Catalog showcase.
* `Routes.SpecificProductRoutes(productId)`: Product detail view.
* `Routes.AddProductRoutes`: New product creation form.
* `Routes.UpdateProductRoutes(productId)`: Product update and cropping studio.
* `Routes.OrdersRoutes`: Orders list and fulfillment hub.
* `Routes.SpecificOrderRoutes(orderId)`: Detailed order milestone receipt.
* `Routes.HistoryRoutes`: Sales history and financial ledger.
* `Routes.UserDetailsRoutes(userId)`: User profile and clearance matrix.
* `Routes.UpdateUserDetailsRoutes(userId)`: User mutation form.
* `Routes.ProfileRoutes`: Admin credentials dossier.
* `Routes.SettingsRoutes`: Platform preferences and toggles.
* `Routes.AboutRoutes`: Application telemetry and architecture overview.

### Navigation Shell (`NavApp.kt`)
Managed by [`NavApp.kt`](file:///c:/Users/09sau/AndroidStudioProjects/MediAdminApp/app/src/main/java/com/saurabh/mediadminapp/ui/screens/nav/NavApp.kt):
* **Automatic Route Observation:** Inspects `currentBackStackEntryAsState()`.
* **Auth Route Exclusion (`isAuthRoute`):** Disables top bar, bottom bar, and drawer on `SplashRoutes`, `SignInRoutes`, `SignUpRoutes`, and `VerifyOtpRoutes`.
* **Session Lifecycle Listener:**
  * When `isLoggedIn = true` on an auth route $\rightarrow$ automatically navigates to `HomeRoutes`.
  * When `isLoggedIn = false` on a protected route $\rightarrow$ automatically resets backstack and redirects to `SignInRoutes`.
* **Gesture Locking:** `ModalNavigationDrawer(gesturesEnabled = isHomeScreen)` ensures drawer drag gestures only function on the root dashboard, avoiding back-swipe conflicts on child screens.

### Contextual Top App Bar (`getTopBarForRoute`)
Declared in `utils/utilityFunctions/getBarForRoute.kt`:
* Features a 100% transparent container (`containerColor = Color.Transparent`).
* On Top-Level Screens (`Home`, `Products`, `Orders`, `History`): Renders bold titles with a hamburger menu button that opens the navigation drawer.
* On Detail Screens (`SpecificProduct`, `AddProduct`, `UpdateProduct`, `UserDetails`, `SpecificOrder`): Renders navigation back arrows that pop the backstack or clean ViewModel states.

### Tactile Bottom Navigation Bar
* Displays 4 tabs: **Dashboard**, **Products**, **Orders**, and **History**.
* Custom pill-shaped indicator with spring corner animation (`18.dp` $\rightarrow$ `22.dp`).
* Hardware canvas shadow that illuminates beneath the active tab.
* Text expands dynamically on selection: `expandHorizontally() + fadeIn()`.

### Navigation Drawer (`ModalNavigationDrawer`)
* Custom gradient header card containing the admin's monogram avatar, full name, and email address.
* Material 3 Navigation items for **Profile**, **Settings**, and **About**.
* Distant bottom item for **Logout** styled in high-visibility crimson red (`#E53935`).

---

## 4. Comprehensive Screen-by-Screen Breakdown

---

### 1. Splash Screen
* **File:** `ui/screens/SplashScreen.kt`
* **Route:** `Routes.SplashRoutes`
* **Archetype:** Minimalist Gateway
* **Visual Elements:**
  * Center white circular well (`140.dp`) housing an Indigo `#6366F1` `MedicalServices` icon.
  * Title: "MediAdmin" (`32.sp`, `ExtraBold`, letter spacing `1.sp`).
  * Subtitle: "Managing Medical Logistics".
* **Animations & Timing:** Non-blocking `delay(1500.milliseconds)` in `LaunchedEffect(isUserLoggedIn)`.
* **Connections:** Observes `viewModel.isAdminLoggedIn`. Routes to `HomeRoutes` or `SignInRoutes`.

---

### 2. Sign In Screen
* **File:** `ui/screens/SignIn.kt`
* **Route:** `Routes.SignInRoutes`
* **Archetype:** Immersive Tactile Auth Shell
* **Visual Elements:**
  * Fullscreen `ClayGradient` with top-right white blob (`180.dp`, $\alpha=0.12$) and bottom-left coral blob (`120.dp`, $\alpha=0.20$).
  * Circular badge with pill emoji (`💊`, `36.sp`).
  * Raised `ClayCard` (`28.dp` radius, `24.dp` shadow) containing:
    * Email `ClayTextField` with `Email` icon.
    * Password `ClayTextField` with `Lock` icon and password visibility toggle.
    * "Forgot Password?" action button.
    * Primary gradient button (`54.dp` height) with embedded loading spinner.
    * Navigation link: "New admin? Create account".
* **Animations:** Button press scale reduction (`0.97f`); loading state cross-fade (`fadeIn()` / `fadeOut()`).
* **Connections:** Calls `viewModel.loginAdmin(email, password)`. Collects `viewModel.loginAdminState`. Navigates to `VerifyOtpRoutes(userId = response.admin_id)`.

---

### 3. Sign Up Screen
* **File:** `ui/screens/SignUp.kt`
* **Route:** `Routes.SignUpRoutes`
* **Archetype:** Multi-State Registration Canvas
* **Visual Elements:**
  * Coral-to-violet gradient background (`SignUpGradient`).
  * Multi-state `AnimatedContent` switching between `FORM`, `LOADING`, and `SUCCESS`.
  * Clustered registration form: Name, Email, Phone Number, Password, Confirm Password.
  * Floating primary action button.
* **Animations:** Smooth state transition via `fadeIn(tween(300)) togetherWith fadeOut(tween(200))`.
* **Connections:** Calls `viewModel.createAdmin(...)`. On success, displays a toast and pops back to `SignInRoutes`.

---

### 4. OTP Verification Screen
* **File:** `ui/screens/OtpScreen.kt`
* **Route:** `Routes.VerifyOtpRoutes(userId)`
* **Archetype:** Tactile PIN Matrix with Physics Error Shake
* **Visual Elements:**
  * Cyan-to-violet canvas (`OtpGradient`).
  * Circular shield icon badge (`🛡️`, `38.sp`).
  * 6 individual digit cells (`OtpDigitCell`) in a horizontal row.
  * Countdown timer (`01:00` $\rightarrow$ `00:00`) before enabling "Resend OTP".
* **Animations & Physics:**
  * **Keyframe Error Shake:** Card shakes horizontally on invalid input over 400ms:
    $$0 \rightarrow -12\text{f} \rightarrow +12\text{f} \rightarrow -8\text{f} \rightarrow +8\text{f} \rightarrow -4\text{f} \rightarrow +4\text{f} \rightarrow 0$$
  * Active cell gains 2dp electric violet border and elevated 8dp drop shadow.
* **Connections:** Calls `viewModel.verifyAdminOtp(adminId, otp)`. Collects `viewModel.verifyOtpState`. Persists tokens in `TokenManager` and navigates to `HomeRoutes`.

---

### 5. Home Dashboard / User Management
* **File:** `ui/screens/HomeScreen1.kt`
* **Route:** `Routes.HomeRoutes`
* **Archetype:** Executive Bento Hub & Role Clearance Matrix
* **Visual Elements:**
  * **Hero Metric Card (`ClayHeroOverviewCard`):** Displays total registered user count, verified percentage, and an interactive radial gauge drawn with `Canvas.drawArc` using a sweep gradient (`ClayPrimary` $\rightarrow$ `ClaySecondary`).
  * **Stat Tiles Row:** 3 mini-cards: Approved (Green), Pending (Amber), Blocked (Coral).
  * **Search & Filter Dock:** Pill-shaped search bar + horizontal `LazyRow` of filter capsules (**All Users**, **Approved**, **Pending**, **Blocked**).
  * **Two-Tier Scannable User Cards (`EachUserCard`):**
    * Monogram avatar (`A`, `S`, etc.).
    * Scannable details: User ID, Name, Email, Phone, Created Date.
    * Interactive real-time Approval Switch.
    * Spring-reactive button: `"Details"`.
* **Animations:** Gauge arc sweep animation ($0.0 \rightarrow \text{ratio}$ over 900ms via `FastOutSlowInEasing`); switch scale bounce on toggle.
* **Connections:** Calls `viewModel.getAllUsers()`. Collects `viewModel.getAllUserState` and `viewModel.isApprovedUser` for optimistic switch toggles. Navigates to `UserDetailsRoutes(userId)`.

---

### 6. Product Catalog Showcase
* **File:** `ui/screens/ProductScreen.kt`
* **Route:** `Routes.ProductRoutes`
* **Archetype:** Asymmetric Catalog Showcase & Inventory Deck
* **Visual Elements:**
  * **Recessed Search Field (`ClaySearchField`):** Search by medicine name or category.
  * **Floating Quick-Filter Dock:** Clay pill chips for categories: **All**, **Tablet**, **Capsule**, **Liquid**, **Injection**.
  * **Hero Inventory Analytics Card (`CatalogCircularStatsCard`):** Central `DonutChart` showing distribution, flanked by a 3-column breakdown strip (In Stock, Low Stock, Out Stock).
  * **Product List Cards (`AsymmetricProductCard`):**
    * Inset square image tile (86x86dp) with Coil async image loader and fallback icon.
    * Title, category chip, and dynamic stock indicator badge (Emerald $>10$, Amber $1-10$, Crimson $0$).
    * Split Price Deck: formatted INR price (`₹450`) next to a tactile `"Details"` button.
* **Animations:** Alternating card colors via `cardColors` list; spring press feedback.
* **Connections:** Calls `viewModel.getAllProduct()`. Navigates to `SpecificProductRoutes(productId)`.

---

### 7. Product Detail Screen
* **File:** `ui/screens/SpecificProductScreen.kt`
* **Route:** `Routes.SpecificProductRoutes(productId)`
* **Archetype:** Deep Immersive Specification Hub & Media Viewer
* **Visual Elements:**
  * **Hero Image Well:** 200x200dp elevated clay container with hardware blur shadow. Tapping the image opens an interactive `FullscreenImagePreviewDialog`.
  * **Product Information Card (`ClayCard`):** Displays clean `ClayInfoRow` items separated by subtle dividers (Product ID, Name, Category, Price, Stock Units, Status).
  * **Dual Action Dock:**
    * `ClayPrimaryButton`: "Update Product Details"
    * `ClayDangerButton`: "Delete Product" (Red gradient `#EF4444` $\rightarrow$ `#FF6584`).
* **Connections:** Calls `viewModel.getSpecificProduct(productId)` and `viewModel.deleteProduct(productId)`. Navigates to `UpdateProductRoutes(productId)`.

---

### 8. Add Product Screen
* **File:** `ui/screens/AddProductScreen.kt`
* **Route:** `Routes.AddProductRoutes`
* **Archetype:** Form Ingestion Studio
* **Visual Elements:**
  * Image well with `AddAPhoto` icon and `ActivityResultContracts.PickVisualMedia`.
  * Inset clay container housing input fields:
    * Medicine Name (`MedicalServices` icon)
    * Price in INR (`PriceChange` icon)
    * Category (`Category` icon)
    * Initial Stock Quantity (`Inventory` icon)
  * Full-width `ClayPrimaryButton` ("Add Product to Catalog").
* **Connections:** Converts image URI to `MultipartBody.Part` via `toMultipartBodyPart()`. Calls `viewModel.addProduct(...)`. Refreshes previous screen on success via `savedStateHandle`.

---

### 9. Update Product Screen
* **File:** `ui/screens/UpdateProductScreen.kt`
* **Route:** `Routes.UpdateProductRoutes(productId)`
* **Archetype:** Advanced Mutation Studio with Image Cropping
* **Visual Elements:**
  * Dual-source image selection dialog: **Camera** or **Gallery**.
  * Integrated **UCrop** intent pipeline for square aspect ratio (`1:1`) cropping.
  * Form inputs pre-populated with existing product data.
* **Connections:** Calls `viewModel.updateProduct(...)` with optional updated multipart image and text form parameters.

---

### 10. Orders Fulfillment Hub
* **File:** `ui/screens/OrderDetailsScreen.kt`
* **Route:** `Routes.OrdersRoutes`
* **Archetype:** Dispatch Radar & 2-Column Order Deck
* **Visual Elements:**
  * **Fulfillment Hub Hero Deck:** Shows total revenue pipeline valuation (`₹1,24,500`) alongside 3 stat cards: **All Orders**, **Pending**, **Approved**.
  * **Animated Visibility:** Hero collapses when searching to maximise viewport space for filtered results.
  * **Two-Tier 2-Column Grid (`EachOrderCard`):**
    * Arranged via `items(filteredOrders.chunked(2))`.
    * Square aspect ratio card ($0.9$ ratio) with horizontal scroll for long drug names.
    * Order ID, status badge, compact inline approval switch, and compact detail trigger.
* **Animations:**
  * Hero exit/enter transitions: `slideInVertically(-it) + fadeIn()` / `slideOutVertically(-it) + fadeOut()`.
  * Grid key based on concatenated IDs (`rowOrders.joinToString("_")`) to prevent recomposition flicker.
* **Connections:** Calls `viewModel.getAllOrders()`. Switches trigger `viewModel.isApproveOrder(orderId, isApproved)`. "Details" button opens `SpecificOrderRoutes(orderId)`.

---

### 11. Specific Order Receipt Screen
* **File:** `ui/screens/SpecificOrderScreen.kt`
* **Route:** `Routes.SpecificOrderRoutes(orderId)`
* **Archetype:** Visual Milestone Receipt & Dispatch Action Hub
* **Visual Elements:**
  * **Interactive Milestone Timeline Node:** Displays order progress tracks with icons (`Schedule` $\rightarrow$ `LocalShipping` $\rightarrow$ `CheckCircle`).
  * **Digital Tax Invoice Card:** Clean typography layout resembling a physical prescription invoice with item breakdown, customer details, date stamp, and total billing.
  * **Floating Dispatch Action Bar:** Allows the administrator to toggle approval status directly from the detail view.
* **Connections:** Calls `viewModel.getOrderById(orderId)`. Collects `viewModel.getOrderByIdState`.

---

### 12. History Transaction Ledger
* **File:** `ui/screens/HistroyScreen.kt`
* **Route:** `Routes.HistoryRoutes`
* **Archetype:** Financial Ledger & Multi-Cadence Analytics
* **Visual Elements:**
  * **Transaction Ledger Hero Deck:** Aggregates completed sales data across three distinct cadences via `SalesFilter`: **Day**, **Month**, **Year**.
  * Dynamic `DonutChart` rendering total revenue and top 5 recent aggregated segments.
  * **Timeline Receipt Cards (`TimelineReceiptCard`):**
    * Milestone progress node with status icon.
    * Primary info: Customer name, units sold, date of sale, and transaction ID.
    * Prominent total value indicator and `"Details"` button linking directly to the corresponding order.
* **Animations:** Donut segment sweep angle animations (`animateFloatAsState` over 700ms); filter chip transitions.
* **Connections:** Calls `viewModel.getAllSellHistory()`. Collects `viewModel.getSellHistory`.

---

### 13. User Details Screen
* **File:** `ui/screens/UserDetailsScreen.kt`
* **Route:** `Routes.UserDetailsRoutes(userId)`
* **Archetype:** Security Dossier & Clearance Hub
* **Visual Elements:**
  * Large circular avatar header with user initials and status badge (**Approved**, **Pending**, or **Blocked**).
  * Clustered metadata cards: Personal Details (Email, Phone), Logistics (Address, Pin Code), and Security/Account Timestamps.
  * Bottom Action Deck: "Edit Details" and "Delete User Profile" (`ClayDangerButton`).
* **Connections:** Calls `viewModel.deleteUser(userId)`. Navigates to `UpdateUserDetailsRoutes(userId)`.

---

### 14. Update User Details Screen
* **File:** `ui/screens/UpdateUserDetailsScreen.kt`
* **Route:** `Routes.UpdateUserDetailsRoutes(userId)`
* **Archetype:** Account Clearance Editor
* **Visual Elements:**
  * Clay input fields for: Name, Email, Phone, Address, Pin Code.
  * Approval and Block toggle switches inside recessed clay clusters (`ClayFieldBg`).
  * "Save Changes" primary action button.
* **Connections:** Calls `viewModel.updateUser(...)` and dispatches state updates back to the parent screen.

---

### 15. Admin Profile Screen
* **File:** `ui/screens/ProfileScreen.kt`
* **Route:** `Routes.ProfileRoutes`
* **Archetype:** Admin Identity Hub
* **Visual Elements:**
  * Monogram badge with electric violet halo.
  * Clustered specification cards: Admin ID, Full Name, Email, Assigned Roles, and Access Clearance Level.
  * Destructive "Sign Out" button.
* **Connections:** Retrieves current authenticated admin via `viewModel.loggedInAdminId` and filters `viewModel.getAllAdminState`. Calls `viewModel.setAdminLoggedOut()` to clear session tokens.

---

### 16. Settings Screen
* **File:** `ui/screens/SettingsScreen.kt`
* **Route:** `Routes.SettingsRoutes`
* **Archetype:** Bento Grid Configuration & Diagnostics Hub
* **Visual Elements:**
  * Configuration tiles with tactile switches:
    * Dark Theme Mode (`DarkMode` / `LightMode` icons)
    * Push Notifications (`NotificationsActive`)
    * Auto-Sync Ledger Data (`Cached`)
  * Security section: Change Password, API Tokens, Session Management.
* **Animations:** Interactive spring switches with tactile thumb animations.

---

### 17. About Screen
* **File:** `ui/screens/AboutScreen.kt`
* **Route:** `Routes.AboutRoutes`
* **Archetype:** Telemetry & Architecture Showcase
* **Visual Elements:**
  * Application version, build hash, and architecture badges.
  * Real-time metrics counters displaying total users managed and total catalog items.
  * Tech stack breakdown: Jetpack Compose, Kotlin Coroutines & Flow, Retrofit2, Dagger Hilt, Coil, and Claymorphism Canvas Shaders.

---

## 5. Reusable UI Component Ecosystem

Located under `ui/screens/components/`:

```
ui/screens/components/
├── ClayCard.kt                 # 5-Layer pillow lighting + blur shadow card primitives
├── ClayButton.kt               # Spring press pills, outlined, danger, and filter chips
├── ClayTextField.kt            # High-contrast inputs preventing text washouts
├── ClayScreenShell.kt          # Fullscreen backdrop, animated loading, error & empty states
├── CircularAnalyticsChart.kt   # Pure Compose Canvas sweep-animated donut chart
├── EachOrderCard.kt            # 2-column grid order card with quick dispatch toggle
├── SalesCard.kt                # Transaction ledger card
├── SalesFilter.kt              # Day / Month / Year aggregation enum
└── Glassmorphism.kt            # Optional dark glassmorphism surfaces & input primitives
```

### Component Details

#### 1. `ClayCard` & `ClayCardOrderScreen`
* Foundation of all elevated surfaces.
* Combines `clayHardwareShadow(cornerRadius)` and `clayPillowLighting(cornerRadius)` with a 1.5dp pure white boundary stroke.

#### 2. `ClayTextField` & `ClaySearchField`
* Solves Material3 input washout bug by explicitly locking `focusedTextColor`, `unfocusedTextColor`, and `errorTextColor` to `ClayTextPrimary` (`#1E293B`).
* `ClaySearchField` implements a pill contour (`50.dp` shape) with inset search icon and single-line constraints.

#### 3. `ClayPrimaryButton` & `ClayDetailButton`
* Hardware canvas shadow button with spring press physics (`Spring.DampingRatioMediumBouncy`).
* Embedded loading indicator and multi-pass pillow reflection.

#### 4. `DonutChart`
* Completely standalone charting component built on Compose `Canvas.drawArc`.
* Accepts `List<DonutSegment>`, calculating angular sweeps dynamically.
* Animates angles from $0^\circ \rightarrow \text{targetAngle}$ over 700ms using `FastOutSlowInEasing`.
* Displays center metric callouts for revenue or inventory counts.

#### 5. `ClayScreenShell` Suite
* `ClayGradientBackdrop`: Standardised root wrapper providing global background gradients with two ambient glow blobs.
* `ClayLoadingScreen`: Clay card pedestal with pulsating alpha indicator (`infiniteRepeatable`).
* `ClayErrorScreen`: Warning icon, descriptive message, and retry button.
* `ClayEmptyState`: Centered emoji banner with contextual help subtitle.

#### 6. `FullscreenImagePreviewDialog`
* Located in `utils/utilityFunctions/FullscreenImagePreviewDialog.kt`.
* Full-screen black dialog for viewing full-resolution product images with dismiss controls.

---

## 6. Motion, Physics & Animation Matrix

| Interaction | Implementation Mechanism | Parameters / Specifications |
|---|---|---|
| **Button Press Physics** | `animateFloatAsState` | Scale $1.0\text{f} \rightarrow 0.95\text{f}$, shadow alpha $1.0\text{f} \rightarrow 0.5\text{f}$ (`Spring.DampingRatioMediumBouncy`) |
| **Filter Pill Selection** | `animateDpAsState` | Corner radius shifts $18\text{dp} \rightarrow 22\text{dp}$ (`Spring.DampingRatioLowBouncy`) |
| **Bottom Bar Tab Expansion** | `AnimatedVisibility` | `expandHorizontally() + fadeIn(tween(180))` |
| **OTP Error Shake** | `Animatable(0f)` | Keyframes over 400ms: $\pm 12\text{dp} \rightarrow \pm 8\text{dp} \rightarrow \pm 4\text{dp} \rightarrow 0$ |
| **Gauge / Chart Entry** | `animateFloatAsState` | Sweep angle $0^\circ \rightarrow 360^\circ$ over 700-900ms (`FastOutSlowInEasing`) |
| **Hero Card Collapse** | `AnimatedVisibility` | `slideInVertically(-it) + fadeIn()` / `slideOutVertically(-it) + fadeOut()` over 600ms |
| **Ambient Blob Drift** | `rememberInfiniteTransition` | Linear drift offset $\pm 40\text{dp}$ over 16,000ms with `RepeatMode.Reverse` |
| **Loading Pulse** | `rememberInfiniteTransition` | Alpha breathing $0.5\text{f} \leftrightarrow 1.0\text{f}$ over 900ms |

---

## 7. Data Flow, State Management & Networking

### Architecture Overview

```
[ Compose UI Layer ]
       │
       ▼  Observes immutable StateFlows (getAllUserState, getAllProduct, etc.)
[ MyViewModel (HiltViewModel) ]
       │
       ▼  Executes suspended calls on Dispatchers.IO
[ Repository ]
       │
       ▼  HTTP Client + Interceptors
[ Retrofit & OkHttp Services ]
       │
       ├─► AuthInterceptor (Injects Bearer Token)
       └─► TokenAuthenticator (Handles 401 & Automatic Refresh)
             │
             └─► (If Refresh Fails) TokenManager.sessionExpiredEvent
                   │
                   └─► MyViewModel clears session
                         │
                         └─► NavApp redirects to SignInRoutes
```

### State Management Patterns
1. **Immutable State Flow Streams:** Every network request emits `ResultState<T>` (Loading, Success, Error) wrapped in a screen-specific data class (e.g., `GetAllUserState`, `AddProductState`).
2. **Optimistic Updates:** Toggle switches for user approval (`isApprovedUser`) and order approval (`isApproveOrdder`) maintain state maps keyed by entity ID. The UI updates instantly upon user interaction while the API request processes asynchronously.
3. **Single-Shot Events (`Channel<UiEvent>`):** Toasts and snackbars are dispatched across a buffered channel to prevent recomposition loops.
4. **Session Recovery:** When the refresh token expires, `TokenAuthenticator` calls `TokenManager.invalidateSession()`. `MyViewModel` listens to this event and sets `isAdminLoggedIn.value = false`, causing `NavApp` to immediately return the user to the login screen.
