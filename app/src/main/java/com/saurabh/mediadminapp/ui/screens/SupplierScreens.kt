package com.saurabh.mediadminapp.ui.screens

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.SupplierQuery
import com.saurabh.mediadminapp.data.v1.core.UiState
import com.saurabh.mediadminapp.data.v1.dto.Supplier
import com.saurabh.mediadminapp.data.v1.dto.SupplierCreateRequest
import com.saurabh.mediadminapp.data.v1.dto.SupplierUpdateRequest
import com.saurabh.mediadminapp.ui.components.AppScreenScaffold
import com.saurabh.mediadminapp.ui.components.ChipLabel
import com.saurabh.mediadminapp.ui.components.ConfirmDialog
import com.saurabh.mediadminapp.ui.components.DetailCard
import com.saurabh.mediadminapp.ui.components.EmptyState
import com.saurabh.mediadminapp.ui.components.ErrorState
import com.saurabh.mediadminapp.ui.components.FormScaffold
import com.saurabh.mediadminapp.ui.components.GsFab
import com.saurabh.mediadminapp.ui.components.GsFilterChip
import com.saurabh.mediadminapp.ui.components.GsTextField
import com.saurabh.mediadminapp.ui.components.InfoTile
import com.saurabh.mediadminapp.ui.components.LoadingState
import com.saurabh.mediadminapp.ui.components.PagedLazyColumn
import com.saurabh.mediadminapp.ui.components.ShadeButton
import com.saurabh.mediadminapp.ui.components.ShadeOutlinedButton
import com.saurabh.mediadminapp.ui.components.SkeletonList
import com.saurabh.mediadminapp.ui.components.Tone
import com.saurabh.mediadminapp.ui.components.ToneChip
import com.saurabh.mediadminapp.ui.components.dateTimeLabel
import com.saurabh.mediadminapp.ui.components.shadeRow
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import com.saurabh.mediadminapp.ui.theme.GS
import com.saurabh.mediadminapp.ui.theme.isLight
import kotlinx.coroutines.delay

/** Route argument that opens the create form. */
const val NEW_SUPPLIER_ID = "new"

private val ACTIVE_FILTERS = listOf<Pair<Boolean?, String>>(true to "Active", false to "Inactive", null to "All")
private val GSTIN = Regex("^[0-9]{2}[A-Z0-9]{13}$")

// ───────────────────────────── List ─────────────────────────────

