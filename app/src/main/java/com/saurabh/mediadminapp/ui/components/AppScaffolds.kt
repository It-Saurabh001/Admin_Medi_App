package com.saurabh.mediadminapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import com.saurabh.mediadminapp.ui.theme.isLight
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.saurabh.mediadminapp.ui.adaptive.AdaptiveContent
import com.saurabh.mediadminapp.ui.adaptive.AdaptiveWidths
import com.saurabh.mediadminapp.ui.device.hasCamera
import com.saurabh.mediadminapp.ui.device.rememberIsOnline
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/**
 * Standard screen: TopAppBar (titleLarge, optional back) wired to [scrollBehavior] + nestedScroll,
 * offline banner under the bar, optional FAB/bottom bar/snackbar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreenScaffold(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
    floatingActionButton: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHostState: SnackbarHostState? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    navigationIcon = {
                        when {
                            navigationIcon != null -> navigationIcon()
                            // U9: detail screens show the back arrow in an Icon-fill tile with family press shades.
                            onBack != null -> TileIconButton(Icons.AutoMirrored.Outlined.ArrowBack, "Back", onBack, Modifier.padding(start = 4.dp))
                        }
                    },
                    actions = actions,
                    scrollBehavior = scrollBehavior,
                    colors = com.saurabh.mediadminapp.ui.theme.GsUi.topBarColors()
                )
                androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                ConnectivityBanner()
            }
        },
        floatingActionButton = floatingActionButton,
        bottomBar = bottomBar,
        snackbarHost = { snackbarHostState?.let { SnackbarHost(it) } },
        content = content
    )
}

/** "No connection" strip; animates in/out from the shared ConnectivityObserver. */
@Composable
fun ConnectivityBanner(modifier: Modifier = Modifier, onRetry: (() -> Unit)? = null) {
    val online by rememberIsOnline()
    AnimatedVisibility(visible = !online, enter = expandVertically(), exit = shrinkVertically()) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            modifier = modifier.fillMaxWidth()
        ) {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 8.dp).heightIn(min = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Outlined.WifiOff, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("No connection", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                onRetry?.let { TextButton(onClick = it) { Text("Retry") } }
            }
        }
    }
}

/** Loading / error+Retry / empty / content switch. Loading shows skeleton rows. */
@Composable
fun ScreenStateHost(
    isLoading: Boolean,
    error: String?,
    isEmpty: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    emptyTitle: String = "Nothing here yet",
    emptyMessage: String = "",
    content: @Composable () -> Unit,
) {
    Box(modifier.fillMaxSize()) {
        when {
            isLoading && isEmpty -> SkeletonList()
            error != null && isEmpty -> ErrorState(message = error, onRetry = onRetry)
            isEmpty -> EmptyState(title = emptyTitle, message = emptyMessage)
            else -> content()
        }
    }
}

@Composable
fun SkeletonList(rows: Int = 6, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(rows) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp)
            ) {}
        }
    }
}

