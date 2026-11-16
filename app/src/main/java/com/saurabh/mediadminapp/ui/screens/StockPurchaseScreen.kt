package com.saurabh.mediadminapp.ui.screens

import com.saurabh.mediadminapp.ui.components.GsTextField
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.data.v1.error.AppError
import com.saurabh.mediadminapp.ui.components.AppScreenScaffold
import com.saurabh.mediadminapp.ui.components.FormScaffold
import com.saurabh.mediadminapp.ui.components.SaveBar
import com.saurabh.mediadminapp.ui.components.SectionHeader
import com.saurabh.mediadminapp.ui.components.StockEntryFields
import com.saurabh.mediadminapp.ui.components.rememberStockDraft

/** Receive stock: creates a batch or tops one up (one Idempotency-Key per attempt, kept on retry). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockPurchaseScreen(productId: String, viewModel: MyViewModel, navController: NavController) {
    val state by viewModel.stockPurchaseState.collectAsStateWithLifecycle()
    val product by viewModel.getSpecificProductState.collectAsStateWithLifecycle()
    val draft = rememberStockDraft()
    var supplier by rememberSaveable { mutableStateOf("") }
    var invoice by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.clearStockPurchaseState() }
    DisposableEffect(Unit) { onDispose { viewModel.clearStockPurchaseState() } }
    LaunchedEffect(state.success) {
        if (submitted && state.success != null) { navController.popBackStack() }
    }
    LaunchedEffect(state.error) {
        if (state.error != null) draft.applyServerFields((state.appError as? AppError.Api)?.fields)
    }
    val name = product.success?.takeIf { it.productId == productId }?.name
    val formError = state.error.takeIf { draft.errors.isEmpty() }

    AppScreenScaffold(
        title = "Receive stock",
        onBack = { navController.navigateUp() },
        bottomBar = {
            SaveBar("Add to stock", loading = state.isLoading, onClick = {
                if (draft.validate()) {
                    submitted = true
                    viewModel.purchaseStock(
                        productId,
                        draft.toRequest().copy(supplierName = supplier.trim().ifBlank { null }, invoiceNo = invoice.trim().ifBlank { null })
                    )
                }
            })
        }
    ) { inner ->
        Box(Modifier.padding(inner)) {
            FormScaffold {
                name?.let { Text(it, style = MaterialTheme.typography.titleMedium, maxLines = 1) }
                Text(
                    "If the batch number already exists, the quantity is added to that batch.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SectionHeader("Batch")
                StockEntryFields(draft, enabled = !state.isLoading, lastImeDone = false)
                SectionHeader("Supplier (optional)")
                Column(Modifier.fillMaxWidth()) {
                    GsTextField(
                        value = supplier, onValueChange = { supplier = it }, enabled = !state.isLoading,
                        label = { Text("Supplier name") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    GsTextField(
                        value = invoice, onValueChange = { invoice = it }, enabled = !state.isLoading,
                        label = { Text("Invoice number") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Ascii, capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Characters, autoCorrectEnabled = false, imeAction = ImeAction.Done)
                    )
                }
                formError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
            }
        }
    }
}
