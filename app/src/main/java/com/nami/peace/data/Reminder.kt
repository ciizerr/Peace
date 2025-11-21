package com.nami.peace.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ReminderType {
    Notification,
    Alarm
}

enum class Frequency {
    Once,
    Daily,
    Weekly
}

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val timeInMillis: Long,
    val type: ReminderType,
    val frequency: Frequency,
    val isCompleted: Boolean = false,
    val categoryId: Long? = null,
    val isEssential: Boolean = false,
    val completedAt: Long? = null
)