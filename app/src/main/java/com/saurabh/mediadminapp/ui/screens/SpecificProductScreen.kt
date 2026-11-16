package com.saurabh.mediadminapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.data.v1.dto.Product
import com.saurabh.mediadminapp.ui.adaptive.AdaptiveContent
import com.saurabh.mediadminapp.ui.adaptive.AdaptiveWidths
import com.saurabh.mediadminapp.ui.components.AppScreenScaffold
import com.saurabh.mediadminapp.ui.components.ConfirmDialog
import com.saurabh.mediadminapp.ui.components.DetailCard
import com.saurabh.mediadminapp.ui.components.ErrorState
import com.saurabh.mediadminapp.ui.components.InfoTile
import com.saurabh.mediadminapp.ui.components.LoadingState
import com.saurabh.mediadminapp.ui.components.MedicineImage
import com.saurabh.mediadminapp.ui.components.ShadeButton
import com.saurabh.mediadminapp.ui.components.ShadeOutlinedButton
import com.saurabh.mediadminapp.ui.components.TileIconButton
import com.saurabh.mediadminapp.ui.components.Tone
import com.saurabh.mediadminapp.ui.components.ToneChip
import com.saurabh.mediadminapp.ui.components.dateTimeLabel
import com.saurabh.mediadminapp.ui.components.shadeColor
import com.saurabh.mediadminapp.ui.components.shadeRow
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import com.saurabh.mediadminapp.ui.theme.GS
import com.saurabh.mediadminapp.ui.theme.Shades
import com.saurabh.mediadminapp.ui.theme.isLight
import com.saurabh.mediadminapp.utils.utilityFunctions.FullscreenImagePreviewDialog
import com.saurabh.mediadminapp.utils.utilityFunctions.capitalizeEachWord
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecificProductScreen(productId: String, viewModel: MyViewModel, navController: NavController) {
    val state by viewModel.getSpecificProductState.collectAsStateWithLifecycle()
    val delete by viewModel.deleteProductState.collectAsStateWithLifecycle()

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

    val product = state.success
    val onEdit = { p: Product -> navController.navigate(Routes.UpdateProductRoutes.invoke(p.productId)) }

    AppScreenScaffold(
        title = "Product",
        onBack = { navController.popBackStack() },
        actions = { if (product != null) TileIconButton(Icons.Outlined.Edit, "Edit product", { onEdit(product) }) },
        bottomBar = {
            // One primary action per screen (U8).
            if (product != null) Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                ShadeButton(
                    text = "Edit product", onClick = { onEdit(product) }, icon = Icons.Outlined.Edit,
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        },
    ) { inner ->
        PullToRefreshBox(
            isRefreshing = state.isLoading && product != null,
            onRefresh = { viewModel.getSpecificProduct(productId, force = true) },
            modifier = Modifier.padding(inner).fillMaxSize()
        ) {
            when {
                state.isLoading && product == null -> LoadingState()
                state.error != null && product == null ->
                    ErrorState(message = state.error.orEmpty(), onRetry = { viewModel.getSpecificProduct(productId, force = true) })
                product != null -> AdaptiveContent(maxWidth = AdaptiveWidths.Form) {
                    ProductDetail(
                        p = product,
                        busy = delete.isLoading,
                        onDeactivate = { viewModel.deleteProduct(product.productId) },
                        onReactivate = { viewModel.reactivateProduct(product.productId) },
                        onBatches = { navController.navigate(Routes.BatchListRoutes.invoke(product.productId)) { launchSingleTop = true } },
                        onMovements = { navController.navigate(Routes.StockMovementsRoutes.invoke(productId = product.productId)) { launchSingleTop = true } },
                    )
                }
                else -> LoadingState()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductDetail(p: Product, busy: Boolean, onDeactivate: () -> Unit, onReactivate: () -> Unit, onBatches: () -> Unit, onMovements: () -> Unit) {
    val money = remember { NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("en").setRegion("IN").build()) }
    var zoom by rememberSaveable { mutableStateOf(false) }
    var confirm by rememberSaveable { mutableStateOf(false) }
    val low = p.inStock && p.stock <= p.reorderLevel

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Header: 220dp 1:1 Fit image (U15 / Part 4 §1), tap to zoom.
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            MedicineImage(
                url = p.imageUrl, size = 220.dp,
                modifier = if (!p.imageUrl.isNullOrEmpty()) Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { zoom = true } else Modifier,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                p.name.capitalizeEachWord(), style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground, maxLines = 2, overflow = TextOverflow.Ellipsis,
            )
            val sub = listOf(p.genericName, p.strength).filter { it.isNotBlank() }.joinToString(" · ")
            if (sub.isNotBlank()) Text(sub, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        // Semantic pills (U14/U15).
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when {
                !p.inStock -> ToneChip("Out of stock", Tone.Danger)
                low -> ToneChip("Low stock · ${p.stock}", Tone.Warning)
                else -> ToneChip("In stock · ${p.stock}", Tone.Success)
            }
            if (p.requiresPrescription) ToneChip("Rx Required", Tone.Info)
            if (p.scheduleType.isNotBlank()) ToneChip("Schedule ${p.scheduleType}", Tone.Info)
            if (!p.isActive) ToneChip("Inactive", Tone.Danger)
        }

        DetailCard(title = "Details", icon = Icons.Outlined.Info) {
            TileGrid(
                "Category" to p.category.capitalizeEachWord(),
                "Dosage form" to p.dosageForm,
                "Brand" to p.brandName,
                "Manufacturer" to p.manufacturer,
                "Strength" to p.strength,
                "Pack size" to p.packSize,
                "Unit" to p.unit,
                "HSN" to p.hsnCode,
                "Barcode" to p.barcode,
                "Storage" to p.storageConditions,
            )
        }

        DetailCard(title = "Pricing", icon = Icons.Outlined.Payments) {
            TileGrid(
                "Selling price" to money.format(p.price),
                "MRP" to money.format(p.mrp),
                "Purchase price" to (p.purchasePrice?.let { money.format(it) } ?: ""),
                "GST" to "${p.gstPercent}%",
            )
        }

        DetailCard(title = "Stock", icon = Icons.Outlined.Inventory2) {
            TileGrid(
                "In stock" to "${p.stock} ${p.unit}".trim(),
                "Sellable" to p.sellableStock.toString(),
                "Reorder level" to p.reorderLevel.toString(),
            )
            TealNavRow("View batches", "Expiry, quantity and stock per batch", Icons.Outlined.Layers, onBatches)
            TealNavRow("Stock movements", "Purchases, sales, adjustments, write-offs", Icons.Outlined.SwapVert, onMovements)
        }

        Text(
            "Added ${p.createdAt.dateTimeLabel()} · Updated ${p.updatedAt.dateTimeLabel()}",
            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (p.isActive) {
            ShadeOutlinedButton("Deactivate product", { confirm = true }, Modifier.fillMaxWidth(), enabled = !busy, contentColor = GS.Red600)
        } else {
            ShadeOutlinedButton("Reactivate product", onReactivate, Modifier.fillMaxWidth(), enabled = !busy, contentColor = GS.Green600)
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

/** Two-column Field-fill info tiles; blank values are skipped. */
@Composable
private fun TileGrid(vararg items: Pair<String, String>) {
    val shown = items.filter { it.second.isNotBlank() }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        shown.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (label, value) -> InfoTile(label, value, Modifier.weight(1f)) }
                if (row.size == 1) Box(Modifier.weight(1f))
            }
        }
    }
}

/** Teal info row (Teal 100/600 tile) whose press shade stays Green 50/100 (guide §5 note). */
@Composable
private fun TealNavRow(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    val light = MaterialTheme.isLight
    Row(
        Modifier.fillMaxWidth().shadeRow(onClick).heightIn(min = 56.dp).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier.size(36.dp).clip(MaterialTheme.shapes.small).background(if (light) GS.Teal100 else MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, contentDescription = null, tint = if (light) GS.Teal600 else MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp)) }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
