package com.nami.peace.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nami.peace.domain.repository.LearningRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker to clean up old suggestion feedback data (older than 90 days).
 * Runs periodically to maintain database size and performance.
 */
@HiltWorker
class CleanupSuggestionFeedbackWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val learningRepository: LearningRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val deletedCount = learningRepository.cleanupOldFeedback()
            
            // Log cleanup result
            android.util.Log.d(
                "CleanupSuggestionFeedbackWorker",
                "Cleaned up $deletedCount old suggestion feedback records"
            )
            
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e(
                "CleanupSuggestionFeedbackWorker",
                "Failed to cleanup suggestion feedback",
                e
            )
            Result.retry()
        }
    }
}
