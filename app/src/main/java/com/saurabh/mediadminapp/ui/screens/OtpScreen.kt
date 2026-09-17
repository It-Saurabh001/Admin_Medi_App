package com.saurabh.mediadminapp.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.saurabh.mediadminapp.MyViewModel
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

// ─────────────────────────────────────────────────────────────────────────────
// OTP Screen — Claymorphism design
//
// FIXES APPLIED:
// 1. Removed duplicate LaunchedEffect(state.success) — only one now handles
//    success navigation (was triggering double navController calls before).
// 2. Removed second Scaffold (loading branch) — single Box layout, no stacking.
// 3. OTP box input with individual digit cells for better UX.
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
    var otp by remember { mutableStateOf("") }
    var isResendEnabled by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableStateOf(60) }
    val state by viewModel.verifyOtpState.collectAsState()

    val screenState: OtpScreenState = when {
        state.isLoading -> OtpScreenState.LOADING
        state.success != null -> OtpScreenState.SUCCESS
        else -> OtpScreenState.FORM
    }

    // Countdown timer for resend
    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000L.milliseconds)
            timeLeft--
        }
        isResendEnabled = true
    }

    // ── Single error + success LaunchedEffect (removes the duplicate) ────────
    LaunchedEffect(state.error) {
        if (state.error != null) {
            Log.d("NAV", "OtpScreen error: ${state.error}")
            Toast.makeText(context, state.error.toString(), Toast.LENGTH_LONG).show()
            viewModel.clearVerifyOtpState()
        }
    }

    LaunchedEffect(state.success) {
        state.success?.let { response ->
            if (response.status == 200 && response.access_token != null) {
                Log.d("NAV", "OTP verified — waiting for NavApp isLoggedIn redirect to Home")
                Toast.makeText(context, "✓ Verified successfully!", Toast.LENGTH_SHORT).show()
                // Navigation to Home is handled by NavApp's LaunchedEffect(isLoggedIn)
                // which fires after viewModel.setAdminLoggedIn() is called inside verifyAdminOtp.
                // DO NOT navigate here — dual navigation causes NavGraph crash.
                viewModel.clearVerifyOtpState()
            } else {
                val msg = response.message ?: "Invalid OTP, please try again"
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
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

                        // ── Clay OTP Card ─────────────────────────────────
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 24.dp,
                                    shape = RoundedCornerShape(28.dp),
                                    ambientColor = OtpPrimary.copy(0.18f),
                                    spotColor = OtpSecondary.copy(0.18f)
                                )
                                .clip(RoundedCornerShape(28.dp))
                                .background(Color(0xFFFAF9FF))
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // ── OTP digit boxes ───────────────────────────
                            OtpInputField(
                                otp = otp,
                                onOtpChange = { if (it.length <= 6) otp = it }
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Timer
                            Text(
                                text = if (isResendEnabled) "Didn't receive the code?"
                                else "Resend code in ${timeLeft}s",
                                fontSize = 13.sp,
                                color = Color(0xFF888AAA),
                                textAlign = TextAlign.Center
                            )

                            if (isResendEnabled) {
                                TextButton(onClick = {
                                    isResendEnabled = false
                                    timeLeft = 60
                                    Toast.makeText(context, "OTP Resent!", Toast.LENGTH_SHORT).show()
                                }) {
                                    Text(
                                        "Resend OTP",
                                        color = OtpPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Verify button
                            ClayGradientButton(
                                text = "Verify OTP →",
                                isLoading = false,
                                onClick = {
                                    when {
                                        otp.length != 6 ->
                                            Toast.makeText(context, "Please enter all 6 digits", Toast.LENGTH_SHORT).show()
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
// OTP Digit Input — 6 individual styled boxes for a premium feel
// =============================================================================
@Composable
private fun OtpInputField(
    otp: String,
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
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(6) { index ->
                    val char = otp.getOrNull(index)
                    val isFocused = otp.length == index
                    OtpDigitCell(char = char, isFocused = isFocused)
                }
            }
        }
    )
}

@Composable
private fun OtpDigitCell(char: Char?, isFocused: Boolean) {
    val borderColor = when {
        isFocused -> Color(0xFF6C63FF)
        char != null -> Color(0xFF6C63FF).copy(0.4f)
        else -> Color(0xFFD0C8FF)
    }
    Box(
        modifier = Modifier
            .size(width = 44.dp, height = 54.dp)
            .shadow(
                elevation = if (char != null) 8.dp else 2.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = Color(0xFF6C63FF).copy(0.15f),
                spotColor = Color(0xFF6C63FF).copy(0.15f)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF0EEFF))
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char?.toString() ?: "",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6C63FF)
        )
        // Cursor blink indicator when focused and no char yet
        if (isFocused && char == null) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(24.dp)
                    .background(Color(0xFF6C63FF))
            )
        }
    }
}