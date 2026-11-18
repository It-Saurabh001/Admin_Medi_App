# 📱 GreenShield Pharmacy: Master UI Execution & Design System Guide

Ye single master document **AI Agent Execution Rules (`U1–U16`)**, **Complete 38-Color System**, **Touch, Press & Screen Movement Transitions**, **Keyboard/Scroll Handling**, aur **Pharma Product Card & Responsive Architecture** ko combine karta hai.

## 🛠️ PART 1: STRICT UI AGENT EXECUTION RULES (U1–U16)

> **CRITICAL DIRECTIVE:** Ye task sirf aur sirf **UI Layer** ke liye hai. Jo code ViewModel, Repository, ya Backend integration tak pehle se ban chuka hai, use bilkul chhedna nahi hai.

- **U1 SCOPE:** UI layer only. **Never touch ViewModels, UiState, repositories, DTOs, network, nav routes or nav logic.** If a screen needs different data, skip it and note it in handoff.

- **U2 EDIT IN PLACE:** Screens already exist. Find them by grep (names differ from `app_screens_navigation.md`, e.g. old names like `HistroyScreen`, `NewOrder`). Edit the existing file with small changes. Free to rewrite a whole screen to change colour/spacing and create custom function only if needed and required to change must need. If a screen already complies, leave it. Can create a new screen if missing and can use already present custom function make sure no dublicate function or screen should be created in project.

- **U3 TOKENS:** No preamble, narration, explanations or file dumps in chat. `grep`/`glob`, read each file once, several files per tool call, skip `build/`, `.gradle/, generated files. No screenshots, no running the app. Run `./gradlew :app:compileDebugKotlin`once per part. Final chat reply = exactly one line:`Part N done. Files changed: N. Build: OK|FAIL. Missing: <list or none>.`

- **U4 LOCK:** `docs/ui_handoff.md` lists `LOCKED` files finished by earlier parts. Later parts must not open, grep inside or edit them; use only the component/theme signatures written in handoff. Exception: none.

- **U5 COLOURS:** Strictly follow the **GreenShield 38-Color Guide** (Part 2 below) mapped through `MaterialTheme.colorScheme.*` and custom theme tokens inside `ui/theme`. Always pair colours with their matching `on*` / family role. No raw hex `Color(0x..)` or `Color.White/Black/Gray` outside `ui/theme`. **Golden Rule:** Touch states MUST stay in the same colour family and shift darker on press/hold.

- **U6 TYPE:** Only via `MaterialTheme.typography.*` (`titleLarge` top bars, `titleMedium` card titles, `bodyMedium` body, `labelLarge` buttons, `labelMedium` chips). No hard-coded `fontSize`. Must survive font scale `1.3x–1.5x` (`maxLines` + `TextOverflow.Ellipsis`).

- **U7 SHAPES & ELEVATION:** Shapes only via `MaterialTheme.shapes.*`. No custom radii. Elevation tonal/clean only (`Card` filled/outlined with `#DDE8E0`). No glow, custom shadows, clay/neumorphic effects, or decorative borders. Only allowed gradient: Hero/Header (`Green 800 -> Green 600 -> Green 400`).

- **U8 LAYOUT:** `Scaffold` on every screen; `16dp` screen margin; `8dp` grid (`4/8/12/16/24dp`); `48dp` min touch target; one primary action per screen; max 2 colour roles besides surface (primary + one semantic container); Logo colours (`#77C148`, `#088878`, `#F8F8E8`) ONLY on splash/logo, never in UI components or text.

- **U9 COMPONENTS:**
  - `TopAppBar` (detail screens: back arrow in `#F4F9F5` icon tile)

  - `NavigationBar` (max 5, labels always, Outlined/Filled icon pair, selected bg `Green 100 #D9F2E8`)

  - One `FAB` max per screen

  - `Button` (Primary `Green 600 #0B8461`) / `FilledTonalButton` (Secondary) / `OutlinedButton` (White + `#DDE8E0` border) / `TextButton` (dialogs)

  - `OutlinedTextField` with label + `supportingText` (backend `fields` errors) + `isError` (Unfocused border `#DDE8E0`, Focused `2dp Green 500 #129B73`, Error `2dp Red 600 #D13440`)

  - `SearchBar`, `FilterChip` (filters), `AssistChip` (actions), `PrimaryTabRow` (status tabs), `SegmentedButton` (2–4 exclusive options), `ListItem` (menus/settings), `Card`/`OutlinedCard` (data blocks), `ModalBottomSheet` (filters, reason input), `AlertDialog` (confirm), `SnackbarHost` (messages), `PullToRefreshBox` on lists, `LinearProgressIndicator`/`CircularProgressIndicator` for loading.

