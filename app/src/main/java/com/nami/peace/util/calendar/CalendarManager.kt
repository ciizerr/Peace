package com.nami.peace.util.calendar

import android.accounts.Account
import com.nami.peace.domain.model.Reminder

/**
 * Interface for managing Google Calendar integration.
 * Handles OAuth authentication, calendar creation, and event synchronization.
 */
interface CalendarManager {
    /**
     * Check if the user has granted calendar permissions.
     */
    suspend fun hasCalendarPermissions(): Boolean
    
    /**
     * Check if the user is authenticated with Google account.
     */
    suspend fun isAuthenticated(): Boolean
    
    /**
     * Get the currently authenticated Google account.
     */
    suspend fun getAuthenticatedAccount(): Account?
    
    /**
     * Request Google Sign-In for calendar access.
     * @return true if authentication successful, false otherwise
     */
    suspend fun requestAuthentication(): Result<Account>
    
    /**
     * Sign out from Google account.
     */
    suspend fun signOut()
    
    /**
     * Create or get the "Peace Reminders" calendar.
     * @return Calendar ID if successful, null otherwise
     */
    suspend fun getOrCreatePeaceCalendar(): Result<String>
    
    /**
     * Sync a single reminder to Google Calendar.
     * @param reminder The reminder to sync
     * @param calendarId The target calendar ID
     * @return Event ID if successful
     */
    suspend fun syncReminder(reminder: Reminder, calendarId: String): Result<String>
    
    /**
     * Sync all active reminders to Google Calendar.
     * @param reminders List of reminders to sync
     * @return Number of successfully synced reminders
     */
    suspend fun syncAllReminders(reminders: List<Reminder>): Result<Int>
    
    /**
     * Update an existing calendar event.
     * @param eventId The event ID to update
     * @param reminder The updated reminder data
     * @param calendarId The calendar ID
     */
    suspend fun updateCalendarEvent(
        eventId: String,
        reminder: Reminder,
        calendarId: String
    ): Result<Unit>
    
    /**
     * Delete a calendar event.
     * @param eventId The event ID to delete
     * @param calendarId The calendar ID
     */
    suspend fun deleteCalendarEvent(eventId: String, calendarId: String): Result<Unit>
    
    /**
     * Get sync statistics.
     * @return Pair of (last sync timestamp, number of synced events)
     */
    suspend fun getSyncStats(): Pair<Long?, Int>
}
