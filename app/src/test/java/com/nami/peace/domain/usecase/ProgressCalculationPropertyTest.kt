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
 * Feature: peace-app-enhancement, Property 8 & 9: Progress calculation accuracy
 * 
 * Property 8: For any reminder with subtasks, the progress percentage should equal 
 * (completed subtasks / total subtasks) * 100.
 * 
 * Property 9: For any subtask deletion, the parent reminder's progress bar should 
 * recalculate immediately to reflect the new completion percentage.
 * 
 * Validates: Requirements 4.3, 4.5
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ProgressCalculationPropertyTest {
    
    private lateinit var database: AppDatabase
    private lateinit var reminderDao: ReminderDao
    private lateinit var subtaskDao: SubtaskDao
    private lateinit var subtaskRepository: SubtaskRepositoryImpl
    private lateinit var calculateProgressUseCase: CalculateProgressUseCase
    private lateinit var deleteSubtaskUseCase: DeleteSubtaskUseCase
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        reminderDao = database.reminderDao()
        subtaskDao = database.subtaskDao()
        subtaskRepository = SubtaskRepositoryImpl(subtaskDao)
        calculateProgressUseCase = CalculateProgressUseCase(subtaskRepository)
        deleteSubtaskUseCase = DeleteSubtaskUseCase(subtaskRepository)
    }
    
    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `Property 8 - Progress calculation accuracy for any reminder with subtasks`() = runBlocking {
        repeat(100) {
            val reminderEntity = generateRandomReminder()
            val reminderId = reminderDao.insertReminder(reminderEntity).toInt()
            
            val totalSubtasks = Random.nextInt(1, 21)
            val completedCount = Random.nextInt(0, totalSubtasks + 1)
            
            repeat(totalSubtasks) { index ->
                val isCompleted = index < completedCount
                val subtaskEntity = SubtaskEntity(
                    reminderId = reminderId,
                    title = generateRandomString(3, 50),
                    isCompleted = isCompleted,
                    order = index,
                    createdAt = System.currentTimeMillis()
                )
                subtaskDao.insert(subtaskEntity)
            }
            
            val expectedProgress = (completedCount * 100) / totalSubtasks
            val actualProgress = calculateProgressUseCase.calculateProgress(reminderId)
            
            assertEquals(
                "Progress should be (completed / total) * 100. Expected: $expectedProgress, Actual: $actualProgress",
                expectedProgress,
                actualProgress
            )
            
            database.clearAllTables()
        }
    }

    @Test
    fun `Property 8 - Progress is 0 when no subtasks exist`() = runBlocking {
        val reminderEntity = generateRandomReminder()
        val reminderId = reminderDao.insertReminder(reminderEntity).toInt()
        
        val progress = calculateProgressUseCase.calculateProgress(reminderId)
        
        assertEquals("Progress should be 0 when no subtasks exist", 0, progress)
    }
    
    @Test
    fun `Property 8 - Progress is 100 when all subtasks are completed`() = runBlocking {
        repeat(50) {
            val reminderEntity = generateRandomReminder()
            val reminderId = reminderDao.insertReminder(reminderEntity).toInt()
            
            val totalSubtasks = Random.nextInt(1, 21)
            
            repeat(totalSubtasks) { index ->
                val subtaskEntity = SubtaskEntity(
                    reminderId = reminderId,
                    title = generateRandomString(3, 50),
                    isCompleted = true,
                    order = index,
                    createdAt = System.currentTimeMillis()
                )
                subtaskDao.insert(subtaskEntity)
            }
            
            val progress = calculateProgressUseCase.calculateProgress(reminderId)
            
            assertEquals("Progress should be 100 when all subtasks are completed", 100, progress)
            
            database.clearAllTables()
        }
    }

    @Test
    fun `Property 9 - Progress recalculates after subtask deletion`() = runBlocking {
        repeat(100) {
            val reminderEntity = generateRandomReminder()
            val reminderId = reminderDao.insertReminder(reminderEntity).toInt()
            
            val totalSubtasks = Random.nextInt(2, 21)
            val completedCount = Random.nextInt(0, totalSubtasks + 1)
            
            val subtasks = mutableListOf<Subtask>()
            
            repeat(totalSubtasks) { index ->
                val isCompleted = index < completedCount
                val subtaskEntity = SubtaskEntity(
                    reminderId = reminderId,
                    title = generateRandomString(3, 50),
                    isCompleted = isCompleted,
                    order = index,
                    createdAt = System.currentTimeMillis()
                )
                val id = subtaskDao.insert(subtaskEntity).toInt()
                subtasks.add(Subtask.fromEntity(subtaskEntity.copy(id = id)))
            }
            
            val initialProgress = calculateProgressUseCase.calculateProgress(reminderId)
            
            val subtaskToDelete = subtasks.random()
            deleteSubtaskUseCase(subtaskToDelete)
            
            val newTotal = totalSubtasks - 1
            val newCompleted = if (subtaskToDelete.isCompleted) completedCount - 1 else completedCount
            val expectedNewProgress = if (newTotal > 0) (newCompleted * 100) / newTotal else 0
            
            val actualNewProgress = calculateProgressUseCase.calculateProgress(reminderId)
            
            assertEquals(
                "Progress should recalculate after deletion. Expected: $expectedNewProgress, Actual: $actualNewProgress",
                expectedNewProgress,
                actualNewProgress
            )
            
            if (newTotal > 0 && totalSubtasks > 0) {
                val expectedInitialProgress = (completedCount * 100) / totalSubtasks
                assertEquals("Initial progress should match expected", expectedInitialProgress, initialProgress)
            }
            
            database.clearAllTables()
        }
    }

    @Test
    fun `Property 9 - Progress updates via Flow after deletion`() = runBlocking {
        val reminderEntity = generateRandomReminder()
        val reminderId = reminderDao.insertReminder(reminderEntity).toInt()
        
        val subtasks = mutableListOf<Subtask>()
        repeat(10) { index ->
            val isCompleted = index < 5
            val subtaskEntity = SubtaskEntity(
                reminderId = reminderId,
                title = "Subtask $index",
                isCompleted = isCompleted,
                order = index,
                createdAt = System.currentTimeMillis()
            )
            val id = subtaskDao.insert(subtaskEntity).toInt()
            subtasks.add(Subtask.fromEntity(subtaskEntity.copy(id = id)))
        }
        
        val initialProgress = calculateProgressUseCase.observeProgress(reminderId).first()
        assertEquals("Initial progress should be 50%", 50, initialProgress)
        
        val completedSubtask = subtasks.first { it.isCompleted }
        deleteSubtaskUseCase(completedSubtask)
        
        val updatedProgress = calculateProgressUseCase.observeProgress(reminderId).first()
        
        val expectedProgress = (4 * 100) / 9
        assertEquals("Progress should update via Flow after deletion", expectedProgress, updatedProgress)
    }

    @Test
    fun `Property 8 - observeProgress emits correct values`() = runBlocking {
        repeat(50) {
            val reminderEntity = generateRandomReminder()
            val reminderId = reminderDao.insertReminder(reminderEntity).toInt()
            
            val totalSubtasks = Random.nextInt(1, 21)
            val completedCount = Random.nextInt(0, totalSubtasks + 1)
            
            repeat(totalSubtasks) { index ->
                val isCompleted = index < completedCount
                val subtaskEntity = SubtaskEntity(
                    reminderId = reminderId,
                    title = generateRandomString(3, 50),
                    isCompleted = isCompleted,
                    order = index,
                    createdAt = System.currentTimeMillis()
                )
                subtaskDao.insert(subtaskEntity)
            }
            
            val flowProgress = calculateProgressUseCase.observeProgress(reminderId).first()
            val directProgress = calculateProgressUseCase.calculateProgress(reminderId)
            
            assertEquals(
                "Flow progress should match direct calculation",
                directProgress,
                flowProgress
            )
            
            database.clearAllTables()
        }
    }
    
    private fun generateRandomReminder(): ReminderEntity {
        val currentTime = System.currentTimeMillis()
        val hasNagMode = Random.nextBoolean()
        return ReminderEntity(
            id = 0,
            title = generateRandomString(5, 50),
            priority = PriorityLevel.entries.random(),
            startTimeInMillis = currentTime + Random.nextLong(0, 86400000),
            recurrenceType = RecurrenceType.entries.random(),
            isNagModeEnabled = hasNagMode,
            nagIntervalInMillis = if (hasNagMode) Random.nextLong(60000, 3600000) else null,
            nagTotalRepetitions = Random.nextInt(1, 10),
            currentRepetitionIndex = 0,
            isCompleted = false,
            isEnabled = true,
            isInNestedSnoozeLoop = false,
            nestedSnoozeStartTime = null,
            category = ReminderCategory.entries.random(),
            isStrictSchedulingEnabled = Random.nextBoolean(),
            dateInMillis = if (Random.nextBoolean()) currentTime + Random.nextLong(0, 86400000) else null,
            daysOfWeek = List(Random.nextInt(0, 7)) { Random.nextInt(1, 8) },
            originalStartTimeInMillis = currentTime
        )
    }
    
    private fun generateRandomString(minLength: Int, maxLength: Int): String {
        val length = Random.nextInt(minLength, maxLength + 1)
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 "
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }
}
