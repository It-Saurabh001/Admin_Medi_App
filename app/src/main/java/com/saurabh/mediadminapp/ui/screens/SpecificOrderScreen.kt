package com.saurabh.mediadminapp.ui.screens

import androidx.compose.foundation.layout.heightIn
import com.saurabh.mediadminapp.ui.components.shadeRow
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import com.saurabh.mediadminapp.ui.components.GsTextField
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.outlined.AssignmentReturn
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import coil.compose.SubcomposeAsyncImage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.data.v1.core.OrderStatus
import com.saurabh.mediadminapp.data.v1.dto.Order
import com.saurabh.mediadminapp.ui.adaptive.AdaptiveContent
import com.saurabh.mediadminapp.ui.adaptive.AdaptiveWidths
import com.saurabh.mediadminapp.ui.components.AppScreenScaffold
import com.saurabh.mediadminapp.ui.components.ConfirmDialog
import com.saurabh.mediadminapp.ui.components.DetailCard
import com.saurabh.mediadminapp.ui.components.ErrorState
import com.saurabh.mediadminapp.ui.components.InfoTile
import com.saurabh.mediadminapp.ui.components.KeyValueRow
import com.saurabh.mediadminapp.ui.components.LoadingState
import com.saurabh.mediadminapp.ui.components.MedicineImage
import com.saurabh.mediadminapp.ui.components.ShadeButton
import com.saurabh.mediadminapp.ui.components.ShadeOutlinedButton
import com.saurabh.mediadminapp.ui.components.StatusChip
import com.saurabh.mediadminapp.ui.components.Tone
import com.saurabh.mediadminapp.ui.components.ToneChip
import com.saurabh.mediadminapp.ui.components.dateTimeLabel
import com.saurabh.mediadminapp.ui.theme.GS
import com.saurabh.mediadminapp.ui.theme.Shades
import com.saurabh.mediadminapp.utils.utilityFunctions.capitalizeEachWord

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecificOrderScreen(orderId: String, viewModel: MyViewModel, navController: NavController) {
    val goBack: () -> Unit = {
        viewModel.clearGetOrderByIdState()
        navController.popBackStack()
    }
    BackHandler(onBack = goBack)

    val state by viewModel.getOrderByIdState.collectAsState()
    val approveState = viewModel.isApproveOrdder.collectAsState().value[orderId]
    val sellState = viewModel.getRecordSellHistory.collectAsState().value[orderId]
    val returnState = viewModel.returnOrderState.collectAsState().value[orderId]
    val rxState = viewModel.verifyPrescriptionState.collectAsState().value[orderId]
    val editState = viewModel.updateOrderState.collectAsState().value[orderId]
    val cancelState by viewModel.deleteOrderState.collectAsState()
    val order = state.success?.takeIf { it.orderId == orderId }
    var dialog by rememberSaveable { mutableStateOf<String?>(null) } // reject | cancel | return | edit

    LaunchedEffect(orderId) { viewModel.getOrderById(orderId) }
    // Order actions only patch the list row; reload this order so status / Rx state update here.
    LaunchedEffect(approveState?.success, sellState?.success, returnState?.success, rxState?.success, editState?.success, cancelState.success) {
        val any = listOf(approveState?.success, sellState?.success, returnState?.success, rxState?.success, editState?.success, cancelState.success)
        if (any.any { it != null }) viewModel.getOrderById(orderId, force = true)
    }
    val busy = listOf(approveState?.isLoading, sellState?.isLoading, returnState?.isLoading, editState?.isLoading).any { it == true } || cancelState.isLoading

    AppScreenScaffold(
        title = "Order details",
        onBack = goBack,
        actions = {
            if (order?.status == OrderStatus.PENDING) {
                com.saurabh.mediadminapp.ui.components.TileIconButton(Icons.Outlined.Edit, "Edit patient and doctor", { dialog = "edit" })
            }
            // Invoice exists only for sold / returned orders.
            if (order?.status == OrderStatus.SOLD || order?.status == OrderStatus.RETURNED) {
                com.saurabh.mediadminapp.ui.components.TileIconButton(
                    Icons.AutoMirrored.Outlined.ReceiptLong, "Invoice",
                    { navController.navigate(Routes.InvoiceRoutes.invoke(order.orderId)) { launchSingleTop = true } },
                )
            }
        },
        bottomBar = {
            if (order != null) {
                OrderActionBar(
                    status = order.status,
                    busy = busy,
                    onApprove = { viewModel.isApproveOrder(order.orderId, true) },
                    onReject = { dialog = "reject" },
                    onSell = { viewModel.recordSell(order.orderId) },
                    onCancel = { dialog = "cancel" },
                    onReturn = { dialog = "return" },
                )
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                order == null && state.error != null -> ErrorState(
                    message = state.error ?: "Could not load order",
                    onRetry = { viewModel.getOrderById(orderId, force = true) },
                )
                order == null -> LoadingState()
                else -> PullToRefreshBox(
                    isRefreshing = state.isLoading,
                    onRefresh = { viewModel.getOrderById(orderId, force = true) },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    AdaptiveContent(maxWidth = AdaptiveWidths.Form) {
                        OrderBody(
                            order = order,
                            verifying = rxState?.isLoading == true,
                            onVerifyRx = { viewModel.verifyPrescription(order.orderId, true) },
                            onOpenRx = { navController.navigate(Routes.PrescriptionRoutes.invoke(order.orderId)) { launchSingleTop = true } },
                        )
                    }
                }
            }
        }
    }

    if (order != null) when (dialog) {
        "reject" -> ReasonDialog(
            title = "Reject order?",
            message = "Order ${order.orderId} will be rejected.",
            confirmText = "Reject",
            onConfirm = { r -> dialog = null; viewModel.isApproveOrder(order.orderId, false, r) },
            onDismiss = { dialog = null },
        )
        "cancel" -> ReasonDialog(
            title = "Cancel order?",
            message = "The order stays in the list with status Cancelled.",
            confirmText = "Cancel order",
            onConfirm = { r -> dialog = null; viewModel.deleteOrder(order.orderId, r) },
            onDismiss = { dialog = null },
        )
        "return" -> ReasonDialog(
            title = "Return order?",
            message = "Sold stock is restocked where possible.",
            confirmText = "Return",
            onConfirm = { r -> dialog = null; viewModel.returnOrder(order.orderId, r) },
            onDismiss = { dialog = null },
        )
        "edit" -> EditOrderDialog(
            order = order,
            onSave = { patient, doctor -> dialog = null; viewModel.updateOrder(order.orderId, patient, doctor) },
            onDismiss = { dialog = null },
        )
    }
}

