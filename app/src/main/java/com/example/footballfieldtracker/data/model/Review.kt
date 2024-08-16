package com.example.footballfieldtracker.data.model

data class Review(
    val user: String = "",
    val rating: Int = 0,
    val text: String = "",
    var likes: Int = 0,
    val markerId: String = ""
)