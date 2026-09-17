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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.network.response.Order
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import com.saurabh.mediadminapp.utils.ScreensState.ApproveOrderState
import com.saurabh.mediadminapp.utils.utilityFunctions.capitalizeEachWord
import com.saurabh.mediadminapp.ui.theme.ClayBadgeApproved
import com.saurabh.mediadminapp.ui.theme.ClayBadgePending
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary

@Composable
fun EachOrderCard(
    order: Order,
    navController: NavController,
    bgColor: Color,
    modifier: Modifier = Modifier,
    isApproveOrder: State<Map<String, ApproveOrderState>>,
    onApprovalToggle: (String, Boolean) -> Unit
) {
    val scrollState = rememberScrollState()
    var isApproved by remember(order.order_id) {
        mutableStateOf(order.isApproved)
    }
    val currentOrder = isApproveOrder.value[order.order_id]
    var pendingToggle by rememberSaveable(order.order_id) {
        mutableStateOf(false)
    }

    LaunchedEffect(currentOrder?.success) {
        if (currentOrder?.success != null && pendingToggle) {
            pendingToggle = false
        }
    }
    LaunchedEffect(currentOrder?.error) {
        if (currentOrder?.error != null && pendingToggle) {
            pendingToggle = false
        }
    }
    LaunchedEffect(order.isApproved) {
        if (!pendingToggle) {
            isApproved = order.isApproved
        }
    }
    val isLoading = currentOrder?.isLoading == true

    ClayCard(
        modifier = modifier.fillMaxWidth(),
        bgColor = bgColor
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.order_id,
                    fontWeight = FontWeight.Bold,
                    color = ClayTextPrimary,
                    fontSize = 16.sp
                )
                // Status Badge & Switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    ClayStatusBadge(
                        text = if (order.isApproved) "Approved" else "Pending",
                        color = if (order.isApproved) ClayBadgeApproved else ClayBadgePending
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(contentAlignment = Alignment.Center) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(8.dp),
                                strokeWidth = 2.dp,
                                color = ClayPrimary
                            )
                        } else {
                            Switch(
                                checked = isApproved,
                                onCheckedChange = { isChecked ->
                                    if (isChecked != isApproved) {
                                        pendingToggle = true
                                        isApproved = isChecked
                                        onApprovalToggle(order.order_id, isChecked)
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = ClayBadgeApproved,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.LightGray
                                ),
                                enabled = !isLoading
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.horizontalScroll(scrollState)
                ) {
                    Text(
                        text = order.product_name.capitalizeEachWord(),
                        fontSize = 15.sp,
                        color = ClayTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "By: ${order.user_name.capitalizeEachWord()}",
                        fontSize = 13.sp,
                        color = ClayTextSecondary
                    )
                }
                Box(
                    modifier = Modifier.wrapContentSize(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    ClayOutlinedButton(
                        text = "Details",
                        onClick = { navController.navigate(Routes.SpecificOrderRoutes.invoke(order.order_id)) },
                        leadingIcon = Icons.Default.Add,
                        accentColor = Color(0xFF7089F0) // matching original color
                    )
                }
            }
            if (order.message.isNotEmpty()) {
                Text(
                    text = order.message,
                    fontSize = 13.sp,
                    color = ClayTextSecondary,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(10.dp),
                    maxLines = 2
                )
            }
        }
    }
}