- **U10 STATES:** Every list/detail screen has `loading`, `empty`, `error(Retry)` via shared components. Lists: `LazyColumn` with stable `key` + load-more footer.

- **U11 DESTRUCTIVE:** Actions (delete, block, cancel, return, write-off, reject) go through `AlertDialog` with `Red 600 (#D13440)` confirm button/text.

- **U12 ICONS:** Material icons, Outlined default, Filled for selected nav item. No emoji in production UI. `contentDescription` on icon-only buttons.

- **U13 MOTION, TOUCH & SCREEN TRANSITIONS (CRITICAL):**
  - **Disable default M3 water-wave ripple (`indication = null`).** Use GreenShield family shade transitions instead.

  - **Color transition duration:** `100–120ms`.

  - **Scale on press:** Shrink to `0.97f` ONLY on Standalone Buttons and Standalone Tappable Cards.

  - **List rows:** DO NOT apply scale animation to list rows (only shift background shade on press/hold).

  - **Screen Movement & Transitions:** Follow the exact directional rules in **Part 3 (Section 2)** — Forward navigation enters from **Right $\rightarrow$ Left**, Back/Pop exits **Left $\rightarrow$ Right**, Bottom Nav tabs use **Crossfade** (no horizontal slide), and Modals/Sheets enter **Bottom $\rightarrow$ Top**. Do not alter nav routes (`U1`).

- **U14 STATUS CHIPS:** Only via `StatusChip(status)` mapped to GreenShield families:
  - **In Stock / Available / Success:** Bg `Green 100 (#D9F2E8)` + Text `Green 600 (#0B8461)`

  - **Low Stock / Near Expiry / Warning:** Bg `Amber 100 (#FFEBC8)` + Bold Text `Amber 600 (#B8730A)`

  - **Out of Stock / Expired / Danger:** Bg `Red 100 (#FFDDE0)` + Bold Text `Red 600 (#D13440)`

  - **Batches / Movements / Info:** Bg `Teal 100 (#D0F1F3)` + Text `Teal 600 (#0F7C86)`

- **U15 PHARMA PRODUCT UI:** Medicine cards/rows must display:
  - `1:1` Square image container with `ContentScale.Fit` (never crop medicine strips/bottles).

  - Smart vector fallback icon (Tablet/Syrup/Injection/Ointment) on `Teal 50 (#EAF8F9)` or `Field Fill (#E8F0EA)` when image is null/missing.

  - Semantic pills (`Rx Required`, `Low Stock`, `Expired`, `Batch/Pack info`).

- **U16 HANDOFF:** At end of each part, append to `docs/ui_handoff.md`:
  `Part status | Files changed (LOCKED) | Screens restyled (old name -> file) | MISSING screens | Skipped/notes`. Never rewrite earlier content.

## 🎨 PART 2: GREENSHIELD PHARMACY OFFICIAL COLOR GUIDE (38 COLORS)

Ye guide logo se bani hai. App ka main rang **Green 600 (`#0B8461`)** hai, aur baaki sab colors us se milte-julte families mein baante gaye hain.

**Kul colors: 38** (35 app ke liye + 3 sirf logo ke liye)

| Family           | Kitne shades | Kaam                                   |
| ---------------- | ------------ | -------------------------------------- |
| **Green**        | 10           | Main brand, buttons, selected, success |
| **Amber (warm)** | 7            | Warning: low stock, near expiry        |
| **Red (warm)**   | 6            | Danger: out of stock, expired, delete  |
| **Teal (cool)**  | 6            | Info: batches, movements, tags         |
| **Neutral**      | 6            | Text, background, borders              |
| **Logo only**    | 3            | Sirf logo/splash, UI mein nahi         |

### 1. Golden Rule: Touch Par Same Family Ka Shade

Kisi cheez ka rang jis family ka hai, touch par uska rang usi family ka rahega. Rang kabhi family badal kar nahi jayega.

- **Green cheez dabao** $\rightarrow$ green ka doosra shade.

- **Amber (warm) cheez dabao** $\rightarrow$ amber ka doosra shade.

