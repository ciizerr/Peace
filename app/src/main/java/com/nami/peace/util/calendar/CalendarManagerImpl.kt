package com.nami.peace.util.calendar

import android.Manifest
import android.accounts.Account
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.CalendarListEntry
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.nami.peace.data.local.SyncOperationType
import com.nami.peace.data.repository.SyncQueueRepository
import com.nami.peace.data.repository.UserPreferencesRepository
import com.nami.peace.domain.model.Reminder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.UnknownHostException
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of CalendarManager for Google Calendar integration.
 */
@Singleton
class CalendarManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: UserPreferencesRepository,
    private val syncQueueRepository: SyncQueueRepository
) : CalendarManager {
    
    private var googleSignInClient: GoogleSignInClient? = null
    private var calendarService: Calendar? = null
    
    companion object {
        private const val TAG = "CalendarManagerImpl"
        private const val PEACE_CALENDAR_NAME = "Peace Reminders"
        private const val PEACE_CALENDAR_DESCRIPTION = "Reminders synced from Peace app"
        private const val PREF_KEY_LAST_SYNC = "calendar_last_sync"
        private const val PREF_KEY_SYNCED_COUNT = "calendar_synced_count"
        private const val PREF_KEY_CALENDAR_ID = "peace_calendar_id"
    }
    
    init {
        initializeGoogleSignIn()
    }
    
    private fun initializeGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(CalendarScopes.CALENDAR))
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }
    
    override suspend fun hasCalendarPermissions(): Boolean = withContext(Dispatchers.IO) {
        val readPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
        
        val writePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
        
        readPermission && writePermission
    }
    
    override suspend fun isAuthenticated(): Boolean = withContext(Dispatchers.IO) {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        account != null && GoogleSignIn.hasPermissions(
            account,
            Scope(CalendarScopes.CALENDAR)
        )
    }
    
    override suspend fun getAuthenticatedAccount(): Account? = withContext(Dispatchers.IO) {
        val signInAccount = GoogleSignIn.getLastSignedInAccount(context)
        signInAccount?.account
    }
    
    override suspend fun requestAuthentication(): Result<Account> = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account != null && GoogleSignIn.hasPermissions(
                    account,
                    Scope(CalendarScopes.CALENDAR)
                )
            ) {
                initializeCalendarService(account)
                Result.success(account.account!!)
            } else {
                // Return failure - actual sign-in must be triggered from Activity
                Result.failure(Exception("Google Sign-In must be initiated from Activity"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun signOut() = withContext(Dispatchers.IO) {
        googleSignInClient?.signOut()
        calendarService = null
    }
    
    private fun initializeCalendarService(account: GoogleSignInAccount) {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(CalendarScopes.CALENDAR)
        ).apply {
            selectedAccount = account.account
        }
        
        calendarService = Calendar.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Peace")
            .build()
    }
    
    override suspend fun getOrCreatePeaceCalendar(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val service = calendarService ?: return@withContext Result.failure(
                Exception("Calendar service not initialized. Please authenticate first.")
            )
            
            // Check if calendar already exists in preferences
            val savedCalendarId = preferencesRepository.getString(PREF_KEY_CALENDAR_ID)
            if (savedCalendarId != null) {
                // Verify it still exists
                try {
                    service.calendars().get(savedCalendarId).execute()
                    return@withContext Result.success(savedCalendarId)
                } catch (e: Exception) {
                    // Calendar was deleted, create new one
                }
            }
            
            // Search for existing Peace calendar
            val calendarList = service.calendarList().list().execute()
            val existingCalendar = calendarList.items?.find { 
                it.summary == PEACE_CALENDAR_NAME 
            }
            
            if (existingCalendar != null) {
                preferencesRepository.saveString(PREF_KEY_CALENDAR_ID, existingCalendar.id)
                return@withContext Result.success(existingCalendar.id)
            }
            
            // Create new calendar
            val calendar = com.google.api.services.calendar.model.Calendar().apply {
                summary = PEACE_CALENDAR_NAME
                description = PEACE_CALENDAR_DESCRIPTION
                timeZone = java.util.TimeZone.getDefault().id
            }
            
            val createdCalendar = service.calendars().insert(calendar).execute()
            preferencesRepository.saveString(PREF_KEY_CALENDAR_ID, createdCalendar.id)
            
            Result.success(createdCalendar.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun syncReminder(
        reminder: Reminder,
        calendarId: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Check permissions first
            if (!hasCalendarPermissions()) {
                Log.w(TAG, "Calendar permissions not granted")
                // Queue for later
                syncQueueRepository.queueSync(
                    reminderId = reminder.id,
                    operationType = SyncOperationType.CREATE
                )
                return@withContext Result.failure(
                    CalendarSyncException.PermissionDenied("Calendar permissions not granted")
                )
            }
            
            // Check authentication
            if (!isAuthenticated()) {
                Log.w(TAG, "Not authenticated with Google")
                syncQueueRepository.queueSync(
                    reminderId = reminder.id,
                    operationType = SyncOperationType.CREATE
                )
                return@withContext Result.failure(
                    CalendarSyncException.NotAuthenticated("Not authenticated with Google")
                )
            }
            
            val service = calendarService ?: return@withContext Result.failure(
                CalendarSyncException.ServiceNotInitialized("Calendar service not initialized")
            )
            
            val event = createEventFromReminder(reminder)
            val createdEvent = service.events().insert(calendarId, event).execute()
            
            Log.d(TAG, "Successfully synced reminder ${reminder.id} to calendar")
            Result.success(createdEvent.id)
        } catch (e: UnknownHostException) {
            Log.e(TAG, "Network error syncing reminder ${reminder.id}", e)
            // Queue for retry
            syncQueueRepository.queueSync(
                reminderId = reminder.id,
                operationType = SyncOperationType.CREATE
            )
            Result.failure(CalendarSyncException.NetworkError("No network connection", e))
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing reminder ${reminder.id}", e)
            
            // Queue for retry if error is retryable
            if (SyncRetryStrategy.isRetryableError(e)) {
                syncQueueRepository.queueSync(
                    reminderId = reminder.id,
                    operationType = SyncOperationType.CREATE
                )
            }
            
            Result.failure(wrapException(e))
        }
    }
    
    override suspend fun syncAllReminders(reminders: List<Reminder>): Result<Int> = 
        withContext(Dispatchers.IO) {
            try {
                // Check permissions first
                if (!hasCalendarPermissions()) {
                    Log.w(TAG, "Calendar permissions not granted for bulk sync")
                    // Queue all reminders
                    reminders.forEach { reminder ->
                        syncQueueRepository.queueSync(
                            reminderId = reminder.id,
                            operationType = SyncOperationType.CREATE
                        )
                    }
                    return@withContext Result.failure(
                        CalendarSyncException.PermissionDenied("Calendar permissions not granted")
                    )
                }
                
                // Check authentication
                if (!isAuthenticated()) {
                    Log.w(TAG, "Not authenticated for bulk sync")
                    reminders.forEach { reminder ->
                        syncQueueRepository.queueSync(
                            reminderId = reminder.id,
                            operationType = SyncOperationType.CREATE
                        )
                    }
                    return@withContext Result.failure(
                        CalendarSyncException.NotAuthenticated("Not authenticated with Google")
                    )
                }
                
                val calendarIdResult = getOrCreatePeaceCalendar()
                if (calendarIdResult.isFailure) {
                    return@withContext Result.failure(
                        calendarIdResult.exceptionOrNull() ?: Exception("Failed to get calendar")
                    )
                }
                
                val calendarId = calendarIdResult.getOrThrow()
                var successCount = 0
                val errors = mutableListOf<Exception>()
                
                reminders.forEach { reminder ->
                    val result = syncReminder(reminder, calendarId)
                    if (result.isSuccess) {
                        successCount++
                    } else {
                        result.exceptionOrNull()?.let { errors.add(it as Exception) }
                    }
                }
                
                // Update sync stats
                preferencesRepository.saveLong(PREF_KEY_LAST_SYNC, System.currentTimeMillis())
                preferencesRepository.saveInt(PREF_KEY_SYNCED_COUNT, successCount)
                
                Log.d(TAG, "Bulk sync completed: $successCount/${reminders.size} successful")
                
                if (successCount == 0 && errors.isNotEmpty()) {
                    // All failed, return first error
                    Result.failure(errors.first())
                } else {
                    Result.success(successCount)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in bulk sync", e)
                Result.failure(wrapException(e))
            }
        }
    
    override suspend fun updateCalendarEvent(
        eventId: String,
        reminder: Reminder,
        calendarId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!hasCalendarPermissions()) {
                syncQueueRepository.queueSync(
                    reminderId = reminder.id,
                    operationType = SyncOperationType.UPDATE,
                    eventId = eventId
                )
                return@withContext Result.failure(
                    CalendarSyncException.PermissionDenied("Calendar permissions not granted")
                )
            }
            
            if (!isAuthenticated()) {
                syncQueueRepository.queueSync(
                    reminderId = reminder.id,
                    operationType = SyncOperationType.UPDATE,
                    eventId = eventId
                )
                return@withContext Result.failure(
                    CalendarSyncException.NotAuthenticated("Not authenticated with Google")
                )
            }
            
            val service = calendarService ?: return@withContext Result.failure(
                CalendarSyncException.ServiceNotInitialized("Calendar service not initialized")
            )
            
            val event = createEventFromReminder(reminder)
            service.events().update(calendarId, eventId, event).execute()
            
            Log.d(TAG, "Successfully updated calendar event $eventId")
            Result.success(Unit)
        } catch (e: UnknownHostException) {
            Log.e(TAG, "Network error updating event $eventId", e)
            syncQueueRepository.queueSync(
                reminderId = reminder.id,
                operationType = SyncOperationType.UPDATE,
                eventId = eventId
            )
            Result.failure(CalendarSyncException.NetworkError("No network connection", e))
        } catch (e: Exception) {
            Log.e(TAG, "Error updating event $eventId", e)
            
            if (SyncRetryStrategy.isRetryableError(e)) {
                syncQueueRepository.queueSync(
                    reminderId = reminder.id,
                    operationType = SyncOperationType.UPDATE,
                    eventId = eventId
                )
            }
            
            Result.failure(wrapException(e))
        }
    }
    
    override suspend fun deleteCalendarEvent(
        eventId: String,
        calendarId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!hasCalendarPermissions()) {
                // For delete, we don't queue - just fail
                return@withContext Result.failure(
                    CalendarSyncException.PermissionDenied("Calendar permissions not granted")
                )
            }
            
            if (!isAuthenticated()) {
                return@withContext Result.failure(
                    CalendarSyncException.NotAuthenticated("Not authenticated with Google")
                )
            }
            
            val service = calendarService ?: return@withContext Result.failure(
                CalendarSyncException.ServiceNotInitialized("Calendar service not initialized")
            )
            
            service.events().delete(calendarId, eventId).execute()
            Log.d(TAG, "Successfully deleted calendar event $eventId")
            Result.success(Unit)
        } catch (e: UnknownHostException) {
            Log.e(TAG, "Network error deleting event $eventId", e)
            Result.failure(CalendarSyncException.NetworkError("No network connection", e))
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting event $eventId", e)
            Result.failure(wrapException(e))
        }
    }
    
    override suspend fun getSyncStats(): Pair<Long?, Int> = withContext(Dispatchers.IO) {
        val lastSync = preferencesRepository.getLong(PREF_KEY_LAST_SYNC)
        val syncedCount = preferencesRepository.getInt(PREF_KEY_SYNCED_COUNT) ?: 0
        Pair(lastSync, syncedCount)
    }
    
    private fun createEventFromReminder(reminder: Reminder): Event {
        val event = Event().apply {
            summary = reminder.title
            description = buildEventDescription(reminder)
            
            // Set start time
            val startDateTime = EventDateTime().apply {
                dateTime = com.google.api.client.util.DateTime(Date(reminder.startTimeInMillis))
                timeZone = java.util.TimeZone.getDefault().id
            }
            start = startDateTime
            
            // Set end time (1 hour after start by default)
            val endDateTime = EventDateTime().apply {
                dateTime = com.google.api.client.util.DateTime(Date(reminder.startTimeInMillis + 3600000))
                timeZone = java.util.TimeZone.getDefault().id
            }
            end = endDateTime
            
            // Add reminder notification
            val eventReminder = com.google.api.services.calendar.model.EventReminder()
            eventReminder.method = "popup"
            eventReminder.minutes = 0
            
            reminders = Event.Reminders().apply {
                useDefault = false
                overrides = listOf(eventReminder)
            }
        }
        
        return event
    }
    
    private fun buildEventDescription(reminder: Reminder): String {
        return buildString {
            append("Priority: ${reminder.priority}\n")
            append("Category: ${reminder.category}\n")
            append("Recurrence: ${reminder.recurrenceType}\n")
            
            if (reminder.isNagModeEnabled) {
                val intervalMinutes = (reminder.nagIntervalInMillis ?: 0) / 60000
                append("Nag Mode: ${reminder.nagTotalRepetitions} times, $intervalMinutes min interval\n")
            }
            
            append("\nSynced from Peace app")
        }
    }
    
    /**
     * Wrap generic exceptions into specific CalendarSyncException types.
     */
    private fun wrapException(e: Exception): CalendarSyncException {
        return when {
            e is CalendarSyncException -> e
            e is UnknownHostException -> CalendarSyncException.NetworkError("No network connection", e)
            e.message?.contains("permission", ignoreCase = true) == true -> 
                CalendarSyncException.PermissionDenied(e.message ?: "Permission denied", e)
            e.message?.contains("unauthorized", ignoreCase = true) == true ||
            e.message?.contains("401", ignoreCase = true) == true ||
            e.message?.contains("403", ignoreCase = true) == true -> 
                CalendarSyncException.NotAuthenticated(e.message ?: "Not authenticated", e)
            e.message?.contains("network", ignoreCase = true) == true ||
            e.message?.contains("timeout", ignoreCase = true) == true -> 
                CalendarSyncException.NetworkError(e.message ?: "Network error", e)
            else -> CalendarSyncException.Unknown(e.message ?: "Unknown error", e)
        }
    }
}

/**
 * Custom exceptions for calendar sync operations.
 */
sealed class CalendarSyncException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class PermissionDenied(message: String, cause: Throwable? = null) : CalendarSyncException(message, cause)
    class NotAuthenticated(message: String, cause: Throwable? = null) : CalendarSyncException(message, cause)
    class NetworkError(message: String, cause: Throwable? = null) : CalendarSyncException(message, cause)
    class ServiceNotInitialized(message: String, cause: Throwable? = null) : CalendarSyncException(message, cause)
    class Unknown(message: String, cause: Throwable? = null) : CalendarSyncException(message, cause)
}
