package com.example.footballfieldtracker.ui.layout.screens.fieldsscreen

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.footballfieldtracker.R
import com.example.footballfieldtracker.data.model.Field
import com.example.footballfieldtracker.ui.Screens
import com.example.footballfieldtracker.ui.layout.util.FilterFieldDialog
import com.example.footballfieldtracker.ui.layout.util.extractAddressPart
import com.example.footballfieldtracker.ui.layout.util.formatDate
import com.example.footballfieldtracker.ui.viewmodels.FieldViewModel
import com.example.footballfieldtracker.ui.viewmodels.MarkerViewModel
import com.example.footballfieldtracker.ui.viewmodels.UserViewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun FieldsScreen(
    navController: NavHostController,
    userViewModel: UserViewModel,
    markerViewModel: MarkerViewModel,
    selectField: (Field) -> Unit,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    val currentUserLocation by userViewModel.currentUserLocation.collectAsState()

    var isFilteredDialogOpen by remember { mutableStateOf(false) }

    val filteredMarkers by markerViewModel.filteredMarkers.collectAsState()
    val markers by markerViewModel.markers.collectAsState(emptyList())

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn {
            val markersToDisplay =
                if (filteredMarkers.isNotEmpty()) filteredMarkers else markers
            items(markersToDisplay) { marker ->
                FieldCard(
                    field = marker,
                    navController = navController,
                    selectField = { selectField(it) }
                )
            }
        }

        if (isFilteredDialogOpen) {
            FilterFieldDialog(
                context = context,
                currentUserLocation = currentUserLocation,
                markerViewModel = markerViewModel,
                onDismiss = { isFilteredDialogOpen = false }
            )
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
            contentPadding = PaddingValues(10.dp) // No extra padding inside the button
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
    }
}


@Composable
fun FieldCard(
    field: Field,
    navController: NavHostController,
    selectField: (Field) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val formattedDate = formatDate(field.timeCreated)
    val formattedAddress = extractAddressPart(field.address)


    Box(
        modifier = modifier.padding(12.dp)
    ) {
        Card(
            modifier = modifier.clickable {
                selectField(field)
                navController.navigate(Screens.Field.name)
            }
        ) {
            Column(
                modifier = Modifier
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    val painter = rememberAsyncImagePainter(field.photo)
                    Image(
                        modifier = modifier
                            .size(64.dp)
                            .padding(8.dp)
                            .clip(MaterialTheme.shapes.small),
                        contentScale = ContentScale.Crop,
                        painter = painter,

                        // Content Description is not needed here - image is decorative, and setting a null content
                        // description allows accessibility services to skip this element during navigation.

                        contentDescription = null
                    )
                    Column(modifier = modifier) {
                        Text(
                            text = field.name,
                            style = MaterialTheme.typography.displayMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(

                            text = formattedAddress,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Row(
                        modifier = modifier
                            .padding(start = 0.dp, top = 0.dp, end = 24.dp, bottom = 0.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically

                    ) {
                        Text(text = "Rating: ${field.avgRating} (Reviews: ${field.reviewCount})")
                    }


                }
                if (expanded) {
                    Row(
                        modifier = modifier.padding(8.dp)
                    ) {
                        Text(
                            // ovo sam menjao
                            text = "by ${field.author}",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )


                        Text(
                            text = "at ${formattedDate}",
                            style = MaterialTheme.typography.bodyLarge
                        )

                    }
                }
            }
        }
        IconButton(
            onClick = { expanded = !expanded },
            modifier = Modifier
                .align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = "Expading Icon",
                tint = MaterialTheme.colorScheme.primary
            )
        }

    }
}



