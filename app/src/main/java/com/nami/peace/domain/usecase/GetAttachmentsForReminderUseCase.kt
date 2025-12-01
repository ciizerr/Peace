package com.nami.peace.domain.usecase

import com.nami.peace.domain.model.Attachment
import com.nami.peace.domain.repository.AttachmentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving all attachments for a reminder.
 * 
 * Attachments are returned in chronological order (oldest first).
 */
class GetAttachmentsForReminderUseCase @Inject constructor(
    private val attachmentRepository: AttachmentRepository
) {
    /**
     * Gets all attachments for a reminder as a Flow.
     * 
     * @param reminderId The reminder ID
     * @return Flow of attachments sorted by timestamp
     */
    operator fun invoke(reminderId: Int): Flow<List<Attachment>> {
        return attachmentRepository.getAttachmentsForReminder(reminderId)
    }
}
