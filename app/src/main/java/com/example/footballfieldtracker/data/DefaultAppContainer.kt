package com.example.footballfieldtracker.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.footballfieldtracker.data.repository.FieldRepository
import com.example.footballfieldtracker.data.repository.MarkerRepository
import com.example.footballfieldtracker.data.repository.UserRepository
import com.example.footballfieldtracker.utils.DefaultLocationClient
import com.example.footballfieldtracker.utils.LocationClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


// Mogao bi i ovde koristiti interfejs, al me mrzi (za DefaultAppContainer), dobro je za testiranje
class DefaultAppContainer(context: Context) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    init {
        createNotificationChannel(context)
    }


    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "location"
            val channelName = "Location"
            val importance = NotificationManager.IMPORTANCE_LOW

            val notificationChannel = NotificationChannel(channelId, channelName, importance)
            val notificationManager: NotificationManager =
                // Ako ovo koristis u Application, radice i bez context. jer je implicitno prisutan (this.getSystemService) ali ti to ne vidis
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }

    }

    val userRepository: UserRepository by lazy {
        UserRepository(auth, firestore, storage)
    }

    val markerRepository: MarkerRepository by lazy {
        MarkerRepository(auth, firestore, storage)
    }

    val fieldRepository: FieldRepository by lazy {
        FieldRepository(auth, firestore)
    }

    val locationClient: LocationClient by lazy {
        DefaultLocationClient(context.applicationContext)
    }


}