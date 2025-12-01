package com.nami.peace.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "subtasks",
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
data class SubtaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reminderId: Int,
    val title: String,
    val isCompleted: Boolean = false,
    val order: Int,
    val createdAt: Long
)
