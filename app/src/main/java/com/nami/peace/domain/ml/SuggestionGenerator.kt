package com.nami.peace.domain.ml

import com.nami.peace.domain.model.Suggestion

/**
 * Interface for generating ML-based suggestions from analyzed patterns.
 * Combines pattern analysis results to create actionable suggestions
 * with confidence scores for improving user productivity.
 */
interface SuggestionGenerator {
    /**
     * Generates all types of suggestions by analyzing user behavior patterns.
     * This is the main entry point that combines all analysis methods.
     * 
     * @return List of all generated suggestions sorted by confidence score (highest first)
     */
    suspend fun generateAllSuggestions(): List<Suggestion>
    
    /**
     * Generates optimal time suggestions for task scheduling.
     * Analyzes when tasks are most likely to be completed on time.
     * 
     * @return List of optimal time suggestions
     */
    suspend fun generateOptimalTimeSuggestions(): List<Suggestion>
    
    /**
     * Generates priority adjustment suggestions.
     * Identifies tasks with incorrect priority levels based on completion behavior.
     * 
     * @return List of priority adjustment suggestions
     */
    suspend fun generatePriorityAdjustmentSuggestions(): List<Suggestion>
    
    /**
     * Generates recurring pattern suggestions.
     * Identifies manually created tasks that should be converted to recurring reminders.
     * 
     * @return List of recurring pattern suggestions
     */
    suspend fun generateRecurringPatternSuggestions(): List<Suggestion>
    
    /**
     * Generates break reminder suggestions.
     * Identifies when users work for extended periods and suggests breaks.
     * 
     * @return List of break reminder suggestions
     */
    suspend fun generateBreakReminderSuggestions(): List<Suggestion>
    
    /**
     * Generates habit formation suggestions.
     * Identifies consistent task completion patterns that could become habits.
     * 
     * @return List of habit formation suggestions
     */
    suspend fun generateHabitFormationSuggestions(): List<Suggestion>
    
    /**
     * Generates template creation suggestions.
     * Identifies frequently created similar tasks that could benefit from templates.
     * 
     * @return List of template creation suggestions
     */
    suspend fun generateTemplateCreationSuggestions(): List<Suggestion>
    
    /**
     * Generates focus session suggestions.
     * Analyzes work patterns to suggest optimal focus session durations.
     * 
     * @return List of focus session suggestions
     */
    suspend fun generateFocusSessionSuggestions(): List<Suggestion>
}
