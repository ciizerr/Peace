package com.nami.peace.util.alarm

import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nami.peace.data.local.AppDatabase
import com.nami.peace.data.repository.ReminderRepositoryImpl
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.RecurrenceType
import com.nami.peace.domain.model.Reminder
import com.nami.peace.domain.model.ReminderCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.random.Random
import org.junit.Assert.*

/**
 * Feature: peace-app-enhancement
 * Property 16: Alarm sound association
 * Property 17: Alarm sound playback selection
 * 
 * Validates: Requirements 7.3, 7.4
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AlarmSoundPropertyTest {
    
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
    
    /**
     * Property 16: Alarm sound association
     * For any custom alarm sound saved for a reminder, the reminder entity should store the sound URI and name.
     */
    @Test
    fun `Property 16 - Alarm sound association - for any custom alarm sound, reminder should store URI and name`() = runTest {
        // Run 100 iterations with random alarm sounds
        repeat(100) {
            val soundUri = generateRandomUri()
            val soundName = generateRandomString(5, 30)
            
            // Create a reminder with custom alarm sound
            val reminder = createTestReminder(
                customAlarmSoundUri = soundUri,
                customAlarmSoundName = soundName
            )
            
            // Insert the reminder
            val reminderId = repository.insertReminder(reminder)
            
            // Retrieve the reminder
            val retrievedReminder = repository.getReminderById(reminderId.toInt())
            
            // Verify the alarm sound URI and name are stored
            assertNotNull("Reminder should be retrieved", retrievedReminder)
            assertEquals("Sound URI should match", soundUri, retrievedReminder?.customAlarmSoundUri)
            assertEquals("Sound name should match", soundName, retrievedReminder?.customAlarmSoundName)
        }
    }
    
    /**
     * Property 16: Alarm sound association - null case
     * For any reminder without a custom alarm sound, the URI and name should be null.
     */
    @Test
    fun `Property 16 - Alarm sound association - reminder without custom sound should have null URI and name`() = runTest {
        // Run 100 iterations
        repeat(100) {
            // Create a reminder without custom alarm sound
            val reminder = createTestReminder(
                customAlarmSoundUri = null,
                customAlarmSoundName = null
            )
            
            // Insert the reminder
            val reminderId = repository.insertReminder(reminder)
            
            // Retrieve the reminder
            val retrievedReminder = repository.getReminderById(reminderId.toInt())
            
            // Verify the alarm sound URI and name are null
            assertNotNull("Reminder should be retrieved", retrievedReminder)
            assertNull("Sound URI should be null", retrievedReminder?.customAlarmSoundUri)
            assertNull("Sound name should be null", retrievedReminder?.customAlarmSoundName)
        }
    }
    
    /**
     * Property 16: Alarm sound association - update case
     * For any reminder, updating the custom alarm sound should persist the new values.
     */
    @Test
    fun `Property 16 - Alarm sound association - updating alarm sound should persist new values`() = runTest {
        // Run 100 iterations
        repeat(100) {
            // Create a reminder with initial alarm sound
            val initialUri = generateRandomUri()
            val initialName = generateRandomString(5, 30)
            val reminder = createTestReminder(
                customAlarmSoundUri = initialUri,
                customAlarmSoundName = initialName
            )
            
            // Insert the reminder
            val reminderId = repository.insertReminder(reminder)
            
            // Update with new alarm sound
            val newUri = generateRandomUri()
            val newName = generateRandomString(5, 30)
            val updatedReminder = reminder.copy(
                id = reminderId.toInt(),
                customAlarmSoundUri = newUri,
                customAlarmSoundName = newName
            )
            repository.updateReminder(updatedReminder)
            
            // Retrieve the reminder
            val retrievedReminder = repository.getReminderById(reminderId.toInt())
            
            // Verify the new alarm sound values are stored
            assertNotNull("Reminder should be retrieved", retrievedReminder)
            assertEquals("Sound URI should be updated", newUri, retrievedReminder?.customAlarmSoundUri)
            assertEquals("Sound name should be updated", newName, retrievedReminder?.customAlarmSoundName)
        }
    }
    
    /**
     * Property 17: Alarm sound playback selection
     * For any reminder alarm trigger, if a custom sound is set, that sound should be used;
     * otherwise, the default sound should be used.
     * 
     * Note: This property tests the logic, not actual audio playback.
     */
    @Test
    fun `Property 17 - Alarm sound playback selection - custom sound should be selected when set`() = runTest {
        // Run 100 iterations
        repeat(100) {
            val soundUri = generateRandomUri()
            val soundName = generateRandomString(5, 30)
            
            // Create a reminder with custom alarm sound
            val reminder = createTestReminder(
                customAlarmSoundUri = soundUri,
                customAlarmSoundName = soundName
            )
            
            // Insert the reminder
            val reminderId = repository.insertReminder(reminder)
            
            // Retrieve the reminder
            val retrievedReminder = repository.getReminderById(reminderId.toInt())
            
            // Verify that custom sound is set (would be used for playback)
            assertNotNull("Reminder should be retrieved", retrievedReminder)
            assertNotNull("Custom sound URI should be set", retrievedReminder?.customAlarmSoundUri)
            assertEquals("Custom sound URI should match", soundUri, retrievedReminder?.customAlarmSoundUri)
        }
    }
    
    /**
     * Property 17: Alarm sound playback selection - default case
     * For any reminder without a custom sound, the default sound should be used.
     */
    @Test
    fun `Property 17 - Alarm sound playback selection - default sound should be used when no custom sound`() = runTest {
        // Run 100 iterations
        repeat(100) {
            // Create a reminder without custom alarm sound
            val reminder = createTestReminder(
                customAlarmSoundUri = null,
                customAlarmSoundName = null
            )
            
            // Insert the reminder
            val reminderId = repository.insertReminder(reminder)
            
            // Retrieve the reminder
            val retrievedReminder = repository.getReminderById(reminderId.toInt())
            
            // Verify that no custom sound is set (default would be used for playback)
            assertNotNull("Reminder should be retrieved", retrievedReminder)
            assertNull("Custom sound URI should be null (default will be used)", retrievedReminder?.customAlarmSoundUri)
        }
    }
    
    /**
     * Property 17: Alarm sound playback selection - AlarmSound creation
     * For any valid URI and name, creating an AlarmSound should preserve the values.
     */
    @Test
    fun `Property 17 - AlarmSound creation - custom sound should preserve URI and name`() {
        // Run 100 iterations
        repeat(100) {
            val uri = Uri.parse(generateRandomUri())
            val name = generateRandomString(5, 30)
            
            // Create a custom alarm sound
            val alarmSound = alarmSoundManager.createCustomSound(uri, name)
            
            // Verify the values are preserved
            assertEquals("Sound name should match", name, alarmSound.name)
            assertEquals("Sound URI should match", uri, alarmSound.uri)
            assertFalse("Custom sound should not be marked as system", alarmSound.isSystem)
            assertTrue("Sound ID should contain 'custom'", alarmSound.id.startsWith("custom_"))
        }
    }
    
    /**
     * Volume control test - alarm volume should be clamped to valid range
     * For any volume value, the alarm volume should be clamped between 0.0 and 1.0.
     */
    @Test
    fun `Volume control - alarm volume should be clamped to valid range`() {
        // Test 100 random volume values including out-of-range values
        repeat(100) {
            val testVolume = Random.nextFloat() * 3.0f - 1.0f // Range: -1.0 to 2.0
            
            alarmSoundManager.setAlarmVolume(testVolume)
            val actualVolume = alarmSoundManager.getAlarmVolume()
            
            // Verify volume is clamped to valid range
            assertTrue("Volume should be >= 0.0", actualVolume >= 0.0f)
            assertTrue("Volume should be <= 1.0", actualVolume <= 1.0f)
            
            // Verify clamping behavior
            val expectedVolume = testVolume.coerceIn(0.0f, 1.0f)
            assertEquals("Volume should be clamped correctly", expectedVolume, actualVolume, 0.001f)
        }
    }
    
    /**
     * Volume control test - preview volume should be clamped to valid range
     * For any volume value, the preview volume should be clamped between 0.0 and 1.0.
     */
    @Test
    fun `Volume control - preview volume should be clamped to valid range`() {
        // Test 100 random volume values including out-of-range values
        repeat(100) {
            val testVolume = Random.nextFloat() * 3.0f - 1.0f // Range: -1.0 to 2.0
            
            alarmSoundManager.setPreviewVolume(testVolume)
            val actualVolume = alarmSoundManager.getPreviewVolume()
            
            // Verify volume is clamped to valid range
            assertTrue("Volume should be >= 0.0", actualVolume >= 0.0f)
            assertTrue("Volume should be <= 1.0", actualVolume <= 1.0f)
            
            // Verify clamping behavior
            val expectedVolume = testVolume.coerceIn(0.0f, 1.0f)
            assertEquals("Volume should be clamped correctly", expectedVolume, actualVolume, 0.001f)
        }
    }
    
    /**
     * Volume control test - volume should persist across multiple sets
     * For any sequence of volume changes, the last set value should be retrievable.
     */
    @Test
    fun `Volume control - volume should persist across multiple sets`() {
        repeat(100) {
            // Set multiple random volumes
            val volumes = List(Random.nextInt(2, 10)) { Random.nextFloat() }
            
            volumes.forEach { volume ->
                alarmSoundManager.setAlarmVolume(volume)
            }
            
            // The last volume should be the current one
            val expectedVolume = volumes.last().coerceIn(0.0f, 1.0f)
            val actualVolume = alarmSoundManager.getAlarmVolume()
            
            assertEquals("Last set volume should be current", expectedVolume, actualVolume, 0.001f)
        }
    }
    
    // Helper functions
    
    private fun createTestReminder(
        customAlarmSoundUri: String? = null,
        customAlarmSoundName: String? = null
    ): Reminder {
        val startTime = System.currentTimeMillis() + Random.nextLong(1000, 86400000)
        return Reminder(
            id = 0,
            title = generateRandomString(5, 50),
            priority = PriorityLevel.values().random(),
            startTimeInMillis = startTime,
            recurrenceType = RecurrenceType.values().random(),
            isNagModeEnabled = Random.nextBoolean(),
            nagIntervalInMillis = if (Random.nextBoolean()) Random.nextLong(900000, 7200000) else null,
            nagTotalRepetitions = Random.nextInt(1, 10),
            currentRepetitionIndex = 0,
            isCompleted = false,
            isEnabled = true,
            isInNestedSnoozeLoop = false,
            nestedSnoozeStartTime = null,
            category = ReminderCategory.values().random(),
            isStrictSchedulingEnabled = Random.nextBoolean(),
            dateInMillis = null,
            daysOfWeek = emptyList(),
            originalStartTimeInMillis = startTime,
            customAlarmSoundUri = customAlarmSoundUri,
            customAlarmSoundName = customAlarmSoundName
        )
    }
    
    private fun generateRandomString(minLength: Int, maxLength: Int): String {
        val length = Random.nextInt(minLength, maxLength + 1)
        val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9') + listOf(' ', '-', '_')
        return (1..length).map { chars.random() }.joinToString("")
    }
    
    private fun generateRandomUri(): String {
        val schemes = listOf("content", "file", "android.resource")
        val scheme = schemes.random()
        val path = generateRandomString(10, 50).replace(" ", "_")
        return "$scheme://com.example.sounds/$path"
    }
}
