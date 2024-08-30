package com.example.footballfieldtracker.ui.layout.screens.mapscreen

import android.Manifest
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.example.footballfieldtracker.MainActivity
import com.example.footballfieldtracker.R
import com.example.footballfieldtracker.data.model.LocationData
import com.example.footballfieldtracker.services.NearbyFieldsDetectionController
import com.example.footballfieldtracker.ui.Screens
import com.example.footballfieldtracker.ui.layout.util.FilterFieldDialog
import com.example.footballfieldtracker.ui.viewmodels.FieldViewModel
import com.example.footballfieldtracker.ui.viewmodels.MarkerViewModel
import com.example.footballfieldtracker.ui.viewmodels.UserViewModel
import com.example.locationserviceexample.utils.hasLocationPermissions
import com.example.locationserviceexample.utils.reverseGeocodeLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

// Todo: Mora jer hoces startForegroundService, da hoces startService ne bi moralo ali probaj to posle
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MapScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    markerViewModel: MarkerViewModel,
    fieldViewModel: FieldViewModel,
    defaultnearbyfieldcontroller: NearbyFieldsDetectionController
) {

    // Context za korišćenje u aktivnostima i za request permissions
    val context = LocalContext.current

    val currentUserLocation by userViewModel.currentUserLocation.collectAsState()

    val filteredMarkers by markerViewModel.filteredMarkers.collectAsState()
    val markers by markerViewModel.markers.collectAsState()

    var isAddFieldDialogOpen by remember { mutableStateOf(false) }
    var isServiceDialogOpen by remember { mutableStateOf(false) }
    var isFilteredDialogOpen by remember { mutableStateOf(false) }

    var isServiceRunning by remember { mutableStateOf(getServiceRunningState(context)) } // Load state


    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true &&
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            ) {

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
    val currentPosition = currentUserLocation ?: LocationData(43.321445, 21.896104)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(
                currentPosition.latitude,
                currentPosition.longitude
            ), 15f // Adjust zoom level as needed
        )
    }

    // Move camera to new location when locationData changes
    LaunchedEffect(currentUserLocation) {
        currentUserLocation.let {
            if (it != null) {
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
    }


    // Define map properties and UI settings
    var uiSettings by remember { mutableStateOf(MapUiSettings()) }
    var properties by remember {
        mutableStateOf(MapProperties(mapType = MapType.NORMAL))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Google Map
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = properties,
            uiSettings = uiSettings,
            onMapLongClick = {
                isAddFieldDialogOpen = true
                markerViewModel.setLatLng(it)
                markerViewModel.setNewAddress(reverseGeocodeLocation(context = context, it))
            }
        ) {
            // Add markers or other map features here
            currentUserLocation?.let {
                Marker(
                    state = MarkerState(
                        position = LatLng(it.latitude, it.longitude)
                    ),
                    title = "You are here",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE) // Custom color for user marker
                )

            }

            // Add markers based on the filtered list or all markers
            val markersToDisplay =
                if (filteredMarkers.isNotEmpty()) filteredMarkers else markers
            markersToDisplay.forEach { marker ->
                Marker(
                    state = MarkerState(
                        position = LatLng(marker.latitude, marker.longitude)
                    ),
                    title = marker.name,
                    onClick = {
                        fieldViewModel.setCurrentFieldState(marker)
                        navController.navigate(Screens.Field.name)
                        true // Indicate that the click event was handled

                    }
                )
            }

        }


        // Da bih pokrenuo lokaction service, prethodno mora da bude odobreni fine i coarse location
        currentUserLocation?.let {
            IconButton(
                onClick = { isServiceDialogOpen = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp) // Adjust padding as needed
                    .size(40.dp) // Larger size for the button
            ) {
                Icon(
                    painter = if (isServiceRunning) painterResource(id = R.drawable.notifications_active_24) else painterResource(
                        id = R.drawable.notifications_24
                    ),
                    contentDescription = if (isServiceRunning) "Notifications" else "Notifications Off",
                    tint = MaterialTheme.colorScheme.primary, // Adjust icon color if needed
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Button(
            onClick = { isFilteredDialogOpen = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .size(56.dp), // Set the size to be circular
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 20.dp
            ),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(5.dp) // No extra padding inside the button
        ) {
            Icon(
                imageVector = Icons.Default.Search, // Replace with your desired icon
                contentDescription = "Filter",
                tint = Color.White // Adjust icon color if needed
            )
        }



        if (filteredMarkers.isNotEmpty()) {
            IconButton(
                onClick = { markerViewModel.removeFilters() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    // Todo: Smisli ako mozes bolji nacin
                    .padding(
                        top = 16.dp,
                        bottom = 24.dp,
                        start = 130.dp
                    ) // Adjust padding as needed
                    .size(40.dp) // Larger size for the button
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.search_off_24),
                    contentDescription = "Remove filters",
                    tint = MaterialTheme.colorScheme.primary, // Adjust icon color if needed
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        if (isServiceDialogOpen) {
            ServiceControlDialog(
                isServiceRunning = isServiceRunning,
                onConfirm = {
                    Log.d("AlertDialog", "Old isServiceRunning value: $isServiceRunning")
                    isServiceRunning = !isServiceRunning
                    Log.d("AlertDialog", "New isServiceRunning value: $isServiceRunning")
                    saveServiceRunningState(context, isServiceRunning) // Save the new state
                    if (isServiceRunning) {
                        defaultnearbyfieldcontroller.startNearbyFieldsDetectionService()
                    } else {
                        defaultnearbyfieldcontroller.stopNearbyFieldsDetectionService()
                    }
                },
                onDismiss = {
                    isServiceDialogOpen = false
                },
            )
        }

        if (isAddFieldDialogOpen) {
            AddFieldDialog(
                context,
                markerViewModel,
                onDismiss = { isAddFieldDialogOpen = false }
            )
        }

        if (isFilteredDialogOpen) {
            FilterFieldDialog(
                context = context,
                currentUserLocation = currentUserLocation,
                markerViewModel = markerViewModel,
                onDismiss = { isFilteredDialogOpen = false }
            )
        }


    }

}


// TODO: PROCITAJ O OVOME, I POKUSAJ DA KORISTIS DATA STORE TAKODJE ISTRAZI I PREZENTACIJE
fun saveServiceRunningState(context: Context, isRunning: Boolean) {
    val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putBoolean("isServiceRunning", isRunning)
    editor.apply()
}

fun getServiceRunningState(context: Context): Boolean {
    val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean("isServiceRunning", false)
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


