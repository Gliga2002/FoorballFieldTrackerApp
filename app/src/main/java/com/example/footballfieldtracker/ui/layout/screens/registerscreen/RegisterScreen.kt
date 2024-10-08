package com.example.footballfieldtracker.ui.layout.screens.registerscreen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.footballfieldtracker.R
import com.example.footballfieldtracker.ui.Screens
import com.example.footballfieldtracker.ui.layout.util.ImagePicker
import com.example.footballfieldtracker.ui.viewmodels.RegisterViewModel


@Composable
fun RegisterScreen(
    registerViewModel: RegisterViewModel,
    navController: NavController
) {
    var isLoading by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .padding(10.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Register",
                style = TextStyle(fontWeight = FontWeight.Bold),
                fontSize = 40.sp
            )
            Spacer(modifier = Modifier.padding(20.dp))

            // Polje za username
            OutlinedTextField(
                value = registerViewModel.username,
                onValueChange = { registerViewModel.username = it },
                label = { Text(text = "Username") },
                placeholder = { Text(text = "Username") },
                singleLine = true,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            // Polje za first name
            OutlinedTextField(
                value = registerViewModel.firstName,
                onValueChange = { registerViewModel.firstName = it },
                label = { Text(text = "First Name") },
                placeholder = { Text(text = "First Name") },
                singleLine = true,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            // Polje za last name
            OutlinedTextField(
                value = registerViewModel.lastName,
                onValueChange = { registerViewModel.lastName = it },
                label = { Text(text = "Last Name") },
                placeholder = { Text(text = "Last Name") },
                singleLine = true,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            // Polje za phone number
            OutlinedTextField(
                value = registerViewModel.phoneNumber,
                onValueChange = { registerViewModel.phoneNumber = it },
                label = { Text(text = "Phone Number") },
                placeholder = { Text(text = "Phone Number") },
                singleLine = true,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            // Polje za email
            OutlinedTextField(
                value = registerViewModel.email,
                onValueChange = { registerViewModel.email = it },
                label = { Text(text = "Email address") },
                placeholder = { Text(text = "Email address") },
                singleLine = true,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            // Polje za password
            OutlinedTextField(
                value = registerViewModel.password,
                onValueChange = { registerViewModel.password = it },
                trailingIcon = {
                    IconButton(onClick = {
                        registerViewModel.passwordVisible = !registerViewModel.passwordVisible
                    }) {
                        Icon(
                            painterResource(if (registerViewModel.passwordVisible) R.drawable.visibility_24 else R.drawable.visibility_off_24),
                            contentDescription = "Password visibility",
                        )
                    }
                },
                label = { Text(text = "Password") },
                placeholder = { Text(text = "Password") },
                singleLine = true,
                enabled = !isLoading,
                visualTransformation = if (registerViewModel.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            // Polje za confirm password
            OutlinedTextField(
                value = registerViewModel.confirmPassword,
                onValueChange = { registerViewModel.confirmPassword = it },
                trailingIcon = {
                    IconButton(onClick = {
                        registerViewModel.confirmPasswordVisible =
                            !registerViewModel.confirmPasswordVisible
                    }) {
                        Icon(
                            painterResource(if (registerViewModel.confirmPasswordVisible) R.drawable.visibility_24 else R.drawable.visibility_off_24),
                            contentDescription = "Password visibility"
                        )
                    }
                },
                label = { Text(text = "Confirm Password") },
                placeholder = { Text(text = "Confirm Password") },
                singleLine = true,
                enabled = !isLoading,
                visualTransformation = if (registerViewModel.confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.padding(12.dp))

            ImagePicker(registerViewModel.imageUri, !isLoading) { newUri ->
                registerViewModel.imageUri = newUri // Ažurira sliku kada korisnik izabere novu
            }

            Spacer(modifier = Modifier.padding(12.dp))
            val context = LocalContext.current
            Button(
                onClick = {
                    isLoading = true
                    registerViewModel.registerUser()
                     { success, toastMsg ->
                        isLoading = false
                        if (success) {
                            navController.navigate(Screens.Login.name)
                            registerViewModel.resetState()
                            Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp),
            ) {
                Text(
                    text = "Register"
                )
            }

            Spacer(modifier = Modifier.padding(20.dp))
            Row {
                Text(
                    text = "Already have an account? ",
                )
                Text(
                    text = "Login",
                    modifier = Modifier.clickable(onClick = {
                        navController.navigate(Screens.Login.name)
                    }),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.padding(20.dp))
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(0.dp)) // Providna pozadina
                    .wrapContentSize(Alignment.Center)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp
                )
            }
        }
    }
}
