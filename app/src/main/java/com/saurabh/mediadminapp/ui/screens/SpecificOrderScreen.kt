package com.saurabh.mediadminapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.saurabh.mediadminapp.utils.utilityFunctions.DismissKeyboardOnTapScreen

@Composable
fun SpecificOrderScreen(orderId : String, viewModel: MyViewModel, navController: NavController) {
    BackHandler {
        viewModel.clearGetOrderByIdState()
        navController.popBackStack()
    }
    val response = viewModel.getOrderByIdState.collectAsState()
    val order = viewModel.getUsersOrdersState.collectAsState()
    LaunchedEffect(orderId) {
        viewModel.getOrderById(orderId)
    }
    DismissKeyboardOnTapScreen {
        Scaffold {
            when {
                response.value.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingScreen()
                    }
                }

                response.value.error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ErrorScreen(
                            errorMessage = response.value.error.toString(),
                            modifier = Modifier.padding(it)
                        )
                    }
                }

                response.value.success != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        EachOrder(response.value.success!!.order, navController)
                    }
                }
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