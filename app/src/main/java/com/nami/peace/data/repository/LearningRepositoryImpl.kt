package com.nami.peace.data.repository

import com.nami.peace.data.local.SuggestionFeedbackDao
import com.nami.peace.data.local.SuggestionType
import com.nami.peace.domain.model.SuggestionFeedback
import com.nami.peace.domain.repository.LearningRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.max

/**
 * Implementation of LearningRepository.
 * Manages ML suggestion learning through user feedback tracking.
 */
class LearningRepositoryImpl @Inject constructor(
    private val dao: SuggestionFeedbackDao
) : LearningRepository {

    override suspend fun recordFeedback(feedback: SuggestionFeedback): Long {
        return dao.insert(feedback.toEntity())
    }

    override fun getFeedbackByType(type: SuggestionType): Flow<List<SuggestionFeedback>> {
        return dao.getFeedbackByType(type).map { entities ->
            entities.map { SuggestionFeedback.fromEntity(it) }
        }
    }

    override suspend fun getAcceptanceRate(type: SuggestionType): Double {
        val acceptedCount = dao.getAcceptanceCountByType(type)
        val dismissedCount = dao.getDismissalCountByType(type)
        val totalCount = acceptedCount + dismissedCount
        
        return if (totalCount > 0) {
            acceptedCount.toDouble() / totalCount.toDouble()
        } else {
            0.5 // Default to 50% if no data
        }
    }

    override suspend fun getAverageAcceptedConfidence(type: SuggestionType): Double {
        return dao.getAverageAcceptedConfidenceByType(type) ?: 50.0
    }

    override suspend fun shouldThrottleSuggestionType(type: SuggestionType): Boolean {
        // Check feedback from last 7 days
        val sevenDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
        val recentFeedback = dao.getRecentFeedback(sevenDaysAgo)
            .filter { it.suggestionType == type }
        
        if (recentFeedback.isEmpty()) {
            return false // No recent feedback, don't throttle
        }
        
        val dismissalCount = recentFeedback.count { !it.wasAccepted }
        val dismissalRate = dismissalCount.toDouble() / recentFeedback.size.toDouble()
        
        // Throttle if dismissal rate > 70%
        return dismissalRate > 0.7
    }

    override suspend fun getRecommendedConfidenceThreshold(type: SuggestionType): Int {
        val acceptedFeedback = dao.getAcceptedFeedbackByType(type)
        val dismissedFeedback = dao.getDismissedFeedbackByType(type)
        
        if (acceptedFeedback.isEmpty() && dismissedFeedback.isEmpty()) {
            return 50 // Default threshold if no data
        }
        
        // Calculate average confidence of accepted suggestions
        val avgAcceptedConfidence = if (acceptedFeedback.isNotEmpty()) {
            acceptedFeedback.map { it.confidenceScore }.average()
        } else {
            50.0
        }
        
        // Calculate average confidence of dismissed suggestions
        val avgDismissedConfidence = if (dismissedFeedback.isNotEmpty()) {
            dismissedFeedback.map { it.confidenceScore }.average()
        } else {
            50.0
        }
        
        // If accepted suggestions have higher confidence, use that as threshold
        // Otherwise, use a conservative threshold
        val threshold = if (avgAcceptedConfidence > avgDismissedConfidence) {
            // Use midpoint between dismissed and accepted averages
            ((avgDismissedConfidence + avgAcceptedConfidence) / 2.0).toInt()
        } else {
            // Be more conservative
            max(60, avgAcceptedConfidence.toInt())
        }
        
        // Clamp to valid range
        return threshold.coerceIn(0, 100)
    }

    override suspend fun cleanupOldFeedback(): Int {
        val ninetyDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(90)
        return dao.deleteOldFeedback(ninetyDaysAgo)
    }
}
