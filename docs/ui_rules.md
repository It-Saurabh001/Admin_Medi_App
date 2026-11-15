use this link to get design from stitch for theme and color code generatee
https://stitch.google.com/projects/6976770719674912209

U1 SCOPE: UI layer only. Never touch ViewModels, UiState, repositories, DTOs, network, nav routes or nav logic. If a screen needs different data, skip it and note it in handoff.
U2 EDIT IN PLACE: screens already exist. Find them by grep (names differ from `app_screens_navigation.md`, e.g. old names like HistroyScreen, NewOrder). Edit the existing file with small changes. Never rewrite a whole screen to change colour/spacing. If a screen already complies, leave it. Never create a new screen: if one from the md is missing, write `MISSING: <name>` in handoff and move on.
U3 TOKENS: no preamble, narration, explanations or file dumps in chat. grep/glob, read each file once, several files per tool call, skip build/, .gradle/, generated files. No screenshots, no running the app. Run `./gradlew :app:compileDebugKotlin` once per part. Final chat reply = exactly one line: `Part N done. Files changed: N. Build: OK|FAIL. Missing: <list or none>.`
U4 LOCK: `docs/ui_handoff.md` lists LOCKED files finished by earlier parts. Later parts must not open, grep inside or edit them; use only the component/theme signatures written in handoff. Exception: none.
U5 COLOURS only via `MaterialTheme.colorScheme.*`, always paired with their `on*` role. No hex, no `Color(0x..)`, no Color.White/Black/Gray outside ui/theme.
U6 TYPE only via `MaterialTheme.typography.*` (titleLarge top bars, titleMedium card titles, bodyMedium body, labelLarge buttons, labelMedium chips). No hard-coded fontSize.
U7 SHAPES only via `MaterialTheme.shapes.*`. No custom radii. Elevation tonal only (surfaceContainer* roles, Card filled/outlined/elevated). No gradients, glow, custom shadows, clay/neumorphic effects, decorative borders.
U8 LAYOUT: `Scaffold` on every screen; 16dp screen margin; 8dp grid (4/8/12/16/24); 48dp min touch target; one primary action per screen; max 2 colour roles besides surface (primary + one container); tertiary/lime only for small accents, never large areas or text.
U9 COMPONENTS: TopAppBar (detail screens: back arrow), NavigationBar (max 5, labels always, outlined/filled icon pair), one FAB max per screen, Button (primary) / FilledTonalButton (secondary) / OutlinedButton (tertiary) / TextButton (dialogs), OutlinedTextField with label + supportingText (backend `fields` errors) + isError, SearchBar, FilterChip (filters), AssistChip (actions), PrimaryTabRow (status tabs), SegmentedButton (2-4 exclusive options), ListItem (menus/settings), Card/OutlinedCard (data blocks), ModalBottomSheet (filters, reason input), AlertDialog (confirm), SnackbarHost (messages), PullToRefreshBox on lists, Linear/CircularProgressIndicator for loading.
U10 STATES: every list/detail screen has loading, empty, error(Retry) via the shared components. Lists: LazyColumn with stable keys + load-more footer.
U11 DESTRUCTIVE actions (delete, block, cancel, return, write-off, reject) go through AlertDialog with error-coloured confirm TextButton.
U12 ICONS: Material icons, Outlined default, Filled for selected nav item. No emoji. contentDescription on icon-only buttons. Must survive font scale 1.3 (maxLines + overflow).
U13 MOTION: default M3/Navigation transitions only.
U14 STATUS chips only via `StatusChip(status)`.
U15 HANDOFF at end of each part: append to `docs/ui_handoff.md`: Part status | Files changed (LOCKED) | Screens restyled (old name -> file) | MISSING screens | Skipped/notes. Never rewrite earlier content.
