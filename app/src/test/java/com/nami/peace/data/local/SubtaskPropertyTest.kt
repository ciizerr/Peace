package com.nami.peace.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.RecurrenceType
import com.nami.peace.domain.model.ReminderCategory
import kotlin.random.Random
import org.junit.Assert.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Feature: peace-app-enhancement, Property 6: Subtask-reminder linkage
 * 
 * Property: For any subtask created, it should be linked to exactly one parent reminder 
 * via foreign key relationship.
 * 
 * Validates: Requirements 4.1
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SubtaskPropertyTest {
    
    private lateinit var database: AppDatabase
    private lateinit var reminderDao: ReminderDao
    private lateinit var subtaskDao: SubtaskDao
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        reminderDao = database.reminderDao()
        subtaskDao = database.subtaskDao()
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun `Property 6 - Subtask-reminder linkage - every subtask must be linked to exactly one parent reminder`() = runBlocking {
        // Run 100 iterations with random data
        repeat(100) {
            val reminderEntity = generateRandomReminder()
            val subtaskTitle = generateRandomString(3, 100)
            val order = Random.nextInt(0, 100)
            
            // Insert a reminder first
            val reminderId = reminderDao.insertReminder(reminderEntity).toInt()
            
            // Create a subtask linked to this reminder
            val subtask = SubtaskEntity(
                reminderId = reminderId,
                title = subtaskTitle,
                isCompleted = false,
                order = order,
                createdAt = System.currentTimeMillis()
            )
            
            // Insert the subtask
            val subtaskId = subtaskDao.insert(subtask)
            
            // Verify the subtask exists and is linked to the reminder
            assertNotEquals(0L, subtaskId)
            
            // Retrieve subtasks for this reminder
            val subtasks = subtaskDao.getSubtasksForReminder(reminderId).first()
            
            // Verify exactly one subtask is linked to this reminder
            assertEquals(1, subtasks.size)
            assertEquals(reminderId, subtasks[0].reminderId)
            assertEquals(subtaskTitle, subtasks[0].title)
            
            // Clean up for next iteration
            reminderDao.deleteReminder(reminderEntity.copy(id = reminderId))
        }
    }
    
    @Test
    fun `Property 6 - Cascade delete - when a reminder is deleted all its subtasks should be deleted`() = runBlocking {
        // Run 50 iterations with random data
        repeat(50) {
            val reminderEntity = generateRandomReminder()
            val subtaskCount = Random.nextInt(1, 10)
            val subtaskTitles = List(subtaskCount) { generateRandomString(3, 50) }
            
            // Insert a reminder
            val reminderId = reminderDao.insertReminder(reminderEntity).toInt()
            
            // Insert multiple subtasks for this reminder
            subtaskTitles.forEachIndexed { index, title ->
                val subtask = SubtaskEntity(
                    reminderId = reminderId,
                    title = title,
                    isCompleted = false,
                    order = index,
                    createdAt = System.currentTimeMillis()
                )
                subtaskDao.insert(subtask)
            }
            
            // Verify subtasks exist
            val subtasksBeforeDelete = subtaskDao.getSubtasksForReminder(reminderId).first()
            assertEquals(subtaskTitles.size, subtasksBeforeDelete.size)
            
            // Delete the reminder (should cascade delete subtasks)
            reminderDao.deleteReminder(reminderEntity.copy(id = reminderId))
            
            // Verify all subtasks are deleted
            val subtasksAfterDelete = subtaskDao.getSubtasksForReminder(reminderId).first()
            assertEquals(0, subtasksAfterDelete.size)
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
