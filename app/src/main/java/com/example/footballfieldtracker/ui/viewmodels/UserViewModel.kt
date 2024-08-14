package com.example.footballfieldtracker.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.footballfieldtracker.data.model.User
import com.example.footballfieldtracker.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
        loadCurrentUser()
    }

    private fun setCurrentUser(user: User?) {
        _currentUser.value = user
    }

    fun loginUserWithEmailAndPassword(
        email: String,
        password: String,
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            userRepository.loginWithEmailAndPassword(email, password) { user ->
                if (user != null) {
                    setCurrentUser(user)
                    callback(true) // Success
                } else {
                    callback(false) // Failure
                }
            }
        }
    }

    fun registerUser(
        email: String,
        password: String,
        confirmPassword: String,
        username: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        imageUri: Uri?,
        callback: (Boolean) -> Unit
    ) {
        if (password != confirmPassword) {
            callback(false)
            return
        }

        viewModelScope.launch {
            userRepository.registerUser(
                email,
                password,
                username,
                firstName,
                lastName,
                phoneNumber,
                imageUri
            ) { success ->
                callback(success)
            }
        }
    }

    fun signOut(callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            userRepository.signOut { success ->
                if (success) {
                    // Clear current user data if sign-out is successful
                    _currentUser.value = null
                }
                callback(success)
            }
        }
    }

    // Hmmm
    fun loadCurrentUser(
    ) {
        viewModelScope.launch {
            userRepository.fetchCurrentUser { user ->
                setCurrentUser(user)
            }
        }

    }

}


    class CurrentUserViewModelFactory(
        private val userRepository: UserRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
                return UserViewModel(userRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }


