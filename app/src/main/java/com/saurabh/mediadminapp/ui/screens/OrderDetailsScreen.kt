package com.saurabh.mediadminapp.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.Order
import com.saurabh.mediadminapp.ui.screens.nav.EachUserOrderRoutes
import com.saurabh.mediadminapp.ui.screens.nav.SpecificOrderRoutes
import com.saurabh.mediadminapp.ui.screens.nav.StatsCard
import com.saurabh.mediadminapp.utils.ScreensState.ApproveOrderState
import com.saurabh.mediadminapp.utils.utilityFunctions.capitalizeEachWord
import java.text.NumberFormat
import java.util.Locale

@Composable
fun OrderDetailsScreen(viewModel: MyViewModel, navController: NavController){
    val response = viewModel.getAllOrderState.collectAsState()
    val isApproveOrder = viewModel.isApproveOrdder.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.getAllOrders()
    }
    LaunchedEffect(response.value.success) {
        response.value.success?.let {
            Log.d("TAG", "OrderDetailsScreen: ${it.message}")
            viewModel.clearGetAllProductState()
            Log.d("TAG", "OrderDetailsScreen: ab recomposition ke baad successfull mai jana chiye")
        }
    }
    Scaffold (modifier = Modifier.background(Color(0xFFffffff))
    ){
        innerpadding ->

        when{
            response.value.isLoading ->{
                Box(
                    modifier = Modifier
                        .padding(innerpadding)
                        .fillMaxSize()

                ) {
                    LoadingScreen(modifier = Modifier)
                    Log.d("TAG", "OrderDetailsScreen: ab recomposition ke baad loading mai aya")

                }
            }
            response.value.error != null ->{
                Box(
                    modifier = Modifier
                        .padding(innerpadding)
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
                        .background(Color(0xFFffffff)),

                ) {
                    Log.d("TAG", "OrderDetailsScreen: ${response.value.success!!.orders}")
                    Log.d("TAG", "OrderDetailsScreen: ab recomposition ke baad successfull mai aya")


                    OrdersListScreen(
                        response.value.success!!.orders,
                        navController,
                        modifier = Modifier,
                        isApproveOrder = isApproveOrder,
                        onApprovalToggle = viewModel::isApproveOrder,
                        viewModel = viewModel
                    )
                }
            }
        }



    }
}

@Composable
fun OrdersListScreen(
    orders: List<Order>,
    navController: NavController,
    modifier: Modifier,
    isApproveOrder: State<Map<String, ApproveOrderState>>,
    onApprovalToggle: (String, Boolean) -> Unit,
    viewModel: MyViewModel
) {
    var searchTerm by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("all") }
    val isApproveOrder = viewModel.isApproveOrdder.collectAsState()

    val filteredOrders = remember(orders, searchTerm, filterStatus) {
        orders.filter { order ->
            val matchesSearch = order.product_name.contains(searchTerm, ignoreCase = true) ||
                    order.user_name.contains(searchTerm, ignoreCase = true) ||
                    order.order_id.contains(searchTerm, ignoreCase = true)

            val matchesFilter = when (filterStatus) {
                "all" -> true
                "pending" -> !order.isApproved
                "approved" -> order.isApproved
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }
    val cardColors = listOf(
        Color(0xFFD3C4FA), // Red
        Color(0xFFACF68F), // Green
        Color(0xFFE9CF87), // Blue
        Color(0xFFFBE89E), // Yellow
        Color(0xFFFACDD4)  // Purple
    )
    // Calculate stats
    val stats = remember(orders) {
        mapOf(
            "total" to orders.size,
            "pending" to orders.count { !it.isApproved },
            "approved" to orders.count { it.isApproved },
            "totalValue" to orders.sumOf { it.total_amount }
        )
    }
    // Medical gradient colors
    val medicalGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF0EA5E9), // sky-500
            Color(0xFF3B82F6)  // blue-500
        )
    )
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchTerm,
                onValueChange = { searchTerm = it },
                placeholder = { Text("Search orders...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total Orders
                StatsCard(
                    modifier = Modifier.weight(1f),
                    value = stats["total"].toString(),
                    label = "Total Orders",
                    valueColor = MaterialTheme.colorScheme.onSurface
                )

                // Pending
                StatsCard(
                    modifier = Modifier.weight(1f),
                    value = stats["pending"].toString(),
                    label = "Pending",
                    valueColor = Color(0xFFD97706) // yellow-600
                )

                // Approved
                StatsCard(
                    modifier = Modifier.weight(1f),
                    value = stats["approved"].toString(),
                    label = "Approved",
                    valueColor = MaterialTheme.colorScheme.primary
                )

                // Total Value
                StatsCard(
                    modifier = Modifier.weight(1f),
                    value = "₹${
                        NumberFormat.getInstance(Locale("en", "IN")).format(stats["totalValue"])
                    }",
                    label = "Total Value",
                    valueColor = MaterialTheme.colorScheme.secondary,
                    valueSize = 24.sp
                )
            }
        }
        // search bar

        // status filter
        item {
            val filterOptions = listOf(
                FilterOption("all", "All Orders", Icons.Default.List),
                FilterOption("pending", "Pending", Icons.Default.CheckCircle),
                FilterOption("approved", "Approved", Icons.Default.CheckCircle)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(filterOptions) { filter ->
                    FilterButton(
                        option = filter,
                        isSelected = filterStatus == filter.key,
                        onClick = { filterStatus = filter.key },
                        medicalGradient = medicalGradient
                    )
                }
            }
        }
        if (filteredOrders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No orders found",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }else{
            itemsIndexed (filteredOrders){index,orderItem->
                val bgColor = cardColors[index%cardColors.size]
                EachOrderCard(orderItem,navController,bgColor, modifier = Modifier,isApproveOrder = isApproveOrder,
                    onApprovalToggle = viewModel::isApproveOrder)
            }
        }
    }

}



