# 🎨 Claymorphism UI Redesign — Master Prompt
 
Use this prompt to have an AI coding assistant **rebuild the UI layer of an existing app
from scratch** using a fixed "Claymorphism" design system — without touching any
data/business logic. Fill in `{{ }}` placeholders before sending.
 
---
 
## PROMPT START
 
You are a **Senior Product Designer + Senior Android/Compose Engineer** who specializes in
building cohesive design systems and translating them into pixel-accurate UI code.
 
### 🔒 HARD CONSTRAINTS (do not violate these)
 
1. **UI-only change.** You may only touch Composables/Views, styling, theming, layout,
   animation, and navigation-presentation code. You must **NOT** modify:
   - ViewModels, UseCases, Repositories, data classes/entities, API/network layer, database
     layer, business logic, state-holding logic (the *shape* of state), or navigation *logic*
     (which screen goes where).
   - If a UI change appears to require a data/logic change, **stop and flag it** instead of
     making the change yourself.
2. **Design from scratch, not from the old screens.** Do not inherit or mimic the layout,
   spacing, or component choices of the current UI. Look only at *what data/fields each
   screen needs to display and what actions it needs to expose* — then design the screen
   fresh using nothing but the design system below. Treat every screen as a blank canvas
   constrained only by: (a) the data it must show, (b) the actions it must trigger, (c) the
   tokens below.
3. **Zero deviation from the token system below.** Every color, radius, elevation, spacing,
   and font value used anywhere in the UI must trace back to a named token in this doc. No
   ad-hoc hex codes, no "close enough" spacing, no unlisted corner radii. If a situation isn't
   covered by an existing token, propose a new token that fits the system's logic (don't
   invent something arbitrary) and flag it explicitly rather than silently adding it.
4. **Apply consistently across every screen**, not just the ones explicitly named — the same
   button, card, input, and badge components must look and behave identically everywhere
   they appear.
---
 
### 🎨 DESIGN SYSTEM — CLAYMORPHISM TOKEN SPEC
 
**Background Gradients**
- `ClayGradient` (global canvas, auth/home screens): vertical linear gradient, `#6C63FF` top → `#48CAE4` bottom
- `FilterPillGradient` — Active: horizontal `#6C63FF` → `#48CAE4`. Inactive: `White` → `#FAF9FF`
**Core Palette**
| Token | Hex | Usage |
|---|---|---|
| ClayPrimary | #6C63FF | icons, primary text, buttons, focused borders |
| ClaySecondary | #48CAE4 | paired with primary in gradients |
| ClayAccent | #FF6584 | decorative blobs, destructive/blocked states |
| ClayCardBg | #FAF9FF | solid background for floating cards |
| ClayFieldBg | #F0EEFF | text fields, recessed/inset sections |
| ClayBorder | #D0C8FF | unfocused input borders, subtle bounding boxes |
| Card Outer Border | White, 1.5dp | frosted-glass/clay edge on all cards |
 
**Text Colors**
| Token | Hex | Usage |
|---|---|---|
| Header/Splash | #FFFFFF | welcome text, overlay titles on gradient |
| ClayTextPrimary | #1E1B4B | card titles, prominent numbers |
| ClayTextSecondary | #6B6893 / #888AAA | placeholders, captions, body |
| Muted Text | #555577 / #666688 | minor labels, secondary hints |
 
**Semantic States**
| State | Hex |
|---|---|
| Approved/Success | #10B981 |
| Pending/Warning | #F59E0B |
| Blocked/Error | #FF6584 or #EF4444 |
 
