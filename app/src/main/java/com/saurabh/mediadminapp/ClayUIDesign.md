# 🎨 Claymorphism Full Structural & Visual UI Overhaul — Master Prompt

Use this document to instruct an AI coding assistant to **completely re-architect both the visual styling and the structural anatomy of an existing Jetpack Compose UI layer from scratch**. This mandates a total redesign of layouts, alignments, spatial hierarchy, component sizing, and element placements—paired with an uncompromising Claymorphic design system and unique architectural layouts per screen—while leaving all underlying data/business logic completely untouched.



### TOKEN CONSERVATION & ZERO-WASTE ENFORCEMENT (CRITICAL)
- STRICT PROHIBITION: Do NOT write implementation plans, summaries, checklists, setup guides, or architectural commentary.
- Do NOT generate `.md` files, READMEs, scratchpads, or documentation artifacts.
- Do NOT output boilerplate explanations before or after code blocks.
- Generate ONLY the exact production Kotlin source files required by the project. Every token must belong strictly to compilable code.
- Write full, complete, production-ready code with NO placeholders, NO `// TODO: implement later`, and NO pseudo-code.

---

## PROMPT START

You are a **Principal Product Designer & Principal Android/Compose UI Architect**. Your objective is to perform a **ground-up structural layout and visual redesign** of the screens listed below into a production-grade Claymorphism interface.

---

### 🔒 HARD ARCHITECTURAL CONSTRAINTS (Zero Tolerance)

1. **Unique Architectural Layout for Every Screen (Anti-Cookie-Cutter):**
   * **Every screen must have its own distinct architectural blueprint.** Do not repeat the same structural template (e.g., standard header + lazy column) across different domains.
   * **Tailor the anatomy to the task:**
     * **Catalog/Products:** Asymmetric product showcase, visual hero tiles, floating stock chips, and split pricing decks.
     * **Orders/Transactions:** Timeline-anchored structures, visual receipts, milestone progress tracks, and floating dispatch action bars.
     * **User Management:** Grid-based status cards, role clearance matrices, avatar-centric badges, and quick-filter docks.
     * **Admin/Settings:** Clustered configuration bento grids, tactile clay switch decks, and diagnostic identity hubs.
   * **No Repetitive Layout Clones:** Each screen must look and feel intentionally architected for its specific operational purpose, while maintaining the unified Claymorphic design language.

2. **Total Structural & Anatomical Re-Architecture (No Mere Reskinning):**
   
   * **Re-engineer the physical layout:** Completely redesign element positioning, visual hierarchy, content grouping, spacing distribution, alignment axes, and component sizing from scratch.
   * **Blank Canvas Principle:** Look at the screen file **only** as a raw data contract (what state flows exist and what user events are triggered). Discard all existing layouts, column/row arrangements, box stacks, and button placements entirely.

3. **Strict UI-Only Scope:**
   * Modify **only** Composables, custom modifiers, canvas shaders, graphics drawing operations, theme tokens, and visual presentation state.
   * **Do not touch:** ViewModels, UseCases, Repositories, Database entities, DataStore/SharedPreferences, Network DTOs, domain models, or navigation graph logic.
   * If a redesigned layout would benefit from an unexposed field or state, **do not invent or alter models**. Note it strictly under a **"Data/Logic Gaps"** section at the end.

4. **Two-Tier Scannable Hierarchy (Overview Card ➔ Dedicated Detail Screen):**
   * **List/Overview Cards (`ProductScreen`, `UsersDetailScreen`, `NewOrder`, `HistoryScreen`, etc.):**
     * Structurally rebuild each card to show **only** primary, scannable information (e.g., identity, status badge, hero metric/price).
     * Eliminate clutter: strip secondary fields from the overview card layout.
     * Every overview card **must** feature a prominent, spring-reactive action button labeled `"Details"`.
   * **Target Detail Screens (`SpecificProductScreen`, `SpecificOrderScreen`, `UpdateUserDetailsScreeen`, etc.):**
     * Must also be built from a blank canvas with a unique architectural layout—restructuring full-width specifications, expanded timelines, metadata clusters, and primary admin controls into an immersive clay canvas layout matching the home screen's design language.
     * Clicking `"Details"` on an overview card triggers navigation to the newly structured detail screen.