- **Red cheez dabao** $\rightarrow$ red ka doosra shade.

- **Teal cheez dabao** $\rightarrow$ teal ka doosra shade.

**Shade kitna badle (2 niyam):**

| Cheez ka background                        | Press par                                                   | Long press / hold par                                       |
| ------------------------------------------ | ----------------------------------------------------------- | ----------------------------------------------------------- |
| **Halka** (50, 100: card, row, chip, tile) | Ek step gehra ($50 \rightarrow 100$, $100 \rightarrow 200$) | Do step gehra ($50 \rightarrow 200$, $100 \rightarrow 300$) |
| **Gehra** (600: solid button)              | Ek step gehra ($600 \rightarrow 700$)                       | Do step gehra ($600 \rightarrow 800$)                       |

> **Note:** Press aur hold ke beech fark hamesha dikhna chahiye. Agar dono ek jaise dikhein, to hold wala shade ek aur gehra karo.

### 2. Green Family (10 Shades) — Brand aur Success

| Naam          | Hex           | Kab use karna hai                                                       |
| ------------- | ------------- | ----------------------------------------------------------------------- |
| **Green 50**  | `#EFF9F5`     | Poori screen ka background. Tappable row ka press shade                 |
| **Green 100** | `#D9F2E8`     | Selected chip, selected nav item, icon tile, success pill ka background |
| **Green 200** | `#B3E5D1`     | Border, progress bar ka khali hissa, chip ka press shade                |
| **Green 300** | `#80D2B3`     | Disabled button, chip ka hold shade                                     |
| **Green 400** | `#3FB88F`     | Gradient ka halka sira, chhote decoration                               |
| **Green 500** | `#129B73`     | Focus border (`TextField`), links ka halka accent                       |
| **Green 600** | **`#0B8461`** | **Primary rang: buttons, active icon, selected text, links**            |
| **Green 700** | `#096B50`     | Primary button dabane par, status bar                                   |
| **Green 800** | `#075340`     | Primary button hold par, gehra gradient                                 |
| **Green 900** | `#053A2D`     | Sabse gehra green text (kam use)                                        |

#### Green Cheezon Ka Touch Behavior

| Cheez                                               | Normal                     | Press            | Long press / hold |
| --------------------------------------------------- | -------------------------- | ---------------- | ----------------- |
| **Primary button** (Add product, Save)              | 600 `#0B8461`              | 700 `#096B50`    | 800 `#075340`     |
| **Text link** ("View all")                          | 600 `#0B8461`              | 700 `#096B50`    | 800 `#075340`     |
| **Selected chip**                                   | 100 `#D9F2E8` + border 600 | 200 `#B3E5D1`    | 300 `#80D2B3`     |
| **Selected bottom-nav item**                        | 100 `#D9F2E8`              | 200 `#B3E5D1`    | 300 `#80D2B3`     |
| **Secondary button** (white + green/outline border) | White `#FFFFFF`            | 100 `#D9F2E8`    | 200 `#B3E5D1`     |
| **Product row / list row**                          | White `#FFFFFF`            | 50 `#EFF9F5`     | 100 `#D9F2E8`     |
| **Tappable card**                                   | White `#FFFFFF`            | 50 `#EFF9F5`     | 100 `#D9F2E8`     |
| **Icon button** (bell, scan, back)                  | `#F4F9F5`                  | 100 `#D9F2E8`    | 200 `#B3E5D1`     |
| **Unselected chip**                                 | White `#FFFFFF`            | 50 `#EFF9F5`     | 100 `#D9F2E8`     |
| **Success pill** (In stock, Available)              | bg 100, text 600           | bg 200, text 700 | bg 300, text 800  |

### 3. Amber Family (7 Shades) — Warm: Warning

| Naam          | Hex           | Kab use karna hai                                        |
| ------------- | ------------- | -------------------------------------------------------- |
| **Amber 50**  | `#FFF8EB`     | Warning card ka halka background, row press shade        |
| **Amber 100** | `#FFEBC8`     | Warning pill ka background (Low stock, Near expiry)      |
| **Amber 200** | `#FFD791`     | Warning pill ka press shade, border                      |
| **Amber 400** | `#E8A23A`     | Warning icon, chart ka warning bar                       |
| **Amber 600** | **`#B8730A`** | **Warning ka text aur icon (white bg par padhne layak)** |
| **Amber 700** | `#965C08`     | Warning button ya text dabane par                        |
| **Amber 800** | `#754806`     | Warning button ya text hold par                          |

