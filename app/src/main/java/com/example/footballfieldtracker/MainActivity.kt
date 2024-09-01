package com.example.footballfieldtracker

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.footballfieldtracker.services.NearbyFieldsDetection
import com.example.footballfieldtracker.services.NearbyFieldsDetectionController
import com.example.footballfieldtracker.ui.FootballFieldApp
import com.example.footballfieldtracker.ui.theme.FootballFieldTrackerTheme
import com.example.footballfieldtracker.ui.viewmodels.FieldViewModel
import com.example.footballfieldtracker.ui.viewmodels.FieldViewModelFactory
import com.example.footballfieldtracker.ui.viewmodels.LoginViewModel
import com.example.footballfieldtracker.ui.viewmodels.LoginViewModelFactory
import com.example.footballfieldtracker.ui.viewmodels.MarkerViewModel
import com.example.footballfieldtracker.ui.viewmodels.MarkerViewModelFactory
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

    private val markerViewModel: MarkerViewModel by viewModels {
        MarkerViewModelFactory((application as MainApplication).container.markerRepository)
    }

    private val fieldViewModel: FieldViewModel by viewModels {
        FieldViewModelFactory(fieldRepository = (application as MainApplication).container.fieldRepository)
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


        val defaultnearbyfieldscontroller = object : NearbyFieldsDetectionController {
            override fun startNearbyFieldsDetectionService() {
                Intent(applicationContext, NearbyFieldsDetection::class.java).apply {
                    action = NearbyFieldsDetection.ACTION_START
                    startService(this)
                }
            }


            override fun stopNearbyFieldsDetectionService() {
                Intent(applicationContext, NearbyFieldsDetection::class.java).apply {
                    action = NearbyFieldsDetection.ACTION_STOP
                    startService(this)
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
                    FootballFieldApp(
                        loginViewModel,
                        registerViewModel,
                        userViewModel,
                        markerViewModel,
                        fieldViewModel,
                        defaultnearbyfieldscontroller
                    )
                }
            }
        }
    }
}


