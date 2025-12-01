package com.nami.peace.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nami.peace.data.local.*
import com.nami.peace.domain.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
 * Integration tests for database operations.
 * 
 * Tests:
 * - Foreign key cascade deletes
 * - Transaction rollback on errors
 * - Complex queries with joins
 * 
 * **Validates: Requirements 4.1, 5.1, 5.2, 18.3, 12.2**
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DatabaseIntegrationTest {
    
    private lateinit var database: AppDatabase
    private lateinit var reminderDao: ReminderDao
    private lateinit var subtaskDao: SubtaskDao
    private lateinit var noteDao: NoteDao
    private lateinit var attachmentDao: AttachmentDao
    private lateinit var gardenDao: GardenDao
    private lateinit var suggestionDao: SuggestionDao
    private lateinit var completionEventDao: CompletionEventDao
    private lateinit var suggestionFeedbackDao: SuggestionFeedbackDao
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        
        reminderDao = database.reminderDao()
        subtaskDao = database.subtaskDao()
        noteDao = database.noteDao()
        attachmentDao = database.attachmentDao()
        gardenDao = database.gardenDao()
        suggestionDao = database.suggestionDao()
        completionEventDao = database.completionEventDao()
        suggestionFeedbackDao = database.suggestionFeedbackDao()
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    // ========== Test 1: Foreign Key Cascade Deletes ==========
    
    @Test
    fun `deleting reminder cascades to subtasks`() = runTest {
        // Arrange: Create reminder with subtasks
        val reminder = createTestReminderEntity(title = "Parent Reminder")
        val reminderId = reminderDao.insert(reminder).toInt()
        
        val subtask1 = SubtaskEntity(
            id = 0,
            reminderId = reminderId,
            title = "Subtask 1",
            isCompleted = false,
            order = 0,
            createdAt = System.currentTimeMillis()
        )
        val subtask2 = SubtaskEntity(
            id = 0,
            reminderId = reminderId,
            title = "Subtask 2",
            isCompleted = false,
            order = 1,
            createdAt = System.currentTimeMillis()
        )
        
        subtaskDao.insert(subtask1)
        subtaskDao.insert(subtask2)
        
        // Verify subtasks exist
        val subtasksBefore = subtaskDao.getSubtasksForReminder(reminderId).first()
        assertEquals("Should have 2 subtasks", 2, subtasksBefore.size)
        
        // Act: Delete reminder
        reminderDao.delete(reminder.copy(id = reminderId))
        
        // Assert: Subtasks should be deleted (cascade)
        val subtasksAfter = subtaskDao.getSubtasksForReminder(reminderId).first()
        assertEquals("Subtasks should be deleted via cascade", 0, subtasksAfter.size)
    }
    
    @Test
    fun `deleting reminder cascades to notes`() = runTest {
        // Arrange: Create reminder with notes
        val reminder = createTestReminderEntity(title = "Reminder with Notes")
        val reminderId = reminderDao.insert(reminder).toInt()
        
        val note1 = NoteEntity(
            id = 0,
            reminderId = reminderId,
            content = "First note",
            timestamp = System.currentTimeMillis()
        )
        val note2 = NoteEntity(
            id = 0,
            reminderId = reminderId,
            content = "Second note",
            timestamp = System.currentTimeMillis() + 1000
        )
        
        noteDao.insert(note1)
        noteDao.insert(note2)
        
        // Verify notes exist
        val notesBefore = noteDao.getNotesForReminder(reminderId).first()
        assertEquals("Should have 2 notes", 2, notesBefore.size)
        
        // Act: Delete reminder
        reminderDao.delete(reminder.copy(id = reminderId))
        
        // Assert: Notes should be deleted (cascade)
        val notesAfter = noteDao.getNotesForReminder(reminderId).first()
        assertEquals("Notes should be deleted via cascade", 0, notesAfter.size)
    }
    
    @Test
    fun `deleting reminder cascades to attachments`() = runTest {
        // Arrange: Create reminder with attachments
        val reminder = createTestReminderEntity(title = "Reminder with Attachments")
        val reminderId = reminderDao.insert(reminder).toInt()
        
        val attachment1 = AttachmentEntity(
            id = 0,
            reminderId = reminderId,
            filePath = "/path/to/image1.jpg",
            thumbnailPath = "/path/to/thumb1.jpg",
            timestamp = System.currentTimeMillis(),
            mimeType = "image/jpeg"
        )
        val attachment2 = AttachmentEntity(
            id = 0,
            reminderId = reminderId,
            filePath = "/path/to/image2.jpg",
            thumbnailPath = "/path/to/thumb2.jpg",
            timestamp = System.currentTimeMillis() + 1000,
            mimeType = "image/jpeg"
        )
        
        attachmentDao.insert(attachment1)
        attachmentDao.insert(attachment2)
        
        // Verify attachments exist
        val attachmentsBefore = attachmentDao.getAttachmentsForReminder(reminderId).first()
        assertEquals("Should have 2 attachments", 2, attachmentsBefore.size)
        
        // Act: Delete reminder
        reminderDao.delete(reminder.copy(id = reminderId))
        
        // Assert: Attachments should be deleted (cascade)
        val attachmentsAfter = attachmentDao.getAttachmentsForReminder(reminderId).first()
        assertEquals("Attachments should be deleted via cascade", 0, attachmentsAfter.size)
    }
    
    @Test
    fun `deleting reminder cascades to all related entities`() = runTest {
        // Arrange: Create reminder with subtasks, notes, and attachments
        val reminder = createTestReminderEntity(title = "Complex Reminder")
        val reminderId = reminderDao.insert(reminder).toInt()
        
        // Add subtasks
        subtaskDao.insert(SubtaskEntity(0, reminderId, "Subtask", false, 0, System.currentTimeMillis()))
        
        // Add notes
        noteDao.insert(NoteEntity(0, reminderId, "Note", System.currentTimeMillis()))
        
        // Add attachments
        attachmentDao.insert(AttachmentEntity(0, reminderId, "/path", "/thumb", System.currentTimeMillis(), "image/jpeg"))
        
        // Verify all exist
        assertEquals(1, subtaskDao.getSubtasksForReminder(reminderId).first().size)
        assertEquals(1, noteDao.getNotesForReminder(reminderId).first().size)
        assertEquals(1, attachmentDao.getAttachmentsForReminder(reminderId).first().size)
        
        // Act: Delete reminder
        reminderDao.delete(reminder.copy(id = reminderId))
        
        // Assert: All related entities deleted
        assertEquals("Subtasks deleted", 0, subtaskDao.getSubtasksForReminder(reminderId).first().size)
        assertEquals("Notes deleted", 0, noteDao.getNotesForReminder(reminderId).first().size)
        assertEquals("Attachments deleted", 0, attachmentDao.getAttachmentsForReminder(reminderId).first().size)
    }
    
    // ========== Test 2: Transaction Rollback ==========
    
    @Test
    fun `transaction rollback on error preserves data integrity`() = runTest {
        // Arrange: Create initial reminder
        val reminder = createTestReminderEntity(title = "Original Reminder")
        val reminderId = reminderDao.insert(reminder).toInt()
        
        // Act & Assert: Try to perform invalid operation in transaction
        try {
            database.runInTransaction {
                // Update reminder
                reminderDao.update(reminder.copy(id = reminderId, title = "Updated Title"))
                
                // Try to insert subtask with invalid foreign key (should fail)
                subtaskDao.insert(SubtaskEntity(
                    id = 0,
                    reminderId = 99999, // Non-existent reminder ID
                    title = "Invalid Subtask",
                    isCompleted = false,
                    order = 0,
                    createdAt = System.currentTimeMillis()
                ))
            }
            fail("Should throw exception for foreign key violation")
        } catch (e: Exception) {
            // Expected exception
        }
        
        // Assert: Original reminder should be unchanged (transaction rolled back)
        val retrievedReminder = reminderDao.getReminderById(reminderId)
        assertNotNull("Reminder should still exist", retrievedReminder)
        assertEquals("Title should be original (rollback)", "Original Reminder", retrievedReminder?.title)
    }
    
    @Test
    fun `successful transaction commits all changes`() = runTest {
        // Arrange: Create reminder
        val reminder = createTestReminderEntity(title = "Test Reminder")
        val reminderId = reminderDao.insert(reminder).toInt()
        
        // Act: Perform multiple operations in transaction
        database.runInTransaction {
            // Update reminder
            reminderDao.update(reminder.copy(id = reminderId, title = "Updated Reminder"))
            
            // Add subtask
            subtaskDao.insert(SubtaskEntity(
                id = 0,
                reminderId = reminderId,
                title = "New Subtask",
                isCompleted = false,
                order = 0,
                createdAt = System.currentTimeMillis()
            ))
            
            // Add note
            noteDao.insert(NoteEntity(
                id = 0,
                reminderId = reminderId,
                content = "New Note",
                timestamp = System.currentTimeMillis()
            ))
        }
        
        // Assert: All changes should be committed
        val retrievedReminder = reminderDao.getReminderById(reminderId)
        assertEquals("Reminder title updated", "Updated Reminder", retrievedReminder?.title)
        
        val subtasks = subtaskDao.getSubtasksForReminder(reminderId).first()
        assertEquals("Subtask added", 1, subtasks.size)
        
        val notes = noteDao.getNotesForReminder(reminderId).first()
        assertEquals("Note added", 1, notes.size)
    }
    
    // ========== Test 3: Complex Queries ==========
    
    @Test
    fun `query reminders with subtask progress calculation`() = runTest {
        // Arrange: Create reminder with mixed completion subtasks
        val reminder = createTestReminderEntity(title = "Task with Progress")
        val reminderId = reminderDao.insert(reminder).toInt()
        
        subtaskDao.insert(SubtaskEntity(0, reminderId, "Subtask 1", true, 0, System.currentTimeMillis()))
        subtaskDao.insert(SubtaskEntity(0, reminderId, "Subtask 2", true, 1, System.currentTimeMillis()))
        subtaskDao.insert(SubtaskEntity(0, reminderId, "Subtask 3", false, 2, System.currentTimeMillis()))
        subtaskDao.insert(SubtaskEntity(0, reminderId, "Subtask 4", false, 3, System.currentTimeMillis()))
        
        // Act: Calculate progress
        val totalCount = subtaskDao.getSubtaskCount(reminderId)
        val completedCount = subtaskDao.getCompletedSubtaskCount(reminderId)
        val progress = if (totalCount > 0) (completedCount * 100) / totalCount else 0
        
        // Assert: Progress should be 50%
        assertEquals("Total subtasks", 4, totalCount)
        assertEquals("Completed subtasks", 2, completedCount)
        assertEquals("Progress percentage", 50, progress)
    }
    
    @Test
    fun `query notes in chronological order`() = runTest {
        // Arrange: Create reminder with notes at different times
        val reminder = createTestReminderEntity(title = "Reminder with Notes")
        val reminderId = reminderDao.insert(reminder).toInt()
        
        val baseTime = System.currentTimeMillis()
        noteDao.insert(NoteEntity(0, reminderId, "Third note", baseTime + 2000))
        noteDao.insert(NoteEntity(0, reminderId, "First note", baseTime))
        noteDao.insert(NoteEntity(0, reminderId, "Second note", baseTime + 1000))
        
        // Act: Retrieve notes (should be ordered by timestamp)
        val notes = noteDao.getNotesForReminder(reminderId).first()
        
        // Assert: Notes should be in chronological order
        assertEquals("Should have 3 notes", 3, notes.size)
        assertEquals("First note", "First note", notes[0].content)
        assertEquals("Second note", "Second note", notes[1].content)
        assertEquals("Third note", "Third note", notes[2].content)
    }
    
    @Test
    fun `query attachments in chronological order`() = runTest {
        // Arrange: Create reminder with attachments at different times
        val reminder = createTestReminderEntity(title = "Reminder with Attachments")
        val reminderId = reminderDao.insert(reminder).toInt()
        
        val baseTime = System.currentTimeMillis()
        attachmentDao.insert(AttachmentEntity(0, reminderId, "/path3", "/thumb3", baseTime + 2000, "image/jpeg"))
        attachmentDao.insert(AttachmentEntity(0, reminderId, "/path1", "/thumb1", baseTime, "image/jpeg"))
        attachmentDao.insert(AttachmentEntity(0, reminderId, "/path2", "/thumb2", baseTime + 1000, "image/jpeg"))
        
        // Act: Retrieve attachments (should be ordered by timestamp)
        val attachments = attachmentDao.getAttachmentsForReminder(reminderId).first()
        
        // Assert: Attachments should be in chronological order
        assertEquals("Should have 3 attachments", 3, attachments.size)
        assertEquals("First attachment", "/path1", attachments[0].filePath)
        assertEquals("Second attachment", "/path2", attachments[1].filePath)
        assertEquals("Third attachment", "/path3", attachments[2].filePath)
    }
    
    // ========== Test 4: Garden State Persistence ==========
    
    @Test
    fun `garden state persists across operations`() = runTest {
        // Arrange: Initialize garden state
        val initialState = GardenEntity(
            id = 1,
            theme = GardenTheme.ZEN,
            growthStage = 0,
            currentStreak = 0,
            longestStreak = 0,
            lastCompletionDate = null,
            totalTasksCompleted = 0
        )
        gardenDao.insertOrUpdate(initialState)
        
        // Act: Update garden state
        val updatedState = initialState.copy(
            growthStage = 3,
            currentStreak = 7,
            longestStreak = 10,
            lastCompletionDate = System.currentTimeMillis(),
            totalTasksCompleted = 25
        )
        gardenDao.insertOrUpdate(updatedState)
        
        // Assert: State should be updated
        val retrievedState = gardenDao.getGardenState().first()
        assertNotNull("Garden state should exist", retrievedState)
        assertEquals("Growth stage", 3, retrievedState?.growthStage)
        assertEquals("Current streak", 7, retrievedState?.currentStreak)
        assertEquals("Longest streak", 10, retrievedState?.longestStreak)
        assertEquals("Total tasks", 25, retrievedState?.totalTasksCompleted)
    }
    
    @Test
    fun `garden theme change persists`() = runTest {
        // Arrange: Initialize with ZEN theme
        gardenDao.insertOrUpdate(GardenEntity(1, GardenTheme.ZEN, 0, 0, 0, null, 0))
        
        // Act: Change to each theme
        val themes = listOf(GardenTheme.FOREST, GardenTheme.DESERT, GardenTheme.OCEAN, GardenTheme.ZEN)
        themes.forEach { theme ->
            val state = gardenDao.getGardenState().first()!!
            gardenDao.insertOrUpdate(state.copy(theme = theme))
            
            // Assert: Theme should be updated
            val updatedState = gardenDao.getGardenState().first()
            assertEquals("Theme should be $theme", theme, updatedState?.theme)
        }
    }
    
    // ========== Test 5: ML Data Collection ==========
    
    @Test
    fun `completion events are stored correctly`() = runTest {
        // Arrange: Create completion event
        val event = CompletionEventEntity(
            id = 0,
            reminderId = 1,
            title = "Test Task",
            priority = PriorityLevel.HIGH.name,
            category = ReminderCategory.WORK.name,
            scheduledTimeInMillis = System.currentTimeMillis(),
            completedTimeInMillis = System.currentTimeMillis() + 60000,
            completionDelayInMillis = 60000,
            wasNagMode = false,
            nagRepetitionIndex = null,
            nagTotalRepetitions = null,
            dayOfWeek = 1,
            hourOfDay = 14,
            wasRecurring = false,
            recurrenceType = RecurrenceType.ONE_TIME.name
        )
        
        // Act: Insert event
        completionEventDao.insert(event)
        
        // Assert: Event should be retrievable
        val events = completionEventDao.getAllEvents().first()
        assertEquals("Should have 1 event", 1, events.size)
        assertEquals("Title matches", "Test Task", events[0].title)
        assertEquals("Priority matches", PriorityLevel.HIGH.name, events[0].priority)
    }
    
    @Test
    fun `old completion events can be deleted`() = runTest {
        // Arrange: Create events at different times
        val now = System.currentTimeMillis()
        val oldTime = now - (91 * 24 * 60 * 60 * 1000L) // 91 days ago
        val recentTime = now - (30 * 24 * 60 * 60 * 1000L) // 30 days ago
        
        completionEventDao.insert(createCompletionEvent(completedTime = oldTime))
        completionEventDao.insert(createCompletionEvent(completedTime = recentTime))
        completionEventDao.insert(createCompletionEvent(completedTime = now))
        
        // Act: Delete events older than 90 days
        val cutoffTime = now - (90 * 24 * 60 * 60 * 1000L)
        completionEventDao.deleteEventsOlderThan(cutoffTime)
        
        // Assert: Only recent events remain
        val remainingEvents = completionEventDao.getAllEvents().first()
        assertEquals("Should have 2 recent events", 2, remainingEvents.size)
        assertTrue("All events should be recent", 
            remainingEvents.all { it.completedTimeInMillis >= cutoffTime })
    }
    
    // ========== Test 6: Suggestion Feedback ==========
    
    @Test
    fun `suggestion feedback is stored correctly`() = runTest {
        // Arrange: Create feedback
        val feedback = SuggestionFeedbackEntity(
            id = 0,
            suggestionId = 1,
            suggestionType = SuggestionType.OPTIMAL_TIME.name,
            wasAccepted = true,
            feedbackTimestamp = System.currentTimeMillis(),
            reminderId = 1,
            confidenceScore = 85
        )
        
        // Act: Insert feedback
        suggestionFeedbackDao.insert(feedback)
        
        // Assert: Feedback should be retrievable
        val allFeedback = suggestionFeedbackDao.getAllFeedback().first()
        assertEquals("Should have 1 feedback", 1, allFeedback.size)
        assertEquals("Type matches", SuggestionType.OPTIMAL_TIME.name, allFeedback[0].suggestionType)
        assertTrue("Was accepted", allFeedback[0].wasAccepted)
    }
    
    @Test
    fun `old suggestion feedback can be deleted`() = runTest {
        // Arrange: Create feedback at different times
        val now = System.currentTimeMillis()
        val oldTime = now - (181 * 24 * 60 * 60 * 1000L) // 181 days ago
        val recentTime = now - (90 * 24 * 60 * 60 * 1000L) // 90 days ago
        
        suggestionFeedbackDao.insert(createSuggestionFeedback(timestamp = oldTime))
        suggestionFeedbackDao.insert(createSuggestionFeedback(timestamp = recentTime))
        suggestionFeedbackDao.insert(createSuggestionFeedback(timestamp = now))
        
        // Act: Delete feedback older than 180 days
        val cutoffTime = now - (180 * 24 * 60 * 60 * 1000L)
        suggestionFeedbackDao.deleteFeedbackOlderThan(cutoffTime)
        
        // Assert: Only recent feedback remains
        val remainingFeedback = suggestionFeedbackDao.getAllFeedback().first()
        assertEquals("Should have 2 recent feedback entries", 2, remainingFeedback.size)
        assertTrue("All feedback should be recent",
            remainingFeedback.all { it.feedbackTimestamp >= cutoffTime })
    }
    
    // ========== Test 7: Index Performance ==========
    
    @Test
    fun `foreign key indexes improve query performance`() = runTest {
        // Arrange: Create reminder with many subtasks
        val reminder = createTestReminderEntity(title = "Performance Test")
        val reminderId = reminderDao.insert(reminder).toInt()
        
        // Insert 100 subtasks
        repeat(100) { i ->
            subtaskDao.insert(SubtaskEntity(
                id = 0,
                reminderId = reminderId,
                title = "Subtask $i",
                isCompleted = i % 2 == 0,
                order = i,
                createdAt = System.currentTimeMillis()
            ))
        }
        
        // Act: Query subtasks (should use index on reminderId)
        val startTime = System.nanoTime()
        val subtasks = subtaskDao.getSubtasksForReminder(reminderId).first()
        val endTime = System.nanoTime()
        val durationMs = (endTime - startTime) / 1_000_000
        
        // Assert: Query should be fast (< 100ms) and return all subtasks
        assertEquals("Should retrieve all 100 subtasks", 100, subtasks.size)
        assertTrue("Query should be fast with index (< 100ms)", durationMs < 100)
    }
    
    // ========== Helper Functions ==========
    
    private fun createTestReminderEntity(
        title: String,
        priority: PriorityLevel = PriorityLevel.MEDIUM,
        category: ReminderCategory = ReminderCategory.GENERAL
    ): ReminderEntity {
        val startTime = System.currentTimeMillis() + 3600000
        return ReminderEntity(
            id = 0,
            title = title,
            priority = priority,
            startTimeInMillis = startTime,
            recurrenceType = RecurrenceType.ONE_TIME,
            isNagModeEnabled = false,
            nagIntervalInMillis = null,
            nagTotalRepetitions = 1,
            currentRepetitionIndex = 0,
            isCompleted = false,
            isEnabled = true,
            isInNestedSnoozeLoop = false,
            nestedSnoozeStartTime = null,
            category = category,
            isStrictSchedulingEnabled = false,
            dateInMillis = null,
            daysOfWeek = emptyList(),
            originalStartTimeInMillis = startTime,
            customAlarmSoundUri = null,
            customAlarmSoundName = null
        )
    }
    
    private fun createCompletionEvent(completedTime: Long): CompletionEventEntity {
        return CompletionEventEntity(
            id = 0,
            reminderId = 1,
            title = "Test Task",
            priority = PriorityLevel.MEDIUM.name,
            category = ReminderCategory.GENERAL.name,
            scheduledTimeInMillis = completedTime - 60000,
            completedTimeInMillis = completedTime,
            completionDelayInMillis = 60000,
            wasNagMode = false,
            nagRepetitionIndex = null,
            nagTotalRepetitions = null,
            dayOfWeek = 1,
            hourOfDay = 14,
            wasRecurring = false,
            recurrenceType = RecurrenceType.ONE_TIME.name
        )
    }
    
    private fun createSuggestionFeedback(timestamp: Long): SuggestionFeedbackEntity {
        return SuggestionFeedbackEntity(
            id = 0,
            suggestionId = 1,
            suggestionType = SuggestionType.OPTIMAL_TIME.name,
            wasAccepted = true,
            feedbackTimestamp = timestamp,
            reminderId = 1,
            confidenceScore = 80
        )
    }
}