#### Amber Cheezon Ka Touch Behavior

| Cheez                                     | Normal                     | Press                      | Long press / hold          |
| ----------------------------------------- | -------------------------- | -------------------------- | -------------------------- |
| **Warning pill / chip** (tappable)        | bg 100 `#FFEBC8`, text 600 | bg 200 `#FFD791`, text 700 | bg 400 `#E8A23A`, text 800 |
| **Warning card** (e.g. "12 low stock")    | bg 50 `#FFF8EB`            | bg 100 `#FFEBC8`           | bg 200 `#FFD791`           |
| **Warning alert row**                     | White `#FFFFFF`            | 50 `#FFF8EB`               | 100 `#FFEBC8`              |
| **Warning text link** ("Restock now")     | 600 `#B8730A`              | 700 `#965C08`              | 800 `#754806`              |
| **Warning solid button** ("Review stock") | 600 `#B8730A`              | 700 `#965C08`              | 800 `#754806` (text white) |

### 4. Red Family (6 Shades) — Warm: Danger

| Naam        | Hex           | Kab use karna hai                                   |
| ----------- | ------------- | --------------------------------------------------- |
| **Red 50**  | `#FFF1F2`     | Error card ka halka background, row press shade     |
| **Red 100** | `#FFDDE0`     | Danger pill ka background (Out of stock, Expired)   |
| **Red 200** | `#FFBFC5`     | Danger pill press shade, error border               |
| **Red 600** | **`#D13440`** | **Danger text, icon, delete button, error message** |
| **Red 700** | `#B52A35`     | Danger button dabane par                            |
| **Red 800** | `#8F1F29`     | Danger button hold par                              |

#### Red Cheezon Ka Touch Behavior

| Cheez                                 | Normal                     | Press                      | Long press / hold          |
| ------------------------------------- | -------------------------- | -------------------------- | -------------------------- |
| **Danger button** (Delete, Write-off) | 600 `#D13440`              | 700 `#B52A35`              | 800 `#8F1F29`              |
| **Danger pill / chip** (tappable)     | bg 100 `#FFDDE0`, text 600 | bg 200 `#FFBFC5`, text 700 | bg 200 `#FFBFC5`, text 800 |
| **Expired / error card**              | bg 50 `#FFF1F2`            | bg 100 `#FFDDE0`           | bg 200 `#FFBFC5`           |
| **Danger text link** ("Remove")       | 600 `#D13440`              | 700 `#B52A35`              | 800 `#8F1F29`              |
| **Error TextField border**            | 600 `#D13440`              | 700 `#B52A35` (focus)      | \-                         |

### 5. Teal Family (6 Shades) — Cool: Info

Teal logo ke capsule ka rang hai. Isse info aur neutral actions (batches, stock movements) ke liye use karo, taaki wo green se thoda alag dikhein.

| Naam         | Hex           | Kab use karna hai                                    |
| ------------ | ------------- | ---------------------------------------------------- |
| **Teal 50**  | `#EAF8F9`     | Info card background, row press shade                |
| **Teal 100** | `#D0F1F3`     | Icon tile (batches, movements), info pill background |
| **Teal 200** | `#A6E2E6`     | Icon tile press shade                                |
| **Teal 600** | **`#0F7C86`** | **Info icon aur text**                               |
| **Teal 700** | `#0C636B`     | Info icon/text dabane par                            |
| **Teal 800** | `#094C52`     | Info icon/text hold par                              |

#### Teal Cheezon Ka Touch Behavior

| Cheez                                         | Normal                     | Press                      | Long press / hold          |
| --------------------------------------------- | -------------------------- | -------------------------- | -------------------------- |
| **Icon tile** (View batches, Stock movements) | bg 100 `#D0F1F3`, icon 600 | bg 200 `#A6E2E6`, icon 700 | bg 200 `#A6E2E6`, icon 800 |
| **Info pill** (tappable)                      | bg 100, text 600           | bg 200, text 700           | bg 200, text 800           |
| **Info card**                                 | bg 50 `#EAF8F9`            | bg 100 `#D0F1F3`           | bg 200 `#A6E2E6`           |
| **Info text link**                            | 600 `#0F7C86`              | 700 `#0C636B`              | 800 `#094C52`              |