/** Destructive confirm (U11) with an optional reason field; confirm text in Red 600. */
@Composable
private fun ReasonDialog(title: String, message: String, confirmText: String, onConfirm: (String?) -> Unit, onDismiss: () -> Unit) {
    var reason by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(title, style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(message, style = MaterialTheme.typography.bodyMedium)
                GsTextField(
                    value = reason, onValueChange = { reason = it.take(200) },
                    label = { Text("Reason (optional)") }, minLines = 2, maxLines = 4,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            com.saurabh.mediadminapp.ui.components.LinkButton(confirmText, { onConfirm(reason.trim().ifBlank { null }) }, shade = com.saurabh.mediadminapp.ui.theme.Shades.RedText)
        },
        dismissButton = { com.saurabh.mediadminapp.ui.components.NeutralTextButton("Keep", onDismiss) },
    )
}

/** Pending orders: edit patient / doctor names (updateOrder). */
@Composable
private fun EditOrderDialog(order: Order, onSave: (String?, String?) -> Unit, onDismiss: () -> Unit) {
    var patient by rememberSaveable { mutableStateOf(order.patientName) }
    var doctor by rememberSaveable { mutableStateOf(order.doctorName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Edit order", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GsTextField(
                    value = patient, onValueChange = { patient = it }, label = { Text("Patient name") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                )
                GsTextField(
                    value = doctor, onValueChange = { doctor = it }, label = { Text("Doctor name") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            com.saurabh.mediadminapp.ui.components.LinkButton("Save", {
                onSave(patient.trim().takeIf { it != order.patientName }, doctor.trim().takeIf { it != order.doctorName })
            })
        },
        dismissButton = { com.saurabh.mediadminapp.ui.components.NeutralTextButton("Cancel", onDismiss) },
    )
}

@Composable
private fun OrderBody(order: Order, verifying: Boolean, onVerifyRx: () -> Unit, onOpenRx: () -> Unit) {
    val inr = rememberInrFormat()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
    ) {
        item(key = "header") {
            DetailCard {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Order", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            order.orderId, style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        )
                    }
                    StatusChip(order.status)
                }
                if (order.status == OrderStatus.PENDING || order.status == OrderStatus.APPROVED || order.status == OrderStatus.SOLD) {
                    ProgressTrack(order.status)
                }
                if (order.status == OrderStatus.CANCELLED && order.cancelReason.isNotBlank()) {
                    InfoTile(label = "Cancel reason", value = order.cancelReason)
                }
            }
        }

        item(key = "product") {
            DetailCard(title = "Product", icon = Icons.Outlined.Receipt) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    MedicineImage(url = order.imageUrl, size = 64.dp)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            order.productName.capitalizeEachWord(), style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            order.category.capitalizeEachWord(), style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        )
                        if (order.requiresPrescription) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ToneChip("Rx Required", Tone.Info)
                                ToneChip(
                                    if (order.prescriptionVerified) "Rx Verified" else "Rx Unverified",
                                    if (order.prescriptionVerified) Tone.Success else Tone.Warning,
                                )
                            }
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                KeyValueRow("Unit price", inr.format(order.price))
                KeyValueRow("Quantity", "${order.quantity}")
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                KeyValueRow("Total", inr.format(order.totalAmount), emphasize = true)
            }
        }

        if (order.requiresPrescription || !order.prescriptionUrl.isNullOrBlank()) item(key = "rx") {
            DetailCard(title = "Prescription", icon = Icons.Outlined.Description) {
                if (!order.prescriptionUrl.isNullOrBlank()) {
                    // Teal info row (guide §5): opens the zoomable viewer; row press shade stays Green 50/100.
                    Row(
                        Modifier.fillMaxWidth().shadeRow(onOpenRx).heightIn(min = 56.dp).padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            Modifier.size(36.dp).clip(MaterialTheme.shapes.small).background(GS.Teal100),
                            contentAlignment = Alignment.Center,
                        ) { Icon(Icons.Outlined.Description, contentDescription = null, tint = GS.Teal600, modifier = Modifier.size(20.dp)) }
                        Column(Modifier.weight(1f)) {
                            Text("View prescription", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text("Zoom, open file, verify", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    val selfRx = order.doctorName.trim().equals("Self prescribed", ignoreCase = true)
                    InfoTile("Prescription", if (selfRx) "Self prescribed by the customer (no file)" else "No prescription uploaded")
                }
                if (order.prescriptionVerified) {
                    ToneChip("Verified", Tone.Success)
                } else if (order.status == OrderStatus.PENDING || order.status == OrderStatus.APPROVED) {
                    ShadeOutlinedButton(
                        text = when {
                            verifying -> "Verifying..."
                            order.prescriptionUrl.isNullOrBlank() -> "Accept without prescription"
                            else -> "Mark prescription verified"
                        },
                        onClick = onVerifyRx, enabled = !verifying, icon = Icons.Outlined.Check,
                        contentColor = GS.Green600, modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        item(key = "customer") {
            DetailCard(title = "Customer & order info", icon = Icons.Outlined.Person) {
                InfoTile("Buyer", order.userName.ifBlank { "Not provided" }, icon = Icons.Outlined.Person)
                InfoTile("Customer ID", order.userId, icon = Icons.Outlined.Numbers)
                InfoTile("Product ID", order.productId, icon = Icons.Outlined.QrCode)
                InfoTile("Ordered on", order.createdAt.dateTimeLabel(), icon = Icons.Outlined.CalendarMonth)
                if (order.patientName.isNotBlank()) InfoTile("Patient", order.patientName)
                if (order.doctorName.isNotBlank()) InfoTile("Doctor", order.doctorName)
                if (order.message.isNotBlank()) InfoTile("Customer note", order.message)
            }
        }
    }
}

/** Placed -> Approved -> Sold. Done = Green 100/600, upcoming = Field fill/Muted. */
@Composable
private fun ProgressTrack(status: OrderStatus) {
    val approved = status == OrderStatus.APPROVED || status == OrderStatus.SOLD
    val sold = status == OrderStatus.SOLD
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TrackStep("Placed", Icons.Outlined.Check, done = true)
        TrackLine(active = approved, modifier = Modifier.weight(1f))
        TrackStep("Approved", Icons.Outlined.Check, done = approved)
        TrackLine(active = sold, modifier = Modifier.weight(1f))
        TrackStep("Sold", Icons.Outlined.Sell, done = sold)
    }
}

@Composable
private fun TrackStep(label: String, icon: ImageVector, done: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            Modifier.size(32.dp).clip(CircleShape).background(if (done) GS.Green100 else GS.FieldFill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = if (done) GS.Green600 else GS.Muted, modifier = Modifier.size(16.dp))
        }
        Text(
            label, style = MaterialTheme.typography.labelMedium, maxLines = 1,
            color = if (done) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TrackLine(active: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier.padding(horizontal = 4.dp).padding(bottom = 18.dp).height(2.dp)
            .background(if (active) GS.Green600 else GS.Outline)
    )
}

/**
 * One primary action per status: PENDING Approve (+ Reject), APPROVED Mark sold (+ Cancel),
 * SOLD Return (destructive, red). CANCELLED / RETURNED have no actions.
 */
@Composable
private fun OrderActionBar(
    status: OrderStatus,
    busy: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onSell: () -> Unit,
    onCancel: () -> Unit,
    onReturn: () -> Unit,
) {
    if (status !in listOf(OrderStatus.PENDING, OrderStatus.APPROVED, OrderStatus.SOLD)) return
    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (status) {
                OrderStatus.PENDING -> {
                    ShadeOutlinedButton("Reject", onReject, Modifier.weight(1f), enabled = !busy, icon = Icons.Outlined.Close, contentColor = GS.Red600)
                    ShadeButton("Approve", onApprove, Modifier.weight(1f), isLoading = busy, icon = Icons.Outlined.Check)
                }
                OrderStatus.APPROVED -> {
                    ShadeOutlinedButton("Cancel", onCancel, Modifier.weight(1f), enabled = !busy, icon = Icons.Outlined.Close, contentColor = GS.Red600)
                    ShadeButton("Mark sold", onSell, Modifier.weight(1f), isLoading = busy, icon = Icons.Outlined.Sell)
                }
                else -> ShadeButton(
                    "Return order", onReturn, Modifier.fillMaxWidth(), shade = Shades.RedSolid, isLoading = busy,
                    icon = Icons.AutoMirrored.Outlined.AssignmentReturn,
                )
            }
        }
    }
}
