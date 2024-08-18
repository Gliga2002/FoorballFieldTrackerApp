package com.example.footballfieldtracker.ui.layout.screens.leadboardscreen

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.footballfieldtracker.data.model.User
import com.example.footballfieldtracker.ui.layout.screens.fieldsscreen.FieldCard
import com.example.footballfieldtracker.ui.viewmodels.UserViewModel

@Composable
fun LeadboardScreen(navController: NavHostController, userViewModel: UserViewModel) {
    val users by userViewModel.allUsers.collectAsState(emptyList())

    LazyColumn {
        items(users) { user ->
            UserCard(user = user)
        }
    }
}

// Todo: Svuda u aplikaciji da lepo pass modifier kao sto je predlozeno
@Composable
fun UserCard(user: User) {
    Card(
        modifier = Modifier.padding(12.dp).clickable { Log.d("Card", "Clicked") }
    ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                val painter = rememberAsyncImagePainter(user.photoPath)
                Image(
                    modifier = Modifier
                        .size(64.dp)
                        .padding(8.dp)
                        .clip(MaterialTheme.shapes.small),
                    contentScale = ContentScale.Crop,
                    painter = painter,

                    // Content Description is not needed here - image is decorative, and setting a null content
                    // description allows accessibility services to skip this element during navigation.

                    contentDescription = null
                )
                Column(modifier = Modifier) {
                    Text(
                        text = user.username,
                        style = MaterialTheme.typography.displayMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(

                        text = user.email,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 0.dp, top = 0.dp, end = 12.dp, bottom = 0.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically

                ) {
                    Text(text = "Score ${user.score}")
                }


            }
    }
}
