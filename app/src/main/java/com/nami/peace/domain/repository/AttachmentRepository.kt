package com.nami.peace.domain.repository

import com.nami.peace.domain.model.Attachment
import kotlinx.coroutines.flow.Flow

interface AttachmentRepository {
    fun getAttachmentsForReminder(reminderId: Int): Flow<List<Attachment>>
    fun getAllAttachments(): Flow<List<Attachment>>
    suspend fun insertAttachment(attachment: Attachment): Long
    suspend fun deleteAttachment(attachment: Attachment)
}