> **Important Rule:** Row jis par icon tile teal hai, uski poori row ka press shade **Green 50 / 100** hi rahega (kyunki row card ka hissa hai). Sirf tile apne teal family ka press shade lega agar tile akela tappable ho.

### 6. Neutral Family (6 Colors) — Text, Surface, Lines

Ye sab hara-bhura (green-tinted) grays hain, taaki green ke saath mel khayein.

| Naam                 | Hex       | Kab use karna hai                                                   |
| -------------------- | --------- | ------------------------------------------------------------------- |
| **White**            | `#FFFFFF` | Card, top bar, bottom bar, button ke upar ka text                   |
| **Icon button fill** | `#F4F9F5` | Chhote square buttons (bell, scan, back) ka normal background       |
| **Field fill**       | `#E8F0EA` | Info tile ka background (Category, Pack size wale boxes), input box |
| **Ink**              | `#0F2A1F` | Heading aur main text                                               |
| **Muted**            | `#60776A` | Subtitle, hint, placeholder, unselected icon aur label              |
| **Outline**          | `#DDE8E0` | Divider, card border, chip border                                   |

#### Neutral Cheezon Ka Touch Behavior

| Cheez                                         | Normal                   | Press                             | Long press / hold      |
| --------------------------------------------- | ------------------------ | --------------------------------- | ---------------------- |
| **Text-only button** (neutral, e.g. "Cancel") | Ink `#0F2A1F`            | Muted ke jagah Green 700 text     | Green 800 text         |
| **Unselected bottom-nav item**                | transparent, icon Muted  | bg Green 50 `#EFF9F5`             | bg Green 100 `#D9F2E8` |
| **Input box (focus)**                         | border Outline `#DDE8E0` | border Green 500 `#129B73`, `2dp` | \-                     |
| **Divider / border**                          | koi touch nahi           | \-                                | \-                     |

### 7. Logo Only (3 Colors)

Ye sirf logo, splash screen aur 1-2 illustrations ke liye hain. **Buttons, cards ya text mein mat lagao**, warna UI mein do-teen greens mix ho jayenge.

| Naam           | Hex       | Kahan                         |
| -------------- | --------- | ----------------------------- |
| **Logo lime**  | `#77C148` | Logo ki patti ka highlight    |
| **Logo teal**  | `#088878` | Shield ka border              |
| **Logo cream** | `#F8F8E8` | Shield ke andar ka background |

### 8. Baaki States (Touch Ke Alawa)

| State                 | Kya dikhna chahiye                                                                                 |
| --------------------- | -------------------------------------------------------------------------------------------------- |
| **Selected**          | Green 100 (`#D9F2E8`) background + Green 600 (`#0B8461`) text/icon (ya status family ka 100 + 600) |
| **Disabled button**   | bg Green 300 (`#80D2B3`), text White (`#FFFFFF`), koi press effect nahi                            |
| **Disabled text**     | Muted (`#60776A`) ka halka ($50\%$ opacity)                                                        |
| **Focus (TextField)** | Border Green 500 (`#129B73`), `2dp`                                                                |
| **Error (TextField)** | Border Red 600 (`#D13440`), neeche Red 600 ka `supportingText` message                             |
| **Loading button**    | Normal shade rakho (`#0B8461`), andar white `CircularProgressIndicator` (`18dp`, `2dp` stroke)     |

### 9. Kahan Kaun Sa Rang: Ek Nazar Mein

