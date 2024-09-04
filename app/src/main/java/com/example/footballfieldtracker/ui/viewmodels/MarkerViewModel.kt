package com.example.footballfieldtracker.ui.viewmodels

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.footballfieldtracker.data.model.Field
import com.example.footballfieldtracker.data.model.LocationData
import com.example.footballfieldtracker.data.repository.MarkerRepository
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MarkerViewModel(private val markerRepository: MarkerRepository) : ViewModel() {


    var name by mutableStateOf("")
    var selectedOption by mutableStateOf("Gradska Liga")
    var imageUri by mutableStateOf<Uri?>(null)
    var lat: Double by mutableStateOf(0.0)
        private set
    var lng: Double by mutableStateOf(0.0)
        private set
   var address: String by mutableStateOf("")
        private set


    val markers: StateFlow<List<Field>> = markerRepository.markers
    val filteredMarkers: StateFlow<List<Field>> = markerRepository.filteredMarkers

    var filteredName by mutableStateOf("")
    var filteredSelectedOption by mutableStateOf("Any Type")
    var filteredRadius by mutableStateOf<Int?>(null)
    var dateRange by mutableStateOf("")

    // Funkcija za resetovanje stanja
    fun resetState() {
        name = ""
        selectedOption = "Gradska Liga"
        address = ""
        lat = 0.0
        lng = 0.0
        imageUri = null
    }

    fun resetFilter() {
        filteredName = ""
        filteredSelectedOption = "Any Type"
        filteredRadius = 0
        dateRange = ""
    }

    fun setLatLng(latLng: LatLng) {
        lat = latLng.latitude
        lng = latLng.longitude
    }

    fun setNewAddress(fieldAddress:String) {
        address = fieldAddress
    }



    fun createMarker(
        callback: (Boolean, String) -> Unit
    ) {
        // Validacija
        // Proveri da li je naziv prisutan i nije prazan
        if (!isValidName(name)) {
            callback(false, "Naziv ne može biti prazan")
            return
        }

        // Proveri da li je tip odabran i validan
        if (!isValidType(selectedOption)) {
            callback(false, "Tip mora biti izabran")
            return
        }

        // Proveri da li je URL slike validan
        if (!isImageUriValid(imageUri)) {
            callback(false, "Molimo unesite sliku")
            return
        }
        // Implement logic to save data, for example, to a database or remote server
        viewModelScope.launch {
            // Example: Log data to console
            markerRepository.addLocationData(
                name = name,
                type = selectedOption,
                lat = lat,
                lng = lng,
                address = address,
                photo = imageUri,
                currentTime =  Timestamp.now()

            )
            callback(true, "Successfully added marker")
        }
    }

    fun applyFilters(currentUserLocationData: LocationData?, cb: (Boolean) -> Unit) {

        viewModelScope.launch {
            markerRepository.applyFilters(callback = cb,filteredName, filteredSelectedOption, dateRange, filteredRadius, currentUserLocationData)
        }
    }

    fun removeFilters() {
        viewModelScope.launch {
            markerRepository.removeFilters()
        }
    }

//    override fun onCleared() {
//        super.onCleared()
//        markerRepository.removeMarkersListener()
//    }

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

private fun isValidName(name: String): Boolean {
    return name.isNotBlank()
}

private fun isValidType(type: String): Boolean {
    val validTypes = listOf("Gradska Liga", "Okruzna Liga", "Zona Zapad", "Zona Istok", "Sprska Liga Istok", "Prva Liga")
    return validTypes.contains(type)
}

private fun isImageUriValid(uri: Uri?): Boolean {
    return uri != null
}