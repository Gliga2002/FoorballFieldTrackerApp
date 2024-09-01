package com.example.footballfieldtracker.data.model


data class User(
    // id je uuid
    var id: String = "",
    var email: String = "",
    var username: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var phoneNumber: String = "",
    var score: Int = 0,
    var likedReviews: MutableList<String> = mutableListOf(),
    var photoPath: String = ""
)
