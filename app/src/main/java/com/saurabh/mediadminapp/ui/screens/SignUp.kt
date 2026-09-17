package com.saurabh.mediadminapp.ui.screens


import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.ui.screens.nav.Routes

@Composable
fun SignUp(viewModel: MyViewModel, navController: NavHostController) {
    val contex = LocalContext.current
    var state = viewModel.createAdminState.collectAsState().value
    val name = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val phoneNumber = remember { mutableStateOf("") }
    val address = remember { mutableStateOf("") }
    val pinCode = remember { mutableStateOf("") }
    val context = LocalContext.current
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    when{
        state.isLoading->{
            Scaffold { innerpadding->
                Column (modifier = Modifier
                    .fillMaxSize()
                    .padding(innerpadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally){
                    CircularProgressIndicator()
                }
            }
        }
        state.error != null ->{
            Scaffold { innerpadding->
                Column (modifier = Modifier
                    .fillMaxSize()
                    .padding(innerpadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally){
                    Text(text = state.error.toString())
                    Toast.makeText(context, state.error.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }
        state.success != null ->{
            // lauched effect ensures on successfull user creation navigation to Signin screen
            LaunchedEffect(key1 = state) {
                Log.d("TAG", " SignUp success screen  move to signinscreen: ${state.success.message}")
                Toast.makeText(context, "Successfully SignedUp success screen move to signinscreen", Toast.LENGTH_SHORT).show()
                navController.navigate(Routes.SignInRoutes){
                    popUpTo( Routes.SignUpRoutes) { inclusive = true}
                }
            }
        }
    }

    Scaffold(modifier = Modifier.padding(8.dp)) { innerPadding ->
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .fillMaxSize(1f)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            //Name
            OutlinedTextField(
                value = name.value,
                onValueChange = { name.value = it },
                label = { Text("Name") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = "Name")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    focusedLabelColor = Color(0xFF2196F3)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Email
            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text("Email") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = "Email")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    focusedLabelColor = Color(0xFF2196F3)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            //Phone Number
            OutlinedTextField(
                value = phoneNumber.value,
                onValueChange = { phoneNumber.value = it },
                label = { Text("+91 Phone Number") },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = "Phone")
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    focusedLabelColor = Color(0xFF2196F3)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Password Field
            OutlinedTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation =if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    focusedLabelColor = Color(0xFF2196F3)
                ),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    val image = if (isPasswordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff
                    IconButton(onClick = {isPasswordVisible = !isPasswordVisible}) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Confirm Password Field
            OutlinedTextField(
                value = confirmPassword.value,
                onValueChange = { confirmPassword.value = it },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation =if (isConfirmPasswordVisible) VisualTransformation.None else  PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    focusedLabelColor = Color(0xFF2196F3)
                ),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    val image = if (isConfirmPasswordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff
                    IconButton(onClick = {isConfirmPasswordVisible = !isConfirmPasswordVisible}) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                if (password.value == confirmPassword.value) {
                    viewModel.createAdmin(name.value,email.value,password.value, phoneNumber.value)         }
                else {
                    Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                }


                Log.d("TAG", "SignUp message: ")
                Log.d("TAG", "SignUp status: ")
            }
            ) {
                Text(text = "Sign Up")
            }

            HorizontalDivider(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp))
            Text(text = "If u have an account click SignIn button")
            Button(onClick = { navController.navigate(Routes.SignInRoutes) }) {
                Text(text = "Sign In")
            }
        }
    }
}

