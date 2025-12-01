package com.nami.peace.domain.usecase

import com.nami.peace.data.local.SuggestionStatus
import com.nami.peace.domain.model.Suggestion
import com.nami.peace.domain.model.SuggestionFeedback
import com.nami.peace.domain.repository.LearningRepository
import com.nami.peace.domain.repository.SuggestionRepository
import javax.inject.Inject

/**
 * Use case for applying a suggestion.
 * Records acceptance feedback for learning and updates suggestion status.
 */
class ApplySuggestionUseCase @Inject constructor(
    private val suggestionRepository: SuggestionRepository,
    private val learningRepository: LearningRepository
) {
    suspend operator fun invoke(suggestion: Suggestion) {
        // Update suggestion status to APPLIED
        val updatedSuggestion = suggestion.copy(status = SuggestionStatus.APPLIED)
        suggestionRepository.updateSuggestion(updatedSuggestion)
        
        // Record acceptance feedback for learning
        val feedback = SuggestionFeedback(
            suggestionId = suggestion.id,
            suggestionType = suggestion.type,
            wasAccepted = true,
            reminderId = suggestion.reminderId,
            confidenceScore = suggestion.confidenceScore
        )
        learningRepository.recordFeedback(feedback)
    }
}
