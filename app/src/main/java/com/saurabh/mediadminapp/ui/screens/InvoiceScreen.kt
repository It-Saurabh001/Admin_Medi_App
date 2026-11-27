package com.saurabh.mediadminapp.ui.screens

import com.saurabh.mediadminapp.ui.components.GsPullToRefreshBox
import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.data.v1.core.UiState
import com.saurabh.mediadminapp.data.v1.dto.Invoice
import com.saurabh.mediadminapp.data.v1.dto.InvoiceItem
import com.saurabh.mediadminapp.ui.adaptive.AdaptiveContent
import com.saurabh.mediadminapp.ui.adaptive.AdaptiveWidths
import com.saurabh.mediadminapp.ui.components.AppScreenScaffold
import com.saurabh.mediadminapp.ui.components.DetailCard
import com.saurabh.mediadminapp.ui.components.ErrorState
import com.saurabh.mediadminapp.ui.components.InfoTile
import com.saurabh.mediadminapp.ui.components.KeyValueRow
import com.saurabh.mediadminapp.ui.components.LoadingState
import com.saurabh.mediadminapp.ui.components.ShadeButton
import com.saurabh.mediadminapp.ui.components.StatusChip
import com.saurabh.mediadminapp.ui.components.TileIconButton
import com.saurabh.mediadminapp.ui.components.Tone
import com.saurabh.mediadminapp.ui.components.ToneChip
import com.saurabh.mediadminapp.ui.components.dateTimeLabel
import com.saurabh.mediadminapp.ui.components.showDate
import com.saurabh.mediadminapp.utils.utilityFunctions.capitalizeEachWord
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Invoice for a sold / returned order (GET orders/{id}/invoice).
 * Header (number, issued date, status) -> Bill to -> line items (batch pill, expiry, qty x price, GST) -> totals.
 * One primary action: Share (ACTION_SEND plain text).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(orderId: String, viewModel: MyViewModel, navController: NavController, checkoutId: String? = null) {
    val state by viewModel.invoiceState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val reload: () -> Unit = { if (checkoutId != null) viewModel.getCheckoutInvoice(checkoutId) else viewModel.getInvoice(orderId) }
    LaunchedEffect(orderId, checkoutId) { reload() }

    // invoiceState is shared and never cleared: only show an invoice that belongs to this order / checkout.
    val invoice = (state as? UiState.Success)?.data?.takeIf {
        if (checkoutId != null) it.checkoutId == checkoutId else it.order.orderId == orderId && it.checkoutId == null
    }
    val money = rememberInvoiceMoney(invoice?.currency)

    val share: () -> Unit = {
        invoice?.let { inv ->
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Invoice ${inv.invoiceNumber}")
                putExtra(Intent.EXTRA_TEXT, inv.asShareText(money))
            }
            try {
                context.startActivity(Intent.createChooser(send, "Share invoice"))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(context, "No app available to share the invoice", Toast.LENGTH_LONG).show()
            }
        }
    }

    AppScreenScaffold(
        title = invoice?.let { "Invoice ${it.invoiceNumber}" } ?: "Invoice",
        onBack = { navController.popBackStack() },
        actions = { if (invoice != null) TileIconButton(Icons.Outlined.Share, "Share invoice", share) },
        bottomBar = {
            if (invoice != null) Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                ShadeButton(
                    text = "Share invoice", onClick = share, icon = Icons.Outlined.Share,
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        },
    ) { inner ->
        Box(Modifier.padding(inner).fillMaxSize()) {
            val s = state
            when {
                invoice != null -> GsPullToRefreshBox(
                    isRefreshing = s is UiState.Loading,
                    onRefresh = reload,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    AdaptiveContent(maxWidth = AdaptiveWidths.Form) { InvoiceBody(invoice, money) }
                }
                s is UiState.Error -> ErrorState(
                    message = s.error.userMessage().ifBlank { "Invoice is available only for sold or returned orders." },
                    onRetry = reload,
                )
                else -> LoadingState()
            }
        }
    }
}

