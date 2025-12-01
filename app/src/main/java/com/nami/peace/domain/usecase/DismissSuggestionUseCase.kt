package com.nami.peace.domain.usecase

import com.nami.peace.data.local.SuggestionStatus
import com.nami.peace.domain.model.Suggestion
import com.nami.peace.domain.model.SuggestionFeedback
import com.nami.peace.domain.repository.LearningRepository
import com.nami.peace.domain.repository.SuggestionRepository
import javax.inject.Inject

/**
 * Use case for dismissing a suggestion.
 * Records dismissal feedback for learning and updates suggestion status.
 */
class DismissSuggestionUseCase @Inject constructor(
    private val suggestionRepository: SuggestionRepository,
    private val learningRepository: LearningRepository
) {
    suspend operator fun invoke(suggestion: Suggestion) {
        // Update suggestion status to DISMISSED
        val updatedSuggestion = suggestion.copy(status = SuggestionStatus.DISMISSED)
        suggestionRepository.updateSuggestion(updatedSuggestion)
        
        // Record dismissal feedback for learning
        val feedback = SuggestionFeedback(
            suggestionId = suggestion.id,
            suggestionType = suggestion.type,
            wasAccepted = false,
            reminderId = suggestion.reminderId,
            confidenceScore = suggestion.confidenceScore
        )
        learningRepository.recordFeedback(feedback)
    }
}
