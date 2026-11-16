package com.saurabh.mediadminapp.ui.screens

import com.saurabh.mediadminapp.ui.components.GsTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.data.v1.core.UiState
import com.saurabh.mediadminapp.data.v1.error.AppError
import com.saurabh.mediadminapp.ui.components.AppScreenScaffold
import com.saurabh.mediadminapp.ui.components.EmptyState
import com.saurabh.mediadminapp.ui.components.ErrorState
import com.saurabh.mediadminapp.ui.components.FormScaffold
import com.saurabh.mediadminapp.ui.components.SaveBar
import com.saurabh.mediadminapp.ui.components.SectionHeader
import com.saurabh.mediadminapp.ui.components.SkeletonList
import com.saurabh.mediadminapp.ui.components.showDate

private val QUICK_REASONS = listOf("Stock count correction", "Damaged", "Lost", "Found extra")

/** Add or remove units on one batch with a reason. Never lets a batch go below zero. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockAdjustScreen(productId: String, viewModel: MyViewModel, navController: NavController) {
    val batches by viewModel.productBatches.collectAsStateWithLifecycle()
    val stock by viewModel.stockPurchaseState.collectAsStateWithLifecycle()
    var batchId by rememberSaveable { mutableStateOf<String?>(null) }
    var adding by rememberSaveable { mutableStateOf(true) }
    var amount by rememberSaveable { mutableStateOf("") }
    var reason by rememberSaveable { mutableStateOf("") }
    var errors by rememberSaveable { mutableStateOf<Map<String, String>>(emptyMap()) }
    var submitted by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(productId) { viewModel.clearStockPurchaseState(); viewModel.loadProductBatches(productId, force = true) }
    DisposableEffect(Unit) { onDispose { viewModel.clearStockPurchaseState() } }
    // Preselected from BatchDetail.
    LaunchedEffect(Unit) {
        navController.currentBackStackEntry?.savedStateHandle?.get<String>(ADJUST_BATCH_KEY)?.let { batchId = it }
    }
    val list = (batches as? UiState.Success)?.data.orEmpty()
    LaunchedEffect(list) {
        if (batchId == null || list.none { it.batchId == batchId }) batchId = list.firstOrNull { it.quantity > 0 }?.batchId ?: list.firstOrNull()?.batchId
    }
    val batch = list.firstOrNull { it.batchId == batchId }
    val n = amount.toIntOrNull()
    val result = if (batch != null && n != null) batch.quantity + if (adding) n else -n else null

    LaunchedEffect(stock.success) { if (submitted && stock.success != null) navController.popBackStack() }
    LaunchedEffect(stock.error) {
        (stock.appError as? AppError.Api)?.fields?.let { f ->
            errors = errors + buildMap { f["change_qty"]?.let { put("amount", it) }; f["reason"]?.let { put("reason", it) }; f["batch_id"]?.let { put("batch", it) } }
        }
    }

    AppScreenScaffold(
        title = "Adjust stock",
        onBack = { navController.navigateUp() },
        bottomBar = {
            SaveBar("Apply adjustment", loading = stock.isLoading, enabled = batch != null, onClick = {
                errors = buildMap {
                    if (n == null || n < 1) put("amount", "Enter a quantity of 1 or more")
                    else if (!adding && batch != null && n > batch.quantity) put("amount", "Only ${batch.quantity} in this batch")
                    if (reason.trim().length < 3) put("reason", "At least 3 characters")
                }
                if (errors.isEmpty() && batch != null && n != null) {
                    submitted = true
                    viewModel.adjustStock(productId, batch.batchId, if (adding) n else -n, reason)
                }
            })
        }
    ) { inner ->
        Box(Modifier.padding(inner)) {
            when (batches) {
                is UiState.Error -> ErrorState((batches as UiState.Error).error.userMessage(), onRetry = { viewModel.loadProductBatches(productId, force = true) })
                is UiState.Empty -> EmptyState("No batches", "Receive stock first, then adjust it here.")
                is UiState.Success -> FormScaffold {
                    var open by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = open, onExpandedChange = { open = it }, modifier = Modifier.fillMaxWidth()) {
                        GsTextField(
                            value = batch?.let { "${it.batchNo} · exp ${it.expiryDate.showDate()} · qty ${it.quantity}" }.orEmpty(),
                            onValueChange = {}, readOnly = true, label = { Text("Batch") }, singleLine = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(open) },
                            isError = errors["batch"] != null, supportingText = errors["batch"]?.let { { Text(it) } },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                            list.forEach { b ->
                                DropdownMenuItem(
                                    text = { Text("${b.batchNo} · exp ${b.expiryDate.showDate()} · qty ${b.quantity}" + if (b.isExpired) " · expired" else "") },
                                    onClick = { batchId = b.batchId; open = false; errors = errors - "amount" }
                                )
                            }
                        }
                    }
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        listOf(true to "Add units", false to "Remove units").forEachIndexed { i, (v, label) ->
                            SegmentedButton(selected = adding == v, onClick = { adding = v; errors = errors - "amount" }, shape = SegmentedButtonDefaults.itemShape(i, 2)) { Text(label, maxLines = 1) }
                        }
                    }
                    GsTextField(
                        value = amount, onValueChange = { amount = it.filter(Char::isDigit).take(7); errors = errors - "amount" },
                        label = { Text("Quantity") }, singleLine = true, enabled = !stock.isLoading,
                        isError = errors["amount"] != null,
                        supportingText = { Text(errors["amount"] ?: result?.let { "Batch will have $it units" } ?: "Whole number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth()
                    )
                    SectionHeader("Reason")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(QUICK_REASONS, key = { it }) { r -> AssistChip(onClick = { reason = r; errors = errors - "reason" }, label = { Text(r) }) }
                    }
                    GsTextField(
                        value = reason, onValueChange = { reason = it; errors = errors - "reason" },
                        label = { Text("Reason for adjustment") }, singleLine = true, enabled = !stock.isLoading,
                        isError = errors["reason"] != null, supportingText = errors["reason"]?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), modifier = Modifier.fillMaxWidth()
                    )
                    stock.error.takeIf { errors.isEmpty() }?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
                }
                else -> SkeletonList(rows = 4)
            }
        }
    }
}