| Screen ka hissa                   | Rang                                                                                          |
| --------------------------------- | --------------------------------------------------------------------------------------------- |
| **Screen background**             | Green 50 (`#EFF9F5`)                                                                          |
| **Top bar / bottom bar**          | White (`#FFFFFF`)                                                                             |
| **Page title**                    | Ink (`#0F2A1F`)                                                                               |
| **Subtitle**                      | Muted (`#60776A`)                                                                             |
| **Primary button**                | Green 600 (`#0B8461`, text White)                                                             |
| **Secondary button**              | White (`#FFFFFF`) + Outline border (`#DDE8E0`), text Ink (`#0F2A1F`)                          |
| **Search box**                    | White (`#FFFFFF`) + Outline border (`#DDE8E0`), placeholder Muted (`#60776A`)                 |
| **Selected chip / nav**           | Green 100 (`#D9F2E8`) + Green 600 (`#0B8461`)                                                 |
| **Product row**                   | White (`#FFFFFF`), divider Outline (`#DDE8E0`)                                                |
| **In stock pill**                 | Green 100 (`#D9F2E8`) + Green 600 (`#0B8461`)                                                 |
| **Low stock pill**                | Amber 100 (`#FFEBC8`) + Bold Amber 600 (`#B8730A`)                                            |
| **Out of stock / Expired pill**   | Red 100 (`#FFDDE0`) + Bold Red 600 (`#D13440`)                                                |
| **Batches / Movements icon tile** | Teal 100 (`#D0F1F3`) + Teal 600 (`#0F7C86`)                                                   |
| **Detail info tile**              | Field fill (`#E8F0EA`)                                                                        |
| **Hero / header gradient**        | Green 800 (`#075340`) $\rightarrow$ Green 600 (`#0B8461`) $\rightarrow$ Green 400 (`#3FB88F`) |

### 10. Contrast (Padhne Layak Hone Ki Jaanch)

| Jodi                        | Contrast  | Natija                              |
| --------------------------- | --------- | ----------------------------------- |
| White text on Green 600     | $4.7 : 1$ | Theek                               |
| White text on Green 700     | $6.8 : 1$ | Achha                               |
| Green 600 text on White     | $4.7 : 1$ | Theek                               |
| Ink text on White           | $14+ : 1$ | Bahut achha                         |
| Muted text on White         | $4.6 : 1$ | Theek                               |
| Amber 600 text on Amber 100 | $3.6 : 1$ | **Sirf bold/chhote labels ke liye** |
| Red 600 text on Red 100     | $4.0 : 1$ | **Sirf bold/chhote labels ke liye** |

## 👆 PART 3: TOUCH, SCREEN MOVEMENT TRANSITIONS, KEYBOARD & SCROLL RULES

### 1. Press Animation Ke 6 Niyam

1. **Ripple (Water Wave) MAT Lagao:** Default Material ripple (`indication = null`) band rakho. Hamara apna family shade badalna kaafi hai, dono ek saath dikhne se UI ganda lagta hai.

2. **Rang Badalne Ka Time:** `100–120 ms` (`tween(110)` — turant, par jhatke se nahi).

3. **Scale Animation:** Press par `0.97f` tak chhota ho (**sirf standalone buttons aur standalone cards par**).

4. **Row Aur List Card Rule:** List ke andar wale rows aur cards par scale mat lagao (sirf background rang badle: White $\rightarrow$ Green 50 `#EFF9F5`), kyunki list scroll karte waqt chhote jhatke ajeeb lagte hain.

5. **Long Press:** Hold shade (`2 steps darker`) + halka vibration (`HapticFeedbackType.LongPress`). Phir koi action: menu, select mode, ya tooltip.

6. **Press-and-Hold Release:** Jab tak ungli rakhi hai hold shade dikhe. Ungli hatate hi ya drag cancel hote hi turant normal shade par wapas aa jaye.

### 2. Screen Movement & Directional Transitions (Enter / Exit Behaviour)

Screen kis disha se aayegi aur kidhar jayegi—ye user ko app mein apni jagah (spatial orientation) samajhne mein madad karta hai:

| Navigation Action                                                                                                      | Enter Animation (Nayi Screen)                                                         | Exit Animation (Purani Screen)                                                 | Duration & Easing               |
| ---------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------ | ------------------------------- |
| **Forward Navigation** (List $\rightarrow$ Medicine Detail, Dashboard $\rightarrow$ Orders)                            | **Right $\rightarrow$ Left** slide (`slideInHorizontally { width }`) + subtle fade in | **Left** ki taraf thoda slide (`-width / 4`) + slight dim/fade (`0.85f`)       | `300ms` (`FastOutSlowInEasing`) |
| **Back / Pop Navigation** (Back button ya Predictive Back gesture)                                                     | **Left $\rightarrow$ Right** wapas aaye (`-width / 4` se `0`) + fade in               | **Left $\rightarrow$ Right** bahar slide ho (`slideOutHorizontally { width }`) | `280ms` (`FastOutSlowInEasing`) |
| **Bottom Navigation Tab Switch** (Home $\leftrightarrow$ Inventory $\leftrightarrow$ Orders $\leftrightarrow$ Profile) | **Pure Crossfade** (`fadeIn(tween(200))`) — **No horizontal slide**                   | **Pure Crossfade** (`fadeOut(tween(200))`)                                     | `200ms` (Linear/Ease)           |
| **Bottom Sheet / Filter / Stock Adjust Drawer**                                                                        | **Bottom $\rightarrow$ Top** slide up (`slideInVertically { height }`) + scrim dim    | **Top $\rightarrow$ Bottom** slide down (`slideOutVertically { height }`)      | `280ms` (Decelerate)            |
| **AlertDialog / Confirm Delete / Write-Off**                                                                           | **Center Scale** (`0.92f` $\rightarrow$ `1.0f`) + fade in                             | **Center Scale** (`1.0f` $\rightarrow$ `0.95f`) + fade out                     | `180ms`                         |
| **Filter / Search Result List Update**                                                                                 | `Modifier.animateItem()` (Compose lazy list smooth reorder/fade)                      | Smooth collapse/fade out                                                       | `200ms`                         |

