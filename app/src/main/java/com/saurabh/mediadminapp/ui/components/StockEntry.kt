package com.saurabh.mediadminapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.saurabh.mediadminapp.OpeningStock
import com.saurabh.mediadminapp.data.v1.dto.StockPurchaseRequest
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val EXPIRY_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)

/** Editable stock-receipt form state that survives rotation. */
@Stable
class StockDraft(
    qty: String, batchNo: String, expiry: String, price: String, mrp: String,
    private val onChange: () -> Unit = {},
) {
    var qty by mutableStateOf(qty)
    var batchNo by mutableStateOf(batchNo)
    /** ISO yyyy-MM-dd or "". */
    var expiry by mutableStateOf(expiry)
    var price by mutableStateOf(price)
    var mrp by mutableStateOf(mrp)
    var errors by mutableStateOf<Map<String, String>>(emptyMap())

    val isBlank get() = qty.isBlank() && batchNo.isBlank() && expiry.isBlank() && price.isBlank() && mrp.isBlank()
    val expiryDate: LocalDate? get() = runCatching { LocalDate.parse(expiry) }.getOrNull()

    /** True when valid. Sets [errors] otherwise. */
    fun validate(): Boolean {
        errors = buildMap {
            val q = qty.trim().toIntOrNull()
            if (q == null || q < 1) put("qty", "Enter a quantity of 1 or more")
            if (batchNo.isBlank()) put("batch", "Enter the batch number")
            val d = expiryDate
            if (d == null) put("expiry", "Pick the expiry date") else if (!d.isAfter(LocalDate.now())) put("expiry", "Expiry must be in the future")
            if (price.isNotBlank() && price.toDoubleOrNull() == null) put("price", "Enter a valid price")
            if (mrp.isNotBlank() && mrp.toDoubleOrNull() == null) put("mrp", "Enter a valid MRP")
        }
        return errors.isEmpty()
    }

    fun toOpeningStock(): OpeningStock = OpeningStock(
        quantity = qty.trim().toInt(), batchNo = batchNo.trim(), expiryDate = expiryDate!!,
        purchasePrice = price.toDoubleOrNull(), mrp = mrp.toDoubleOrNull()
    )

    fun toRequest(): StockPurchaseRequest = toOpeningStock().let {
        StockPurchaseRequest(batchNo = it.batchNo, expiryDate = it.expiryDate, quantity = it.quantity, purchasePrice = it.purchasePrice, mrp = it.mrp)
    }

    /** Server field errors (snake_case) merged in. */
    fun applyServerFields(fields: Map<String, String>?) {
        if (fields.isNullOrEmpty()) return
        errors = errors + buildMap {
            fields["quantity"]?.let { put("qty", it) }
            fields["batch_no"]?.let { put("batch", it) }
            fields["expiry_date"]?.let { put("expiry", it) }
            fields["purchase_price"]?.let { put("price", it) }
            fields["mrp"]?.let { put("mrp", it) }
        }
    }
}

@Composable
fun rememberStockDraft(): StockDraft {
    var qty by rememberSaveable { mutableStateOf("") }
    var batch by rememberSaveable { mutableStateOf("") }
    var expiry by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var mrp by rememberSaveable { mutableStateOf("") }
    // Mirror the saveable strings through a StockDraft; writes flow back via the setters below.
    val draft = androidx.compose.runtime.remember { StockDraft(qty, batch, expiry, price, mrp) }
    androidx.compose.runtime.SideEffect { qty = draft.qty; batch = draft.batchNo; expiry = draft.expiry; price = draft.price; mrp = draft.mrp }
    return draft
}

/** Quantity, batch, expiry (date picker), purchase price, MRP. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockEntryFields(draft: StockDraft, enabled: Boolean, lastImeDone: Boolean = true) {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val fm = Modifier.fillMaxWidth()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GsTextField(
            value = draft.qty, onValueChange = { draft.qty = it.filter(Char::isDigit).take(7); draft.errors = draft.errors - "qty" },
            label = { Text("Quantity received") }, singleLine = true, enabled = enabled,
            isError = draft.errors["qty"] != null, supportingText = draft.errors["qty"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next), modifier = fm
        )
        GsTextField(
            value = draft.batchNo, onValueChange = { draft.batchNo = it; draft.errors = draft.errors - "batch" },
            label = { Text("Batch number") }, singleLine = true, enabled = enabled,
            isError = draft.errors["batch"] != null, supportingText = draft.errors["batch"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Ascii, capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Characters, autoCorrectEnabled = false, imeAction = ImeAction.Next), modifier = fm
        )
        GsTextField(
            value = draft.expiryDate?.format(EXPIRY_FMT).orEmpty(), onValueChange = {}, readOnly = true,
            label = { Text("Expiry date") }, singleLine = true, enabled = enabled,
            trailingIcon = { IconButton(onClick = { showPicker = true }, enabled = enabled) { Icon(Icons.Outlined.CalendarMonth, contentDescription = "Pick expiry date") } },
            isError = draft.errors["expiry"] != null, supportingText = draft.errors["expiry"]?.let { { Text(it) } },
            modifier = fm
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GsTextField(
                value = draft.price, onValueChange = { draft.price = it; draft.errors = draft.errors - "price" },
                label = { Text("Purchase price") }, singleLine = true, enabled = enabled,
                isError = draft.errors["price"] != null, supportingText = draft.errors["price"]?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next), modifier = Modifier.weight(1f)
            )
            GsTextField(
                value = draft.mrp, onValueChange = { draft.mrp = it; draft.errors = draft.errors - "mrp" },
                label = { Text("MRP") }, singleLine = true, enabled = enabled,
                isError = draft.errors["mrp"] != null, supportingText = draft.errors["mrp"]?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = if (lastImeDone) ImeAction.Done else ImeAction.Next), modifier = Modifier.weight(1f)
            )
        }
    }
    if (showPicker) {
        val tomorrow = LocalDate.now().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val picker = rememberDatePickerState(
            initialSelectedDateMillis = draft.expiryDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= tomorrow
            }
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    enabled = picker.selectedDateMillis != null,
                    onClick = {
                        picker.selectedDateMillis?.let { draft.expiry = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString(); draft.errors = draft.errors - "expiry" }
                        showPicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = picker) }
    }
}

/** Bottom sheet to receive stock for an existing product (POST stock/purchase). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveStockSheet(
    productName: String,
    isLoading: Boolean,
    error: String?,
    serverFields: Map<String, String>?,
    success: Boolean,
    onSubmit: (StockPurchaseRequest) -> Unit,
    onDismiss: () -> Unit,
) {
    val draft = rememberStockDraft()
    LaunchedEffect(serverFields) { draft.applyServerFields(serverFields) }
    LaunchedEffect(success) { if (success) onDismiss() }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).imePadding().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Add stock", style = MaterialTheme.typography.titleLarge)
            Text(productName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            StockEntryFields(draft, enabled = !isLoading)
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
            ShadeButton(
                text = "Add to stock",
                onClick = { if (draft.validate()) onSubmit(draft.toRequest()) },
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
