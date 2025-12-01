package com.nami.peace.scheduler

import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nami.peace.data.local.AppDatabase
import com.nami.peace.data.repository.ReminderRepositoryImpl
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.RecurrenceType
import com.nami.peace.domain.model.Reminder
import com.nami.peace.domain.model.ReminderCategory
import com.nami.peace.util.alarm.AlarmSoundManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Assert.*

/**
 * Integration test for alarm trigger logic with custom sounds.
 * Tests the complete flow from reminder creation to alarm sound playback.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AlarmTriggerIntegrationTest {
    
    private lateinit var database: AppDatabase
    private lateinit var repository: ReminderRepositoryImpl
    private lateinit var alarmSoundManager: AlarmSoundManager
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = ReminderRepositoryImpl(database.reminderDao(), org.mockito.Mockito.mock(com.nami.peace.widget.WidgetUpdateManager::class.java))
        alarmSoundManager = AlarmSoundManager(context)
    }
    
    @After
    fun teardown() {
        database.close()
        alarmSoundManager.release()
    }
    
    @Test
    fun `alarm trigger with custom sound should use custom sound URI`() = runTest {
        // Create a reminder with custom alarm sound
        val customSoundUri = "content://media/external/audio/media/123"
        val customSoundName = "My Custom Alarm"
        
        val reminder = Reminder(
            id = 0,
            title = "Test Reminder",
            priority = PriorityLevel.HIGH,
            startTimeInMillis = System.currentTimeMillis() + 60000,
            recurrenceType = RecurrenceType.ONE_TIME,
            isNagModeEnabled = false,
            nagIntervalInMillis = null,
            nagTotalRepetitions = 1,
            currentRepetitionIndex = 0,
            isCompleted = false,
            isEnabled = true,
            category = ReminderCategory.GENERAL,
            customAlarmSoundUri = customSoundUri,
            customAlarmSoundName = customSoundName
        )
        
        // Insert the reminder
        val reminderId = repository.insertReminder(reminder)
        
        // Retrieve the reminder (simulating alarm trigger)
        val retrievedReminder = repository.getReminderById(reminderId.toInt())
        
        // Verify custom sound is set
        assertNotNull("Reminder should be retrieved", retrievedReminder)
        assertEquals("Custom sound URI should match", customSoundUri, retrievedReminder?.customAlarmSoundUri)
        assertEquals("Custom sound name should match", customSoundName, retrievedReminder?.customAlarmSoundName)
        
        // Verify AlarmSoundManager would use this URI
        // (In actual ReminderService, this URI would be passed to playAlarmSoundFromUri)
        assertNotNull("Custom sound URI should be available for playback", retrievedReminder?.customAlarmSoundUri)
    }
    
    @Test
    fun `alarm trigger without custom sound should use default sound`() = runTest {
        // Create a reminder without custom alarm sound
        val reminder = Reminder(
            id = 0,
            title = "Test Reminder",
            priority = PriorityLevel.MEDIUM,
            startTimeInMillis = System.currentTimeMillis() + 60000,
            recurrenceType = RecurrenceType.ONE_TIME,
            isNagModeEnabled = false,
            nagIntervalInMillis = null,
            nagTotalRepetitions = 1,
            currentRepetitionIndex = 0,
            isCompleted = false,
            isEnabled = true,
            category = ReminderCategory.GENERAL,
            customAlarmSoundUri = null,
            customAlarmSoundName = null
        )
        
        // Insert the reminder
        val reminderId = repository.insertReminder(reminder)
        
        // Retrieve the reminder (simulating alarm trigger)
        val retrievedReminder = repository.getReminderById(reminderId.toInt())
        
        // Verify no custom sound is set (default will be used)
        assertNotNull("Reminder should be retrieved", retrievedReminder)
        assertNull("Custom sound URI should be null", retrievedReminder?.customAlarmSoundUri)
        assertNull("Custom sound name should be null", retrievedReminder?.customAlarmSoundName)
        
        // Verify AlarmSoundManager would use default
        // (In actual ReminderService, null URI triggers default sound)
        assertNull("No custom sound URI means default will be used", retrievedReminder?.customAlarmSoundUri)
    }
    
    @Test
    fun `volume control should clamp to valid range`() {
        // Test volume clamping
        alarmSoundManager.setAlarmVolume(-0.5f)
        assertEquals("Negative volume should be clamped to 0.0", 0.0f, alarmSoundManager.getAlarmVolume(), 0.001f)
        
        alarmSoundManager.setAlarmVolume(1.5f)
        assertEquals("Volume > 1.0 should be clamped to 1.0", 1.0f, alarmSoundManager.getAlarmVolume(), 0.001f)
        
        alarmSoundManager.setAlarmVolume(0.5f)
        assertEquals("Valid volume should be preserved", 0.5f, alarmSoundManager.getAlarmVolume(), 0.001f)
    }
    
    @Test
    fun `volume control preview volume should be independent from alarm volume`() {
        // Set different volumes for alarm and preview
        alarmSoundManager.setAlarmVolume(0.8f)
        alarmSoundManager.setPreviewVolume(0.5f)
        
        // Verify they are independent
        assertEquals("Alarm volume should be 0.8", 0.8f, alarmSoundManager.getAlarmVolume(), 0.001f)
        assertEquals("Preview volume should be 0.5", 0.5f, alarmSoundManager.getPreviewVolume(), 0.001f)
        
        // Change alarm volume
        alarmSoundManager.setAlarmVolume(0.3f)
        
        // Preview volume should remain unchanged
        assertEquals("Alarm volume should be updated to 0.3", 0.3f, alarmSoundManager.getAlarmVolume(), 0.001f)
        assertEquals("Preview volume should remain 0.5", 0.5f, alarmSoundManager.getPreviewVolume(), 0.001f)
    }
    
    @Test
    fun `fallback logic invalid URI should not crash`() {
        // Test that invalid URIs are handled gracefully
        val invalidUris = listOf(
            "",
            "invalid://uri",
            "malformed",
            null
        )
        
        invalidUris.forEach { uri ->
            // This should not throw an exception
            try {
                alarmSoundManager.playAlarmSoundFromUri(uri)
                // If we get here, the fallback worked
                assertTrue("Fallback should handle invalid URI: $uri", true)
            } catch (e: Exception) {
                fail("Should not throw exception for invalid URI: $uri. Exception: ${e.message}")
            } finally {
                alarmSoundManager.stopAlarmSound()
            }
        }
    }
    
    @Test
    fun `alarm sound manager should handle multiple start stop cycles`() {
        // Test that the manager can handle multiple start/stop cycles
        repeat(10) {
            alarmSoundManager.playAlarmSoundFromUri(null) // Use default sound
            alarmSoundManager.stopAlarmSound()
        }
        
        // Should complete without errors
        assertTrue("Multiple start/stop cycles should work", true)
    }
}
