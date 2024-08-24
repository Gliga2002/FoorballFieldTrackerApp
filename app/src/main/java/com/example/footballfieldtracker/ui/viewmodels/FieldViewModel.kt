package com.example.footballfieldtracker.ui.viewmodels

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.footballfieldtracker.data.model.Field
import com.example.footballfieldtracker.data.model.LocationData
import com.example.footballfieldtracker.data.model.Review
import com.example.footballfieldtracker.data.repository.FieldRepository
import com.example.footballfieldtracker.data.repository.MarkerRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FieldViewModel(
    // ne treba ti markerRepository
    private val markerRepository: MarkerRepository, private val fieldRepository: FieldRepository
) : ViewModel() {

    var comment by mutableStateOf("")
    var selectedStars by mutableStateOf(1)

    var selectedFieldState: Field by mutableStateOf(Field())
        private set

    fun setCurrentFieldState(field: Field) {
        selectedFieldState = field
    }

    // Todo: hou da prestanem da slusam kad se navigiram nazad, smisli kako
    val selectedField: StateFlow<Field?> get() = fieldRepository.selectedField



    fun loadField(fieldId: String) {
        viewModelScope.launch {
            fieldRepository.addFieldSnapshotListener(fieldId)
        }
    }

    fun stopListening() {
        fieldRepository.stopListening()
    }
    fun createReview(fieldId: String,callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val hasReviewed = fieldRepository.hasUserReviewed(fieldId)
            if (!hasReviewed) {
                fieldRepository.addReview(fieldId, text = comment, rating = selectedStars)
                callback(true)

            } else {
                callback(false)
            }
        }
    }

    fun resetReviewData() {
        comment = ""
        selectedStars = 1
    }


    fun handleLikedReviews(review: Review, isLiked: Boolean) {
        viewModelScope.launch {
            fieldRepository.handleLikedReview(review, isLiked)
        }
    }

    // kreiraj funkciju koja proverava da li je review ocenjen od strane korisnika


    override fun onCleared() {
        super.onCleared()
        stopListening()
    }


}


class FieldViewModelFactory(
    private val markerRepository: MarkerRepository,
    private val fieldRepository: FieldRepository // Dodano polje za FieldRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FieldViewModel::class.java)) {
            return FieldViewModel(markerRepository, fieldRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

