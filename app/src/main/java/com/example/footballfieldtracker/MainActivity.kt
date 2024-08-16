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
import com.example.footballfieldtracker.ui.theme.FootballFieldTrackerTheme
import com.example.footballfieldtracker.ui.viewmodels.LoginViewModel
import com.example.footballfieldtracker.ui.viewmodels.LoginViewModelFactory
import com.example.footballfieldtracker.ui.viewmodels.MarkerViewModel
import com.example.footballfieldtracker.ui.viewmodels.MarkerViewModelFactory
import com.example.footballfieldtracker.ui.viewmodels.RegisterViewModel
import com.example.footballfieldtracker.ui.viewmodels.RegisterViewModelFactory
import com.example.footballfieldtracker.ui.viewmodels.UserViewModel
import com.example.footballfieldtracker.ui.viewmodels.UserViewModelFactory


class MainActivity : ComponentActivity() {
    // Todo: 1 greska je bila jer si pristupao container pre nego sto je on bio kreira, a ovim nacinom pristupas tek kada treba, ne radis ti to eksplicitno

    private val loginViewModel: LoginViewModel by viewModels {
        LoginViewModelFactory((application as MainApplication).container.userRepository)
    }

    private val registerViewModel: RegisterViewModel by viewModels {
        RegisterViewModelFactory((application as MainApplication).container.userRepository)
    }

    // nije htelo da radi jer je lazy, mora ga koristim dosta sam izgubio vreme seti se
    // Todo: 1 nastavak Lazy Initialization: Ako koristite by viewModels() delegat, ViewModel se inicijalizuje automatski kada je potreban. Ako se ViewModel ne koristi, možda se neće inicijalizovati.
    private val markerViewModel: MarkerViewModel by viewModels {
        MarkerViewModelFactory((application as MainApplication).container.markerRepository)
    }

    // ????
    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(
            userRepository = (application as MainApplication).container.userRepository,
            locationClient = (application as MainApplication).container.locationClient
        )
    }




    // stavi sve ovo u jedan objekat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FootballFieldTrackerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FootballApp(loginViewModel, registerViewModel, userViewModel, markerViewModel)
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