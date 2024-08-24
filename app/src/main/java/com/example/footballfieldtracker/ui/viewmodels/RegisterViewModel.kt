package com.example.footballfieldtracker.ui.viewmodels

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.footballfieldtracker.data.repository.UserRepository
import kotlinx.coroutines.launch


class RegisterViewModel(private val userRepository: UserRepository) : ViewModel() {
    // Definišemo state za sve inpute samo zbo config changes je ovde!!!
    var email by mutableStateOf("")
    var password by  mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var username by  mutableStateOf("")
    var firstName by  mutableStateOf("")
    var lastName by  mutableStateOf("")
    var phoneNumber by  mutableStateOf("")
    var imageUri by  mutableStateOf<Uri?>(null)


    var passwordVisible by  mutableStateOf(false)
    var confirmPasswordVisible by  mutableStateOf(false)

    // Funkcija za resetovanje stanja na početne vrednosti
    fun resetState() {
        email = ""
        password = ""
        confirmPassword = ""
        username = ""
        firstName = ""
        lastName = ""
        phoneNumber = ""
        imageUri = null
        passwordVisible = false
        confirmPasswordVisible = false
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
        callback: (Boolean, String?) -> Unit
    ) {
        // Proveri da li e-mail ima validan format
        if (!isValidEmail(email)) {
            // Obavestite korisnika o nevažećem e-mailu
            callback(false, "E-mail nije ispravan")
            return
        }

        // Proveri da li lozinka zadovoljava minimalne kriterijume
        if (!isValidPassword(password)) {
            // Obavestite korisnika o nevalidnoj lozinci
            callback(false, "Sifra mora da sadrzi najmanje 6 karaktera" )
            return
        }

        // Proveri da li lozinke odgovaraju
        if (!doPasswordsMatch(password, confirmPassword)) {
            // Obavestite korisnika o neusklađenim lozinkama
            callback(false, "Sifre se ne poklapaju")
            return
        }

        // Proveri da li je imageUri prisutan
        if (!isImageUriValid(imageUri)) {
            // Obavestite korisnika da nije odabrana slika
            callback(false, "Molimo unesite sliku")
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
                if (success) {
                    callback(success, "Uspesno ste se registrovali")
                }

            }
        }
    }

}

class RegisterViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

private fun isValidEmail(email: String): Boolean {
    val emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z]+\\.[a-zA-Z]+"
    return email.matches(emailPattern.toRegex())
}

private fun isValidPassword(password: String): Boolean {
    // Firebase zahteva minimalno 6 karaktera
    val minLength = 6
    return password.length >= minLength
}

private fun doPasswordsMatch(password: String, confirmPassword: String): Boolean {
    return password == confirmPassword
}
private fun isImageUriValid(uri: Uri?): Boolean {
    return uri != null
}