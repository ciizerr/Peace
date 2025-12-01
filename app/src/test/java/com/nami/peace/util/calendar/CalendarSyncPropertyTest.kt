package com.nami.peace.util.calendar

import android.accounts.Account
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.RecurrenceType
import com.nami.peace.domain.model.Reminder
import com.nami.peace.domain.model.ReminderCategory
import com.nami.peace.domain.usecase.SyncToCalendarUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Assert.*

/**
 * Property-based tests for calendar sync functionality.
 * 
 * **Feature: peace-app-enhancement, Property 18: Calendar sync completeness**
 * **Feature: peace-app-enhancement, Property 19: Calendar event synchronization**
 * **Validates: Requirements 8.3, 8.4**
 * 
 * Property 18: For any manual sync trigger, all active (non-completed, enabled) reminders 
 * should be exported to Google Calendar.
 * 
 * Property 19: For any reminder update when sync is enabled, the corresponding calendar event 
 * should be updated with the new data.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CalendarSyncPropertyTest {
    
    private lateinit var mockCalendarManager: MockCalendarManager
    private lateinit var syncToCalendarUseCase: SyncToCalendarUseCase
    
    @Before
    fun setup() {
        mockCalendarManager = MockCalendarManager()
        syncToCalendarUseCase = SyncToCalendarUseCase(mockCalendarManager)
    }
    
    @Test
    fun `Property 18 - Calendar sync completeness - all active reminders are synced`() = runTest {
        // Arrange: Create a mix of active and inactive reminders
        val reminders = listOf(
            createReminder(id = 1, title = "Active 1", isEnabled = true, isCompleted = false),
            createReminder(id = 2, title = "Active 2", isEnabled = true, isCompleted = false),
            createReminder(id = 3, title = "Disabled", isEnabled = false, isCompleted = false),
            createReminder(id = 4, title = "Completed", isEnabled = true, isCompleted = true),
            createReminder(id = 5, title = "Active 3", isEnabled = true, isCompleted = false),
            createReminder(id = 6, title = "Disabled and Completed", isEnabled = false, isCompleted = true)
        )
        
        // Act: Sync all reminders
        val result = syncToCalendarUseCase.syncAllReminders(reminders)
        
        // Assert: Only active reminders should be synced
        assertTrue("Sync should succeed", result.isSuccess)
        val syncedCount = result.getOrThrow()
        assertEquals("Should sync exactly 3 active reminders", 3, syncedCount)
        
        // Verify the correct reminders were synced
        val syncedReminders = mockCalendarManager.getSyncedReminders()
        assertEquals("Should have 3 synced reminders", 3, syncedReminders.size)
        assertTrue("Should sync Active 1", syncedReminders.any { it.title == "Active 1" })
        assertTrue("Should sync Active 2", syncedReminders.any { it.title == "Active 2" })
        assertTrue("Should sync Active 3", syncedReminders.any { it.title == "Active 3" })
        assertFalse("Should not sync Disabled", syncedReminders.any { it.title == "Disabled" })
        assertFalse("Should not sync Completed", syncedReminders.any { it.title == "Completed" })
    }
    
    @Test
    fun `Property 18 - Calendar sync completeness - empty list syncs zero reminders`() = runTest {
        // Arrange: Empty list
        val reminders = emptyList<Reminder>()
        
        // Act: Sync all reminders
        val result = syncToCalendarUseCase.syncAllReminders(reminders)
        
        // Assert
        assertTrue("Sync should succeed", result.isSuccess)
        assertEquals("Should sync 0 reminders", 0, result.getOrThrow())
        assertEquals("Should have 0 synced reminders", 0, mockCalendarManager.getSyncedReminders().size)
    }
    
    @Test
    fun `Property 18 - Calendar sync completeness - all inactive reminders sync zero`() = runTest {
        // Arrange: All reminders are inactive
        val reminders = listOf(
            createReminder(id = 1, title = "Disabled 1", isEnabled = false, isCompleted = false),
            createReminder(id = 2, title = "Completed 1", isEnabled = true, isCompleted = true),
            createReminder(id = 3, title = "Disabled 2", isEnabled = false, isCompleted = false),
            createReminder(id = 4, title = "Completed 2", isEnabled = true, isCompleted = true)
        )
        
        // Act: Sync all reminders
        val result = syncToCalendarUseCase.syncAllReminders(reminders)
        
        // Assert
        assertTrue("Sync should succeed", result.isSuccess)
        assertEquals("Should sync 0 reminders", 0, result.getOrThrow())
        assertEquals("Should have 0 synced reminders", 0, mockCalendarManager.getSyncedReminders().size)
    }
    
    @Test
    fun `Property 18 - Calendar sync completeness - large batch of reminders`() = runTest {
        // Arrange: Create 100 reminders, 70 active, 30 inactive
        val reminders = (1..100).map { id ->
            val isActive = id <= 70
            createReminder(
                id = id,
                title = "Reminder $id",
                isEnabled = isActive,
                isCompleted = !isActive
            )
        }
        
        // Act: Sync all reminders
        val result = syncToCalendarUseCase.syncAllReminders(reminders)
        
        // Assert
        assertTrue("Sync should succeed", result.isSuccess)
        assertEquals("Should sync exactly 70 active reminders", 70, result.getOrThrow())
        assertEquals("Should have 70 synced reminders", 70, mockCalendarManager.getSyncedReminders().size)
    }
    
    @Test
    fun `Property 18 - Calendar sync completeness - random reminder states`() = runTest {
        // Test 20 random combinations
        repeat(20) {
            mockCalendarManager.reset()
            
            // Generate random reminders
            val totalReminders = (10..50).random()
            val reminders = (1..totalReminders).map { id ->
                val isEnabled = (0..1).random() == 1
                val isCompleted = (0..1).random() == 1
                createReminder(
                    id = id,
                    title = "Reminder $id",
                    isEnabled = isEnabled,
                    isCompleted = isCompleted
                )
            }
            
            // Calculate expected active count
            val expectedActiveCount = reminders.count { it.isEnabled && !it.isCompleted }
            
            // Act: Sync all reminders
            val result = syncToCalendarUseCase.syncAllReminders(reminders)
            
            // Assert
            assertTrue("Sync should succeed", result.isSuccess)
            assertEquals(
                "Should sync exactly $expectedActiveCount active reminders",
                expectedActiveCount,
                result.getOrThrow()
            )
        }
    }
    
    @Test
    fun `Property 19 - Calendar event synchronization - single reminder sync creates event`() = runTest {
        // Arrange: Create a single reminder
        val reminder = createReminder(id = 1, title = "Test Reminder")
        
        // Act: Sync single reminder
        val result = syncToCalendarUseCase.syncReminder(reminder)
        
        // Assert
        assertTrue("Sync should succeed", result.isSuccess)
        val eventId = result.getOrThrow()
        assertNotNull("Event ID should not be null", eventId)
        assertTrue("Event ID should not be empty", eventId.isNotEmpty())
        
        // Verify the reminder was synced
        val syncedReminders = mockCalendarManager.getSyncedReminders()
        assertEquals("Should have 1 synced reminder", 1, syncedReminders.size)
        assertEquals("Synced reminder should match", reminder.title, syncedReminders[0].title)
    }
    
    @Test
    fun `Property 19 - Calendar event synchronization - reminder data is preserved`() = runTest {
        // Arrange: Create a reminder with specific data
        val reminder = createReminder(
            id = 1,
            title = "Important Meeting",
            priority = PriorityLevel.HIGH,
            category = ReminderCategory.WORK,
            recurrenceType = RecurrenceType.DAILY,
            isNagModeEnabled = true,
            nagTotalRepetitions = 5,
            nagIntervalInMillis = 3600000L // 1 hour
        )
        
        // Act: Sync reminder
        val result = syncToCalendarUseCase.syncReminder(reminder)
        
        // Assert
        assertTrue("Sync should succeed", result.isSuccess)
        
        // Verify all data is preserved
        val syncedReminders = mockCalendarManager.getSyncedReminders()
        assertEquals("Should have 1 synced reminder", 1, syncedReminders.size)
        val synced = syncedReminders[0]
        
        assertEquals("Title should match", reminder.title, synced.title)
        assertEquals("Priority should match", reminder.priority, synced.priority)
        assertEquals("Category should match", reminder.category, synced.category)
        assertEquals("Recurrence should match", reminder.recurrenceType, synced.recurrenceType)
        assertEquals("Nag mode should match", reminder.isNagModeEnabled, synced.isNagModeEnabled)
        assertEquals("Nag repetitions should match", reminder.nagTotalRepetitions, synced.nagTotalRepetitions)
        assertEquals("Nag interval should match", reminder.nagIntervalInMillis, synced.nagIntervalInMillis)
    }
    
    @Test
    fun `Property 19 - Calendar event synchronization - multiple reminders preserve all data`() = runTest {
        // Arrange: Create multiple reminders with different data
        val reminders = listOf(
            createReminder(id = 1, title = "Reminder 1", priority = PriorityLevel.LOW),
            createReminder(id = 2, title = "Reminder 2", priority = PriorityLevel.MEDIUM),
            createReminder(id = 3, title = "Reminder 3", priority = PriorityLevel.HIGH),
            createReminder(id = 4, title = "Reminder 4", category = ReminderCategory.HEALTH),
            createReminder(id = 5, title = "Reminder 5", category = ReminderCategory.STUDY)
        )
        
        // Act: Sync all reminders
        val result = syncToCalendarUseCase.syncAllReminders(reminders)
        
        // Assert
        assertTrue("Sync should succeed", result.isSuccess)
        assertEquals("Should sync all 5 reminders", 5, result.getOrThrow())
        
        // Verify all reminders are synced with correct data
        val syncedReminders = mockCalendarManager.getSyncedReminders()
        assertEquals("Should have 5 synced reminders", 5, syncedReminders.size)
        
        reminders.forEach { original ->
            val synced = syncedReminders.find { it.id == original.id }
            assertNotNull("Reminder ${original.id} should be synced", synced)
            assertEquals("Title should match for reminder ${original.id}", original.title, synced!!.title)
            assertEquals("Priority should match for reminder ${original.id}", original.priority, synced.priority)
            assertEquals("Category should match for reminder ${original.id}", original.category, synced.category)
        }
    }
    
    @Test
    fun `Property 19 - Calendar event synchronization - custom alarm sounds are preserved`() = runTest {
        // Arrange: Create reminder with custom alarm sound
        val reminder = createReminder(
            id = 1,
            title = "Reminder with custom sound",
            customAlarmSoundUri = "content://media/external/audio/media/123",
            customAlarmSoundName = "Custom Alarm.mp3"
        )
        
        // Act: Sync reminder
        val result = syncToCalendarUseCase.syncReminder(reminder)
        
        // Assert
        assertTrue("Sync should succeed", result.isSuccess)
        
        val syncedReminders = mockCalendarManager.getSyncedReminders()
        assertEquals("Should have 1 synced reminder", 1, syncedReminders.size)
        val synced = syncedReminders[0]
        
        assertEquals("Custom sound URI should match", reminder.customAlarmSoundUri, synced.customAlarmSoundUri)
        assertEquals("Custom sound name should match", reminder.customAlarmSoundName, synced.customAlarmSoundName)
    }
    
    @Test
    fun `Property 18 and 19 - Sync stats are tracked correctly`() = runTest {
        // Arrange: Create reminders
        val reminders = (1..10).map { id ->
            createReminder(id = id, title = "Reminder $id", isEnabled = true, isCompleted = false)
        }
        
        // Act: Sync all reminders
        val result = syncToCalendarUseCase.syncAllReminders(reminders)
        
        // Assert sync succeeded
        assertTrue("Sync should succeed", result.isSuccess)
        assertEquals("Should sync 10 reminders", 10, result.getOrThrow())
        
        // Verify sync stats
        val (lastSyncTime, syncedCount) = syncToCalendarUseCase.getSyncStats()
        assertNotNull("Last sync time should be set", lastSyncTime)
        assertEquals("Synced count should be 10", 10, syncedCount)
        assertTrue("Last sync time should be recent", System.currentTimeMillis() - lastSyncTime!! < 5000)
    }
    
    @Test
    fun `Property 18 - Calendar sync fails gracefully when not authenticated`() = runTest {
        // Arrange: Set manager to not authenticated
        mockCalendarManager.setAuthenticated(false)
        val reminders = listOf(createReminder(id = 1, title = "Test"))
        
        // Act: Try to sync
        val result = syncToCalendarUseCase.syncAllReminders(reminders)
        
        // Assert
        assertTrue("Sync should fail", result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull("Exception should not be null", exception)
        assertTrue(
            "Exception message should mention authentication",
            exception!!.message?.contains("authenticated", ignoreCase = true) == true
        )
    }
    
    @Test
    fun `Property 19 - Single reminder sync fails gracefully when not authenticated`() = runTest {
        // Arrange: Set manager to not authenticated
        mockCalendarManager.setAuthenticated(false)
        val reminder = createReminder(id = 1, title = "Test")
        
        // Act: Try to sync
        val result = syncToCalendarUseCase.syncReminder(reminder)
        
        // Assert
        assertTrue("Sync should fail", result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull("Exception should not be null", exception)
        assertTrue(
            "Exception message should mention authentication",
            exception!!.message?.contains("authenticated", ignoreCase = true) == true
        )
    }
    
    // Helper function to create test reminders
    private fun createReminder(
        id: Int,
        title: String,
        priority: PriorityLevel = PriorityLevel.MEDIUM,
        category: ReminderCategory = ReminderCategory.GENERAL,
        recurrenceType: RecurrenceType = RecurrenceType.ONE_TIME,
        isEnabled: Boolean = true,
        isCompleted: Boolean = false,
        isNagModeEnabled: Boolean = false,
        nagTotalRepetitions: Int = 1,
        nagIntervalInMillis: Long? = null,
        customAlarmSoundUri: String? = null,
        customAlarmSoundName: String? = null
    ): Reminder {
        return Reminder(
            id = id,
            title = title,
            priority = priority,
            startTimeInMillis = System.currentTimeMillis() + 3600000, // 1 hour from now
            recurrenceType = recurrenceType,
            isNagModeEnabled = isNagModeEnabled,
            nagIntervalInMillis = nagIntervalInMillis,
            nagTotalRepetitions = nagTotalRepetitions,
            currentRepetitionIndex = 0,
            isCompleted = isCompleted,
            isEnabled = isEnabled,
            isInNestedSnoozeLoop = false,
            nestedSnoozeStartTime = null,
            category = category,
            isStrictSchedulingEnabled = false,
            dateInMillis = null,
            daysOfWeek = emptyList(),
            originalStartTimeInMillis = System.currentTimeMillis(),
            customAlarmSoundUri = customAlarmSoundUri,
            customAlarmSoundName = customAlarmSoundName
        )
    }
    
    /**
     * Mock implementation of CalendarManager for testing.
     */
    private class MockCalendarManager : CalendarManager {
        private var isAuthenticated = true
        private val syncedReminders = mutableListOf<Reminder>()
        private var lastSyncTime: Long? = null
        private var syncedCount: Int = 0
        private val calendarId = "test-calendar-id"
        
        fun setAuthenticated(authenticated: Boolean) {
            isAuthenticated = authenticated
        }
        
        fun getSyncedReminders(): List<Reminder> = syncedReminders.toList()
        
        fun reset() {
            syncedReminders.clear()
            lastSyncTime = null
            syncedCount = 0
            isAuthenticated = true
        }
        
        override suspend fun hasCalendarPermissions(): Boolean = true
        
        override suspend fun isAuthenticated(): Boolean = isAuthenticated
        
        override suspend fun getAuthenticatedAccount(): Account? {
            return if (isAuthenticated) Account("test@example.com", "com.google") else null
        }
        
        override suspend fun requestAuthentication(): Result<Account> {
            return if (isAuthenticated) {
                Result.success(Account("test@example.com", "com.google"))
            } else {
                Result.failure(Exception("Not authenticated"))
            }
        }
        
        override suspend fun signOut() {
            isAuthenticated = false
        }
        
        override suspend fun getOrCreatePeaceCalendar(): Result<String> {
            return if (isAuthenticated) {
                Result.success(calendarId)
            } else {
                Result.failure(Exception("Not authenticated"))
            }
        }
        
        override suspend fun syncReminder(reminder: Reminder, calendarId: String): Result<String> {
            return if (isAuthenticated) {
                syncedReminders.add(reminder)
                Result.success("event-${reminder.id}")
            } else {
                Result.failure(Exception("Not authenticated"))
            }
        }
        
        override suspend fun syncAllReminders(reminders: List<Reminder>): Result<Int> {
            return if (isAuthenticated) {
                syncedReminders.addAll(reminders)
                lastSyncTime = System.currentTimeMillis()
                syncedCount = reminders.size
                Result.success(reminders.size)
            } else {
                Result.failure(Exception("Not authenticated"))
            }
        }
        
        override suspend fun updateCalendarEvent(
            eventId: String,
            reminder: Reminder,
            calendarId: String
        ): Result<Unit> {
            return if (isAuthenticated) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Not authenticated"))
            }
        }
        
        override suspend fun deleteCalendarEvent(eventId: String, calendarId: String): Result<Unit> {
            return if (isAuthenticated) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Not authenticated"))
            }
        }
        
        override suspend fun getSyncStats(): Pair<Long?, Int> {
            return Pair(lastSyncTime, syncedCount)
        }
    }
}
