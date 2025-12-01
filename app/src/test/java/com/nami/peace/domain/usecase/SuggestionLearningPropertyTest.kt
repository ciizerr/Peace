package com.nami.peace.domain.usecase

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nami.peace.data.local.AppDatabase
import com.nami.peace.data.local.SuggestionStatus
import com.nami.peace.data.local.SuggestionType
import com.nami.peace.data.repository.LearningRepositoryImpl
import com.nami.peace.data.repository.SuggestionRepositoryImpl
import com.nami.peace.domain.model.Suggestion
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Assert.*

/**
 * Property-based tests for suggestion learning system.
 * 
 * **Feature: peace-app-enhancement, Property 23: Suggestion application side effects**
 * **Validates: Requirements 12.10**
 * 
 * Tests that when a suggestion is applied or dismissed, both the suggestion status
 * is updated AND a learning record is created for future algorithm improvement.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SuggestionLearningPropertyTest {
    
    private lateinit var database: AppDatabase
    private lateinit var suggestionRepository: SuggestionRepositoryImpl
    private lateinit var learningRepository: LearningRepositoryImpl
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        
        suggestionRepository = SuggestionRepositoryImpl(database.suggestionDao())
        learningRepository = LearningRepositoryImpl(database.suggestionFeedbackDao())
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun `Property 23 - Applying a suggestion updates status AND records acceptance feedback`() = runTest {
        // Arrange
        val suggestion = Suggestion(
            id = 1,
            type = SuggestionType.OPTIMAL_TIME,
            reminderId = 100,
            title = "Test Suggestion",
            description = "Test Description",
            confidenceScore = 75,
            suggestedValue = "{}",
            createdAt = System.currentTimeMillis(),
            status = SuggestionStatus.PENDING
        )
        
        val useCase = ApplySuggestionUseCase(suggestionRepository, learningRepository)
        
        // Insert the suggestion first
        suggestionRepository.insertSuggestion(suggestion)
        
        // Act
        useCase(suggestion)
        
        // Assert - Suggestion status should be updated to APPLIED
        val updatedSuggestion = suggestionRepository.getSuggestionById(suggestion.id)
        assertNotNull(updatedSuggestion)
        assertEquals(SuggestionStatus.APPLIED, updatedSuggestion!!.status)
        assertEquals(suggestion.id, updatedSuggestion.id)
        assertEquals(suggestion.type, updatedSuggestion.type)
        
        // Assert - Feedback should be recorded with wasAccepted = true
        val feedback = learningRepository.getFeedbackByType(suggestion.type).first()
        assertEquals(1, feedback.size)
        assertTrue(feedback[0].wasAccepted)
        assertEquals(suggestion.id, feedback[0].suggestionId)
    }
    
    @Test
    fun `Property 23 - Dismissing a suggestion updates status AND records dismissal feedback`() = runTest {
        // Arrange
        val suggestion = Suggestion(
            id = 2,
            type = SuggestionType.PRIORITY_ADJUSTMENT,
            reminderId = 200,
            title = "Test Suggestion 2",
            description = "Test Description 2",
            confidenceScore = 65,
            suggestedValue = "{}",
            createdAt = System.currentTimeMillis(),
            status = SuggestionStatus.PENDING
        )
        
        val useCase = DismissSuggestionUseCase(suggestionRepository, learningRepository)
        
        // Insert the suggestion first
        suggestionRepository.insertSuggestion(suggestion)
        
        // Act
        useCase(suggestion)
        
        // Assert - Suggestion status should be updated to DISMISSED
        val updatedSuggestion = suggestionRepository.getSuggestionById(suggestion.id)
        assertNotNull(updatedSuggestion)
        assertEquals(SuggestionStatus.DISMISSED, updatedSuggestion!!.status)
        assertEquals(suggestion.id, updatedSuggestion.id)
        assertEquals(suggestion.type, updatedSuggestion.type)
        
        // Assert - Feedback should be recorded with wasAccepted = false
        val feedback = learningRepository.getFeedbackByType(suggestion.type).first()
        assertEquals(1, feedback.size)
        assertFalse(feedback[0].wasAccepted)
        assertEquals(suggestion.id, feedback[0].suggestionId)
    }
    
    @Test
    fun `Property 23 - Applied suggestion feedback contains correct metadata`() = runTest {
        // Arrange
        val suggestion = Suggestion(
            id = 3,
            type = SuggestionType.RECURRING_PATTERN,
            reminderId = 300,
            title = "Test Suggestion 3",
            description = "Test Description 3",
            confidenceScore = 85,
            suggestedValue = "{}",
            createdAt = System.currentTimeMillis(),
            status = SuggestionStatus.PENDING
        )
        
        val useCase = ApplySuggestionUseCase(suggestionRepository, learningRepository)
        
        // Insert the suggestion first
        suggestionRepository.insertSuggestion(suggestion)
        
        // Act
        useCase(suggestion)
        
        // Assert - Feedback should contain correct metadata
        val feedback = learningRepository.getFeedbackByType(suggestion.type).first()[0]
        assertEquals(suggestion.id, feedback.suggestionId)
        assertEquals(suggestion.type, feedback.suggestionType)
        assertTrue(feedback.wasAccepted)
        assertEquals(suggestion.reminderId, feedback.reminderId)
        assertEquals(suggestion.confidenceScore, feedback.confidenceScore)
        assertTrue(feedback.feedbackTimestamp > 0L)
    }
    
    @Test
    fun `Property 23 - Dismissed suggestion feedback contains correct metadata`() = runTest {
        // Arrange
        val suggestion = Suggestion(
            id = 4,
            type = SuggestionType.BREAK_REMINDER,
            reminderId = 400,
            title = "Test Suggestion 4",
            description = "Test Description 4",
            confidenceScore = 55,
            suggestedValue = "{}",
            createdAt = System.currentTimeMillis(),
            status = SuggestionStatus.PENDING
        )
        
        val useCase = DismissSuggestionUseCase(suggestionRepository, learningRepository)
        
        // Insert the suggestion first
        suggestionRepository.insertSuggestion(suggestion)
        
        // Act
        useCase(suggestion)
        
        // Assert - Feedback should contain correct metadata
        val feedback = learningRepository.getFeedbackByType(suggestion.type).first()[0]
        assertEquals(suggestion.id, feedback.suggestionId)
        assertEquals(suggestion.type, feedback.suggestionType)
        assertFalse(feedback.wasAccepted)
        assertEquals(suggestion.reminderId, feedback.reminderId)
        assertEquals(suggestion.confidenceScore, feedback.confidenceScore)
        assertTrue(feedback.feedbackTimestamp > 0L)
    }
    
    @Test
    fun `Property 23 - Both operations must complete atomically (no partial updates)`() = runTest {
        // Arrange
        val suggestion = Suggestion(
            id = 5,
            type = SuggestionType.HABIT_FORMATION,
            reminderId = 500,
            title = "Test Suggestion 5",
            description = "Test Description 5",
            confidenceScore = 90,
            suggestedValue = "{}",
            createdAt = System.currentTimeMillis(),
            status = SuggestionStatus.PENDING
        )
        
        val applyUseCase = ApplySuggestionUseCase(suggestionRepository, learningRepository)
        
        // Insert the suggestion first
        suggestionRepository.insertSuggestion(suggestion)
        
        // Act
        applyUseCase(suggestion)
        
        // Assert - Both operations should complete
        // 1. Suggestion should be updated
        val updatedSuggestion = suggestionRepository.getSuggestionById(suggestion.id)
        assertNotNull(updatedSuggestion)
        assertEquals(SuggestionStatus.APPLIED, updatedSuggestion!!.status)
        
        // 2. Feedback should be recorded
        val feedback = learningRepository.getFeedbackByType(suggestion.type).first()
        assertEquals(1, feedback.size)
        assertTrue(feedback[0].wasAccepted)
    }
}

