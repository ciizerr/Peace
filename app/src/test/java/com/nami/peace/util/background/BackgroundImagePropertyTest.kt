package com.nami.peace.util.background

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random

/**
 * Property-based tests for background image operations.
 * 
 * Tests the following correctness properties:
 * - Property 14: Background image application
 * 
 * Feature: peace-app-enhancement, Property 14: Background image application
 * Validates: Requirements 6.1
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class BackgroundImagePropertyTest {
    
    private lateinit var context: Context
    private lateinit var backgroundImageManager: BackgroundImageManager
    private val testFiles = mutableListOf<File>()
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        backgroundImageManager = BackgroundImageManagerImpl(context)
    }
    
    @After
    fun tearDown() {
        // Clean up test files
        testFiles.forEach { it.delete() }
        testFiles.clear()
        backgroundImageManager.clearCache()
    }
    
    /**
     * Helper function to create a test image file.
     */
    private fun createTestImageFile(width: Int = 100, height: Int = 100): File {
        val file = File(context.cacheDir, "test_image_${Random.nextInt()}.png")
        testFiles.add(file)
        
        // Create a simple bitmap
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.rgb(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256)))
        
        // Save to file
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        
        return file
    }
    
    /**
     * Property 14: Background image application
     * 
     * For any attachment image selected as background, the current screen should display that image as the background.
     * 
     * Test: For any valid image file, loading it as a background should return a non-null Bitmap.
     */
    @Test
    fun `Property 14 - Background image application - any valid image loads successfully`() = runBlocking {
        // Generate 10 random test images
        repeat(10) {
            val testFile = createTestImageFile(
                width = Random.nextInt(50, 500),
                height = Random.nextInt(50, 500)
            )
            
            // Load the image as background
            val bitmap = backgroundImageManager.loadImageAsBitmap(testFile.absolutePath)
            
            // Property: The image should load successfully
            assertNotNull("Image should load as bitmap", bitmap)
            assertTrue("Bitmap should have positive width", bitmap!!.width > 0)
            assertTrue("Bitmap should have positive height", bitmap.height > 0)
        }
    }
    
    /**
     * Property 14: Background image application - Caching
     * 
     * Test: For any image loaded twice, the second load should return the cached bitmap.
     */
    @Test
    fun `Property 14 - Background image caching - second load returns cached bitmap`() = runBlocking {
        repeat(5) {
            val testFile = createTestImageFile()
            val filePath = testFile.absolutePath
            
            // First load
            val bitmap1 = backgroundImageManager.loadImageAsBitmap(filePath)
            assertNotNull("First load should succeed", bitmap1)
            
            // Second load should return cached bitmap
            val cachedBitmap = backgroundImageManager.getCachedBitmap(filePath)
            assertNotNull("Cached bitmap should exist", cachedBitmap)
            assertSame("Second load should return same cached instance", bitmap1, cachedBitmap)
        }
    }
    
    /**
     * Property 14: Background image application - Invalid paths
     * 
     * Test: For any non-existent file path, loading should return null without crashing.
     */
    @Test
    fun `Property 14 - Background image application - invalid paths return null`() = runBlocking {
        val invalidPaths = listOf(
            "/non/existent/path/image.png",
            "",
            "/invalid/path/to/nowhere.jpg",
            "not_a_real_file.png",
            "/tmp/missing_${Random.nextInt()}.png"
        )
        
        invalidPaths.forEach { path ->
            val bitmap = backgroundImageManager.loadImageAsBitmap(path)
            assertNull("Invalid path should return null: $path", bitmap)
        }
    }
    
    /**
     * Property 14: Background image application - Slideshow
     * 
     * Test: For any list of valid images, slideshow should cycle through them.
     */
    @Test
    fun `Property 14 - Slideshow cycles through images`() = runBlocking {
        // Create 3 test images
        val testFiles = List(3) { createTestImageFile() }
        val filePaths = testFiles.map { it.absolutePath }
        
        // Get slideshow flow
        val slideshowFlow = backgroundImageManager.getSlideshowFlow(filePaths)
        
        // Collect first 3 emissions (should cycle through all images)
        val emissions = withTimeout(16000) { // 16 seconds timeout (3 images * 5 seconds + buffer)
            slideshowFlow.take(3).toList()
        }
        
        // Property: All emissions should be non-null bitmaps
        assertEquals("Should emit 3 bitmaps", 3, emissions.size)
        emissions.forEach { bitmap ->
            assertNotNull("Each emission should be a valid bitmap", bitmap)
        }
    }
    
    /**
     * Property 14: Background image application - Empty slideshow
     * 
     * Test: For an empty list of images, slideshow should emit null.
     */
    @Test
    fun `Property 14 - Empty slideshow emits null`() = runBlocking {
        val slideshowFlow = backgroundImageManager.getSlideshowFlow(emptyList())
        
        val firstEmission = withTimeout(1000) {
            slideshowFlow.first()
        }
        
        assertNull("Empty slideshow should emit null", firstEmission)
    }
    
    /**
     * Property 14: Background image application - Multiple images for slideshow
     * 
     * Test: For any list of images, loadImagesForSlideshow should load all valid images.
     */
    @Test
    fun `Property 14 - Load multiple images for slideshow`() = runBlocking {
        repeat(3) { iteration ->
            val imageCount = Random.nextInt(2, 6)
            val testFiles = List(imageCount) { createTestImageFile() }
            val filePaths = testFiles.map { it.absolutePath }
            
            val bitmaps = backgroundImageManager.loadImagesForSlideshow(filePaths)
            
            // Property: All valid images should load
            assertEquals(
                "All $imageCount images should load (iteration $iteration)",
                imageCount,
                bitmaps.size
            )
            bitmaps.forEach { bitmap ->
                assertNotNull("Each bitmap should be valid", bitmap)
                assertTrue("Bitmap should have positive dimensions", bitmap.width > 0 && bitmap.height > 0)
            }
        }
    }
    
    /**
     * Property 14: Background image application - Cache clearing
     * 
     * Test: After clearing cache, previously cached images should no longer be in cache.
     */
    @Test
    fun `Property 14 - Cache clearing removes all cached bitmaps`() = runBlocking {
        // Load several images
        val testFiles = List(3) { createTestImageFile() }
        val filePaths = testFiles.map { it.absolutePath }
        
        // Load all images (they get cached)
        filePaths.forEach { path ->
            backgroundImageManager.loadImageAsBitmap(path)
        }
        
        // Verify they're cached
        filePaths.forEach { path ->
            assertNotNull("Image should be cached before clear", backgroundImageManager.getCachedBitmap(path))
        }
        
        // Clear cache
        backgroundImageManager.clearCache()
        
        // Property: After clearing, cache should be empty
        filePaths.forEach { path ->
            assertNull("Image should not be cached after clear", backgroundImageManager.getCachedBitmap(path))
        }
    }
}
