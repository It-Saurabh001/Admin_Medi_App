package com.saurabh.mediadminapp.ui.screens

import com.saurabh.mediadminapp.ui.components.GsTextField
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.data.v1.dto.User as UserItem
import com.saurabh.mediadminapp.ui.components.AppScreenScaffold
import com.saurabh.mediadminapp.ui.components.DetailCard
import com.saurabh.mediadminapp.ui.components.EmptyState
import com.saurabh.mediadminapp.ui.components.FormScaffold
import com.saurabh.mediadminapp.ui.components.LoadingState
import com.saurabh.mediadminapp.ui.components.PersonAvatar
import com.saurabh.mediadminapp.ui.components.ShadeButton
import com.saurabh.mediadminapp.ui.components.clearFocusOnTap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateUserDetailsScreen(user_id: String, viewModel: MyViewModel, navController: NavController) {
    val usersState by viewModel.getAllUserState.collectAsState()
    val user = usersState.success?.items?.find { it.userId == user_id }

    LaunchedEffect(Unit) {
        if (usersState.success == null) viewModel.getAllUsers()
    }

    when {
        user != null -> UpdateUserForm(user, viewModel, navController)
        usersState.isLoading || usersState.success == null && usersState.error == null ->
            AppScreenScaffold(title = "Edit user", onBack = { navController.popBackStack() }) { padding ->
                Box(Modifier.padding(padding).fillMaxSize()) { LoadingState() }
            }
        else -> AppScreenScaffold(title = "Edit user", onBack = { navController.popBackStack() }) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                EmptyState(title = "User not found", message = "This user may have been deleted.")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpdateUserForm(user: UserItem, viewModel: MyViewModel, navController: NavController) {
    val focus = LocalFocusManager.current
    val updateState by viewModel.updateUserState.collectAsState()

    var name by remember(user.userId) { mutableStateOf(user.name) }
    var email by remember(user.userId) { mutableStateOf(user.email) }
    var phone by remember(user.userId) { mutableStateOf(user.phoneNumber.orEmpty()) }
    var address by remember(user.userId) { mutableStateOf(user.address.orEmpty()) }
    var pinCode by remember(user.userId) { mutableStateOf(user.pinCode.orEmpty()) }
    var submitted by remember { mutableStateOf(false) }
    var sawLoading by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }

    // updateUserState is not cleared by the VM: only react to the request this screen started.
    LaunchedEffect(updateState, submitted) {
        if (!submitted) return@LaunchedEffect
        if (updateState.isLoading) sawLoading = true
        else if (sawLoading && updateState.success != null) navController.popBackStack()
        else if (sawLoading) submitted = false
    }

    val saving = submitted && updateState.isLoading
    val serverError = if (submitted) null else updateState.takeIf { sawLoading }?.appError
    val formError = updateState.takeIf { sawLoading && !submitted }?.error

    val save: () -> Unit = {
        focus.clearFocus()
        nameError = if (name.isBlank()) "Name is required" else null
        emailError = if (email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) "Enter a valid email" else null
        if (nameError == null && emailError == null) {
            sawLoading = false
            submitted = true
            viewModel.updateUser(
                userId = user.userId,
                name = name.trim(),
                email = email.trim(),
                phonenumber = phone.trim(),
                address = address.trim(),
                pincode = pinCode.trim(),
            )
        }
    }

    AppScreenScaffold(
        title = "Edit user",
        onBack = { navController.popBackStack() },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                ShadeButton(
                    text = "Save changes", onClick = save, isLoading = saving,
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        },
    ) { padding ->
        FormScaffold(Modifier.padding(padding).clearFocusOnTap()) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                PersonAvatar(url = user.imageUrl, name = user.name, size = 80.dp)
            }

            DetailCard(title = "Personal details", icon = Icons.Outlined.Person) {
                GsTextField(
                    value = name, onValueChange = { name = it; nameError = null },
                    label = { Text("Full name") },
                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                    singleLine = true, enabled = !saving,
                    isError = nameError != null || serverError.fieldError("name") != null,
                    supportingText = (nameError ?: serverError.fieldError("name"))?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier.fillMaxWidth(),
                )
                GsTextField(
                    value = email, onValueChange = { email = it; emailError = null },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                    singleLine = true, enabled = !saving,
                    isError = emailError != null || serverError.fieldError("email") != null,
                    supportingText = (emailError ?: serverError.fieldError("email"))?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier.fillMaxWidth(),
                )
                GsTextField(
                    value = phone, onValueChange = { phone = it },
                    label = { Text("Phone number") },
                    leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null) },
                    singleLine = true, enabled = !saving,
                    isError = serverError.fieldError("phone_number") != null,
                    supportingText = serverError.fieldError("phone_number")?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            DetailCard(title = "Address", icon = Icons.Outlined.Home) {
                GsTextField(
                    value = address, onValueChange = { address = it },
                    label = { Text("Street address") },
                    leadingIcon = { Icon(Icons.Outlined.Home, contentDescription = null) },
                    minLines = 2, maxLines = 4, enabled = !saving,
                    isError = serverError.fieldError("address") != null,
                    supportingText = serverError.fieldError("address")?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier.fillMaxWidth(),
                )
                GsTextField(
                    value = pinCode, onValueChange = { pinCode = it.filter(Char::isDigit).take(6) },
                    label = { Text("Pincode") },
                    leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null) },
                    singleLine = true, enabled = !saving,
                    isError = serverError.fieldError("pin_code") != null,
                    supportingText = serverError.fieldError("pin_code")?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { save() }),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (formError != null) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(formError, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(16.dp)) }
            }
        }
    }
}

@Composable
fun UpdateUserDetailsScreen1(user_id: String, viewModel: MyViewModel, navController: NavController) {
    UpdateUserDetailsScreen(user_id = user_id, viewModel = viewModel, navController = navController)
}
