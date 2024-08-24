package com.example.footballfieldtracker.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Field(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val address: String = "",
    val longitude: Double = 0.0,
    val latitude: Double = 0.0,
    // nije bas ovo pametno, bolje da je isla u firestore kao collection jer za array nema metoda
    val reviews: MutableList<Review> = mutableListOf(),
    val avgRating: Double = 0.0,
    val reviewCount: Int = 0,
    val photo: String = "",
    val timeCreated: Timestamp = Timestamp.now(),
    // Todo: procitaj za kesiranje, zapamtio mi User pa to ti je
    val author: String = ""
)