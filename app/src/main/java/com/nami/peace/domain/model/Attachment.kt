package com.nami.peace.domain.model

import com.nami.peace.data.local.AttachmentEntity

data class Attachment(
    val id: Int = 0,
    val reminderId: Int,
    val filePath: String,
    val thumbnailPath: String,
    val timestamp: Long = System.currentTimeMillis(),
    val mimeType: String = "image/*"
) {
    fun toEntity(): AttachmentEntity {
        return AttachmentEntity(
            id = id,
            reminderId = reminderId,
            filePath = filePath,
            thumbnailPath = thumbnailPath,
            timestamp = timestamp,
            mimeType = mimeType
        )
    }

    companion object {
        fun fromEntity(entity: AttachmentEntity): Attachment {
            return Attachment(
                id = entity.id,
                reminderId = entity.reminderId,
                filePath = entity.filePath,
                thumbnailPath = entity.thumbnailPath,
                timestamp = entity.timestamp,
                mimeType = entity.mimeType
            )
        }
    }
}
