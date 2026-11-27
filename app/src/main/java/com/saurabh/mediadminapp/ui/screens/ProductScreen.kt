package com.saurabh.mediadminapp.ui.screens

import com.saurabh.mediadminapp.ui.components.GsPullToRefreshBox
import com.saurabh.mediadminapp.ui.components.GsFilterChip
import com.saurabh.mediadminapp.ui.components.GsTextField
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.data.v1.core.UiState
import com.saurabh.mediadminapp.data.v1.dto.Product
import com.saurabh.mediadminapp.ui.adaptive.adaptiveGridCells
import com.saurabh.mediadminapp.ui.components.AppScreenScaffold
import com.saurabh.mediadminapp.ui.components.MedicineImage
import com.saurabh.mediadminapp.ui.components.Tone
import com.saurabh.mediadminapp.ui.components.ToneChip
import com.saurabh.mediadminapp.ui.components.shadeRow
import com.saurabh.mediadminapp.ui.components.EmptyState
import com.saurabh.mediadminapp.ui.components.ErrorState
import com.saurabh.mediadminapp.ui.components.SkeletonList
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import com.saurabh.mediadminapp.utils.utilityFunctions.capitalizeEachWord
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged

private val SCHEDULES = listOf("OTC", "H", "H1", "X", "NARCOTIC")
private val SORTS = listOf("name" to "Name", "price" to "Price", "stock" to "Stock", "updated_at" to "Recently updated")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    viewModel: MyViewModel,
    navController: NavController,
    onMenuClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
) {
    val state by viewModel.getAllProduct.collectAsStateWithLifecycle()
    val filter by viewModel.productFilter.collectAsStateWithLifecycle()
    val sort by viewModel.productSort.collectAsStateWithLifecycle()
    val categories by viewModel.productCategories.collectAsStateWithLifecycle()
    val focus = LocalFocusManager.current
    LaunchedEffect(Unit) { viewModel.getAllProduct(); viewModel.loadProductCategories() }

    // Debounced server search; the text field stays responsive.
    var query by rememberSaveable { mutableStateOf(filter.q.orEmpty()) }
    LaunchedEffect(query) {
        val q = query.trim().ifBlank { null }
        if (q == viewModel.productFilter.value.q) return@LaunchedEffect
        delay(400)
        viewModel.setProductFilter(viewModel.productFilter.value.copy(q = q))
    }

    val gridState = rememberLazyGridState()
    val fabExpanded by remember { derivedStateOf { gridState.firstVisibleItemIndex == 0 || gridState.lastScrolledBackward } }
    var sortMenu by remember { mutableStateOf(false) }

    // Load more when 4 items from the end (guarded in VM too).
    val loadMore by rememberUpdatedState { viewModel.loadMoreProducts() }
    LaunchedEffect(gridState) {
        snapshotFlow {
            val info = gridState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && last >= info.totalItemsCount - 4
        }.distinctUntilChanged().collect { if (it) loadMore() }
    }

    AppScreenScaffold(
        title = "Products",
        navigationIcon = { com.saurabh.mediadminapp.ui.components.TileIconButton(Icons.Outlined.Menu, "Menu", onMenuClick, Modifier.padding(start = 4.dp)) },
        scrollBehavior = scrollBehavior,
        actions = {
            com.saurabh.mediadminapp.ui.components.TileIconButton(
                Icons.Outlined.QrCodeScanner, "Scan barcode", { navController.navigate(Routes.BarcodeScannerRoutes.route) { launchSingleTop = true } }
            )
            Box {
                com.saurabh.mediadminapp.ui.components.TileIconButton(Icons.AutoMirrored.Outlined.Sort, "Sort", { sortMenu = true })
                DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }, containerColor = MaterialTheme.colorScheme.surface) {
                    SORTS.forEach { (key, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            leadingIcon = { RadioButton(selected = sort.sort == key, onClick = null) },
                            onClick = {
                                sortMenu = false
                                val order = if (key == "name") "asc" else "desc"
                                viewModel.setProductFilter(sort = key, order = order)
                            }
                        )
                    }
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text(if (sort.order == "desc") "Descending" else "Ascending") },
                        onClick = {
                            sortMenu = false
                            viewModel.setProductFilter(order = if (sort.order == "desc") "asc" else "desc")
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(visible = fabExpanded, enter = scaleIn(), exit = scaleOut()) {
                com.saurabh.mediadminapp.ui.components.GsFab(
                    text = "Add product", icon = Icons.Outlined.Add,
                    onClick = { navController.navigate(Routes.AddProductRoutes.route) { launchSingleTop = true } },
                )
            }
        }
    ) { inner ->
        val items = state.success?.items
        GsPullToRefreshBox(
            isRefreshing = state.isLoading && items != null,
            onRefresh = { viewModel.getAllProduct(force = true); viewModel.loadProductCategories(force = true) },
            modifier = Modifier.padding(inner).fillMaxSize()
        ) {
            LazyVerticalGrid(
                columns = adaptiveGridCells(320.dp),
                state = gridState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "search", span = { GridItemSpan(maxLineSpan) }, contentType = "search") {
                    GsTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search name, generic, brand, barcode") },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                        trailingIcon = if (query.isNotEmpty()) {
                            { IconButton(onClick = { query = "" }) { Icon(Icons.Outlined.Close, contentDescription = "Clear search") } }
                        } else null,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
                        shape = MaterialTheme.shapes.extraLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item(key = "filters", span = { GridItemSpan(maxLineSpan) }, contentType = "filters") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item(key = "instock") {
                            GsFilterChip(
                                selected = filter.inStock == true,
                                onClick = { viewModel.setProductFilter(filter.copy(inStock = if (filter.inStock == true) null else true)) },
                                label = { Text("In stock") }
                            )
                        }
                        item(key = "active") {
                            GsFilterChip(
                                selected = filter.isActive != null,
                                onClick = {
                                    // All -> Active -> Inactive -> All
                                    val next = when (filter.isActive) { null -> true; true -> false; false -> null }
                                    viewModel.setProductFilter(filter.copy(isActive = next))
                                },
                                label = { Text(when (filter.isActive) { null -> "Any status"; true -> "Active"; false -> "Inactive" }) }
                            )
                        }
                        items(SCHEDULES, key = { "s$it" }) { s ->
                            GsFilterChip(
                                selected = filter.scheduleType == s,
                                onClick = { viewModel.setProductFilter(filter.copy(scheduleType = if (filter.scheduleType == s) null else s)) },
                                label = { Text(if (s == "OTC") s else "Sch $s", maxLines = 1) }
                            )
                        }
                        val cats = (categories as? UiState.Success)?.data.orEmpty()
                        items(cats, key = { "c${it.name}" }) { c ->
                            GsFilterChip(
                                selected = filter.category.equals(c.name, ignoreCase = true),
                                onClick = {
                                    val same = filter.category.equals(c.name, ignoreCase = true)
                                    viewModel.setProductFilter(filter.copy(category = if (same) null else c.name))
                                },
                                label = { Text("${c.name.capitalizeEachWord()} (${c.productCount})", maxLines = 1, overflow = TextOverflow.Ellipsis) }
                            )
                        }
                    }
                }
                when {
                    items == null && state.error != null -> item(key = "error", span = { GridItemSpan(maxLineSpan) }, contentType = "state") {
                        ErrorState(message = state.error.orEmpty(), onRetry = { viewModel.getAllProduct(force = true) })
                    }
                    items == null -> item(key = "skeleton", span = { GridItemSpan(maxLineSpan) }, contentType = "state") {
                        SkeletonList(rows = 6, modifier = Modifier.padding(0.dp))
                    }
                    items.isEmpty() -> item(key = "empty", span = { GridItemSpan(maxLineSpan) }, contentType = "state") {
                        val filtered = filter != com.saurabh.mediadminapp.data.v1.repository.ProductFilter()
                        EmptyState(
                            title = if (filtered) "No matching products" else "No products yet",
                            message = if (filtered) "Try a different search or filter." else "Add your first product with the button below."
                        )
                    }
                    else -> {
                        items(items, key = { it.productId }, contentType = { "product" }) { p ->
                            ProductRow(p) { navController.navigate(Routes.SpecificProductRoutes.invoke(p.productId)) { launchSingleTop = true } }
                        }
                        if (state.isLoadingMore) item(key = "more", span = { GridItemSpan(maxLineSpan) }, contentType = "footer") {
                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductRow(p: Product, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().shadeRow(onClick)) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
            leadingContent = { MedicineImage(p.imageUrl, size = 64.dp) },
            headlineContent = {
                Text(p.name.capitalizeEachWord(), style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            },
            supportingContent = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        listOf(p.strength, p.packSize).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { p.category.capitalizeEachWord() },
                        style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        when {
                            !p.inStock -> ToneChip("Out of stock", Tone.Danger)
                            p.stock <= p.reorderLevel -> ToneChip("Low stock · ${p.stock}", Tone.Warning)
                            else -> ToneChip("In stock · ${p.stock}", Tone.Success)
                        }
                        if (p.requiresPrescription) ToneChip("Rx ${p.scheduleType}", Tone.Info)
                        if (!p.isActive) ToneChip("Inactive", Tone.Danger)
                    }
                }
            },
            trailingContent = {
                Text("₹%.2f".format(p.price), style = MaterialTheme.typography.titleSmall, maxLines = 1)
            }
        )
    }
}

