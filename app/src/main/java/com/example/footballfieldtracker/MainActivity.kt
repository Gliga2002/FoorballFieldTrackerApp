package com.example.footballfieldtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.footballfieldtracker.ui.FootballApp
import com.example.footballfieldtracker.ui.FootballFieldApp
import com.example.footballfieldtracker.ui.theme.FootballFieldTrackerTheme
import com.example.footballfieldtracker.ui.viewmodels.LoginViewModel
import com.example.footballfieldtracker.ui.viewmodels.LoginViewModelFactory
import com.example.footballfieldtracker.ui.viewmodels.RegisterViewModel
import com.example.footballfieldtracker.ui.viewmodels.RegisterViewModelFactory
import com.example.footballfieldtracker.ui.viewmodels.UserViewModel
import com.example.footballfieldtracker.ui.viewmodels.UserViewModelFactory


class MainActivity : ComponentActivity() {

    private val loginViewModel: LoginViewModel by viewModels {
        LoginViewModelFactory((application as MainApplication).container.userRepository)
    }

    private val registerViewModel: RegisterViewModel by viewModels {
        RegisterViewModelFactory((application as MainApplication).container.userRepository)
    }

    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory((application as MainApplication).container.userRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContent {
            FootballFieldTrackerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FootballApp(loginViewModel, registerViewModel, userViewModel)
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FootballFieldTrackerTheme {
//        ProfileApp()
    }
}