package com.nami.peace.domain.ml

import com.nami.peace.data.local.SuggestionType
import com.nami.peace.data.repository.CompletionEventRepository
import com.nami.peace.domain.model.CompletionEvent
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.Suggestion
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.min

/**
 * Implementation of PatternAnalyzer that analyzes completion events to generate suggestions.
 * Uses statistical analysis of the last 90 days of completion data.
 */
@Singleton
class PatternAnalyzerImpl @Inject constructor(
    private val completionEventRepository: CompletionEventRepository
) : PatternAnalyzer {
    
    companion object {
        private const val MIN_EVENTS_FOR_ANALYSIS = 7 // Minimum events needed for pattern detection
        private const val MIN_CONFIDENCE_SCORE = 50 // Minimum confidence to generate suggestion
        private const val OPTIMAL_TIME_THRESHOLD = 0.7 // 70% completion rate for optimal time
        private const val PRIORITY_MISMATCH_THRESHOLD = 0.6 // 60% completion delay for priority mismatch
        private const val RECURRING_PATTERN_MIN_OCCURRENCES = 3 // Minimum occurrences to suggest recurring
        private const val RECURRING_PATTERN_DAYS = 7 // Days to look for recurring patterns
        private const val FOCUS_SESSION_MIN_DURATION_HOURS = 2 // Minimum hours for focus session
        private const val FOCUS_SESSION_MAX_GAP_MINUTES = 30 // Max gap between tasks in focus session
    }
    
    @Serializable
    data class OptimalTimeSuggestion(
        val currentHour: Int,
        val suggestedHour: Int,
        val completionRate: Double
    )
    
    @Serializable
    data class PriorityAdjustmentSuggestion(
        val currentPriority: String,
        val suggestedPriority: String,
        val averageDelayHours: Double
    )
    
    @Serializable
    data class RecurringPatternSuggestion(
        val taskTitle: String,
        val occurrences: Int,
        val averageIntervalHours: Int,
        val suggestedRecurrenceType: String
    )
    
    @Serializable
    data class FocusSessionSuggestion(
        val averageSessionDurationHours: Double,
        val suggestedBreakIntervalMinutes: Int
    )
    
    override suspend fun analyzeCompletionPatterns(): List<Suggestion> {
        val events = completionEventRepository.getRecentEvents().first()
        
        if (events.size < MIN_EVENTS_FOR_ANALYSIS) {
            return emptyList()
        }
        
        val suggestions = mutableListOf<Suggestion>()
        
        // Group events by reminder ID to analyze individual task patterns
        val eventsByReminder = events.groupBy { it.reminderId }
        
        for ((reminderId, reminderEvents) in eventsByReminder) {
            if (reminderEvents.size < 3) continue // Need at least 3 completions for pattern
            
            // Analyze completion times by hour
            val completionsByHour = reminderEvents.groupBy { it.hourOfDay }
            val scheduledHour = reminderEvents.first().let { event ->
                Calendar.getInstance().apply {
                    timeInMillis = event.scheduledTimeInMillis
                }.get(Calendar.HOUR_OF_DAY)
            }
            
            // Find the hour with best completion rate (completed on time)
            val hourStats = completionsByHour.mapValues { (_, events) ->
                val onTimeCount = events.count { it.completionDelayInMillis <= TimeUnit.MINUTES.toMillis(15) }
                onTimeCount.toDouble() / events.size
            }
            
            val bestHour = hourStats.maxByOrNull { it.value }
            
            if (bestHour != null && bestHour.value >= OPTIMAL_TIME_THRESHOLD && bestHour.key != scheduledHour) {
                val confidence = min(100, (bestHour.value * 100).toInt())
                
                if (confidence >= MIN_CONFIDENCE_SCORE) {
                    val taskTitle = reminderEvents.first().title
                    val suggestionData = OptimalTimeSuggestion(
                        currentHour = scheduledHour,
                        suggestedHour = bestHour.key,
                        completionRate = bestHour.value
                    )
                    
                    suggestions.add(
                        Suggestion(
                            type = SuggestionType.OPTIMAL_TIME,
                            reminderId = reminderId,
                            title = "Optimal Time for \"$taskTitle\"",
                            description = "You complete this task ${(bestHour.value * 100).toInt()}% of the time at ${formatHour(bestHour.key)}, " +
                                    "but it's scheduled for ${formatHour(scheduledHour)}.",
                            confidenceScore = confidence,
                            suggestedValue = Json.encodeToString(suggestionData)
                        )
                    )
                }
            }
        }
        
        return suggestions
    }
    
    override suspend fun analyzePriorityPatterns(): List<Suggestion> {
        val events = completionEventRepository.getRecentEvents().first()
        
        if (events.size < MIN_EVENTS_FOR_ANALYSIS) {
            return emptyList()
        }
        
        val suggestions = mutableListOf<Suggestion>()
        
        // Group events by reminder ID
        val eventsByReminder = events.groupBy { it.reminderId }
        
        for ((reminderId, reminderEvents) in eventsByReminder) {
            if (reminderEvents.size < 3) continue
            
            val currentPriority = reminderEvents.first().priority
            val taskTitle = reminderEvents.first().title
            
            // Calculate average completion delay
            val averageDelayHours = reminderEvents
                .map { it.completionDelayInMillis.toDouble() / TimeUnit.HOURS.toMillis(1) }
                .average()
            
            // Determine if priority should be adjusted based on completion behavior
            val suggestedPriority = when {
                // High priority but consistently completed late -> should be lower priority
                currentPriority == PriorityLevel.HIGH && averageDelayHours > 2 -> PriorityLevel.MEDIUM
                
                // Medium priority but consistently completed very late -> should be low priority
                currentPriority == PriorityLevel.MEDIUM && averageDelayHours > 4 -> PriorityLevel.LOW
                
                // Low priority but consistently completed on time -> should be higher priority
                currentPriority == PriorityLevel.LOW && averageDelayHours < 0.5 -> PriorityLevel.MEDIUM
                
                // Medium priority but consistently completed early -> should be high priority
                currentPriority == PriorityLevel.MEDIUM && averageDelayHours < -1 -> PriorityLevel.HIGH
                
                else -> null
            }
            
            if (suggestedPriority != null && suggestedPriority != currentPriority) {
                // Calculate confidence based on consistency of delay pattern
                val delayVariance = reminderEvents
                    .map { it.completionDelayInMillis.toDouble() / TimeUnit.HOURS.toMillis(1) }
                    .map { (it - averageDelayHours) * (it - averageDelayHours) }
                    .average()
                
                val consistency = 1.0 / (1.0 + delayVariance) // Lower variance = higher consistency
                val confidence = min(100, (consistency * 100).toInt())
                
                if (confidence >= MIN_CONFIDENCE_SCORE) {
                    val suggestionData = PriorityAdjustmentSuggestion(
                        currentPriority = currentPriority.name,
                        suggestedPriority = suggestedPriority.name,
                        averageDelayHours = averageDelayHours
                    )
                    
                    val delayDescription = when {
                        averageDelayHours > 0 -> "completed ${averageDelayHours.toInt()} hours late on average"
                        averageDelayHours < 0 -> "completed ${abs(averageDelayHours).toInt()} hours early on average"
                        else -> "completed on time"
                    }
                    
                    suggestions.add(
                        Suggestion(
                            type = SuggestionType.PRIORITY_ADJUSTMENT,
                            reminderId = reminderId,
                            title = "Priority Adjustment for \"$taskTitle\"",
                            description = "This task is marked ${currentPriority.name} but is $delayDescription. " +
                                    "Consider changing to ${suggestedPriority.name} priority.",
                            confidenceScore = confidence,
                            suggestedValue = Json.encodeToString(suggestionData)
                        )
                    )
                }
            }
        }
        
        return suggestions
    }
    
    override suspend fun analyzeRecurringPatterns(): List<Suggestion> {
        val events = completionEventRepository.getRecentEvents().first()
        
        if (events.size < MIN_EVENTS_FOR_ANALYSIS) {
            return emptyList()
        }
        
        val suggestions = mutableListOf<Suggestion>()
        
        // Group events by similar titles (case-insensitive, trimmed)
        val eventsByTitle = events
            .filter { !it.wasRecurring } // Only look at non-recurring tasks
            .groupBy { it.title.trim().lowercase() }
        
        for ((titleKey, titleEvents) in eventsByTitle) {
            if (titleEvents.size < RECURRING_PATTERN_MIN_OCCURRENCES) continue
            
            // Check if events occurred within the last week
            val recentEvents = titleEvents
                .filter { it.completedTimeInMillis >= System.currentTimeMillis() - TimeUnit.DAYS.toMillis(RECURRING_PATTERN_DAYS.toLong()) }
            
            if (recentEvents.size < RECURRING_PATTERN_MIN_OCCURRENCES) continue
            
            // Calculate average interval between completions
            val sortedEvents = recentEvents.sortedBy { it.completedTimeInMillis }
            val intervals = sortedEvents.zipWithNext { a, b ->
                b.completedTimeInMillis - a.completedTimeInMillis
            }
            
            if (intervals.isEmpty()) continue
            
            val averageIntervalMillis = intervals.average()
            val averageIntervalHours = (averageIntervalMillis / TimeUnit.HOURS.toMillis(1)).toInt()
            
            // Determine suggested recurrence type based on interval
            val suggestedRecurrence = when {
                averageIntervalHours < 6 -> "HOURLY"
                averageIntervalHours < 30 -> "DAILY"
                averageIntervalHours < 200 -> "WEEKLY"
                else -> "MONTHLY"
            }
            
            // Calculate confidence based on interval consistency
            val intervalVariance = intervals
                .map { (it - averageIntervalMillis) * (it - averageIntervalMillis) }
                .average()
            
            val consistency = 1.0 / (1.0 + (intervalVariance / (averageIntervalMillis * averageIntervalMillis)))
            val confidence = min(100, (consistency * 80 + 20).toInt()) // Base confidence of 20
            
            if (confidence >= MIN_CONFIDENCE_SCORE) {
                val taskTitle = titleEvents.first().title
                val suggestionData = RecurringPatternSuggestion(
                    taskTitle = taskTitle,
                    occurrences = recentEvents.size,
                    averageIntervalHours = averageIntervalHours,
                    suggestedRecurrenceType = suggestedRecurrence
                )
                
                suggestions.add(
                    Suggestion(
                        type = SuggestionType.RECURRING_PATTERN,
                        reminderId = null, // No specific reminder, this is a pattern across multiple
                        title = "Recurring Pattern Detected: \"$taskTitle\"",
                        description = "You've created this task ${recentEvents.size} times in the last $RECURRING_PATTERN_DAYS days. " +
                                "Consider making it a $suggestedRecurrence recurring reminder.",
                        confidenceScore = confidence,
                        suggestedValue = Json.encodeToString(suggestionData)
                    )
                )
            }
        }
        
        return suggestions
    }
    
    override suspend fun analyzeFocusSessions(): List<Suggestion> {
        val events = completionEventRepository.getRecentEvents().first()
        
        if (events.size < MIN_EVENTS_FOR_ANALYSIS) {
            return emptyList()
        }
        
        val suggestions = mutableListOf<Suggestion>()
        
        // Sort events by completion time
        val sortedEvents = events.sortedBy { it.completedTimeInMillis }
        
        // Identify focus sessions (clusters of task completions)
        val focusSessions = mutableListOf<List<CompletionEvent>>()
        var currentSession = mutableListOf<CompletionEvent>()
        
        for (event in sortedEvents) {
            if (currentSession.isEmpty()) {
                currentSession.add(event)
            } else {
                val lastEvent = currentSession.last()
                val gapMinutes = (event.completedTimeInMillis - lastEvent.completedTimeInMillis) / TimeUnit.MINUTES.toMillis(1)
                
                if (gapMinutes <= FOCUS_SESSION_MAX_GAP_MINUTES) {
                    currentSession.add(event)
                } else {
                    if (currentSession.size >= 2) { // At least 2 tasks for a session
                        focusSessions.add(currentSession.toList())
                    }
                    currentSession = mutableListOf(event)
                }
            }
        }
        
        // Add last session if valid
        if (currentSession.size >= 2) {
            focusSessions.add(currentSession)
        }
        
        if (focusSessions.isEmpty()) {
            return suggestions
        }
        
        // Calculate average focus session duration
        val sessionDurations = focusSessions.map { session ->
            val duration = session.last().completedTimeInMillis - session.first().completedTimeInMillis
            duration.toDouble() / TimeUnit.HOURS.toMillis(1)
        }
        
        val averageSessionHours = sessionDurations.average()
        
        // Only suggest breaks for sessions longer than minimum duration
        if (averageSessionHours >= FOCUS_SESSION_MIN_DURATION_HOURS) {
            // Suggest break intervals based on session length
            val suggestedBreakInterval = when {
                averageSessionHours >= 4 -> 60 // Every hour for long sessions
                averageSessionHours >= 3 -> 90 // Every 1.5 hours for medium sessions
                else -> 120 // Every 2 hours for shorter sessions
            }
            
            val confidence = min(100, (focusSessions.size * 10 + 50).coerceAtMost(100))
            
            if (confidence >= MIN_CONFIDENCE_SCORE) {
                val suggestionData = FocusSessionSuggestion(
                    averageSessionDurationHours = averageSessionHours,
                    suggestedBreakIntervalMinutes = suggestedBreakInterval
                )
                
                suggestions.add(
                    Suggestion(
                        type = SuggestionType.FOCUS_SESSION,
                        reminderId = null,
                        title = "Focus Session Break Reminder",
                        description = "You typically work for ${averageSessionHours.toInt()} hours straight. " +
                                "Consider adding break reminders every $suggestedBreakInterval minutes to maintain productivity.",
                        confidenceScore = confidence,
                        suggestedValue = Json.encodeToString(suggestionData)
                    )
                )
            }
        }
        
        return suggestions
    }
    
    /**
     * Format hour (0-23) to human-readable time
     */
    private fun formatHour(hour: Int): String {
        return when {
            hour == 0 -> "12:00 AM"
            hour < 12 -> "$hour:00 AM"
            hour == 12 -> "12:00 PM"
            else -> "${hour - 12}:00 PM"
        }
    }
}
