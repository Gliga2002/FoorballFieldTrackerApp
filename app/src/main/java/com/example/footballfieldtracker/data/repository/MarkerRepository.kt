package com.example.footballfieldtracker.data.repository

import android.net.Uri
import android.util.Log
import com.example.footballfieldtracker.data.model.Field
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
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
        suspend fun addLocationData(
            name: String,
            type: String,
            address: String,
            lng: Double,
            lat: Double,
            photo: Uri?
        ) {

            val currentTime = Timestamp.now()
            val userId = auth.currentUser!!.uid

            var fieldData = Field("", name, type, address, lng, lat, mutableListOf(),0.0, 0,"", currentTime, userId)
            val firestore = FirebaseFirestore.getInstance()
            try {
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


}