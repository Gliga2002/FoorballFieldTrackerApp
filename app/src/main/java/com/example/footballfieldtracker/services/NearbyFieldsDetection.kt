package com.example.footballfieldtracker.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.footballfieldtracker.MainActivity
import com.example.footballfieldtracker.MainApplication
import com.example.footballfieldtracker.R
import com.example.footballfieldtracker.data.model.User
import com.example.footballfieldtracker.data.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow

class NearbyFieldsDetection : Service() {
    // OVO JE NAJVECA GRESKA KOJU SAM IMAO I SA KOJOM SAM SE NAJVISE MUCIO
    // Todo: !!!Kada imas lyfecycle NIKAD NEMOJ NISTA DA INIT PRE LIFYCECLY METODA, STAVLJAJ LATEINIT, ZATO IMAS TE GRESKE, i u servise i u activity. U onCreate to init!!!

    private lateinit var app: MainApplication
    private lateinit var currentUser: StateFlow<User?>
    private lateinit var firestore: FirebaseFirestore

    // e nemoj odma da pristupas resursima ovako pogledaj ServisiLab isao je sa lateinit pa u on create inicijalizovao

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

//    // Lazy initialization for container
//    private val appContainer by lazy {
//        val context = applicationContext
//        Log.d("NearbyFieldsDetection", "Application context class: ${context::class.java.name}")
//        (context as? MainApplication)?.container
//            ?: throw IllegalStateException("Application context is not of type MainApplication")
//    }
//
//    private val userRepo: StateFlow<User?> by lazy {
//        appContainer.userRepository.currentUser
//    }
//    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        app = application as MainApplication
        currentUser = app.container.userRepository.currentUser
        firestore = FirebaseFirestore.getInstance()
        // inicijalizacija, kada se pozove, jednom se izvrsi, jos nije startovan
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()

        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder( this, "location")
            .setContentTitle("Searching nearby fields")
            .setContentText("There isn't any field nearby")
            .setSmallIcon(R.drawable.visibility_24)
            .setStyle(
                NotificationCompat
                    .BigPictureStyle()
                    .bigPicture(
                       // hocu da dodam sliku iz drawable
                        BitmapFactory.decodeResource(resources, R.drawable.cartoon_detective)
                    )
            )
            .setOngoing(true)
            .setContentIntent(pendingIntent) // Set the intent to be fired when notification is clicked
            .setAutoCancel(true) // Remove notification when clicked

        // uzmi notification manager i u ovoj firebase fju ako zadovolji kriterijm ces update notification

        // pozovi firebase fju koja daje live data

        startForeground(1, notification.build())
    }

    private fun stop() {
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        // cancel Coroutine koju si gore kreirao
        serviceScope.cancel()
    }


    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}



