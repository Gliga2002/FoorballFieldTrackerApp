package com.example.footballfieldtracker.data.repository

import android.net.Uri
import android.util.Log
import com.example.footballfieldtracker.data.model.LocationData
import com.example.footballfieldtracker.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

// mogao bi i ovde interfejs, jer ces da ga koristis kao DI u viewModel i dobar je za mocking

class UserRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    private var listenerRegistration: ListenerRegistration? = null
    // current user i location mogu ti budu data source ali ne mora se bakces
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    // Dodao
    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers

    // Todo: promeni svuda naziv na currentUserLocation
    private val _locationData = MutableStateFlow<LocationData?>(null)
    val locationData: StateFlow<LocationData?> = _locationData

    fun updateLocationData(locationData: LocationData) {
        _locationData.value = locationData
    }

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

            // ovo si mogao sa User class da instanciras, rekli su na firestore https://firebase.google.com/docs/firestore/manage-data/add-data
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

                val user = userDocument.toObject(User::class.java)
                cb(user)
            } catch (e: Exception) {
                // Handle exceptions (e.g., log error)
                cb(null)
            }
        } else {
            cb(null)
        }
    }

    // Dodao
    fun fetchAllUsers() {
        listenerRegistration = firestore.collection("users")
            .orderBy("score", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("UserRepository", "Error fetching users", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    // Convert query snapshot to list of users
                    val users = snapshot.documents.mapNotNull { document ->
                        document.toObject(User::class.java)
                    }
                    // Update StateFlow with the list of users
                    _allUsers.value = users
                }
            }
    }

    fun stopFetchAllUsers() {
        listenerRegistration?.remove()
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




