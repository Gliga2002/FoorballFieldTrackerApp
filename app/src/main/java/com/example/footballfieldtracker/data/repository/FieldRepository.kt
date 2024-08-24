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

    fun stopListening() {
        snapshotListenerRegistration?.remove()
    }


    suspend fun hasUserReviewed(): Boolean {
        val user = getUser(auth.currentUser!!.uid)
        val username = user?.username ?: return false

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

        try {

            // Dodajte recenziju u niz reviews u dokumentu fieldId
            firestore.collection("markers")
                .document(fieldId)
                .update("reviews", FieldValue.arrayUnion(newReview))
                .await()

            // Ažurirajte score korisnika
            changeAuthorScore(5, true)

            // Ažurirajte reviewCount i avgRating u dokumentu fieldId
            // TODO: NISI KORISTION SUSPEND FUNCTION VEC ONO ADDONSUCCESS
            updateFieldStats(
                reviewCount = selectedField.value!!.reviews.size,
                reviews = _selectedField.value!!.reviews
            )

        } catch (e: Exception) {

        }
    }


    private suspend fun updateFieldStats(reviewCount: Int, reviews: MutableList<Review>) {
        try {
            val symbols = DecimalFormatSymbols(Locale.US)
            val decimalFormat = DecimalFormat("#.00", symbols)
            val formattedAvgRating =
                decimalFormat.format(calculateAvgRating(reviews = reviews)).toDouble()



            updateAvgRatingAndSizeInFirestore(formattedAvgRating, reviewCount)
        } catch (e: Exception) {
            // Obrada grešaka
        }
    }

    // Function to calculate the average rating of the location
    private fun calculateAvgRating(reviews: MutableList<Review>): Double {
        return if (reviews.isEmpty()) {
            0.0
        } else {
            var totalRating = 0.0
            for (review in reviews) {
                totalRating += review.rating
            }

            totalRating / reviews.size

        }

    }


    // Function to update the average rating in Firestore
    private suspend fun updateAvgRatingAndSizeInFirestore(
        formattedAvgRating: Double,
        reviewCount: Int
    ) {
        if (_selectedField.value?.id?.isNotEmpty() == true) {
            val db = FirebaseFirestore.getInstance()
            val collectionRef = db.collection("markers")
            val documentRef = collectionRef.document(_selectedField.value!!.id)

            val data =
                hashMapOf("avgRating" to formattedAvgRating, "reviewCount" to reviewCount)

            try {
                documentRef
                    .set(data, SetOptions.merge())
                    .await()

            } catch (e: Exception) {
                // Obrada grešaka
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

            changeAuthorScore(5, true)

        } else {

            updateReviewLikes(review.id, isLiked, review.markerId)

            updateUserLikedReviews(currUserId, isLiked, review.id)

            changeAuthorScore(5, false)
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
                val reviewsList =
                    documentSnapshot.get("reviews") as? List<Map<String, Any>> ?: emptyList()

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

    private suspend fun updateUserLikedReviews(
        userId: String,
        isLiked: Boolean,
        reviewId: String
    ) {
        val db = Firebase.firestore
        val userRef = db.collection("users").document(userId)
        userRef.update(
            "likedReviews",
            if (isLiked) FieldValue.arrayUnion(reviewId) else FieldValue.arrayRemove(reviewId)
        ).await()
    }


    // refaktorisi
    suspend fun changeAuthorScore(scoreValue: Int, increase: Boolean) {

        val user = getUser(auth.currentUser!!.uid)

        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            val usersCollectionRef = db.collection("users")

            try {
                // Pretražuje kolekciju Users za korisnika sa odgovarajućim korisničkim imenom
                val querySnapshot = usersCollectionRef.whereEqualTo("username", user.username).get().await()

                for (documentSnapshot in querySnapshot) {
                    val userRef = usersCollectionRef.document(documentSnapshot.id)
                    val innerDocumentSnapshot = userRef.get().await()

                    if (innerDocumentSnapshot.exists()) {
                        var existingScore = innerDocumentSnapshot.getLong("score") ?: 0
                        val newScore = if (increase) existingScore + scoreValue else existingScore - scoreValue

                        // Ažurira score u Firestore
                        userRef.update("score", newScore).await()
                    }
                }
            } catch (e: Exception) {
                // Obrada grešaka
                Log.e("Change Author Score", "Error updating score: ${e.message}")
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


}