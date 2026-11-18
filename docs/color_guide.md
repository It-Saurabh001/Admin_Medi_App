# GreenShield colour rules (strict)

Source: `ui/theme/Color.kt` (`GS` = 38 raw colours, `Shades` = normal/pressed/held sets). Never write hex in screens.
Use `MaterialTheme.colorScheme.*`, `MaterialTheme.statusColors`, or `Shades.*` / `GS.*` for family colours.

## Meaning (fixed)
Green = ok/brand, Amber = attention (low stock, near expiry), Red = danger (out of stock, expired, delete, error), Teal = info (batches, movements, tags).
Pressed/held shade ALWAYS stays in the same family. Light bg steps 50>100>200 (100>200>300); solid 600>700>800.

## Scheme mapping (light)
| Role | GS colour |
|---|---|
| primary / onPrimary | Green600 / White |
| primaryContainer / on | Green100 / Green900 |
| secondary (info) / container / on | Teal600 / Teal100 / Teal800 |
| tertiary (warning) / container / on | Amber600 / Amber100 / Amber800 |
| error / container / on | Red600 / Red100 / Red800 |
| background | Green50 |
| surface (cards, top/bottom bar) | White |
| onBackground, onSurface | Ink |
| onSurfaceVariant (subtitle, hint) | Muted |
| surfaceVariant, surfaceContainerHigh (info tile, input fill) | FieldFill |
| surfaceContainer (icon button fill) | IconFill |
| outline, outlineVariant | Outline |
| pending status | Amber100 / Amber800 |

## Which colour where
| Part | Colour |
|---|---|
| Primary button | Shades.GreenSolid, white text |
| Secondary button | White + Outline border, Ink text (press Shades.GreenSecondaryButton) |
| Selected chip / nav | Green100 + Green600 (Shades.GreenChipSelected); unselected chip White |
| Product / list row, tappable card | Shades.GreenRow |
| Icon button (bell, scan, back) | Shades.IconButton |
| In stock pill | Shades.SuccessPill, text Green600/700/800 |
| Low stock / near expiry pill, card, row, link, button | Shades.AmberPill / AmberCard / AmberRow / AmberText / AmberSolid |
| Out of stock / expired pill, card, delete button, link | Shades.RedPill / RedCard / RedSolid / RedText |
| Batches / movements icon tile, info card, link | Shades.TealTile / TealCard / TealText |
| Disabled button | Green300 bg, white text, no press effect |
| Disabled text | Muted at 50% |
| TextField focus / error border | Green500 2dp / Red600 + Red600 message |
| Hero gradient | Green800 > Green600 > Green400 |
| Logo lime/teal/cream | splash and logo only, never UI |

## Touch rules
- Colour change 100-120 ms; no ripple (use own shade swap).
- Scale 0.97 on buttons only; NOT on rows or cards.
- Long press: held shade + haptic; back to normal on release.
- Amber/Red pill text bold (contrast 3.6-4.0:1).
- Row containing a teal tile keeps Green50/100 row press.

## Five rules
1. One brand green `#0B8461`. 2. Colour meaning is fixed. 3. Press stays in family. 4. Light bg deeper (50>100>200), solid deeper (600>700>800). 5. Press and hold must look different.

Dark scheme is unchanged (not in the guide); revisit if dark mode is required.
