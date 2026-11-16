package com.saurabh.mediadminapp.ui.screens

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.data.v1.dto.Admin
import com.saurabh.mediadminapp.ui.components.AppScreenScaffold
import com.saurabh.mediadminapp.ui.components.ConfirmDialog
import com.saurabh.mediadminapp.ui.components.DetailCard
import com.saurabh.mediadminapp.ui.components.EmptyState
import com.saurabh.mediadminapp.ui.components.ErrorState
import com.saurabh.mediadminapp.ui.components.FormScaffold
import com.saurabh.mediadminapp.ui.components.GsTextField
import com.saurabh.mediadminapp.ui.components.InfoTile
import com.saurabh.mediadminapp.ui.components.LoadingState
import com.saurabh.mediadminapp.ui.components.PersonAvatar
import com.saurabh.mediadminapp.ui.components.ShadeButton
import com.saurabh.mediadminapp.ui.components.ShadeOutlinedButton
import com.saurabh.mediadminapp.ui.components.Tone
import com.saurabh.mediadminapp.ui.components.ToneChip
import com.saurabh.mediadminapp.ui.components.dateTimeLabel
import com.saurabh.mediadminapp.ui.theme.GS
import kotlinx.coroutines.delay

/** Route argument that opens the create form instead of an existing admin. */
const val NEW_ADMIN_ID = "new"

/** Admin detail + edit (existing id) or create admin ([NEW_ADMIN_ID]). Entry: Admins list. */
@Composable
fun AdminFormScreen(adminId: String, viewModel: MyViewModel, navController: NavController) {
    if (adminId == NEW_ADMIN_ID) CreateAdminForm(viewModel, navController)
    else AdminDetailForm(adminId, viewModel, navController)
}

