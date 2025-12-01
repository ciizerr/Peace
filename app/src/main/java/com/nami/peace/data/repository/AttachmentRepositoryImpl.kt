package com.nami.peace.data.repository

import com.nami.peace.data.local.AttachmentDao
import com.nami.peace.domain.model.Attachment
import com.nami.peace.domain.repository.AttachmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AttachmentRepositoryImpl @Inject constructor(
    private val dao: AttachmentDao
) : AttachmentRepository {

    override fun getAttachmentsForReminder(reminderId: Int): Flow<List<Attachment>> {
        return dao.getAttachmentsForReminder(reminderId).map { entities ->
            entities.map { Attachment.fromEntity(it) }
        }
    }
    
    override fun getAllAttachments(): Flow<List<Attachment>> {
        return dao.getAllAttachments().map { entities ->
            entities.map { Attachment.fromEntity(it) }
        }
    }

    override suspend fun insertAttachment(attachment: Attachment): Long {
        return dao.insert(attachment.toEntity())
    }

    override suspend fun deleteAttachment(attachment: Attachment) {
        dao.delete(attachment.toEntity())
    }
}
