package com.example.footballfieldtracker.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.footballfieldtracker.MainActivity
import com.example.footballfieldtracker.MainApplication
import com.example.footballfieldtracker.R
import com.example.footballfieldtracker.data.model.Field
import com.example.footballfieldtracker.data.model.LocationData

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow


class NearbyFieldsDetection : Service() {

    // Nemoj nista da inicijalizujes, pre onCreate!! Koristi lateinit
    private lateinit var app: MainApplication
    private lateinit var currentUserLocation: StateFlow<LocationData?>
    private lateinit var firestore: FirebaseFirestore
    // ovo ovde je okej
    private lateinit var notification: NotificationCompat.Builder


    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        app = application as MainApplication
        currentUserLocation = app.container.userRepository.currentUserLocation
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

        notification = NotificationCompat.Builder( this, "location")
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



        startForeground(1, notification.build())
        observeMarkers()


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


    private fun observeMarkers() {
        // Pretplata na promene u Firebase kolekciji
        // Mogo si i iz repository, imas markere live, ili ovako
        firestore.collection("markers")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val markers = snapshot.documents.mapNotNull { it.toObject(Field::class.java) }
                    checkProximityToMarkers(markers)
                }
            }
    }

    private fun checkProximityToMarkers(markers: List<Field>) {
        // Proverite da li je trenutna lokacija unutar radijusa od 10km od nekog markera
        val userLocation = currentUserLocation.value ?: return
        markers.forEach { marker ->
            val markerLocation = LocationData(marker.latitude, marker.longitude)
            val distance = calculateDistance(userLocation, markerLocation)
            if (distance < 10_000) { // 10km
                // Ažurirajte notifikaciju
                updateNotification(
                    "Nearby field found: ${marker.name}",
                    notification
                )
            }
        }
    }

    private fun calculateDistance(location1: LocationData, location2: LocationData): Float {
        val results = FloatArray(1)
        Location.distanceBetween(location1.latitude, location1.longitude, location2.latitude, location2.longitude, results)
        return results[0]
    }


    private fun updateNotification(message: String, notification: NotificationCompat.Builder) {
            notification
                .setContentText(message)
                .setSmallIcon(R.drawable.visibility_24)
                .setStyle(
                    NotificationCompat.BigPictureStyle()
                    .bigPicture(BitmapFactory.decodeResource(resources, R.drawable.found))
                )
                .setOngoing(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification.build())
    }





    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        private const val TAG = "NearbyFieldsDetection"
    }
}



