package com.saurabh.mediadminapp.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.ui.screens.components.ClayOutlinedButton
import com.saurabh.mediadminapp.ui.screens.components.ClayPrimaryButton
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import com.saurabh.mediadminapp.ui.theme.ClayError
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

// ─────────────────────────────────────────────────────────────────────────────
// OTP Screen — Production-Grade Claymorphism with Focus & Shake Error Feedback
// ─────────────────────────────────────────────────────────────────────────────

private val OtpGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF48CAE4), Color(0xFF6C63FF))
)
private val OtpPrimary = Color(0xFF6C63FF)
private val OtpSecondary = Color(0xFF48CAE4)

private enum class OtpScreenState { FORM, LOADING, SUCCESS }

@Composable
fun OtpScreen(adminId: String, viewModel: MyViewModel, navController: NavHostController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var otp by remember { mutableStateOf("") }
    var isResendEnabled by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableIntStateOf(60) }
    var hasError by remember { mutableStateOf(false) }
    val shakeOffset = remember { Animatable(0f) }
    val state by viewModel.verifyOtpState.collectAsState()

    val screenState: OtpScreenState = when {
        state.isLoading -> OtpScreenState.LOADING
        state.success != null -> OtpScreenState.SUCCESS
        else -> OtpScreenState.FORM
    }

    // Countdown timer for resend
    LaunchedEffect(isResendEnabled) {
        if (!isResendEnabled) {
            timeLeft = 60
            while (timeLeft > 0) {
                delay(1000L.milliseconds)
                timeLeft--
            }
            isResendEnabled = true
        }
    }

    // Function to trigger shake animation
    fun triggerErrorShake() {
        hasError = true
        coroutineScope.launch {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    0f at 0
                    -12f at 50
                    12f at 100
                    -8f at 150
                    8f at 200
                    -4f at 250
                    4f at 300
                    0f at 400
                }
            )
        }
    }

    // Handle verification errors
    LaunchedEffect(state.error) {
        if (state.error != null) {
            Log.d("NAV", "OtpScreen error: ${state.error}")
            Toast.makeText(context, state.error.toString(), Toast.LENGTH_LONG).show()
            triggerErrorShake()
            viewModel.clearVerifyOtpState()
        }
    }

    // Handle verification response success/failure
    LaunchedEffect(state.success) {
        state.success?.let { response ->
            if (response.status == 200 && response.access_token != null) {
                Log.d("NAV", "OTP verified — waiting for NavApp isLoggedIn redirect to Home")
                Toast.makeText(context, "✓ Verified successfully!", Toast.LENGTH_SHORT).show()
                // Success स्क्रीन/एनीमेशन दिखाने के लिए 800ms का पॉज
                kotlinx.coroutines.delay(800)

                // पूरी ऑथ हिस्ट्री (SignIn, OTP) को हटाकर सीधे Home पर जाएं
                navController.navigate(Routes.HomeRoutes()) {
                    popUpTo(navController.graph.id) { inclusive = true }
                    launchSingleTop = true
                }
                viewModel.clearVerifyOtpState()
            } else {
                val msg = response.message ?: "Invalid OTP, please try again"
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                triggerErrorShake()
                viewModel.clearVerifyOtpState()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = OtpGradient),
        contentAlignment = Alignment.Center
    ) {
        // Decorative blobs
        ClayBlob(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.TopStart)
                .padding(top = 16.dp, start = 8.dp),
            color = Color.White.copy(alpha = 0.10f)
        )
        ClayBlob(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.BottomEnd)
                .padding(bottom = 48.dp, end = 16.dp),
            color = Color(0xFFFF6584).copy(alpha = 0.18f)
        )

        // Back button
        IconButton(
            onClick = { navController.navigateUp() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }

        AnimatedContent(
            targetState = screenState,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
            label = "otpState"
        ) { screen ->
            when (screen) {
                OtpScreenState.LOADING -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 4.dp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Verifying OTP…", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                OtpScreenState.SUCCESS -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("✅", fontSize = 72.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Verified!", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            Text("Taking you to your dashboard…", color = Color.White.copy(0.8f), fontSize = 14.sp)
                        }
                    }
                }

                OtpScreenState.FORM -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Shield badge
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .shadow(
                                    elevation = 18.dp,
                                    shape = CircleShape,
                                    ambientColor = OtpPrimary.copy(0.25f),
                                    spotColor = OtpSecondary.copy(0.25f)
                                )
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🛡️", fontSize = 38.sp)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            "OTP Verification",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Enter the 6-digit code sent to\nyour registered email",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.82f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // ── Clay OTP Card with Shake & Error Outline ───────────────
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                                .shadow(
                                    elevation = 24.dp,
                                    shape = RoundedCornerShape(28.dp),
                                    ambientColor = if (hasError) ClayError.copy(0.3f) else OtpPrimary.copy(0.18f),
                                    spotColor = if (hasError) ClayError.copy(0.3f) else OtpSecondary.copy(0.18f)
                                )
                                .clip(RoundedCornerShape(28.dp))
                                .background(Color(0xFFFAF9FF))
                                .border(
                                    width = if (hasError) 2.dp else 0.dp,
                                    color = if (hasError) ClayError else Color.Transparent,
                                    shape = RoundedCornerShape(28.dp)
                                )
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // ── OTP digit boxes ───────────────────────────
                            OtpInputField(
                                otp = otp,
                                hasError = hasError,
                                onOtpChange = { input ->
                                    val cleaned = input.filter { it.isDigit() }
                                    if (cleaned.length <= 6) {
                                        otp = cleaned
                                        if (hasError) hasError = false
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Formatted Countdown Timer
                            val minutes = timeLeft / 60
                            val seconds = timeLeft % 60
                            val formattedTime = "%02d:%02d".format(minutes, seconds)

                            Text(
                                text = if (isResendEnabled) "Didn't receive the code?"
                                else "Resend code in $formattedTime",
                                fontSize = 13.sp,
                                color = if (isResendEnabled) OtpPrimary else Color(0xFF888AAA),
                                fontWeight = if (isResendEnabled) FontWeight.SemiBold else FontWeight.Normal,
                                textAlign = TextAlign.Center
                            )

                            if (isResendEnabled) {
                                Spacer(modifier = Modifier.height(8.dp))
                                ClayOutlinedButton(
                                    text = "Resend OTP",
                                    onClick = {
                                        isResendEnabled = false
                                        Toast.makeText(context, "OTP Resent!", Toast.LENGTH_SHORT).show()
                                    },
                                    accentColor = OtpPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Verify button
                            ClayPrimaryButton(
                                text = "Verify OTP →",
                                isLoading = false,
                                onClick = {
                                    when {
                                        otp.length != 6 -> {
                                            Toast.makeText(context, "Please enter all 6 digits", Toast.LENGTH_SHORT).show()
                                            triggerErrorShake()
                                        }
                                        else -> {
                                            viewModel.verifyAdminOtp(adminId, otp)
                                        }
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        TextButton(onClick = { navController.navigateUp() }) {
                            Text(
                                "← Wrong account? Go back",
                                color = Color.White.copy(0.85f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// OTP Digit Input — 6 individual styled boxes with error & focus indicators
// =============================================================================
@Composable
private fun OtpInputField(
    otp: String,
    hasError: Boolean,
    onOtpChange: (String) -> Unit
) {
    BasicTextField(
        value = otp,
        onValueChange = onOtpChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        singleLine = true,
        textStyle = TextStyle(color = Color.Transparent, fontSize = 1.sp),
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(6) { index ->
                    val char = otp.getOrNull(index)
                    val isFocused = otp.length == index || (otp.length == 6 && index == 5)
                    OtpDigitCell(
                        char = char,
                        isFocused = isFocused,
                        hasError = hasError,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    )
}

@Composable
private fun OtpDigitCell(
    char: Char?,
    isFocused: Boolean,
    hasError: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        hasError -> ClayError
        isFocused -> Color(0xFF6C63FF)
        char != null -> Color(0xFF6C63FF).copy(0.5f)
        else -> Color(0xFFD0C8FF)
    }
    Box(
        modifier = modifier
            .height(52.dp)
            .shadow(
                elevation = if (char != null) 8.dp else 2.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = if (hasError) ClayError.copy(0.2f) else Color(0xFF6C63FF).copy(0.15f),
                spotColor = if (hasError) ClayError.copy(0.2f) else Color(0xFF6C63FF).copy(0.15f)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(if (hasError) Color(0xFFFFF0F2) else Color(0xFFF0EEFF))
            .border(
                width = if (isFocused || hasError) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char?.toString() ?: "",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = if (hasError) ClayError else Color(0xFF6C63FF)
        )
        // Cursor blink indicator when focused and no char yet
        if (isFocused && char == null && !hasError) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(24.dp)
                    .background(Color(0xFF6C63FF))
            )
        }
    }
}