package com.nami.peace.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity = ReminderEntity::class,
            parentColumns = ["id"],
            childColumns = ["reminderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("reminderId")]
)
data class AttachmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reminderId: Int,
    val filePath: String,
    val thumbnailPath: String,
    val timestamp: Long,
    val mimeType: String
)
