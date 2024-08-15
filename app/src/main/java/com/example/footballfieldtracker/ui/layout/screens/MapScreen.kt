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

    // Definišite Location Client i Launcher za Permissions
    val locationClient = LocationServices.getFusedLocationProviderClient(context)
    val defaultLocationClient = remember {
        DefaultLocationClient(context, locationClient)
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true &&
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {

                // Start location updates if permissions are granted
                startLocationUpdates(defaultLocationClient, userViewModel)
            } else {
                handlePermissionRationale(context, permissions)

            }
        }
    )

    // Request permission and start location updates if permissions are granted
    LaunchedEffect(Unit) {
        if (hasLocationPermissions(context)) {

            // Start location updates if permissions are granted
            startLocationUpdates(defaultLocationClient, userViewModel)

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
        Marker(
            state = MarkerState(
                position = LatLng(
                    currentPosition.latitude,
                    currentPosition.longitude
                )
            )
        )
    }
}

fun hasLocationPermissions(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
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

fun startLocationUpdates(
    locationClient: DefaultLocationClient,
    userViewModel: UserViewModel
) {
    locationClient.getLocationUpdates(1000L)
        .catch { e -> e.printStackTrace() }
        .onEach { location ->
            Log.d("SERVICE", location.toString())
            val lat = location.latitude
            val long = location.longitude
            userViewModel.updateLocation(LocationData(lat, long))
        }
        .launchIn(CoroutineScope(Dispatchers.Main)) // Use appropriate coroutine scope
}
//@SuppressLint("CoroutineCreationDuringComposition")
//@Composable
//fun MapScreen(navController: NavController, userViewModel: UserViewModel) {
//
//    val locationData by userViewModel.locationData.collectAsState()
//
//
//
//
//
//    val currentPosition = locationData ?: LocationData(43.321445, 21.896104)
//    val cameraPositionState = rememberCameraPositionState {
//        position = CameraPosition.fromLatLngZoom(
//            LatLng(
//                currentPosition.latitude,
//                currentPosition.longitude
//            ), 100f
//        )
//    }
//
//    // Move camera to new location when locationData changes
//    LaunchedEffect(locationData) {
//        locationData?.let {
//            cameraPositionState.animate(
//                CameraUpdateFactory.newLatLngZoom(
//                    LatLng(
//                        it.latitude,
//                        it.longitude
//                    ), 15f // Adjust zoom level as needed
//                )
//            )
//        }
//    }
//    var uiSettings by remember { mutableStateOf(MapUiSettings()) }
//    var properties by remember {
//        mutableStateOf(MapProperties(mapType = MapType.NORMAL))
//    }
//    GoogleMap(
//        modifier = Modifier.fillMaxSize(),
//        cameraPositionState = cameraPositionState,
//        properties = properties,
//        uiSettings = uiSettings
//    ) {
//        Marker(
//            state = MarkerState(
//                position = LatLng(
//                    currentPosition.latitude,
//                    currentPosition.longitude
//                )
//            ),
//        )
//    }
//    val context = LocalContext.current
//    val locationClient = LocationServices.getFusedLocationProviderClient(context)
//    val defaultLocationClient = remember {
//        DefaultLocationClient(context, locationClient)
//    }
//
//
//    val requestPermissionLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestMultiplePermissions(),
//        onResult = { permissions ->
//            if (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true &&
//                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
//            ) {
//
//                // TODO: zovi fuesed location client
//                // Permissions granted, start location updates
//                defaultLocationClient.getLocationUpdates(1000L)
//                    .catch { e -> e.printStackTrace() }
//                    .onEach { location ->
//                        Log.d("SERVICE", location.toString())
//                        // Use location data, e.g., update UI or ViewModel
//                        val lat = location.latitude.toString()
//                        val long = location.longitude.toString()
//                        // Update ViewModel or handle location data here
//                        // You can use a SharedFlow or other mechanism to notify the ViewModel
//                        userViewModel.updateLocation(LocationData(lat.toDouble(), long.toDouble()))
//                    }
//                    .launchIn(CoroutineScope(Dispatchers.Main)) // Use appropriate coroutine scope
//            } else {
//                // ove dve stvari vracaju boolean, znai razlog da li trebamo poslati permission rational (reason)
////                The rationale is essentially a message explaining why your app needs a particular permission.
//                val rationalRequired = ActivityCompat.shouldShowRequestPermissionRationale(
//                    // ovaj context kaze gde da se pojavi
//                    context as MainActivity,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ) || ActivityCompat.shouldShowRequestPermissionRationale(
//                    context as MainActivity,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                )
//                if (rationalRequired) {
//                    // Ako stavis Ask me every time, prvi put ce ovo da izadje a svaki sledeci put ovo dole
//                    Toast.makeText(
//                        context,
//                        "Location Permission is required for this feature to work",
//                        Toast.LENGTH_LONG
//                    )
//                        .show()
//                } else {
//                    // mora rucno sad da namesti, kad ne dopustim second time, ovo ce mi se cesce pojavljivati
//                    // ako stavim Don't Allow nece da mi se poajvi pop-up, vec ova poruka
//                    Toast.makeText(
//                        context,
//                        "Location Permission is required, please enable it in the Android Settings",
//                        Toast.LENGTH_LONG
//                    )
//                        .show()
//                }
//            }
//        })
//
//
//    // Using LaunchedEffect to handle side effects
//    LaunchedEffect((ContextCompat.checkSelfPermission(
//        context,
//        Manifest.permission.ACCESS_FINE_LOCATION
//    ) == PackageManager.PERMISSION_GRANTED
//            &&
//            ContextCompat.checkSelfPermission(
//                context,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//            )) {
//        if (ContextCompat.checkSelfPermission(
//                context,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//            &&
//            ContextCompat.checkSelfPermission(
//                context,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            defaultLocationClient.getLocationUpdates(1000L)
//                .catch { e -> e.printStackTrace() }
//                .onEach { location ->
//                    Log.d("SERVICE", location.toString())
//                    // Use location data, e.g., update UI or ViewModel
//                    val lat = location.latitude.toString()
//                    val long = location.longitude.toString()
//                    // Update ViewModel or handle location data here
//                    // You can use a SharedFlow or other mechanism to notify the ViewModel
//                    userViewModel.updateLocation(LocationData(lat.toDouble(), long.toDouble()))
//                }
//                .launchIn(CoroutineScope(Dispatchers.Main)) // Use appropriate coroutine scope
//
//
//        } else {
//            requestPermissionLauncher.launch(
//                arrayOf(
//                    Manifest.permission.ACCESS_COARSE_LOCATION,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                )
//            )
//        }
//    }
//
//
//
//
//
//
//}