**Elevation & Shadow**
- Shadow tint: `ClayCardShadow` = `#6C63FF` @ 25–30% opacity (colored ambient + spot shadow — this is what makes it "clay," not a plain grey shadow)
- Heavy elevation (SignIn card, Hero card): 24dp
- Standard card elevation: 20dp
- Input/button elevation: 12–16dp
- Avatar/badge elevation: 8–10dp
**Radii**
- Large cards: 28dp + 1.5dp white border
- Stat tiles: 22dp + 1.5dp white border
- Text fields/buttons: 16–20dp
- Recessed fields/inner chips: 12–18dp
- Avatars/blobs: full circle (50dp / 100%)
**Backdrop Blobs**
- Top blob: White @ 12% opacity
- Bottom blob: `#FF6584` @ 20–22% opacity
- Size range: 120–190dp
- Status badges: tinted background at 12% alpha of the state color, hairline border at 25% alpha of the same color
**Typography** (system sans — Roboto/Inter)
| Role | Size | Weight | Color |
|---|---|---|---|
| Screen Title / H1 | 30sp | ExtraBold | #FFFFFF |
| Hero Metric / H2 | 22sp | ExtraBold | #1E1B4B |
| Tile Metric / H3 | 20sp | ExtraBold | #1E1B4B |
| Card Title/Name | 16sp | Bold | #1E1B4B |
| Button Label | 16sp (or 14sp) | Bold | #FFFFFF |
| Body/Subtitle | 14sp | Normal/Medium | #FFFFFF @ 82% |
| Input & Filter | 13sp | SemiBold | #888AAA / #1E1B4B |
| Metadata/ID | 12sp | Normal/Medium | #888AAA / #666688 |
| Micro Label/Badge | 11sp | Bold | varies, 1sp letter-spacing, UPPERCASE |
 
**Spacing & Sizing**
- SignIn container padding: 24dp horizontal, 40dp vertical
- List screen padding: 20dp start/end/top, 90dp bottom (clears nav/FAB)
- Card inner padding: 20–24dp
- Vertical list item gap: 18dp
- Horizontal row gap (tiles/pills): 10–12dp
- Primary button height: 54dp (auth) / 44dp (secondary actions)
- Avatar size: 48dp
**Motion**
- Button press: scale to 0.97f on click/loading (`animateFloatAsState`)
- Filter pill select: background → active gradient, border → transparent, elevation 4dp → 10dp
- Circular gauge/chart fill: `tween(900)` stroke sweep animation
- Loader/button label swap: `scaleIn()+fadeIn()` vs `fadeOut()`, `tween(150)`
---
 
### 🛠️ IMPLEMENTATION CRAFT BAR — non-negotiable quality floor
 
Every component you build must match the **implementation depth of the reference component
below**, not a simplified approximation of it. This is the actual bar, not an inspiration:
 
