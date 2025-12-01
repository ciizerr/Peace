package com.nami.peace.domain.repository

import com.nami.peace.domain.model.Suggestion
import kotlinx.coroutines.flow.Flow

interface SuggestionRepository {
    fun getPendingSuggestions(): Flow<List<Suggestion>>
    suspend fun getSuggestionById(id: Int): Suggestion?
    suspend fun insertSuggestion(suggestion: Suggestion): Long
    suspend fun updateSuggestion(suggestion: Suggestion)
    suspend fun deleteSuggestion(suggestion: Suggestion)
}
