package com.example.footballfieldtracker.data

import com.example.footballfieldtracker.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class DefaultAppContainer {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    // Instancira UserRepository sa potrebnim zavisnostima
    val userRepository: UserRepository by lazy {
        UserRepository(auth, firestore, storage)
    }
}