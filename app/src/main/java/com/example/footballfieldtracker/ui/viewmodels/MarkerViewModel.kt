package com.example.footballfieldtracker.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.footballfieldtracker.data.model.Field

import com.example.footballfieldtracker.data.model.Review
import com.example.footballfieldtracker.data.repository.MarkerRepository
import com.example.footballfieldtracker.data.repository.UserRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MarkerViewModel(private val markerRepository: MarkerRepository) : ViewModel() {

    val markers: StateFlow<List<Field>> = markerRepository.markers

    init {
        Log.d(
            "MarkerViewModel",
            "Izbrsavam se "
        )
        // Automatski dodaj dummy podatke prilikom inicijalizacije ViewModel-a
        viewModelScope.launch {
            addDummyData()
        }
    }

    private suspend fun addDummyData() {
        val dummyData = listOf(
            Field(
                name = "Location 1",
                type = "Type A",
                address = "123 Example St",
                longitude = 20.2602128,
                latitude =  44.6910817,
                reviews = mutableListOf(),
                avgRating = 4.5,
                reviewCount = 1,
                photo = "https://picsum.photos/id/237/200/300",
                timeCreated = Timestamp.now(),
                author = "Author1"
            ),
            Field(
                name = "Location 2",
                type = "Type B",
                address = "456 Another St",
                longitude = 22.6997321,
                latitude = 53.6010061,
                reviews = mutableListOf(),
                avgRating = 3.0,
                reviewCount = 1,
                photo = "https://picsum.photos/seed/picsum/200/300",
                timeCreated = Timestamp.now(),
                author = "Author2"
            )
        )

        for (data in dummyData) {
            markerRepository.addLocationData(data)
        }
    }

}

class MarkerViewModelFactory(
    private val markerRepository: MarkerRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MarkerViewModel::class.java)) {
            return MarkerViewModel(markerRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}