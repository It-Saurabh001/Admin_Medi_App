package com.saurabh.mediadminapp.ui.screens

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.SellHistory
import com.saurabh.mediadminapp.ui.screens.components.SalesCard
import com.saurabh.mediadminapp.ui.screens.components.SalesFilter
import com.saurabh.mediadminapp.utils.cardColors
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(viewModel: MyViewModel, navController: NavController){

    val state  = viewModel.getSellHistory.collectAsState()
    LaunchedEffect(key1 = Unit) {
        viewModel.getAllSellHistory()
    }
    Scaffold {
        innerpadding->
        when {
            state.value.isLoading ->{
                LoadingScreen(modifier = Modifier)
            }
            state.value.error != null->{
                Log.d("TAG", "HistoryScreen:  error :-> ${state.value.error}")
                ErrorScreen(errorMessage = state.value.error.toString(),modifier = Modifier.padding(innerpadding))
            }
            state.value.success != null ->{
                HistoryListScreen(state.value.success!!.sell_history, modifier = Modifier.padding(innerpadding))
            }
        }
    }
}


fun aggregateSalesPerDay(data: List<SellHistory>, filter: SalesFilter): List<Pair<String, Double>>
{


    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return when(filter){
        SalesFilter.Day ->  {
            // Group by exact date (yyyy-MM-dd)
            val shortFormatter = DateTimeFormatter.ofPattern("dd")
            data.groupBy { it.date_of_sell } // date wise group
                .map { (date, sales) ->
                    date to sales.sumOf { it.total_amount }
                }
                .sortedBy { LocalDate.parse(it.first,formatter) } // ascending order of date
        }
        SalesFilter.Month -> {
            // Group by Month-Year (e.g. "Jul 2025")
            val monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy")
            data.groupBy {
                LocalDate.parse(it.date_of_sell,formatter).withDayOfMonth(1)
            } // "YYYY-MM" format
                .map { (month, sales) ->
                    month.format(monthFormatter) to sales.sumOf { it.total_amount }
                }
                .sortedBy { YearMonth.parse(it.first, monthFormatter) } // ascending order of month
        }
        SalesFilter.Year -> {
            data.groupBy {
                LocalDate.parse(it.date_of_sell,formatter).year
            } // "YYYY"
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        SalesFilter.values().forEach { filter ->
            Button(
                onClick = { onSelect(filter) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (filter == selected) Color.LightGray else  Color(0xFF4CAF50)
                )
            ) {
                Text(filter.name)
            }
        }
    }
}

@Composable
fun SalesChart(data: List<SellHistory>) {
    var selectedFilter = rememberSaveable { mutableStateOf(SalesFilter.Year) }

    val aggregated = remember(data, selectedFilter.value) {
        aggregateSalesPerDay(data, selectedFilter.value)
    }

    // Determine visible items based on filter
    val visibleCount = when (selectedFilter.value) {
        SalesFilter.Day -> 15
        SalesFilter.Month -> 6
        SalesFilter.Year -> 3
    }

    // Always show last N items (latest days/months/years)
    val startIndex = if (aggregated.size > visibleCount) aggregated.size - visibleCount else 0
    val visibleData = aggregated.subList(startIndex, aggregated.size)

    Column(
        modifier = Modifier.padding(16.dp).background(color =Color(0xFF4CAF50) )
            .border(1.dp, Color.Black, shape = RoundedCornerShape(4.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(9.dp))
        Text(
            text = "Sales History",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Filter Tabs
        SalesFilterTabs(selected = selectedFilter.value) { selectedFilter.value = it }

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal scroll container
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
            Canvas(
                modifier = Modifier
                    .height(250.dp)
                    .width((visibleData.size * 300).dp) // dynamic width based on visible points
            ) {
                val leftPadding = 60f
                val bottomPadding = 50f
                val canvasWidth = size.width
                val canvasHeight = size.height - bottomPadding
                val maxAmount = aggregated.maxOfOrNull { it.second } ?: 0.0
                val spacingX = (canvasWidth - leftPadding) / (visibleData.size + 1)

                // Grid effect
                val gridEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                // Y-axis lines + labels
                val yLineCount = when (selectedFilter.value) {
                    SalesFilter.Day -> 15
                    SalesFilter.Month -> 6
                    SalesFilter.Year -> 3
                }
                val stepY = if (yLineCount != 0) maxAmount / yLineCount else 1.0

                // Draw Y-axis line
                drawLine(
                    color = Color.Black,
                    start = Offset(leftPadding, 0f),
                    end = Offset(leftPadding, canvasHeight),
                    strokeWidth = 2f
                )

                // Draw X-axis line
                drawLine(
                    color = Color.Black,
                    start = Offset(leftPadding, canvasHeight),
                    end = Offset(canvasWidth, canvasHeight),
                    strokeWidth = 2f
                )

                // Draw horizontal grid + Y labels
                for (i in 0..yLineCount) {
                    val y = canvasHeight - (i * stepY / maxAmount * canvasHeight).toFloat()
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(leftPadding, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1f,
                        pathEffect = gridEffect
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        "${(i * stepY).toInt()}",
                        0f,
                        y + 5f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 28f
                            textAlign = android.graphics.Paint.Align.LEFT
                        }
                    )
                }

                // Draw vertical grid + X labels
                visibleData.forEachIndexed { i, (date, amount) ->
                    val x = leftPadding + (i + 1) * spacingX
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(x, 0f),
                        end = Offset(x, canvasHeight),
                        strokeWidth = 1f,
                        pathEffect = gridEffect
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        date,
                        x,
                        canvasHeight + 30f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 28f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }

                // Draw lines between points
                for (i in 1 until visibleData.size) {
                    val x1 = leftPadding + i * spacingX
                    val y1 = canvasHeight - (visibleData[i - 1].second / maxAmount * canvasHeight).toFloat()
                    val x2 = leftPadding + (i + 1) * spacingX
                    val y2 = canvasHeight - (visibleData[i].second / maxAmount * canvasHeight).toFloat()
                    drawLine(
                        color = Color(0xFF4CAF50),
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = 4f
                    )
                }

                // Draw points
                visibleData.forEachIndexed { i, (_, value) ->
                    val x = leftPadding + (i + 1) * spacingX
                    val y = canvasHeight - (value / maxAmount * canvasHeight)
                    drawCircle(
                        color = Color(0xFF2E7D32),
                        radius = 8f,
                        center = Offset(x, y.toFloat())
                    )
                }
            }
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
//            StatsCards(histories)
            SalesChart(histories)
        }
        itemsIndexed(histories) { index,historyItem ->
            val bgColor = cardColors[index%cardColors.size]
            Row(modifier = Modifier.fillMaxWidth().padding(10.dp)){
                SalesCard(historyItem, bgColor,)
            }
        }
    }
}


@Composable
fun EachHistoryCard(sellHistory: SellHistory){

    ElevatedCard(modifier = Modifier
        .fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)) {
        Row (modifier = Modifier.fillMaxWidth()){
            Column(modifier = Modifier
                .fillMaxWidth())
            {
                // userId and Name
                Row (modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,){

                    HorizontalScrollableText(sellHistory.user_name, style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .padding(16.dp)
                    )
                    HorizontalScrollableText(sellHistory.Sell_id , modifier = Modifier.padding(start = 16.dp,top = 16.dp, bottom = 16.dp),style = TextStyle(
                        color = Color.Gray,
                        fontSize = 12.sp
                    ))
                }
                Row (modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,){
                    HorizontalScrollableText(sellHistory.product_name, style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .padding(start = 16.dp, bottom = 16.dp)
                    )
                    HorizontalScrollableText(sellHistory.date_of_sell , modifier = Modifier.padding(start = 16.dp, bottom = 16.dp),style = TextStyle(
                        color = Color.Gray,
                        fontSize = 12.sp
                    ))
                }


            }
        }


    }
}


