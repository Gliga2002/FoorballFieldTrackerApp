package com.example.footballfieldtracker.ui.layout.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.footballfieldtracker.R
import com.example.footballfieldtracker.data.model.User
import com.example.footballfieldtracker.ui.Screens


data class DrawerMenu(val icon: Painter, val title: String, val route: String)


@Composable
fun DrawerContent(
    currentUser: User?,
    onAction: (String?) -> Unit
) {

    val menus = arrayOf(
        DrawerMenu(painterResource(id = R.drawable.map_24), "Google Map", Screens.GoogleMap.name),
        DrawerMenu(painterResource(id = R.drawable.leaderboard_24), "Leaderboard", Screens.Leaderboard.name),
        DrawerMenu(painterResource(id = R.drawable.sports_soccer_24), "Fields", Screens.Fields.name)
    )

    val name = "${currentUser?.firstName} ${currentUser?.lastName}"
    val imageUrl = currentUser?.photoPath
    val userEmail = currentUser?.email
    val phoneNumber = currentUser?.phoneNumber

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(270.dp)
            .background(Color(0xFFd2e8d4)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            // Tekst centriran u odnosu na celu širinu
            Text(
                text = "Welcome $name",
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            // IconButton skroz desno
            IconButton(
                onClick = { onAction(null) },
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
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop ,
            placeholder = rememberVectorPainter(image = Icons.Default.AccountCircle),
            error = rememberVectorPainter(image = Icons.Default.AccountCircle),
            contentDescription = null,
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
                    text = userEmail ?: "", // Dummy email tekst
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
                    text = phoneNumber ?: "", // Dummy broj telefona tekst
                )
            }
        }

    }
    Spacer(modifier = Modifier.height(12.dp))
    menus.forEach {
        NavigationDrawerItem(
            label = { Text(text = it.title) },
            icon = { Icon(painter = it.icon, contentDescription = null) },
            selected = false,
            onClick = {
                onAction(it.route)
            }
        )
    }

}