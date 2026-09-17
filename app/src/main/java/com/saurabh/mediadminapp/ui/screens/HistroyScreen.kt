package com.saurabh.mediadminapp.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.SellHistory
import com.saurabh.mediadminapp.ui.screens.components.ClayCard
import com.saurabh.mediadminapp.ui.screens.components.ClayErrorScreen
import com.saurabh.mediadminapp.ui.screens.components.ClayFilterChip
import com.saurabh.mediadminapp.ui.screens.components.ClayLoadingScreen
import com.saurabh.mediadminapp.ui.screens.components.SalesCard
import com.saurabh.mediadminapp.ui.screens.components.SalesFilter
import com.saurabh.mediadminapp.ui.screens.components.DonutChart
import com.saurabh.mediadminapp.ui.screens.components.DonutSegment
import com.saurabh.mediadminapp.ui.theme.ClayChartColors
import com.saurabh.mediadminapp.ui.theme.ClayHistoryGradient
import com.saurabh.mediadminapp.ui.theme.ClayScreenBg
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.utils.cardColors
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: MyViewModel, navController: NavController){
    val state  = viewModel.getSellHistory.collectAsState()
    
    LaunchedEffect(key1 = Unit) {
        viewModel.getAllSellHistory()
    }
    
    Scaffold(modifier = Modifier.background(ClayScreenBg)) { innerpadding ->
        when {
            state.value.isLoading -> {
                ClayLoadingScreen(modifier = Modifier.padding(innerpadding))
            }
            state.value.error != null -> {
                Log.d("TAG", "HistoryScreen:  error :-> ${state.value.error}")
                ClayErrorScreen(
                    errorMessage = state.value.error.toString(),
                    modifier = Modifier.padding(innerpadding)
                )
            }
            state.value.success != null -> {
                HistoryListScreen(
                    histories = state.value.success!!.sell_history, 
                    modifier = Modifier
                        .padding(innerpadding)
                        .background(ClayScreenBg)
                )
            }
        }
    }
}

fun aggregateSalesPerDay(data: List<SellHistory>, filter: SalesFilter): List<Pair<String, Double>> {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return when(filter){
        SalesFilter.Day ->  {
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
fun SalesFilterTabs(
    selected: SalesFilter,
    onSelect: (SalesFilter) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(SalesFilter.entries.toTypedArray()) { filter ->
            ClayFilterChip(
                label = filter.name,
                selected = filter == selected,
                onClick = { onSelect(filter) },
                gradient = ClayHistoryGradient
            )
        }
    }
}

@Composable
fun SalesAnalyticsSection(data: List<SellHistory>) {
    val selectedFilter = rememberSaveable { mutableStateOf(SalesFilter.Year) }

    val aggregated = remember(data, selectedFilter.value) {
        aggregateSalesPerDay(data, selectedFilter.value)
    }

    // Convert top 5 recent aggregated data to DonutSegments for the animated chart
    val visibleData = aggregated.takeLast(5)
    val totalSalesValue = visibleData.sumOf { it.second }
    
    val segments = visibleData.mapIndexed { index, (label, value) ->
        DonutSegment(
            label = label,
            value = value,
            color = ClayChartColors[index % ClayChartColors.size]
        )
    }

    ClayCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Sales Overview",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = ClayTextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        SalesFilterTabs(selected = selectedFilter.value) { selectedFilter.value = it }

        Spacer(modifier = Modifier.height(16.dp))
        
        if (segments.isEmpty()) {
            Text("No data for selected period", modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            DonutChart(
                segments = segments,
                centerLabel = "₹${NumberFormat.getInstance(Locale.Builder().setLanguage("en").setRegion("IN").build()).format(totalSalesValue)}",
                centerSubLabel = "Total Sales",
                chartSize = 200.dp
            )
        }
    }
}

@Composable
fun HistoryListScreen(
    histories: List<SellHistory>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        item {
            SalesAnalyticsSection(histories)
        }
        
        itemsIndexed(histories) { index, historyItem ->
            val bgColor = cardColors[index % cardColors.size]
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                SalesCard(historyItem, bgColor)
            }
        }
    }
}