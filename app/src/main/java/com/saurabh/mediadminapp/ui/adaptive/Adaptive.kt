package com.saurabh.mediadminapp.ui.adaptive

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** M3 window width classes: COMPACT <600dp, MEDIUM 600-839dp, EXPANDED >=840dp. */
enum class WindowSize { COMPACT, MEDIUM, EXPANDED }

@Immutable
data class WindowInfo(val size: WindowSize, val isLandscape: Boolean, val widthDp: Int) {
    val isCompact get() = size == WindowSize.COMPACT
    val isExpanded get() = size == WindowSize.EXPANDED
}

@Composable
fun rememberWindowSize(): WindowInfo {
    val c = LocalConfiguration.current
    val w = c.screenWidthDp
    val size = when {
        w < 600 -> WindowSize.COMPACT
        w < 840 -> WindowSize.MEDIUM
        else -> WindowSize.EXPANDED
    }
    return WindowInfo(size, c.orientation == Configuration.ORIENTATION_LANDSCAPE, w)
}

object AdaptiveWidths {
    val Form: Dp = 560.dp
    val List: Dp = 840.dp
}

/** Centers [content] and caps its width (forms 560dp, lists 840dp). Full width on COMPACT. */
@Composable
fun AdaptiveContent(
    modifier: Modifier = Modifier,
    maxWidth: Dp = AdaptiveWidths.List,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Box(Modifier.widthIn(max = maxWidth).fillMaxWidth().fillMaxHeight(), content = content)
    }
}

/** Grid columns that grow with width (use in LazyVerticalGrid). */
fun adaptiveGridCells(minCellWidth: Dp = 160.dp): GridCells = GridCells.Adaptive(minCellWidth)

/**
 * List/detail: one pane on COMPACT/MEDIUM (shows [detail] when [showDetail]), two panes on EXPANDED.
 * Selection state is hoisted by the caller (rememberSaveable).
 */
@Composable
fun TwoPane(
    showDetail: Boolean,
    list: @Composable () -> Unit,
    detail: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    emptyDetail: @Composable () -> Unit = {},
) {
    val window = rememberWindowSize()
    if (window.isExpanded) {
        Row(modifier.fillMaxSize()) {
            Box(Modifier.weight(0.42f).fillMaxHeight()) { list() }
            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Box(Modifier.weight(0.58f).fillMaxHeight()) { if (showDetail) detail() else emptyDetail() }
        }
    } else {
        Box(modifier.fillMaxSize()) { if (showDetail) detail() else list() }
    }
}
