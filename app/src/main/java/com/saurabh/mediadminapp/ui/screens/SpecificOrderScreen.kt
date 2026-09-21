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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.Order
import com.saurabh.mediadminapp.ui.screens.components.ClayCard
import com.saurabh.mediadminapp.ui.screens.components.ClayErrorScreen
import com.saurabh.mediadminapp.ui.screens.components.ClayGradientBackdrop
import com.saurabh.mediadminapp.ui.screens.components.ClayLoadingScreen
import com.saurabh.mediadminapp.ui.screens.components.ClayPrimaryButton
import com.saurabh.mediadminapp.ui.screens.components.ClayStatusBadge
import com.saurabh.mediadminapp.ui.theme.ClayBadgeApproved
import com.saurabh.mediadminapp.ui.theme.ClayBadgePending
import com.saurabh.mediadminapp.ui.theme.ClayBorder
import com.saurabh.mediadminapp.ui.theme.ClayFieldBg
import com.saurabh.mediadminapp.ui.theme.ClayOrderGradient
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
import com.saurabh.mediadminapp.ui.theme.ClaySecondary
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary
import com.saurabh.mediadminapp.utils.utilityFunctions.DismissKeyboardOnTapScreen
import com.saurabh.mediadminapp.utils.utilityFunctions.capitalizeEachWord
import java.text.NumberFormat
import java.util.Locale

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
                        modifier = Modifier.padding(innerPadding),
                        onRetry = { viewModel.getOrderById(orderId) }
                    )
                }
                response.value.success != null -> {
                    ClayGradientBackdrop(gradient = ClayOrderGradient) {
                        SpecificOrderReceiptView(
                            order = response.value.success!!.order,
                            navController = navController,
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Detail Screen for Specific Order:
 * Features a milestone progress track, visual tactile receipt card,
 * metadata specification cluster, and a floating dispatch approval bar.
 */
@Composable
fun SpecificOrderReceiptView(
    order: Order,
    navController: NavController,
    viewModel: MyViewModel,
    modifier: Modifier = Modifier
) {
    val isApproved = order.isApproved
    val statusColor = if (isApproved) ClayBadgeApproved else ClayBadgePending
    val statusText = if (isApproved) "Approved" else "Pending Review"

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 100.dp)
    ) {
        // ── 1. APP BAR & ORDER IDENTITY ──────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Order Receipt",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                ClayStatusBadge(
                    text = order.order_id,
                    color = Color.White
                )
            }
        }

        // ── 2. MILESTONE PROGRESS TRACK (Order Lifecycle) ─────────────────
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Fulfillment Milestones",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ClayTextPrimary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MilestoneStep(
                            label = "Placed",
                            isComplete = true,
                            icon = Icons.Default.Check
                        )
                        MilestoneConnector(active = true)
                        MilestoneStep(
                            label = "Verified",
                            isComplete = true,
                            icon = Icons.Default.Check
                        )
                        MilestoneConnector(active = isApproved)
                        MilestoneStep(
                            label = "Approved",
                            isComplete = isApproved,
                            icon = if (isApproved) Icons.Default.Check else Icons.Default.HourglassTop
                        )
                        MilestoneConnector(active = isApproved)
                        MilestoneStep(
                            label = "Dispatched",
                            isComplete = isApproved,
                            icon = Icons.Default.LocalShipping
                        )
                    }
                }
            }
        }

        // ── 3. VISUAL CLAY RECEIPT CARD ──────────────────────────────────
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 28.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Receipt Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(ClayPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Medication,
                                    contentDescription = null,
                                    tint = ClayPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = order.product_name.capitalizeEachWord(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ClayTextPrimary
                                )
                                Text(
                                    text = "Category: ${order.category.capitalizeEachWord()}",
                                    fontSize = 12.sp,
                                    color = ClayTextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        ClayStatusBadge(
                            text = statusText,
                            color = statusColor
                        )
                    }

                    HorizontalDivider(
                        color = ClayBorder.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // Line Items
                    ReceiptLineRow("Unit Price", "₹${order.price}")
                    ReceiptLineRow("Quantity", "${order.quantity} units")
                    ReceiptLineRow("Processing & Dispatch Fee", "₹0.00 (Free)")

                    HorizontalDivider(
                        color = ClayBorder.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // Grand Total Deck
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL AMOUNT",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ClayTextSecondary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "₹${NumberFormat.getInstance(Locale.Builder().setLanguage("en").setRegion("IN").build()).format(order.total_amount)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = ClayPrimary
                        )
                    }
                }
            }
        }

        // ── 4. CUSTOMER & AUDIT METADATA CLUSTER ─────────────────────────
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Customer & Audit Specifications",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ClayTextPrimary
                    )

                    MetadataClusterRow(Icons.Default.AccountCircle, "Buyer Name", order.user_name)
                    MetadataClusterRow(Icons.Default.Numbers, "Customer ID", order.user_id)
                    MetadataClusterRow(Icons.Default.QrCode, "Product ID", order.product_id)
                    MetadataClusterRow(Icons.Default.CalendarMonth, "Order Date", order.date_of_order_creation)

                    if (order.message.isNotEmpty()) {
                        HorizontalDivider(
                            color = ClayBorder.copy(alpha = 0.4f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Text(
                            text = "Special Dispatch Instructions:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ClayTextSecondary
                        )
                        Text(
                            text = order.message,
                            fontSize = 13.sp,
                            color = ClayTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // ── 5. FLOATING DISPATCH ACTION BAR ──────────────────────────────
        item {
            ClayPrimaryButton(
                text = if (isApproved) "Revoke Approval (Set Pending)" else "Approve Order Dispatch",
                onClick = {
                    viewModel.isApproveOrder(order.order_id, !isApproved)
                    // Refresh current order state
                    viewModel.getOrderById(order.order_id)
                },
                gradient = if (isApproved) {
                    Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
                } else {
                    Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MilestoneStep(label: String, isComplete: Boolean, icon: ImageVector) {
    val tint = if (isComplete) ClayBadgeApproved else ClayBorder
    val bg = if (isComplete) ClayBadgeApproved.copy(alpha = 0.14f) else ClayFieldBg

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(bg)
                .border(1.5.dp, tint, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isComplete) FontWeight.Bold else FontWeight.Medium,
            color = if (isComplete) ClayTextPrimary else ClayTextSecondary
        )
    }
}

@Composable
private fun MilestoneConnector(active: Boolean) {
    Box(
        modifier = Modifier
            .width(28.dp)
            .height(2.dp)
            .background(if (active) ClayBadgeApproved else ClayBorder.copy(alpha = 0.6f))
    )
}

@Composable
private fun ReceiptLineRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = ClayTextSecondary,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = ClayTextPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun MetadataClusterRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ClayPrimary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: ",
            fontSize = 12.sp,
            color = ClayTextSecondary,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = ClayTextPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}