@Preview
@Composable
private fun histryscreen() {
    HistoryListScreen(
        histories = listOf(
            SellHistory(Order_id = "12345",Sell_id = "67890",
                id = 1,
                isApproved = true,
                price = 100.0,
                product_id = "prod_001",
                product_name = "Sample Product",
                quantity = 2,
                remaining_stock = 50,
                total_amount = 200.0,
                user_id = "user_001",
                user_name = "John Doe",
                date_of_sell = "2025-07-17"
            ),
            SellHistory(Order_id = "54321",Sell_id = "09876",
                id = 2,
                isApproved = false,
                price = 150.0,
                product_id = "prod_002",
                product_name = "Another Product",
                quantity = 1,
                remaining_stock = 30,
                total_amount = 150.0,
                user_id = "user_002",
                user_name = "Jane Smith",
                date_of_sell = "2025-07-18"
            )
        )
    )
    
}


//@Preview
@Composable
private fun histroy() {
    EachHistoryCard(
            SellHistory(Order_id = "12345",Sell_id = "67890",
                id = 1,
                isApproved = true,
                price = 100.0,
                product_id = "prod_001",
                product_name = "Sample Product",
                quantity = 2,
                remaining_stock = 50,
                total_amount = 200.0,
                user_id = "user_001",
                user_name = "John Doe",
                date_of_sell = "2025-07-17"
            )
    )
}