package com.nami.peace.domain.model

/**
 * Represents a task completion event for ML pattern analysis.
 * Tracks when tasks are completed and their characteristics at completion time.
 */
data class CompletionEvent(
    val id: Int = 0,
    val reminderId: Int,
    val title: String,
    val priority: PriorityLevel,
    val category: ReminderCategory,
    val scheduledTimeInMillis: Long,
    val completedTimeInMillis: Long,
    val completionDelayInMillis: Long, // How late/early was it completed
    val wasNagMode: Boolean,
    val nagRepetitionIndex: Int?, // Which repetition was completed (if nag mode)
    val nagTotalRepetitions: Int?, // Total repetitions (if nag mode)
    val dayOfWeek: Int, // 1=Sunday, 2=Monday, ..., 7=Saturday
    val hourOfDay: Int, // 0-23
    val wasRecurring: Boolean,
    val recurrenceType: RecurrenceType
)
