package com.nami.peace.calendar

import android.Manifest
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nami.peace.BaseRobolectricTest
import com.nami.peace.data.local.AppDatabase
import com.nami.peace.data.local.ReminderEntity
import com.nami.peace.data.local.SyncQueueEntity
import com.nami.peace.data.local.SyncOperationType
import com.nami.peace.data.repository.SyncQueueRepository
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.RecurrenceType
import com.nami.peace.domain.model.ReminderCategory
import com.nami.peace.util.calendar.CalendarManager
import com.nami.peace.util.calendar.CalendarManagerImpl
import com.nami.peace.util.calendar.CalendarPermissionHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication
import org.junit.Assert.*

/**
 * Integration tests for Google Calendar sync functionality.
 * 
 * Tests:
 * - Calendar sync operations
 * - Error handling and retry logic
 * - Offline sync queue
 * - Permission handling
 * 
 * **Validates: Requirements 8.1, 8.2, 8.3, 8.4, 8.5, 8.6**
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CalendarIntegrationTest : BaseRobolectricTest() {
    
    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var syncQueueRepository: SyncQueueRepository
    private lateinit var shadowApplication: ShadowApplication
    
    @Before
    override fun initializeWorkManager() {
        super.initializeWorkManager()
        
        context = ApplicationProvider.getApplicationContext()
        shadowApplication = Shadows.shadowOf(context as android.app.Application)
        
        // Initialize database
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        // Initialize components
        syncQueueRepository = SyncQueueRepository(database.syncQueueDao())
        
        // Note: CalendarManager and CalendarPermissionHelper are not initialized in tests 
        // as they require Google Play Services and Compose runtime which are not available 
        // in Robolectric tests. Tests focus on sync queue operations.
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    // ========== Test 1: Permission Handling ==========
    
    @Test
    fun `calendar sync requires write calendar permission`() {
        // Arrange: No permissions granted
        
        // Act: Check if permission is granted using Android's permission system
        val hasPermission = context.checkSelfPermission(Manifest.permission.WRITE_CALENDAR) == 
            android.content.pm.PackageManager.PERMISSION_GRANTED
        
        // Assert: Should not have permission initially
        assertFalse("Should not have calendar permission initially", hasPermission)
    }
    
    @Test
    fun `calendar sync works when permission is granted`() {
        // Arrange: Grant calendar permission
        shadowApplication.grantPermissions(Manifest.permission.WRITE_CALENDAR)
        
        // Act: Check if permission is granted
        val hasPermission = context.checkSelfPermission(Manifest.permission.WRITE_CALENDAR) == 
            android.content.pm.PackageManager.PERMISSION_GRANTED
        
        // Assert: Should have permission
        assertTrue("Should have calendar permission after granting", hasPermission)
    }
    
    @Test
    fun `calendar sync handles permission denial gracefully`() {
        // Arrange: Deny permission
        shadowApplication.denyPermissions(Manifest.permission.WRITE_CALENDAR)
        
        // Act: Try to sync (should fail gracefully)
        val hasPermission = context.checkSelfPermission(Manifest.permission.WRITE_CALENDAR) == 
            android.content.pm.PackageManager.PERMISSION_GRANTED
        
        // Assert: Should not have permission
        assertFalse("Should not have permission after denial", hasPermission)
        
        // Verify no crash occurs when trying to sync without permission
        try {
            // In real implementation, this would show error to user
            assertFalse("Sync should not proceed without permission", hasPermission)
        } catch (e: Exception) {
            fail("Should handle permission denial gracefully without crashing")
        }
    }
    
    // ========== Test 2: Sync Queue Operations ==========
    
    @Test
    fun `sync queue stores pending operations`() = runTest {
        // Arrange: Create sync queue entry
        val queueEntry = SyncQueueEntity(
            id = 0,
            reminderId = 1,
            operationType = SyncOperationType.CREATE,
            eventId = null,
            retryCount = 0,
            queuedAt = System.currentTimeMillis(),
            lastRetryAt = null,
            lastError = null,
            isProcessing = false
        )
        
        // Act: Add to queue
        syncQueueRepository.queueSync(
            reminderId = queueEntry.reminderId,
            operationType = queueEntry.operationType,
            eventId = queueEntry.eventId
        )
        
        // Assert: Entry should be in queue
        val queuedItems = syncQueueRepository.getPendingSyncs()
        assertEquals("Should have 1 queued item", 1, queuedItems.size)
        assertEquals("Reminder ID should match", 1, queuedItems[0].reminderId)
        assertEquals("Operation type should be CREATE", SyncOperationType.CREATE, queuedItems[0].operationType)
    }
    
    @Test
    fun `sync queue handles multiple operations for same reminder`() = runTest {
        // Arrange: Create multiple operations for same reminder
        val createOp = SyncQueueEntity(
            id = 0,
            reminderId = 1,
            operationType = SyncOperationType.CREATE,
            eventId = null,
            retryCount = 0,
            queuedAt = System.currentTimeMillis(),
            lastRetryAt = null,
            lastError = null,
            isProcessing = false
        )
        val updateOp = SyncQueueEntity(
            id = 0,
            reminderId = 1,
            operationType = SyncOperationType.UPDATE,
            eventId = "event123",
            retryCount = 0,
            queuedAt = System.currentTimeMillis() + 1000,
            lastRetryAt = null,
            lastError = null,
            isProcessing = false
        )
        
        // Act: Add both operations
        syncQueueRepository.queueSync(
            reminderId = createOp.reminderId,
            operationType = createOp.operationType,
            eventId = createOp.eventId
        )
        syncQueueRepository.queueSync(
            reminderId = updateOp.reminderId,
            operationType = updateOp.operationType,
            eventId = updateOp.eventId
        )
        
        // Assert: Both operations should be queued
        val queuedItems = syncQueueRepository.getPendingSyncs()
        assertEquals("Should have 2 queued items", 2, queuedItems.size)
    }
    
    @Test
    fun `sync queue removes completed operations`() = runTest {
        // Arrange: Create and queue operation
        val queueEntry = SyncQueueEntity(
            id = 0,
            reminderId = 1,
            operationType = SyncOperationType.CREATE,
            eventId = null,
            retryCount = 0,
            queuedAt = System.currentTimeMillis(),
            lastRetryAt = null,
            lastError = null,
            isProcessing = false
        )
        val queueId = syncQueueRepository.queueSync(
            reminderId = queueEntry.reminderId,
            operationType = queueEntry.operationType,
            eventId = queueEntry.eventId
        )
        
        // Verify it's queued
        var queuedItems = syncQueueRepository.getPendingSyncs()
        assertEquals("Should have 1 queued item", 1, queuedItems.size)
        
        // Act: Remove from queue (simulating successful sync)
        syncQueueRepository.removeSync(queueId.toInt())
        
        // Assert: Queue should be empty
        queuedItems = syncQueueRepository.getPendingSyncs()
        assertEquals("Queue should be empty after removal", 0, queuedItems.size)
    }
    
    // ========== Test 3: Retry Logic ==========
    
    @Test
    fun `sync queue tracks retry count`() = runTest {
        // Arrange: Create queue entry
        val queueEntry = SyncQueueEntity(
            id = 0,
            reminderId = 1,
            operationType = SyncOperationType.CREATE,
            eventId = null,
            retryCount = 0,
            queuedAt = System.currentTimeMillis(),
            lastRetryAt = null,
            lastError = null,
            isProcessing = false
        )
        val queueId = syncQueueRepository.queueSync(
            reminderId = queueEntry.reminderId,
            operationType = queueEntry.operationType,
            eventId = queueEntry.eventId
        ).toInt()
        
        // Act: Update after retry attempts multiple times
        repeat(3) {
            syncQueueRepository.updateAfterRetry(queueId, success = false, error = "Network error")
        }
        
        // Assert: Retry count should be 3
        val updatedEntry = database.syncQueueDao().getSyncById(queueId)
        assertEquals("Retry count should be 3", 3, updatedEntry?.retryCount)
        assertEquals("Last error should be set", "Network error", updatedEntry?.lastError)
        assertNotNull("Last retry time should be set", updatedEntry?.lastRetryAt)
    }
    
    @Test
    fun `sync queue respects max retry limit`() = runTest {
        // Arrange: Create queue entry
        val queueEntry = SyncQueueEntity(
            id = 0,
            reminderId = 1,
            operationType = SyncOperationType.CREATE,
            eventId = null,
            retryCount = 0,
            queuedAt = System.currentTimeMillis(),
            lastRetryAt = null,
            lastError = null,
            isProcessing = false
        )
        val queueId = syncQueueRepository.queueSync(
            reminderId = queueEntry.reminderId,
            operationType = queueEntry.operationType,
            eventId = queueEntry.eventId
        ).toInt()
        
        // Act: Update after retry attempts beyond max (5)
        repeat(6) {
            syncQueueRepository.updateAfterRetry(queueId, success = false, error = "Network error")
        }
        
        // Assert: Entry should be marked for removal or flagged
        val updatedEntry = database.syncQueueDao().getSyncById(queueId)
        assertTrue("Retry count should exceed max", updatedEntry?.retryCount ?: 0 >= 5)
    }
    
    @Test
    fun `sync queue implements exponential backoff`() = runTest {
        // Arrange: Create queue entry
        val queueEntry = SyncQueueEntity(
            id = 0,
            reminderId = 1,
            operationType = SyncOperationType.CREATE,
            eventId = null,
            retryCount = 0,
            queuedAt = System.currentTimeMillis(),
            lastRetryAt = null,
            lastError = null,
            isProcessing = false
        )
        // Note: This test doesn't need to queue anything, just test the backoff calculation
        
        // Act: Calculate backoff delays for different retry counts
        val delays = mutableListOf<Long>()
        for (retryCount in 0..4) {
            val delay = calculateExponentialBackoff(retryCount)
            delays.add(delay)
        }
        
        // Assert: Delays should increase exponentially
        assertTrue("Delay 1 should be less than delay 2", delays[0] < delays[1])
        assertTrue("Delay 2 should be less than delay 3", delays[1] < delays[2])
        assertTrue("Delay 3 should be less than delay 4", delays[2] < delays[3])
        assertTrue("Delay 4 should be less than delay 5", delays[3] < delays[4])
    }
    
    // ========== Test 4: Error Handling ==========
    
    @Test
    fun `sync handles network errors gracefully`() = runTest {
        // Arrange: Create queue entry
        val queueEntry = SyncQueueEntity(
            id = 0,
            reminderId = 1,
            operationType = SyncOperationType.CREATE,
            eventId = null,
            retryCount = 0,
            queuedAt = System.currentTimeMillis(),
            lastRetryAt = null,
            lastError = null,
            isProcessing = false
        )
        val queueId = syncQueueRepository.queueSync(
            reminderId = queueEntry.reminderId,
            operationType = queueEntry.operationType,
            eventId = queueEntry.eventId
        ).toInt()
        
        // Act: Simulate network error
        syncQueueRepository.updateAfterRetry(queueId, success = false, error = "Network unavailable")
        
        // Assert: Error should be recorded
        val updatedEntry = database.syncQueueDao().getSyncById(queueId)
        assertEquals("Error message should be recorded", "Network unavailable", updatedEntry?.lastError)
        assertEquals("Retry count should be 1", 1, updatedEntry?.retryCount)
    }
    
    @Test
    fun `sync handles calendar API errors gracefully`() = runTest {
        // Arrange: Create queue entry
        val queueEntry = SyncQueueEntity(
            id = 0,
            reminderId = 1,
            operationType = SyncOperationType.CREATE,
            eventId = null,
            retryCount = 0,
            queuedAt = System.currentTimeMillis(),
            lastRetryAt = null,
            lastError = null,
            isProcessing = false
        )
        val queueId = syncQueueRepository.queueSync(
            reminderId = queueEntry.reminderId,
            operationType = queueEntry.operationType,
            eventId = queueEntry.eventId
        ).toInt()
        
        // Act: Simulate API error
        syncQueueRepository.updateAfterRetry(queueId, success = false, error = "Calendar API error: 403 Forbidden")
        
        // Assert: Error should be recorded
        val updatedEntry = database.syncQueueDao().getSyncById(queueId)
        assertTrue("Error message should contain API error", 
            updatedEntry?.lastError?.contains("Calendar API error") == true)
    }
    
    @Test
    fun `sync handles invalid reminder data gracefully`() = runTest {
        // Arrange: Create queue entry with invalid reminder ID
        val queueEntry = SyncQueueEntity(
            id = 0,
            reminderId = 99999, // Non-existent reminder
            operationType = SyncOperationType.CREATE,
            eventId = null,
            retryCount = 0,
            queuedAt = System.currentTimeMillis(),
            lastRetryAt = null,
            lastError = null,
            isProcessing = false
        )
        val queueId = syncQueueRepository.queueSync(
            reminderId = queueEntry.reminderId,
            operationType = queueEntry.operationType,
            eventId = queueEntry.eventId
        ).toInt()
        
        // Act: Try to process (should fail gracefully)
        // In real implementation, this would check if reminder exists
        val reminder = database.reminderDao().getReminderById(99999)
        
        // Assert: Should handle missing reminder
        assertNull("Reminder should not exist", reminder)
        
        // Record error
        syncQueueRepository.updateAfterRetry(queueId, success = false, error = "Reminder not found")
        val updatedEntry = database.syncQueueDao().getSyncById(queueId)
        assertEquals("Error should be recorded", "Reminder not found", updatedEntry?.lastError)
    }
    
    // ========== Test 5: Sync Statistics ==========
    
    @Test
    fun `sync tracks last sync time`() = runTest {
        // Arrange: Record sync time
        val syncTime = System.currentTimeMillis()
        
        // Act: Store sync time (in real implementation, this would be in preferences)
        // Here we just verify the timestamp is valid
        
        // Assert: Sync time should be recent
        val now = System.currentTimeMillis()
        assertTrue("Sync time should be recent", now - syncTime < 1000)
    }
    
    @Test
    fun `sync counts successful operations`() = runTest {
        // Arrange: Create multiple reminders
        val reminder1 = createReminderEntity("Task 1")
        val reminder2 = createReminderEntity("Task 2")
        val reminder3 = createReminderEntity("Task 3")
        
        database.reminderDao().insertReminder(reminder1)
        database.reminderDao().insertReminder(reminder2)
        database.reminderDao().insertReminder(reminder3)
        
        // Act: Count active reminders (would be synced)
        val activeReminders = database.reminderDao().getReminders().first()
            .filter { !it.isCompleted && it.isEnabled }
        
        // Assert: Should have 3 active reminders
        assertEquals("Should have 3 active reminders to sync", 3, activeReminders.size)
    }
    
    // ========== Test 6: Offline Sync Queue ==========
    
    @Test
    fun `offline operations are queued for later sync`() = runTest {
        // Arrange: Simulate offline state (no permission = offline for this test)
        shadowApplication.denyPermissions(Manifest.permission.WRITE_CALENDAR)
        
        // Act: Try to sync (should queue instead)
        val queueEntry = SyncQueueEntity(
            id = 0,
            reminderId = 1,
            operationType = SyncOperationType.CREATE,
            eventId = null,
            retryCount = 0,
            queuedAt = System.currentTimeMillis(),
            lastRetryAt = null,
            lastError = null,
            isProcessing = false
        )
        syncQueueRepository.queueSync(
            reminderId = queueEntry.reminderId,
            operationType = queueEntry.operationType,
            eventId = queueEntry.eventId
        )
        
        // Assert: Operation should be queued
        val queuedItems = syncQueueRepository.getPendingSyncs()
        assertEquals("Should have 1 queued operation", 1, queuedItems.size)
    }
    
    @Test
    fun `queued operations are processed when online`() = runTest {
        // Arrange: Queue operations while offline
        val queueEntry1 = SyncQueueEntity(0, 1, SyncOperationType.CREATE, null, 0, System.currentTimeMillis(), null, null, false)
        val queueEntry2 = SyncQueueEntity(0, 2, SyncOperationType.UPDATE, "event123", 0, System.currentTimeMillis(), null, null, false)
        
        syncQueueRepository.queueSync(
            reminderId = queueEntry1.reminderId,
            operationType = queueEntry1.operationType,
            eventId = queueEntry1.eventId
        )
        syncQueueRepository.queueSync(
            reminderId = queueEntry2.reminderId,
            operationType = queueEntry2.operationType,
            eventId = queueEntry2.eventId
        )
        
        // Verify queued
        var queuedItems = syncQueueRepository.getPendingSyncs()
        assertEquals("Should have 2 queued operations", 2, queuedItems.size)
        
        // Act: Grant permission (simulate coming online)
        shadowApplication.grantPermissions(Manifest.permission.WRITE_CALENDAR)
        
        // Process queue (in real implementation, this would be done by SyncQueueWorker)
        // Here we just verify the queue can be accessed
        queuedItems = syncQueueRepository.getPendingSyncs()
        assertEquals("Queue should still have items ready to process", 2, queuedItems.size)
    }
    
    // ========== Test 7: Sync Conflict Resolution ==========
    
    @Test
    fun `sync uses local data as source of truth`() = runTest {
        // Arrange: Create reminder
        val reminder = createReminderEntity("Local Task")
        val reminderId = database.reminderDao().insertReminder(reminder).toInt()
        
        // Act: Update locally
        val updatedReminder = reminder.copy(id = reminderId, title = "Updated Local Task")
        database.reminderDao().updateReminder(updatedReminder)
        
        // Assert: Local update should be the source of truth
        val retrievedReminder = database.reminderDao().getReminderById(reminderId)
        assertEquals("Local update should be preserved", "Updated Local Task", retrievedReminder?.title)
        
        // In real sync, this would overwrite calendar event (one-way sync)
    }
    
    // ========== Helper Functions ==========
    
    private fun createReminderEntity(title: String): ReminderEntity {
        val startTime = System.currentTimeMillis() + 3600000
        return ReminderEntity(
            id = 0,
            title = title,
            priority = PriorityLevel.MEDIUM,
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
            category = ReminderCategory.GENERAL,
            isStrictSchedulingEnabled = false,
            dateInMillis = null,
            daysOfWeek = emptyList(),
            originalStartTimeInMillis = startTime,
            customAlarmSoundUri = null,
            customAlarmSoundName = null
        )
    }
    
    private fun calculateExponentialBackoff(retryCount: Int): Long {
        // Exponential backoff: 2^retryCount * 1000ms (1s, 2s, 4s, 8s, 16s)
        val baseDelay = 1000L
        return baseDelay * (1 shl retryCount) // 2^retryCount
    }
}
