package com.nami.peace.util.attachment

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * AttachmentManager handles file operations for image attachments.
 * 
 * Features:
 * - Image storage in app-private directory
 * - Thumbnail generation (200x200)
 * - File size validation (max 5MB)
 * - File deletion with cleanup
 */
interface AttachmentManager {
    /**
     * Saves an image from URI to app-private storage.
     * 
     * @param uri The source image URI
     * @param reminderId The reminder ID to associate with this attachment
     * @return AttachmentPaths containing full image and thumbnail paths
     * @throws FileSizeExceededException if image exceeds 5MB
     * @throws IOException if file operations fail
     */
    suspend fun saveImage(uri: Uri, reminderId: Int): AttachmentPaths
    
    /**
     * Deletes an attachment and its thumbnail from storage.
     * 
     * @param filePath The full image file path
     * @param thumbnailPath The thumbnail file path
     */
    suspend fun deleteAttachment(filePath: String, thumbnailPath: String)
    
    /**
     * Validates that an image file is under the size limit.
     * 
     * @param uri The image URI to validate
     * @return true if valid, false otherwise
     */
    suspend fun validateFileSize(uri: Uri): Boolean
    
    /**
     * Gets the file size in bytes for a given URI.
     * 
     * @param uri The file URI
     * @return File size in bytes, or -1 if unable to determine
     */
    suspend fun getFileSize(uri: Uri): Long
}

/**
 * Result of saving an attachment, containing paths to full image and thumbnail.
 */
data class AttachmentPaths(
    val filePath: String,
    val thumbnailPath: String
)

/**
 * Exception thrown when a file exceeds the maximum allowed size.
 */
class FileSizeExceededException(message: String) : Exception(message)
