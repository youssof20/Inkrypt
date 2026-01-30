package com.betterappsstudio.inkrypt.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.betterappsstudio.inkrypt.data.MediaManager
import kotlinx.coroutines.launch

/**
 * Displays a gallery of images with thumbnails.
 */
@Composable
fun ImageGallery(
    imagePaths: List<String>,
    mediaManager: MediaManager,
    modifier: Modifier = Modifier,
    onImageClick: ((String) -> Unit)? = null
) {
    if (imagePaths.isEmpty()) return
    
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(imagePaths) { path ->
            ImageThumbnail(
                imagePath = path,
                mediaManager = mediaManager,
                onClick = { onImageClick?.invoke(path) }
            )
        }
    }
}

@Composable
private fun ImageThumbnail(
    imagePath: String,
    mediaManager: MediaManager,
    onClick: () -> Unit
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(imagePath) {
        scope.launch {
            bitmap = mediaManager.getImage(imagePath)
        }
    }
    
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "Image thumbnail",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

