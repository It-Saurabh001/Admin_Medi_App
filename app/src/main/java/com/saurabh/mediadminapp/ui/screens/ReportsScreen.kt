package com.saurabh.mediadminapp.ui.screens

import com.saurabh.mediadminapp.ui.components.GsFilterChip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.ReportQuery
import com.saurabh.mediadminapp.SalesReportState
import com.saurabh.mediadminapp.data.v1.dto.SalesReportRow
import com.saurabh.mediadminapp.ui.adaptive.rememberWindowSize
import com.saurabh.mediadminapp.ui.components.AppScreenScaffold
import com.saurabh.mediadminapp.ui.components.EmptyState
import com.saurabh.mediadminapp.ui.components.ErrorState
import com.saurabh.mediadminapp.ui.components.PagedLazyColumn
import com.saurabh.mediadminapp.ui.components.SectionHeader
import com.saurabh.mediadminapp.ui.components.SkeletonList
import com.saurabh.mediadminapp.ui.components.StatCard
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val PRESET_DAYS = listOf(7L, 30L, 90L)
private val SHORT_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)

@Composable
internal fun rememberInrFormat(): NumberFormat = remember {
    NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("en").setRegion("IN").build())
        .apply { maximumFractionDigits = 2 }
}

/** Sales report: date range, group by day/product, sort; summary + paged rows. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: MyViewModel, navController: NavController) {
    val state by viewModel.salesReportState.collectAsStateWithLifecycle()
    val window = rememberWindowSize()
    LaunchedEffect(Unit) { viewModel.loadSalesReport() }
    val onQuery: (ReportQuery) -> Unit = { viewModel.loadSalesReport(it) }

    AppScreenScaffold(
        title = "Sales report",
        onBack = { navController.navigateUp() },
        actions = {
            com.saurabh.mediadminapp.ui.components.TileIconButton(
                Icons.AutoMirrored.Outlined.TrendingUp, "Top products", { navController.navigate(Routes.TopProductsRoutes.route) { launchSingleTop = true } }
            )
        }
    ) { inner ->
        val retry = { viewModel.loadSalesReport(force = true) }
        if (window.isExpanded) {
            Row(Modifier.padding(inner).fillMaxSize()) {
                Column(
                    Modifier.width(360.dp).fillMaxHeight().verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ReportFilters(state.query, onQuery, showGroupBy = true, sortOptions = SALES_SORTS)
                    SalesSummary(state)
                }
                VerticalDivider()
                Column(Modifier.weight(1f).fillMaxHeight()) { SalesRows(state, retry, viewModel::loadMoreSalesReport, header = null) }
            }
        } else {
            Column(Modifier.padding(inner).fillMaxSize()) {
                SalesRows(state, retry, viewModel::loadMoreSalesReport) {
                    ReportFilters(state.query, onQuery, showGroupBy = true, sortOptions = SALES_SORTS)
                    Spacer(Modifier.height(12.dp))
                    SalesSummary(state)
                }
            }
        }
    }
}

private val SALES_SORTS = listOf("key" to "Default", "revenue" to "Revenue", "quantity" to "Quantity")

@Composable
private fun SalesSummary(state: SalesReportState) {
    val money = rememberInrFormat()
    val sum = state.summary ?: return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        StatCard("Revenue", money.format(sum.revenue), Icons.Outlined.CurrencyRupee, onClick = null, modifier = Modifier.fillMaxWidth(), highlight = true)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Orders", sum.orders.toString(), Icons.Outlined.ReceiptLong, onClick = null, modifier = Modifier.weight(1f))
            StatCard("Units sold", sum.quantity.toString(), Icons.Outlined.Inventory2, onClick = null, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SalesRows(
    state: SalesReportState,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    header: (@Composable () -> Unit)?,
) {
    val money = rememberInrFormat()
    val listState = rememberLazyListState()
    val maxRevenue by remember(state.rows) { derivedStateOf { state.rows.maxOfOrNull { it.revenue ?: 0.0 }?.takeIf { it > 0 } ?: 1.0 } }
    val byProduct = state.query.groupBy == "product"
    PagedLazyColumn(
        isRefreshing = state.isLoading && state.loaded,
        onRefresh = onRetry,
        isLoadingMore = state.isLoadingMore,
        endReached = state.endReached,
        onLoadMore = onLoadMore,
        state = listState,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
    ) {
        if (header != null) item(key = "header", contentType = "header") { Column { header() } }
        when {
            state.isLoading && !state.loaded -> item(key = "skeleton", contentType = "skeleton") { SkeletonList(rows = 5) }
            state.error != null && state.rows.isEmpty() -> item(key = "error", contentType = "state") {
                val fieldMsg = state.appError.fieldError("group_by") ?: state.appError.fieldError("from") ?: state.appError.fieldError("to")
                ErrorState(message = fieldMsg ?: state.error, onRetry = onRetry)
            }
            state.loaded && state.rows.isEmpty() -> item(key = "empty", contentType = "state") {
                EmptyState(title = "No sales in this range", message = "Try a wider date range.")
            }
            else -> {
                item(key = "rowsHeader", contentType = "header") {
                    SectionHeader(if (byProduct) "By product" else "By day")
                }
                items(state.rows, key = { "${it.key}_${it.productName}" }, contentType = { "row" }) { row ->
                    SalesRowItem(row, byProduct, money, ((row.revenue ?: 0.0) / maxRevenue).toFloat())
                }
            }
        }
    }
}

@Composable
private fun SalesRowItem(row: SalesReportRow, byProduct: Boolean, money: NumberFormat, share: Float) {
    val title = if (byProduct) row.productName ?: row.key ?: "Unknown product"
    else row.key?.let { runCatching { LocalDate.parse(it).format(SHORT_DATE) }.getOrDefault(it) } ?: "-"
    ListItem(
        headlineContent = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    buildString {
                        append("${row.quantity ?: 0} units")
                        row.orders?.let { append(" · $it orders") }
                    },
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                LinearProgressIndicator(
                    progress = { share.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = if (com.saurabh.mediadminapp.ui.theme.GS.White == MaterialTheme.colorScheme.surface) com.saurabh.mediadminapp.ui.theme.GS.Green200 else MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        },
        trailingContent = {
            Text(money.format(row.revenue ?: 0.0), style = MaterialTheme.typography.titleSmall, maxLines = 1)
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium)
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
    )
}

/** Shared by Reports and TopProducts: range presets + custom range, optional group-by, sort + order. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReportFilters(
    query: ReportQuery,
    onQueryChange: (ReportQuery) -> Unit,
    showGroupBy: Boolean,
    sortOptions: List<Pair<String, String>>,
) {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val today = LocalDate.now()
    val presetDays = if (query.to == today) ChronoUnit.DAYS.between(query.from, query.to) else -1L
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "${query.from.format(SHORT_DATE)} – ${query.to.format(SHORT_DATE)}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(PRESET_DAYS, key = { it }) { d ->
                GsFilterChip(
                    selected = presetDays == d,
                    onClick = { onQueryChange(query.copy(from = today.minusDays(d), to = today)) },
                    label = { Text("$d days") }
                )
            }
            item(key = "custom") {
                GsFilterChip(
                    selected = presetDays !in PRESET_DAYS,
                    onClick = { showPicker = true },
                    label = { Text("Custom") },
                    leadingIcon = { Icon(Icons.Outlined.DateRange, contentDescription = null) }
                )
            }
        }
        if (showGroupBy) {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                listOf("day" to "By day", "product" to "By product").forEachIndexed { i, (key, label) ->
                    SegmentedButton(
                        selected = query.groupBy == key,
                        onClick = { onQueryChange(query.copy(groupBy = key)) },
                        shape = SegmentedButtonDefaults.itemShape(i, 2)
                    ) { Text(label, maxLines = 1) }
                }
            }
        }
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            LazyRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sortOptions, key = { it.first }) { (key, label) ->
                    GsFilterChip(
                        selected = (query.sort ?: sortOptions.first().first) == key,
                        onClick = { onQueryChange(query.copy(sort = key)) },
                        label = { Text(label) }
                    )
                }
            }
            val desc = query.order != "asc"
            IconButton(onClick = { onQueryChange(query.copy(order = if (desc) "asc" else "desc")) }) {
                Icon(
                    if (desc) Icons.Outlined.ArrowDownward else Icons.Outlined.ArrowUpward,
                    contentDescription = if (desc) "Descending, tap for ascending" else "Ascending, tap for descending"
                )
            }
        }
    }

    if (showPicker) {
        val todayUtcMillis = today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val pickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = query.from.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            initialSelectedEndDateMillis = query.to.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayUtcMillis
                override fun isSelectableYear(year: Int) = year <= today.year
            }
        )
        val toDate = { ms: Long -> Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate() }
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    enabled = pickerState.selectedStartDateMillis != null,
                    onClick = {
                        val start = pickerState.selectedStartDateMillis?.let(toDate)
                        val end = pickerState.selectedEndDateMillis?.let(toDate) ?: start
                        if (start != null && end != null) onQueryChange(query.copy(from = start, to = end))
                        showPicker = false
                    }
                ) { Text("Apply", style = MaterialTheme.typography.labelLarge) }
            },
            dismissButton = { com.saurabh.mediadminapp.ui.components.NeutralTextButton("Cancel", { showPicker = false }) }
        ) {
            DateRangePicker(state = pickerState, modifier = Modifier.weight(1f))
        }
    }
}
