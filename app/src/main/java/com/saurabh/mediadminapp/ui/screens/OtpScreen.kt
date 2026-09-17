package com.saurabh.mediadminapp.ui.screens


import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import kotlinx.coroutines.delay


@Preview
@Composable
private fun sdsf() {
//    OtpScreen( navController = NavHostController(LocalContext.current))

}

@Composable
fun OtpScreen(adminId:String, viewModel: MyViewModel, navController: NavHostController) {
    val context = LocalContext.current
    val otp = remember { mutableStateOf("") }
    val phoneNumber = remember { mutableStateOf("") }
    var isResendEnabled by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableStateOf(60) }

    val state = viewModel.verifyOtpState.collectAsState().value

    LaunchedEffect(state.error) {
        if (state.error != null) {
            Log.d("TAG", "OtpScreen error: ${state.error.toString()} ")
            Toast.makeText(context, state.error.toString(), Toast.LENGTH_LONG).show()
            viewModel.clearVerifyOtpState()
        }
    }

    LaunchedEffect(state.success) {
        state.success?.let { successResponse ->
            if (successResponse.status == 200 && successResponse.access_token != null) {
                // Navigation is handled by NavApp's LaunchedEffect(isLoggedIn) which fires
                // automatically when setAdminLoggedIn() is called in verifyAdminOtp().
                // DO NOT navigate here — dual navigation causes NavGraph crash.
                Log.d("TAG", "OtpScreen: OTP verified. access_token=${successResponse.access_token?.take(20)}...")
                Log.d("TAG", "OtpScreen: refresh_token received: ${successResponse.refresh_token != null}")
                Log.d("TAG", "OtpScreen: role=${successResponse.role}, status=${successResponse.status}")
                Log.d("TAG", "OtpScreen: Tokens saved. Waiting for NavApp to redirect to HomeRoutes.")
                Toast.makeText(context, "Successfully Verified", Toast.LENGTH_SHORT).show()
                viewModel.clearVerifyOtpState()
            } else {
                val errMsg = successResponse.message ?: "Invalid OTP"
                Log.d("TAG", "OtpScreen backend error: $errMsg (status: ${successResponse.status})")
                Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show()
                viewModel.clearVerifyOtpState()
            }
        }
    }

    if (state.isLoading) {
        Scaffold { innerpadding->
            Column (modifier = Modifier
                .fillMaxSize()
                .padding(innerpadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally){
                CircularProgressIndicator()
            }
        }
        return
    }
    // Timer for resend OTP
    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
        isResendEnabled = true
    }

    LaunchedEffect(state.success) {
        if(state.success != null){
            Log.d("TAG", "OtpScreen: OTP verified successfully , navigating to product screen")
            navController
        }
    }

    Scaffold(modifier = Modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            Text(
                text = "OTP Verification",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2196F3)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Enter the verification code sent to",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = phoneNumber.value.ifEmpty { "+91 XXXXXXXXXX" },
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(32.dp))

            // OTP Input Field
            OutlinedTextField(
                value = otp.value,
                onValueChange = {
                    if (it.length <= 6) otp.value = it
                },
                label = { Text("Enter OTP") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = "OTP")
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    focusedLabelColor = Color(0xFF2196F3)
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Timer/Resend Text
            Text(
                text = if (isResendEnabled) "Didn't receive code?" else "Resend code in ${timeLeft}s",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Verify Button
            Button(
                onClick = {
                    if (otp.value.length == 6) {
                        // Call your verify OTP function
                        viewModel.verifyAdminOtp(adminId, otp.value)
                        Toast.makeText(context, "Verifying OTP...", Toast.LENGTH_SHORT).show()
                        // Navigate to next screen on success
                        // navController.navigate(Routes.HomeRoutes.route)
                    } else {
                        Toast.makeText(context, "Please enter 6-digit OTP", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Verify OTP",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Resend Button
            TextButton(
                onClick = {
                    if (isResendEnabled) {
                        // Call resend OTP function
                        // viewModel.resendOtp(phoneNumber.value)
                        Toast.makeText(context, "OTP Resent!", Toast.LENGTH_SHORT).show()
                        isResendEnabled = false
                        timeLeft = 60
                    }
                },
                enabled = isResendEnabled
            ) {
                Text(
                    text = "Resend OTP",
                    color = if (isResendEnabled) Color(0xFF2196F3) else Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            HorizontalDivider(modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(16.dp))

            // Back to Sign In
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Wrong number? ",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                TextButton(
                    onClick = { navController.navigateUp() }
                ) {
                    Text(
                        text = "Go Back",
                        color = Color(0xFF2196F3),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}