package com.example.footballfieldtracker.ui.layout.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.footballfieldtracker.R
import com.example.footballfieldtracker.data.model.User
import com.example.footballfieldtracker.ui.Screens
import com.example.footballfieldtracker.ui.viewmodels.CurrentUserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Validacija

@Composable
fun LoginScreen(navController: NavController, currentUserViewModel: CurrentUserViewModel) {
    val context = LocalContext.current
    val email = remember {
        mutableStateOf("")
    }
    val password = remember {
        mutableStateOf("")
    }

    val passwordVisible = remember {
        mutableStateOf(false)
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(10.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Login",
            style = TextStyle(fontWeight = FontWeight.Bold),
            fontSize = 40.sp
        )
        Spacer(modifier = Modifier.padding(20.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text(text = "Email address") },
                placeholder = { Text(text = "Email address") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            OutlinedTextField(
                value = password.value,
                onValueChange = { password.value = it },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                        Icon(
                            painterResource( if (passwordVisible.value)R.drawable.visibility_24 else R.drawable.visibility_off_24),
                            contentDescription = "Password visibility",
                            tint = if (passwordVisible.value) colorResource(id = R.color.purple_700) else Color.Gray
                        )
                    }
                },
                label = { Text(text = "Password") },
                placeholder = { Text(text = "Password") },
                singleLine = true,
                visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            Spacer(modifier = Modifier.padding(10.dp))
            Button(
                onClick = {
                    FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(email.value, password.value)
                        .addOnSuccessListener() {
                            // User login successful, retrieve user details from Firestore
                            val user = FirebaseAuth.getInstance().currentUser
                            val uid = user?.uid ?: ""

                            // Fetch user details from Firestore
                            FirebaseFirestore.getInstance().collection("users")
                                .document(uid)
                                .get()
                                .addOnSuccessListener { document ->
                                    if (document != null && document.exists()) {
                                        // User document exists, extract user data
                                        val userData = document.toObject(User::class.java)
                                        userData?.let { currentUser ->
                                            Log.d("UserData", "User ID: ${currentUser.id}")
                                            Log.d("UserData", "Email: ${currentUser.email}")
                                            Log.d("UserData", "Username: ${currentUser.username}")
                                            Log.d("UserData", "First Name: ${currentUser.firstName}")
                                            Log.d("UserData", "Last Name: ${currentUser.lastName}")
                                            Log.d("UserData", "Phone Number: ${currentUser.phoneNumber}")
                                            Log.d("UserData", "Score: ${currentUser.score}")
                                            Log.d("UserData", "Liked Reviews: ${currentUser.likedReviews.joinToString()}")
                                            Log.d("UserData", "Photo Path: ${currentUser.photoPath}")
                                            // Set the current user in the ViewModel
                                            currentUserViewModel.setCurrentUser(currentUser)
                                            navController.navigate(Screens.GoogleMap.name)
                                        }

                                    } else {
                                        // Handle if the user document doesn't exist
                                        Log.d("UserData", "No such document")

                                    }
                                }
                                .addOnFailureListener {
                                    // Handle any errors while fetching user details
                                    Log.e("UserData", "Error getting document", it)
                                }


                        }
                        .addOnFailureListener {
                            // TODO: STAVI TOST
                            Toast.makeText(context,it.message, Toast.LENGTH_SHORT).show()
                        }
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp),
            ) {
                Text(
                    text = "Login"
                )
            }
            Row {
                Text(
                    text = "Don't have an account? ",
                )
                Text(
                    text = "Register",
                    modifier = Modifier.clickable(onClick = {
                        navController.navigate(Screens.Register.name)
                    }),
                    color = MaterialTheme.colorScheme.primary
                )
            }

        }


    }
}