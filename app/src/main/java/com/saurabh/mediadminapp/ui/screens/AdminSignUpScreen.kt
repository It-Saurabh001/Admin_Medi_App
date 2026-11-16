package com.saurabh.mediadminapp.ui.screens

import com.saurabh.mediadminapp.ui.components.GsTextField
import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.data.v1.error.AppError
import com.saurabh.mediadminapp.data.v1.error.ErrorCode
import com.saurabh.mediadminapp.ui.screens.nav.Routes

private val PHONE_REGEX = Regex("^\\+?[0-9]{7,15}$")

/** Public admin signup (POST /admins/register). First admin = approved owner; later ones await approval. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSignUpScreen(viewModel: MyViewModel, navController: NavHostController) {
    val state by viewModel.registerAdminState.collectAsStateWithLifecycle()
    val config = remember { viewModel.adminSignupConfig() }
    val codeRequired = config?.codeRequired == true
    val focus = LocalFocusManager.current

    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var formError by rememberSaveable { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) { onDispose { viewModel.clearRegisterAdminState() } }

    fun backToLogin(message: String? = null) {
        runCatching {
            if (message != null) navController.getBackStackEntry(Routes.SignInRoutes.route).savedStateHandle[AUTH_MESSAGE_KEY] = message
        }
        if (!navController.popBackStack(Routes.SignInRoutes.route, inclusive = false)) {
            navController.navigate(Routes.SignInRoutes.route) { popUpTo(0) { inclusive = true }; launchSingleTop = true }
        }
    }

    LaunchedEffect(state.success) {
        val created = state.success ?: return@LaunchedEffect
        navController.navigate(Routes.VerifyEmailRoutes(created.email)) { launchSingleTop = true }
    }

    LaunchedEffect(state.error) {
        val msg = state.error ?: return@LaunchedEffect
        val api = state.appError as? AppError.Api
        if (api?.code == ErrorCode.EMAIL_DELIVERY_FAILED) {
            // Server rolled the signup back; the same email can be used again.
            formError = "Could not send the verification email. Please try again later."
            viewModel.clearRegisterAdminState()
            return@LaunchedEffect
        }
        val fields = api?.fields.orEmpty()
        errors = buildMap {
            fields["name"]?.let { put("name", it) }
            fields["email"]?.let { put("email", it) }
            fields["phone_number"]?.let { put("phone", it) }
            fields["password"]?.let { put("password", it) }
            fields["signup_code"]?.let { put("code", it) }
            if (api?.code == ErrorCode.FORBIDDEN) put("code", "Invalid signup code")
        }
        formError = when {
            api?.code == ErrorCode.RATE_LIMITED -> "Too many sign-up attempts. Try again later."
            api?.code == ErrorCode.FORBIDDEN && !codeRequired -> "Sign up is not allowed on this server. Ask the owner for a signup code."
            errors.isEmpty() -> msg
            else -> null
        }
    }

    fun submit() {
        if (state.isLoading) return
        val e = email.trim()
        errors = buildMap {
            if (name.isBlank()) put("name", "Enter your name")
            if (e.isEmpty()) put("email", "Enter your email")
            else if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) put("email", "Enter a valid email")
            if (phone.isNotBlank() && !PHONE_REGEX.matches(phone.trim())) put("phone", "7-15 digits, optional +")
            if (password.length < 8) put("password", "Minimum 8 characters")
            else if (password.length > 128) put("password", "Maximum 128 characters")
            if (confirm != password) put("confirm", "Passwords do not match")
            if (codeRequired && code.isBlank()) put("code", "Enter the signup code")
        }
        if (errors.isNotEmpty()) return
        focus.clearFocus()
        formError = null
        viewModel.registerAdmin(name, e, password, phone, code.takeIf { codeRequired || it.isNotBlank() })
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = { AuthTopBar("Create account") { navController.navigateUp() } }
    ) { inner ->
        val created = state.success
        if (created != null) {
            val approved = created.isApproved != false
            AuthFormColumn(modifier = Modifier.padding(inner)) {
                AuthHeader(
                    icon = if (approved) Icons.Outlined.VerifiedUser else Icons.Outlined.HourglassTop,
                    title = if (approved) "Account created" else "Awaiting approval",
                    subtitle = when {
                        created.isOwner == true -> "You are the first admin and the owner of this pharmacy. Sign in to continue."
                        approved -> "Your account is ready. Sign in to continue."
                        else -> "An existing admin must approve ${created.email} before you can sign in."
                    }
                )
                Spacer(Modifier.height(24.dp))
                AuthPrimaryButton(
                    text = "Back to sign in",
                    isLoading = false,
                    onClick = {
                        backToLogin(if (approved) "Account created. Sign in with ${created.email}." else "Account created. Awaiting approval by an admin.")
                    }
                )
            }
            return@Scaffold
        }

        AuthFormColumn(modifier = Modifier.padding(inner), verticalArrangement = Arrangement.Top) {
            AuthHeader(
                icon = Icons.Outlined.PersonAdd,
                title = "New admin account",
                subtitle = "The first account becomes the owner. Later accounts need approval by an admin."
            )
            Spacer(Modifier.height(24.dp))
            SignUpField(name, { name = it; errors = errors - "name" }, "Full name", Icons.Outlined.Person, errors["name"], KeyboardType.Text, !state.isLoading)
            SignUpField(email, { email = it; errors = errors - "email" }, "Email", Icons.Outlined.Email, errors["email"], KeyboardType.Email, !state.isLoading)
            SignUpField(phone, { phone = it; errors = errors - "phone" }, "Phone (optional)", Icons.Outlined.Phone, errors["phone"], KeyboardType.Phone, !state.isLoading)
            PasswordField(
                value = password,
                onValueChange = { password = it; errors = errors - "password" },
                label = "Password",
                error = errors["password"],
                hint = "8-128 characters",
                imeAction = ImeAction.Next
            )
            Spacer(Modifier.height(8.dp))
            PasswordField(
                value = confirm,
                onValueChange = { confirm = it; errors = errors - "confirm" },
                label = "Confirm password",
                error = errors["confirm"],
                imeAction = if (codeRequired) ImeAction.Next else ImeAction.Done,
                onDone = ::submit
            )
            if (codeRequired || errors["code"] != null) {
                Spacer(Modifier.height(8.dp))
                SignUpField(code, { code = it; errors = errors - "code" }, "Signup code", Icons.Outlined.Key, errors["code"], KeyboardType.Password, !state.isLoading, ImeAction.Done)
            }
            AuthFormError(formError)
            Spacer(Modifier.height(16.dp))
            AuthPrimaryButton(text = "Create account", isLoading = state.isLoading, onClick = ::submit)
            AuthFooterLink(prompt = "Already have an account?", action = "Sign in", onClick = { if (!state.isLoading) backToLogin() })
        }
    }
}

@Composable
private fun SignUpField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    error: String?,
    keyboardType: KeyboardType,
    enabled: Boolean,
    imeAction: ImeAction = ImeAction.Next,
) {
    GsTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        singleLine = true,
        enabled = enabled,
        isError = error != null,
        supportingText = error?.let { { SupportingText(it) } },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(8.dp))
}