```kotlin
@Composable
private fun AppBottomBar(
    items: List<BottomNavigationItem>,
    selected: Int,
    onItemSelected: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        ClayPrimary.copy(alpha = 0.08f),
                        ClaySecondary.copy(alpha = 0.08f)
                    )
                )
            )
            .background(
                brush = Brush.verticalGradient(
                    0.0f to Color.White.copy(alpha = 0.16f),
                    0.35f to Color.Transparent
                )
            )
            .drawBehind {
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.04f),
                            Color.White.copy(alpha = 0.45f),
                            Color.White.copy(alpha = 0.04f)
                        )
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selected == index
 
                // FIX A ─ Uniform corners, 22dp = height÷2 = true capsule/oval
                // Before: asymmetric 18dp top / 12dp bottom → flat bottom edges leaked white
                // After:  uniform 22dp all sides → perfect stadium pill, no corner gaps
                val animatedCorner by animateDpAsState(
                    targetValue = if (isSelected) 22.dp else 18.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "cornerAnim"
                )
 
                // Drives shadow fade-in/out without needing elevation change
                val animatedShadowAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "shadowAlpha"
                )
 
                val animatedIconTint by animateColorAsState(
                    targetValue = if (isSelected) ClayPrimary else Color.White,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "iconTintAnim"
                )
 
                val currentShape = RoundedCornerShape(animatedCorner) // uniform all sides
 
                Box(
                    modifier = Modifier
                        .height(44.dp)
 
                        // FIX B ─ Replace graphicsLayer { shadowElevation } with
                        //         drawBehind + BlurMaskFilter via nativeCanvas
                        //
                        // Why graphicsLayer caused white corners:
                        //   When shadowElevation > 0, Android allocates a RenderNode for the
                        //   compositing layer. That node's RECTANGULAR bounds get a white
                        //   default background during shadow pass. Your rounded content only
                        //   fills the pill area, so the four rectangular corners stay white.
                        //
                        // Why drawBehind + BlurMaskFilter doesn't:
                        //   It draws directly on the same hardware canvas as everything else.
                        //   No separate compositing layer → no white rectangle → no artifacts.
                        .drawBehind {
                            if (animatedShadowAlpha > 0f) {
                                val cr = animatedCorner.toPx()
                                drawIntoCanvas { canvas ->
                                    canvas.nativeCanvas.drawRoundRect(
                                        /* left   */ 3.dp.toPx(),
                                        /* top    */ 4.dp.toPx(),
                                        /* right  */ size.width - 3.dp.toPx(),
                                        /* bottom */ size.height + 3.dp.toPx(),
                                        /* rx     */ cr,
                                        /* ry     */ cr,
                                        android.graphics.Paint().apply {
                                            isAntiAlias = true
                                            color = android.graphics.Color.argb(
                                                (55 * animatedShadowAlpha).toInt(),
                                                0, 0, 0
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
 
                        // Pill base color
                        .then(
                            if (isSelected) {
                                Modifier.background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFFFFFFFF), // Top — direct light
                                            Color(0xFFF2F6FC), // Upper body
                                            Color(0xFFE5EBF7), // Lower body
                                            Color(0xFFD5DFEE)  // Bottom — contact zone
                                        )
                                    ),
                                    shape = currentShape
                                )
                            } else {
                                Modifier.background(
                                    color = Color(0xFF1E293B).copy(alpha = 0.70f),
                                    shape = currentShape
                                )
                            }
                        )
 
                        // 5-layer pillow lighting model (unchanged, still fully valid)
                        .drawWithContent {
                            drawContent()
                            if (isSelected) {
                                val cr = CornerRadius(animatedCorner.toPx())
                                val rimWidth = 1.8.dp.toPx()
 
                                // Layer 1 — Top Specular Highlight
                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        0.00f to Color.White.copy(alpha = 0.80f),
                                        0.30f to Color.White.copy(alpha = 0.18f),
                                        0.52f to Color.White.copy(alpha = 0.00f)
                                    ),
                                    size = size,
                                    cornerRadius = cr
                                )
 
                                // Layer 2 — Bottom Contact Shadow
                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        0.48f to Color.Black.copy(alpha = 0.00f),
                                        0.78f to Color.Black.copy(alpha = 0.06f),
                                        1.00f to Color.Black.copy(alpha = 0.22f)
                                    ),
                                    size = size,
                                    cornerRadius = cr
                                )
 
                                // Layer 3 — Side Curvature Shading
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
 
                                // Layer 4 — Top Rim Highlight
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
 
                                // Layer 5 — Bottom Rim Shadow
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
                        }
                        .clip(currentShape)
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onItemSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = if (isSelected) 16.dp else 12.dp,
                            vertical = 8.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.name,
                            tint = animatedIconTint,
                            modifier = Modifier.size(20.dp)
                        )
 
                        AnimatedVisibility(
                            visible = isSelected,
                            enter = fadeIn(animationSpec = tween(180, delayMillis = 40)) +
                                    expandHorizontally(
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    ),
                            exit = fadeOut(animationSpec = tween(100)) +
                                    shrinkHorizontally(
                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                    )
                        ) {
                            Row {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.name,
                                    color = ClayPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
```
 
What "matching this level" concretely means for every component you build:
 
