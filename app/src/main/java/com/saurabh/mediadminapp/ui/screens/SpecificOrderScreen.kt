package com.saurabh.mediadminapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.Order
import com.saurabh.mediadminapp.utils.ScreensState.ApproveOrderState
import com.saurabh.mediadminapp.utils.utilityFunctions.DismissKeyboardOnTapScreen
import com.saurabh.mediadminapp.utils.utilityFunctions.capitalizeEachWord

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
        Scaffold (modifier = Modifier.background(color = Color.White)){
            when {
                response.value.isLoading -> {
                    Box(
                        modifier = Modifier
                            .background(color = Color.White)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingScreen()
                    }
                }

                response.value.error != null -> {
                    Box(
                        modifier = Modifier
                            .background(color = Color.White)
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
                            .background(color = Color.White)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
//                        EachOrder(response.value.success!!.order, navController)
                        EachOrderList(response.value.success!!.order)
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewEachOrderCard1() {
    val sampleOrder = Order(
        id = 1,
        order_id = "ORD001",
        user_id = "59e359d8-4ae3-4134-b0f6-c5e5d59eefb0",
        product_id = "PRD001",
        product_name = "Paracetamol 500mg",
        user_name = "Dr. Rajesh Kumar",
        quantity = 100,
        price = 25.50,
        total_amount = 2550.00,
        category = "Tablet",
        date_of_order_creation = "2024-01-28",
        _isApproved = false,
        message = "Urgent requirement for hospital ward Urgent requirement for hospital ward Urgent requirement for hospital ward Urgent requirement for hospital ward Urgent requirement for hospital ward Urgent requirement for hospital ward Urgent requirement for hospital ward Urgent requirement for hospital ward Urgent requirement for hospital ward Urgent requirement for hospital ward Urgent requirement for hospital ward Urgent requirement for hospital ward Urgent requirement for hospital ward "

    )

    val dummyApprovalState = remember {
        mutableStateOf(
            mapOf(sampleOrder.order_id to ApproveOrderState(  success= null,
                isLoading = false,
                error = null))
        )
    }
    EachOrderList(sampleOrder)
}

@Composable
fun EachOrderCard5(name: String, order: String, bgColor: Color, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    ElevatedCard (modifier = modifier
        .fillMaxWidth()
        .border(
            BorderStroke(2.dp, Color(0xFFE0a7F6)), // 👈 Change color here
            shape = RoundedCornerShape(8.dp)
        )
        .shadow(2.dp, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor =  bgColor),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)


    ){
        Column (modifier = modifier
            .fillMaxWidth()
            .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            ){
            Text(text = name.capitalizeEachWord(),
                modifier = Modifier.padding(5.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp)
            Text(text = order,
                modifier = Modifier.verticalScroll(scrollState),
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,

                )
        }


    }
    
}


@Composable
fun EachOrderList(order: Order) {
    val cardColors = listOf(
        Color(0xFFD3C4FA), // Red
        Color(0xFFACF68F), // Green
        Color(0xFFE9CF87), // Blue
        Color(0xFFFBE89E), // Yellow
        Color(0xFFFACDD4)  // Purple
    )
    val orderItems = listOf(
        "Order ID" to order.order_id,
        "User ID" to order.user_id,
        "Product ID" to order.product_id,
        "Product Name" to order.product_name,
//        "User Name" to order.user_name,
        "Quantity" to order.quantity.toString(),
        "Price" to "₹${order.price}",
        "Total" to "₹${order.total_amount}",
        "Category" to order.category,
        "Date" to order.date_of_order_creation,
        "Approved" to if (order._isApproved == true) "Yes" else "No",

    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(3.dp),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(span = { GridItemSpan(2) }) {
            Box(modifier = Modifier.fillMaxWidth()){
                ElevatedCard (modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(2.dp, Color(0xFFE0a7F6)), // 👈 Change color here
                        shape = RoundedCornerShape(8.dp)
                    )
                    .shadow(2.dp, RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor =  Color(0xFFE0a7F6)),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)


                ){
                    Column (modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                        ,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ){
                        Text(text = "User Name".capitalizeEachWord(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp)
                        Text(text = order.user_name.toString().capitalizeEachWord(),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }


                }
            }
        }
        itemsIndexed(orderItems) { index, item ->
            val bgColor = cardColors[index % cardColors.size] // Linear rotation
            EachOrderCard5(
                name = item.first,
                order = item.second.capitalizeEachWord(),
                bgColor = bgColor,
                modifier= Modifier.padding(top = 0.dp, bottom = 0.dp).height(80.dp)
            )
        }
        if (order.message.isNotEmpty()){
            item (span = { GridItemSpan(2)}){

                EachOrderCard5(
                    name = "Message",
                    order = order.message,
                    bgColor = Color(0xFFFACDD4),
                    modifier = Modifier.height(150.dp)
                )
            }
        }
    }
}
