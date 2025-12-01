package com.nami.peace.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nami.peace.domain.ml.SuggestionGenerator
import com.nami.peace.domain.repository.SuggestionRepository
import com.nami.peace.util.notification.SuggestionNotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.withTimeout

/**
 * Background worker to perform daily ML analysis and generate suggestions.
 * Analyzes user behavior patterns and creates actionable suggestions.
 * 
 * Features:
 * - Runs daily analysis of completion patterns
 * - Generates ML suggestions with confidence scores
 * - Stores suggestions in database
 * - Sends notification if new suggestions are available
 * - Implements 30-second timeout for analysis
 */
@HiltWorker
class AnalysisWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val suggestionGenerator: SuggestionGenerator,
    private val suggestionRepository: SuggestionRepository,
    private val suggestionNotificationHelper: SuggestionNotificationHelper
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "ml_analysis"
        private const val ANALYSIS_TIMEOUT_MILLIS = 30_000L // 30 seconds
        private const val TAG = "AnalysisWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            android.util.Log.d(TAG, "Starting ML analysis...")
            
            // Run analysis with timeout
            val suggestions = withTimeout(ANALYSIS_TIMEOUT_MILLIS) {
                suggestionGenerator.generateAllSuggestions()
            }
            
            android.util.Log.d(TAG, "Analysis complete. Generated ${suggestions.size} suggestions")
            
            // Store suggestions in database
            var newSuggestionsCount = 0
            suggestions.forEach { suggestion ->
                try {
                    suggestionRepository.insertSuggestion(suggestion)
                    newSuggestionsCount++
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "Failed to insert suggestion: ${suggestion.title}", e)
                }
            }
            
            android.util.Log.d(TAG, "Stored $newSuggestionsCount new suggestions")
            
            // Send notification if new suggestions are available
            if (newSuggestionsCount > 0) {
                suggestionNotificationHelper.showNewSuggestionsNotification(newSuggestionsCount)
                android.util.Log.d(TAG, "Sent notification for $newSuggestionsCount new suggestions")
            }
            
            Result.success()
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            android.util.Log.e(TAG, "Analysis timed out after ${ANALYSIS_TIMEOUT_MILLIS}ms", e)
            // Don't retry on timeout - wait for next scheduled run
            Result.failure()
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Analysis failed", e)
            // Retry on other errors
            Result.retry()
        }
    }
}
