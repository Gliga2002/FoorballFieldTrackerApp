package com.example.footballfieldtracker.data.model

import com.google.firebase.Timestamp

data class Field(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val address: String = "",
    val longitude: Double = 0.0,
    val latitude: Double = 0.0,
    val reviews: MutableList<Review> = mutableListOf(),
    val avgRating: Double = 0.0,
    val reviewCount: Int = 0,
    val photo: String = "",
    val timeCreated: Timestamp = Timestamp.now(),
    val author: String = ""
)