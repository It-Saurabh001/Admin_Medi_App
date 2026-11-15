package com.saurabh.mediadminapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.data.v1.core.UiState
import com.saurabh.mediadminapp.data.v1.dto.Batch
import com.saurabh.mediadminapp.data.v1.dto.Product
import com.saurabh.mediadminapp.data.v1.error.AppError
import com.saurabh.mediadminapp.ui.components.AppScreenScaffold
import com.saurabh.mediadminapp.ui.components.ErrorState
import com.saurabh.mediadminapp.ui.components.SectionHeader
import com.saurabh.mediadminapp.ui.components.SkeletonList
import com.saurabh.mediadminapp.ui.components.StockEntryFields
import com.saurabh.mediadminapp.ui.components.rememberStockDraft
import com.saurabh.mediadminapp.utils.utilityFunctions.toImagePart
import java.time.format.DateTimeFormatter
import java.util.Locale

private val BATCH_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)

/** Edit product: every field editable (only changed fields are sent) plus stock correction. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProductScreen(
    productId: String,
    viewModel: MyViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val productState by viewModel.getSpecificProductState.collectAsStateWithLifecycle()
    LaunchedEffect(productId) { viewModel.getSpecificProduct(productId, force = true) }
    val product = productState.success?.takeIf { it.productId == productId }

    if (product == null) {
        AppScreenScaffold(title = "Edit product", onBack = { navController.popBackStack() }) { inner ->
            Box(Modifier.padding(inner).fillMaxSize()) {
                if (productState.error != null) ErrorState(productState.error.orEmpty(), onRetry = { viewModel.getSpecificProduct(productId, force = true) })
                else SkeletonList(rows = 6)
            }
        }
        return
    }
    EditProductForm(product, viewModel, navController)
}

@Composable
private fun EditProductForm(product: Product, viewModel: MyViewModel, navController: NavController) {
    val response by viewModel.updateProductState.collectAsStateWithLifecycle()
    val stock by viewModel.stockPurchaseState.collectAsStateWithLifecycle()
    val batches by viewModel.productBatches.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val enums = remember { viewModel.cachedAppConfig()?.enums }
    val draft = rememberSaveable(product.productId, saver = ProductDraft.Saver) { ProductDraft.from(product) }
    val image = rememberProductImagePicker()
    val newBatchDraft = rememberStockDraft()

    var localError by rememberSaveable { mutableStateOf<String?>(null) }
    var newBatch by rememberSaveable { mutableStateOf(false) }
    var batchId by rememberSaveable { mutableStateOf<String?>(null) }
    var batchQty by rememberSaveable { mutableStateOf("") }
    var reason by rememberSaveable { mutableStateOf("Stock correction") }
    var batchQtyError by rememberSaveable { mutableStateOf<String?>(null) }
    var reasonError by rememberSaveable { mutableStateOf<String?>(null) }
    // 1 while a stock call is in flight; the product PATCH (if any) runs after it succeeds.
    var stage by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(product.productId) { viewModel.loadProductBatches(product.productId, force = true) }
    DisposableEffect(Unit) { onDispose { viewModel.clearStockPurchaseState() } }
    val batchList = (batches as? UiState.Success)?.data.orEmpty()
    val selected = batchList.firstOrNull { it.batchId == batchId }
    // Preselect the first batch that has stock.
    LaunchedEffect(batchList) {
        if (batchId == null || batchList.none { it.batchId == batchId }) {
            batchList.firstOrNull { it.quantity > 0 && !it.isExpired }?.let { batchId = it.batchId; batchQty = it.quantity.toString() }
        }
    }

    fun finishOrUpdate() {
        if (draft.hasChanges(product, image.uri != null)) {
            viewModel.updateProduct(product.productId, draft.toUpdate(product), image.uri?.toImagePart(context))
        } else {
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh_screen", true)
            navController.popBackStack()
        }
    }

    LaunchedEffect(stock.success) {
        if (stage == 1 && stock.success != null) { viewModel.clearStockPurchaseState(); stage = 0; finishOrUpdate() }
    }
    LaunchedEffect(stock.error) {
        if (stage == 1 && stock.error != null) {
            stage = 0
            val f = (stock.appError as? AppError.Api)?.fields
            newBatchDraft.applyServerFields(f)
            f?.get("change_qty")?.let { batchQtyError = it }
            f?.get("reason")?.let { reasonError = it }
        }
    }
    LaunchedEffect(response.success) {
        if (response.success != null) {
            viewModel.clearUpdateProductState()
            viewModel.getSpecificProduct(product.productId, force = true)
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh_screen", true)
            navController.popBackStack()
        }
    }
    LaunchedEffect(response.error) {
        if (response.error != null) draft.applyServerFields((response.appError as? AppError.Api)?.fields)
    }

    val busy = response.isLoading || stock.isLoading || stage == 1
    val fieldErrors = draft.errors.isNotEmpty() || newBatchDraft.errors.isNotEmpty() || batchQtyError != null || reasonError != null
    val formError = localError ?: stock.error.takeIf { !fieldErrors } ?: response.error.takeIf { !fieldErrors }

    ProductFormScreen(
        title = "Edit product", saveText = "Save changes",
        draft = draft, enums = enums, image = image, existingImageUrl = product.imageUrl,
        isLoading = busy, formError = formError,
        onBack = { navController.popBackStack() },
        extra = {
            StockEditSection(
                currentStock = product.stock, unit = product.unit,
                batches = batches, batchList = batchList, selected = selected,
                onSelect = { batchId = it.batchId; batchQty = it.quantity.toString(); batchQtyError = null },
                batchQty = batchQty, onBatchQty = { batchQty = it.filter(Char::isDigit).take(7); batchQtyError = null },
                batchQtyError = batchQtyError,
                reason = reason, onReason = { reason = it; reasonError = null }, reasonError = reasonError,
                newBatch = newBatch || (batches is UiState.Empty), onNewBatch = { newBatch = it },
                canToggleNewBatch = batches !is UiState.Empty,
                newBatchDraft = newBatchDraft, enabled = !busy,
                onRetry = { viewModel.loadProductBatches(product.productId, force = true) }
            )
        },
        onSave = {
            localError = null
            val productOk = draft.validate()
            var stockOk = true
            var op = 0 // 1 adjust, 2 receive new batch
            var delta = 0
            val receiving = newBatch || batches is UiState.Empty
            if (receiving) {
                if (newBatchDraft.isBlank) { /* nothing to receive */ }
                else if (newBatchDraft.validate()) op = 2 else stockOk = false
            } else if (selected != null && batchQty.isNotBlank()) {
                val q = batchQty.toIntOrNull()
                if (q == null || q < 0) { batchQtyError = "Whole number, 0 or more"; stockOk = false }
                else if (q != selected.quantity) {
                    if (reason.trim().length < 3) { reasonError = "At least 3 characters"; stockOk = false }
                    else { op = 1; delta = q - selected.quantity }
                }
            }
            if (!productOk || !stockOk) return@ProductFormScreen
            val productChanged = draft.hasChanges(product, image.uri != null)
            when {
                op == 0 && !productChanged -> localError = "No changes to save"
                op == 1 -> { stage = 1; viewModel.adjustStock(product.productId, selected!!.batchId, delta, reason) }
                op == 2 -> { stage = 1; viewModel.purchaseStock(product.productId, newBatchDraft.toRequest()) }
                else -> finishOrUpdate()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StockEditSection(
    currentStock: Int, unit: String,
    batches: UiState<List<Batch>>, batchList: List<Batch>, selected: Batch?,
    onSelect: (Batch) -> Unit,
    batchQty: String, onBatchQty: (String) -> Unit, batchQtyError: String?,
    reason: String, onReason: (String) -> Unit, reasonError: String?,
    newBatch: Boolean, onNewBatch: (Boolean) -> Unit, canToggleNewBatch: Boolean,
    newBatchDraft: com.saurabh.mediadminapp.ui.components.StockDraft, enabled: Boolean,
    onRetry: () -> Unit,
) {
    SectionHeader("Stock")
    Text("Current stock: $currentStock ${unit}".trim(), style = MaterialTheme.typography.titleSmall)
    when (batches) {
        is UiState.Loading -> Text("Loading batches…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        is UiState.Error -> Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Could not load batches.", Modifier.weight(1f), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onRetry) { Text("Retry") }
        }
        is UiState.Empty -> Text("No batches yet. Receive the first batch below.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        else -> {}
    }
    if (canToggleNewBatch && batchList.isNotEmpty()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Receive a new batch instead", Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Switch(checked = newBatch, onCheckedChange = onNewBatch, enabled = enabled)
        }
    }
    if (newBatch) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Leave empty to skip. Quantity is added to stock as a new batch.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            StockEntryFields(newBatchDraft, enabled = enabled)
        }
    } else if (batchList.isNotEmpty()) {
        var open by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = open && enabled, onExpandedChange = { open = it }, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selected?.label().orEmpty(), onValueChange = {}, readOnly = true, enabled = enabled,
                label = { Text("Batch to correct") }, singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(open) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = open && enabled, onDismissRequest = { open = false }) {
                batchList.forEach { b -> DropdownMenuItem(text = { Text(b.label()) }, onClick = { onSelect(b); open = false }) }
            }
        }
        OutlinedTextField(
            value = batchQty, onValueChange = onBatchQty, enabled = enabled,
            label = { Text("Quantity in this batch") }, singleLine = true,
            isError = batchQtyError != null,
            supportingText = {
                val old = selected?.quantity
                val new = batchQty.toIntOrNull()
                Text(batchQtyError ?: if (old != null && new != null && new != old) "Change: ${if (new > old) "+" else ""}${new - old}" else "Edit the number to correct stock")
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = reason, onValueChange = onReason, enabled = enabled,
            label = { Text("Reason for change") }, singleLine = true,
            isError = reasonError != null, supportingText = reasonError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun Batch.label(): String {
    val exp = expiryDate?.format(BATCH_DATE)?.let { "exp $it" } ?: "no expiry"
    return "$batchNo · $exp · qty $quantity" + if (isExpired) " · expired" else ""
}
