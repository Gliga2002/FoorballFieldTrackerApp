package com.example.footballfieldtracker.data.repository

import android.util.Log
import com.example.footballfieldtracker.data.model.Field
import com.example.footballfieldtracker.data.model.Review
import com.example.footballfieldtracker.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.UUID

class FieldRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) {


    private val _selectedField = MutableStateFlow<Field?>(null)
    val selectedField: StateFlow<Field?> get() = _selectedField


    private var snapshotListenerRegistration: ListenerRegistration? = null


    fun addFieldSnapshotListener(fieldId: String) {
        val documentReference = firestore.collection("markers").document(fieldId)
        snapshotListenerRegistration =
            documentReference.addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    // Handle error
                    _selectedField.value = null
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    _selectedField.value = snapshot.toObject(Field::class.java)
                } else {
                    _selectedField.value = null
                }
            }
    }


    suspend fun hasUserReviewed(fieldId: String): Boolean {
        // Pretpostavljam da već imaš funkciju getUser koja vraća korisnika na osnovu UID-a
        val user = getUser(auth.currentUser!!.uid)
        val username = user?.username ?: return false // Ako nema korisnika, vrati false odmah

        return try {
            val reviews = _selectedField.value?.reviews
            if (reviews != null) {
                for (review in reviews) {
                    if (review.user == username) {
                        return true
                    }
                }
            }

            return false
        } catch (e: Exception) {
            // Obradi greške
            false
        }
    }


    suspend fun addReview(fieldId: String, text: String, rating: Int) {

        val user = getUser(auth.currentUser!!.uid)

        val newReview = user?.let {
            Review(
                id = UUID.randomUUID().toString(),
                user = it.username,
                rating = rating,
                text = text,
                likes = 0, markerId = fieldId
            )
        }

        Log.i("AddReview", "Created new review: $newReview")

        try {

            // Dodajte recenziju u niz reviews u dokumentu fieldId
            firestore.collection("markers")
                .document(fieldId)
                .update("reviews", FieldValue.arrayUnion(newReview))
                .await()
            Log.i(
                "AddReview",
                "Successfully added review to 'reviews' array in document with ID: $fieldId"
            )

            // Ažurirajte score korisnika
            addToAuthorScore(5)

            // Ažurirajte reviewCount i avgRating u dokumentu fieldId
            // TODO: NISI KORISTION SUSPEND FUNCTION VEC ONO ADDONSUCCESS
            updateFieldStats()

            true
        } catch (e: Exception) {
            // Obrada grešaka
            false
        }
    }


    private fun updateFieldStats() {
        try {
            // Todo: formatiraj
            val symbols = DecimalFormatSymbols(Locale.US)
            val decimalFormat = DecimalFormat("#.00", symbols)
            val formattedAvgRating = decimalFormat.format(calculateAvgRating()).toDouble()
            Log.i("Debaging", "formatedavgrating ${formattedAvgRating}")


            Log.i("Debaging", "SelectedField ${_selectedField.value}")

            Log.i("Debaging", "Formatiran rejting $formattedAvgRating")

            val reviewCount = _selectedField.value!!.reviews.size

            Log.i("Debaging", "Formatiran count $reviewCount")



            updateAvgRatingAndSizeInFirestore(formattedAvgRating, reviewCount)
        } catch (e: Exception) {
            // Obrada grešaka
            Log.i("Debaging", e.toString())
        }
    }

    // Function to calculate the average rating of the location
    private fun calculateAvgRating(): Double {
        val reviews = _selectedField.value?.reviews
        Log.i("Debaging", "reviews $reviews")
        if (reviews != null) {
            if (reviews.isEmpty()) {
                Log.i("Debaging", "Greska")
                return 0.0
            }
        }
        var totalRating = 0.0
        if (reviews != null) {
            for (review in reviews) {
                totalRating += review.rating
            }
        }

        Log.i("Debaging", "ovo vracam iz calculate Avg ${totalRating / reviews!!.size}")
        return totalRating / reviews!!.size
    }

    // Function to update the average rating in Firestore
    private fun updateAvgRatingAndSizeInFirestore(formattedAvgRating: Double, reviewCount: Int) {
        Log.i("Debaging", "zadnja provera ${_selectedField.value!!.id}")
        if (_selectedField.value?.id?.isNotEmpty() == true) {
            Log.i("Debaging", "tu sam")
            val db = FirebaseFirestore.getInstance()
            val collectionRef = db.collection("markers")
            val documentRef = collectionRef.document(_selectedField.value!!.id)

            val data = hashMapOf("avgRating" to formattedAvgRating, "reviewCount" to reviewCount)

            documentRef
                .set(data, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("Update Avg Rating", "AvgRating updated successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("Update Avg Rating", "Error updating avgRating: ${e.message}")
                }
        } else {
            Log.e("Update Avg Rating", "Invalid currentLocation.id")
        }
    }


    suspend fun handleLikedReview(review: Review, isLiked: Boolean) {

        val currUserId = auth.currentUser!!.uid
        if (isLiked) {

            updateReviewLikes(review.id, isLiked, review.markerId)

            updateUserLikedReviews(currUserId, isLiked, review.id)

            addToAuthorScore(5)

        } else {

            updateReviewLikes(review.id, isLiked, review.markerId)

            updateUserLikedReviews(currUserId, isLiked, review.id)

            subtractToAuthorScore(5)
        }
    }


    // Todo: ne valja nista, ali zato jer si ga ti cuvao kao niz stringova a on je kao novu collection tako si i ti trebao, za array nemas update funkciju pa je teze

    // Todo: IMAS DVA NACINA SA ASYN OPERACTION OVO SA CB FUNKCIJOM ADDONSUCCESSLISTENER LI SA AWAIT
//        // Todo: Da si koristion addonsucces listener ne bi bila suspend function i ne bi ti trebala courtine, istrazi


    // https://www.youtube.com/watch?v=Bthy1Dla_ws
    private suspend fun updateReviewLikes(
        reviewId: String,
        isLiked: Boolean,
        clickedLocationId: String
    ) {

        val db = FirebaseFirestore.getInstance()
        val markerRef = db.collection("markers").document(clickedLocationId)

        try {
            // Retrieve the marker document
            val documentSnapshot = markerRef.get().await()
            if (documentSnapshot.exists()) {
                // Retrieve the 'reviews' field as a list of maps
                val reviewsList = documentSnapshot.get("reviews") as? List<Map<String, Any>> ?: emptyList()

                // Convert each map to a Review object
                val reviewList = reviewsList.mapNotNull { map ->
                    try {
                        // Convert map to Review object
                        val review = Review(
                            id = map["id"] as? String ?: "",
                            user = map["user"] as? String ?: "",
                            rating = (map["rating"] as? Number)?.toInt() ?: 0,
                            text = map["text"] as? String ?: "",
                            likes = (map["likes"] as? Number)?.toInt() ?: 0,
                            markerId = map["markerId"] as? String ?: ""
                        )
                        review
                    } catch (e: Exception) {
                        Log.w("UpdateReviewLikes", "Error converting map to Review object", e)
                        null
                    }
                }.toMutableList()

                // Update the specific review in the list
                val updatedReviews = reviewList.map { review ->
                    if (review.id == reviewId) {
                        val newLikes = if (isLiked) review.likes + 1 else review.likes - 1
                        review.copy(likes = newLikes)
                    } else {
                        review
                    }
                }

                // Update the 'reviews' field with the modified list
                markerRef.update("reviews", updatedReviews).await()
                Log.d("UpdateReviewLikes", "Likes successfully updated.")
            } else {
                Log.w("UpdateReviewLikes", "Marker document does not exist.")
            }
        } catch (e: Exception) {
            Log.w("UpdateReviewLikes", "Error updating likes", e)
        }


    }

    private suspend fun updateUserLikedReviews(userId: String, isLiked: Boolean, reviewId: String) {
        val db = Firebase.firestore
        val userRef = db.collection("users").document(userId)
        userRef.update(
            "likedReviews",
            if (isLiked) FieldValue.arrayUnion(reviewId) else FieldValue.arrayRemove(reviewId)
        ).await()
    }


    // refaktorisi
    suspend fun addToAuthorScore(scoreValue: Int) {

        val user = getUser(auth.currentUser!!.uid)

        val db = Firebase.firestore
        val usersCollectionRef = db.collection("users")

        // Query the Users collection to find the user with the matching username
        if (user != null) {
            usersCollectionRef.whereEqualTo("username", user.username)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (documentSnapshot in querySnapshot) {
                        val userRef = usersCollectionRef.document(documentSnapshot.id)
                        userRef.get().addOnSuccessListener { innerDocumentSnapshot ->
                            if (innerDocumentSnapshot.exists()) {
                                var existingScore = innerDocumentSnapshot.getLong("score") ?: 0
                                var newScore = existingScore + scoreValue
                                userRef.update("score", newScore)
                            }
                        }
                    }
                }
        }
    }

    suspend fun subtractToAuthorScore(scoreValue: Int) {

        val user = getUser(auth.currentUser!!.uid)

        val db = Firebase.firestore
        val usersCollectionRef = db.collection("users")

        // Query the Users collection to find the user with the matching username
        if (user != null) {
            usersCollectionRef.whereEqualTo("username", user.username)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (documentSnapshot in querySnapshot) {
                        val userRef = usersCollectionRef.document(documentSnapshot.id)
                        userRef.get().addOnSuccessListener { innerDocumentSnapshot ->
                            if (innerDocumentSnapshot.exists()) {
                                var existingScore = innerDocumentSnapshot.getLong("score") ?: 0
                                var newScore = existingScore - scoreValue
                                userRef.update("score", newScore)
                            }
                        }
                    }
                }
        }
    }


    // Funkcija za dobijanje korisnika iz kolekcije
    private suspend fun getUser(userId: String): User? {
        return try {
            val documentSnapshot = firestore
                .collection("users")
                .document(userId)
                .get()
                .await()
            documentSnapshot.toObject(User::class.java)
        } catch (e: Exception) {
            // Obrada grešaka
            null
        }
    }

    fun stopListening() {
        snapshotListenerRegistration?.remove()
    }


}