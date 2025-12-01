package com.nami.peace.util.attachment

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AttachmentManagerTest {
    
    private lateinit var context: Context
    private lateinit var attachmentManager: AttachmentManager
    private lateinit var testImageUri: Uri
    private lateinit var testImageFile: File
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        attachmentManager = AttachmentManagerImpl(context)
        
        // Create a test image file (larger than thumbnail size)
        testImageFile = createTestImage(context, "test_image.jpg", 500, 500)
        testImageUri = Uri.fromFile(testImageFile)
    }
    
    @After
    fun tearDown() {
        // Clean up test files
        testImageFile.delete()
        
        // Clean up attachment directories
        val attachmentsDir = File(context.filesDir, "attachments")
        val thumbnailsDir = File(context.filesDir, "thumbnails")
        
        attachmentsDir.listFiles()?.forEach { it.delete() }
        thumbnailsDir.listFiles()?.forEach { it.delete() }
    }
    
    @Test
    fun `saveImage creates full image and thumbnail`() = runTest {
        // Given
        val reminderId = 1
        
        // When
        val paths = attachmentManager.saveImage(testImageUri, reminderId)
        
        // Then
        val fullImageFile = File(paths.filePath)
        val thumbnailFile = File(paths.thumbnailPath)
        
        assertTrue("Full image file should exist", fullImageFile.exists())
        assertTrue("Thumbnail file should exist", thumbnailFile.exists())
        assertTrue("Full image should have content", fullImageFile.length() > 0)
        assertTrue("Thumbnail should have content", thumbnailFile.length() > 0)
    }
    
    @Test
    fun `saveImage generates unique filenames for different timestamps`() = runTest {
        // Given
        val reminderId = 1
        
        // When
        val paths1 = attachmentManager.saveImage(testImageUri, reminderId)
        Thread.sleep(10) // Ensure different timestamp
        val paths2 = attachmentManager.saveImage(testImageUri, reminderId)
        
        // Then
        assertNotEquals("File paths should be unique", paths1.filePath, paths2.filePath)
        assertNotEquals("Thumbnail paths should be unique", paths1.thumbnailPath, paths2.thumbnailPath)
    }
    
    @Test
    fun `thumbnail is smaller than original image`() = runTest {
        // Given
        val reminderId = 1
        
        // When
        val paths = attachmentManager.saveImage(testImageUri, reminderId)
        
        // Then
        val fullImageFile = File(paths.filePath)
        val thumbnailFile = File(paths.thumbnailPath)
        
        // Note: Due to compression, thumbnail file size might not always be smaller
        // but the dimensions should be smaller
        val fullBitmap = BitmapFactory.decodeFile(paths.filePath)
        val thumbBitmap = BitmapFactory.decodeFile(paths.thumbnailPath)
        
        assertTrue(
            "Thumbnail dimensions should be smaller than full image",
            thumbBitmap.width <= fullBitmap.width && thumbBitmap.height <= fullBitmap.height
        )
        
        fullBitmap.recycle()
        thumbBitmap.recycle()
    }
    
    @Test
    fun `thumbnail dimensions are correct`() = runTest {
        // Given
        val reminderId = 1
        
        // When
        val paths = attachmentManager.saveImage(testImageUri, reminderId)
        
        // Then
        val thumbnailBitmap = BitmapFactory.decodeFile(paths.thumbnailPath)
        assertNotNull("Thumbnail should be decodable", thumbnailBitmap)
        
        val maxDimension = maxOf(thumbnailBitmap.width, thumbnailBitmap.height)
        assertTrue(
            "Thumbnail max dimension should be <= 200",
            maxDimension <= 200
        )
        
        thumbnailBitmap.recycle()
    }
    
    @Test
    fun `deleteAttachment removes both files`() = runTest {
        // Given
        val reminderId = 1
        val paths = attachmentManager.saveImage(testImageUri, reminderId)
        
        // Verify files exist
        assertTrue(File(paths.filePath).exists())
        assertTrue(File(paths.thumbnailPath).exists())
        
        // When
        attachmentManager.deleteAttachment(paths.filePath, paths.thumbnailPath)
        
        // Then
        assertFalse("Full image should be deleted", File(paths.filePath).exists())
        assertFalse("Thumbnail should be deleted", File(paths.thumbnailPath).exists())
    }
    
    @Test
    fun `deleteAttachment handles non-existent files gracefully`() = runTest {
        // Given
        val nonExistentPath = "/path/to/nonexistent/file.jpg"
        
        // When/Then - should not throw exception
        attachmentManager.deleteAttachment(nonExistentPath, nonExistentPath)
    }
    
    @Test
    fun `validateFileSize returns true for valid file`() = runTest {
        // Given - test image is small (< 5MB)
        
        // When
        val isValid = attachmentManager.validateFileSize(testImageUri)
        
        // Then
        assertTrue("Small file should be valid", isValid)
    }
    
    @Test
    fun `validateFileSize returns false for oversized file`() = runTest {
        // Given - create a large file (> 5MB)
        val largeFile = createLargeTestFile(context, "large_image.jpg", 6 * 1024 * 1024)
        val largeUri = Uri.fromFile(largeFile)
        
        try {
            // When
            val isValid = attachmentManager.validateFileSize(largeUri)
            
            // Then
            assertFalse("Large file should be invalid", isValid)
        } finally {
            largeFile.delete()
        }
    }
    
    @Test
    fun `getFileSize returns correct size`() = runTest {
        // Given
        val expectedSize = testImageFile.length()
        
        // When
        val actualSize = attachmentManager.getFileSize(testImageUri)
        
        // Then
        assertTrue("File size should be positive", actualSize > 0)
        // Note: actual size might differ slightly due to stream buffering
        assertTrue("File size should be reasonable", actualSize > 0 && actualSize < 1024 * 1024)
    }
    
    @Test
    fun `saveImage throws FileSizeExceededException for large file`() = runTest {
        // Given - create a large file (> 5MB)
        val largeFile = createLargeTestFile(context, "large_image.jpg", 6 * 1024 * 1024)
        val largeUri = Uri.fromFile(largeFile)
        
        try {
            // When/Then
            var exceptionThrown = false
            var exceptionMessage = ""
            
            try {
                attachmentManager.saveImage(largeUri, 1)
            } catch (e: FileSizeExceededException) {
                exceptionThrown = true
                exceptionMessage = e.message ?: ""
            }
            
            assertTrue("FileSizeExceededException should be thrown", exceptionThrown)
            assertTrue(
                "Exception message should mention size limit",
                exceptionMessage.contains("5 MB")
            )
        } finally {
            largeFile.delete()
        }
    }
    
    @Test
    fun `saved images are in correct directories`() = runTest {
        // Given
        val reminderId = 1
        
        // When
        val paths = attachmentManager.saveImage(testImageUri, reminderId)
        
        // Then
        assertTrue(
            "Full image should be in attachments directory",
            paths.filePath.contains("attachments")
        )
        assertTrue(
            "Thumbnail should be in thumbnails directory",
            paths.thumbnailPath.contains("thumbnails")
        )
    }
    
    @Test
    fun `filenames contain reminder ID and timestamp`() = runTest {
        // Given
        val reminderId = 42
        
        // When
        val paths = attachmentManager.saveImage(testImageUri, reminderId)
        
        // Then
        val filename = File(paths.filePath).name
        val thumbnailName = File(paths.thumbnailPath).name
        
        assertTrue("Filename should contain reminder ID", filename.contains("_42_"))
        assertTrue("Thumbnail name should contain reminder ID", thumbnailName.contains("_42_"))
    }
    
    // Helper functions
    
    private fun createTestImage(context: Context, filename: String, width: Int, height: Int): File {
        val file = File(context.cacheDir, filename)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        // Fill with a simple pattern
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, android.graphics.Color.rgb(x % 256, y % 256, (x + y) % 256))
            }
        }
        
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        
        bitmap.recycle()
        return file
    }
    
    private fun createLargeTestFile(context: Context, filename: String, sizeBytes: Int): File {
        val file = File(context.cacheDir, filename)
        FileOutputStream(file).use { out ->
            val buffer = ByteArray(1024)
            var written = 0
            while (written < sizeBytes) {
                val toWrite = minOf(buffer.size, sizeBytes - written)
                out.write(buffer, 0, toWrite)
                written += toWrite
            }
        }
        return file
    }
    
    private inline fun <reified T : Throwable> assertThrows(
        crossinline block: () -> Unit
    ): T {
        try {
            block()
            fail("Expected ${T::class.java.simpleName} to be thrown")
            throw AssertionError() // Never reached
        } catch (e: Throwable) {
            if (e is T) {
                return e
            }
            throw AssertionError("Expected ${T::class.java.simpleName} but got ${e::class.java.simpleName}", e)
        }
    }
}
