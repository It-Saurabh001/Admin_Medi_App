package com.saurabh.mediadminapp.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.data.v1.core.OrderStatus
import com.saurabh.mediadminapp.data.v1.dto.Order
import com.saurabh.mediadminapp.ui.components.AppScreenScaffold
import com.saurabh.mediadminapp.ui.components.ConfirmDialog
import com.saurabh.mediadminapp.ui.components.EmptyState
import com.saurabh.mediadminapp.ui.components.ErrorState
import com.saurabh.mediadminapp.ui.components.LinkButton
import com.saurabh.mediadminapp.ui.components.LoadingState
import com.saurabh.mediadminapp.ui.components.ShadeButton
import com.saurabh.mediadminapp.ui.components.ShadeOutlinedButton
import com.saurabh.mediadminapp.ui.components.TileIconButton
import com.saurabh.mediadminapp.ui.components.Tone
import com.saurabh.mediadminapp.ui.components.ToneChip
import com.saurabh.mediadminapp.ui.theme.GS
import com.saurabh.mediadminapp.ui.theme.Shades

/**
 * Prescription for one order: zoomable 1:1-Fit image (pinch / double-tap), PDF -> open externally.
 * Status pills (Rx Required / Verified / Unverified) and one primary action: Mark verified.
 * "Mark unverified" is a red text link behind a confirm dialog (U11).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionViewerScreen(orderId: String, viewModel: MyViewModel, navController: NavController) {
    val state by viewModel.getOrderByIdState.collectAsState()
    val rxState = viewModel.verifyPrescriptionState.collectAsState().value[orderId]
    val order = state.success?.takeIf { it.orderId == orderId }
    val context = LocalContext.current
    var confirmUnverify by rememberSaveable { mutableStateOf(false) }

    // Usually opened from Order details (order already loaded); load it when opened directly.
    LaunchedEffect(orderId) { viewModel.getOrderById(orderId) }
    LaunchedEffect(rxState?.success) {
        if (rxState?.success != null) viewModel.getOrderById(orderId, force = true)
    }

    val url = order?.prescriptionUrl?.takeIf { it.isNotBlank() }
    val isPdf = url?.substringBefore('?')?.endsWith(".pdf", ignoreCase = true) == true
    val openExternally: () -> Unit = {
        url?.let {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, it.toUri()))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(context, "No app found to open this file", Toast.LENGTH_LONG).show()
            }
        }
    }
    val canChange = order != null && (order.status == OrderStatus.PENDING || order.status == OrderStatus.APPROVED)
    val busy = rxState?.isLoading == true

    AppScreenScaffold(
        title = "Prescription",
        onBack = { navController.popBackStack() },
        actions = { if (url != null) TileIconButton(Icons.AutoMirrored.Outlined.OpenInNew, "Open in another app", openExternally) },
        bottomBar = {
            if (order != null && url != null && canChange) {
                Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                    Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
                        if (!order.prescriptionVerified) {
                            ShadeButton(
                                text = "Mark prescription verified", onClick = { viewModel.verifyPrescription(orderId, true) },
                                isLoading = busy, icon = Icons.Outlined.Check, modifier = Modifier.fillMaxWidth(),
                            )
                        } else {
                            LinkButton(
                                text = "Mark as unverified", onClick = { confirmUnverify = true }, enabled = !busy,
                                shade = Shades.RedText, modifier = Modifier.align(Alignment.CenterHorizontally),
                            )
                        }
                    }
                }
            }
        },
    ) { inner ->
        Box(Modifier.padding(inner).fillMaxSize()) {
            when {
                order == null && state.error != null -> ErrorState(
                    message = state.error ?: "Could not load order",
                    onRetry = { viewModel.getOrderById(orderId, force = true) },
                )
                order == null -> LoadingState()
                url == null -> EmptyState(
                    title = "No prescription uploaded",
                    message = if (order.requiresPrescription) "This order needs a prescription before it can be sold." else "This order does not need a prescription.",
                )
                else -> Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    RxHeader(order)
                    Box(
                        Modifier.fillMaxWidth().weight(1f)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isPdf) PdfCard(openExternally) else ZoomableImage(url)
                    }
                    if (!isPdf) Text(
                        "Pinch or double-tap to zoom.", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }
            }
        }
    }

    if (confirmUnverify) {
        ConfirmDialog(
            title = "Mark as unverified?",
            message = "The order cannot be sold until the prescription is verified again.",
            confirmText = "Mark unverified",
            onConfirm = { confirmUnverify = false; viewModel.verifyPrescription(orderId, false) },
            onDismiss = { confirmUnverify = false },
        )
    }
}

@Composable
private fun RxHeader(order: Order) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (order.requiresPrescription) ToneChip("Rx Required", Tone.Info)
        if (order.prescriptionVerified) ToneChip("Verified", Tone.Success) else ToneChip("Not verified", Tone.Warning)
        Text(
            order.patientName.ifBlank { order.userName }, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, modifier = Modifier.weight(1f),
        )
    }
}

/** ContentScale.Fit (never crop), pinch zoom 1x-5x with pan, double-tap toggles 1x / 2.5x. */
@Composable
private fun ZoomableImage(url: String) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transform = rememberTransformableState { zoom, pan, _ ->
        scale = (scale * zoom).coerceIn(1f, 5f)
        offset = if (scale == 1f) Offset.Zero else offset + pan
    }
    SubcomposeAsyncImage(
        model = url,
        contentDescription = "Prescription image",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = {
                    if (scale > 1f) { scale = 1f; offset = Offset.Zero } else scale = 2.5f
                })
            }
            .transformable(transform)
            .graphicsLayer { scaleX = scale; scaleY = scale; translationX = offset.x; translationY = offset.y },
        loading = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(32.dp), strokeWidth = 2.dp) } },
        error = {
            Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(Icons.Outlined.ImageNotSupported, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                Text("Image could not be loaded. Use the open button above.", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            }
        },
    )
}

@Composable
private fun PdfCard(onOpen: () -> Unit) {
    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(shape = MaterialTheme.shapes.medium, color = GS.Teal100, contentColor = GS.Teal600, modifier = Modifier.size(72.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Description, contentDescription = null, modifier = Modifier.size(36.dp)) }
        }
        Text("PDF prescription", style = MaterialTheme.typography.titleMedium)
        Text("Open it in a PDF viewer to read it.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        ShadeOutlinedButton("Open PDF", onOpen, icon = Icons.AutoMirrored.Outlined.OpenInNew)
    }
}
