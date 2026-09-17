package com.saurabh.mediadminapp.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
// Claymorphism Design System — MediAdmin
// Palette:  Primary   #6C63FF (electric violet)
//           Secondary #48CAE4 (sky cyan)
//           Accent    #FF6584 (coral pink)
//           Surface   #FFFFFF / #F0EEFF (lavender tint)
//           BG Top    #6C63FF → #48CAE4 gradient
// ─────────────────────────────────────────────────────────────────────────────

private val ClayPrimary = Color(0xFF6C63FF)
private val ClaySecondary = Color(0xFF48CAE4)
private val ClayAccent = Color(0xFFFF6584)
private val ClayCardBg = Color(0xFFFAF9FF)
private val ClayFieldBg = Color(0xFFF0EEFF)
private val ClayBorder = Color(0xFFD0C8FF)
private val ClayGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF6C63FF), Color(0xFF48CAE4))
)
private val ClayCardShadow = Color(0xFF6C63FF).copy(alpha = 0.18f)

@Composable
fun SignIn(viewModel: MyViewModel, navController: NavHostController) {
    val state = viewModel.loginAdminState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Log.d("NAV", "SignIn composed — state: ${state.value}")

    // Error handling
    LaunchedEffect(state.value.error) {
        if (state.value.error != null) {
            Toast.makeText(context, state.value.error.toString(), Toast.LENGTH_LONG).show()
            Log.d("NAV", "SignIn: error : ${state.value.error}")
            viewModel.clearLoginState()
        }
    }

    // Success handling — navigate to OTP
    LaunchedEffect(state.value.success) {
        state.value.success?.let { response ->
            if (response.status == 200 && response.admin_id != null) {
                navController.navigate(Routes.VerifyOtpRoutes(userId = response.admin_id))
                viewModel.clearLoginState()
            } else {
                val msg = response.message ?: "Invalid email or password"
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                viewModel.clearLoginState()
            }
        }
    }

    // ── SINGLE Scaffold-free full-screen layout ──────────────────────────────
    // Auth screens deliberately skip Scaffold to avoid padding from the outer
    // shell (which has no top/bottom bars on auth routes anyway).
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = ClayGradient),
        contentAlignment = Alignment.Center
    ) {
        // Decorative blobs
        ClayBlob(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.TopEnd)
                .padding(top = 20.dp, end = 8.dp),
            color = Color.White.copy(alpha = 0.12f)
        )
        ClayBlob(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.BottomStart)
                .padding(bottom = 40.dp, start = 8.dp),
            color = ClayAccent.copy(alpha = 0.2f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Logo badge ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .shadow(
                        elevation = 18.dp,
                        shape = CircleShape,
                        ambientColor = ClayCardShadow,
                        spotColor = ClayCardShadow
                    )
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "💊",
                    fontSize = 36.sp
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Welcome Back!",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "Sign in to your admin dashboard",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.82f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Clay Card ────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = ClayCardShadow,
                        spotColor = ClayCardShadow
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(ClayCardBg)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Email field
                ClayTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email address",
                    leadingIcon = {
                        Icon(Icons.Default.Email, null, tint = ClayPrimary)
                    },
                    keyboardType = KeyboardType.Email
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password field
                ClayTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    leadingIcon = {
                        Icon(Icons.Default.Lock, null, tint = ClayPrimary)
                    },
                    keyboardType = KeyboardType.Password,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password",
                                tint = ClayPrimary
                            )
                        }
                    }
                )

                // Forgot password
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {}) {
                        Text(
                            text = "Forgot Password?",
                            color = ClayPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Sign In button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = ClayPrimary.copy(0.3f),
                            spotColor = ClayPrimary.copy(0.3f)
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(ClayPrimary, ClaySecondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.value.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Button(
                            onClick = { viewModel.loginAdmin(email, password) },
                            modifier = Modifier.fillMaxSize(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "Sign In →",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "New admin? ",
                        color = Color(0xFF555577),
                        fontSize = 14.sp
                    )
                    TextButton(onClick = { navController.navigate(Routes.SignUpRoutes) }) {
                        Text(
                            text = "Create account",
                            color = ClayPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// Shared Claymorphism components — used across auth screens
// =============================================================================

@Composable
internal fun ClayTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color(0xFF888AAA), fontSize = 13.sp) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ClayPrimary,
            unfocusedBorderColor = ClayBorder,
            focusedContainerColor = ClayFieldBg,
            unfocusedContainerColor = ClayFieldBg,
            focusedLabelColor = ClayPrimary,
            unfocusedLabelColor = Color(0xFF888AAA),
            cursorColor = ClayPrimary
        )
    )
}

@Composable
internal fun ClayBlob(modifier: Modifier = Modifier, color: Color) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
internal fun ClayGradientButton(
    text: String,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isLoading) 0.97f else 1f,
        animationSpec = tween(150),
        label = "buttonScale"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = ClayPrimary.copy(0.28f),
                spotColor = ClayPrimary.copy(0.28f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(listOf(ClayPrimary, ClaySecondary))
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(visible = isLoading, enter = fadeIn(), exit = fadeOut()) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(28.dp),
                strokeWidth = 3.dp
            )
        }
        AnimatedVisibility(
            visible = !isLoading,
            enter = scaleIn() + fadeIn(),
            exit = fadeOut()
        ) {
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxSize(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
