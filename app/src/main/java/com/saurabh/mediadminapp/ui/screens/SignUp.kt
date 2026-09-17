package com.saurabh.mediadminapp.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.ui.screens.nav.Routes

// ─────────────────────────────────────────────────────────────────────────────
// SignUp — Claymorphism design + BUG-06 dual-Scaffold fix
//
// OLD BUG: when { state.isLoading -> Scaffold{...}; state.error -> Scaffold{...} }
// did NOT have early returns, so the main form Scaffold always rendered below,
// creating 2 full-screen Scaffolds stacked — the top one absorbed all touches.
//
// FIX: Single layout with AnimatedContent switching between Loading, Success,
// Error, and Form states. Zero stacked Scaffolds.
// ─────────────────────────────────────────────────────────────────────────────

private val SignUpGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFFF6584), Color(0xFF6C63FF))
)
private val SignUpPrimary = Color(0xFF6C63FF)
private val SignUpAccent = Color(0xFFFF6584)

private enum class SignUpScreen { FORM, LOADING, SUCCESS }

@Composable
fun SignUp(viewModel: MyViewModel, navController: NavHostController) {
    val state by viewModel.createAdminState.collectAsState()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Determine which screen state to show
    val screenState: SignUpScreen = when {
        state.isLoading -> SignUpScreen.LOADING
        state.success != null -> SignUpScreen.SUCCESS
        else -> SignUpScreen.FORM
    }

    Log.d("SignUp", "Current screen state: $screenState")
    // Error Toast
    LaunchedEffect(state.error) {
        if (state.error != null) {
            Toast.makeText(context, state.error.toString(), Toast.LENGTH_LONG).show()
            Log.d("TAG", "SignUp: error : ${state.error}")
        }
    }

    // Navigate on success
    LaunchedEffect(state.success) {
        if (state.success != null) {
            Log.d("NAV", "SignUp success — navigating to SignIn")
            Toast.makeText(context, "Account created! Please sign in.", Toast.LENGTH_SHORT).show()
            navController.navigate(Routes.SignInRoutes) {
                popUpTo(Routes.SignUpRoutes) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = SignUpGradient),
        contentAlignment = Alignment.Center
    ) {
        // Decorative blobs
        ClayBlob(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.TopStart)
                .padding(top = 8.dp),
            color = Color.White.copy(alpha = 0.10f)
        )
        ClayBlob(
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 8.dp),
            color = Color(0xFF48CAE4).copy(alpha = 0.18f)
        )

        // Single-state animated content — ZERO stacked Scaffolds
        AnimatedContent(
            targetState = screenState,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
            label = "signUpState"
        ) { screen ->
            when (screen) {
                SignUpScreen.LOADING -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 4.dp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Creating your account…",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                SignUpScreen.SUCCESS -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🎉", fontSize = 64.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Account Created!",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Redirecting to sign in…",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                SignUpScreen.FORM -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Logo badge
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .shadow(
                                    elevation = 16.dp,
                                    shape = CircleShape,
                                    ambientColor = SignUpAccent.copy(0.2f),
                                    spotColor = SignUpAccent.copy(0.2f)
                                )
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✨", fontSize = 32.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Create Account",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Join the MediAdmin family",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.82f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(28.dp))

                        // ── Clay Card ─────────────────────────────────────
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 24.dp,
                                    shape = RoundedCornerShape(28.dp),
                                    ambientColor = SignUpAccent.copy(0.15f),
                                    spotColor = SignUpPrimary.copy(0.15f)
                                )
                                .clip(RoundedCornerShape(28.dp))
                                .background(Color(0xFFFAF9FF))
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Name
                            ClayTextField(
                                value = name,
                                onValueChange = {
                                    name = it
                                    nameError = null
                                },
                                label = "Full Name",
                                leadingIcon = {
                                    Icon(Icons.Default.Person, null, tint = SignUpPrimary)
                                }
                            )
                            if (nameError != null) {
                                Text(
                                    text = nameError!!,
                                    color = Color(0xFFE53935),
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 4.dp, top = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))

                            // Email
                            ClayTextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    emailError = null
                                },
                                label = "Email address",
                                leadingIcon = {
                                    Icon(Icons.Default.Email, null, tint = SignUpPrimary)
                                },
                                keyboardType = KeyboardType.Email
                            )
                            if (emailError != null) {
                                Text(
                                    text = emailError!!,
                                    color = Color(0xFFE53935),
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 4.dp, top = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))

                            // Phone
                            ClayTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                label = "+91 Phone Number",
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, null, tint = SignUpPrimary)
                                },
                                keyboardType = KeyboardType.Phone
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            // Password
                            ClayTextField(
                                value = password,
                                onValueChange = {
                                    password = it
                                    passwordError = null
                                },
                                label = "Password",
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, null, tint = SignUpPrimary)
                                },
                                keyboardType = KeyboardType.Password,
                                visualTransformation = if (isPasswordVisible)
                                    VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                        Icon(
                                            if (isPasswordVisible) Icons.Default.Visibility
                                            else Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle",
                                            tint = SignUpPrimary
                                        )
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            // Confirm Password
                            ClayTextField(
                                value = confirmPassword,
                                onValueChange = {
                                    confirmPassword = it
                                    passwordError = null
                                },
                                label = "Confirm Password",
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, null, tint = SignUpPrimary)
                                },
                                keyboardType = KeyboardType.Password,
                                visualTransformation = if (isConfirmPasswordVisible)
                                    VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = {
                                        isConfirmPasswordVisible = !isConfirmPasswordVisible
                                    }) {
                                        Icon(
                                            if (isConfirmPasswordVisible) Icons.Default.Visibility
                                            else Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle",
                                            tint = SignUpPrimary
                                        )
                                    }
                                }
                            )
                            if (passwordError != null) {
                                Text(
                                    text = passwordError!!,
                                    color = Color(0xFFE53935),
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 4.dp, top = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Sign Up button
                            ClayGradientButton(
                                text = "Create Account →",
                                isLoading = false,
                                onClick = {
                                    Log.d("SignUp", "Sign Up button clicked for email: $email")
                                    var valid = true
                                    if (name.isBlank()) {
                                        nameError = "Name is required"
                                        valid = false
                                    }
                                    if (!email.contains("@")) {
                                        emailError = "Enter a valid email"
                                        valid = false
                                    }
                                    if (password != confirmPassword) {
                                        passwordError = "Passwords do not match"
                                        valid = false
                                    }
                                    if (password.length < 6) {
                                        passwordError = "Password must be at least 6 characters"
                                        valid = false
                                    }
                                    if (valid) {
                                        viewModel.createAdmin(
                                            name, email, password, phoneNumber
                                        )
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "Already have an account? ",
                                    color = Color(0xFF555577),
                                    fontSize = 14.sp
                                )
                                TextButton(onClick = {
                                    navController.navigate(Routes.SignInRoutes)
                                }) {
                                    Text(
                                        "Sign In",
                                        color = SignUpPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
