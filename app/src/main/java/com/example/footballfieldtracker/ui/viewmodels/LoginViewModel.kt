package com.example.footballfieldtracker.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.footballfieldtracker.data.model.User
import com.example.footballfieldtracker.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {

    var email by mutableStateOf("")
    var password by  mutableStateOf("")

    var passwordVisible by  mutableStateOf(false)


    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
        loadCurrentUser()
    }

    private fun setCurrentUser(user: User?) {
        _currentUser.value = user
    }

    // Funkcija za resetovanje stanja na početne vrednosti
    fun resetState() {
        email = ""
        password = ""
        passwordVisible = false
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

class LoginViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
