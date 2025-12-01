package com.nami.peace.domain.usecase

import com.nami.peace.domain.model.Attachment
import com.nami.peace.domain.repository.AttachmentRepository
import com.nami.peace.util.attachment.AttachmentManager
import javax.inject.Inject

/**
 * Use case for deleting an attachment.
 * 
 * This use case:
 * 1. Deletes the attachment record from the database
 * 2. Deletes the image file from storage
 * 3. Deletes the thumbnail file from storage
 */
class DeleteAttachmentUseCase @Inject constructor(
    private val attachmentRepository: AttachmentRepository,
    private val attachmentManager: AttachmentManager
) {
    /**
     * Deletes an attachment and its associated files.
     * 
     * @param attachment The attachment to delete
     */
    suspend operator fun invoke(attachment: Attachment) {
        // Delete from database first
        attachmentRepository.deleteAttachment(attachment)
        
        // Then delete files from storage
        attachmentManager.deleteAttachment(
            filePath = attachment.filePath,
            thumbnailPath = attachment.thumbnailPath
        )
    }
}
