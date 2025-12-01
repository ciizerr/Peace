package com.nami.peace.ui.components

import android.graphics.Bitmap
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.nami.peace.domain.model.Attachment
import com.nami.peace.util.background.BackgroundImageManager
import kotlinx.coroutines.launch

/**
 * BackgroundWrapper wraps content with a blurred background based on attachments.
 * 
 * Features:
 * - Displays blurred background from attachment images
 * - Supports slideshow mode
 * - Falls back to theme background when no images or disabled
 * 
 * @param attachments List of attachments to use for background
 * @param blurIntensity Blur intensity (0-100)
 * @param slideshowEnabled Whether slideshow mode is enabled
 * @param backgroundImageManager Manager for loading background images
 * @param modifier Modifier for the composable
 * @param content Content to display on top of the background
 */
@Composable
fun BackgroundWrapper(
    attachments: List<Attachment>,
    blurIntensity: Int,
    slideshowEnabled: Boolean,
    backgroundImageManager: BackgroundImageManager?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    // Load background images
    LaunchedEffect(attachments, slideshowEnabled) {
        if (attachments.isEmpty() || backgroundImageManager == null) {
            currentBitmap = null
            return@LaunchedEffect
        }
        
        val filePaths = attachments.map { it.filePath }
        
        if (slideshowEnabled && filePaths.size > 1) {
            // Use slideshow flow
            backgroundImageManager.getSlideshowFlow(filePaths).collect { bitmap ->
                currentBitmap = bitmap
            }
        } else {
            // Load first image only
            scope.launch {
                currentBitmap = backgroundImageManager.loadImageAsBitmap(filePaths.first())
            }
        }
    }
    
    // Display with blurred background
    BlurredBackground(
        bitmap = currentBitmap,
        blurIntensity = blurIntensity,
        modifier = modifier,
        content = content
    )
}
