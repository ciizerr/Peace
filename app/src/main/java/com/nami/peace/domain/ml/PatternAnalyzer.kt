package com.nami.peace.domain.ml

import com.nami.peace.domain.model.Suggestion

/**
 * Interface for analyzing user behavior patterns to generate ML suggestions.
 * Analyzes completion events from the last 90 days to identify patterns and
 * generate actionable suggestions for improving productivity.
 */
interface PatternAnalyzer {
    /**
     * Analyzes completion time patterns to suggest optimal scheduling times.
     * Identifies when tasks are most likely to be completed on time.
     * 
     * @return List of suggestions for optimal task scheduling times
     */
    suspend fun analyzeCompletionPatterns(): List<Suggestion>
    
    /**
     * Analyzes priority patterns to suggest priority adjustments.
     * Identifies tasks that are marked with incorrect priority levels
     * based on actual completion behavior.
     * 
     * @return List of suggestions for priority adjustments
     */
    suspend fun analyzePriorityPatterns(): List<Suggestion>
    
    /**
     * Analyzes recurring patterns to suggest converting manual tasks to recurring.
     * Identifies tasks that are created repeatedly and could benefit from
     * automatic recurrence.
     * 
     * @return List of suggestions for creating recurring reminders
     */
    suspend fun analyzeRecurringPatterns(): List<Suggestion>
    
    /**
     * Analyzes focus session patterns to suggest optimal work/break intervals.
     * Identifies when users work for extended periods and suggests break reminders.
     * 
     * @return List of suggestions for break reminders and focus sessions
     */
    suspend fun analyzeFocusSessions(): List<Suggestion>
}
