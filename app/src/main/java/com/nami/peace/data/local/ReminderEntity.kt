package com.nami.peace.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.RecurrenceType
import com.nami.peace.domain.model.Reminder

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val priority: PriorityLevel,
    val startTimeInMillis: Long,
    val recurrenceType: RecurrenceType,
    val isNagModeEnabled: Boolean,
    val nagIntervalInMillis: Long?,
    val nagTotalRepetitions: Int,
    val currentRepetitionIndex: Int = 0,
    val isCompleted: Boolean = false,
    val isInNestedSnoozeLoop: Boolean = false,
    val nestedSnoozeStartTime: Long? = null
) {
    fun toDomain(): Reminder {
        return Reminder(
            id = id,
            title = title,
            priority = priority,
            startTimeInMillis = startTimeInMillis,
            recurrenceType = recurrenceType,
            isNagModeEnabled = isNagModeEnabled,
            nagIntervalInMillis = nagIntervalInMillis,
            nagTotalRepetitions = nagTotalRepetitions,
            currentRepetitionIndex = currentRepetitionIndex,
            isCompleted = isCompleted,
            isInNestedSnoozeLoop = isInNestedSnoozeLoop,
            nestedSnoozeStartTime = nestedSnoozeStartTime
        )
    }

    companion object {
        fun fromDomain(reminder: Reminder): ReminderEntity {
            return ReminderEntity(
                id = reminder.id,
                title = reminder.title,
                priority = reminder.priority,
                startTimeInMillis = reminder.startTimeInMillis,
                recurrenceType = reminder.recurrenceType,
                isNagModeEnabled = reminder.isNagModeEnabled,
                nagIntervalInMillis = reminder.nagIntervalInMillis,
                nagTotalRepetitions = reminder.nagTotalRepetitions,
                currentRepetitionIndex = reminder.currentRepetitionIndex,
                isCompleted = reminder.isCompleted,
                isInNestedSnoozeLoop = reminder.isInNestedSnoozeLoop,
                nestedSnoozeStartTime = reminder.nestedSnoozeStartTime
            )
        }
    }
}