/**
 * Paged list: PullToRefresh + LazyColumn (keys required) + load-more near the end
 * (guarded by [isLoadingMore]/[endReached]) + footer spinner. Bottom padding keeps FAB clear.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagedLazyColumn(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    isLoadingMore: Boolean,
    endReached: Boolean,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    content: LazyListScope.() -> Unit,
) {
    val loadMore by rememberUpdatedState(onLoadMore)
    val guard by rememberUpdatedState(isLoadingMore || endReached)
    LaunchedEffect(state) {
        snapshotFlow {
            val info = state.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: -1
            info.totalItemsCount > 0 && last >= info.totalItemsCount - 4
        }.distinctUntilChanged().filter { it && !guard }.collect { loadMore() }
    }
    PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = onRefresh, modifier = modifier.fillMaxSize()) {
        AdaptiveContent(maxWidth = AdaptiveWidths.List) {
            LazyColumn(
                state = state,
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
                verticalArrangement = verticalArrangement
            ) {
                content()
                if (isLoadingMore) item(key = "__load_more", contentType = "footer") {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                }
            }
        }
    }
}

/** Form body: one vertical scroller + imePadding + navigationBarsPadding, centered at 560dp. */
@Composable
fun FormScaffold(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    // Part 3 §3: tap on empty space clears focus + hides the keyboard.
    AdaptiveContent(modifier = modifier.clearFocusOnTap(), maxWidth = AdaptiveWidths.Form) {
        Column(
            Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

/** Backend `fields[key]` message (snake_case key), shown as supportingText. */
fun Map<String, String>?.fieldError(key: String): String? = this?.get(key)

@Composable
fun FieldError(text: String?) {
    text ?: return
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
}

/**
 * Counter tile (standalone card, U13: family press shade + 0.97 scale, no ripple).
 * [tone] gives the colour meaning (guide Part 5): null/Success = green icon tile on White card,
 * Warning = Amber 50 card + Amber 100/600 tile, Danger = Red 50 card + Red 100/600 tile, Info = Teal 100/600 tile.
 * [highlight] is kept for callers that only need "success" emphasis.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
    tone: Tone? = if (highlight) Tone.Success else null,
) {
    val G = com.saurabh.mediadminapp.ui.theme.GS
    val S = com.saurabh.mediadminapp.ui.theme.Shades
    val light = MaterialTheme.isLight
    val source = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    // Card surface: alert tones use their 50 card shade (50 -> 100 -> 200); others are White rows (White -> Green 50 -> 100).
    val cardShade = when (tone) {
        Tone.Warning -> com.saurabh.mediadminapp.ui.theme.GsUi.familyShade(S.AmberCard)
        Tone.Danger -> com.saurabh.mediadminapp.ui.theme.GsUi.familyShade(S.RedCard)
        else -> com.saurabh.mediadminapp.ui.theme.GsUi.rowShade()
    }
    val (tileBg, tileFg, valueColor) = when {
        !light -> Triple(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, MaterialTheme.colorScheme.onSurface)
        tone == Tone.Warning -> Triple(G.Amber100, G.Amber600, G.Amber600)
        tone == Tone.Danger -> Triple(G.Red100, G.Red600, G.Red600)
        tone == Tone.Info -> Triple(G.Teal100, G.Teal600, G.Ink)
        tone == Tone.Success -> Triple(G.Green100, G.Green600, G.Green600)
        else -> Triple(G.Green100, G.Green600, G.Ink)
    }
    val scale by source.pressScale(enabled = onClick != null)
    val container by source.shadeColor(cardShade, "statColor")
    val contentColor = MaterialTheme.colorScheme.onSurface
    val body: @Composable ColumnScope.() -> Unit = {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(shape = MaterialTheme.shapes.small, color = tileBg, contentColor = tileFg, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp)) }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                value, style = MaterialTheme.typography.headlineMedium, color = valueColor,
                fontWeight = if (tone == Tone.Warning || tone == Tone.Danger) androidx.compose.ui.text.font.FontWeight.Bold else null,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 2, overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    val colors = CardDefaults.cardColors(containerColor = container, contentColor = contentColor)
    val border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    val m = modifier.fillMaxWidth().graphicsLayer { scaleX = scale; scaleY = scale }
    if (onClick != null) Card(onClick = onClick, colors = colors, border = border, interactionSource = source, modifier = m, content = body)
    else Card(colors = colors, border = border, modifier = m, content = body)
}

/**
 * Hero / header card: the only gradient allowed (U7): Green 800 -> Green 600 -> Green 400, White text.
 * Standalone tappable card: 0.97 scale on press, slight dim (no ripple, no colour-family change).
 */
@Composable
fun HeroCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
) {
    val G = com.saurabh.mediadminapp.ui.theme.GS
    val source = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val scale by source.pressScale(enabled = onClick != null)
    val level by source.pressLevel()
    val shape = MaterialTheme.shapes.large
    Box(
        modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale; alpha = if (level == PressLevel.None) 1f else 0.92f }
            .clip(shape)
            .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(G.Green800, G.Green600, G.Green400)))
            .then(if (onClick != null) Modifier.clickable(interactionSource = source, indication = null, onClick = onClick) else Modifier)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.labelLarge, color = G.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(value, style = MaterialTheme.typography.headlineMedium, color = G.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                subtitle?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = G.White, maxLines = 2, overflow = TextOverflow.Ellipsis) }
            }
            icon?.let { Icon(it, contentDescription = null, tint = G.White, modifier = Modifier.size(40.dp)) }
        }
    }
}

/** Camera / Gallery / Remove sheet. Camera row hidden when the device has no camera. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSourceSheet(
    onDismiss: () -> Unit,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onRemove: (() -> Unit)? = null,
    title: String = "Add image",
) {
    val context = LocalContext.current
    val camera = remember(context) { context.hasCamera() }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        Text(
            title, style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp).semantics { heading() }
        )
        val rowColors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
        if (camera) ListItem(
            headlineContent = { Text("Take photo") },
            leadingContent = { Icon(Icons.Outlined.CameraAlt, null) },
            colors = rowColors,
            modifier = Modifier.clickableRow { onDismiss(); onCamera() }
        )
        ListItem(
            headlineContent = { Text("Choose from gallery") },
            leadingContent = { Icon(Icons.Outlined.PhotoLibrary, null) },
            colors = rowColors,
            modifier = Modifier.clickableRow { onDismiss(); onGallery() }
        )
        onRemove?.let { remove ->
            ListItem(
                headlineContent = { Text("Remove image", color = MaterialTheme.colorScheme.error) },
                leadingContent = { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) },
                colors = rowColors,
                modifier = Modifier.clickableRow(danger = true) { onDismiss(); remove() }
            )
        }
        Spacer(Modifier.navigationBarsPadding().height(16.dp))
    }
}

/** Sheet row: White -> Green 50 -> Green 100 (danger rows: Red 50 / Red 100), no ripple. */
@Composable
private fun Modifier.clickableRow(danger: Boolean = false, onClick: () -> Unit): Modifier {
    val source = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val shade = if (danger) com.saurabh.mediadminapp.ui.theme.GsUi.familyShade(
        com.saurabh.mediadminapp.ui.theme.Shade(com.saurabh.mediadminapp.ui.theme.GS.White, com.saurabh.mediadminapp.ui.theme.GS.Red50, com.saurabh.mediadminapp.ui.theme.GS.Red100)
    ) else com.saurabh.mediadminapp.ui.theme.GsUi.rowShade()
    val bg by source.shadeColor(shade, "sheetRow")
    return this.heightIn(min = 56.dp)
        .background(bg)
        .clickable(interactionSource = source, indication = null, onClick = onClick)
}
