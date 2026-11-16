package com.saurabh.mediadminapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalViewConfiguration
import com.saurabh.mediadminapp.ui.theme.Shade
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Touch level for the GreenShield press rules: normal, pressed (1 step darker), held (2 steps darker). */
enum class PressLevel { None, Pressed, Held }

/**
 * Press -> [PressLevel.Pressed]; still down after the long-press timeout -> [PressLevel.Held];
 * release / cancel / drag-away -> [PressLevel.None] immediately (Part 3 §1 rule 6).
 */
@Composable
fun InteractionSource.pressLevel(): State<PressLevel> {
    val level = remember { mutableStateOf(PressLevel.None) }
    val holdMs = LocalViewConfiguration.current.longPressTimeoutMillis
    LaunchedEffect(this) {
        var hold: Job? = null
        interactions.collect { i ->
            when (i) {
                is PressInteraction.Press -> {
                    level.value = PressLevel.Pressed
                    hold?.cancel()
                    hold = launch { delay(holdMs); level.value = PressLevel.Held }
                }
                is PressInteraction.Release, is PressInteraction.Cancel -> {
                    hold?.cancel(); level.value = PressLevel.None
                }
            }
        }
    }
    return level
}

fun Shade.at(level: PressLevel): Color = when (level) {
    PressLevel.None -> normal
    PressLevel.Pressed -> pressed
    PressLevel.Held -> held
}

/** Animated colour (110 ms) for [shade] at the current press level of this source. */
@Composable
fun InteractionSource.shadeColor(shade: Shade, label: String = "shade"): State<Color> {
    val level by pressLevel()
    return animateColorAsState(shade.at(level), tween(110), label = label)
}

/** 0.97 scale while pressed; standalone buttons and standalone cards only (never list rows). */
@Composable
fun InteractionSource.pressScale(enabled: Boolean = true): State<Float> {
    val level by pressLevel()
    return animateFloatAsState(if (enabled && level != PressLevel.None) 0.97f else 1f, tween(110), label = "pressScale")
}
