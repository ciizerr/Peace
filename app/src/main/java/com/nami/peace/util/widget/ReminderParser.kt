package com.nami.peace.util.widget

import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.Reminder
import com.nami.peace.domain.model.ReminderCategory
import com.nami.peace.domain.model.RecurrenceType
import java.util.Calendar
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parses natural language text into Reminder objects.
 * Uses pattern matching and NLP techniques to extract reminder details.
 * 
 * In a production environment, this would integrate with Gemini AI API
 * for more sophisticated natural language understanding.
 * 
 * Implements Requirements 17.7, 17.8
 */
@Singleton
class ReminderParser @Inject constructor() {
    
    private val timePatterns = listOf(
        // "at 5pm", "at 17:00", "at 5:30pm"
        Pattern.compile("at\\s+(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?", Pattern.CASE_INSENSITIVE),
        // "5pm", "17:00", "5:30pm"
        Pattern.compile("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)", Pattern.CASE_INSENSITIVE)
    )
    
    private val datePatterns = listOf(
        // "tomorrow", "today"
        Pattern.compile("\\b(today|tomorrow)\\b", Pattern.CASE_INSENSITIVE),
        // "Monday", "Tuesday", etc.
        Pattern.compile("\\b(monday|tuesday|wednesday|thursday|friday|saturday|sunday)\\b", Pattern.CASE_INSENSITIVE),
        // "next Monday", "this Friday"
        Pattern.compile("\\b(next|this)\\s+(monday|tuesday|wednesday|thursday|friday|saturday|sunday)\\b", Pattern.CASE_INSENSITIVE)
    )
    
    private val recurrencePatterns = listOf(
        // "every day", "every week", "daily", "weekly"
        Pattern.compile("\\b(every\\s+day|daily|everyday)\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\b(every\\s+week|weekly)\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\b(every\\s+month|monthly)\\b", Pattern.CASE_INSENSITIVE)
    )
    
    private val priorityKeywords = mapOf(
        "urgent" to PriorityLevel.HIGH,
        "important" to PriorityLevel.HIGH,
        "asap" to PriorityLevel.HIGH,
        "high priority" to PriorityLevel.HIGH,
        "low priority" to PriorityLevel.LOW
    )
    
    private val categoryKeywords = mapOf(
        "work" to ReminderCategory.WORK,
        "meeting" to ReminderCategory.WORK,
        "office" to ReminderCategory.WORK,
        "study" to ReminderCategory.STUDY,
        "homework" to ReminderCategory.STUDY,
        "exam" to ReminderCategory.STUDY,
        "health" to ReminderCategory.HEALTH,
        "doctor" to ReminderCategory.HEALTH,
        "exercise" to ReminderCategory.HEALTH,
        "workout" to ReminderCategory.HEALTH,
        "gym" to ReminderCategory.HEALTH,
        "home" to ReminderCategory.HOME,
        "house" to ReminderCategory.HOME,
        "clean" to ReminderCategory.HOME
    )
    
    /**
     * Parses natural language text into a Reminder object.
     * 
     * Examples:
     * - "Buy milk at 5pm" -> Reminder with title "Buy milk" at 5pm today
     * - "Meeting tomorrow at 2pm" -> Reminder with title "Meeting" at 2pm tomorrow
     * - "Call mom every day at 6pm" -> Recurring daily reminder at 6pm
     */
    fun parse(text: String): Reminder {
        val cleanText = text.trim()
        
        // Extract time
        val (timeInMillis, timeText) = extractTime(cleanText)
        
        // Extract date
        val dateAdjustedTime = extractDate(cleanText, timeInMillis)
        
        // Extract recurrence
        val (isRecurring, recurrenceInterval) = extractRecurrence(cleanText)
        
        // Extract priority
        val priority = extractPriority(cleanText)
        
        // Extract category
        val category = extractCategory(cleanText)
        
        // Extract title (remove time, date, and keyword patterns)
        val title = extractTitle(cleanText, timeText)
        
        return Reminder(
            id = 0,
            title = title.ifBlank { "New Reminder" },
            startTimeInMillis = dateAdjustedTime,
            isEnabled = true,
            isCompleted = false,
            priority = priority,
            category = category,
            recurrenceType = if (isRecurring) RecurrenceType.DAILY else RecurrenceType.ONE_TIME,
            isNagModeEnabled = false,
            nagIntervalInMillis = null,
            nagTotalRepetitions = 1,
            currentRepetitionIndex = 0,
            isInNestedSnoozeLoop = false,
            nestedSnoozeStartTime = null,
            isStrictSchedulingEnabled = false,
            dateInMillis = null,
            daysOfWeek = emptyList(),
            originalStartTimeInMillis = dateAdjustedTime,
            customAlarmSoundUri = null,
            customAlarmSoundName = null
        )
    }
    
