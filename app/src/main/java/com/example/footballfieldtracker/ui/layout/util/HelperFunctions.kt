package com.example.footballfieldtracker.ui.layout.util

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDate(timestamp: Timestamp): String {
    val date: Date = timestamp.toDate()
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return dateFormat.format(date)
}

fun extractAddressPart(address: String): String {
    val parts = address.split(",")

    return if (parts.size > 1) {
        parts.takeLast(2).joinToString(",").trim()
    } else {
        ""
    }
}