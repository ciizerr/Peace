package com.nami.peace.domain.usecase

import com.nami.peace.data.repository.CompletionEventRepository
import com.nami.peace.domain.model.CompletionEvent
import com.nami.peace.domain.model.Reminder
import java.util.Calendar
import javax.inject.Inject

/**
 * Use case for tracking task completion events for ML pattern analysis.
 * Records completion data including timing, priority, category, and context.
 */
class TrackCompletionEventUseCase @Inject constructor(
    private val completionEventRepository: CompletionEventRepository
) {
    /**
     * Track a completion event for a reminder
     * 
     * @param reminder The reminder that was completed
     * @param completedTimeInMillis When the task was actually completed
     * @return The ID of the created completion event
     */
    suspend operator fun invoke(
        reminder: Reminder,
        completedTimeInMillis: Long = System.currentTimeMillis()
    ): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = completedTimeInMillis
        }
        
        // Calculate completion delay (positive = late, negative = early)
        val completionDelay = completedTimeInMillis - reminder.startTimeInMillis
        
        val event = CompletionEvent(
            reminderId = reminder.id,
            title = reminder.title,
            priority = reminder.priority,
            category = reminder.category,
            scheduledTimeInMillis = reminder.startTimeInMillis,
            completedTimeInMillis = completedTimeInMillis,
            completionDelayInMillis = completionDelay,
            wasNagMode = reminder.isNagModeEnabled,
            nagRepetitionIndex = if (reminder.isNagModeEnabled) reminder.currentRepetitionIndex else null,
            nagTotalRepetitions = if (reminder.isNagModeEnabled) reminder.nagTotalRepetitions else null,
            dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK), // 1=Sunday, 7=Saturday
            hourOfDay = calendar.get(Calendar.HOUR_OF_DAY), // 0-23
            wasRecurring = reminder.recurrenceType != com.nami.peace.domain.model.RecurrenceType.ONE_TIME,
            recurrenceType = reminder.recurrenceType
        )
        
        return completionEventRepository.recordCompletionEvent(event)
    }
    
    /**
     * Clean up old completion events (older than 90 days)
     * Should be called periodically
     */
    suspend fun cleanupOldEvents(): Int {
        return completionEventRepository.cleanupOldEvents()
    }
}