5. **Zero Tolerance for Fake/Naive Elevation:**
   * Standard Android `shadowElevation` or `Modifier.shadow()` is strictly forbidden on clay surfaces. It creates rectangular RenderNode compositing passes that cause white corner artifacts on rounded geometry.
   * Every shadow must be drawn manually on the hardware canvas using `drawBehind` + `drawIntoCanvas` with an anti-aliased `android.graphics.BlurMaskFilter` colored with `ClayCardShadow` (`#6C63FF` at 25%–30% opacity).

---

### 📐 STRUCTURAL LAYOUT GUIDELINES & ANATOMY

* **Hero Focal Areas:** Each screen must establish a clear, domain-specific focal point (e.g., an asymmetric hero metric block, floating status summary, or interactive canvas header) before presenting list/form content.
* **Modern Form Anatomy:** Input fields must move away from generic single-column form lists. Group related inputs into recessed clay clusters (`ClayFieldBg`), with floating labels and dedicated trailing icon anchors.
* **Component Sizing Standard:**
  * **Primary CTA/Hero Actions:** `54dp` height, full width or anchored floating pill.
  * **Card Detail Triggers (`"Details"`):** Compact `38dp`–`42dp` height, high-contrast clay pill.
  * **Status & Meta Chips:** `26dp`–`30dp` height with centered typography and strict uppercase tracking.
  * **Visual Padding Rhythm:** Uniform base grid: `4dp`, `8dp`, `12dp`, `16dp`, `20dp`, `28dp`.

---

### 🎨 DESIGN SYSTEM & TOKEN SPECIFICATIONS

### SCREENS TO MODIFY
 * `AboutScreen`
 * `AddProductScreen`
 * `HistoryScreen`
 * `NewOrder`
 * `OrderDetailsScreen`
 * `ProductScreen`
 * `ProfileScreen`
 * `SettingsScreen`
 * `SpecificOrderScreen`
 * `UpdateProductScreen`
 * `UpdateUserDetailsScreen`
 * `UsersDetailsScreen`

#### Canvas & Backgrounds
* `ClayCanvasGradient`: Vertical linear gradient from `#6C63FF` (top) to `#48CAE4` (bottom).
* `ClayCardBg`: Solid `#FAF9FF` (raised clay surface base).
* `ClayFieldBg`: Solid `#F0EEFF` (recessed/inset container base).
* `FilterPillGradientActive`: Horizontal linear gradient `#6C63FF` → `#48CAE4`.
* `FilterPillInactive`: Solid `#FAF9FF` with `1.5.dp` border `#D0C8FF`.

#### Palette Tokens
| Token | Value | Target Elements |
|---|---|---|
| `ClayPrimary` | `#6C63FF` | Primary button surfaces, active indicators, focused strokes, key icons |
| `ClaySecondary` | `#48CAE4` | Gradient accents, complementary highlights |
| `ClayAccent` | `#FF6584` | Ambient backdrop blobs, error states, destructive tags |
| `ClayBorder` | `#D0C8FF` | Inset field outlines, unselected card boundaries |
| `ClayCardBorder` | `#FFFFFF` (`1.5.dp`) | Outer perimeter rim on raised clay surfaces |
| `StateApproved` | `#10B981` | Success badges, verified/approved states |
| `StatePending` | `#F59E0B` | In-progress indicators, pending approvals |
| `StateBlocked` | `#FF6584` | Blocked users, destructive alerts, error states |

#### Typography Tokens
| Token | Size | Weight | Color / Opacity |
|---|---|---|---|
| `ClayH1` | `30.sp` | ExtraBold | `#FFFFFF` (100%) |
| `ClayH2` | `22.sp` | ExtraBold | `#1E1B4B` |
| `ClayH3` | `20.sp` | Bold | `#1E1B4B` |
| `ClayCardTitle` | `16.sp` | Bold | `#1E1B4B` |
| `ClayButtonText` | `15.sp` | Bold | `#FFFFFF` (Letter-spacing: `0.5.sp`) |
| `ClayBody` | `14.sp` | Medium | `#FFFFFF` (82% opacity on gradients) / `#1E1B4B` (on cards) |
| `ClayInputText` | `14.sp` | SemiBold | `#1E1B4B` (Placeholder: `#888AAA`) |
| `ClayMetadata` | `12.sp` | Medium | `#6B6893` |
| `ClayMicroBadge` | `11.sp` | ExtraBold | Uppercase, `1.sp` letter-spacing |

