package com.example.footballfieldtracker.ui.layout.drawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.footballfieldtracker.ui.Screens

data class DrawerMenu(val icon: ImageVector, val title: String, val route: String)

val menus = arrayOf(
    DrawerMenu(Icons.Filled.Face, "Google Map", Screens.GoogleMap.name),
    DrawerMenu(Icons.Filled.Settings, "Register", Screens.Register.name),
    DrawerMenu(Icons.Filled.Info, "Login", Screens.Login.name)
)

@Composable
fun DrawerContent(
    menus: Array<DrawerMenu>,
    onMenuClick: (String) -> Unit,
    onClose: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(Color(0xFF3DDC84)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            // Tekst centriran u odnosu na celu širinu
            Text(
                text = "Darko Gligorijevic",
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            // IconButton skroz desno
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close"
                )
            }
        }
        // na pojedinacn text ces staviti color  Color(0xFF3ddc84)
        Image(
            modifier = Modifier.size(150.dp),
            imageVector = Icons.Filled.AccountCircle,
            contentScale = ContentScale.Crop,
            contentDescription = null
        )
        Column(
            modifier = Modifier
                .padding(4.dp) // Dodaj padding oko kolone
        ) {
            // Row za email
            Row(
                modifier = Modifier
                    .padding(vertical = 4.dp), // Padding samo vertikalno između redova
                verticalAlignment = Alignment.CenterVertically, // Poravnanje ikone i teksta po visini
            ) {
                Icon(
                    imageVector = Icons.Filled.Email, // Ikona za email
                    contentDescription = "Email Icon",
                    modifier = Modifier.size(24.dp) // Veličina ikone
                )
                Spacer(modifier = Modifier.width(8.dp)) // Razmak između ikone i teksta
                Text(
                    text = "email@example.com", // Dummy email tekst
                )
            }

            // Row za broj telefona
            Row(
                modifier = Modifier
                    .padding(vertical = 4.dp), // Padding samo vertikalno između redova
                verticalAlignment = Alignment.CenterVertically, // Poravnanje ikone i teksta po visini
            ) {
                Icon(
                    imageVector = Icons.Filled.Phone, // Ikona za telefon
                    contentDescription = "Phone Icon",
                    modifier = Modifier.size(24.dp) // Veličina ikone
                )
                Spacer(modifier = Modifier.width(8.dp)) // Razmak između ikone i teksta
                Text(
                    text = "+1234567890", // Dummy broj telefona tekst
                )
            }
        }

    }
    Spacer(modifier = Modifier.height(12.dp))
    menus.forEach {
        NavigationDrawerItem(
            label = { Text(text = it.title) },
            icon = { Icon(imageVector = it.icon, contentDescription = null) },
            selected = false,
            onClick = {
                onMenuClick(it.route)
            }
        )
    }

}