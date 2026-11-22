package com.saurabh.mediadminapp.ui.screens

import com.saurabh.mediadminapp.ui.components.GsTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.saurabh.mediadminapp.ui.components.ShadeButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.ui.adaptive.AdaptiveContent
import com.saurabh.mediadminapp.ui.adaptive.AdaptiveWidths
import com.saurabh.mediadminapp.ui.components.AppScreenScaffold
import com.saurabh.mediadminapp.ui.components.ConfirmDialog
import com.saurabh.mediadminapp.ui.components.DetailCard
import com.saurabh.mediadminapp.ui.components.EmptyState
import com.saurabh.mediadminapp.ui.components.ErrorState
import com.saurabh.mediadminapp.ui.components.InfoTile
import com.saurabh.mediadminapp.ui.components.LoadingState
import com.saurabh.mediadminapp.ui.components.PersonAvatar
import com.saurabh.mediadminapp.ui.components.ShadeOutlinedButton
import com.saurabh.mediadminapp.ui.components.Tone
import com.saurabh.mediadminapp.ui.components.ToneChip
import com.saurabh.mediadminapp.ui.components.dateTimeLabel
import com.saurabh.mediadminapp.ui.theme.GS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: MyViewModel, navController: NavController) {
    LaunchedEffect(Unit) { viewModel.getAllAdmin() }

    val adminState by viewModel.getAllAdminState.collectAsState()
    val loggedInAdminId by viewModel.loggedInAdminId.collectAsState()
    val admin = adminState.success?.items?.find { it.adminId == loggedInAdminId }
    var confirmLogout by rememberSaveable { mutableStateOf(false) }
    var editing by rememberSaveable { mutableStateOf(false) }

    AppScreenScaffold(
        title = "My profile",
        onBack = { navController.popBackStack() },
        actions = {
            if (admin != null) com.saurabh.mediadminapp.ui.components.TileIconButton(Icons.Outlined.Edit, "Edit profile", { editing = true })
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                adminState.isLoading && adminState.success == null -> LoadingState()
                adminState.error != null && adminState.success == null -> ErrorState(
                    message = adminState.error ?: "Could not load profile",
                    onRetry = { viewModel.getAllAdmin(force = true) },
                )
                admin == null -> EmptyState(
                    title = "Profile unavailable",
                    message = "Your admin record was not found.",
                    action = { ShadeOutlinedButton("Log out", { confirmLogout = true }, contentColor = GS.Red600) },
                )
                else -> PullToRefreshBox(
                    isRefreshing = adminState.isLoading,
                    onRefresh = { viewModel.getAllAdmin(force = true) },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    AdaptiveContent(maxWidth = AdaptiveWidths.Form) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                        ) {
                            item(key = "identity") {
                                Column(
                                    Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    PersonAvatar(url = admin.imageUrl, name = admin.name, size = 96.dp)
                                    Text(
                                        admin.name, style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis,
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        ToneChip(if (admin.isOwner == true) "Owner" else admin.role.replaceFirstChar { it.uppercase() }, Tone.Info)
                                        ToneChip(if (admin.isActive) "Active" else "Disabled", if (admin.isActive) Tone.Success else Tone.Danger)
                                    }
                                }
                            }
                            item(key = "details") {
                                DetailCard(title = "Account details", icon = Icons.Outlined.Person) {
                                    InfoTile("Email", admin.email.ifBlank { "Not provided" }, icon = Icons.Outlined.Email)
                                    InfoTile("Phone", admin.phoneNumber.orEmpty().ifBlank { "Not provided" }, icon = Icons.Outlined.Phone)
                                    InfoTile("Admin ID", admin.adminId, icon = Icons.Outlined.Fingerprint)
                                    InfoTile("Member since", admin.createdAt.dateTimeLabel(), icon = Icons.Outlined.CalendarMonth)
                                }
                            }
                            item(key = "edit") {
                                ShadeButton("Edit profile", { editing = true }, Modifier.fillMaxWidth(), icon = Icons.Outlined.Edit)
                            }
                            item(key = "logout") {
                                ShadeOutlinedButton(
                                    text = "Log out", onClick = { confirmLogout = true },
                                    icon = Icons.AutoMirrored.Outlined.Logout, contentColor = GS.Red600,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (editing && admin != null) {
        EditAdminDialog(
            name0 = admin.name, email0 = admin.email, phone0 = admin.phoneNumber.orEmpty(),
            emailEditable = admin.googleLinked != true,
            onSave = { n, e, p ->
                editing = false
                viewModel.updateAdmin(
                    adminId = admin.adminId,
                    name = n.takeIf { it != admin.name },
                    email = e.takeIf { it != admin.email },
                    phoneNumber = p.takeIf { it != admin.phoneNumber.orEmpty() },
                )
            },
            onDismiss = { editing = false },
        )
    }

    if (confirmLogout) {
        ConfirmDialog(
            title = "Log out?",
            message = "You will need to sign in again to manage orders and stock.",
            confirmText = "Log out",
            onConfirm = { confirmLogout = false; viewModel.setAdminLoggedOut() },
            onDismiss = { confirmLogout = false },
        )
    }
}

/** Own admin record: name / email / phone (updateAdmin sends only changed fields). */
@Composable
private fun EditAdminDialog(name0: String, email0: String, phone0: String, emailEditable: Boolean, onSave: (String, String, String) -> Unit, onDismiss: () -> Unit) {
    var name by rememberSaveable { mutableStateOf(name0) }
    var email by rememberSaveable { mutableStateOf(email0) }
    var phone by rememberSaveable { mutableStateOf(phone0) }
    val nameError = if (name.isBlank()) "Name is required" else null
    val emailError = if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) "Enter a valid email" else null
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Edit profile", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GsTextField(
                    value = name, onValueChange = { name = it }, label = { Text("Full name") }, singleLine = true,
                    isError = nameError != null, supportingText = nameError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                )
                GsTextField(
                    value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true,
                    enabled = emailEditable,
                    isError = emailError != null,
                    supportingText = (emailError ?: "Linked to Google; email can't be changed".takeIf { !emailEditable })
                        ?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                )
                GsTextField(
                    value = phone, onValueChange = { phone = it }, label = { Text("Phone number") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            com.saurabh.mediadminapp.ui.components.LinkButton("Save", { onSave(name.trim(), email.trim(), phone.trim()) }, enabled = nameError == null && emailError == null)
        },
        dismissButton = { com.saurabh.mediadminapp.ui.components.NeutralTextButton("Cancel", onDismiss) },
    )
}
