package com.example.footballfieldtracker.data.repository

import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.footballfieldtracker.data.model.User
import com.example.footballfieldtracker.ui.Screens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    fun updateCurrentUser(user: User?) {
        _currentUser.value = user
    }


    fun loginWithEmailAndPassword(
        email: String,
        password: String,
        callback: (User?) -> Unit
    ) {
        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(email, password)
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
                                callback(currentUser) // Return the User object
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
//                                currentUserViewModel.setCurrentUser(currentUser)
//                                navController.navigate(Screens.GoogleMap.name)
                            }

                        } else {
                            // Handle if the user document doesn't exist
                            callback(null)

                        }
                    }
                    .addOnFailureListener {
                        // Handle any errors while fetching user details
                        callback(null)
                    }


            }
            .addOnFailureListener {
                // Handle any errors while fetching user details
                callback(null)

            }
    }

    suspend fun registerUser(
        email: String,
        password: String,
        username: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        imageUri: Uri?,
        callback: (Boolean) -> Unit
    ) {
        try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: return callback(false)

            val user = mapOf(
                "id" to userId,
                "email" to email,
                "username" to username,
                "firstName" to firstName,
                "lastName" to lastName,
                "phoneNumber" to phoneNumber,
                "score" to 0,
                "likedReviews" to mutableListOf<String>(),
                "photoPath" to ""
            )

            firestore.collection("users").document(userId).set(user).await()

            imageUri?.let {
                val profilePicRef = storage.getReference("profile_pictures/$userId")
                profilePicRef.putFile(it).await()
                val downloadUri = profilePicRef.downloadUrl.await()
                firestore.collection("users").document(userId)
                    .update("photoPath", downloadUri.toString()).await()
            }

            callback(true)
        } catch (e: Exception) {
            callback(false)
        }
    }

    suspend fun fetchCurrentUser(
        cb: (User?) -> Unit
    ) {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            try {
                val userDocument = firestore.collection("users")
                    .document(firebaseUser.uid)
                    .get()
                    .await()

                userDocument.toObject(User::class.java)
                cb(null)
            } catch (e: Exception) {
                // Handle exceptions (e.g., log error)
                cb(null)
            }
        } else {
            cb(null)
        }
    }

    fun signOut(callback: (Boolean) -> Unit) {
        try {
            auth.signOut()
            callback(true)
        } catch (e: Exception) {
            // Log error or handle failure
            callback(false)
        }
    }
}


