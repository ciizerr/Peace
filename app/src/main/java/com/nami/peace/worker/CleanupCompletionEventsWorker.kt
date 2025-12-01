package com.nami.peace.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nami.peace.domain.usecase.TrackCompletionEventUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Background worker to clean up completion events older than 90 days.
 * Runs periodically to maintain the 90-day rolling window for ML analysis.
 */
@HiltWorker
class CleanupCompletionEventsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val trackCompletionEventUseCase: TrackCompletionEventUseCase
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val deletedCount = trackCompletionEventUseCase.cleanupOldEvents()
            
            // Log the cleanup result
            android.util.Log.d(
                "CleanupCompletionEvents",
                "Cleaned up $deletedCount old completion events"
            )
            
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e(
                "CleanupCompletionEvents",
                "Failed to clean up completion events",
                e
            )
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "cleanup_completion_events"
    }
}
