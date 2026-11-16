package com.saurabh.mediadminapp.ui.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.saurabh.mediadminapp.data.v1.core.OrderStatus
import com.saurabh.mediadminapp.ui.theme.statusColors

/** The only way to render an order status. Non-interactive label chip. */
@Composable
fun StatusChip(status: OrderStatus, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.statusColors.of(status)
    Surface(
        modifier = modifier.heightIn(min = 24.dp),
        shape = MaterialTheme.shapes.small,
        color = colors.container,
        contentColor = colors.content,
    ) {
        Text(
            text = status.label(),
            style = MaterialTheme.typography.labelMedium,
            // U14: Amber/Red labels are bold for contrast (3.6:1 / 4.0:1).
            fontWeight = if (status == OrderStatus.PENDING || status == OrderStatus.CANCELLED) androidx.compose.ui.text.font.FontWeight.Bold else null,
            softWrap = false,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

fun OrderStatus.label(): String = when (this) {
    OrderStatus.PENDING -> "Pending"
    OrderStatus.APPROVED -> "Approved"
    OrderStatus.SOLD -> "Sold"
    OrderStatus.CANCELLED -> "Cancelled"
    OrderStatus.RETURNED -> "Returned"
    OrderStatus.UNKNOWN -> "Unknown"
}
