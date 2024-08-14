package com.example.footballfieldtracker.ui.layout.screens

import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

// Todo: Validacija inputa (pogledaj CampLife kako izvrsila validaciju) i proveri da li radi
// Todo: Refactor ovo u view model da prezivi configuration changes
@Composable
fun RegisterScreen(navController: NavController) {
    // Definišemo state za sve inpute
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }
    val username = remember { mutableStateOf("") }
    val firstName = remember { mutableStateOf("") }
    val lastName = remember { mutableStateOf("") }
    val phoneNumber = remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }


    val passwordVisible = remember { mutableStateOf(false) }
    val confirmPasswordVisible = remember { mutableStateOf(false) }

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
                value = username.value,
                onValueChange = { username.value = it },
                label = { Text(text = "Username") },
                placeholder = { Text(text = "Username") },
                singleLine = true,
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
                value = firstName.value,
                onValueChange = { firstName.value = it },
                label = { Text(text = "First Name") },
                placeholder = { Text(text = "First Name") },
                singleLine = true,
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
                value = lastName.value,
                onValueChange = { lastName.value = it },
                label = { Text(text = "Last Name") },
                placeholder = { Text(text = "Last Name") },
                singleLine = true,
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
                value = phoneNumber.value,
                onValueChange = { phoneNumber.value = it },
                label = { Text(text = "Phone Number") },
                placeholder = { Text(text = "Phone Number") },
                singleLine = true,
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
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text(text = "Email address") },
                placeholder = { Text(text = "Email address") },
                singleLine = true,
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
                value = password.value,
                onValueChange = { password.value = it },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                        Icon(
                            painterResource( if (passwordVisible.value)R.drawable.visibility_24 else R.drawable.visibility_off_24),
                            contentDescription = "Password visibility",
                        )
                    }
                },
                label = { Text(text = "Password") },
                placeholder = { Text(text = "Password") },
                singleLine = true,
                visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
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
                value = confirmPassword.value,
                onValueChange = { confirmPassword.value = it },
                trailingIcon = {
                    IconButton(onClick = {
                        confirmPasswordVisible.value = !confirmPasswordVisible.value
                    }) {
                        Icon(
                            painterResource( if (passwordVisible.value)R.drawable.visibility_24 else R.drawable.visibility_off_24),
                            contentDescription = "Password visibility"
                        )
                    }
                },
                label = { Text(text = "Confirm Password") },
                placeholder = { Text(text = "Confirm Password") },
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.padding(12.dp))

            ProfileImage(imageUri) { newUri ->
                imageUri = newUri // Ažurira sliku kada korisnik izabere novu
            }

            Spacer(modifier = Modifier.padding(12.dp))
            val context = LocalContext.current
            Button(
                onClick = {
                    if (password.value == confirmPassword.value) {
                        // Kreiranje korisnika u Firebase Auth
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                            email.value,
                            password.value
                        ).addOnSuccessListener {
                            val userId = FirebaseAuth.getInstance().currentUser?.uid
                            val user = mapOf(
                                "id" to userId,
                                "email" to email.value,
                                "username" to username.value,
                                "firstName" to firstName.value,
                                "lastName" to lastName.value,
                                "phoneNumber" to phoneNumber.value,
                                "score" to 0,
                                "likedReviews" to mutableListOf<String>(),
                                "photoPath" to ""
                            )

                            if (userId != null && imageUri != null) {
                                FirebaseFirestore.getInstance().collection("users").document(userId)
                                    .set(user)
                                    .addOnSuccessListener {
                                        // Uspešno kreiran korisnik u Firestore
                                        updateProfilePicture(imageUri!!) {
                                            navController.navigate(
                                                Screens.Login.name
                                            )
                                        }
                                    }
                                    .addOnFailureListener {
                                        // Handle Firestore error
                                    }
                            }
                        }.addOnFailureListener {
                            // Handle authentication error
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
    }
}

private fun updateProfilePicture(uri: Uri, onNavigateToLogin: () -> Unit) {
    val riversRef =
        FirebaseStorage.getInstance().getReference("profile_pictures/${FirebaseAuth.getInstance().currentUser?.uid.toString()}")
    val uploadTask = riversRef.putFile(uri)

// Register observers to listen for when the download is done or if it fails
    uploadTask.addOnFailureListener {
        // Handle unsuccessful uploads
    }.addOnSuccessListener { taskSnapshot ->
        // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
        taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
            FirebaseFirestore.getInstance().collection("users")
                .document(FirebaseAuth.getInstance().currentUser?.uid.toString())
                .update("photoPath", uri.toString())
                .addOnSuccessListener {
                    // Navigiraj na drugi ekran nakon uspešnog ažuriranja
                    onNavigateToLogin()
                }
                .addOnFailureListener { e ->
                    // Handle failure in Firestore update
                }
        }

    }
}