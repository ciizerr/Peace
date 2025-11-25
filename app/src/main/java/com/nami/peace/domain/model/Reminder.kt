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
    val isInNestedSnoozeLoop: Boolean = false,
    val nestedSnoozeStartTime: Long? = null
)
