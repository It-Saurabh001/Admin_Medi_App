package com.saurabh.mediadminapp.ui.screens

import com.saurabh.mediadminapp.ui.components.GsTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.saurabh.mediadminapp.data.v1.dto.Batch
import com.saurabh.mediadminapp.data.v1.dto.BatchUpdateRequest
import com.saurabh.mediadminapp.data.v1.error.AppError
import com.saurabh.mediadminapp.ui.components.AppScreenScaffold
import com.saurabh.mediadminapp.ui.components.ConfirmDialog
import com.saurabh.mediadminapp.ui.components.DateField
import com.saurabh.mediadminapp.ui.components.ErrorState
import com.saurabh.mediadminapp.ui.components.FormScaffold
import com.saurabh.mediadminapp.ui.components.SaveBar
import com.saurabh.mediadminapp.ui.components.SectionHeader
import com.saurabh.mediadminapp.ui.components.SkeletonList
import com.saurabh.mediadminapp.ui.components.StatCard
import com.saurabh.mediadminapp.ui.components.showDate
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import java.time.LocalDate

/** Batch metadata (not quantity) and write-off. Quantity changes go through Adjust stock. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchDetailScreen(batchId: String, viewModel: MyViewModel, navController: NavController) {
    val state by viewModel.batchDetail.collectAsStateWithLifecycle()
    LaunchedEffect(batchId) { viewModel.clearBatchAction(); viewModel.getBatch(batchId, force = true) }
    DisposableEffect(Unit) { onDispose { viewModel.clearBatchAction() } }
    val batch = (state as? UiState.Success)?.data?.takeIf { it.batchId == batchId }

    AppScreenScaffold(title = batch?.let { "Batch ${it.batchNo}" } ?: "Batch", onBack = { navController.navigateUp() }) { inner ->
        PullToRefreshBox(
            isRefreshing = state is UiState.Loading && batch != null,
            onRefresh = { viewModel.getBatch(batchId, force = true) },
            modifier = Modifier.padding(inner).fillMaxSize()
        ) {
            when {
                batch != null -> BatchEditor(batch, viewModel, navController)
                state is UiState.Error -> ErrorState((state as UiState.Error).error.userMessage(), onRetry = { viewModel.getBatch(batchId, force = true) })
                else -> SkeletonList(rows = 5)
            }
        }
    }
}

@Composable
private fun BatchEditor(b: Batch, viewModel: MyViewModel, navController: NavController) {
    val action by viewModel.batchAction.collectAsStateWithLifecycle()
    var batchNo by rememberSaveable(b.batchId) { mutableStateOf(b.batchNo) }
    var expiry by rememberSaveable(b.batchId) { mutableStateOf(b.expiryDate?.toString().orEmpty()) }
    var mfg by rememberSaveable(b.batchId) { mutableStateOf(b.mfgDate?.toString().orEmpty()) }
    var received by rememberSaveable(b.batchId) { mutableStateOf(b.receivedDate?.toString().orEmpty()) }
    var buy by rememberSaveable(b.batchId) { mutableStateOf(b.purchasePrice.fmt()) }
    var mrp by rememberSaveable(b.batchId) { mutableStateOf(b.mrp.fmt()) }
    var supplier by rememberSaveable(b.batchId) { mutableStateOf(b.supplierName) }
    var note by rememberSaveable { mutableStateOf("") }
    var errors by rememberSaveable { mutableStateOf<Map<String, String>>(emptyMap()) }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmWriteOff by rememberSaveable { mutableStateOf(false) }
    val busy = action.isLoading

    LaunchedEffect(action.success) {
        if (action.success != null) {
            val wasWriteOff = action.wroteOff
            viewModel.clearBatchAction()
            if (!wasWriteOff) navController.popBackStack()
        }
    }
    LaunchedEffect(action.error) {
        (action.appError as? AppError.Api)?.fields?.let { f ->
            errors = buildMap {
                f["batch_no"]?.let { put("batch", it) }; f["expiry_date"]?.let { put("expiry", it) }
                f["purchase_price"]?.let { put("buy", it) }; f["mrp"]?.let { put("mrp", it) }
            }
        }
    }
    val formError = localError ?: action.error.takeIf { errors.isEmpty() }

    fun save() {
        localError = null
        val exp = runCatching { LocalDate.parse(expiry) }.getOrNull()
        errors = buildMap {
            if (batchNo.isBlank()) put("batch", "Enter the batch number")
            if (exp == null) put("expiry", "Pick the expiry date")
            if (buy.isNotBlank() && buy.toDoubleOrNull()?.takeIf { it >= 0 } == null) put("buy", "Enter a valid price")
            if (mrp.isNotBlank() && mrp.toDoubleOrNull()?.takeIf { it >= 0 } == null) put("mrp", "Enter a valid MRP")
        }
        if (errors.isNotEmpty()) return
        val req = BatchUpdateRequest(
            batchNo = batchNo.trim(), expiryDate = exp,
            mfgDate = runCatching { LocalDate.parse(mfg) }.getOrNull()?.takeIf { it != b.mfgDate },
            receivedDate = runCatching { LocalDate.parse(received) }.getOrNull()?.takeIf { it != b.receivedDate },
            purchasePrice = buy.toDoubleOrNull()?.takeIf { it != b.purchasePrice },
            mrp = mrp.toDoubleOrNull()?.takeIf { it != b.mrp },
            supplierName = supplier.trim().takeIf { it.isNotBlank() && it != b.supplierName }
        )
        val unchanged = req.batchNo == b.batchNo && req.expiryDate == b.expiryDate && req.mfgDate == null &&
            req.receivedDate == null && req.purchasePrice == null && req.mrp == null && req.supplierName == null
        if (unchanged) localError = "No changes to save" else viewModel.updateBatch(b.batchId, req)
    }

    androidx.compose.foundation.layout.Column {
        androidx.compose.foundation.layout.Box(Modifier.weight(1f)) {
            FormScaffold {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("In stock", "${b.quantity}", Icons.Outlined.Inventory2, onClick = null, modifier = Modifier.weight(1f), highlight = !b.isExpired)
                    StatCard(
                        if (b.isExpired) "Expired" else "Expires in",
                        if (b.isExpired) b.expiryDate.showDate() else (b.daysToExpiry?.let { "$it days" } ?: "-"),
                        Icons.Outlined.EventBusy, onClick = null, modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    "${b.productName} · received ${b.receivedDate.showDate()}",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SectionHeader("Details")
                GsTextField(
                    value = batchNo, onValueChange = { batchNo = it; errors = errors - "batch" }, enabled = !busy,
                    label = { Text("Batch number") }, singleLine = true, isError = errors["batch"] != null,
                    supportingText = errors["batch"]?.let { { Text(it) } }, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Ascii, capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Characters, autoCorrectEnabled = false, imeAction = ImeAction.Next)
                )
                DateField("Expiry date", expiry, { expiry = it; errors = errors - "expiry" }, !busy, errors["expiry"])
                DateField("Manufactured (optional)", mfg, { mfg = it }, !busy, null)
                DateField("Received (optional)", received, { received = it }, !busy, null)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GsTextField(
                        value = buy, onValueChange = { buy = it; errors = errors - "buy" }, enabled = !busy,
                        label = { Text("Purchase price") }, singleLine = true, isError = errors["buy"] != null,
                        supportingText = errors["buy"]?.let { { Text(it) } }, modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
                    )
                    GsTextField(
                        value = mrp, onValueChange = { mrp = it; errors = errors - "mrp" }, enabled = !busy,
                        label = { Text("MRP") }, singleLine = true, isError = errors["mrp"] != null,
                        supportingText = errors["mrp"]?.let { { Text(it) } }, modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
                    )
                }
                GsTextField(
                    value = supplier, onValueChange = { supplier = it }, enabled = !busy,
                    label = { Text("Supplier") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
                formError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }

                SectionHeader("Quantity")
                com.saurabh.mediadminapp.ui.components.LinkButton(
                    text = "View stock movements for this batch",
                    onClick = { navController.navigate(Routes.StockMovementsRoutes.invoke(batchId = b.batchId)) { launchSingleTop = true } },
                )
                com.saurabh.mediadminapp.ui.components.ShadeOutlinedButton(
                    text = "Adjust quantity",
                    onClick = {
                        navController.navigate(Routes.StockAdjustRoutes.invoke(b.productId)) { launchSingleTop = true }
                        navController.currentBackStackEntry?.savedStateHandle?.set(ADJUST_BATCH_KEY, b.batchId)
                    },
                    enabled = !busy, modifier = Modifier.fillMaxWidth()
                )
                if (b.quantity > 0) {
                    GsTextField(
                        value = note, onValueChange = { note = it }, enabled = !busy,
                        label = { Text("Write-off note (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                    com.saurabh.mediadminapp.ui.components.ShadeOutlinedButton(
                        text = "Write off ${b.quantity} units", onClick = { confirmWriteOff = true }, enabled = !busy,
                        contentColor = com.saurabh.mediadminapp.ui.theme.GS.Red600, modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        SaveBar("Save changes", loading = busy, onClick = ::save)
    }

    if (confirmWriteOff) {
        ConfirmDialog(
            title = "Write off this batch?",
            message = "All ${b.quantity} units of batch ${b.batchNo} will be removed from stock. This cannot be undone.",
            confirmText = "Write off",
            onConfirm = { confirmWriteOff = false; viewModel.writeOffBatch(b.batchId, note) },
            onDismiss = { confirmWriteOff = false }
        )
    }
}

internal const val ADJUST_BATCH_KEY = "adjust_batch_id"

private fun Double.fmt() = if (this % 1.0 == 0.0) toLong().toString() else toString()
