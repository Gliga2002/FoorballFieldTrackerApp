package com.example.footballfieldtracker.ui.layout.topappbar

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.footballfieldtracker.R
import com.example.footballfieldtracker.ui.Screens
import com.example.footballfieldtracker.ui.viewmodels.LoginViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomAppBar(
    drawerState: DrawerState,
    loginViewModel: LoginViewModel,
    navController: NavHostController
) {

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        ?: Screens.Register.name
    val coroutineScope = rememberCoroutineScope()

    val title = when (currentRoute) {
        Screens.GoogleMap.name -> "Google Map"
        Screens.Register.name -> "Register"
        Screens.Login.name -> "Login"
        else -> "Football Field" // Podrazumevani naslov
    }

    TopAppBar(
        navigationIcon = {
            if (currentRoute !in listOf(Screens.Login.name, Screens.Register.name)) {
                IconButton(onClick = {
                    // Otvori drawer kada klikneš na ikonu
                    coroutineScope.launch {
                        drawerState.open()
                    }
                }) {
                    Icon(Icons.Filled.Menu, contentDescription = null)
                }
            }
        },
        title = { Text(text = title) },
        actions = {
            // Prikazuj logout dugme samo ako nismo na Login ili Register ekranu
            if (currentRoute !in listOf(Screens.Login.name, Screens.Register.name)) {
                IconButton(onClick = {
                    // Izvrši logout operaciju i navigiraj na Login ekran
                    loginViewModel.signOut { success ->
                        if (success) {
                            navController.navigate(Screens.Login.name) {
                                popUpTo(Screens.GoogleMap.name) { inclusive = true }
                            }
                        } else {
                            Log.d("Greska", "Greska prilikom sign out")
                        }
                    }
                }) {
                    Icon(
                        painter = painterResource(R.drawable.logout_24),
                        contentDescription = "Logout"
                    )
                }
            }
        }
    )
}