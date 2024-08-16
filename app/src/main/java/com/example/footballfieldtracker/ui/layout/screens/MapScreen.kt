package com.example.footballfieldtracker.ui.layout.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.footballfieldtracker.MainActivity
import com.example.footballfieldtracker.data.model.LocationData
import com.example.footballfieldtracker.ui.viewmodels.UserViewModel
import com.example.locationserviceexample.utils.DefaultLocationClient
import com.example.locationserviceexample.utils.hasLocationPermissions
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun MapScreen(navController: NavController, userViewModel: UserViewModel) {
    // Dobijanje Location Data iz ViewModela
    val locationData by userViewModel.locationData.collectAsState()

    // Context za korišćenje u aktivnostima i za request permissions
    val context = LocalContext.current


    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true &&
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {

               userViewModel.updateLocation()
            } else {
                handlePermissionRationale(context, permissions)

            }
        }
    )

    // Request permission and start location updates if permissions are granted
    LaunchedEffect(Unit) {
        if (hasLocationPermissions(context)) {

            userViewModel.updateLocation()

        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    // Definišite poziciju kamere na osnovu locationData
    val currentPosition = locationData ?: LocationData(43.321445, 21.896104)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(
                currentPosition.latitude,
                currentPosition.longitude
            ), 15f // Adjust zoom level as needed
        )
    }

    // Move camera to new location when locationData changes
    LaunchedEffect(locationData) {
        locationData?.let {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        it.latitude,
                        it.longitude
                    ), 15f // Adjust zoom level as needed
                )
            )
        }
    }

    // Define map properties and UI settings
    var uiSettings by remember { mutableStateOf(MapUiSettings()) }
    var properties by remember {
        mutableStateOf(MapProperties(mapType = MapType.NORMAL))
    }

    // Display Google Map
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = properties,
        uiSettings = uiSettings
    ) {
        // Display marker only if locationData is not null
        locationData?.let {
            Marker(
                state = MarkerState(
                    position = LatLng(it.latitude, it.longitude)
                ),
                title="You are here"
            )
        }
    }
}


// Helper function to handle permission rationale
fun handlePermissionRationale(context: Context, permissions: Map<String, Boolean>) {
    val rationalRequired = ActivityCompat.shouldShowRequestPermissionRationale(
        context as MainActivity,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) || ActivityCompat.shouldShowRequestPermissionRationale(
        context as MainActivity,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    if (rationalRequired) {
        Toast.makeText(
            context,
            "Location Permission is required for this feature to work",
            Toast.LENGTH_LONG
        ).show()
    } else {
        Toast.makeText(
            context,
            "Location Permission is required, please enable it in the Android Settings",
            Toast.LENGTH_LONG
        ).show()
    }
}


