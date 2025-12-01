package com.nami.peace.domain.usecase

import com.nami.peace.domain.model.Reminder
import com.nami.peace.util.calendar.CalendarManager
import javax.inject.Inject

/**
 * Use case for syncing reminders to Google Calendar.
 */
class SyncToCalendarUseCase @Inject constructor(
    private val calendarManager: CalendarManager
) {
    /**
     * Sync a single reminder to Google Calendar.
     * @param reminder The reminder to sync
     * @return Result with event ID if successful
     */
    suspend fun syncReminder(reminder: Reminder): Result<String> {
        // Check if authenticated
        if (!calendarManager.isAuthenticated()) {
            return Result.failure(Exception("Not authenticated with Google"))
        }
        
        // Get or create Peace calendar
        val calendarResult = calendarManager.getOrCreatePeaceCalendar()
        if (calendarResult.isFailure) {
            return Result.failure(
                calendarResult.exceptionOrNull() ?: Exception("Failed to get calendar")
            )
        }
        
        val calendarId = calendarResult.getOrThrow()
        
        // Sync the reminder
        return calendarManager.syncReminder(reminder, calendarId)
    }
    
    /**
     * Sync all active reminders to Google Calendar.
     * @param reminders List of reminders to sync
     * @return Result with number of successfully synced reminders
     */
    suspend fun syncAllReminders(reminders: List<Reminder>): Result<Int> {
        // Check if authenticated
        if (!calendarManager.isAuthenticated()) {
            return Result.failure(Exception("Not authenticated with Google"))
        }
        
        // Filter only active reminders (not completed, enabled)
        val activeReminders = reminders.filter { it.isEnabled && !it.isCompleted }
        
        // Sync all reminders
        return calendarManager.syncAllReminders(activeReminders)
    }
    
    /**
     * Get sync statistics.
     * @return Pair of (last sync timestamp, number of synced events)
     */
    suspend fun getSyncStats(): Pair<Long?, Int> {
        return calendarManager.getSyncStats()
    }
}
