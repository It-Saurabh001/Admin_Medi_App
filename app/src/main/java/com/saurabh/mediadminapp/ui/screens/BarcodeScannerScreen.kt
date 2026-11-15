package com.saurabh.mediadminapp.ui.screens

import android.os.SystemClock
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FlashlightOff
import androidx.compose.material.icons.outlined.FlashlightOn
import androidx.compose.material.icons.outlined.NoPhotography
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.saurabh.mediadminapp.BarcodeLookupState
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.ui.adaptive.rememberWindowSize
import com.saurabh.mediadminapp.ui.components.AppScreenScaffold
import com.saurabh.mediadminapp.ui.device.CAMERA_PERMISSION
import com.saurabh.mediadminapp.ui.device.PermissionStatus
import com.saurabh.mediadminapp.ui.device.hasCamera
import com.saurabh.mediadminapp.ui.device.rememberPermissionState
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import java.util.concurrent.Executors

/** savedStateHandle key AddProduct can read to prefill the barcode (see ledger row 14). */
const val PREFILL_BARCODE_KEY = "prefill_barcode"
private const val SAME_CODE_DEBOUNCE_MS = 2500L

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen(viewModel: MyViewModel, navController: NavController) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val window = rememberWindowSize()
    val lookup by viewModel.barcodeLookup.collectAsStateWithLifecycle()
    val deviceHasCamera = remember { context.hasCamera() }
    val permission = rememberPermissionState(CAMERA_PERMISSION)
    var asked by rememberSaveable { mutableStateOf(false) }
    var torchOn by rememberSaveable { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var manual by rememberSaveable { mutableStateOf("") }

    // Fresh state on entry and exit.
    DisposableEffect(Unit) {
        viewModel.clearBarcodeLookup()
        onDispose { viewModel.clearBarcodeLookup() }
    }
    // Ask once, in context (the admin opened the scanner).
    LaunchedEffect(deviceHasCamera) {
        if (deviceHasCamera && !asked && permission.status == PermissionStatus.NotRequested) {
            asked = true
            permission.request()
        }
    }
    LaunchedEffect(torchOn, camera) { camera?.cameraControl?.enableTorch(torchOn) }

    LaunchedEffect(lookup) {
        val found = lookup as? BarcodeLookupState.Found ?: return@LaunchedEffect
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        navController.navigate(Routes.SpecificProductRoutes.invoke(found.product.productId)) {
            popUpTo(Routes.BarcodeScannerRoutes.route) { inclusive = true }
            launchSingleTop = true
        }
    }

    val busy = lookup is BarcodeLookupState.Loading || lookup is BarcodeLookupState.Found
    val onDetected: (String) -> Unit = { code ->
        if (!busy && lookup !is BarcodeLookupState.NotFound && lookup !is BarcodeLookupState.Failed) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            viewModel.lookupBarcode(code)
        }
    }
    val addWithBarcode: (String) -> Unit = { code ->
        navController.navigate(Routes.AddProductRoutes.route) {
            popUpTo(Routes.BarcodeScannerRoutes.route) { inclusive = true }
            launchSingleTop = true
        }
        navController.currentBackStackEntry?.savedStateHandle?.set(PREFILL_BARCODE_KEY, code)
    }

    AppScreenScaffold(
        title = "Scan barcode",
        onBack = { navController.navigateUp() },
        actions = {
            if (permission.isGranted && camera?.cameraInfo?.hasFlashUnit() == true) {
                IconButton(onClick = { torchOn = !torchOn }) {
                    Icon(
                        if (torchOn) Icons.Outlined.FlashlightOff else Icons.Outlined.FlashlightOn,
                        contentDescription = if (torchOn) "Turn torch off" else "Turn torch on"
                    )
                }
            }
        }
    ) { inner ->
        val preview: @Composable (Modifier) -> Unit = { m ->
            Box(m.background(MaterialTheme.colorScheme.surfaceContainerHighest), contentAlignment = Alignment.Center) {
                when {
                    !deviceHasCamera -> CameraMessage(Icons.Outlined.NoPhotography, "No camera on this device. Enter the barcode below.")
                    permission.isGranted -> {
                        CameraPreview(onDetected = onDetected, onCamera = { camera = it }, modifier = Modifier.fillMaxSize())
                        Box(
                            Modifier.fillMaxWidth(0.75f).height(140.dp)
                                .border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
                        )
                    }
                    else -> PermissionPrompt(permission.status, onRequest = permission::request, onSettings = permission::openSettings)
                }
            }
        }
        val panel: @Composable ColumnScope.() -> Unit = {
            ResultPanel(
                lookup = lookup,
                onScanAgain = { viewModel.clearBarcodeLookup() },
                onRetry = { (lookup as? BarcodeLookupState.Failed)?.let { viewModel.clearBarcodeLookup(); viewModel.lookupBarcode(it.barcode) } },
                onAdd = addWithBarcode
            )
            OutlinedTextField(
                value = manual,
                onValueChange = { manual = it.trim() },
                label = { Text("Enter barcode manually") },
                singleLine = true,
                enabled = !busy,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { if (manual.isNotBlank()) { viewModel.clearBarcodeLookup(); viewModel.lookupBarcode(manual) } }),
                modifier = Modifier.fillMaxWidth()
            )
            FilledTonalButton(
                onClick = { viewModel.clearBarcodeLookup(); viewModel.lookupBarcode(manual) },
                enabled = manual.isNotBlank() && !busy,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Look up") }
        }

        val sideBySide = window.isExpanded || window.isLandscape
        if (sideBySide) {
            Row(Modifier.padding(inner).fillMaxSize()) {
                preview(Modifier.weight(1f).fillMaxHeight())
                Column(
                    Modifier.width(360.dp).fillMaxHeight().verticalScroll(rememberScrollState()).imePadding().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    content = panel
                )
            }
        } else {
            Column(Modifier.padding(inner).fillMaxSize()) {
                preview(Modifier.fillMaxWidth().weight(1f))
                Column(
                    Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).imePadding().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    content = panel
                )
            }
        }
    }
}

