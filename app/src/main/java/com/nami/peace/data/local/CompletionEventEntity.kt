package com.nami.peace.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nami.peace.domain.model.CompletionEvent
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.RecurrenceType
import com.nami.peace.domain.model.ReminderCategory

/**
 * Database entity for storing completion events for ML analysis.
 * Stores the last 90 days of completion history.
 */
@Entity(tableName = "completion_events")
data class CompletionEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reminderId: Int,
    val title: String,
    val priority: PriorityLevel,
    val category: ReminderCategory,
    val scheduledTimeInMillis: Long,
    val completedTimeInMillis: Long,
    val completionDelayInMillis: Long,
    val wasNagMode: Boolean,
    val nagRepetitionIndex: Int?,
    val nagTotalRepetitions: Int?,
    val dayOfWeek: Int,
    val hourOfDay: Int,
    val wasRecurring: Boolean,
    val recurrenceType: RecurrenceType
)

/**
 * Extension function to convert entity to domain model
 */
fun CompletionEventEntity.toDomain(): CompletionEvent {
    return CompletionEvent(
        id = id,
        reminderId = reminderId,
        title = title,
        priority = priority,
        category = category,
        scheduledTimeInMillis = scheduledTimeInMillis,
        completedTimeInMillis = completedTimeInMillis,
        completionDelayInMillis = completionDelayInMillis,
        wasNagMode = wasNagMode,
        nagRepetitionIndex = nagRepetitionIndex,
        nagTotalRepetitions = nagTotalRepetitions,
        dayOfWeek = dayOfWeek,
        hourOfDay = hourOfDay,
        wasRecurring = wasRecurring,
        recurrenceType = recurrenceType
    )
}

/**
 * Extension function to convert domain model to entity
 */
fun CompletionEvent.toEntity(): CompletionEventEntity {
    return CompletionEventEntity(
        id = id,
        reminderId = reminderId,
        title = title,
        priority = priority,
        category = category,
        scheduledTimeInMillis = scheduledTimeInMillis,
        completedTimeInMillis = completedTimeInMillis,
        completionDelayInMillis = completionDelayInMillis,
        wasNagMode = wasNagMode,
        nagRepetitionIndex = nagRepetitionIndex,
        nagTotalRepetitions = nagTotalRepetitions,
        dayOfWeek = dayOfWeek,
        hourOfDay = hourOfDay,
        wasRecurring = wasRecurring,
        recurrenceType = recurrenceType
    )
}
