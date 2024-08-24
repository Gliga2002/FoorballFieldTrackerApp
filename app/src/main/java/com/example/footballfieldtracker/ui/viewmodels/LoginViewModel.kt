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
    var password by mutableStateOf("")

    var passwordVisible by mutableStateOf(false)


    fun resetState() {
        email = ""
        password = ""
        passwordVisible = false
    }


    fun loginUserWithEmailAndPassword(
        email: String, password: String, callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            userRepository.loginWithEmailAndPassword(email, password) { success ->
                if (success) {
                    callback(true)
                } else {
                    callback(false)
                }

            }
        }

    }


    fun signOut(callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            userRepository.signOut { success ->
                if (success) {
                    callback(success)
                }

            }
        }
    }


}

class LoginViewModelFactory(private val userRepository: UserRepository) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
