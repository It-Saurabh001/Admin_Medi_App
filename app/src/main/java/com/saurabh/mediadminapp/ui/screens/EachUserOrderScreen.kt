package com.saurabh.mediadminapp.ui.screens

import android.R.attr.checked
import android.util.Log
import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.Order
import com.saurabh.mediadminapp.ui.screens.nav.SpecificOrderRoutes
import com.saurabh.mediadminapp.utils.ScreensState.UpdateOrderState
import com.saurabh.mediadminapp.utils.utilityFunctions.DismissKeyboardOnTapScreen

@Composable
fun EachUserOrderScreen(userId: String,viewModel: MyViewModel,navController: NavController) {
    BackHandler {
        viewModel.clearGetUsersOrdersState()
        navController.popBackStack()
    }
    val response = viewModel.getUsersOrdersState.collectAsState()
    val order = response.value.success?.order
    val updateOrderState = viewModel.updateOrderState.collectAsState()
    LaunchedEffect(userId) {
        viewModel.getUsersOrders(userId)
    }

    DismissKeyboardOnTapScreen {
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

                    if (order != null){
                        UserOrderListScreen(
                            order,
                            navController,
                            updateOrderState,
                            viewModel::updateOrder
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



}

@Composable
fun UserOrderListScreen(orders: List<Order>, navController: NavController, updateOrderState: State<Map<String, UpdateOrderState>>,onApprovalToggle : (String, Boolean)-> Unit) {

    Column(
        // it shows all products in list
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Name: "+ orders.first().user_name,
            style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items (orders){orderItem->
                EachUserOrderCard(orderItem,  navController, updateOrderState = updateOrderState,onApprovalToggle=onApprovalToggle,)
            }
        }

    }
}


@Composable
fun EachUserOrderCard(order: Order,navController: NavController,updateOrderState: State<Map<String, UpdateOrderState>>,onApprovalToggle : (String, Boolean)-> Unit
) {

    var isApproved by rememberSaveable (order.order_id){
        mutableStateOf(order.isApproved == true) }
    val currentOrder = updateOrderState.value[order.order_id]
    var  pendingToggle by rememberSaveable(order.order_id) {
        mutableStateOf(false)
    }
    LaunchedEffect(order.isApproved) {
        isApproved = order.isApproved == true
        Log.d("TAG", "EachUserOrderCard: isapproved launcheffect ${order.isApproved}")
    }
    LaunchedEffect(currentOrder?.success) {
        if (currentOrder?.success != null && pendingToggle) {
            isApproved = !isApproved
            pendingToggle = false
            Log.d("TAG", "EachUserOrderCard: current order state ${currentOrder.success.message} & ${currentOrder.success.status}")
            Log.d("TAG", "EachUserOrderCard: launcheffect  $isApproved")

        }
    }
    LaunchedEffect(currentOrder?.error) {
        if (currentOrder?.error != null && pendingToggle) {
//            isApproved.value = !isApproved.value
            pendingToggle = false
        }
    }

    val isLoading = currentOrder?.isLoading == true

    ElevatedCard (modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)
        .clickable(onClick = { navController.navigate(SpecificOrderRoutes.invoke(order.order_id)) }),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)){
        Row (modifier = Modifier.fillMaxWidth()){
            Column (modifier = Modifier.fillMaxWidth(0.5f)){
                HorizontalScrollableText("Id: \n"+order.order_id, style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(13.dp)
                )
                Box(modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center){
                    Switch(             // switch is use as toggle button
                        checked = isApproved ,
                        onCheckedChange = {

                            if (!isLoading){
                                pendingToggle = true
//                                viewModel.updateOrder(order.order_id, isApproved=it)
                                onApprovalToggle(order.order_id, it)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFFFA500), // Orange
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.LightGray
                        ),
                        enabled = !isLoading
                    )
                }

            }
            Column(modifier = Modifier.fillMaxWidth()) {
                HorizontalScrollableText(
                    "ProductId: \n "+order.product_id, style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(13.dp)
                )
                HorizontalScrollableText(
                    "Product Name: \n "+order.product_name, style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(13.dp)
                )
            }
        }
    }
}

