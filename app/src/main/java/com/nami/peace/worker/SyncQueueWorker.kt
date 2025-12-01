package com.nami.peace.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.nami.peace.domain.usecase.ProcessSyncQueueUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker for processing the calendar sync queue.
 * Runs periodically to retry failed sync operations.
 */
@HiltWorker
class SyncQueueWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val processSyncQueueUseCase: ProcessSyncQueueUseCase
) : CoroutineWorker(appContext, workerParams) {
    
    companion object {
        private const val TAG = "SyncQueueWorker"
        const val WORK_NAME = "sync_queue_worker"
        
        /**
         * Schedule periodic sync queue processing.
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val workRequest = PeriodicWorkRequestBuilder<SyncQueueWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
            
            Log.d(TAG, "Scheduled periodic sync queue processing")
        }
        
        /**
         * Cancel scheduled sync queue processing.
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "Cancelled sync queue processing")
        }
        
        /**
         * Trigger immediate sync queue processing.
         */
        fun triggerImmediate(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val workRequest = OneTimeWorkRequestBuilder<SyncQueueWorker>()
                .setConstraints(constraints)
                .build()
            
            WorkManager.getInstance(context).enqueue(workRequest)
            Log.d(TAG, "Triggered immediate sync queue processing")
        }
    }
    
    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting sync queue processing")
        
        return try {
            val result = processSyncQueueUseCase.processPendingSyncs()
            
            if (result.isSuccess) {
                val successCount = result.getOrThrow()
                Log.d(TAG, "Successfully processed $successCount syncs")
                Result.success()
            } else {
                val error = result.exceptionOrNull()
                Log.e(TAG, "Failed to process sync queue: ${error?.message}", error)
                
                // Retry if it's a retryable error
                if (error?.message?.contains("network", ignoreCase = true) == true ||
                    error?.message?.contains("timeout", ignoreCase = true) == true) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in sync queue worker", e)
            Result.retry()
        }
    }
}