> **Predictive Back Gesture (Android 14+):** Detail screens par jab user edge se swipe kare, toh screen ungli ke saath right ki taraf drag hoti hui aur thodi shrink (`0.95f`) hoti hui dikhni chahiye, aur peeche wali screen reveal honi chahiye.

### 3. TextField Touch, Focus & Keyboard Behavior

- **Touch on TextField:**
  - Border `1dp Outline (#DDE8E0)` se `2dp Green 500 (#129B73)` mein `120ms` mein animate ho.

  - Label Muted (`#60776A`) se Green 600 (`#0B8461`) mein shift ho.

- **Outside Touch Dismiss:** Screen ke kisi bhi khali hisse (`Green 50 #EFF9F5` area) par touch karte hi `focusManager.clearFocus()` aur `softwareKeyboardController?.hide()` call ho taaki keyboard turant niche chala jaye.

- **Correct Keyboard Types (`KeyboardOptions`):**
  - **Medicine / Salt Search:** `KeyboardType.Text`, `autoCorrect = false`, `ImeAction.Search`

  - **Batch Number / SKU / GST:** `KeyboardType.Ascii`, `KeyboardCapitalization.Characters`, `ImeAction.Next`

  - **Stock Quantity / Strip Count:** `KeyboardType.Number`, `ImeAction.Next`

  - **MRP / Purchase Price / Discount:** `KeyboardType.Decimal`, `ImeAction.Done`

  - **Phone / OTP / Pincode:** `KeyboardType.NumberPassword` / `Phone`

- **Scroll & Bottom Bar Inset Fix:**
  - Jab keyboard open ho, toh **Bottom Navigation Bar keyboard ke upar jump nahi karna chahiye** (wo keyboard ke peeche hide rahe ya fixed rahe).

  - Form content aur Bottom CTA Button (`Save` / `Add Stock`) `Modifier.imePadding()` aur `verticalScroll()` ke zariye keyboard ke upar smoothly scroll ho jaye, aur focused `OutlinedTextField` automatically view mein aa jaye (`bringIntoView`).

  - **Recomposition Safety:** Scroll offsets, focus animations, aur press states ko `InteractionSource` aur `graphicsLayer` ke andar read karein taaki typing ya scrolling ke waqt poori screen dobara recompose na ho.

## 💊 PART 4: PHARMA PRODUCT CARDS, IMAGE HANDLING & RESPONSIVE SIZING

### 1. Medicine Image Handling (Jab Image Ho vs Jab Na Ho)

- **Jab Image Available Ho:**
  - **Container:** `1:1` Square aspect ratio (`64dp` list row mein, `80dp–100dp` grid card mein, `220dp` detail header mein), background pure White (`#FFFFFF`) ya Field Fill (`#E8F0EA`), border `1dp Outline (#DDE8E0)`, shape `MaterialTheme.shapes.small` (`8dp`).

  - **Scaling (`ContentScale.Fit`):** Hamesha `ContentScale.Fit` + `4dp` inner padding use karein. Medicine strip ya bottle ko kabhi `Crop` mat karein, warna tablet ki mg/strength kat jayegi.

  - **Loading State:** Shimmer ya subtle pulse placeholder (`Field Fill #E8F0EA` $\leftrightarrow$ `Icon Fill #F4F9F5`).