#### Geometry & Dimensional Scales
* **Primary Containers & Structured Cards:** `28.dp` corner radius + `1.5.dp` solid `#FFFFFF` outer stroke.
* **Secondary Tiles & Inset Clusters:** `22.dp` corner radius + `1.5.dp` solid `#FFFFFF` outer border stroke.
* **Input Fields & Clay Buttons:** `18.dp` corner radius.
* **Badges & Action Pills:** Full Stadium / Capsule (`height / 2`).
* **Canvas Insets:** `20.dp` horizontal, `16.dp` top, `96.dp` bottom (nav bar & FAB clearance).
* **Inner Card Spacing:** `20.dp` to `24.dp` internal padding.

---

### 🛠️ IMPLEMENTATION CRAFT BAR (Mandatory Mechanics)

Every component (cards, buttons, input fields, badges) must match the craftsmanship of the reference implementation below:

```kotlin
// ARCHITECTURAL BENCHMARK: Multi-layer Pillow Lighting + BlurMaskFilter Hardware Shadow
@Composable
fun ClayPillSample(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedCorner by animateDpAsState(
        targetValue = if (isSelected) 22.dp else 18.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "cornerAnim"
    )
    val animatedShadowAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "shadowAlpha"
    )
    val currentShape = RoundedCornerShape(animatedCorner)

    Box(
        modifier = Modifier
            .height(44.dp)
            // 1. Hardware Canvas Blur Shadow (Eliminates RenderNode artifacts)
            .drawBehind {
                if (animatedShadowAlpha > 0f) {
                    val cr = animatedCorner.toPx()
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawRoundRect(
                            3.dp.toPx(),
                            4.dp.toPx(),
                            size.width - 3.dp.toPx(),
                            size.height + 3.dp.toPx(),
                            cr,
                            cr,
                            android.graphics.Paint().apply {
                                isAntiAlias = true
                                color = android.graphics.Color.argb(
                                    (65 * animatedShadowAlpha).toInt(),
                                    108, 99, 255 // ClayPrimary tinted shadow
                                )
                                maskFilter = android.graphics.BlurMaskFilter(
                                    10.dp.toPx(),
                                    android.graphics.BlurMaskFilter.Blur.NORMAL
                                )
                            }
                        )
                    }
                }
            }
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFFFFFFF), Color(0xFFF2F6FC), Color(0xFFE5EBF7), Color(0xFFD5DFEE))
                ),
                shape = currentShape
            )
            // 2. Multi-Pass Pillow Lighting
            .drawWithContent {
                drawContent()
                val cr = CornerRadius(animatedCorner.toPx())
                val rimWidth = 1.8.dp.toPx()

                // Layer 1: Top Specular Highlight
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        0.00f to Color.White.copy(alpha = 0.80f),
                        0.30f to Color.White.copy(alpha = 0.18f),
                        0.52f to Color.White.copy(alpha = 0.00f)
                    ),
                    size = size,
                    cornerRadius = cr
                )
                // Layer 2: Bottom Contact Shadow
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        0.48f to Color.Black.copy(alpha = 0.00f),
                        0.78f to Color.Black.copy(alpha = 0.06f),
                        1.00f to Color.Black.copy(alpha = 0.22f)
                    ),
                    size = size,
                    cornerRadius = cr
                )
                // Layer 3: Side Curvature Shading
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        0.00f to Color.Black.copy(alpha = 0.08f),
                        0.15f to Color.Black.copy(alpha = 0.00f),
                        0.85f to Color.Black.copy(alpha = 0.00f),
                        1.00f to Color.Black.copy(alpha = 0.08f)
                    ),
                    size = size,
                    cornerRadius = cr
                )
                // Layer 4: Top Rim Highlight
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        0.00f to Color.White.copy(alpha = 1.00f),
                        0.38f to Color.White.copy(alpha = 0.30f),
                        0.65f to Color.White.copy(alpha = 0.00f)
                    ),
                    size = size,
                    cornerRadius = cr,
                    style = Stroke(width = rimWidth)
                )
                // Layer 5: Bottom Rim Shadow
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        0.40f to Color.Black.copy(alpha = 0.00f),
                        0.78f to Color.Black.copy(alpha = 0.08f),
                        1.00f to Color.Black.copy(alpha = 0.22f)
                    ),
                    size = size,
                    cornerRadius = cr,
                    style = Stroke(width = rimWidth)
                )
            }
            .clip(currentShape)
    ) { /* Content */ }
}