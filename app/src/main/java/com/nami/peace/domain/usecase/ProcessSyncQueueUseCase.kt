package com.nami.peace.domain.usecase

import android.util.Log
import com.nami.peace.data.local.SyncOperationType
import com.nami.peace.data.repository.SyncQueueRepository
import com.nami.peace.domain.repository.ReminderRepository
import com.nami.peace.util.calendar.CalendarManager
import com.nami.peace.util.calendar.SyncRetryStrategy
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Use case for processing the calendar sync queue with retry logic.
 */
class ProcessSyncQueueUseCase @Inject constructor(
    private val syncQueueRepository: SyncQueueRepository,
    private val reminderRepository: ReminderRepository,
    private val calendarManager: CalendarManager
) {
    
    companion object {
        private const val TAG = "ProcessSyncQueue"
    }
    
    /**
     * Process all pending sync operations in the queue.
     * Applies exponential backoff retry strategy.
     * 
     * @return Number of successfully processed syncs
     */
    suspend fun processPendingSyncs(): Result<Int> {
        return try {
            // Check if we have permissions and authentication
            if (!calendarManager.hasCalendarPermissions()) {
                Log.w(TAG, "Cannot process sync queue: no calendar permissions")
                return Result.failure(Exception("Calendar permissions not granted"))
            }
            
            if (!calendarManager.isAuthenticated()) {
                Log.w(TAG, "Cannot process sync queue: not authenticated")
                return Result.failure(Exception("Not authenticated with Google"))
            }
            
            // Get calendar ID
            val calendarIdResult = calendarManager.getOrCreatePeaceCalendar()
            if (calendarIdResult.isFailure) {
                Log.e(TAG, "Failed to get calendar ID", calendarIdResult.exceptionOrNull())
                return Result.failure(
                    calendarIdResult.exceptionOrNull() ?: Exception("Failed to get calendar")
                )
            }
            val calendarId = calendarIdResult.getOrThrow()
            
            // Get pending syncs
            val pendingSyncs = syncQueueRepository.getPendingSyncs()
            Log.d(TAG, "Processing ${pendingSyncs.size} pending syncs")
            
            var successCount = 0
            
            for (sync in pendingSyncs) {
                // Check if we should retry this sync
                if (!SyncRetryStrategy.shouldRetry(sync.retryCount)) {
                    Log.w(TAG, "Sync ${sync.id} exceeded max retries, removing from queue")
                    syncQueueRepository.removeSync(sync.id)
                    continue
                }
                
                // Check if enough time has passed for retry
                if (!SyncRetryStrategy.canRetryNow(sync.lastRetryAt, sync.retryCount)) {
                    Log.d(TAG, "Sync ${sync.id} not ready for retry yet")
                    continue
                }
                
                // Mark as processing
                syncQueueRepository.markAsProcessing(sync.id)
                
                // Get the reminder
                val reminder = reminderRepository.getReminderById(sync.reminderId)
                if (reminder == null) {
                    Log.w(TAG, "Reminder ${sync.reminderId} not found, removing sync")
                    syncQueueRepository.removeSync(sync.id)
                    continue
                }
                
                // Process based on operation type
                val result = when (sync.operationType) {
                    SyncOperationType.CREATE -> {
                        calendarManager.syncReminder(reminder, calendarId)
                    }
                    SyncOperationType.UPDATE -> {
                        if (sync.eventId != null) {
                            calendarManager.updateCalendarEvent(sync.eventId, reminder, calendarId)
                                .map { sync.eventId }
                        } else {
                            Result.failure(Exception("Event ID missing for update operation"))
                        }
                    }
                    SyncOperationType.DELETE -> {
                        if (sync.eventId != null) {
                            calendarManager.deleteCalendarEvent(sync.eventId, calendarId)
                                .map { sync.eventId }
                        } else {
                            Result.failure(Exception("Event ID missing for delete operation"))
                        }
                    }
                }
                
                // Update sync queue based on result
                if (result.isSuccess) {
                    Log.d(TAG, "Successfully processed sync ${sync.id}")
                    syncQueueRepository.updateAfterRetry(sync.id, success = true)
                    successCount++
                } else {
                    val error = result.exceptionOrNull()
                    Log.e(TAG, "Failed to process sync ${sync.id}: ${error?.message}", error)
                    
                    // Check if error is retryable
                    if (error != null && SyncRetryStrategy.isRetryableError(error)) {
                        syncQueueRepository.updateAfterRetry(
                            sync.id,
                            success = false,
                            error = error.message
                        )
                        Log.d(TAG, "Sync ${sync.id} will be retried (attempt ${sync.retryCount + 1})")
                    } else {
                        // Non-retryable error, remove from queue
                        Log.w(TAG, "Sync ${sync.id} has non-retryable error, removing from queue")
                        syncQueueRepository.removeSync(sync.id)
                    }
                }
                
                // Small delay between operations to avoid rate limiting
                delay(100)
            }
            
            Log.d(TAG, "Processed $successCount/${pendingSyncs.size} syncs successfully")
            Result.success(successCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing sync queue", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get the count of pending syncs in the queue.
     */
    suspend fun getPendingSyncCount(): Int {
        return syncQueueRepository.getPendingSyncCount()
    }
    
    /**
     * Clear failed syncs that have exceeded max retry attempts.
     */
    suspend fun clearFailedSyncs() {
        syncQueueRepository.clearFailedSyncs()
    }
}
