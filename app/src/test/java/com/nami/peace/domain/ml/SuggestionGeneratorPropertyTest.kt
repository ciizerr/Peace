package com.nami.peace.domain.ml

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nami.peace.data.local.AppDatabase
import com.nami.peace.data.local.CompletionEventDao
import com.nami.peace.data.local.CompletionEventEntity
import com.nami.peace.data.local.SuggestionType
import com.nami.peace.data.repository.CompletionEventRepository
import com.nami.peace.data.repository.LearningRepositoryImpl
import com.nami.peace.domain.model.PriorityLevel
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Assert.*
import java.util.concurrent.TimeUnit

/**
 * Property-based tests for ML suggestion generation.
 * 
 * **Feature: peace-app-enhancement, Property 22: Suggestion confidence score validity**
 * **Validates: Requirements 12.9**
 * 
 * Property: For any ML suggestion generated, the confidence score should be between 0 and 100 inclusive.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SuggestionGeneratorPropertyTest {
    
    private lateinit var database: AppDatabase
    private lateinit var completionEventDao: CompletionEventDao
    private lateinit var completionEventRepository: CompletionEventRepository
    private lateinit var patternAnalyzer: PatternAnalyzer
    private lateinit var suggestionGenerator: SuggestionGenerator
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        completionEventDao = database.completionEventDao()
        completionEventRepository = CompletionEventRepository(completionEventDao)
        patternAnalyzer = PatternAnalyzerImpl(completionEventRepository)
        
        // Create learning repository for testing
        val learningRepository = LearningRepositoryImpl(database.suggestionFeedbackDao())
        
        suggestionGenerator = SuggestionGeneratorImpl(patternAnalyzer, completionEventRepository, learningRepository)
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun `Property 22 - Confidence score validity - all suggestions have scores between 0 and 100`() = runTest {
        // Test with various data patterns to generate different types of suggestions
        val testScenarios = listOf(
            // Scenario 1: Optimal time pattern
            generateOptimalTimePattern(),
            // Scenario 2: Priority mismatch pattern
            generatePriorityMismatchPattern(),
            // Scenario 3: Recurring pattern
            generateRecurringPattern(),
            // Scenario 4: Focus session pattern
            generateFocusSessionPattern(),
            // Scenario 5: Habit formation pattern
            generateHabitFormationPattern(),
            // Scenario 6: Template creation pattern
            generateTemplateCreationPattern(),
            // Scenario 7: Mixed patterns
            generateMixedPattern()
        )
        
        testScenarios.forEach { events ->
            // Clear database and insert test events
            database.clearAllTables()
            events.forEach { event ->
                completionEventDao.insert(event)
            }
            
            // Generate all suggestions
            val suggestions = suggestionGenerator.generateAllSuggestions()
            
            // Verify all confidence scores are valid
            suggestions.forEach { suggestion ->
                assertTrue(
                    "Confidence score ${suggestion.confidenceScore} for ${suggestion.type} should be >= 0",
                    suggestion.confidenceScore >= 0
                )
                assertTrue(
                    "Confidence score ${suggestion.confidenceScore} for ${suggestion.type} should be <= 100",
                    suggestion.confidenceScore <= 100
                )
            }
        }
    }
    
    @Test
    fun `Property 22 - Confidence score validity - optimal time suggestions have valid scores`() = runTest {
        // Generate optimal time pattern
        val events = generateOptimalTimePattern()
        events.forEach { completionEventDao.insert(it) }
        
        // Generate optimal time suggestions
        val suggestions = suggestionGenerator.generateOptimalTimeSuggestions()
        
        // Verify all confidence scores are valid
        suggestions.forEach { suggestion ->
            assertTrue("Confidence score should be >= 0", suggestion.confidenceScore >= 0)
            assertTrue("Confidence score should be <= 100", suggestion.confidenceScore <= 100)
        }
    }
    
    @Test
    fun `Property 22 - Confidence score validity - priority adjustment suggestions have valid scores`() = runTest {
        // Generate priority mismatch pattern
        val events = generatePriorityMismatchPattern()
        events.forEach { completionEventDao.insert(it) }
        
        // Generate priority adjustment suggestions
        val suggestions = suggestionGenerator.generatePriorityAdjustmentSuggestions()
        
        // Verify all confidence scores are valid
        suggestions.forEach { suggestion ->
            assertTrue("Confidence score should be >= 0", suggestion.confidenceScore >= 0)
            assertTrue("Confidence score should be <= 100", suggestion.confidenceScore <= 100)
        }
    }
    
    @Test
    fun `Property 22 - Confidence score validity - recurring pattern suggestions have valid scores`() = runTest {
        // Generate recurring pattern
        val events = generateRecurringPattern()
        events.forEach { completionEventDao.insert(it) }
        
        // Generate recurring pattern suggestions
        val suggestions = suggestionGenerator.generateRecurringPatternSuggestions()
        
        // Verify all confidence scores are valid
        suggestions.forEach { suggestion ->
            assertTrue("Confidence score should be >= 0", suggestion.confidenceScore >= 0)
            assertTrue("Confidence score should be <= 100", suggestion.confidenceScore <= 100)
        }
    }
    
    @Test
    fun `Property 22 - Confidence score validity - break reminder suggestions have valid scores`() = runTest {
        // Generate focus session pattern
        val events = generateFocusSessionPattern()
        events.forEach { completionEventDao.insert(it) }
        
        // Generate break reminder suggestions
        val suggestions = suggestionGenerator.generateBreakReminderSuggestions()
        
        // Verify all confidence scores are valid
        suggestions.forEach { suggestion ->
            assertTrue("Confidence score should be >= 0", suggestion.confidenceScore >= 0)
            assertTrue("Confidence score should be <= 100", suggestion.confidenceScore <= 100)
        }
    }
    
    @Test
    fun `Property 22 - Confidence score validity - habit formation suggestions have valid scores`() = runTest {
        // Generate habit formation pattern
        val events = generateHabitFormationPattern()
        events.forEach { completionEventDao.insert(it) }
        
        // Generate habit formation suggestions
        val suggestions = suggestionGenerator.generateHabitFormationSuggestions()
        
        // Verify all confidence scores are valid
        suggestions.forEach { suggestion ->
            assertTrue("Confidence score should be >= 0", suggestion.confidenceScore >= 0)
            assertTrue("Confidence score should be <= 100", suggestion.confidenceScore <= 100)
        }
    }
    
    @Test
    fun `Property 22 - Confidence score validity - template creation suggestions have valid scores`() = runTest {
        // Generate template creation pattern
        val events = generateTemplateCreationPattern()
        events.forEach { completionEventDao.insert(it) }
        
        // Generate template creation suggestions
        val suggestions = suggestionGenerator.generateTemplateCreationSuggestions()
        
        // Verify all confidence scores are valid
        suggestions.forEach { suggestion ->
            assertTrue("Confidence score should be >= 0", suggestion.confidenceScore >= 0)
            assertTrue("Confidence score should be <= 100", suggestion.confidenceScore <= 100)
        }
    }
    
    @Test
    fun `Property 22 - Confidence score validity - focus session suggestions have valid scores`() = runTest {
        // Generate focus session pattern
        val events = generateFocusSessionPattern()
        events.forEach { completionEventDao.insert(it) }
        
        // Generate focus session suggestions
        val suggestions = suggestionGenerator.generateFocusSessionSuggestions()
        
        // Verify all confidence scores are valid
        suggestions.forEach { suggestion ->
            assertTrue("Confidence score should be >= 0", suggestion.confidenceScore >= 0)
            assertTrue("Confidence score should be <= 100", suggestion.confidenceScore <= 100)
        }
    }
    
    @Test
    fun `Property 22 - Confidence score validity - suggestions are sorted by confidence`() = runTest {
        // Generate mixed pattern with various confidence levels
        val events = generateMixedPattern()
        events.forEach { completionEventDao.insert(it) }
        
        // Generate all suggestions
        val suggestions = suggestionGenerator.generateAllSuggestions()
        
        // Verify suggestions are sorted by confidence (highest first)
        if (suggestions.size > 1) {
            for (i in 0 until suggestions.size - 1) {
                assertTrue(
                    "Suggestions should be sorted by confidence (highest first)",
                    suggestions[i].confidenceScore >= suggestions[i + 1].confidenceScore
                )
            }
        }
    }
    
    @Test
    fun `Property 22 - Confidence score validity - no suggestions with insufficient data`() = runTest {
        // Test with insufficient data (less than 7 events)
        val events = generateInsufficientData()
        events.forEach { completionEventDao.insert(it) }
        
        // Generate all suggestions
        val suggestions = suggestionGenerator.generateAllSuggestions()
        
        // Should return empty list with insufficient data
        assertTrue("Should return empty list with insufficient data", suggestions.isEmpty())
    }
    
    @Test
    fun `Property 22 - Confidence score validity - random data patterns produce valid scores`() = runTest {
        // Test 20 random data patterns
        repeat(20) {
            database.clearAllTables()
            
            // Generate random events
            val eventCount = (10..50).random()
            val events = generateRandomEvents(eventCount)
            events.forEach { event ->
                completionEventDao.insert(event)
            }
            
            // Generate all suggestions
            val suggestions = suggestionGenerator.generateAllSuggestions()
            
            // Verify all confidence scores are valid
            suggestions.forEach { suggestion ->
                assertTrue(
                    "Confidence score ${suggestion.confidenceScore} should be >= 0",
                    suggestion.confidenceScore >= 0
                )
                assertTrue(
                    "Confidence score ${suggestion.confidenceScore} should be <= 100",
                    suggestion.confidenceScore <= 100
                )
            }
        }
    }
    
    // Helper functions to generate test data patterns
    
    private fun generateOptimalTimePattern(): List<CompletionEventEntity> {
        val now = System.currentTimeMillis()
        val events = mutableListOf<CompletionEventEntity>()
        
        // Create pattern: task scheduled at 9am but completed at 7am consistently
        repeat(10) { i ->
            val dayOffset = TimeUnit.DAYS.toMillis(i.toLong())
            events.add(
                CompletionEventEntity(
                    reminderId = 1,
                    title = "Morning Exercise",
                    priority = PriorityLevel.HIGH,
                    category = com.nami.peace.domain.model.ReminderCategory.HEALTH,
                    scheduledTimeInMillis = now - dayOffset + TimeUnit.HOURS.toMillis(9),
                    completedTimeInMillis = now - dayOffset + TimeUnit.HOURS.toMillis(7),
                    completionDelayInMillis = -TimeUnit.HOURS.toMillis(2),
                    hourOfDay = 7,
                    dayOfWeek = ((i % 7) + 1),
                    wasRecurring = false,
                    wasNagMode = false,
                    nagRepetitionIndex = null,
                    nagTotalRepetitions = null,
                    recurrenceType = com.nami.peace.domain.model.RecurrenceType.ONE_TIME
                )
            )
        }
        
        return events
    }
    
    private fun generatePriorityMismatchPattern(): List<CompletionEventEntity> {
        val now = System.currentTimeMillis()
        val events = mutableListOf<CompletionEventEntity>()
        
        // Create pattern: HIGH priority task consistently completed late
        repeat(10) { i ->
            val dayOffset = TimeUnit.DAYS.toMillis(i.toLong())
            events.add(
                CompletionEventEntity(
                    reminderId = 2,
                    title = "Check Email",
                    priority = PriorityLevel.HIGH,
                    category = com.nami.peace.domain.model.ReminderCategory.WORK,
                    scheduledTimeInMillis = now - dayOffset + TimeUnit.HOURS.toMillis(9),
                    completedTimeInMillis = now - dayOffset + TimeUnit.HOURS.toMillis(12),
                    completionDelayInMillis = TimeUnit.HOURS.toMillis(3),
                    hourOfDay = 12,
                    dayOfWeek = ((i % 7) + 1),
                    wasRecurring = false,
                    wasNagMode = false,
                    nagRepetitionIndex = null,
                    nagTotalRepetitions = null,
                    recurrenceType = com.nami.peace.domain.model.RecurrenceType.ONE_TIME
                )
            )
        }
        
        return events
    }
    
    private fun generateRecurringPattern(): List<CompletionEventEntity> {
        val now = System.currentTimeMillis()
        val events = mutableListOf<CompletionEventEntity>()
        
        // Create pattern: same task created multiple times
        repeat(5) { i ->
            val dayOffset = TimeUnit.DAYS.toMillis(i.toLong())
            events.add(
                CompletionEventEntity(
                    reminderId = 100 + i, // Different reminder IDs
                    title = "Drink Water",
                    priority = PriorityLevel.MEDIUM,
                    category = com.nami.peace.domain.model.ReminderCategory.HEALTH,
                    scheduledTimeInMillis = now - dayOffset + TimeUnit.HOURS.toMillis(10),
                    completedTimeInMillis = now - dayOffset + TimeUnit.HOURS.toMillis(10),
                    completionDelayInMillis = 0,
                    hourOfDay = 10,
                    dayOfWeek = ((i % 7) + 1),
                    wasRecurring = false,
                    wasNagMode = false,
                    nagRepetitionIndex = null,
                    nagTotalRepetitions = null,
                    recurrenceType = com.nami.peace.domain.model.RecurrenceType.ONE_TIME
                )
            )
        }
        
        return events
    }
    
    private fun generateFocusSessionPattern(): List<CompletionEventEntity> {
        val now = System.currentTimeMillis()
        val events = mutableListOf<CompletionEventEntity>()
        
        // Create pattern: multiple tasks completed in quick succession (focus session)
        repeat(3) { sessionIndex ->
            val sessionStart = now - TimeUnit.DAYS.toMillis(sessionIndex.toLong())
            
            repeat(5) { taskIndex ->
                events.add(
                    CompletionEventEntity(
                        reminderId = 200 + sessionIndex * 10 + taskIndex,
                        title = "Work Task $taskIndex",
                        priority = PriorityLevel.HIGH,
                        category = com.nami.peace.domain.model.ReminderCategory.WORK,
                        scheduledTimeInMillis = sessionStart + TimeUnit.MINUTES.toMillis(taskIndex * 20L),
                        completedTimeInMillis = sessionStart + TimeUnit.MINUTES.toMillis(taskIndex * 20L),
                        completionDelayInMillis = 0,
                        hourOfDay = 9 + (taskIndex / 3),
                        dayOfWeek = ((sessionIndex % 7) + 1),
                        wasRecurring = false,
                        wasNagMode = false,
                        nagRepetitionIndex = null,
                        nagTotalRepetitions = null,
                        recurrenceType = com.nami.peace.domain.model.RecurrenceType.ONE_TIME
                    )
                )
            }
        }
        
        return events
    }
    
    private fun generateHabitFormationPattern(): List<CompletionEventEntity> {
        val now = System.currentTimeMillis()
        val events = mutableListOf<CompletionEventEntity>()
        
        // Create pattern: task completed for 10 consecutive days
        repeat(10) { i ->
            val dayOffset = TimeUnit.DAYS.toMillis(i.toLong())
            events.add(
                CompletionEventEntity(
                    reminderId = 3,
                    title = "Morning Meditation",
                    priority = PriorityLevel.MEDIUM,
                    category = com.nami.peace.domain.model.ReminderCategory.HEALTH,
                    scheduledTimeInMillis = now - dayOffset + TimeUnit.HOURS.toMillis(7),
                    completedTimeInMillis = now - dayOffset + TimeUnit.HOURS.toMillis(7),
                    completionDelayInMillis = 0,
                    hourOfDay = 7,
                    dayOfWeek = ((i % 7) + 1),
                    wasRecurring = false,
                    wasNagMode = false,
                    nagRepetitionIndex = null,
                    nagTotalRepetitions = null,
                    recurrenceType = com.nami.peace.domain.model.RecurrenceType.ONE_TIME
                )
            )
        }
        
        return events
    }
    
    private fun generateTemplateCreationPattern(): List<CompletionEventEntity> {
        val now = System.currentTimeMillis()
        val events = mutableListOf<CompletionEventEntity>()
        
        // Create pattern: similar tasks created multiple times
        val similarTitles = listOf(
            "Buy groceries at store",
            "Buy groceries online",
            "Buy groceries tomorrow",
            "Buy groceries for week",
            "Buy groceries and supplies",
            "Buy weekly groceries"
        )
        
        similarTitles.forEachIndexed { index, title ->
            events.add(
                CompletionEventEntity(
                    reminderId = 300 + index,
                    title = title,
                    priority = PriorityLevel.MEDIUM,
                    category = com.nami.peace.domain.model.ReminderCategory.HOME,
                    scheduledTimeInMillis = now - TimeUnit.DAYS.toMillis(index.toLong()),
                    completedTimeInMillis = now - TimeUnit.DAYS.toMillis(index.toLong()),
                    completionDelayInMillis = 0,
                    hourOfDay = 10,
                    dayOfWeek = ((index % 7) + 1),
                    wasRecurring = false,
                    wasNagMode = false,
                    nagRepetitionIndex = null,
                    nagTotalRepetitions = null,
                    recurrenceType = com.nami.peace.domain.model.RecurrenceType.ONE_TIME
                )
            )
        }
        
        return events
    }
    
    private fun generateMixedPattern(): List<CompletionEventEntity> {
        val events = mutableListOf<CompletionEventEntity>()
        events.addAll(generateOptimalTimePattern())
        events.addAll(generatePriorityMismatchPattern())
        events.addAll(generateRecurringPattern())
        return events
    }
    
    private fun generateInsufficientData(): List<CompletionEventEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            CompletionEventEntity(
                reminderId = 1,
                title = "Task 1",
                priority = PriorityLevel.MEDIUM,
                category = com.nami.peace.domain.model.ReminderCategory.GENERAL,
                scheduledTimeInMillis = now,
                completedTimeInMillis = now,
                completionDelayInMillis = 0,
                hourOfDay = 10,
                dayOfWeek = 1,
                wasRecurring = false,
                wasNagMode = false,
                nagRepetitionIndex = null,
                nagTotalRepetitions = null,
                recurrenceType = com.nami.peace.domain.model.RecurrenceType.ONE_TIME
            ),
            CompletionEventEntity(
                reminderId = 2,
                title = "Task 2",
                priority = PriorityLevel.MEDIUM,
                category = com.nami.peace.domain.model.ReminderCategory.GENERAL,
                scheduledTimeInMillis = now,
                completedTimeInMillis = now,
                completionDelayInMillis = 0,
                hourOfDay = 11,
                dayOfWeek = 1,
                wasRecurring = false,
                wasNagMode = false,
                nagRepetitionIndex = null,
                nagTotalRepetitions = null,
                recurrenceType = com.nami.peace.domain.model.RecurrenceType.ONE_TIME
            )
        )
    }
    
    private fun generateRandomEvents(count: Int): List<CompletionEventEntity> {
        val now = System.currentTimeMillis()
        val events = mutableListOf<CompletionEventEntity>()
        
        repeat(count) { i ->
            val dayOffset = TimeUnit.DAYS.toMillis((0..30).random().toLong())
            val hourOfDay = (0..23).random()
            val delay = TimeUnit.HOURS.toMillis((-2..4).random().toLong())
            
            events.add(
                CompletionEventEntity(
                    reminderId = i,
                    title = "Random Task $i",
                    priority = PriorityLevel.values().random(),
                    category = com.nami.peace.domain.model.ReminderCategory.values().random(),
                    scheduledTimeInMillis = now - dayOffset + TimeUnit.HOURS.toMillis(hourOfDay.toLong()),
                    completedTimeInMillis = now - dayOffset + TimeUnit.HOURS.toMillis(hourOfDay.toLong()) + delay,
                    completionDelayInMillis = delay,
                    hourOfDay = hourOfDay,
                    dayOfWeek = ((i % 7) + 1),
                    wasRecurring = (0..1).random() == 1,
                    wasNagMode = false,
                    nagRepetitionIndex = null,
                    nagTotalRepetitions = null,
                    recurrenceType = if ((0..1).random() == 1) com.nami.peace.domain.model.RecurrenceType.DAILY else com.nami.peace.domain.model.RecurrenceType.ONE_TIME
                )
            )
        }
        
        return events
    }
}
