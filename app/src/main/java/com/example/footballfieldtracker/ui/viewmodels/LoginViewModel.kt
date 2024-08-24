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
                    userRepository.updateCurrentUser(user) // Update the current user in the repository
                    // todo: ovo izmeni ovo sam radio zbog fieldViewModel
                    userRepository.startObservingUser()
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
                    userRepository.updateCurrentUser(null)
                }
                callback(success)
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
