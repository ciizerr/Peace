package com.nami.peace.property

import android.net.Uri
import com.nami.peace.data.local.GardenTheme
import com.nami.peace.data.local.SuggestionStatus
import com.nami.peace.data.local.SuggestionType
import com.nami.peace.domain.model.*
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*

/**
 * Custom generators for property-based testing.
 * These generators create random valid instances of domain models for testing.
 */
object CustomGenerators {
    
    /**
     * Generates random PriorityLevel values
     */
    fun priorityLevel(): Arb<PriorityLevel> = Arb.enum<PriorityLevel>()
    
    /**
     * Generates random ReminderCategory values
     */
    fun reminderCategory(): Arb<ReminderCategory> = Arb.enum<ReminderCategory>()
    
    /**
     * Generates random RecurrenceType values
     */
    fun recurrenceType(): Arb<RecurrenceType> = Arb.enum<RecurrenceType>()
    
    /**
     * Generates random GardenTheme values
     */
    fun gardenTheme(): Arb<GardenTheme> = Arb.enum<GardenTheme>()
    
    /**
     * Generates random SuggestionType values
     */
    fun suggestionType(): Arb<SuggestionType> = Arb.enum<SuggestionType>()
    
    /**
     * Generates random SuggestionStatus values
     */
    fun suggestionStatus(): Arb<SuggestionStatus> = Arb.enum<SuggestionStatus>()
    
