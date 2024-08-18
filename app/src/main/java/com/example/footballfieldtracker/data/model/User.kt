package com.example.footballfieldtracker.data.model

// Imao si dve greske sa firoste, nisi provide default argument i nisi lepo mapirao iz baze u class (toObject)
data class User(
    // id je uuid, necu da mi on sam generise
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