// ───────────────────────────── Detail + edit ─────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AdminDetailForm(adminId: String, viewModel: MyViewModel, navController: NavController) {
    val listState by viewModel.getAllAdminState.collectAsState()
    val myId by viewModel.loggedInAdminId.collectAsState()
    val busyIds by viewModel.adminActionIds.collectAsState()
    val focus = LocalFocusManager.current
    LaunchedEffect(Unit) { viewModel.getAllAdmin() }

    val admin = listState.success?.items?.firstOrNull { it.adminId == adminId }
    val isMe = adminId == myId
    val isOwner = admin?.isOwner == true
    val pending = admin?.isApproved == false
    val busy = adminId in busyIds

    var name by rememberSaveable(admin?.adminId) { mutableStateOf(admin?.name.orEmpty()) }
    var email by rememberSaveable(admin?.adminId) { mutableStateOf(admin?.email.orEmpty()) }
    var phone by rememberSaveable(admin?.adminId) { mutableStateOf(admin?.phoneNumber.orEmpty()) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var saving by rememberSaveable { mutableStateOf(false) }
    var removing by rememberSaveable { mutableStateOf(false) }
    var confirmRemove by rememberSaveable { mutableStateOf(false) }

    // Keep the form in sync when the record first arrives.
    LaunchedEffect(admin?.adminId) {
        if (admin != null && name.isEmpty() && email.isEmpty()) { name = admin.name; email = admin.email; phone = admin.phoneNumber.orEmpty() }
    }
    val dirty = admin != null && (name.trim() != admin.name || email.trim() != admin.email || phone.trim() != admin.phoneNumber.orEmpty())

    // updateAdmin has no state: it reloads the list on success (snackbar from the VM). Saved = list shows our values.
    LaunchedEffect(saving, admin) {
        if (saving && admin != null && !dirty) saving = false
    }
    LaunchedEffect(saving) { if (saving) { delay(15_000); saving = false } } // error path: VM shows a snackbar
    // deleteAdmin removes the row from the list on success.
    LaunchedEffect(removing, admin, busy) {
        if (removing && admin == null) navController.popBackStack()
        else if (removing && !busy && admin != null) removing = false
    }

    fun save() {
        val a = admin ?: return
        focus.clearFocus()
        nameError = if (name.isBlank()) "Name is required" else null
        emailError = if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) "Enter a valid email" else null
        if (nameError != null || emailError != null) return
        saving = true
        viewModel.updateAdmin(
            adminId = a.adminId,
            name = name.trim().takeIf { it != a.name },
            email = email.trim().takeIf { it != a.email },
            phoneNumber = phone.trim().takeIf { it != a.phoneNumber.orEmpty() },
        )
    }

    AppScreenScaffold(
        title = if (isMe) "My admin account" else "Admin",
        onBack = { navController.popBackStack() },
        bottomBar = {
            if (admin != null) Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                Row(
                    Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (pending) {
                        ShadeOutlinedButton("Reject", { confirmRemove = true }, Modifier.weight(1f), enabled = !busy, icon = Icons.Outlined.Close, contentColor = GS.Red600)
                        ShadeButton("Approve", { viewModel.approveAdmin(adminId) }, Modifier.weight(1f), isLoading = busy, icon = Icons.Outlined.Check)
                    } else {
                        ShadeButton("Save changes", ::save, Modifier.fillMaxWidth(), enabled = dirty, isLoading = saving)
                    }
                }
            }
        },
    ) { inner ->
        Box(Modifier.padding(inner).fillMaxSize()) {
            when {
                admin == null && listState.isLoading -> LoadingState()
                admin == null && listState.error != null -> ErrorState(listState.error ?: "Could not load admins", { viewModel.getAllAdmin(force = true) })
                admin == null -> EmptyState("Admin not found", "This admin may have been removed.")
                else -> FormScaffold {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PersonAvatar(admin.imageUrl, admin.name, size = 88.dp)
                        Text(
                            admin.name + if (isMe) " (you)" else "", style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis,
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ToneChip(if (isOwner) "Owner" else "Admin", Tone.Info)
                            when {
                                pending -> ToneChip("Pending approval", Tone.Warning)
                                admin.role == "disabled" || !admin.isActive -> ToneChip("Disabled", Tone.Danger)
                                else -> ToneChip("Active", Tone.Success)
                            }
                        }
                    }

                    DetailCard(title = "Account", icon = Icons.Outlined.Badge) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            InfoTile("Admin ID", admin.adminId, Modifier.weight(1f))
                            InfoTile("Joined", admin.createdAt.dateTimeLabel(), Modifier.weight(1f), Icons.Outlined.CalendarMonth)
                        }
                    }

                    DetailCard(title = "Details", icon = Icons.Outlined.Person) {
                        GsTextField(
                            value = name, onValueChange = { name = it; nameError = null }, label = { Text("Full name") },
                            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) }, singleLine = true, enabled = !saving,
                            isError = nameError != null, supportingText = nameError?.let { { Text(it) } },
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        GsTextField(
                            value = email, onValueChange = { email = it; emailError = null }, label = { Text("Email") },
                            leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) }, singleLine = true, enabled = !saving,
                            isError = emailError != null, supportingText = emailError?.let { { Text(it) } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, autoCorrectEnabled = false, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        GsTextField(
                            value = phone, onValueChange = { phone = it.filter { c -> c.isDigit() || c == '+' }.take(15) }, label = { Text("Phone (optional)") },
                            leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null) }, singleLine = true, enabled = !saving,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { if (dirty) save() else focus.clearFocus() }),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            "Password and role changes are not available here.", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    // Owner and your own account cannot be removed (server rejects both).
                    if (!pending && !isOwner && !isMe) {
                        ShadeOutlinedButton(
                            "Remove admin", { confirmRemove = true }, Modifier.fillMaxWidth(),
                            enabled = !busy && !removing, contentColor = GS.Red600,
                        )
                    }
                }
            }
        }
    }

    if (confirmRemove && admin != null) {
        ConfirmDialog(
            title = if (pending) "Reject sign-up?" else "Remove admin?",
            message = if (pending) "${admin.name} (${admin.email}) will not be able to sign in."
            else "${admin.name} will lose access to the admin app.",
            confirmText = if (pending) "Reject" else "Remove",
            onConfirm = { confirmRemove = false; removing = true; viewModel.deleteAdmin(adminId) },
            onDismiss = { confirmRemove = false },
        )
    }
}

