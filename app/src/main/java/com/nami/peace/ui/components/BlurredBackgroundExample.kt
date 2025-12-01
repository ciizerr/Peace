package com.nami.peace.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nami.peace.domain.model.Attachment
import com.nami.peace.util.background.BackgroundImageManager

/**
 * Example: Single background image with adjustable blur
 */
@Composable
fun SingleBackgroundExample(
    backgroundImageManager: BackgroundImageManager,
    attachment: Attachment
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var blurIntensity by remember { mutableIntStateOf(50) }
    
    LaunchedEffect(attachment.filePath) {
        bitmap = backgroundImageManager.loadImageAsBitmap(attachment.filePath)
    }
    
    BlurredBackground(
        bitmap = bitmap,
        blurIntensity = blurIntensity
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Blur Intensity: $blurIntensity",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Slider(
                    value = blurIntensity.toFloat(),
                    onValueChange = { blurIntensity = it.toInt() },
                    valueRange = 0f..100f,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
    }
}

/**
 * Example: Slideshow background with multiple images
 */
@Composable
fun SlideshowBackgroundExample(
    backgroundImageManager: BackgroundImageManager,
    attachments: List<Attachment>,
    blurIntensity: Int = 30
) {
    val filePaths = remember(attachments) {
        attachments.map { it.filePath }
    }
    
    val slideshowFlow = remember(filePaths) {
        backgroundImageManager.getSlideshowFlow(filePaths)
    }
    
    BlurredBackgroundWithSlideshow(
        slideshowFlow = slideshowFlow,
        blurIntensity = blurIntensity
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Slideshow: ${attachments.size} images\nChanging every 5 seconds",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Example: Conditional background based on preferences
 */
@Composable
fun ConditionalBackgroundExample(
    backgroundImageManager: BackgroundImageManager,
    attachments: List<Attachment>,
    backgroundEnabled: Boolean,
    slideshowEnabled: Boolean,
    blurIntensity: Int,
    content: @Composable () -> Unit
) {
    if (!backgroundEnabled || attachments.isEmpty()) {
        // No background, just show content
        content()
    } else if (slideshowEnabled && attachments.size > 1) {
        // Slideshow mode
        val filePaths = remember(attachments) {
            attachments.map { it.filePath }
        }
        
        val slideshowFlow = remember(filePaths) {
            backgroundImageManager.getSlideshowFlow(filePaths)
        }
        
        BlurredBackgroundWithSlideshow(
            slideshowFlow = slideshowFlow,
            blurIntensity = blurIntensity,
            content = content
        )
    } else {
        // Single image mode
        var bitmap by remember { mutableStateOf<Bitmap?>(null) }
        
        LaunchedEffect(attachments.first().filePath) {
            bitmap = backgroundImageManager.loadImageAsBitmap(attachments.first().filePath)
        }
        
        BlurredBackground(
            bitmap = bitmap,
            blurIntensity = blurIntensity,
            content = content
        )
    }
}
