package com.example.footballfieldtracker.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.footballfieldtracker.data.model.Field
import com.example.footballfieldtracker.data.model.Review
import com.example.footballfieldtracker.data.repository.FieldRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FieldViewModel(
   private val fieldRepository: FieldRepository
) : ViewModel() {

    var comment by mutableStateOf("")
    var selectedStars by mutableStateOf(1)

    var selectedFieldState: Field by mutableStateOf(Field())
        private set

    fun setCurrentFieldState(field: Field) {
        selectedFieldState = field
    }

    val selectedField: StateFlow<Field?> get() = fieldRepository.selectedField


    fun loadField(fieldId: String) {
        viewModelScope.launch {
            fieldRepository.addFieldSnapshotListener(fieldId)
        }
    }

    fun createReview(fieldId: String,callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val hasReviewed = fieldRepository.hasUserReviewed()
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


    override fun onCleared() {
        super.onCleared()
        fieldRepository.stopListening()
    }


}


class FieldViewModelFactory(
    private val fieldRepository: FieldRepository // Dodano polje za FieldRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FieldViewModel::class.java)) {
            return FieldViewModel(fieldRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

