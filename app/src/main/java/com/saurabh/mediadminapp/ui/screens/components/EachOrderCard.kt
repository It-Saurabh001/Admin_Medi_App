package com.saurabh.mediadminapp.ui.screens.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.network.response.Order
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import com.saurabh.mediadminapp.utils.ScreensState.ApproveOrderState
import com.saurabh.mediadminapp.utils.utilityFunctions.capitalizeEachWord


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
                        onClick = { navController.navigate(Routes.SpecificOrderRoutes.invoke(order.order_id))  },
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