@Composable
private fun ResultPanel(
    lookup: BarcodeLookupState,
    onScanAgain: () -> Unit,
    onRetry: () -> Unit,
    onAdd: (String) -> Unit,
) {
    when (lookup) {
        BarcodeLookupState.Idle -> Text(
            "Point the camera at a barcode. It is looked up automatically.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        is BarcodeLookupState.Loading, is BarcodeLookupState.Found -> Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(12.dp))
            Text("Looking up ${barcodeOf(lookup)}…", maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        is BarcodeLookupState.NotFound -> ResultCard(Icons.Outlined.SearchOff, "No product with barcode ${lookup.barcode}") {
            Button(onClick = { onAdd(lookup.barcode) }, modifier = Modifier.weight(1f)) { Text("Add product", maxLines = 1) }
            OutlinedButton(onClick = onScanAgain, modifier = Modifier.weight(1f)) { Text("Scan again", maxLines = 1) }
        }
        is BarcodeLookupState.Failed -> ResultCard(Icons.Outlined.SearchOff, lookup.message, isError = true) {
            Button(onClick = onRetry, modifier = Modifier.weight(1f)) { Text("Retry") }
            OutlinedButton(onClick = onScanAgain, modifier = Modifier.weight(1f)) { Text("Scan again", maxLines = 1) }
        }
    }
}

private fun barcodeOf(s: BarcodeLookupState) = when (s) {
    is BarcodeLookupState.Loading -> s.barcode
    is BarcodeLookupState.Found -> s.barcode
    else -> ""
}

@Composable
private fun ResultCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    isError: Boolean = false,
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
        contentColor = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text(text, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), content = actions)
        }
    }
}

@Composable
private fun PermissionPrompt(status: PermissionStatus, onRequest: () -> Unit, onSettings: () -> Unit) {
    Column(
        Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Outlined.PhotoCamera, contentDescription = null, modifier = Modifier.size(40.dp))
        Text(
            if (status == PermissionStatus.PermanentlyDenied) "Camera access is turned off. Enable it in Settings to scan, or type the barcode below."
            else "Camera access is needed to scan product barcodes. Images are not stored.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
        if (status == PermissionStatus.PermanentlyDenied) Button(onClick = onSettings) { Text("Open settings") }
        else Button(onClick = onRequest) { Text("Allow camera") }
    }
}

@Composable
private fun CameraMessage(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(8.dp))
        Text(text, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
    }
}

/** CameraX preview + ML Kit analysis bound to the screen lifecycle; unbinds and closes on dispose. */
@OptIn(ExperimentalGetImage::class)
@Composable
private fun CameraPreview(onDetected: (String) -> Unit, onCamera: (Camera?) -> Unit, modifier: Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestOnDetected by rememberUpdatedState(onDetected)
    val previewView = remember { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }

    DisposableEffect(lifecycleOwner) {
        val executor = Executors.newSingleThreadExecutor()
        val scanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder().setBarcodeFormats(
                Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_CODE_128, Barcode.FORMAT_CODE_39, Barcode.FORMAT_QR_CODE, Barcode.FORMAT_DATA_MATRIX
            ).build()
        )
        var lastCode: String? = null
        var lastAt = 0L
        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { a ->
                a.setAnalyzer(executor) { proxy ->
                    val media = proxy.image
                    if (media == null) { proxy.close(); return@setAnalyzer }
                    scanner.process(InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees))
                        .addOnSuccessListener { codes ->
                            val code = codes.firstNotNullOfOrNull { it.rawValue?.trim()?.takeIf(String::isNotEmpty) }
                            val now = SystemClock.elapsedRealtime()
                            if (code != null && (code != lastCode || now - lastAt > SAME_CODE_DEBOUNCE_MS)) {
                                lastCode = code; lastAt = now
                                ContextCompat.getMainExecutor(context).execute { latestOnDetected(code) }
                            }
                        }
                        .addOnCompleteListener { proxy.close() }
                }
            }
        val providerFuture = ProcessCameraProvider.getInstance(context)
        var provider: ProcessCameraProvider? = null
        providerFuture.addListener({
            runCatching {
                val p = providerFuture.get().also { provider = it }
                val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                p.unbindAll()
                onCamera(p.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis))
            }.onFailure { onCamera(null) }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            runCatching { provider?.unbindAll() }
            onCamera(null)
            analysis.clearAnalyzer()
            scanner.close()
            executor.shutdown()
        }
    }

    AndroidView(factory = { previewView }, modifier = modifier)
}
