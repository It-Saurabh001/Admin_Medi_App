package com.saurabh.mediadminapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.data.v1.dto.Product
import com.saurabh.mediadminapp.ui.components.ConfirmDialog
import com.saurabh.mediadminapp.ui.components.ErrorState
import com.saurabh.mediadminapp.ui.components.LoadingState
import com.saurabh.mediadminapp.ui.components.SectionHeader
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import com.saurabh.mediadminapp.utils.utilityFunctions.FullscreenImagePreviewDialog
import com.saurabh.mediadminapp.utils.utilityFunctions.capitalizeEachWord
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecificProductScreen(productId: String, viewModel: MyViewModel, navController: NavController) {
    val state by viewModel.getSpecificProductState.collectAsState()
    val delete by viewModel.deleteProductState.collectAsState()

    LaunchedEffect(productId) {
        viewModel.clearDeleteProductState()
        viewModel.clearGetSpecificProductState()
        viewModel.getSpecificProduct(productId)
    }
    LaunchedEffect(delete.success) {
        if (delete.success != null) {
            viewModel.clearDeleteProductState()
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh_screen", true)
            navController.popBackStack()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Product", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { inner ->
        Box(Modifier.padding(inner).fillMaxSize()) {
            val product = state.success
            when {
                state.isLoading && product == null -> LoadingState()
                state.error != null && product == null ->
                    ErrorState(message = state.error.orEmpty(), onRetry = { viewModel.getSpecificProduct(productId, force = true) })
                product != null -> ProductDetail(
                    p = product,
                    onEdit = { navController.navigate(Routes.UpdateProductRoutes.invoke(product.productId)) },
                    onDeactivate = { viewModel.deleteProduct(product.productId) },
                    onReactivate = { viewModel.reactivateProduct(product.productId) }
                )
                else -> LoadingState()
            }
        }
    }
}

@Composable
private fun ProductDetail(p: Product, onEdit: () -> Unit, onDeactivate: () -> Unit, onReactivate: () -> Unit) {
    val money = NumberFormat.getInstance(Locale.Builder().setLanguage("en").setRegion("IN").build())
    var zoom by remember { mutableStateOf(false) }
    var confirm by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            val img = p.imageUrl
            if (!img.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(img),
                    contentDescription = p.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(160.dp).clip(MaterialTheme.shapes.large).clickable { zoom = true }
                )
            } else {
                Icon(Icons.Outlined.Inventory2, contentDescription = null, modifier = Modifier.size(96.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(
            p.name.capitalizeEachWord(),
            style = MaterialTheme.typography.titleLarge,
            maxLines = 2, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (!p.isActive) {
            Text("Deactivated", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
        }

        SectionHeader("Details", Modifier.padding(top = 16.dp))
        InfoRow("Category", p.category.capitalizeEachWord())
        InfoRow("Generic name", p.genericName)
        InfoRow("Brand", p.brandName)
        InfoRow("Manufacturer", p.manufacturer)
        InfoRow("Dosage form", p.dosageForm)
        InfoRow("Strength / pack", listOf(p.strength, p.packSize).filter { it.isNotBlank() }.joinToString(" · "))
        InfoRow("Schedule", p.scheduleType + if (p.requiresPrescription) " · Rx" else "")
        InfoRow("Barcode", p.barcode)

        HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
        SectionHeader("Pricing")
        InfoRow("Selling price", "₹" + money.format(p.price))
        InfoRow("MRP", "₹" + money.format(p.mrp))
        p.purchasePrice?.let { InfoRow("Purchase price", "₹" + money.format(it)) }
        InfoRow("GST", "${p.gstPercent}%")

        HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
        SectionHeader("Stock")
        InfoRow("In stock", "${p.stock} ${p.unit}".trim())
        InfoRow("Sellable", p.sellableStock.toString())
        InfoRow("Reorder level", p.reorderLevel.toString())

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(onClick = onEdit, modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("Edit") }
            TextButton(
                onClick = { if (p.isActive) confirm = true else onReactivate() },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) { Text(if (p.isActive) "Deactivate" else "Reactivate") }
        }
    }

    if (zoom && !p.imageUrl.isNullOrEmpty()) {
        FullscreenImagePreviewDialog(imageUrl = p.imageUrl, onDismiss = { zoom = false })
    }
    if (confirm) {
        ConfirmDialog(
            title = "Deactivate product?",
            message = "${p.name.capitalizeEachWord()} will be hidden from sales. You can reactivate it later.",
            confirmText = "Deactivate",
            onConfirm = { confirm = false; onDeactivate() },
            onDismiss = { confirm = false }
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    if (value.isBlank()) return
    ListItem(
        overlineContent = { Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1) },
        headlineContent = { Text(value, style = MaterialTheme.typography.bodyLarge, maxLines = 3, overflow = TextOverflow.Ellipsis) }
    )
}
