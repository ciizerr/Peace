package com.nami.peace.domain.model

import com.nami.peace.data.local.NoteEntity

data class Note(
    val id: Int = 0,
    val reminderId: Int,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toEntity(): NoteEntity {
        return NoteEntity(
            id = id,
            reminderId = reminderId,
            content = content,
            timestamp = timestamp
        )
    }

    companion object {
        fun fromEntity(entity: NoteEntity): Note {
            return Note(
                id = entity.id,
                reminderId = entity.reminderId,
                content = entity.content,
                timestamp = entity.timestamp
            )
        }
    }
}
