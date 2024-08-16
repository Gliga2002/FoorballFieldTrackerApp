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

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading
    val currentUser: StateFlow<User?> = userRepository.currentUser

    val locationData: StateFlow<LocationData?> = userRepository.locationData


    init {
        loadCurrentUser()
    }


    fun loadCurrentUser(
    ) {
        viewModelScope.launch {
            userRepository.fetchCurrentUser { user ->
                if (user != null) {
                    userRepository.updateCurrentUser(user) // Update the current user in the repository
                }
                _loading.value = false  // Loading is done
            }
        }

    }

    fun updateLocation() {
        // TODO: ovde je izvrsi uz potrebne modifikacije
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