@Composable
private fun InvoiceBody(inv: Invoice, money: NumberFormat) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
    ) {
        item(key = "header") {
            DetailCard(title = "Invoice", icon = Icons.AutoMirrored.Outlined.ReceiptLong) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(inv.invoiceNumber, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            inv.checkoutId?.let { "Checkout $it · ${inv.orders?.size ?: 1} orders" } ?: "Order ${inv.order.orderId}", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        )
                    }
                    StatusChip(inv.order.status)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoTile("Issued", inv.issuedAt.dateTimeLabel(), Modifier.weight(1f))
                    InfoTile("Currency", inv.currency.ifBlank { "INR" }, Modifier.weight(1f))
                }
            }
        }

        item(key = "billTo") {
            val u = inv.user
            DetailCard(title = "Bill to", icon = Icons.Outlined.Person) {
                Text(u.name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                listOfNotNull(
                    u.email.takeIf { it.isNotBlank() },
                    u.phoneNumber?.takeIf { it.isNotBlank() },
                    listOfNotNull(u.address?.takeIf { it.isNotBlank() }, u.pinCode?.takeIf { it.isNotBlank() })
                        .joinToString(" - ").takeIf { it.isNotBlank() },
                ).forEach {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
                if (inv.order.patientName.isNotBlank() || inv.order.doctorName.isNotBlank()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (inv.order.patientName.isNotBlank()) InfoTile("Patient", inv.order.patientName, Modifier.weight(1f))
                        if (inv.order.doctorName.isNotBlank()) InfoTile("Doctor", inv.order.doctorName, Modifier.weight(1f))
                    }
                }
            }
        }

        item(key = "itemsHeader") {
            Text(
                "Items (${inv.items.size})", style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(top = 4.dp),
            )
        }
        items(inv.items, key = { "${it.productId}_${it.batchId}" }, contentType = { "line" }) { line ->
            InvoiceLine(line, money)
        }

        item(key = "totals") {
            DetailCard(title = "Totals") {
                KeyValueRow("Subtotal (taxable)", money.format(inv.subtotal))
                KeyValueRow("GST", money.format(inv.gstTotal))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                KeyValueRow("Grand total", money.format(inv.grandTotal), emphasize = true)
                Text(
                    if (inv.pricesIncludeGst) "Prices include GST." else "GST added to prices.",
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                inv.generatedAt?.let {
                    Text("Generated ${it.dateTimeLabel()}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

/** One invoice line: name, Teal batch pill + expiry, qty x unit price, GST, line total. Not tappable. */
@Composable
private fun InvoiceLine(line: InvoiceItem, money: NumberFormat) {
    DetailCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    line.productName.capitalizeEachWord(), style = MaterialTheme.typography.titleMedium,
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    ToneChip("Batch ${line.batchNo}", Tone.Info)
                    line.expiryDate?.let {
                        Text("Exp ${it.showDate()}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                }
                Text(
                    "${line.quantity} x ${money.format(line.unitPrice)} - GST ${line.gstPercent}% (${money.format(line.gstAmount)})",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                money.format(line.lineTotal), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface, maxLines = 1, modifier = Modifier.padding(start = 12.dp),
            )
        }
    }
}

@Composable
private fun rememberInvoiceMoney(currency: String?): NumberFormat = remember(currency) {
    NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("en").setRegion("IN").build()).apply {
        maximumFractionDigits = 2
        runCatching { if (!currency.isNullOrBlank()) this.currency = Currency.getInstance(currency) }
    }
}

private fun Invoice.asShareText(money: NumberFormat): String = buildString {
    appendLine("Invoice $invoiceNumber")
    appendLine("Issued: ${issuedAt.dateTimeLabel()}")
    appendLine("Order: ${order.orderId}")
    appendLine("Bill to: ${user.name}${if (user.phoneNumber.isNullOrBlank()) "" else " (${user.phoneNumber})"}")
    appendLine()
    items.forEach { l ->
        appendLine("${l.productName} [${l.batchNo}]  ${l.quantity} x ${money.format(l.unitPrice)} = ${money.format(l.lineTotal)}")
    }
    appendLine()
    appendLine("Subtotal: ${money.format(subtotal)}")
    appendLine("GST: ${money.format(gstTotal)}")
    append("Grand total: ${money.format(grandTotal)}")
}
