package com.example.footballfieldtracker.ui.layout.util

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun ImagePicker(imageUrl: Uri?, isEnabled: Boolean = true, onGalleryChange: (Uri) -> Unit) {
    val color = MaterialTheme.colorScheme

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onGalleryChange(it)
        }
    }

    Box(Modifier.height(140.dp)) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .border(3.dp, color.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop ,
                placeholder = rememberVectorPainter(image = Icons.Default.AccountCircle),
                error = rememberVectorPainter(image = Icons.Default.AccountCircle),
                contentDescription = null,
            )
        }
        IconButton(
            onClick = { launcher.launch("image/*") },
            enabled = isEnabled,
            modifier = Modifier
                .size(35.dp)
                .padding(2.dp)
                .clip(CircleShape)
                .align(Alignment.BottomEnd),
            colors = IconButtonDefaults.iconButtonColors(color.primary),
        ) {
            Icon(
                imageVector = Icons.Default.Face,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp),
                tint = color.onPrimary
            )
        }
    }

}