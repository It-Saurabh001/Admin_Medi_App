package com.saurabh.mediadminapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.data.v1.error.AppError
import com.saurabh.mediadminapp.ui.components.SectionHeader
import com.saurabh.mediadminapp.ui.components.StockEntryFields
import com.saurabh.mediadminapp.ui.components.rememberStockDraft
import com.saurabh.mediadminapp.utils.utilityFunctions.toImagePart

/** Add product: every product field plus optional opening stock (received as the first batch). */
@Composable
fun AddProductScreen(viewModel: MyViewModel, navController: NavController, modifier: Modifier = Modifier) {
    val response by viewModel.addProductState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val enums = remember { viewModel.cachedAppConfig()?.enums }
    val draft = rememberProductDraft()
    val image = rememberProductImagePicker()
    val stock = rememberStockDraft()

    // Barcode handed over by the scanner when "No product" was shown.
    LaunchedEffect(Unit) {
        val code = navController.currentBackStackEntry?.savedStateHandle?.get<String>(PREFILL_BARCODE_KEY)
        if (!code.isNullOrBlank() && draft["barcode"].isBlank()) draft["barcode"] = code
    }
    LaunchedEffect(response.success) {
        if (response.success != null) {
            viewModel.clearAddProductState()
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh_screen", true)
            navController.popBackStack()
        }
    }
    LaunchedEffect(response.error) {
        if (response.error != null) {
            draft.applyServerFields((response.appError as? AppError.Api)?.fields)
            stock.applyServerFields((response.appError as? AppError.Api)?.fields)
        }
    }
    val formError = response.error.takeIf { draft.errors.isEmpty() && stock.errors.isEmpty() }

    ProductFormScreen(
        title = "Add product", saveText = "Save product",
        draft = draft, enums = enums, image = image, existingImageUrl = null,
        isLoading = response.isLoading, formError = formError,
        onBack = { navController.popBackStack() },
        extra = {
            SectionHeader("Opening stock (optional)")
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "New products start at 0 stock. Fill this in to receive the first batch now, or leave it empty.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StockEntryFields(stock, enabled = !response.isLoading)
            }
        },
        onSave = {
            val productOk = draft.validate()
            val wantsStock = !stock.isBlank
            val stockOk = !wantsStock || stock.validate()
            if (productOk && stockOk) {
                viewModel.addProduct(
                    draft.toCreate(), image.uri?.toImagePart(context),
                    openingStock = if (wantsStock) stock.toOpeningStock() else null
                )
            }
        }
    )
}
