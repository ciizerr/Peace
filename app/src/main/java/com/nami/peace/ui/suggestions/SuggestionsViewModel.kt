package com.nami.peace.ui.suggestions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nami.peace.domain.model.Suggestion
import com.nami.peace.domain.repository.SuggestionRepository
import com.nami.peace.domain.usecase.ApplySuggestionUseCase
import com.nami.peace.domain.usecase.DismissSuggestionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the ML Suggestions screen.
 * 
 * Manages:
 * - Loading pending suggestions
 * - Applying suggestions
 * - Dismissing suggestions
 * - Handling empty states
 * 
 * Requirements: 12.2, 12.9, 12.10, 12.11
 */
@HiltViewModel
class SuggestionsViewModel @Inject constructor(
    private val suggestionRepository: SuggestionRepository,
    private val applySuggestionUseCase: ApplySuggestionUseCase,
    private val dismissSuggestionUseCase: DismissSuggestionUseCase
) : ViewModel() {

    // Pending suggestions from repository
    val suggestions: StateFlow<List<Suggestion>> = suggestionRepository.getPendingSuggestions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Action in progress (to disable buttons during operations)
    private val _actionInProgress = MutableStateFlow<Int?>(null)
    val actionInProgress: StateFlow<Int?> = _actionInProgress.asStateFlow()

    /**
     * Apply a suggestion.
     * Updates the suggestion status and records acceptance feedback.
     * 
     * @param suggestion The suggestion to apply
     */
    fun applySuggestion(suggestion: Suggestion) {
        viewModelScope.launch {
            try {
                _actionInProgress.value = suggestion.id
                _error.value = null
                
                applySuggestionUseCase(suggestion)
                
                _actionInProgress.value = null
            } catch (e: Exception) {
                _error.value = "Failed to apply suggestion: ${e.message}"
                _actionInProgress.value = null
            }
        }
    }

    /**
     * Dismiss a suggestion.
     * Updates the suggestion status and records dismissal feedback.
     * 
     * @param suggestion The suggestion to dismiss
     */
    fun dismissSuggestion(suggestion: Suggestion) {
        viewModelScope.launch {
            try {
                _actionInProgress.value = suggestion.id
                _error.value = null
                
                dismissSuggestionUseCase(suggestion)
                
                _actionInProgress.value = null
            } catch (e: Exception) {
                _error.value = "Failed to dismiss suggestion: ${e.message}"
                _actionInProgress.value = null
            }
        }
    }

    /**
     * Clear the error message.
     */
    fun clearError() {
        _error.value = null
    }
}