- **Jab Image Na Ho (Smart Category Vector Fallback):**
  - Broken link icon kabhi mat dikhao. Medicine ke `form` / `category` ke hisab se clean Material Outlined icon dikhao:
    - **Tablet / Capsule:** `Medication` icon inside `Teal 100 (#D0F1F3)` tile with `Teal 600 (#0F7C86)` tint.

    - **Syrup / Liquid / Drops:** `LocalDrink` / `WaterDrop` icon inside `Green 100 (#D9F2E8)` tile with `Green 600 (#0B8461)` tint.

    - **Injection / Vial / Insulin:** `Vaccines` icon inside `Amber 100 (#FFEBC8)` tile with `Amber 600 (#B8730A)` tint.

### 2. GreenShield Medicine Card / Inventory Row Template

```
+-----------------------------------------------------------------------+
| [In Stock: 142 strips (Green 100/600)]     [Batch: B402 (Teal 100/600)]|
|                                                                       |
| +------------+  Augmentin 625 Duo Tablet          (titleMedium, Ink)  |
| |   IMAGE    |  Amoxycillin (500mg) + Clavulanic  (bodyMedium, Muted) |
| |  1:1 Fit   |  GSK Pharmaceuticals • 1x10 Tabs   (labelMedium, Muted)|
| |  (64x64dp) |                                                        |
| +------------+  MRP ₹201.71 • Exp: 08/2027        (bodyMedium, Ink)   |
|                 [View Batches (Teal)]          [ + Adjust / Add (600)]|
+-----------------------------------------------------------------------+

```

- **Card Surface:** Pure White (`#FFFFFF`) with `1dp` Outline (`#DDE8E0`) on `Green 50 (#EFF9F5)` screen background.

- **Detail Info Tiles (Category, Pack Size, Rack/Shelf No, HSN, GST):** Put inside small rounded boxes with `Field Fill (#E8F0EA)` background, `Muted (#60776A)` label, and `Ink (#0F2A1F)` bold value.

### 3. Responsive Layout & Font Scaling (Phones, Foldables & Seniors)

- **Font Scale Survival (`1.3x` to `1.5x`):**
  - Kisi bhi Card ya Row ki **height hardcode mat karo** (e.g., `Modifier.height(80.dp)` is BANNED). `Modifier.heightIn(min = 72.dp)` use karo taaki jab font bada ho, toh card vertically expand ho sake.

  - Medicine Title: `maxLines = 2`, `overflow = TextOverflow.Ellipsis`.

  - Salt Composition / Subtitle: `maxLines = 1`, `overflow = TextOverflow.Ellipsis`.

  - Status Chips (`StatusChip`): `maxLines = 1`, `softWrap = false`.

- **Screen Size Adaptation (`WindowSizeClass` / `GridCells.Adaptive`):**
  - **Compact (`< 600dp` Phone Portrait):** `LazyColumn` single-column list (`16dp` outer margin, `8dp–12dp` item spacing).

  - **Medium/Expanded (`>= 600dp` Foldable / Landscape / Tablet):** `LazyVerticalGrid` with `GridCells.Adaptive(minSize = 280.dp)` taaki cards khud 2 ya 3 columns mein set ho jayein bina stretch huye.

## ✅ PART 5: YAAD RAKHNE KE 5 GOLDEN NIYAM

1. **Ek Brand Green:** `#0B8461` (`Green 600`). Uske alawa koi doosra green UI mein nahi.

2. **Rang Ka Matlab Fix Rakho:**
   - **Green** = Theek hai / Brand / In Stock (`#0B8461`)

   - **Amber** = Dhyan do / Low Stock / Near Expiry (`#B8730A`)

   - **Red** = Khatra / Out of Stock / Expired / Delete (`#D13440`)

   - **Teal** = Jaankari / Batches / Stock Movements (`#0F7C86`)

3. **Touch Par Family Wahi:** Green ka press green hi rahe, warm (Amber/Red) ka press warm hi rahe. Ripple effect (`water wave`) hamesha band rakho.

4. **Shade Shift Rule:**
   - **Halke background** $\rightarrow$ gehra karo ($50 \rightarrow 100 \rightarrow 200$).

   - **Gehre background** $\rightarrow$ aur gehra karo ($600 \rightarrow 700 \rightarrow 800$).

5. **UI-Only Discipline:** Sirf Compose UI, Theme tokens, aur Modifier interactions badlo; ViewModels, Repositories, aur Navigation logic ko kabhi haath mat lagao.
