package com.saurabh.mediadminapp.ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.network.response.Order
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import com.saurabh.mediadminapp.ui.theme.ClayBadgeApproved
import com.saurabh.mediadminapp.ui.theme.ClayBadgePending
import com.saurabh.mediadminapp.ui.theme.ClayFieldBg
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary
import com.saurabh.mediadminapp.utils.ScreensState.ApproveOrderState
import com.saurabh.mediadminapp.utils.utilityFunctions.capitalizeEachWord
import java.text.NumberFormat
import java.util.Locale

/**
 * Two-Tier Scannable Order Overview Card:
 * Displays scannable identity (Order ID, Product Name, Customer),
 * status badge, interactive dispatch approval switch, split price deck,
 * and high-contrast spring-reactive "Details" button.
 */
@Composable
fun EachOrderCard(
    order: Order,
    navController: NavController,
    bgColor: Color,
    modifier: Modifier = Modifier,
    isApproveOrder: State<Map<String, ApproveOrderState>>,
    onApprovalToggle: (String, Boolean) -> Unit
) {
    val isApproved = order.isApproved
    val currentOrder = isApproveOrder.value[order.order_id]
    val isLoading = currentOrder?.isLoading == true
    val statusColor = if (isApproved) ClayBadgeApproved else ClayBadgePending
    val statusText = if (isApproved) "Approved" else "Pending"

    ClayCardOrderScreen(
        modifier = modifier.fillMaxSize().aspectRatio(0.9f),
        cornerRadius = 24.dp,
        bgColor = bgColor
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Header: Product Name  ───────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                Text(
                    text = order.product_name.capitalizeEachWord(),
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ClayTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            // icon + order ID
            Row( modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top)
            {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(ClayFieldBg)
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = ClayPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                    Text(
                        text = "${order.order_id} ",
                        fontWeight = FontWeight.ExtraBold,
                        color = ClayTextPrimary,
                        maxLines = 2,
                        fontSize = 15.sp,
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .height(40.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            )
            // ── Footer: Split Revenue Deck + Prominent Spring "Details" Button ──

            {

                ClayStatusBadgeOrderDetail(
                    text = statusText,
                    color = statusColor
                )
                Spacer(modifier= Modifier.height(4.dp))

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(15.dp),
                        strokeWidth = 2.dp,
                        color = ClayPrimary
                    )
                } else {
                    Switch(
                        checked = isApproved,
                        onCheckedChange = { onApprovalToggle(order.order_id, it) },
                        modifier= Modifier.scale(0.78f),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ClayBadgeApproved,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFD0C8FF)
                        )
                    )
                }


            }
            ClayDetailButtonOrderScreen(
                text = "Details",
                onClick = {
                    navController.navigate(Routes.SpecificOrderRoutes.invoke(order.order_id))
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        }
    }
