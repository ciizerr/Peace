package com.nami.peace.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for tracking user feedback on ML suggestions.
 * Used to improve future suggestion algorithms through learning.
 */
@Entity(tableName = "suggestion_feedback")
data class SuggestionFeedbackEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val suggestionId: Int,
    val suggestionType: SuggestionType,
    val wasAccepted: Boolean,
    val feedbackTimestamp: Long,
    val reminderId: Int?,
    val confidenceScore: Int
)