// ───────────────────────────── Create ─────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateAdminForm(viewModel: MyViewModel, navController: NavController) {
    val state by viewModel.createAdminState.collectAsState()
    val context = LocalContext.current
    val focus = LocalFocusManager.current

    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var formError by remember { mutableStateOf<String?>(null) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var sawLoading by rememberSaveable { mutableStateOf(false) }

    // createAdminState is never cleared by the VM: react only to the request this screen started.
    LaunchedEffect(state, submitted) {
        if (!submitted) return@LaunchedEffect
        when {
            state.isLoading -> sawLoading = true
            sawLoading && state.success != null -> {
                submitted = false
                Toast.makeText(context, "Admin ${state.success!!.name} created", Toast.LENGTH_LONG).show()
                viewModel.getAllAdmin(force = true)
                navController.popBackStack()
            }
            sawLoading && state.error != null -> {
                submitted = false
                val e = state.appError
                errors = listOf("name", "email", "phone_number", "password").mapNotNull { k -> e.fieldError(k)?.let { k to it } }.toMap()
                formError = if (errors.isEmpty()) state.error else null
            }
        }
    }

    fun submit() {
        focus.clearFocus()
        errors = buildMap {
            if (name.isBlank()) put("name", "Name is required")
            if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) put("email", "Enter a valid email")
            if (password.length < 8) put("password", "Use at least 8 characters")
            if (confirm != password) put("confirm", "Passwords do not match")
        }
        if (errors.isNotEmpty()) return
        formError = null
        sawLoading = false
        submitted = true
        viewModel.createAdmin(name.trim(), email.trim(), password, phone.trim())
    }
    val loading = submitted && state.isLoading

    AppScreenScaffold(
        title = "Add admin",
        onBack = { navController.popBackStack() },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                ShadeButton(
                    "Create admin", ::submit, isLoading = loading,
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        },
    ) { inner ->
        FormScaffold(Modifier.padding(inner)) {
            DetailCard(title = "New admin", icon = Icons.Outlined.Person) {
                Text(
                    "The new admin can sign in with this email and password.", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                GsTextField(
                    value = name, onValueChange = { name = it; errors = errors - "name" }, label = { Text("Full name") },
                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) }, singleLine = true, enabled = !loading,
                    isError = errors["name"] != null, supportingText = errors["name"]?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier.fillMaxWidth(),
                )
                GsTextField(
                    value = email, onValueChange = { email = it; errors = errors - "email" }, label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) }, singleLine = true, enabled = !loading,
                    isError = errors["email"] != null, supportingText = errors["email"]?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, autoCorrectEnabled = false, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier.fillMaxWidth(),
                )
                GsTextField(
                    value = phone, onValueChange = { phone = it.filter { c -> c.isDigit() || c == '+' }.take(15); errors = errors - "phone_number" },
                    label = { Text("Phone (optional)") }, leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null) },
                    singleLine = true, enabled = !loading,
                    isError = errors["phone_number"] != null, supportingText = errors["phone_number"]?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            DetailCard(title = "Password", icon = Icons.Outlined.Lock) {
                PasswordField(
                    value = password, onValueChange = { password = it; errors = errors - "password" },
                    label = "Password", error = errors["password"], hint = "At least 8 characters", imeAction = ImeAction.Next,
                )
                PasswordField(
                    value = confirm, onValueChange = { confirm = it; errors = errors - "confirm" },
                    label = "Confirm password", error = errors["confirm"], onDone = ::submit,
                )
            }
            AuthFormError(formError)
        }
    }
}
