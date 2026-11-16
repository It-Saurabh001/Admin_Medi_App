package com.saurabh.mediadminapp.ui.screens

import com.saurabh.mediadminapp.ui.components.GsTextField
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import com.saurabh.mediadminapp.utils.utilityFunctions.toImagePart

private const val SIGNUP_MIN_PASSWORD = 8

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUp(viewModel: MyViewModel, navController: NavHostController) {
    val state by viewModel.createAdminState.collectAsState()
    val context = LocalContext.current

    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }
    var formError by remember { mutableStateOf<String?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) imageUri = uri }

    fun goToSignIn(message: String? = null) {
        val signIn = runCatching { navController.getBackStackEntry(Routes.SignInRoutes.route) }.getOrNull()
        if (signIn != null) {
            if (message != null) signIn.savedStateHandle[AUTH_MESSAGE_KEY] = message
            navController.popBackStack(Routes.SignInRoutes.route, inclusive = false)
        } else {
            navController.navigate(Routes.SignInRoutes.route) {
                popUpTo(Routes.SignUpRoutes.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // Error -> field / form messages
    LaunchedEffect(state.error) {
        val error = state.error ?: return@LaunchedEffect
        Log.d("TAG", "SignUp: error : $error")
        val appError = state.appError
        nameError = appError.fieldError("name")
        emailError = appError.fieldError("email")
        phoneError = appError.fieldError("phone_number")
        passwordError = appError.fieldError("password")
        formError = if (listOf(nameError, emailError, phoneError, passwordError).all { it == null }) error else null
    }

    // Navigate on success
    LaunchedEffect(state.success) {
        if (state.success != null) {
            Log.d("NAV", "SignUp success — navigating to SignIn")
            goToSignIn("Account created. Please sign in.")
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = { AuthTopBar("Create account") { goToSignIn() } }
    ) { innerPadding ->
        AuthFormColumn(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.Top
        ) {
            // ── Profile photo (optional) ──
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = "Profile photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Outlined.AddAPhoto,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            com.saurabh.mediadminapp.ui.components.ShadeOutlinedButton(
                text = if (imageUri == null) "Add photo (optional)" else "Change photo",
                onClick = { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Set up an admin account for your pharmacy.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            GsTextField(
                value = name,
                onValueChange = { name = it; nameError = null; formError = null },
                label = { Text("Full name") },
                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true,
                isError = nameError != null,
                supportingText = nameError?.let { { SupportingText(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            GsTextField(
                value = email,
                onValueChange = { email = it; emailError = null; formError = null },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                singleLine = true,
                isError = emailError != null,
                supportingText = emailError?.let { { SupportingText(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            GsTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it; phoneError = null; formError = null },
                label = { Text("Phone number") },
                leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                singleLine = true,
                isError = phoneError != null,
                supportingText = { SupportingText(phoneError, "Optional") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            PasswordField(
                value = password,
                onValueChange = { password = it; passwordError = null; formError = null },
                label = "Password",
                error = passwordError,
                hint = "Minimum 8 characters",
                imeAction = ImeAction.Next
            )
            Spacer(modifier = Modifier.height(8.dp))
            PasswordField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; confirmError = null },
                label = "Confirm password",
                error = confirmError
            )

            AuthFormError(formError)

            Spacer(modifier = Modifier.height(16.dp))
            AuthPrimaryButton(
                text = "Create account",
                isLoading = state.isLoading,
                onClick = {
                    Log.d("SignUp", "Create account clicked")
                    nameError = if (name.isBlank()) "Name is required" else null
                    emailError = if (!email.contains("@")) "Enter a valid email" else null
                    passwordError = if (password.length < SIGNUP_MIN_PASSWORD) "Minimum 8 characters" else null
                    confirmError = if (password != confirmPassword) "Passwords do not match" else null
                    if (listOf(nameError, emailError, passwordError, confirmError).all { it == null }) {
                        val imagePart = imageUri?.toImagePart(context)
                        viewModel.createAdmin(name.trim(), email.trim(), password, phoneNumber.trim(), imagePart)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))
            AuthFooterLink(
                prompt = "Already have an account?",
                action = "Sign in",
                onClick = { goToSignIn() }
            )
        }
    }
}
