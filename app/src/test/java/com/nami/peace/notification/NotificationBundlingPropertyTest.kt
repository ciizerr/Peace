package com.nami.peace.notification

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nami.peace.domain.model.*
import com.nami.peace.domain.repository.SubtaskRepository
import com.nami.peace.util.icon.IoniconsManager
import com.nami.peace.util.notification.ReminderNotificationHelper
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.random.Random

/**
 * Property-based test for notification bundling.
 * 
 * **Feature: peace-app-enhancement, Property 27: Notification bundling**
 * **Validates: Requirements 14.5**
 * 
 * Property 27: For any set of reminders triggering within a 1-minute window, 
 * they should be displayed as a single bundled notification.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NotificationBundlingPropertyTest {
    
    private lateinit var context: Context
    private lateinit var notificationHelper: ReminderNotificationHelper
    private lateinit var iconManager: IoniconsManager
    private lateinit var subtaskRepository: SubtaskRepository
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        iconManager = IoniconsManager(context)
        subtaskRepository = mock(SubtaskRepository::class.java)
        notificationHelper = ReminderNotificationHelper(context, iconManager, subtaskRepository)
    }
    
    /**
     * Property 27: Notification bundling - simultaneous reminders
     * For any set of reminders triggering within a 1-minute window, 
     * they should be detected as simultaneous and bundled.
     */
    @Test
    fun `Property 27 - Simultaneous reminders within 1 minute window are detected for bundling`() = runTest {
        // Test with 100 random scenarios
        repeat(100) { iteration ->
            val baseTime = System.currentTimeMillis()
            
            // Generate 2-10 reminders
            val reminderCount = Random.nextInt(2, 11)
            val reminders = (0 until reminderCount).map { index ->
                // All reminders within 1 minute window (60,000 ms)
                val timeOffset = Random.nextLong(0, 60_000)
                
                Reminder(
                    id = iteration * 100 + index,
                    title = "Test Reminder $iteration-$index",
                    priority = PriorityLevel.values().random(),
                    startTimeInMillis = baseTime + timeOffset,
                    recurrenceType = RecurrenceType.ONE_TIME,
                    isNagModeEnabled = false,
                    nagIntervalInMillis = null,
                    nagTotalRepetitions = 1,
                    currentRepetitionIndex = 0,
                    isCompleted = false,
                    isEnabled = true,
                    category = ReminderCategory.values().random(),
                    originalStartTimeInMillis = baseTime + timeOffset
                )
            }
            
            // Action: Detect simultaneous reminders
            val bundledIds = notificationHelper.detectSimultaneousReminders(reminders, baseTime)
            
            // Verify: All reminders should be bundled (since all are within 1 minute)
            assertEquals(
                "All $reminderCount reminders within 1-minute window should be bundled",
                reminderCount,
                bundledIds.size
            )
            
            // Verify: Bundled IDs are sorted by priority (HIGH=0, MEDIUM=1, LOW=2)
            val bundledReminders = bundledIds.map { id -> reminders.first { it.id == id } }
            val priorities = bundledReminders.map { it.priority.ordinal }
            
            // Check if sorted
            val isSorted = priorities.zipWithNext().all { (a, b) -> a <= b }
            assertTrue(
                "Bundled reminders should be sorted by priority (HIGH first)",
                isSorted
            )
        }
    }
    
    /**
     * Property 27: Notification bundling - reminders outside window not bundled
     * For any set of reminders where some are outside the 1-minute window,
     * only those within the window should be bundled.
     */
    @Test
    fun `Property 27 - Reminders outside 1 minute window are not bundled`() = runTest {
        // Test with 100 random scenarios
        repeat(100) { iteration ->
            val baseTime = System.currentTimeMillis()
            
            // Generate reminders: some within window, some outside
            val remindersInWindow = Random.nextInt(2, 6)
            val remindersOutsideWindow = Random.nextInt(1, 5)
            
            val allReminders = mutableListOf<Reminder>()
            
            // Add reminders within window
            repeat(remindersInWindow) { index ->
                val timeOffset = Random.nextLong(0, 60_000)
                allReminders.add(
                    Reminder(
                        id = iteration * 100 + index,
                        title = "In Window $iteration-$index",
                        priority = PriorityLevel.values().random(),
                        startTimeInMillis = baseTime + timeOffset,
                        recurrenceType = RecurrenceType.ONE_TIME,
                        isNagModeEnabled = false,
                        nagIntervalInMillis = null,
                        nagTotalRepetitions = 1,
                        currentRepetitionIndex = 0,
                        isCompleted = false,
                        isEnabled = true,
                        category = ReminderCategory.values().random(),
                        originalStartTimeInMillis = baseTime + timeOffset
                    )
                )
            }
            
            // Add reminders outside window (more than 1 minute away)
            repeat(remindersOutsideWindow) { index ->
                val timeOffset = Random.nextLong(61_000, 300_000) // 1-5 minutes away
                allReminders.add(
                    Reminder(
                        id = iteration * 100 + 50 + index,
                        title = "Outside Window $iteration-$index",
                        priority = PriorityLevel.values().random(),
                        startTimeInMillis = baseTime + timeOffset,
                        recurrenceType = RecurrenceType.ONE_TIME,
                        isNagModeEnabled = false,
                        nagIntervalInMillis = null,
                        nagTotalRepetitions = 1,
                        currentRepetitionIndex = 0,
                        isCompleted = false,
                        isEnabled = true,
                        category = ReminderCategory.values().random(),
                        originalStartTimeInMillis = baseTime + timeOffset
                    )
                )
            }
            
            // Action: Detect simultaneous reminders
            val bundledIds = notificationHelper.detectSimultaneousReminders(allReminders, baseTime)
            
            // Verify: Only reminders within window should be bundled
            assertEquals(
                "Only $remindersInWindow reminders within window should be bundled",
                remindersInWindow,
                bundledIds.size
            )
            
            // Verify: All bundled reminders are within the window
            val bundledReminders = bundledIds.map { id -> allReminders.first { it.id == id } }
            bundledReminders.forEach { reminder ->
                val timeDiff = kotlin.math.abs(reminder.startTimeInMillis - baseTime)
                assertTrue(
                    "Bundled reminder should be within 1-minute window (diff: $timeDiff ms)",
                    timeDiff < 60_000
                )
            }
        }
    }
    
    /**
     * Property 27: Notification bundling - single reminder not bundled
     * For any single reminder, it should not be bundled (bundling requires 2+).
     */
    @Test
    fun `Property 27 - Single reminder is not bundled`() = runTest {
        // Test with 50 random single reminders
        repeat(50) { iteration ->
            val baseTime = System.currentTimeMillis()
            
            val reminder = Reminder(
                id = iteration,
                title = "Single Reminder $iteration",
                priority = PriorityLevel.values().random(),
                startTimeInMillis = baseTime,
                recurrenceType = RecurrenceType.ONE_TIME,
                isNagModeEnabled = false,
                nagIntervalInMillis = null,
                nagTotalRepetitions = 1,
                currentRepetitionIndex = 0,
                isCompleted = false,
                isEnabled = true,
                category = ReminderCategory.values().random(),
                originalStartTimeInMillis = baseTime
            )
            
            // Action: Detect simultaneous reminders
            val bundledIds = notificationHelper.detectSimultaneousReminders(listOf(reminder), baseTime)
            
            // Verify: Single reminder should not be bundled
            assertTrue(
                "Single reminder should not be bundled (empty list expected)",
                bundledIds.isEmpty()
            )
        }
    }
    
    /**
     * Property 27: Notification bundling - completed reminders excluded
     * For any set of reminders, completed or disabled reminders should not be bundled.
     */
    @Test
    fun `Property 27 - Completed or disabled reminders are excluded from bundling`() = runTest {
        // Test with 50 scenarios
        repeat(50) { iteration ->
            val baseTime = System.currentTimeMillis()
            
            // Generate mix of active and inactive reminders
            val activeCount = Random.nextInt(2, 5)
            val inactiveCount = Random.nextInt(1, 4)
            
            val allReminders = mutableListOf<Reminder>()
            
            // Add active reminders
            repeat(activeCount) { index ->
                allReminders.add(
                    Reminder(
                        id = iteration * 100 + index,
                        title = "Active $iteration-$index",
                        priority = PriorityLevel.values().random(),
                        startTimeInMillis = baseTime,
                        recurrenceType = RecurrenceType.ONE_TIME,
                        isNagModeEnabled = false,
                        nagIntervalInMillis = null,
                        nagTotalRepetitions = 1,
                        currentRepetitionIndex = 0,
                        isCompleted = false,
                        isEnabled = true,
                        category = ReminderCategory.values().random(),
                        originalStartTimeInMillis = baseTime
                    )
                )
            }
            
            // Add inactive reminders (completed or disabled)
            repeat(inactiveCount) { index ->
                // Make sure at least one of isCompleted or isEnabled makes it inactive
                val isCompleted = Random.nextBoolean()
                val isEnabled = if (isCompleted) Random.nextBoolean() else false
                
                allReminders.add(
                    Reminder(
                        id = iteration * 100 + 50 + index,
                        title = "Inactive $iteration-$index",
                        priority = PriorityLevel.values().random(),
                        startTimeInMillis = baseTime,
                        recurrenceType = RecurrenceType.ONE_TIME,
                        isNagModeEnabled = false,
                        nagIntervalInMillis = null,
                        nagTotalRepetitions = 1,
                        currentRepetitionIndex = 0,
                        isCompleted = isCompleted,
                        isEnabled = isEnabled,
                        category = ReminderCategory.values().random(),
                        originalStartTimeInMillis = baseTime
                    )
                )
            }
            
            // Action: Detect simultaneous reminders
            val bundledIds = notificationHelper.detectSimultaneousReminders(allReminders, baseTime)
            
            // Verify: Only active reminders should be bundled
            assertEquals(
                "Only $activeCount active reminders should be bundled",
                activeCount,
                bundledIds.size
            )
            
            // Verify: All bundled reminders are active
            val bundledReminders = bundledIds.map { id -> allReminders.first { it.id == id } }
            bundledReminders.forEach { reminder ->
                assertFalse("Bundled reminder should not be completed", reminder.isCompleted)
                assertTrue("Bundled reminder should be enabled", reminder.isEnabled)
            }
        }
    }
    
    /**
     * Property 27: Notification bundling - bundled notification creation
     * For any set of 2+ reminders, a bundled notification should be created successfully.
     */
    @Test
    fun `Property 27 - Bundled notification is created for multiple reminders`() = runTest {
        // Test with 20 scenarios
        repeat(20) { iteration ->
            val baseTime = System.currentTimeMillis()
            
            // Generate 2-5 reminders
            val reminderCount = Random.nextInt(2, 6)
            val reminders = (0 until reminderCount).map { index ->
                Reminder(
                    id = iteration * 100 + index,
                    title = "Bundled Test $iteration-$index",
                    priority = PriorityLevel.values().random(),
                    startTimeInMillis = baseTime,
                    recurrenceType = RecurrenceType.ONE_TIME,
                    isNagModeEnabled = false,
                    nagIntervalInMillis = null,
                    nagTotalRepetitions = 1,
                    currentRepetitionIndex = 0,
                    isCompleted = false,
                    isEnabled = true,
                    category = ReminderCategory.values().random(),
                    originalStartTimeInMillis = baseTime
                )
            }
            
            // Action: Create bundled notification
            val notificationBuilder = notificationHelper.createBundledNotification(reminders)
            val notification = notificationBuilder.build()
            
            // Verify: Notification was created
            assertNotNull("Bundled notification should be created", notification)
            
            // Verify: Notification has group key
            assertEquals(
                "Bundled notification should have correct group key",
                ReminderNotificationHelper.BUNDLED_GROUP_KEY,
                notificationBuilder.build().group
            )
        }
    }
    
    /**
     * Property 27: Notification bundling - priority sorting
     * For any bundled notification, reminders should be sorted by priority (HIGH first).
     */
    @Test
    fun `Property 27 - Bundled reminders are sorted by priority`() = runTest {
        // Test with 50 scenarios
        repeat(50) { iteration ->
            val baseTime = System.currentTimeMillis()
            
            // Generate reminders with random priorities
            val reminderCount = Random.nextInt(3, 8)
            val reminders = (0 until reminderCount).map { index ->
                Reminder(
                    id = iteration * 100 + index,
                    title = "Priority Test $iteration-$index",
                    priority = PriorityLevel.values().random(),
                    startTimeInMillis = baseTime,
                    recurrenceType = RecurrenceType.ONE_TIME,
                    isNagModeEnabled = false,
                    nagIntervalInMillis = null,
                    nagTotalRepetitions = 1,
                    currentRepetitionIndex = 0,
                    isCompleted = false,
                    isEnabled = true,
                    category = ReminderCategory.values().random(),
                    originalStartTimeInMillis = baseTime
                )
            }
            
            // Action: Detect simultaneous reminders (which sorts by priority)
            val bundledIds = notificationHelper.detectSimultaneousReminders(reminders, baseTime)
            
            // Verify: Bundled IDs are sorted by priority
            val bundledReminders = bundledIds.map { id -> reminders.first { it.id == id } }
            val priorities = bundledReminders.map { it.priority.ordinal }
            
            // Check if sorted (HIGH=0, MEDIUM=1, LOW=2)
            val isSorted = priorities.zipWithNext().all { (a, b) -> a <= b }
            assertTrue(
                "Bundled reminders should be sorted by priority (HIGH first), got: $priorities",
                isSorted
            )
            
            // Verify: If there's a HIGH priority reminder, it should be first
            val hasHighPriority = reminders.any { it.priority == PriorityLevel.HIGH }
            if (hasHighPriority) {
                val firstReminder = bundledReminders.first()
                assertEquals(
                    "First bundled reminder should be HIGH priority if any HIGH exists",
                    PriorityLevel.HIGH,
                    firstReminder.priority
                )
            }
        }
    }
}
