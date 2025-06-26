package com.saurabh.mediadminapp.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.Order
import com.saurabh.mediadminapp.ui.screens.nav.AddProductRoutes
import com.saurabh.mediadminapp.ui.screens.nav.SpecificProductRoutes

@Composable
fun OrderDetailsScreen(viewModel: MyViewModel, navController: NavController){
    val response = viewModel.getAllOrderState.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.getAllOrders()
    }

    Scaffold (
        floatingActionButton = {
            FloatingActionButton(onClick = {}) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ){
        innerpadding ->
        Text(text = "Order Details Screen", modifier = Modifier.padding(innerpadding))

        when{
            response.value.isLoading ->{
                Box(
                    modifier = Modifier.padding(innerpadding)
                        .fillMaxSize()

                ) {
                    LoadingScreen(modifier = Modifier)
                }
            }
            response.value.error != null ->{
                Box(
                    modifier = Modifier.padding(innerpadding)
                        .fillMaxSize()

                ) {
                    Log.d("TAG", "ProductScreen: error :-> ${response.value.error}")
                    ErrorScreen(
                        errorMessage = response.value.error.toString(),
                    )
                }
            }
            response.value.success != null ->{
                Box(
                    modifier = Modifier
                        .fillMaxSize()

                ) {
                    OrdersListScreen(
                        response.value.success!!.orders,
                        navController,
                    )
                }
            }
        }



    }
}

@Composable
fun OrdersListScreen(orders: List<Order>, navController: NavController) {

    Box(
        // it shows all products in list
        modifier = Modifier.fillMaxSize(),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Top
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items (orders){orderItem->
                EachOrderCard(orderItem,  navController)
            }
        }

    }
}

@Composable
fun EachOrderCard(order: Order, navController: NavController) {
    ElevatedCard (modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)
        .clickable(onClick = {}),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)){
        Row (modifier = Modifier.fillMaxWidth()){
            Column (modifier = Modifier.fillMaxWidth(0.5f)){
                HorizontalScrollableText("Id: \n"+order.order_id, style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                    modifier = Modifier.fillMaxWidth().padding(13.dp)
                )
            }
            Column(modifier = Modifier.fillMaxWidth()) {
                HorizontalScrollableText(
                    "UserId: \n "+order.user_id, style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    modifier = Modifier.fillMaxWidth().padding(13.dp)
                )
                HorizontalScrollableText(
                    "User Name: \n "+order.user_name, style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    modifier = Modifier.fillMaxWidth().padding(13.dp)
                )
            }
        }
    }
}