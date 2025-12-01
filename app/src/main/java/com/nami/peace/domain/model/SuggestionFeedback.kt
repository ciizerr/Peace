package com.nami.peace.domain.model

import com.nami.peace.data.local.SuggestionFeedbackEntity
import com.nami.peace.data.local.SuggestionType

/**
 * Domain model for suggestion feedback.
 * Tracks user acceptance/dismissal of ML suggestions for learning purposes.
 */
data class SuggestionFeedback(
    val id: Int = 0,
    val suggestionId: Int,
    val suggestionType: SuggestionType,
    val wasAccepted: Boolean,
    val feedbackTimestamp: Long = System.currentTimeMillis(),
    val reminderId: Int?,
    val confidenceScore: Int
) {
    fun toEntity(): SuggestionFeedbackEntity {
        return SuggestionFeedbackEntity(
            id = id,
            suggestionId = suggestionId,
            suggestionType = suggestionType,
            wasAccepted = wasAccepted,
            feedbackTimestamp = feedbackTimestamp,
            reminderId = reminderId,
            confidenceScore = confidenceScore
        )
    }

    companion object {
        fun fromEntity(entity: SuggestionFeedbackEntity): SuggestionFeedback {
            return SuggestionFeedback(
                id = entity.id,
                suggestionId = entity.suggestionId,
                suggestionType = entity.suggestionType,
                wasAccepted = entity.wasAccepted,
                feedbackTimestamp = entity.feedbackTimestamp,
                reminderId = entity.reminderId,
                confidenceScore = entity.confidenceScore
            )
        }
    }
}
