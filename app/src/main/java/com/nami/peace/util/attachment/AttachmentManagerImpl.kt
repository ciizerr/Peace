package com.nami.peace.util.attachment

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttachmentManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AttachmentManager {
    
    companion object {
        private const val MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024L // 5MB
        private const val THUMBNAIL_SIZE = 200
        private const val ATTACHMENTS_DIR = "attachments"
        private const val THUMBNAILS_DIR = "thumbnails"
        private const val JPEG_QUALITY = 90
    }
    
    private val attachmentsDir: File by lazy {
        File(context.filesDir, ATTACHMENTS_DIR).apply {
            if (!exists()) mkdirs()
        }
    }
    
    private val thumbnailsDir: File by lazy {
        File(context.filesDir, THUMBNAILS_DIR).apply {
            if (!exists()) mkdirs()
        }
    }
    
    override suspend fun saveImage(uri: Uri, reminderId: Int): AttachmentPaths = withContext(Dispatchers.IO) {
        // Validate file size first
        if (!validateFileSize(uri)) {
            val sizeInMB = getFileSize(uri) / (1024.0 * 1024.0)
            throw FileSizeExceededException(
                "Image size (%.2f MB) exceeds maximum allowed size (5 MB)".format(sizeInMB)
            )
        }
        
        // Generate unique filename
        val timestamp = System.currentTimeMillis()
        val filename = "attachment_${reminderId}_${timestamp}.jpg"
        val thumbnailFilename = "thumb_${reminderId}_${timestamp}.jpg"
        
        // Load bitmap from URI
        val bitmap = loadBitmapFromUri(uri)
            ?: throw IOException("Failed to load image from URI")
        
        try {
            // Save full image
            val fullImageFile = File(attachmentsDir, filename)
            saveBitmap(bitmap, fullImageFile, JPEG_QUALITY)
            
            // Generate and save thumbnail
            val thumbnail = createThumbnail(bitmap, THUMBNAIL_SIZE)
            val thumbnailFile = File(thumbnailsDir, thumbnailFilename)
            saveBitmap(thumbnail, thumbnailFile, JPEG_QUALITY)
            
            // Clean up bitmaps
            thumbnail.recycle()
            bitmap.recycle()
            
            AttachmentPaths(
                filePath = fullImageFile.absolutePath,
                thumbnailPath = thumbnailFile.absolutePath
            )
        } catch (e: Exception) {
            // Clean up on failure
            bitmap.recycle()
            throw IOException("Failed to save image: ${e.message}", e)
        }
    }
    
    override suspend fun deleteAttachment(filePath: String, thumbnailPath: String): Unit = withContext(Dispatchers.IO) {
        try {
            // Delete full image
            val fullImageFile = File(filePath)
            if (fullImageFile.exists()) {
                fullImageFile.delete()
            }
            
            // Delete thumbnail
            val thumbnailFile = File(thumbnailPath)
            if (thumbnailFile.exists()) {
                thumbnailFile.delete()
            }
        } catch (e: Exception) {
            // Log error but don't throw - deletion is best-effort
            android.util.Log.e("AttachmentManager", "Error deleting attachment files", e)
        }
    }
    
    override suspend fun validateFileSize(uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            val size = getFileSize(uri)
            size in 1..MAX_FILE_SIZE_BYTES
        }
    }
    
    override suspend fun getFileSize(uri: Uri): Long {
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.available().toLong()
                } ?: -1L
            } catch (e: Exception) {
                -1L
            }
        }
    }
    
    /**
     * Loads a bitmap from a URI.
     */
    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Creates a thumbnail bitmap with the specified maximum dimension.
     * Maintains aspect ratio.
     */
    private fun createThumbnail(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        val scale = if (width > height) {
            maxSize.toFloat() / width
        } else {
            maxSize.toFloat() / height
        }
        
        val scaledWidth = (width * scale).toInt()
        val scaledHeight = (height * scale).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
    }
    
    /**
     * Saves a bitmap to a file with the specified quality.
     */
    private fun saveBitmap(bitmap: Bitmap, file: File, quality: Int) {
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.flush()
        }
    }
}
