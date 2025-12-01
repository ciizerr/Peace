package com.nami.peace.domain.repository

import com.nami.peace.data.local.SuggestionType
import com.nami.peace.domain.model.SuggestionFeedback
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing ML suggestion learning and feedback.
 * Tracks user acceptance/dismissal of suggestions to improve future recommendations.
 */
interface LearningRepository {
    /**
     * Record user feedback on a suggestion.
     */
    suspend fun recordFeedback(feedback: SuggestionFeedback): Long
    
    /**
     * Get all feedback for a specific suggestion type.
     */
    fun getFeedbackByType(type: SuggestionType): Flow<List<SuggestionFeedback>>
    
    /**
     * Get acceptance rate for a suggestion type (0.0 to 1.0).
     */
    suspend fun getAcceptanceRate(type: SuggestionType): Double
    
    /**
     * Get average confidence score of accepted suggestions for a type.
     */
    suspend fun getAverageAcceptedConfidence(type: SuggestionType): Double
    
    /**
     * Check if a suggestion type should be throttled based on recent dismissals.
     * Returns true if too many recent dismissals (>70% in last 7 days).
     */
    suspend fun shouldThrottleSuggestionType(type: SuggestionType): Boolean
    
    /**
     * Get recommended confidence threshold for a suggestion type based on learning.
     * Returns minimum confidence score (0-100) that should be used.
     */
    suspend fun getRecommendedConfidenceThreshold(type: SuggestionType): Int
    
    /**
     * Clean up old feedback data (older than 90 days).
     */
    suspend fun cleanupOldFeedback(): Int
}
