package com.example.footballfieldtracker

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.footballfieldtracker.services.NearbyFieldsDetection
import com.example.footballfieldtracker.services.NearbyFieldsDetectionController
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
import com.google.android.gms.location.LocationServices

// Todo: za single screen gledaj lab vezbe za poi
class MainActivity : ComponentActivity() {
    // Iako se Application class kreira pre Activity, ti nisi postovao lyfecyle pristupanja resurima  tokom kreiranj, i izgleda da je tokom kompliacije ili izvrsenja, prvo pokusao da pristupi Application pre nego sto je on uopste kreiran
    // Todo: 1 greska je bila jer si pristupao container pre nego sto je on bio kreira, a ovim nacinom pristupas tek kada treba, ne radis ti to eksplicitno (!!!!pristupio si application container globaly, a trebao si sa lateinit!!!)

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


    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(
            userRepository = (application as MainApplication).container.userRepository,
            locationClient = (application as MainApplication).container.locationClient
        )
    }



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Todo: ili stavi ovde, ili sa lazy gore, nemoj nista da init pre ovog lyfecile, stavi onda lateinit
        val defaultnearbyfieldscontroller = object : NearbyFieldsDetectionController {

            @RequiresApi(Build.VERSION_CODES.O)
            override fun startNearbyFieldsDetectionService() {
                Intent(applicationContext, NearbyFieldsDetection::class.java).apply {
                    action = NearbyFieldsDetection.ACTION_START
                    startForegroundService(this)
                }
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun stopNearbyFieldsDetectionService() {
                Intent(applicationContext, NearbyFieldsDetection::class.java).apply {
                    action = NearbyFieldsDetection.ACTION_STOP
                    startForegroundService(this)
                }
            }
        }

        setContent {
            FootballFieldTrackerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FootballApp(
                        loginViewModel,
                        registerViewModel,
                        userViewModel,
                        markerViewModel,
                        defaultnearbyfieldscontroller)
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