---
name: admin-screen-builder
description: Build or update one Medi Admin App screen at a time in Jetpack Compose (M3, adaptive, correct scroll, navigation and device access) using the ledger docs/ui_build_status.md. Use when continuing the admin UI build ("next screen", "continue UI batch").
---

# Admin screen builder

Admin-only app (no signup/customer features). Visual + navigation + device-access layer only.

## Inputs (each once)
1. `docs/ui_build_status.md` (ledger): resume at the first row not DONE. Shared component signatures live there; never reopen finished screens.
2. Spec path from the ledger: only the row for the current screen.
3. `app/src/main/java/com/saurabh/mediadminapp/data/v1/viewmodel_api.md` + `ui_followups.md`: VM access. Do not open MyViewModel.kt / ScreensState / Repository.kt unless a compile error points there.
4. `response.md`: grep only the endpoint, fields, enums, limits, error codes the screen needs. Never guess; unknown -> nullable + `// unverified`.
5. Design image `docs/design/<screen>.png` if present (read once).

## Screen loop
1. Existing file -> targeted edits; keep VM calls, state names, working logic. Missing -> one new file (<400 lines).
2. VM fn/state missing but Repository has it -> minimal additive MyViewModel edit (UiState<T>, collectInto, pagination fields, one idempotency key per tap), append row to viewmodel_api.md. Repository lacks it -> BLOCKED.
3. Compile once: `./gradlew :app:compileDebugKotlin -q 2>&1 | grep -E "^e: " | head -40` (max 2 per screen).
4. Update ledger row (status, file, route, VM fns, device access, note <= 1 line).
5. Stop after 4 screens or ~70% context.

## Definition of done (per screen)
- States: skeleton loading, empty, error + Retry, offline banner (AppScreenScaffold has it), content.
- `appError.fields` under inputs (supportingText); `appError.code` messages (OUT_OF_STOCK, PRESCRIPTION_NOT_VERIFIED, ALREADY_SOLD, INVALID_TRANSITION, BATCH_EXPIRED, ACCOUNT_*, RATE_LIMITED, PAYLOAD_TOO_LARGE, UNSUPPORTED_MEDIA_TYPE).
- Snackbars via VM uiEvent (NavApp host) or local SnackbarHostState.
- Destructive actions through ConfirmDialog (error-coloured confirm).

## Adaptive rules
- `rememberWindowSize()`: COMPACT <600, MEDIUM 600-839, EXPANDED >=840.
- COMPACT single column; MEDIUM/EXPANDED `AdaptiveContent` (forms 560dp, lists 840dp), card grids via `adaptiveGridCells()`, `TwoPane` list->detail on EXPANDED for Orders, Products, Users, Admins, Suppliers, Batches; forms two-column on EXPANDED.
- Landscape and font scale 1.3: no fixed text heights, maxLines + ellipsis.

## Scroll rules
- One vertical scroller per screen; never nest vertical scrollables without bounded height.
- Lazy lists: stable `key` + `contentType`; `PagedLazyColumn` for paged lists (load-more guarded, footer spinner, pull-to-refresh, bottom padding for FAB).
- TopAppBar scrollBehavior + nestedScroll (AppScreenScaffold does this).
- Save scroll state (rememberLazyListState is saveable; hoist per tab).
- Forms: `FormScaffold` (verticalScroll + imePadding + navigationBarsPadding), IME Next/Done, scroll to first error.
- FAB hides on scroll down; sticky headers for grouped lists; LazyRow for chips; derivedStateOf for derived values.

## Navigation rules
- Keep existing route objects; add routes only for new screens (Routes.kt + NavApp composable).
- Tabs: Dashboard, Products, Orders, (Inventory), More. `popUpTo(Home){saveState}; launchSingleTop; restoreState`. Re-tap = pop to tab root. Back on tab root -> Dashboard -> exit.
- Auth/session guard lives in NavApp only; logout/blocked/session invalid -> `popUpTo(0){inclusive=true}`.
- One-shot results via previousBackStackEntry.savedStateHandle; validate args (missing id -> NotFound/ErrorState).
- Bottom bar/rail only on tab roots; detail/scanner/form screens show a back arrow.

## Device-access rules
- Request permissions in context only via `rememberPermissionState(CAMERA_PERMISSION)`; handle Denied (rationale) and PermanentlyDenied (`openSettings()`).
- Images: `ImageSourceSheet` (camera hidden when `hasCamera()` false) + Photo Picker + TakePicture/FileProvider (`createImageUri()`); product images keep uCrop.
- Prescriptions: `OpenDocument` with app-config `allowed_mime_types`.
- Every upload goes through `prepareUpload(context, uri, viewModel.cachedAppConfig()?.uploads, imageOnly)`; map 413/415 via `AppError.uploadMessage()`.
- Scanner: CameraX + ML Kit, lifecycle-safe unbind, torch, haptic, debounce, manual entry fallback.
- Invoice: ACTION_SEND / PrintManager; no storage permissions. No location/contacts/SMS/biometric/storage permissions.

## Token rules
- Colours/type/shapes only from MaterialTheme (no `Color(0x` in ui/screens). Light + dark.
- grep before reading; read line ranges; targeted edits; never re-read a written file.
- Final reply: `UI batch done. Screens DONE: X/36, BLOCKED: Y, next: <screen>. Build: OK|FAIL.`
