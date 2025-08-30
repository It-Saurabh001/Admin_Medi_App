package com.saurabh.mediadminapp.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.SellHistory
import com.saurabh.mediadminapp.network.response.UserItem
import com.saurabh.mediadminapp.ui.screens.nav.UserSettingsRoutes
import com.saurabh.mediadminapp.utils.ScreensState.IsApprovedUserState

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

@Composable
fun HistoryListScreen(
    histories: List<SellHistory>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {


        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
//            verticalArrangement = Arrangement.spacedBy(8.dp),
//            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 8.dp)
        ) {
            items(histories) { historyItem ->
                Row(modifier = Modifier.fillMaxWidth().padding(10.dp)){
                    EachHistoryCard(historyItem)

                }
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