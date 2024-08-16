package com.example.footballfieldtracker.ui.layout.screens.mapscreen

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.example.footballfieldtracker.MainActivity
import com.example.footballfieldtracker.R
import com.example.footballfieldtracker.data.model.LocationData
import com.example.footballfieldtracker.ui.viewmodels.UserViewModel
import com.example.locationserviceexample.utils.hasLocationPermissions
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


@Composable
fun MapScreen(navController: NavController, userViewModel: UserViewModel) {
    // Dobijanje Location Data iz ViewModela
    val locationData by userViewModel.locationData.collectAsState()

    var isDialogOpen by remember { mutableStateOf(false) }
    var isServiceRunning by remember { mutableStateOf(false) }

    // Context za korišćenje u aktivnostima i za request permissions
    val context = LocalContext.current

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

    Box(modifier = Modifier.fillMaxSize()) {
        // Google Map
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = properties,
            uiSettings = uiSettings
        ) {
            // Add markers or other map features here
            locationData?.let {
                Marker(
                    state = MarkerState(
                        position = LatLng(it.latitude, it.longitude)
                    ),
                    title = "You are here"
                )
            }
        }


        // TODO: ovde je pikazi ali align se budi ne prepoznaje da treba u ovaj scope
        IconButton(
            onClick = { isDialogOpen = true },
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


        Button(
            onClick = { /* Handle click event */ },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .size(56.dp), // Set the size to be circular
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 20.dp
            ),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(10.dp) // No extra padding inside the button
        ) {
            Icon(
                imageVector = Icons.Default.Search, // Replace with your desired icon
                contentDescription = "Filter",
                tint = Color.White // Adjust icon color if needed
            )
        }

        if (isDialogOpen) {
            AlertDialogComponent(
                isServiceRunning = isServiceRunning,
                onConfirm = {
                    isServiceRunning = !isServiceRunning
                },
                onDismiss = {
                    isDialogOpen = false
                }
            )
        }
    }

}

@Composable
fun AlertDialogComponent(
    isServiceRunning: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Nearby Fields Detection") },
        text = {
            Text(
                text = """
                    Are you sure you want to ${if (isServiceRunning) "stop" else "start"} the location tracking service?
                    ${if (isServiceRunning)
                        "This action will stop monitoring your location and you will no longer receive notifications about nearby objects."
                    else
                        "This will begin monitoring your current location and you'll receive notifications if any objects come within your vicinity."
                     }   
                        """.trimIndent(),
                textAlign = TextAlign.Left
            )
        },
        confirmButton = {
            Button(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text(if (isServiceRunning) "Stop Tracking" else "Start Tracking")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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


