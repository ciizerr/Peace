package com.nami.peace.domain.usecase

import android.net.Uri
import com.nami.peace.domain.model.Attachment
import com.nami.peace.domain.repository.AttachmentRepository
import com.nami.peace.util.attachment.AttachmentManager
import javax.inject.Inject

/**
 * Use case for adding an image attachment to a reminder.
 * 
 * This use case:
 * 1. Validates the image file size (max 5MB)
 * 2. Saves the image to app-private storage
 * 3. Generates a thumbnail
 * 4. Stores the attachment metadata in the database
 */
class AddAttachmentUseCase @Inject constructor(
    private val attachmentRepository: AttachmentRepository,
    private val attachmentManager: AttachmentManager
) {
    /**
     * Adds an image attachment to a reminder.
     * 
     * @param uri The image URI from the image picker
     * @param reminderId The reminder to attach the image to
     * @return The ID of the newly created attachment
     * @throws FileSizeExceededException if image exceeds 5MB
     * @throws IOException if file operations fail
     */
    suspend operator fun invoke(uri: Uri, reminderId: Int): Long {
        // Save image and generate thumbnail
        val paths = attachmentManager.saveImage(uri, reminderId)
        
        // Create attachment model
        val attachment = Attachment(
            reminderId = reminderId,
            filePath = paths.filePath,
            thumbnailPath = paths.thumbnailPath,
            timestamp = System.currentTimeMillis(),
            mimeType = "image/jpeg"
        )
        
        // Store in database
        return attachmentRepository.insertAttachment(attachment)
    }
}
