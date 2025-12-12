package com.nami.peace.domain.model

data class Reminder(
    val id: Int = 0,
    val title: String,
    val priority: PriorityLevel,
    val startTimeInMillis: Long,
    val recurrenceType: RecurrenceType,
    val isNagModeEnabled: Boolean,
    val nagIntervalInMillis: Long?,
    val nagTotalRepetitions: Int,
    val currentRepetitionIndex: Int = 0,
    val isCompleted: Boolean = false,
    val isEnabled: Boolean = true,
    val isInNestedSnoozeLoop: Boolean = false,
    val nestedSnoozeStartTime: Long? = null,
    val category: ReminderCategory = ReminderCategory.GENERAL,
    val isStrictSchedulingEnabled: Boolean = false,
    val dateInMillis: Long? = null,
    val daysOfWeek: List<Int> = emptyList(), // 1=Sun, 2=Mon, ..., 7=Sat
    val originalStartTimeInMillis: Long = startTimeInMillis, // Default to startTime for new/migration
    val completedTime: Long? = null,
    val isAbandoned: Boolean = false,
    val notes: String? = null
)
