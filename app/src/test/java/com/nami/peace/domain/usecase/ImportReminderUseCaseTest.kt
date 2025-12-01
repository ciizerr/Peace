package com.nami.peace.domain.usecase

import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.RecurrenceType
import com.nami.peace.domain.model.Reminder
import com.nami.peace.domain.model.ReminderCategory
import com.nami.peace.domain.repository.ReminderRepository
import com.nami.peace.scheduler.AlarmScheduler
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for ImportReminderUseCase.
 * 
 * Tests the reminder import logic from deep links.
 * Requirements: 9.3, 9.5
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ImportReminderUseCaseTest {
    
    private lateinit var reminderRepository: FakeReminderRepository
    private lateinit var alarmScheduler: AlarmScheduler
    private lateinit var importReminderUseCase: ImportReminderUseCase
    private lateinit var context: android.content.Context
    
    @Before
    fun setup() {
        context = androidx.test.core.app.ApplicationProvider.getApplicationContext()
        reminderRepository = FakeReminderRepository()
        alarmScheduler = AlarmScheduler(context)
        importReminderUseCase = ImportReminderUseCase(reminderRepository, alarmScheduler)
    }
    
    @Test
    fun `invoke - valid reminder - inserts into database`() = runTest {
        // Arrange
        val reminder = createTestReminder()
        
        // Act
        val reminderId = importReminderUseCase(reminder)
        
        // Assert
        assertTrue(reminderId > 0)
        assertEquals(1, reminderRepository.insertedReminders.size)
    }
    
    @Test
    fun `invoke - future reminder - completes successfully`() = runTest {
        // Arrange
        val futureTime = System.currentTimeMillis() + 3600000 // 1 hour from now
        val reminder = createTestReminder(startTimeInMillis = futureTime)
        
        // Act
        val reminderId = importReminderUseCase(reminder)
        
        // Assert
        assertTrue(reminderId > 0)
        assertEquals(1, reminderRepository.insertedReminders.size)
    }
    
    @Test
    fun `invoke - past reminder - completes successfully`() = runTest {
        // Arrange
        val pastTime = System.currentTimeMillis() - 3600000 // 1 hour ago
        val reminder = createTestReminder(startTimeInMillis = pastTime)
        
        // Act
        val reminderId = importReminderUseCase(reminder)
        
        // Assert
        assertTrue(reminderId > 0)
        assertEquals(1, reminderRepository.insertedReminders.size)
    }
    
    @Test
    fun `invoke - resets runtime state fields`() = runTest {
        // Arrange
        val reminder = createTestReminder(
            id = 42,
            isCompleted = true,
            isEnabled = false,
            currentRepetitionIndex = 3,
            isInNestedSnoozeLoop = true,
            nestedSnoozeStartTime = System.currentTimeMillis()
        )
        
        // Act
        importReminderUseCase(reminder)
        
        // Assert
        val imported = reminderRepository.insertedReminders.first()
        assertEquals(0, imported.id)
        assertEquals(false, imported.isCompleted)
        assertEquals(true, imported.isEnabled)
        assertEquals(0, imported.currentRepetitionIndex)
        assertEquals(false, imported.isInNestedSnoozeLoop)
        assertNull(imported.nestedSnoozeStartTime)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `invoke - blank title - throws exception`() = runTest {
        // Arrange
        val reminder = createTestReminder(title = "   ")
        
        // Act
        importReminderUseCase(reminder)
        
        // Assert: Exception thrown
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `invoke - invalid start time - throws exception`() = runTest {
        // Arrange
        val reminder = createTestReminder(startTimeInMillis = 0)
        
        // Act
        importReminderUseCase(reminder)
        
        // Assert: Exception thrown
    }
    
    @Test
    fun `invoke - preserves all shareable fields`() = runTest {
        // Arrange
        val reminder = createTestReminder(
            title = "Test Reminder",
            priority = PriorityLevel.HIGH,
            category = ReminderCategory.WORK,
            recurrenceType = RecurrenceType.DAILY,
            isNagModeEnabled = true,
            nagIntervalInMillis = 3600000L,
            nagTotalRepetitions = 5,
            isStrictSchedulingEnabled = true,
            customAlarmSoundUri = "content://test",
            customAlarmSoundName = "Test.mp3"
        )
        
        // Act
        importReminderUseCase(reminder)
        
        // Assert
        val imported = reminderRepository.insertedReminders.first()
        assertEquals(reminder.title, imported.title)
        assertEquals(reminder.priority, imported.priority)
        assertEquals(reminder.category, imported.category)
        assertEquals(reminder.recurrenceType, imported.recurrenceType)
        assertEquals(reminder.isNagModeEnabled, imported.isNagModeEnabled)
        assertEquals(reminder.nagIntervalInMillis, imported.nagIntervalInMillis)
        assertEquals(reminder.nagTotalRepetitions, imported.nagTotalRepetitions)
        assertEquals(reminder.isStrictSchedulingEnabled, imported.isStrictSchedulingEnabled)
        assertEquals(reminder.customAlarmSoundUri, imported.customAlarmSoundUri)
        assertEquals(reminder.customAlarmSoundName, imported.customAlarmSoundName)
    }
    
    private fun createTestReminder(
        id: Int = 0,
        title: String = "Test Reminder",
        priority: PriorityLevel = PriorityLevel.MEDIUM,
        category: ReminderCategory = ReminderCategory.GENERAL,
        recurrenceType: RecurrenceType = RecurrenceType.ONE_TIME,
        startTimeInMillis: Long = System.currentTimeMillis() + 3600000,
        isNagModeEnabled: Boolean = false,
        nagIntervalInMillis: Long? = null,
        nagTotalRepetitions: Int = 1,
        currentRepetitionIndex: Int = 0,
        isCompleted: Boolean = false,
        isEnabled: Boolean = true,
        isInNestedSnoozeLoop: Boolean = false,
        nestedSnoozeStartTime: Long? = null,
        isStrictSchedulingEnabled: Boolean = false,
        customAlarmSoundUri: String? = null,
        customAlarmSoundName: String? = null
    ): Reminder {
        return Reminder(
            id = id,
            title = title,
            priority = priority,
            startTimeInMillis = startTimeInMillis,
            recurrenceType = recurrenceType,
            isNagModeEnabled = isNagModeEnabled,
            nagIntervalInMillis = nagIntervalInMillis,
            nagTotalRepetitions = nagTotalRepetitions,
            currentRepetitionIndex = currentRepetitionIndex,
            isCompleted = isCompleted,
            isEnabled = isEnabled,
            isInNestedSnoozeLoop = isInNestedSnoozeLoop,
            nestedSnoozeStartTime = nestedSnoozeStartTime,
            category = category,
            isStrictSchedulingEnabled = isStrictSchedulingEnabled,
            dateInMillis = null,
            daysOfWeek = emptyList(),
            originalStartTimeInMillis = startTimeInMillis,
            customAlarmSoundUri = customAlarmSoundUri,
            customAlarmSoundName = customAlarmSoundName
        )
    }
    
    // Fake implementations for testing
    private class FakeReminderRepository : ReminderRepository {
        val insertedReminders = mutableListOf<Reminder>()
        private var nextId = 1L
        
        override suspend fun insertReminder(reminder: Reminder): Long {
            insertedReminders.add(reminder)
            return nextId++
        }
        
        override fun getReminders() = throw NotImplementedError()
        override suspend fun getReminderById(id: Int) = throw NotImplementedError()
        override suspend fun updateReminder(reminder: Reminder) = throw NotImplementedError()
        override suspend fun deleteReminder(reminder: Reminder) = throw NotImplementedError()
        override suspend fun getActiveReminders(currentTime: Long) = throw NotImplementedError()
        override suspend fun getIncompleteReminders() = throw NotImplementedError()
        override suspend fun setTaskCompleted(id: Int, isCompleted: Boolean) = throw NotImplementedError()
    }
    

}
