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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class NearbyFieldsDetection : Service() {

    private lateinit var app: MainApplication
    private lateinit var currentUserLocation: StateFlow<LocationData?>
    private lateinit var markers: StateFlow<List<Field?>>

    private lateinit var notification: NotificationCompat.Builder

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        app = application as MainApplication
        currentUserLocation = app.container.userRepository.currentUserLocation
        markers = app.container.markerRepository.markers


    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("Searching nearby fields")
            .setContentText("There isn't any field nearby")
            .setSmallIcon(R.drawable.search_24)
            .setStyle(
                NotificationCompat
                    .BigPictureStyle()
                    .bigPicture(
                        BitmapFactory.decodeResource(resources, R.drawable.cartoon_detective)
                    )
            )
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        startForeground(1, notification.build())
        // Start observing location and markers changes
        observeLocationChanges()
        observeMarkerChanges()
    }

    private fun stop() {
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel Coroutine
        serviceScope.cancel()
    }

    private fun observeLocationChanges() {
        serviceScope.launch {
            currentUserLocation.collect { userLocation ->
                userLocation?.let { location ->
                    // Fetch markers from the StateFlow and check proximity
                    checkProximityToMarkers(markers.value, location)
                }
            }
        }
    }

    private fun observeMarkerChanges() {
        serviceScope.launch {
            markers.collect { markerList ->
                // Fetch current location and check proximity to new markers
                currentUserLocation.value?.let { userLocation ->
                    checkProximityToMarkers(markerList, userLocation)
                }
            }
        }
    }

    private fun checkProximityToMarkers(markers: List<Field?>, userLocation: LocationData) {
        var nearbyFieldFound = false
        markers.forEach { marker ->
            marker?.let {
                val markerLocation = LocationData(it.latitude, it.longitude)
                val distance = calculateDistance(userLocation, markerLocation)
                if (distance < 10_000) { // 10km
                    if (!nearbyFieldFound) {
                        // Ažurirajte obaveštenje sa statusom da je polje pronađeno
                        updateNotification(
                            "Nearby field found: ${it.name}",
                            notification,
                            isNearbyFieldFound = true
                        )
                        nearbyFieldFound = true
                    }
                }
            }
        }
        if (!nearbyFieldFound) {
            // Ažurirajte obaveštenje sa statusom da polje nije pronađeno
            updateNotification(
                "There isn't any field nearby",
                notification,
                isNearbyFieldFound = false
            )
        }
    }

    private fun calculateDistance(location1: LocationData, location2: LocationData): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            location1.latitude,
            location1.longitude,
            location2.latitude,
            location2.longitude,
            results
        )
        return results[0]
    }

    private fun updateNotification(
        message: String,
        notification: NotificationCompat.Builder,
        isNearbyFieldFound: Boolean
    ) {
        // Odaberite sliku na osnovu toga da li je polje pronađeno
        val pictureResId = if (isNearbyFieldFound) {
            R.drawable.found
        } else {
            R.drawable.cartoon_detective
        }

        // Ažurirajte obaveštenje
        notification
            .setContentText(message)
            .setSmallIcon(R.drawable.search_24)
            .setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(BitmapFactory.decodeResource(resources, pictureResId))
            )
            .setOngoing(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification.build())
    }


    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        private const val TAG = "NearbyFieldsDetection"
    }
}