1. **Physically-motivated shadows, not flat elevation.** Don't reach for a default
   `shadowElevation`/`elevation` modifier when it produces artifacts (e.g. rectangular
   RenderNode compositing layers clipping a rounded shape and leaving background-color
   corners showing through). Diagnose *why* the naive approach breaks, then draw the shadow
   manually (`drawBehind` + `BlurMaskFilter`/equivalent) so it composites directly on the same
   canvas as the content.
2. **Multi-layer "pillow" lighting on raised/selected surfaces**, not a single flat shadow:
   - top specular highlight (bright, fading down)
   - bottom contact shadow (dark, concentrated at the base)
   - side curvature shading (subtle darkening at the left/right edges)
   - a thin top rim highlight and bottom rim shadow stroke around the shape's edge
   Each layer is its own gradient draw call — don't collapse them into one shadow.
3. **Spring physics for interactive state changes**, not linear tweens, wherever the user is
   directly interacting (selection, press, toggle): `spring(dampingRatio=DampingRatioLowBouncy,
   stiffness=StiffnessMediumLow)` or equivalent — gives the "clay" a sense of squish/weight.
   Reserve simple `tween()` easing for non-interactive/ambient animation (chart fills, fades).
4. **Every animatable visual property gets its own animated state** (corner radius, shadow
   alpha, tint color, size) rather than baking transitions into a single opaque transition
   block — this is what lets each property move at its own natural rate.
5. **Comment the *why*, not just the *what*.** Where you deviate from the "obvious" API (e.g.
   custom shadow drawing instead of built-in elevation), leave a short comment explaining the
   failure mode being avoided, mirroring the reasoning style in the reference.
6. **No shortcuts on secondary states.** The unselected/inactive/idle version of a component
   deserves the same deliberate treatment as the selected/active one (see how the reference
   still defines a distinct dark idle pill, not just "no styling").
If a component is simple enough that this level of treatment would be genuinely excessive
(e.g. a plain divider), say so explicitly rather than silently under-building it — don't
apply this bar mechanically where it doesn't serve the design.
 
---
 
### 📋 YOUR TASK
 
Project: `{{MediAdminApp}}`
Screens to redesign: `{{AboutScreen, AddProductScreen, HistoryScreen, NewOrder, OrderDetailsScreen, ProductScreen, ProfileScreen, SettingsScreen,SpecificOrderScreen, SpecificProductScreen, UpdateProductScreen,UpdateUserDetailsScreeen, UsersDetailScreen}}`
Framework: `{{Jetpack Compose}}`
Current UI code location: `{{app\src\main\java\com\saurabh\mediadminapp}}`
 
For each screen, do the following **in order**:
 
1. **Inventory** — List the data fields this screen displays and the actions/events it
   exposes (from the existing ViewModel/state — read-only, do not touch). This is your only
   input from the old code.
2. **Fresh layout design** — Design the screen's visual composition from first principles:
   what's the hierarchy, what's the focal point, how do the Claymorphism tokens above express
   it. Explicitly do not reference the old screen's arrangement.
3. **Component build-out** — Implement using only design-system tokens, at the implementation
   depth defined in the **Implementation Craft Bar** above. Reuse a shared component
   (`ClayCard`, `ClayButton`, `ClayTextField`, `ClayBadge`, `ClayFilterPill`, etc.) rather than
   one-off styling per screen — build these as a shared UI kit first if it doesn't exist yet.
4. **Wire to existing state** — Bind the new UI to the existing ViewModel/state/callbacks
   exactly as they are now. No renaming, no restructuring, no new fields.
5. **Flag anything blocked** — If a design decision would require a data/state change (e.g.
   "the design needs a `lastActiveAt` field that doesn't exist"), list it separately at the
   end instead of adding it yourself.
### OUTPUT FORMAT
1. Shared UI kit / component library (tokens as constants + reusable components)
2. Per-screen redesign, one at a time, each as complete UI code
3. A short "data/logic gaps found" list (if any) — things the UI wants but current state
   doesn't provide, left for you to decide on separately
