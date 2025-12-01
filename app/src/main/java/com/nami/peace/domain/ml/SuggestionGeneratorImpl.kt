package com.nami.peace.domain.ml

import com.nami.peace.data.local.SuggestionType
import com.nami.peace.data.repository.CompletionEventRepository
import com.nami.peace.domain.model.Suggestion
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

/**
 * Implementation of SuggestionGenerator that creates actionable suggestions
 * from user behavior patterns. Uses PatternAnalyzer for data analysis and
 * generates suggestions with confidence scores.
 */
@Singleton
class SuggestionGeneratorImpl @Inject constructor(
    private val patternAnalyzer: PatternAnalyzer,
    private val completionEventRepository: CompletionEventRepository,
    private val learningRepository: com.nami.peace.domain.repository.LearningRepository
) : SuggestionGenerator {
    
    companion object {
        private const val MIN_EVENTS_FOR_SUGGESTIONS = 7
        private const val MIN_CONFIDENCE_SCORE = 50
        private const val HABIT_FORMATION_MIN_STREAK = 7 // 7 consecutive days
        private const val HABIT_FORMATION_CONSISTENCY_THRESHOLD = 0.8 // 80% completion rate
        private const val TEMPLATE_MIN_SIMILAR_TASKS = 5 // Minimum similar tasks for template suggestion
        private const val TEMPLATE_SIMILARITY_THRESHOLD = 0.7 // 70% similarity in title
    }
    
    @Serializable
    data class HabitFormationSuggestion(
        val taskTitle: String,
        val consecutiveDays: Int,
        val completionRate: Double,
        val suggestedHabitType: String
    )
    
    @Serializable
    data class TemplateCreationSuggestion(
        val baseTitle: String,
        val similarTaskCount: Int,
        val commonPatterns: List<String>
    )
    
    override suspend fun generateAllSuggestions(): List<Suggestion> {
        val events = completionEventRepository.getRecentEvents().first()
        
        if (events.size < MIN_EVENTS_FOR_SUGGESTIONS) {
            return emptyList()
        }
        
        val allSuggestions = mutableListOf<Suggestion>()
        
        // Generate all types of suggestions with learning-based filtering
        allSuggestions.addAll(filterByLearning(SuggestionType.OPTIMAL_TIME, generateOptimalTimeSuggestions()))
        allSuggestions.addAll(filterByLearning(SuggestionType.PRIORITY_ADJUSTMENT, generatePriorityAdjustmentSuggestions()))
        allSuggestions.addAll(filterByLearning(SuggestionType.RECURRING_PATTERN, generateRecurringPatternSuggestions()))
        allSuggestions.addAll(filterByLearning(SuggestionType.BREAK_REMINDER, generateBreakReminderSuggestions()))
        allSuggestions.addAll(filterByLearning(SuggestionType.HABIT_FORMATION, generateHabitFormationSuggestions()))
        allSuggestions.addAll(filterByLearning(SuggestionType.TEMPLATE_CREATION, generateTemplateCreationSuggestions()))
        allSuggestions.addAll(filterByLearning(SuggestionType.FOCUS_SESSION, generateFocusSessionSuggestions()))
        
        // Sort by confidence score (highest first) and return
        return allSuggestions.sortedByDescending { it.confidenceScore }
    }
    
    /**
     * Filter suggestions based on learning feedback.
     * Applies throttling and confidence threshold adjustments.
     */
    private suspend fun filterByLearning(type: SuggestionType, suggestions: List<Suggestion>): List<Suggestion> {
        // Check if this suggestion type should be throttled
        if (learningRepository.shouldThrottleSuggestionType(type)) {
            return emptyList() // Throttle this type due to high dismissal rate
        }
        
        // Get recommended confidence threshold based on learning
        val minConfidence = learningRepository.getRecommendedConfidenceThreshold(type)
        
        // Filter suggestions by learned confidence threshold
        return suggestions.filter { it.confidenceScore >= minConfidence }
    }
    
    override suspend fun generateOptimalTimeSuggestions(): List<Suggestion> {
        return patternAnalyzer.analyzeCompletionPatterns()
    }
    
    override suspend fun generatePriorityAdjustmentSuggestions(): List<Suggestion> {
        return patternAnalyzer.analyzePriorityPatterns()
    }
    
    override suspend fun generateRecurringPatternSuggestions(): List<Suggestion> {
        return patternAnalyzer.analyzeRecurringPatterns()
    }
    
    override suspend fun generateBreakReminderSuggestions(): List<Suggestion> {
        // Break reminders are part of focus session analysis
        return patternAnalyzer.analyzeFocusSessions()
    }
    
    override suspend fun generateHabitFormationSuggestions(): List<Suggestion> {
        val events = completionEventRepository.getRecentEvents().first()
        
        if (events.size < MIN_EVENTS_FOR_SUGGESTIONS) {
            return emptyList()
        }
        
        val suggestions = mutableListOf<Suggestion>()
        
        // Group events by reminder ID
        val eventsByReminder = events.groupBy { it.reminderId }
        
        for ((reminderId, reminderEvents) in eventsByReminder) {
            if (reminderEvents.size < HABIT_FORMATION_MIN_STREAK) continue
            
            // Sort events by completion time
            val sortedEvents = reminderEvents.sortedBy { it.completedTimeInMillis }
            
            // Check for consecutive daily completions
            var consecutiveDays = 1
            var maxConsecutiveDays = 1
            
            for (i in 1 until sortedEvents.size) {
                val prevDay = TimeUnit.MILLISECONDS.toDays(sortedEvents[i - 1].completedTimeInMillis)
                val currentDay = TimeUnit.MILLISECONDS.toDays(sortedEvents[i].completedTimeInMillis)
                
                if (currentDay == prevDay + 1) {
                    consecutiveDays++
                    maxConsecutiveDays = maxOf(maxConsecutiveDays, consecutiveDays)
                } else if (currentDay != prevDay) {
                    consecutiveDays = 1
                }
            }
            
            // Calculate completion rate (tasks completed on time)
            val onTimeCount = reminderEvents.count { 
                it.completionDelayInMillis <= TimeUnit.MINUTES.toMillis(15) 
            }
            val completionRate = onTimeCount.toDouble() / reminderEvents.size
            
            // Generate suggestion if streak is strong and completion rate is high
            if (maxConsecutiveDays >= HABIT_FORMATION_MIN_STREAK && 
                completionRate >= HABIT_FORMATION_CONSISTENCY_THRESHOLD) {
                
                val taskTitle = reminderEvents.first().title
                
                // Determine habit type based on task characteristics
                val suggestedHabitType = when {
                    taskTitle.contains("exercise", ignoreCase = true) || 
                    taskTitle.contains("workout", ignoreCase = true) -> "HEALTH"
                    taskTitle.contains("read", ignoreCase = true) || 
                    taskTitle.contains("study", ignoreCase = true) -> "LEARNING"
                    taskTitle.contains("water", ignoreCase = true) || 
                    taskTitle.contains("meal", ignoreCase = true) -> "WELLNESS"
                    else -> "PRODUCTIVITY"
                }
                
                // Calculate confidence based on streak length and completion rate
                val streakFactor = min(1.0, maxConsecutiveDays.toDouble() / 30.0) // Max at 30 days
                val confidence = min(100, ((streakFactor * 0.5 + completionRate * 0.5) * 100).toInt())
                
                if (confidence >= MIN_CONFIDENCE_SCORE) {
                    val suggestionData = HabitFormationSuggestion(
                        taskTitle = taskTitle,
                        consecutiveDays = maxConsecutiveDays,
                        completionRate = completionRate,
                        suggestedHabitType = suggestedHabitType
                    )
                    
                    suggestions.add(
                        Suggestion(
                            type = SuggestionType.HABIT_FORMATION,
                            reminderId = reminderId,
                            title = "Habit Forming: \"$taskTitle\"",
                            description = "You've completed this task for $maxConsecutiveDays consecutive days " +
                                    "with ${(completionRate * 100).toInt()}% on-time completion. " +
                                    "This is becoming a strong $suggestedHabitType habit!",
                            confidenceScore = confidence,
                            suggestedValue = Json.encodeToString(suggestionData)
                        )
                    )
                }
            }
        }
        
        return suggestions
    }
    
    override suspend fun generateTemplateCreationSuggestions(): List<Suggestion> {
        val events = completionEventRepository.getRecentEvents().first()
        
        if (events.size < MIN_EVENTS_FOR_SUGGESTIONS) {
            return emptyList()
        }
        
        val suggestions = mutableListOf<Suggestion>()
        
        // Group events by similar titles using fuzzy matching
        val titleGroups = mutableMapOf<String, MutableList<String>>()
        val processedTitles = mutableSetOf<String>()
        
        for (event in events) {
            val title = event.title.trim()
            if (title in processedTitles) continue
            
            // Find or create a group for similar titles
            var foundGroup = false
            for ((baseTitle, group) in titleGroups) {
                if (calculateSimilarity(title, baseTitle) >= TEMPLATE_SIMILARITY_THRESHOLD) {
                    group.add(title)
                    foundGroup = true
                    break
                }
            }
            
            if (!foundGroup) {
                titleGroups[title] = mutableListOf(title)
            }
            
            processedTitles.add(title)
        }
        
        // Generate suggestions for groups with enough similar tasks
        for ((baseTitle, similarTitles) in titleGroups) {
            if (similarTitles.size < TEMPLATE_MIN_SIMILAR_TASKS) continue
            
            // Extract common patterns from similar titles
            val commonPatterns = extractCommonPatterns(similarTitles)
            
            // Calculate confidence based on group size and pattern consistency
            val groupSizeFactor = min(1.0, similarTitles.size.toDouble() / 20.0) // Max at 20 tasks
            val patternFactor = min(1.0, commonPatterns.size.toDouble() / 3.0) // Max at 3 patterns
            val confidence = min(100, ((groupSizeFactor * 0.6 + patternFactor * 0.4) * 100).toInt())
            
            if (confidence >= MIN_CONFIDENCE_SCORE) {
                val suggestionData = TemplateCreationSuggestion(
                    baseTitle = baseTitle,
                    similarTaskCount = similarTitles.size,
                    commonPatterns = commonPatterns
                )
                
                suggestions.add(
                    Suggestion(
                        type = SuggestionType.TEMPLATE_CREATION,
                        reminderId = null,
                        title = "Template Suggestion: \"$baseTitle\"",
                        description = "You've created ${similarTitles.size} similar tasks. " +
                                "Consider creating a template to save time. " +
                                "Common patterns: ${commonPatterns.joinToString(", ")}",
                        confidenceScore = confidence,
                        suggestedValue = Json.encodeToString(suggestionData)
                    )
                )
            }
        }
        
        return suggestions
    }
    
    override suspend fun generateFocusSessionSuggestions(): List<Suggestion> {
        return patternAnalyzer.analyzeFocusSessions()
    }
    
    /**
     * Calculate similarity between two strings using Levenshtein distance.
     * Returns a value between 0.0 (completely different) and 1.0 (identical).
     */
    private fun calculateSimilarity(s1: String, s2: String): Double {
        val longer = if (s1.length > s2.length) s1 else s2
        val shorter = if (s1.length > s2.length) s2 else s1
        
        if (longer.isEmpty()) return 1.0
        
        val distance = levenshteinDistance(longer.lowercase(), shorter.lowercase())
        return (longer.length - distance).toDouble() / longer.length
    }
    
    /**
     * Calculate Levenshtein distance between two strings.
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val costs = IntArray(s2.length + 1) { it }
        
        for (i in 1..s1.length) {
            var lastValue = i
            for (j in 1..s2.length) {
                val newValue = if (s1[i - 1] == s2[j - 1]) {
                    costs[j - 1]
                } else {
                    1 + minOf(costs[j - 1], costs[j], lastValue)
                }
                costs[j - 1] = lastValue
                lastValue = newValue
            }
            costs[s2.length] = lastValue
        }
        
        return costs[s2.length]
    }
    
    /**
     * Extract common patterns from a list of similar titles.
     * Returns a list of common words or phrases.
     */
    private fun extractCommonPatterns(titles: List<String>): List<String> {
        if (titles.isEmpty()) return emptyList()
        
        // Split titles into words and count frequency
        val wordFrequency = mutableMapOf<String, Int>()
        
        for (title in titles) {
            val words = title.lowercase()
                .split(Regex("\\s+"))
                .filter { it.length > 2 } // Ignore very short words
            
            for (word in words) {
                wordFrequency[word] = wordFrequency.getOrDefault(word, 0) + 1
            }
        }
        
        // Find words that appear in at least 50% of titles
        val threshold = titles.size / 2
        return wordFrequency
            .filter { it.value >= threshold }
            .map { it.key }
            .take(5) // Return top 5 patterns
    }
}
