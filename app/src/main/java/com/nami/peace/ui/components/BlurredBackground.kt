package com.nami.peace.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.flow.Flow

/**
 * BlurredBackground displays an image with adjustable blur effect.
 * 
 * Note: For Android 12+ (API 31+), this could use RenderEffect for true blur.
 * For now, we use alpha/opacity as a simple alternative that works on all API levels.
 * 
 * @param bitmap The background image bitmap
 * @param blurIntensity Blur intensity (0-100), where 0 is no blur and 100 is maximum blur
 * @param modifier Modifier for the composable
 * @param content Content to display on top of the blurred background
 */
@Composable
fun BlurredBackground(
    bitmap: Bitmap?,
    blurIntensity: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Display background image
        // Note: True blur effect requires RenderEffect (API 31+) or external libraries
        // For simplicity and compatibility, we use alpha to simulate blur effect
        bitmap?.let {
            val alpha = 1f - (blurIntensity / 200f) // Reduce opacity as blur increases
            
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Background image",
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(alpha.coerceIn(0.3f, 1f)), // Keep minimum 30% opacity
                contentScale = ContentScale.Crop
            )
        }
        
        // Content on top
        content()
    }
}

/**
 * BlurredBackgroundWithSlideshow displays a slideshow of images with blur effect.
 * 
 * @param slideshowFlow Flow emitting bitmaps for slideshow
 * @param blurIntensity Blur intensity (0-100)
 * @param modifier Modifier for the composable
 * @param content Content to display on top of the blurred background
 */
@Composable
fun BlurredBackgroundWithSlideshow(
    slideshowFlow: Flow<Bitmap?>,
    blurIntensity: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val currentBitmap by slideshowFlow.collectAsState(initial = null)
    
    BlurredBackground(
        bitmap = currentBitmap,
        blurIntensity = blurIntensity,
        modifier = modifier,
        content = content
    )
}