/** Suppliers (response.md #63): server search (400 ms debounce), Active / Inactive / All, PTR, load-more, one FAB. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierListScreen(viewModel: MyViewModel, navController: NavController) {
    val state by viewModel.suppliers.collectAsStateWithLifecycle()
    val query by viewModel.supplierQuery.collectAsStateWithLifecycle()
    val focus = LocalFocusManager.current
    var text by rememberSaveable { mutableStateOf(query.q) }

    LaunchedEffect(Unit) { viewModel.loadSuppliers() }
    LaunchedEffect(text) {
        if (text == query.q) return@LaunchedEffect
        delay(400)
        viewModel.loadSuppliers(query.copy(q = text))
    }
    val open: (String) -> Unit = { id -> navController.navigate(Routes.SupplierFormRoutes.invoke(id)) { launchSingleTop = true } }

    AppScreenScaffold(
        title = "Suppliers",
        onBack = { navController.popBackStack() },
        floatingActionButton = { GsFab("Add supplier", Icons.Outlined.Add, { open(NEW_SUPPLIER_ID) }) },
    ) { inner ->
        PagedLazyColumn(
            isRefreshing = state.isLoading && state.loaded,
            onRefresh = { viewModel.loadSuppliers(force = true) },
            isLoadingMore = state.isLoadingMore,
            endReached = state.endReached,
            onLoadMore = viewModel::loadMoreSuppliers,
            state = rememberLazyListState(),
            modifier = Modifier.padding(inner).fillMaxSize(),
        ) {
            item(key = "search", contentType = "search") {
                GsTextField(
                    value = text, onValueChange = { text = it },
                    placeholder = { Text("Search name, phone or GSTIN") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    trailingIcon = if (text.isNotEmpty()) {
                        { IconButton(onClick = { text = "" }) { Icon(Icons.Outlined.Close, contentDescription = "Clear search") } }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, autoCorrectEnabled = false, imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item(key = "chips", contentType = "chips") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ACTIVE_FILTERS, key = { it.second }) { (value, label) ->
                        GsFilterChip(selected = query.isActive == value, onClick = { viewModel.loadSuppliers(query.copy(isActive = value)) }, label = { ChipLabel(label) })
                    }
                }
            }
            when {
                !state.loaded && state.error != null -> item(key = "error", contentType = "state") { ErrorState(state.error ?: "Could not load", { viewModel.loadSuppliers(force = true) }) }
                !state.loaded -> item(key = "skeleton", contentType = "state") { SkeletonList(rows = 5) }
                state.items.isEmpty() -> item(key = "empty", contentType = "state") {
                    EmptyState(
                        title = if (query.q.isBlank()) "No suppliers yet" else "No matching suppliers",
                        message = if (query.q.isBlank()) "Add the distributors you buy stock from." else "Try another name, phone or GSTIN.",
                    )
                }
                else -> items(state.items, key = { it.supplierId }, contentType = { "supplier" }) { s -> SupplierRow(s) { open(s.supplierId) } }
            }
        }
    }
}

@Composable
private fun SupplierRow(s: Supplier, onClick: () -> Unit) {
    val light = MaterialTheme.isLight
    ListItem(
        leadingContent = {
            Box(
                Modifier.size(44.dp).clip(MaterialTheme.shapes.small).background(if (light) GS.Teal100 else MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.LocalShipping, contentDescription = null, tint = if (light) GS.Teal600 else MaterialTheme.colorScheme.onSecondaryContainer) }
        },
        headlineContent = { Text(s.name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val line = listOfNotNull(s.contactPhone?.takeIf { it.isNotBlank() }, s.email?.takeIf { it.isNotBlank() }).joinToString(" · ")
                if (line.isNotBlank()) Text(line, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (s.isActive) ToneChip("Active", Tone.Success) else ToneChip("Inactive", Tone.Danger)
                    s.gstNumber?.takeIf { it.isNotBlank() }?.let { ToneChip("GST $it", Tone.Info) }
                }
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth().shadeRow(onClick),
    )
}

// ───────────────────────────── Detail / edit / create ─────────────────────────────

/** Supplier detail + edit (PATCH, changed fields; name always sent) or create ([NEW_SUPPLIER_ID]). Deactivate behind a confirm. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierFormScreen(supplierId: String, viewModel: MyViewModel, navController: NavController) {
    val isNew = supplierId == NEW_SUPPLIER_ID
    val detail by viewModel.supplierDetail.collectAsStateWithLifecycle()
    val action by viewModel.supplierAction.collectAsStateWithLifecycle()
    val focus = LocalFocusManager.current

    LaunchedEffect(supplierId) {
        viewModel.clearSupplierAction()
        if (!isNew) viewModel.getSupplier(supplierId, force = true)
    }
    val supplier = (detail as? UiState.Success)?.data?.takeIf { !isNew && it.supplierId == supplierId }

    var name by rememberSaveable(supplier?.supplierId) { mutableStateOf(supplier?.name.orEmpty()) }
    var phone by rememberSaveable(supplier?.supplierId) { mutableStateOf(supplier?.contactPhone.orEmpty()) }
    var email by rememberSaveable(supplier?.supplierId) { mutableStateOf(supplier?.email.orEmpty()) }
    var gst by rememberSaveable(supplier?.supplierId) { mutableStateOf(supplier?.gstNumber.orEmpty()) }
    var address by rememberSaveable(supplier?.supplierId) { mutableStateOf(supplier?.address.orEmpty()) }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var mySeq by rememberSaveable { mutableStateOf(-1) }
    var pendingAction by rememberSaveable { mutableStateOf("") } // save | deactivate
    var confirmDeactivate by rememberSaveable { mutableStateOf(false) }

    val dirty = isNew || (supplier != null && (name.trim() != supplier.name || phone.trim() != supplier.contactPhone.orEmpty() ||
        email.trim() != supplier.email.orEmpty() || gst.trim() != supplier.gstNumber.orEmpty() || address.trim() != supplier.address.orEmpty()))
    val mine = action.seq == mySeq
    val busy = mine && action.isLoading

    // React only to the request this screen started (state is shared).
    LaunchedEffect(action) {
        if (!mine || action.isLoading) return@LaunchedEffect
        if (action.success != null) {
            if (isNew || pendingAction == "save") navController.popBackStack()
            pendingAction = ""
        } else if (action.error != null) {
            val e = action.appError
            errors = listOf("name", "contact_phone", "email", "gst_number", "address").mapNotNull { k -> e.fieldError(k)?.let { k to it } }.toMap()
            pendingAction = ""
        }
    }

    fun save() {
        focus.clearFocus()
        errors = buildMap {
            if (name.isBlank()) put("name", "Name is required")
            else if (name.trim().length > 200) put("name", "Use at most 200 characters")
            if (email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) put("email", "Enter a valid email")
            if (gst.isNotBlank() && !GSTIN.matches(gst.trim())) put("gst_number", "GSTIN has 15 characters, e.g. 27ABCDE1234F1Z5")
        }
        if (errors.isNotEmpty()) return
        mySeq = action.seq + 1
        pendingAction = "save"
        if (isNew) {
            viewModel.createSupplier(
                SupplierCreateRequest(
                    name = name.trim(),
                    contactPhone = phone.trim().ifBlank { null },
                    email = email.trim().ifBlank { null },
                    gstNumber = gst.trim().ifBlank { null },
                    address = address.trim().ifBlank { null },
                )
            )
        } else if (supplier != null) {
            viewModel.updateSupplier(
                supplier.supplierId,
                SupplierUpdateRequest(
                    name = name.trim(),
                    contactPhone = phone.trim().takeIf { it != supplier.contactPhone.orEmpty() },
                    email = email.trim().takeIf { it != supplier.email.orEmpty() },
                    gstNumber = gst.trim().takeIf { it != supplier.gstNumber.orEmpty() },
                    address = address.trim().takeIf { it != supplier.address.orEmpty() },
                )
            )
        }
    }

    AppScreenScaffold(
        title = if (isNew) "Add supplier" else "Supplier",
        onBack = { navController.popBackStack() },
        bottomBar = {
            if (isNew || supplier != null) Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                ShadeButton(
                    text = if (isNew) "Add supplier" else "Save changes", onClick = ::save,
                    enabled = dirty && (isNew || supplier?.isActive == true), isLoading = busy && pendingAction == "save",
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        },
    ) { inner ->
        Box(Modifier.padding(inner).fillMaxSize()) {
            val d = detail
            when {
                !isNew && supplier == null && d is UiState.Error -> ErrorState(d.error.userMessage(), { viewModel.getSupplier(supplierId, force = true) })
                !isNew && supplier == null -> LoadingState()
                else -> FormScaffold {
                    if (supplier != null) {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(supplier.name, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            if (supplier.isActive) ToneChip("Active", Tone.Success) else ToneChip("Inactive", Tone.Danger)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            InfoTile("Supplier ID", supplier.supplierId, Modifier.weight(1f), Icons.Outlined.Badge)
                            InfoTile("Updated", supplier.updatedAt.dateTimeLabel(), Modifier.weight(1f))
                        }
                        if (!supplier.isActive) Text(
                            "Inactive suppliers cannot be edited or picked for new stock.", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    val editable = !busy && (isNew || supplier?.isActive == true)
                    DetailCard(title = "Supplier details", icon = Icons.Outlined.LocalShipping) {
                        GsTextField(
                            value = name, onValueChange = { name = it; errors = errors - "name" }, label = { Text("Name *") },
                            singleLine = true, enabled = editable,
                            isError = errors["name"] != null, supportingText = errors["name"]?.let { { Text(it) } },
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        GsTextField(
                            value = phone, onValueChange = { phone = it.filter { c -> c.isDigit() || c == '+' }.take(15); errors = errors - "contact_phone" },
                            label = { Text("Contact phone") }, leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null) },
                            singleLine = true, enabled = editable,
                            isError = errors["contact_phone"] != null, supportingText = errors["contact_phone"]?.let { { Text(it) } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        GsTextField(
                            value = email, onValueChange = { email = it; errors = errors - "email" },
                            label = { Text("Email") }, leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                            singleLine = true, enabled = editable,
                            isError = errors["email"] != null, supportingText = errors["email"]?.let { { Text(it) } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, autoCorrectEnabled = false, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        // GST: Ascii + CAPS (Part 3 §3).
                        GsTextField(
                            value = gst, onValueChange = { gst = it.uppercase().filter { c -> c.isLetterOrDigit() }.take(15); errors = errors - "gst_number" },
                            label = { Text("GSTIN") }, leadingIcon = { Icon(Icons.Outlined.Receipt, contentDescription = null) },
                            singleLine = true, enabled = editable,
                            isError = errors["gst_number"] != null,
                            supportingText = { Text(errors["gst_number"] ?: "${gst.length}/15") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Ascii, capitalization = KeyboardCapitalization.Characters,
                                autoCorrectEnabled = false, imeAction = ImeAction.Next,
                            ),
                            keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        GsTextField(
                            value = address, onValueChange = { address = it; errors = errors - "address" },
                            label = { Text("Address") }, leadingIcon = { Icon(Icons.Outlined.Home, contentDescription = null) },
                            minLines = 2, maxLines = 4, enabled = editable,
                            isError = errors["address"] != null, supportingText = errors["address"]?.let { { Text(it) } },
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { if (dirty) save() else focus.clearFocus() }),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    if (mine && action.error != null && errors.isEmpty()) AuthFormError(action.error)
                    if (supplier != null) {
                        if (supplier.isActive) ShadeOutlinedButton(
                            text = "Deactivate supplier", onClick = { confirmDeactivate = true }, modifier = Modifier.fillMaxWidth(),
                            enabled = !busy, contentColor = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }

    if (confirmDeactivate && supplier != null) {
        ConfirmDialog(
            title = "Deactivate supplier?",
            message = "${supplier.name} will be hidden from new stock entries. This cannot be undone from the app.",
            confirmText = "Deactivate",
            onConfirm = {
                confirmDeactivate = false
                mySeq = action.seq + 1
                pendingAction = "deactivate"
                viewModel.deactivateSupplier(supplier.supplierId)
            },
            onDismiss = { confirmDeactivate = false },
        )
    }
}
