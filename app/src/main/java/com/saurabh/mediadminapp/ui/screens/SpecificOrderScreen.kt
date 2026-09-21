package com.saurabh.mediadminapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.Order
import com.saurabh.mediadminapp.ui.screens.components.ClayErrorScreen
import com.saurabh.mediadminapp.ui.screens.components.ClayGradientBackdrop
import com.saurabh.mediadminapp.ui.screens.components.ClayLoadingScreen
import com.saurabh.mediadminapp.ui.screens.components.ClayStatusBadge
import com.saurabh.mediadminapp.ui.theme.ClayBadgeApproved
import com.saurabh.mediadminapp.ui.theme.ClayBadgePending
import com.saurabh.mediadminapp.ui.theme.ClayBorder
import com.saurabh.mediadminapp.ui.theme.ClayCardBg
import com.saurabh.mediadminapp.ui.theme.ClayOrderGradient
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
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
        Scaffold(containerColor = Color.Transparent) { innerPadding ->
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
                    ClayGradientBackdrop(gradient = ClayOrderGradient) {
                        EachOrderList(
                            order = response.value.success!!.order,
                            navController = navController,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

// Tile accent colors for the detail grid
private val orderTileColors = listOf(
    Color(0xFFD3C4FA), // Lavender
    Color(0xFFACF68F), // Green
    Color(0xFFE9CF87), // Amber
    Color(0xFFFBE89E), // Yellow
    Color(0xFFFACDD4)  // Pink
)

@Composable
fun EachOrderList(order: Order, navController: NavController, modifier: Modifier = Modifier) {
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
        "Approved" to if (order.isApproved) "Yes ✓" else "No ✗",
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Back nav + title header
        item(span = { GridItemSpan(2) }) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Order Details",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // Hero card — user name + approval status
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 24.dp,
                            shape = RoundedCornerShape(28.dp),
                            ambientColor = ClayPrimary.copy(0.25f),
                            spotColor = ClayPrimary.copy(0.3f)
                        )
                        .clip(RoundedCornerShape(28.dp))
                        .background(ClayCardBg)
                        .border(1.5.dp, Color.White, RoundedCornerShape(28.dp))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(ClayPrimary, Color(0xFF48CAE4))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = order.user_name.firstOrNull()?.uppercase() ?: "U",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = order.user_name.capitalizeEachWord(),
                        fontWeight = FontWeight.ExtraBold,
                        color = ClayTextPrimary,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ClayStatusBadge(
                        text = if (order.isApproved) "Approved" else "Pending Approval",
                        color = if (order.isApproved) ClayBadgeApproved else ClayBadgePending
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Detail tiles
        itemsIndexed(orderItems) { index, item ->
            val bgColor = orderTileColors[index % orderTileColors.size]
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = bgColor.copy(alpha = 0.4f),
                        spotColor = bgColor.copy(alpha = 0.4f)
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgColor)
                    .border(1.5.dp, Color.White.copy(0.8f), RoundedCornerShape(20.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.first,
                    modifier = Modifier.padding(bottom = 6.dp),
                    fontWeight = FontWeight.Bold,
                    color = ClayTextPrimary,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Text(
                    text = item.second,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = ClayTextSecondary,
                    maxLines = 2
                )
            }
        }
    }
}