    /**
     * Generates random valid timestamps (within reasonable range)
     */
    fun timestamp(): Arb<Long> = Arb.long(
        min = System.currentTimeMillis() - (365L * 24 * 60 * 60 * 1000), // 1 year ago
        max = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)  // 1 year future
    )
    

    
    /**
     * Generates random valid confidence scores (0-100)
     */
    fun confidenceScore(): Arb<Int> = Arb.int(0..100)
    
    /**
     * Generates random blur intensity values (0-100)
     */
    fun blurIntensity(): Arb<Int> = Arb.int(0..100)
    
    /**
     * Generates random font padding values (0-20)
     */
    fun fontPadding(): Arb<Int> = Arb.int(0..20)
    
    /**
     * Generates random growth stage values (0-9)
     */
    fun growthStage(): Arb<Int> = Arb.int(0..9)
    
    /**
     * Generates random streak values (0-1000)
     */
    fun streak(): Arb<Int> = Arb.int(0..1000)
    
    /**
     * Generates random valid Reminder instances
     */
    fun reminder(): Arb<Reminder> = arbitrary {
        val id = Arb.int(1..10000).bind()
        val now = timestamp().bind()
        val isNagMode = Arb.bool().bind()
        val hasCustomSound = Arb.bool().bind()
        
        Reminder(
            id = id,
            title = Arb.string(5..50).bind(),
            priority = priorityLevel().bind(),
            startTimeInMillis = now,
            recurrenceType = recurrenceType().bind(),
            isNagModeEnabled = isNagMode,
            nagIntervalInMillis = if (isNagMode) Arb.long(60000L..3600000L).bind() else null,
            nagTotalRepetitions = if (isNagMode) Arb.int(2..10).bind() else 1,
            currentRepetitionIndex = if (isNagMode) Arb.int(0..5).bind() else 0,
            isCompleted = Arb.bool().bind(),
            isEnabled = Arb.bool().bind(),
            category = reminderCategory().bind(),
            originalStartTimeInMillis = now,
            customAlarmSoundUri = if (hasCustomSound) Arb.string(10..50).bind() else null,
            customAlarmSoundName = if (hasCustomSound) Arb.string(5..30).bind() else null
        )
    }
    
    /**
     * Generates random valid Subtask instances
     */
    fun subtask(reminderId: Int? = null): Arb<Subtask> = arbitrary {
        Subtask(
            id = Arb.int(1..10000).bind(),
            reminderId = reminderId ?: Arb.int(1..10000).bind(),
            title = Arb.string(5..50).bind(),
            isCompleted = Arb.bool().bind(),
            order = Arb.int(0..100).bind(),
            createdAt = timestamp().bind()
        )
    }
    
    /**
     * Generates a reminder with a list of subtasks
     */
    fun reminderWithSubtasks(): Arb<Pair<Reminder, List<Subtask>>> = arbitrary {
        val reminder = reminder().bind()
        val subtaskCount = Arb.int(0..20).bind()
        val subtasks = List(subtaskCount) { index ->
            Subtask(
                id = index + 1,
                reminderId = reminder.id,
                title = Arb.string(5..50).bind(),
                isCompleted = Arb.bool().bind(),
                order = index,
                createdAt = timestamp().bind()
            )
        }
        reminder to subtasks
    }
    
    /**
     * Generates random valid Note instances
     */
    fun note(reminderId: Int? = null): Arb<Note> = arbitrary {
        Note(
            id = Arb.int(1..10000).bind(),
            reminderId = reminderId ?: Arb.int(1..10000).bind(),
            content = Arb.string(10..200).bind(),
            timestamp = timestamp().bind()
        )
    }
    
    /**
     * Generates random valid Attachment instances
     */
    fun attachment(reminderId: Int? = null): Arb<Attachment> = arbitrary {
        Attachment(
            id = Arb.int(1..10000).bind(),
            reminderId = reminderId ?: Arb.int(1..10000).bind(),
            filePath = "/storage/emulated/0/Peace/attachments/${Arb.string(10..20).bind()}.jpg",
            thumbnailPath = "/storage/emulated/0/Peace/thumbnails/${Arb.string(10..20).bind()}.jpg",
            timestamp = timestamp().bind(),
            mimeType = "image/jpeg"
        )
    }
    
    /**
     * Generates random valid GardenState instances
     */
    fun gardenState(): Arb<GardenState> = arbitrary {
        val currentStreak = Arb.int(0..1000).bind()
        val longestStreak = Arb.int(currentStreak..1000).bind()
        val hasLastCompletion = Arb.bool().bind()
        
        GardenState(
            theme = gardenTheme().bind(),
            growthStage = growthStage().bind(),
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            lastCompletionDate = if (hasLastCompletion) timestamp().bind() else null,
            totalTasksCompleted = Arb.int(0..10000).bind()
        )
    }
    
    /**
     * Generates random valid Suggestion instances
     */
    fun suggestion(): Arb<Suggestion> = arbitrary {
        val reminderIdOrNull = if (Arb.bool().bind()) Arb.int(1..10000).bind() else null
        Suggestion(
            id = Arb.int(1..10000).bind(),
            type = suggestionType().bind(),
            reminderId = reminderIdOrNull,
            title = Arb.string(10..50).bind(),
            description = Arb.string(20..200).bind(),
            confidenceScore = confidenceScore().bind(),
            suggestedValue = Arb.string(10..100).bind(),
            createdAt = timestamp().bind(),
            status = suggestionStatus().bind()
        )
    }
    
    /**
     * Generates random valid AlarmSound instances
     */
    fun alarmSound(): Arb<AlarmSound> = arbitrary {
        val mediaId = Arb.int(1..1000).bind()
        AlarmSound(
            id = Arb.string(5..20).bind(),
            name = Arb.string(5..30).bind(),
            uri = Uri.parse("content://media/internal/audio/media/$mediaId"),
            isSystem = Arb.bool().bind()
        )
    }
    
    /**
     * Generates random language codes
     */
    fun languageCode(): Arb<String> = Arb.of(
        "en", "es", "fr", "de", "hi", "ja", "pt", "zh"
    )
    
    /**
     * Generates random font names
     */
    fun fontName(): Arb<String> = Arb.of(
        "system", "roboto", "open_sans", "lato", "montserrat", "raleway"
    )
    
    /**
     * Generates random icon names
     */
    fun iconName(): Arb<String> = Arb.of(
        "home", "add", "settings", "alarm", "calendar", "check", "close",
        "edit", "delete", "share", "notifications", "search"
    )
    
    /**
     * Generates a list of reminders with varying properties
     */
    fun reminderList(size: Int = 10): Arb<List<Reminder>> = arbitrary {
        List(size) { reminder().bind() }
    }
    
    /**
     * Generates a nag mode reminder with specific repetition state
     */
    fun nagModeReminder(
        totalReps: Int? = null,
        currentRep: Int? = null
    ): Arb<Reminder> = arbitrary {
        val total = totalReps ?: Arb.int(2..10).bind()
        val current = currentRep ?: Arb.int(0 until total).bind()
        val now = timestamp().bind()
        
        Reminder(
            id = Arb.int(1..10000).bind(),
            title = Arb.string(5..50).bind(),
            priority = priorityLevel().bind(),
            startTimeInMillis = now,
            recurrenceType = RecurrenceType.ONE_TIME,
            isNagModeEnabled = true,
            nagIntervalInMillis = Arb.long(60000L..3600000L).bind(),
            nagTotalRepetitions = total,
            currentRepetitionIndex = current,
            isCompleted = false,
            isEnabled = true,
            category = reminderCategory().bind(),
            originalStartTimeInMillis = now
        )
    }
    
    /**
     * Generates a completed reminder
     */
    fun completedReminder(): Arb<Reminder> = arbitrary {
        val base = reminder().bind()
        base.copy(isCompleted = true)
    }
    
    /**
     * Generates an active (non-completed, enabled) reminder
     */
    fun activeReminder(): Arb<Reminder> = arbitrary {
        val base = reminder().bind()
        base.copy(isCompleted = false, isEnabled = true)
    }
}
