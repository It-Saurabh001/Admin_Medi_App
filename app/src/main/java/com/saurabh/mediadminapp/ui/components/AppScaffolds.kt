package com.saurabh.mediadminapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                            onBack != null -> IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = actions,
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
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
                modifier = Modifier.fillMaxWidth().height(72.dp)
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
    AdaptiveContent(modifier = modifier, maxWidth = AdaptiveWidths.Form) {
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

/** Dashboard counter tile. [highlight] uses primaryContainer (one per screen). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
) {
    val container = if (highlight) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow
    val contentColor = if (highlight) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    val body: @Composable ColumnScope.() -> Unit = {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
                shape = RoundedCornerShape(50),
                color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                contentColor = if (highlight) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(40.dp)
            ) { Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp)) } }
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 2, overflow = TextOverflow.Ellipsis,
                color = if (highlight) contentColor else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    val colors = CardDefaults.cardColors(containerColor = container, contentColor = contentColor)
    if (onClick != null) Card(onClick = onClick, colors = colors, modifier = modifier.fillMaxWidth(), content = body)
    else Card(colors = colors, modifier = modifier.fillMaxWidth(), content = body)
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
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
        Text(
            title, style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp).semantics { heading() }
        )
        val rowColors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
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
                modifier = Modifier.clickableRow { onDismiss(); remove() }
            )
        }
        Spacer(Modifier.navigationBarsPadding().height(16.dp))
    }
}

private fun Modifier.clickableRow(onClick: () -> Unit): Modifier =
    this.heightIn(min = 56.dp).clickable(onClick = onClick)
