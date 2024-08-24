package com.example.footballfieldtracker.ui.layout.topappbar

import android.util.Log
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.footballfieldtracker.R
import com.example.footballfieldtracker.ui.Screens
import com.example.footballfieldtracker.ui.viewmodels.FieldViewModel
import com.example.footballfieldtracker.ui.viewmodels.LoginViewModel
import com.example.footballfieldtracker.ui.viewmodels.MarkerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomAppBar(
    drawerState: DrawerState,
    loginViewModel: LoginViewModel,
    fieldViewModel: FieldViewModel,
    markerViewModel: MarkerViewModel,
    navController: NavHostController
) {

    val context = LocalContext.current

    var showLogoutDialog by remember { mutableStateOf(false) }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val coroutineScope = rememberCoroutineScope()

    val title = when (currentRoute) {
        Screens.GoogleMap.name -> "Google Map"
        Screens.Register.name -> "Register"
        Screens.Login.name -> "Login"
        Screens.Leadboard.name -> "Leadboard"
        Screens.Fields.name -> "Fields"
        Screens.Field.name -> fieldViewModel.selectedFieldState.name
        else -> "Redirecting..." // Podrazumevani naslov
    }

    TopAppBar(
        // ako imas vreme smisli bolje. bolje pitaj ako
        navigationIcon = {
            if (currentRoute in listOf(Screens.GoogleMap.name, Screens.Leadboard.name, Screens.Fields.name)) {
                IconButton(onClick = {
                    // Otvori drawer kada klikneš na ikonu
                    coroutineScope.launch {
                        drawerState.open()
                    }
                }) {
                    Icon(Icons.Filled.Menu, contentDescription = null)
                }
            } else if (currentRoute in listOf(Screens.Field.name)) {
                IconButton(onClick = {
                    navController.popBackStack()
                }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            }
        },
        title = { Text(text = title) },
        actions = {
            // Prikazuj logout dugme samo ako nismo na Login ili Register ekranu
            if (currentRoute !in listOf(Screens.Login.name, Screens.Register.name)) {
                IconButton(onClick = {
                    showLogoutDialog = true
                }) {
                    Icon(
                        painter = painterResource(R.drawable.logout_24),
                        contentDescription = "Logout"
                    )
                }
            }
        }
    )

    // Display the AlertDialog when showLogoutDialog is true
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = {
                // Handle dismiss
                showLogoutDialog = false
            },
            title = {
                Text(text = "Confirm Logout")
            },
            text = {
                Text(text = "Are you sure you want to log out?")
            },
            confirmButton = {
                Button(onClick = {
                    // Perform the logout action
                    loginViewModel.signOut { success ->
                        if (success) {
                            navController.navigate(Screens.Login.name) {
                            popUpTo(Screens.GoogleMap.name) { inclusive = true }
                            }
                            markerViewModel.removeFilters()
                            Toast.makeText(context, "Successfully signed out", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Sign out failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                    // Close the dialog after action
                    showLogoutDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = {
                    // Close the dialog
                    showLogoutDialog = false
                }) {
                    Text("No")
                }
            }
        )
    }
}