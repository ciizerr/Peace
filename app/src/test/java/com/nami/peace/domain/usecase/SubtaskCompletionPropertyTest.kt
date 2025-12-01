package com.nami.peace.domain.usecase

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nami.peace.data.local.AppDatabase
import com.nami.peace.data.local.ReminderDao
import com.nami.peace.data.local.ReminderEntity
import com.nami.peace.data.local.SubtaskDao
import com.nami.peace.data.local.SubtaskEntity
import com.nami.peace.data.repository.SubtaskRepositoryImpl
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.RecurrenceType
import com.nami.peace.domain.model.ReminderCategory
import com.nami.peace.domain.model.Subtask
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.random.Random

/**
 * Feature: peace-app-enhancement, Property 7: Subtask completion state update
 * 
 * Property: For any subtask checkbox interaction, the subtask's completion state 
 * should toggle immediately.
 * 
 * Validates: Requirements 4.2
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SubtaskCompletionPropertyTest {
    
    private lateinit var database: AppDatabase
    private lateinit var reminderDao: ReminderDao
    private lateinit var subtaskDao: SubtaskDao
    private lateinit var subtaskRepository: SubtaskRepositoryImpl
    private lateinit var updateSubtaskUseCase: UpdateSubtaskUseCase
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        reminderDao = database.reminderDao()
        subtaskDao = database.subtaskDao()
        subtaskRepository = SubtaskRepositoryImpl(subtaskDao)
        updateSubtaskUseCase = UpdateSubtaskUseCase(subtaskRepository)
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun `Property 7 - Subtask completion state toggles immediately on checkbox interaction`() = runBlocking {
        // Run 100 iterations with random data
        repeat(100) {
            // Create a random reminder
            val reminderEntity = generateRandomReminder()
            val reminderId = reminderDao.insertReminder(reminderEntity).toInt()
            
            // Create a random subtask with random initial completion state
            val initialCompletionState = Random.nextBoolean()
            val subtaskEntity = SubtaskEntity(
                reminderId = reminderId,
                title = generateRandomString(3, 100),
                isCompleted = initialCompletionState,
                order = Random.nextInt(0, 100),
                createdAt = System.currentTimeMillis()
            )
            
            val subtaskId = subtaskDao.insert(subtaskEntity).toInt()
            
            // Convert to domain model
            val subtask = Subtask(
                id = subtaskId,
                reminderId = reminderId,
                title = subtaskEntity.title,
                isCompleted = initialCompletionState,
                order = subtaskEntity.order,
                createdAt = subtaskEntity.createdAt
            )
            
            // Toggle completion using the use case
            updateSubtaskUseCase.toggleCompletion(subtask)
            
            // Verify the completion state has toggled
            val updatedSubtasks = subtaskDao.getSubtasksForReminder(reminderId).first()
            assertEquals(1, updatedSubtasks.size)
            
            val updatedSubtask = updatedSubtasks[0]
            assertEquals(!initialCompletionState, updatedSubtask.isCompleted)
            
            // Toggle again to verify it toggles back
            val subtaskAfterFirstToggle = Subtask.fromEntity(updatedSubtask)
            updateSubtaskUseCase.toggleCompletion(subtaskAfterFirstToggle)
            
            // Verify it's back to the original state
            val finalSubtasks = subtaskDao.getSubtasksForReminder(reminderId).first()
            assertEquals(1, finalSubtasks.size)
            assertEquals(initialCompletionState, finalSubtasks[0].isCompleted)
            
            // Clean up
            reminderDao.deleteReminder(reminderEntity.copy(id = reminderId))
        }
    }
    
    @Test
    fun `Property 7 - Multiple subtasks can be toggled independently`() = runBlocking {
        // Run 50 iterations with random data
        repeat(50) {
            // Create a random reminder
            val reminderEntity = generateRandomReminder()
            val reminderId = reminderDao.insertReminder(reminderEntity).toInt()
            
            // Create multiple subtasks with random initial states
            val subtaskCount = Random.nextInt(2, 10)
            val initialStates = List(subtaskCount) { Random.nextBoolean() }
            val subtaskIds = mutableListOf<Int>()
            
            initialStates.forEachIndexed { index, isCompleted ->
                val subtaskEntity = SubtaskEntity(
                    reminderId = reminderId,
                    title = generateRandomString(3, 50),
                    isCompleted = isCompleted,
                    order = index,
                    createdAt = System.currentTimeMillis()
                )
                val id = subtaskDao.insert(subtaskEntity).toInt()
                subtaskIds.add(id)
            }
            
            // Pick a random subtask to toggle
            val indexToToggle = Random.nextInt(0, subtaskCount)
            val subtasksBeforeToggle = subtaskDao.getSubtasksForReminder(reminderId).first()
            val subtaskToToggle = Subtask.fromEntity(subtasksBeforeToggle[indexToToggle])
            
            // Toggle the selected subtask
            updateSubtaskUseCase.toggleCompletion(subtaskToToggle)
            
            // Verify only the selected subtask changed
            val subtasksAfterToggle = subtaskDao.getSubtasksForReminder(reminderId).first()
            assertEquals(subtaskCount, subtasksAfterToggle.size)
            
            subtasksAfterToggle.forEachIndexed { index, subtask ->
                if (index == indexToToggle) {
                    // This one should be toggled
                    assertEquals(!initialStates[index], subtask.isCompleted)
                } else {
                    // All others should remain unchanged
                    assertEquals(initialStates[index], subtask.isCompleted)
                }
            }
            
            // Clean up
            reminderDao.deleteReminder(reminderEntity.copy(id = reminderId))
        }
    }
    
    @Test
    fun `Property 7 - Completion state persists across reads`() = runBlocking {
        // Run 100 iterations
        repeat(100) {
            // Create a random reminder
            val reminderEntity = generateRandomReminder()
            val reminderId = reminderDao.insertReminder(reminderEntity).toInt()
            
            // Create a subtask
            val subtaskEntity = SubtaskEntity(
                reminderId = reminderId,
                title = generateRandomString(3, 50),
                isCompleted = false,
                order = 0,
                createdAt = System.currentTimeMillis()
            )
            val subtaskId = subtaskDao.insert(subtaskEntity).toInt()
            
            // Toggle it to completed
            val subtask = Subtask(
                id = subtaskId,
                reminderId = reminderId,
                title = subtaskEntity.title,
                isCompleted = false,
                order = 0,
                createdAt = subtaskEntity.createdAt
            )
            updateSubtaskUseCase.toggleCompletion(subtask)
            
            // Read it multiple times and verify it stays completed
            repeat(5) {
                val subtasks = subtaskDao.getSubtasksForReminder(reminderId).first()
                assertEquals(1, subtasks.size)
                assertTrue("Completion state should persist across reads", subtasks[0].isCompleted)
            }
            
            // Clean up
            reminderDao.deleteReminder(reminderEntity.copy(id = reminderId))
        }
    }
    
    // Helper functions to generate random test data
    private fun generateRandomReminder(): ReminderEntity {
        val currentTime = System.currentTimeMillis()
        val hasNagMode = Random.nextBoolean()
        return ReminderEntity(
            id = 0,
            title = generateRandomString(5, 50),
            priority = PriorityLevel.values().random(),
            startTimeInMillis = currentTime + Random.nextLong(0, 86400000),
            recurrenceType = RecurrenceType.values().random(),
            isNagModeEnabled = hasNagMode,
            nagIntervalInMillis = if (hasNagMode) Random.nextLong(60000, 3600000) else null,
            nagTotalRepetitions = Random.nextInt(1, 10),
            currentRepetitionIndex = 0,
            isCompleted = false,
            isEnabled = true,
            isInNestedSnoozeLoop = false,
            nestedSnoozeStartTime = null,
            category = ReminderCategory.values().random(),
            isStrictSchedulingEnabled = Random.nextBoolean(),
            dateInMillis = if (Random.nextBoolean()) currentTime + Random.nextLong(0, 86400000) else null,
            daysOfWeek = List(Random.nextInt(0, 7)) { Random.nextInt(1, 8) },
            originalStartTimeInMillis = currentTime
        )
    }
    
    private fun generateRandomString(minLength: Int, maxLength: Int): String {
        val length = Random.nextInt(minLength, maxLength + 1)
        val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9') + ' '
        return (1..length).map { chars.random() }.joinToString("")
    }
}
