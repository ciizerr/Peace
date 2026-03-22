package com.nami.peace.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.RecurrenceType
import com.nami.peace.domain.model.Reminder
import com.nami.peace.domain.model.ReminderCategory

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val originalTitle: String,
    val completedTime: Long,
    val status: String, // "Done" or "Missed"
    val priority: PriorityLevel = PriorityLevel.MEDIUM,
    val category: ReminderCategory = ReminderCategory.GENERAL,
    val nagInfo: String? = null
) {
    fun toDomain(): Reminder {
        return Reminder(
            id = id,
            title = originalTitle,
            priority = priority,
            startTimeInMillis = 0, // Not stored in history, using default
            recurrenceType = RecurrenceType.ONE_TIME,
            isNagModeEnabled = false,
            nagIntervalInMillis = null,
            nagTotalRepetitions = 0,
            completedTime = completedTime,
            isCompleted = status == "Done",
            isAbandoned = status == "Missed",
            category = category,
            notes = nagInfo
        )
    }

    companion object {
        fun fromDomain(reminder: Reminder): HistoryEntity {
            val nagInfoText = if (reminder.isNagModeEnabled) {
                val minutes = (reminder.nagIntervalInMillis ?: 0) / 60000
                "${reminder.nagTotalRepetitions} reps @ $minutes mins" + (reminder.notes?.let { " • $it" } ?: "")
            } else {
                reminder.notes
            }
            return HistoryEntity(
                id = 0, // Auto-generated
                originalTitle = reminder.title,
                completedTime = reminder.completedTime ?: System.currentTimeMillis(),
                status = if (reminder.isAbandoned) "Missed" else "Done",
                priority = reminder.priority,
                category = reminder.category,
                nagInfo = nagInfoText
            )
        }
    }
}
