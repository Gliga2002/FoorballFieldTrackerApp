package com.example.footballfieldtracker.ui.layout.topappbar

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
import com.example.footballfieldtracker.R
import com.example.footballfieldtracker.ui.Screens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomAppBar(drawerState: DrawerState, currentRoute: String) {
    val coroutineScope = rememberCoroutineScope()

    val title = when (currentRoute) {
        Screens.GoogleMap.name -> "Google Map"
        Screens.Register.name -> "Register"
        Screens.Login.name -> "Login"
        else -> "Football Field" // Podrazumevani naslov
    }

    TopAppBar(
        navigationIcon = {
            IconButton(onClick = {
                coroutineScope.launch {
                    drawerState.open()
                }
            }) {
                Icon(Icons.Filled.Menu, contentDescription = null)
            }
        },
        title = { Text(text = title) },
        actions = {
            IconButton(onClick = {
                // Akcija za logout ikonu
//                FirebaseAuth.getInstance().signOut()
//                navController.navigate(Screens.Login.name)
            }) {
                Icon(painter = painterResource(R.drawable.logout_24), contentDescription = "Logout")
            }
        }
    )
}