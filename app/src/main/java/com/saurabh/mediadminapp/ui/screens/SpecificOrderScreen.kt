package com.saurabh.mediadminapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.Order
import com.saurabh.mediadminapp.ui.screens.components.ClayCard
import com.saurabh.mediadminapp.ui.screens.components.ClayErrorScreen
import com.saurabh.mediadminapp.ui.screens.components.ClayLoadingScreen
import com.saurabh.mediadminapp.ui.theme.ClayScreenBg
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary
import com.saurabh.mediadminapp.utils.utilityFunctions.DismissKeyboardOnTapScreen
import com.saurabh.mediadminapp.utils.utilityFunctions.capitalizeEachWord

@Composable
fun SpecificOrderScreen(orderId: String, viewModel: MyViewModel, navController: NavController) {
    BackHandler {
        viewModel.clearGetOrderByIdState()
        navController.popBackStack()
    }
    
    val response = viewModel.getOrderByIdState.collectAsState()
    
    LaunchedEffect(orderId) {
        viewModel.getOrderById(orderId)
    }
    
    DismissKeyboardOnTapScreen {
        Scaffold(modifier = Modifier.background(color = ClayScreenBg)) { innerPadding ->
            when {
                response.value.isLoading -> {
                    ClayLoadingScreen(modifier = Modifier.padding(innerPadding))
                }

                response.value.error != null -> {
                    ClayErrorScreen(
                        errorMessage = response.value.error.toString(),
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                response.value.success != null -> {
                    Box(
                        modifier = Modifier
                            .background(color = ClayScreenBg)
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        EachOrderList(response.value.success!!.order)
                    }
                }
            }
        }
    }
}

@Composable
fun EachOrderList(order: Order) {
    val cardColors = listOf(
        Color(0xFFD3C4FA), // Lavender
        Color(0xFFACF68F), // Green
        Color(0xFFE9CF87), // Amber
        Color(0xFFFBE89E), // Yellow
        Color(0xFFFACDD4)  // Pink
    )
    
    val orderItems = listOf(
        "Order ID" to order.order_id,
        "User ID" to order.user_id,
        "Product ID" to order.product_id,
        "Product Name" to order.product_name,
        "Quantity" to order.quantity.toString(),
        "Price" to "₹${order.price}",
        "Total" to "₹${order.total_amount}",
        "Category" to order.category,
        "Date" to order.date_of_order_creation,
        "Approved" to if (order.isApproved) "Yes" else "No",
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(2) }) {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                bgColor = Color(0xFFD0C8FF)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "User Name".capitalizeEachWord(),
                        fontWeight = FontWeight.Bold,
                        color = ClayTextPrimary,
                        fontSize = 17.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = order.user_name.capitalizeEachWord(),
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = ClayTextSecondary
                    )
                }
            }
        }
        
        itemsIndexed(orderItems) { index, item ->
            val bgColor = cardColors[index % cardColors.size]
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                bgColor = bgColor
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = item.first.capitalizeEachWord(),
                        modifier = Modifier.padding(bottom = 6.dp),
                        fontWeight = FontWeight.Bold,
                        color = ClayTextPrimary,
                        fontSize = 15.sp,
                        maxLines = 1
                    )
                    Text(
                        text = item.second,
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = ClayTextSecondary
                    )
                }
            }
        }
    }
}