    private fun extractTime(text: String): Pair<Long, String> {
        for (pattern in timePatterns) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val hour = matcher.group(1)?.toIntOrNull() ?: 12
                val minute = matcher.group(2)?.toIntOrNull() ?: 0
                val ampm = matcher.group(3)?.lowercase()
                
                var adjustedHour = hour
                if (ampm == "pm" && hour < 12) {
                    adjustedHour += 12
                } else if (ampm == "am" && hour == 12) {
                    adjustedHour = 0
                }
                
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, adjustedHour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    
                    // If time is in the past today, schedule for tomorrow
                    if (timeInMillis < System.currentTimeMillis()) {
                        add(Calendar.DAY_OF_MONTH, 1)
                    }
                }
                
                return Pair(calendar.timeInMillis, matcher.group(0) ?: "")
            }
        }
        
        // Default to 1 hour from now
        val calendar = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 1)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return Pair(calendar.timeInMillis, "")
    }
    
    private fun extractDate(text: String, baseTime: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = baseTime
        }
        
        for (pattern in datePatterns) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val dateText = matcher.group(0)?.lowercase() ?: continue
                
                when {
                    dateText.contains("tomorrow") -> {
                        calendar.add(Calendar.DAY_OF_MONTH, 1)
                        return calendar.timeInMillis
                    }
                    dateText.contains("today") -> {
                        // Already set to today
                        return calendar.timeInMillis
                    }
                    else -> {
                        // Handle day of week
                        val dayOfWeek = getDayOfWeek(dateText)
                        if (dayOfWeek != -1) {
                            val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
                            var daysToAdd = dayOfWeek - currentDay
                            
                            if (daysToAdd <= 0) {
                                daysToAdd += 7 // Next week
                            }
                            
                            if (dateText.contains("next")) {
                                daysToAdd += 7 // Force next week
                            }
                            
                            calendar.add(Calendar.DAY_OF_MONTH, daysToAdd)
                            return calendar.timeInMillis
                        }
                    }
                }
            }
        }
        
        return baseTime
    }
    
    private fun extractRecurrence(text: String): Pair<Boolean, Long> {
        val lowerText = text.lowercase()
        
        for (pattern in recurrencePatterns) {
            val matcher = pattern.matcher(lowerText)
            if (matcher.find()) {
                val recurrenceText = matcher.group(0) ?: ""
                
                return when {
                    recurrenceText.contains("day") -> {
                        Pair(true, 24 * 60 * 60 * 1000L) // Daily
                    }
                    recurrenceText.contains("week") -> {
                        Pair(true, 7 * 24 * 60 * 60 * 1000L) // Weekly
                    }
                    recurrenceText.contains("month") -> {
                        Pair(true, 30 * 24 * 60 * 60 * 1000L) // Monthly (approximate)
                    }
                    else -> Pair(false, 0L)
                }
            }
        }
        
        return Pair(false, 0L)
    }
    
    private fun extractPriority(text: String): PriorityLevel {
        val lowerText = text.lowercase()
        
        for ((keyword, priority) in priorityKeywords) {
            if (lowerText.contains(keyword)) {
                return priority
            }
        }
        
        return PriorityLevel.MEDIUM
    }
    
    private fun extractCategory(text: String): ReminderCategory {
        val lowerText = text.lowercase()
        
        for ((keyword, category) in categoryKeywords) {
            if (lowerText.contains(keyword)) {
                return category
            }
        }
        
        return ReminderCategory.GENERAL
    }
    
    private fun extractTitle(text: String, timeText: String): String {
        var title = text
        
        // Remove time patterns
        if (timeText.isNotEmpty()) {
            title = title.replace(timeText, "")
        }
        
        // Remove date patterns
        for (pattern in datePatterns) {
            title = pattern.matcher(title).replaceAll("")
        }
        
        // Remove recurrence patterns
        for (pattern in recurrencePatterns) {
            title = pattern.matcher(title).replaceAll("")
        }
        
        // Remove priority keywords
        for (keyword in priorityKeywords.keys) {
            title = title.replace(keyword, "", ignoreCase = true)
        }
        
        // Clean up extra whitespace
        title = title.replace("\\s+".toRegex(), " ").trim()
        
        return title
    }
    
    private fun getDayOfWeek(text: String): Int {
        return when {
            text.contains("sunday") -> Calendar.SUNDAY
            text.contains("monday") -> Calendar.MONDAY
            text.contains("tuesday") -> Calendar.TUESDAY
            text.contains("wednesday") -> Calendar.WEDNESDAY
            text.contains("thursday") -> Calendar.THURSDAY
            text.contains("friday") -> Calendar.FRIDAY
            text.contains("saturday") -> Calendar.SATURDAY
            else -> -1
        }
    }
}
