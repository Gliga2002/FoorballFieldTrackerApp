package com.example.footballfieldtracker.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.footballfieldtracker.data.model.LocationData
import com.example.footballfieldtracker.data.model.User
import com.example.footballfieldtracker.data.repository.UserRepository
import com.example.locationserviceexample.utils.LocationClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class UserViewModel(
    private val userRepository: UserRepository,
    private val locationClient: LocationClient
) : ViewModel() {



    val allUsers: StateFlow<List<User>> = userRepository.allUsers

    val currentUser: StateFlow<User?> = userRepository.currentUser

    val currentUserLocation: StateFlow<LocationData?> = userRepository.currentUserLocation


    init {
        Log.i("CurrentUserViewModel", "Ovde")
        loadCurrentUser()
        loadAllUsers()
    }

    fun isUserLoggedIn() : Boolean {
       return userRepository.isUserLoggedIn()
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            Log.i("CurrentUserViewModel", "Ovde")
            userRepository.startObservingUser()
            // ne moze ovako on je to sa cb, ili napravi onu funkciju da bude await ili sa cb

        }
    }


    // Dodao
    fun loadAllUsers() {
        viewModelScope.launch {
            userRepository.fetchAllUsers()
        }
    }

    fun updateLocation() {
        viewModelScope.launch {
            locationClient.getLocationUpdates(1000L)
                .catch { e -> Log.e("UserViewModel", "Location update error", e) }
                .onEach { location ->
                    Log.d("UserViewModel", "Location received: $location")
                    val lat = location.latitude
                    val long = location.longitude
                    userRepository.updateLocationData(LocationData(lat, long))
                }
                .launchIn(viewModelScope)
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Stop listening when ViewModel is cleared
        userRepository.stopFetchAllUsers()
        userRepository.stopObservingUser()
    }


}

class UserViewModelFactory(
    private val userRepository: UserRepository,
    private val locationClient: LocationClient
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(userRepository, locationClient) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}