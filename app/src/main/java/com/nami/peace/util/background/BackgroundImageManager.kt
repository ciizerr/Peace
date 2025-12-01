package com.nami.peace.util.background

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow

/**
 * BackgroundImageManager handles background image operations.
 * 
 * Features:
 * - Image-to-background conversion
 * - Background image caching
 * - Slideshow management with 5-second intervals
 * - Integration with blur effects
 */
interface BackgroundImageManager {
    /**
     * Loads an image from file path as a Bitmap for background use.
     * 
     * @param filePath The full path to the image file
     * @return Bitmap of the image, or null if loading fails
     */
    suspend fun loadImageAsBitmap(filePath: String): Bitmap?
    
    /**
     * Loads multiple images for slideshow.
     * 
     * @param filePaths List of image file paths
     * @return List of successfully loaded Bitmaps
     */
    suspend fun loadImagesForSlideshow(filePaths: List<String>): List<Bitmap>
    
    /**
     * Gets a Flow that emits the current background image for slideshow.
     * Cycles through images every 5 seconds.
     * 
     * @param filePaths List of image file paths to cycle through
     * @return Flow emitting Bitmaps at 5-second intervals
     */
    fun getSlideshowFlow(filePaths: List<String>): Flow<Bitmap?>
    
    /**
     * Clears the background image cache.
     */
    fun clearCache()
    
    /**
     * Gets the cached bitmap for a file path if available.
     * 
     * @param filePath The image file path
     * @return Cached Bitmap or null if not in cache
     */
    fun getCachedBitmap(filePath: String): Bitmap?
}
