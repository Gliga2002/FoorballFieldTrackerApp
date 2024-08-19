package com.example.footballfieldtracker.data.repository

import android.net.Uri
import android.util.Log
import com.example.footballfieldtracker.data.model.Field
import com.example.footballfieldtracker.data.model.LocationData
import com.example.footballfieldtracker.data.model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.pow

class MarkerRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    private val _markers = MutableStateFlow<List<Field>>(emptyList())
    val markers: StateFlow<List<Field>> = _markers


    private val _filteredMarkers = MutableStateFlow<List<Field>>(emptyList())
    val filteredMarkers: StateFlow<List<Field>> = _filteredMarkers.asStateFlow()

    init {
        fetchMarkers()
    }

    // Funkcija za preuzimanje markera iz Firestore
    private fun fetchMarkers() {
        val markersCollectionRef = firestore.collection("markers")

        // Dodajte listener za promene u kolekciji
        markersCollectionRef.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                // Handle error
                return@addSnapshotListener
            }

            // Mapirajte rezultate u listu Marker
            val updatedMarkers = snapshot?.documents?.mapNotNull { document ->
                document.toObject(Field::class.java)
            } ?: emptyList()

            // Ažurirajte _markers sa novim podacima
            _markers.value = updatedMarkers
        }

    }


        // Funkcija za dodavanje LocationData u kolekciju markers s automatski generisanim ID-om
        suspend fun addLocationData(
            name: String,
            type: String,
            address: String,
            lng: Double,
            lat: Double,
            photo: Uri?
        ) {
            // ovo currentTime mozes i da passujes
            val currentTime = Timestamp.now()
            val userId = auth.currentUser!!.uid

            try {
                // ovo sam menjao
                val userRef = firestore.collection("users").document(userId).get().await()
                val user = userRef.toObject(User::class.java) ?: User() // Ako ne postoji korisnik, koristi praznog User-a

                var fieldData = Field("", name, type, address, lng, lat, mutableListOf(),0.0, 0,"", currentTime, user.username)
                // Dodaj dokument i automatski generiši ID
                val documentRef = firestore.collection("markers").add(fieldData).await()

                // Uzmi generisani ID
                val fieldId = documentRef.id

                // Ako postoji slika, sačuvaj je u Firebase Storage
                if (photo != null) {
                    val fieldPicRef = storage.getReference("field_pictures/$fieldId")
                    fieldPicRef.putFile(photo).await()  // Čeka da se slika upload-uje
                    val downloadUri = fieldPicRef.downloadUrl.await()  // Čeka da se preuzme URL slike

                    // Ažuriraj Firestore dokument sa URL-om slike i ID-jem
                    documentRef.update(
                        mapOf(
                            "photo" to downloadUri.toString(),
                            "id" to fieldId
                        )
                    ).await()
                } else {
                    // Ako slika nije dostupna, samo ažuriraj ID
                    documentRef.update("id", fieldId).await()
                }

                // Ažuriraj score korisnika
                val userDocRef = firestore.collection("users").document(userId)
                userDocRef.update("score", FieldValue.increment(20)).await()

                Log.d("MarkerRepository", "LocationData successfully written with ID: $fieldId")
            } catch (e: Exception) {
                Log.e("MarkerRepository", "Error writing LocationData", e)
            }
        }

    fun applyFilters(
        callback: (Boolean) -> Unit,
        author: String,
        type: String,
        date: String,
        radius: Int,
        currentLoc: LocationData
    ) {
        val filteredList = _markers.value.filter { location ->

            // ovo sam menjao
            var authorMatch = location.author.contains(author, ignoreCase = true)
            var typeMatch = location.type.contains(type, ignoreCase = true)
            val dateMatch = date.isEmpty() || isDateInRange(location.timeCreated.toDate(), date)

            // Log the match criteria for each marker
            Log.i("FilterDebug", "Checking marker: ${location.name}")

            if (author.isBlank()) {
                authorMatch = true
            }
            if (type == "Any Type") {
                typeMatch = true
            }

            Log.i("FilterDebug", "Author match: $authorMatch, Type match: $typeMatch, Date match: $dateMatch")


            // Check if the marker is within the specified radius
            val distance = calculateDistance(
                currentLoc.latitude,
                currentLoc.longitude,
                location.latitude,
                location.longitude
            )
            val withinRadius = distance < radius

            // Log distance and radius information
            Log.i("FilterDebug", "Distance to marker: $distance km, Radius: $radius km, Within radius: $withinRadius")


            authorMatch && typeMatch && dateMatch && withinRadius
        }
        // Log the count and details of filtered markers
        Log.i("FilterDebug", "Filtered markers count: ${filteredList.size}")
        filteredList.forEach { marker ->
            Log.i("FilterDebug", "Filtered marker: ${marker.name}, Location: (${marker.latitude}, ${marker.longitude})")


        }

        if (filteredList.size > 0) {
            callback(true)
            _filteredMarkers.value = filteredList
        } else {
            callback(false)
        }

    }


    // mogao si sa compareTo
    private fun isDateInRange(locationDate: Date, filterDate: String): Boolean {
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
        val filterDateParts = filterDate.split(" - ")

        try {
            if (filterDateParts.size == 2) {
                val startDate = dateFormat.parse(filterDateParts[0])
                val endDate = dateFormat.parse(filterDateParts[1])

                if (startDate != null && endDate != null) {
                    return locationDate >= startDate && locationDate <= endDate
                }
            }
        } catch (e: ParseException) {
            Log.d("ParseException", e.message.toString())
        }
        return false
    }

    // mogao si onu drugu funkciju
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val radiusOfEarth = 6371 // Earth's radius in kilometers

        val lat1Rad = Math.toRadians(lat1)
        val lon1Rad = Math.toRadians(lon1)
        val lat2Rad = Math.toRadians(lat2)
        val lon2Rad = Math.toRadians(lon2)

        val dLat = lat2Rad - lat1Rad
        val dLon = lon2Rad - lon1Rad
        val a = Math.sin(dLat / 2).pow(2) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.sin(dLon / 2).pow(2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return radiusOfEarth * c
    }


    fun removeFilters() {
        _filteredMarkers.value = emptyList()
    }
}


