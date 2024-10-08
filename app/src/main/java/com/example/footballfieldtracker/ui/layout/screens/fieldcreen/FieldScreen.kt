package com.example.footballfieldtracker.ui.layout.screens.fieldcreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.footballfieldtracker.data.model.Review
import com.example.footballfieldtracker.ui.layout.util.extractAddressPart
import com.example.footballfieldtracker.ui.layout.util.formatDate
import com.example.footballfieldtracker.ui.viewmodels.FieldViewModel
import com.example.footballfieldtracker.ui.viewmodels.UserViewModel


@Composable
fun FieldScreen(
    userViewModel: UserViewModel,
    fieldViewModel: FieldViewModel,
    modifier: Modifier = Modifier
) {

    // Pratite stanje iz ViewModel-a
    val selectedFieldState = fieldViewModel.selectedFieldState


    // Observe the state
    val selectedField by fieldViewModel.selectedField.collectAsState()
    val currentUser by userViewModel.currentUser.collectAsState()



    LaunchedEffect(selectedFieldState) {
        fieldViewModel.loadField(selectedFieldState.id)
    }


    var isReviewDialogOpen by remember { mutableStateOf(false) }

    val formattedDate = selectedField?.let { formatDate(it.timeCreated) }
    val formattedAddress = selectedField?.let { extractAddressPart(it.address) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Column {
            // Box with image and rating overlay
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Image(
                    painter = rememberImagePainter(selectedField?.photo),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                )

                // Rating overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(4.dp))
                        .padding(4.dp)
                ) {
                    Row {
                        Text(
                            text = "${selectedField?.avgRating} (${selectedField?.reviewCount} reviews)",
                            color = Color.White,
                            style = TextStyle(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Author and Date Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Spacer(modifier = Modifier.weight(1f)) // Pushes the Text to the right
                // cudno
                Text(
                    text = "by ${selectedField?.author} at $formattedDate",
                    style = MaterialTheme.typography.labelSmall,
                    fontStyle = FontStyle.Italic
                )
            }

            // Name, Type, and Address Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {

                Text(
                    text = "${selectedField?.name}, ${selectedField?.type} (${formattedAddress})",
                )
            }

            LazyColumn(
                modifier = Modifier
                    .padding(start = 16.dp, top = 8.dp, end = 0.dp, bottom = 8.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                selectedField?.let { field ->
                    items(field.reviews) { review ->
                        currentUser?.likedReviews?.let { likedReviews ->
                            FieldReview(
                                review = review,
                                hasUserLikedReview = likedReviews.contains(review.id),
                                handleLikedReview = { review, isLiked ->
                                    fieldViewModel.handleLikedReviews(
                                        review,
                                        isLiked
                                    )
                                }
                            )
                        }
                    }
                }

            }


        }

        // Add Button in the bottom right corner
        Button(
            onClick = { isReviewDialogOpen = true },
            modifier = Modifier
                .align(Alignment.BottomEnd) // Aligns the button to the bottom end
                .padding(16.dp)
                .size(56.dp), // Set the size to be circular
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 20.dp),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(5.dp) // No extra padding inside the button
        ) {
            Icon(
                imageVector = Icons.Default.Add, // Replace with your desired icon
                contentDescription = "Add",
                tint = Color.White // Adjust icon color if needed
            )
        }
    }
    if (isReviewDialogOpen) {
        selectedField?.let {
            ReviewDialog(
                onDismissRequest = { isReviewDialogOpen = false },
                fieldViewModel = fieldViewModel,
                fieldId = it.id
            )
        }
    }


}

@Composable
fun FieldReview(
    review: Review,
    hasUserLikedReview: Boolean,
    handleLikedReview: (Review, Boolean) -> Unit
) {


    Card(
        modifier = Modifier
            .padding(start = 8.dp, top = 8.dp, end = 0.dp, bottom = 8.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary, //Card background color
            contentColor = Color.White  //Card content color,e.g.text
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "By ${review.user}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            // Likes as bold stars
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {

                (1..5).forEach { index ->
                    Icon(
                        imageVector = if (index <= review.rating) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = null,
                        tint = if (index <= review.rating) Color.Yellow else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Comment text
            if (review.text.isNotEmpty()) {
                Text(
                    text = review.text,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center
                )
            }

            // Heart icon and like count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    imageVector = if (hasUserLikedReview) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (hasUserLikedReview) Color.Red else Color.Gray,
                    modifier = Modifier
                        .clickable {
                            handleLikedReview(review, !hasUserLikedReview)
                        }
                        .size(24.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (review.likes == 1) "${review.likes} like" else "${review.likes} likes",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

