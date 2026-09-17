package com.saurabh.mediadminapp.ui.screens.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saurabh.mediadminapp.network.response.SellHistory
import com.saurabh.mediadminapp.ui.theme.ClayBadgeApproved
import com.saurabh.mediadminapp.ui.theme.ClayBadgePending
import com.saurabh.mediadminapp.ui.theme.ClayError
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SalesCard(
    sale: SellHistory,
    bgColor: Color,
    onView: () -> Unit = {},
    onAnalyze: () -> Unit = {}
) {
    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        bgColor = bgColor
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sale.Sell_id,
                    fontWeight = FontWeight.Bold,
                    color = ClayTextPrimary,
                    fontSize = 16.sp
                )

                // Status Badge
                ClayStatusBadge(
                    text = if (sale.isApproved) "Completed" else "Pending",
                    color = if (sale.isApproved) ClayBadgeApproved else ClayBadgePending
                )
            }

            // Product Info
            Text(
                text = sale.product_name,
                fontSize = 15.sp,
                color = ClayTextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            ClayInfoRow(label = "Sold to:", value = sale.user_name)

            // Order and Quantity Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Order: ${sale.Order_id}",
                    fontSize = 13.sp,
                    color = ClayTextSecondary
                )
                Text(
                    text = "Qty: ${sale.quantity}",
                    fontSize = 13.sp,
                    color = ClayTextSecondary
                )
            }

            // Amount and Stock Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹${NumberFormat.getInstance(Locale.Builder().setLanguage("en").setRegion("IN").build()).format(sale.total_amount)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ClayPrimary
                )

                // Stock Status
                Text(
                    text = if (sale.remaining_stock == 0) "Out of Stock" else "Stock: ${sale.remaining_stock}",
                    fontSize = 13.sp,
                    color = if (sale.remaining_stock == 0) ClayError else ClayTextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Date
            Text(
                text = "Sold on: ${sale.date_of_sell}",
                fontSize = 12.sp,
                color = ClayTextSecondary
            )

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ClayOutlinedButton(
                    text = "View",
                    onClick = onView,
                    leadingIcon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )

                ClayPrimaryButton(
                    text = "Analyze",
                    onClick = onAnalyze,
                    modifier = Modifier.weight(1f),
                    gradient = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF7C3AED), Color(0xFF6C63FF))
                    )
                )
            }
        }
    }
}