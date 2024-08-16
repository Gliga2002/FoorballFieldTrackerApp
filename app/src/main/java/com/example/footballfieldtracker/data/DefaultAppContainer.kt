package com.example.footballfieldtracker.data

import android.content.Context
import android.util.Log
import com.example.footballfieldtracker.data.repository.MarkerRepository
import com.example.footballfieldtracker.data.repository.UserRepository
import com.example.locationserviceexample.utils.DefaultLocationClient
import com.example.locationserviceexample.utils.LocationClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

// razmisli o tome da napravis interfejs za defaultAppContainer

class DefaultAppContainer(context: Context) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    // mogao bi i ovde koristiti interfejs
    val userRepository: UserRepository by lazy {
        UserRepository(auth, firestore, storage)
    }

    val markerRepository: MarkerRepository by lazy {
        MarkerRepository(auth, firestore, storage)
    }

    // zbog unit testa koristim interfejs
    val locationClient: LocationClient by lazy {
        DefaultLocationClient(context.applicationContext)
    }


}