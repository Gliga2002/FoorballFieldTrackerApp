package com.example.footballfieldtracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.footballfieldtracker.data.model.LocationData
import com.example.footballfieldtracker.data.model.User
import com.example.footballfieldtracker.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    val currentUser: StateFlow<User?> = userRepository.currentUser

    val locationData: StateFlow<LocationData?> = userRepository.locationData

    fun updateLocation(locationData: LocationData) {
        userRepository.updateLocationData(locationData)
    }



    init {
        loadCurrentUser()
    }


    // Hmmm
    fun loadCurrentUser(
    ) {
        viewModelScope.launch {
            userRepository.fetchCurrentUser { user ->
                if (user != null) {
                    userRepository.updateCurrentUser(user) // Update the current user in the repository
                    _loading.value = false  // Loading is done
                }
            }
        }

    }

}

class UserViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}