//@Preview(showSystemUi = true)
@Composable
fun previw(modifier: Modifier = Modifier) {
    val orders = Order(
        id = 1,
        order_id = "ORD001",
        user_id = "USR001",
        product_id = "PRD001",
        product_name = "Paracetamol 500mg",
        user_name = "Dr. Rajesh Kumar",
        quantity = 100,
        price = 25.50,
        total_amount = 2550.00,
        category = "Tablet",
        date_of_order_creation = "2024-01-28",
        _isApproved = false,
        message = "Urgent requirement for hospital ward"

    )

    // 🔹 Dummy approval state banate hain
    val fakeApprovalState = remember {
        mutableStateOf(
            mapOf(
                orders.order_id to ApproveOrderState(
                    success= null,
                    isLoading = false,
                    error = null
                )
            )
        )
    }
    val navController = rememberNavController()
    EachOrderCard(
        orders, navController, Color(0xFFF59EBB), modifier = Modifier,
        isApproveOrder =fakeApprovalState ,
        onApprovalToggle = {} as (String, Boolean) -> Unit,
    )
}
@Preview(showBackground = true)
@Composable
fun PreviewEachOrderCard() {
    val sampleOrder = Order(
        id = 1,
        order_id = "ORD001",
        user_id = "USR001",
        product_id = "PRD001",
        product_name = "Paracetamol 500mg",
        user_name = "Dr. Rajesh Kumar",
        quantity = 100,
        price = 25.50,
        total_amount = 2550.00,
        category = "Tablet",
        date_of_order_creation = "2024-01-28",
        _isApproved = false,
        message = "Urgent requirement for hospital ward"

    )

    val dummyApprovalState = remember {
        mutableStateOf(
            mapOf(sampleOrder.order_id to ApproveOrderState(  success= null,
                isLoading = false,
                error = null))
        )
    }

    EachOrderCard(
        order = sampleOrder,
        navController = rememberNavController(),
        bgColor = Color(0xFFE0F7FA),
        modifier = Modifier.padding(16.dp),
        isApproveOrder = dummyApprovalState,
        onApprovalToggle = { orderId, newState ->
            Log.d("Preview", "Approval toggled for $orderId: $newState")
        }
    )
}


@Composable
fun EachOrderCard(
    order: Order,
    navController: NavController,
    bgColor: Color,
    modifier: Modifier,
    isApproveOrder: State<Map<String, ApproveOrderState>>,
    onApprovalToggle: (String, Boolean) -> Unit
) {
    val scrollState = rememberScrollState()
    var isApproved by remember (order.order_id){
        mutableStateOf(order.isApproved)
    }
    Log.d("TAG", "EachOrderCard: ${order.isApproved}")
    val currentOrder = isApproveOrder.value[order.order_id]
    var  pendingToggle by rememberSaveable(order.order_id) {
        mutableStateOf(false)
    }
    Log.d("TAG", "EachUserOrderCard: isapproved  ${order.isApproved}")

    LaunchedEffect(currentOrder?.success) {
        if (currentOrder?.success != null && pendingToggle) {
            pendingToggle = false
            Log.d("TAG", "EachUserOrderCard: current order state ${currentOrder.success.message} & ${currentOrder.success.status}")
            Log.d("TAG", "EachUserOrderCard: launcheffect  $isApproved")
        }
    }
    LaunchedEffect(currentOrder?.error) {
        if (currentOrder?.error != null && pendingToggle) {
            pendingToggle = false
        }
    }
    // Keep local state in sync with order data when it changes from parent
    LaunchedEffect(order.isApproved) {
        if (!pendingToggle) {
            isApproved = order.isApproved
        }
    }
    val isLoading = currentOrder?.isLoading == true
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        )  {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.order_id,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                // Status Badge
                Row (modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center){
                    Surface(
                        color = if (order.isApproved) Color(0xFF10B981) else Color(0xFFF59E0B),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (order.isApproved) "Approved" else "Pending",
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Box(modifier = Modifier,
                        contentAlignment = Alignment.Center) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(8.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Switch(             // switch is use as toggle button
                                checked = isApproved,
                                onCheckedChange = {isCkecked->
                                    if (!isLoading && isCkecked != isApproved) {
                                        pendingToggle = true
                                        isApproved = isCkecked
                                        onApprovalToggle(order.order_id, isCkecked)
                                        Log.d("TAG", "EachOrderCard: ${isCkecked}")
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF10B981), // Orange
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.LightGray
                                ),
                                enabled = !isLoading
                            )
                        }
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column (modifier = Modifier
                    .horizontalScroll(scrollState),
                    ){
                    // Product Info
                    Text(
                        text = order.product_name.capitalizeEachWord(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "By: ${order.user_name.capitalizeEachWord()}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // detail button to move to each user order screen
                Box(modifier = Modifier.wrapContentSize(),
                    contentAlignment = Alignment.CenterStart){
                    OutlinedButton(
                        onClick = { navController.navigate(SpecificOrderRoutes.invoke(order.order_id))  },
                        modifier = Modifier,
                        colors = ButtonDefaults.buttonColors(Color(0xFF7089F0))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,  //Visibility
                            contentDescription = "Details",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Details")
                    }
                }
            }
            // Message (if exists)
            if (order.message.isNotEmpty()) {
                Text(
                    text = order.message,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(4.dp)
                        )
                        .padding(8.dp),
                    maxLines = 2
                )
            }
        }
    }
}