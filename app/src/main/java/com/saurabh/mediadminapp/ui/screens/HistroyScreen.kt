package com.saurabh.mediadminapp.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.SellHistory
import com.saurabh.mediadminapp.ui.screens.components.ClayCard
import com.saurabh.mediadminapp.ui.screens.components.ClayDetailButton
import com.saurabh.mediadminapp.ui.screens.components.ClayEmptyState
import com.saurabh.mediadminapp.ui.screens.components.ClayErrorScreen
import com.saurabh.mediadminapp.ui.screens.components.ClayFilterChip
import com.saurabh.mediadminapp.ui.screens.components.ClayGradientBackdrop
import com.saurabh.mediadminapp.ui.screens.components.ClayLoadingScreen
import com.saurabh.mediadminapp.ui.screens.components.ClayStatusBadge
import com.saurabh.mediadminapp.ui.screens.components.DonutChart
import com.saurabh.mediadminapp.ui.screens.components.DonutSegment
import com.saurabh.mediadminapp.ui.screens.components.SalesFilter
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import com.saurabh.mediadminapp.ui.theme.ClayBadgeApproved
import com.saurabh.mediadminapp.ui.theme.ClayBadgePending
import com.saurabh.mediadminapp.ui.theme.ClayChartColors
import com.saurabh.mediadminapp.ui.theme.ClayFieldBg
import com.saurabh.mediadminapp.ui.theme.ClayHistoryGradient
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary
import com.saurabh.mediadminapp.utils.cardColors
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: MyViewModel, navController: NavController) {
    val state = viewModel.getSellHistory.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.getAllSellHistory()
    }

    Scaffold(containerColor = Color.Transparent) { innerPadding ->
        ClayGradientBackdrop(gradient = ClayHistoryGradient) {
            when {
                state.value.isLoading -> {
                    ClayLoadingScreen(modifier = Modifier.padding(innerPadding))
                }
                state.value.error != null -> {
                    Log.d("TAG", "HistoryScreen error: ${state.value.error}")
                    ClayErrorScreen(
                        errorMessage = state.value.error.toString(),
                        modifier = Modifier.padding(innerPadding),
                        onRetry = { viewModel.getAllSellHistory() }
                    )
                }
                state.value.success != null -> {
                    HistoryLedgerScreen(
                        histories = state.value.success!!.sell_history,
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

fun aggregateSalesPerDay(data: List<SellHistory>, filter: SalesFilter): List<Pair<String, Double>> {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return when (filter) {
        SalesFilter.Day -> {
            data.groupBy { it.date_of_sell }
                .map { (date, sales) -> date to sales.sumOf { it.total_amount } }
                .sortedBy { LocalDate.parse(it.first, formatter) }
        }
        SalesFilter.Month -> {
            val monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy")
            data.groupBy { LocalDate.parse(it.date_of_sell, formatter).withDayOfMonth(1) }
                .map { (month, sales) -> month.format(monthFormatter) to sales.sumOf { it.total_amount } }
                .sortedBy { YearMonth.parse(it.first, monthFormatter) }
        }
        SalesFilter.Year -> {
            data.groupBy { LocalDate.parse(it.date_of_sell, formatter).year }
                .map { (year, sales) -> year.toString() to sales.sumOf { it.total_amount } }
                .sortedBy { it.first.toInt() }
        }
    }
}

@Composable
fun HistoryLedgerScreen(
    histories: List<SellHistory>,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val selectedFilter = rememberSaveable { mutableStateOf(SalesFilter.Year) }

    val aggregated = remember(histories, selectedFilter.value) {
        aggregateSalesPerDay(histories, selectedFilter.value)
    }

    val visibleData = aggregated.takeLast(5)
    val totalSalesValue = histories.sumOf { it.total_amount }

    val segments = visibleData.mapIndexed { index, (label, value) ->
        DonutSegment(
            label = label,
            value = value,
            color = ClayChartColors[index % ClayChartColors.size]
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp)
    ) {
        // ── 1. ASYMMETRIC REVENUE & MILESTONE HERO DECK ───────────────────
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 28.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ClayPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = ClayPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Transaction Ledger",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ClayTextPrimary
                                )
                                Text(
                                    text = "${histories.size} Completed Sales",
                                    fontSize = 12.sp,
                                    color = ClayTextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Filter Pill Dock
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(SalesFilter.entries.toTypedArray()) { filter ->
                                ClayFilterChip(
                                    label = filter.name,
                                    selected = filter == selectedFilter.value,
                                    onClick = { selectedFilter.value = filter },
                                    gradient = ClayHistoryGradient
                                )
                            }
                        }
                    }

                    // Interactive Donut Chart / Metric
                    if (segments.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            DonutChart(
                                segments = segments,
                                centerLabel = "₹${NumberFormat.getInstance(Locale.Builder().setLanguage("en").setRegion("IN").build()).format(totalSalesValue)}",
                                centerSubLabel = "Total Revenue",
                                chartSize = 170.dp
                            )
                        }
                    }
                }
            }
        }

        // ── 2. TIMELINE-ANCHORED TRANSACTION RECEIPT CARDS ───────────────
        if (histories.isEmpty()) {
            item {
                ClayEmptyState(
                    message = "No sales history records",
                    subtitle = "Completed orders will appear here in the ledger stream",
                    emoji = "🧾"
                )
            }
        } else {
            itemsIndexed(histories, key = { _, item -> item.Sell_id }) { index, historyItem ->
                val bgTint = cardColors[index % cardColors.size]
                TimelineReceiptCard(
                    sale = historyItem,
                    bgColor = bgTint,
                    onDetailsClick = {
                        // Two-tier scannable hierarchy: navigate to dedicated SpecificOrderScreen
                        navController.navigate(Routes.SpecificOrderRoutes.invoke(historyItem.Order_id))
                    }
                )
            }
        }
    }
}

/**
 * Two-Tier Scannable Timeline Receipt Card:
 * Features a milestone progress track node, scannable primary identity (Sell ID, Product, User),
 * high-contrast price deck, and spring-reactive "Details" button navigating to SpecificOrderScreen.
 */
@Composable
fun TimelineReceiptCard(
    sale: SellHistory,
    bgColor: Color,
    onDetailsClick: () -> Unit
) {
    val isDone = sale.isApproved
    val statusColor = if (isDone) ClayBadgeApproved else ClayBadgePending
    val statusText = if (isDone) "Completed" else "Pending"

    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        bgColor = bgColor
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Timeline Milestone Node
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(end = 14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.14f))
                        .border(1.5.dp, statusColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.Schedule,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Scannable Primary Receipt Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sale.product_name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ClayTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    ClayStatusBadge(
                        text = statusText,
                        color = statusColor
                    )
                }

                Text(
                    text = "Customer: ${sale.user_name}  •  Qty: ${sale.quantity}",
                    fontSize = 12.sp,
                    color = ClayTextSecondary,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "Sold: ${sale.date_of_sell}  •  ID: ${sale.Sell_id}",
                    fontSize = 11.sp,
                    color = ClayTextSecondary.copy(alpha = 0.8f)
                )

                // Split Revenue Deck + Prominent "Details" Trigger
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL VALUE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ClayTextSecondary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "₹${NumberFormat.getInstance(Locale.Builder().setLanguage("en").setRegion("IN").build()).format(sale.total_amount)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ClayPrimary
                        )
                    }

                    ClayDetailButton(
                        text = "Details",
                        onClick = onDetailsClick
                    )
                }
            }
        }
    }
}