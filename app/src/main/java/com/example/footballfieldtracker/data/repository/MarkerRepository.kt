package com.example.footballfieldtracker.data.repository

import android.util.Log
import com.example.footballfieldtracker.data.model.Field
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class MarkerRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    private val _markers = MutableStateFlow<List<Field>>(emptyList())
    val markers: StateFlow<List<Field>> = _markers

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
        suspend fun addLocationData(locationData: Field) {
            try {
                // Koristi `add` da automatski generiše ID
                val documentRef = firestore.collection("markers").add(locationData).await()
                Log.d(
                    "MarkerRepository",
                    "LocationData successfully written with ID: ${documentRef.id}"
                )
            } catch (e: Exception) {
                Log.e("MarkerRepository", "Error writing LocationData", e)
            }
        }


}