package com.saurabh.mediadminapp.ui.screens

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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.Order

@Composable
fun EachUserOrderScreen(orderId: String,viewModel: MyViewModel,navController: NavController) {
    val response = viewModel.getUsersOrdersState.collectAsState()

    Scaffold { innerpadding->
        when{
            response.value.isLoading->{
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            response.value.error != null ->{
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerpadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error loading Product data", fontSize = 18.sp)
                        Text(response.value.error.toString(), fontSize = 14.sp)
                    }
                }
            }
            response.value.success != null ->{
                val order = response.value.success!!.order
                if (order != null){
                    UserOrderListScreen(
                        order,
                        navController
                    )
                }
                else{
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Product not found or has been deleted", fontSize = 18.sp)
                    }
                }
            }


        }
    }


}

@Composable
fun UserOrderListScreen(orders: List<Order>, navController: NavController) {
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
                EachUserOrderCard(orderItem,  navController)
            }
        }

    }
}


@Composable
fun EachUserOrderCard(order: Order, navController: NavController) {
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
                    "ProductId: \n "+order.product_id, style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    modifier = Modifier.fillMaxWidth().padding(13.dp)
                )
                HorizontalScrollableText(
                    "Product Name: \n "+order.product_name, style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    modifier = Modifier.fillMaxWidth().padding(13.dp)
                )
            }
        }
    }
}


@Composable
fun EachOrder(order : Order, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    )  {
        Text(
            text = "Id: "+order.id,
            style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp),
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "OrderId: "+order.order_id,
            style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp),
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "UserId: "+order.user_id,
            style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp),
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "ProductId: "+order.product_id,
            style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp),
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "Product: "+order.product_name,
            style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp),
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "Category: "+order.category,
            style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp),
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "Price: "+order.price,
            style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp),
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "Quantity: "+order.quantity,
            style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp),
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "Total Amount: "+order.total_amount,
            style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp),
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "User Name: "+order.user_name,
            style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp),
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "Approval: "+order.isApproved,
            style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp),
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "Date: "+order.date_of_order_creation,
            style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp),
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "Message: "+order.message,
            style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp),
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

    }

}