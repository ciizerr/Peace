package com.nami.peace.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SuggestionFeedbackDao {
    @Insert
    suspend fun insert(feedback: SuggestionFeedbackEntity): Long
    
    @Query("SELECT * FROM suggestion_feedback WHERE suggestionType = :type ORDER BY feedbackTimestamp DESC")
    fun getFeedbackByType(type: SuggestionType): Flow<List<SuggestionFeedbackEntity>>
    
    @Query("SELECT * FROM suggestion_feedback WHERE suggestionType = :type AND wasAccepted = 1")
    suspend fun getAcceptedFeedbackByType(type: SuggestionType): List<SuggestionFeedbackEntity>
    
    @Query("SELECT * FROM suggestion_feedback WHERE suggestionType = :type AND wasAccepted = 0")
    suspend fun getDismissedFeedbackByType(type: SuggestionType): List<SuggestionFeedbackEntity>
    
    @Query("SELECT COUNT(*) FROM suggestion_feedback WHERE suggestionType = :type AND wasAccepted = 1")
    suspend fun getAcceptanceCountByType(type: SuggestionType): Int
    
    @Query("SELECT COUNT(*) FROM suggestion_feedback WHERE suggestionType = :type AND wasAccepted = 0")
    suspend fun getDismissalCountByType(type: SuggestionType): Int
    
    @Query("SELECT AVG(confidenceScore) FROM suggestion_feedback WHERE suggestionType = :type AND wasAccepted = 1")
    suspend fun getAverageAcceptedConfidenceByType(type: SuggestionType): Double?
    
    @Query("SELECT * FROM suggestion_feedback WHERE feedbackTimestamp >= :since ORDER BY feedbackTimestamp DESC")
    suspend fun getRecentFeedback(since: Long): List<SuggestionFeedbackEntity>
    
    @Query("DELETE FROM suggestion_feedback WHERE feedbackTimestamp < :before")
    suspend fun deleteOldFeedback(before: Long): Int
}
