package com.nami.peace.data.repository

import com.nami.peace.data.local.SuggestionDao
import com.nami.peace.domain.model.Suggestion
import com.nami.peace.domain.repository.SuggestionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SuggestionRepositoryImpl @Inject constructor(
    private val suggestionDao: SuggestionDao
) : SuggestionRepository {
    
    override fun getPendingSuggestions(): Flow<List<Suggestion>> {
        return suggestionDao.getPendingSuggestions().map { entities ->
            entities.map { Suggestion.fromEntity(it) }
        }
    }
    
    override suspend fun getSuggestionById(id: Int): Suggestion? {
        return suggestionDao.getSuggestionById(id)?.let { Suggestion.fromEntity(it) }
    }
    
    override suspend fun insertSuggestion(suggestion: Suggestion): Long {
        return suggestionDao.insert(suggestion.toEntity())
    }
    
    override suspend fun updateSuggestion(suggestion: Suggestion) {
        suggestionDao.update(suggestion.toEntity())
    }
    
    override suspend fun deleteSuggestion(suggestion: Suggestion) {
        suggestionDao.delete(suggestion.toEntity())
    }
}
