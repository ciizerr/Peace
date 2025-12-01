package com.nami.peace.notification

import android.content.Context
import android.content.Intent
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nami.peace.data.local.AppDatabase
import com.nami.peace.data.local.GardenTheme
import com.nami.peace.data.local.HistoryDao
import com.nami.peace.data.repository.GardenRepositoryImpl
import com.nami.peace.data.repository.ReminderRepositoryImpl
import com.nami.peace.domain.model.*
import com.nami.peace.domain.repository.GardenRepository
import com.nami.peace.domain.repository.ReminderRepository
import com.nami.peace.domain.usecase.*
import com.nami.peace.scheduler.AlarmReceiver
import com.nami.peace.scheduler.AlarmScheduler
import com.nami.peace.util.notification.MilestoneNotificationHelper
import com.nami.peace.widget.WidgetUpdateManager
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Property-based tests for notification actions.
 * 
 * **Feature: peace-app-enhancement, Property 26: Notification completion side effects**
 * **Feature: peace-app-enhancement, Property 28: Nag mode progression**
 * **Feature: peace-app-enhancement, Property 29: Panic loop activation**
 * **Validates: Requirements 14.2, 19.3, 19.4**
 * 
 * Property 26: For any reminder completion via notification, both the reminder should be marked 
 * complete AND the Peace Garden should update.
 * 
 * Property 28: For any nag mode reminder completion, if not the final repetition, the next 
 * repetition should be scheduled; if final, the reminder should be marked complete.
 * 
 * Property 29: For any snooze action during nag mode, the panic loop should activate with 
 * the next alarm scheduled in exactly 2 minutes.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NotificationActionPropertyTest {
    
    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var reminderRepository: ReminderRepository
    private lateinit var gardenRepository: GardenRepository
    private lateinit var historyDao: HistoryDao
    private lateinit var alarmReceiver: AlarmReceiver
    private lateinit var alarmScheduler: AlarmScheduler
    private lateinit var completeTaskUseCase: CompleteTaskUseCase
    private lateinit var milestoneNotificationHelper: MilestoneNotificationHelper
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        reminderRepository = ReminderRepositoryImpl(database.reminderDao(), mock(WidgetUpdateManager::class.java))
        gardenRepository = GardenRepositoryImpl(database.gardenDao(), mock(WidgetUpdateManager::class.java))
        historyDao = database.historyDao()
        
        // Initialize garden state
        kotlinx.coroutines.runBlocking {
            gardenRepository.insertGardenState(
                GardenState(
                    theme = GardenTheme.ZEN,
                    growthStage = 0,
                    currentStreak = 0,
                    longestStreak = 0,
                    lastCompletionDate = null,
                    totalTasksCompleted = 0
                )
            )
        }
        
        // Create use cases
        val updateStreakUseCase = UpdateStreakUseCase(gardenRepository)
        val advanceGrowthStageUseCase = AdvanceGrowthStageUseCase(gardenRepository)
        val checkMilestoneUseCase = CheckMilestoneUseCase(gardenRepository)
        val trackCompletionEventUseCase = mock(TrackCompletionEventUseCase::class.java)
        val widgetUpdateManager = mock(WidgetUpdateManager::class.java)
        
        completeTaskUseCase = CompleteTaskUseCase(
            reminderRepository,
            updateStreakUseCase,
            advanceGrowthStageUseCase,
            checkMilestoneUseCase,
            trackCompletionEventUseCase,
            widgetUpdateManager
        )
        
        alarmScheduler = mock(AlarmScheduler::class.java)
        milestoneNotificationHelper = mock(MilestoneNotificationHelper::class.java)
        
        // Create AlarmReceiver with dependencies
        alarmReceiver = AlarmReceiver().apply {
            repository = reminderRepository
            this.alarmScheduler = this@NotificationActionPropertyTest.alarmScheduler
            this.historyDao = this@NotificationActionPropertyTest.historyDao
            this.completeTaskUseCase = this@NotificationActionPropertyTest.completeTaskUseCase
            this.milestoneNotificationHelper = this@NotificationActionPropertyTest.milestoneNotificationHelper
        }
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    /**
     * Property 26: Notification completion side effects
     * For any reminder completion via notification, both the reminder should be marked 
     * complete AND the Peace Garden should update.
     */
    @Test
    fun `Property 26 - Notification completion side effects - single reminder`() = runTest {
        // Test with 10 different reminders
        repeat(10) { iteration ->
            database.clearAllTables()
            
            // Re-initialize garden state
            gardenRepository.insertGardenState(
                GardenState(
                    theme = GardenTheme.ZEN,
                    growthStage = 0,
                    currentStreak = 0,
                    longestStreak = 0,
                    lastCompletionDate = null,
                    totalTasksCompleted = 0
                )
            )
            
            // Create a reminder
            val now = System.currentTimeMillis()
            val reminder = Reminder(
                id = iteration + 1,
                title = "Test Reminder $iteration",
                priority = PriorityLevel.values().random(),
                startTimeInMillis = now,
                recurrenceType = RecurrenceType.ONE_TIME,
                isNagModeEnabled = false,
                nagIntervalInMillis = null,
                nagTotalRepetitions = 1,
                currentRepetitionIndex = 0,
                isCompleted = false,
                isEnabled = true,
                category = ReminderCategory.GENERAL,
                originalStartTimeInMillis = now
            )
            
            // Setup: Insert reminder
            reminderRepository.insertReminder(reminder)
            
            // Get initial garden state
            val initialGardenState = gardenRepository.getGardenStateOnce()
            val initialTasksCompleted = initialGardenState?.totalTasksCompleted ?: 0
            
            // Action: Simulate ACTION_COMPLETE
            val intent = Intent("com.nami.peace.ACTION_COMPLETE").apply {
                putExtra("REMINDER_ID", reminder.id)
            }
            alarmReceiver.onReceive(context, intent)
            
            // Give async operations time to complete
            kotlinx.coroutines.delay(500)
            
            // Verify: Reminder is marked complete
            val updatedReminder = reminderRepository.getReminderById(reminder.id)
            assertNotNull("Updated reminder should not be null", updatedReminder)
            assertTrue("Reminder should be marked complete", updatedReminder!!.isCompleted)
            
            // Verify: Garden state was updated
            val updatedGardenState = gardenRepository.getGardenStateOnce()
            assertNotNull("Updated garden state should not be null", updatedGardenState)
            assertEquals(
                "Garden tasks completed should increment",
                initialTasksCompleted + 1,
                updatedGardenState!!.totalTasksCompleted
            )
        }
    }
    
    /**
     * Property 28: Nag mode progression - not final repetition
     * For any nag mode reminder completion, if not the final repetition, the next 
     * repetition should be scheduled.
     */
    @Test
    fun `Property 28 - Nag mode progression - not final repetition`() = runTest {
        // Test with various nag mode configurations
        val testCases = listOf(
            Triple(2, 0, "2 reps, first"),
            Triple(3, 0, "3 reps, first"),
            Triple(3, 1, "3 reps, middle"),
            Triple(5, 2, "5 reps, middle"),
            Triple(5, 3, "5 reps, near end")
        )
        
        testCases.forEach { (totalReps, currentRep, description) ->
            database.clearAllTables()
            
            // Re-initialize garden state
            gardenRepository.insertGardenState(
                GardenState(
                    theme = GardenTheme.ZEN,
                    growthStage = 0,
                    currentStreak = 0,
                    longestStreak = 0,
                    lastCompletionDate = null,
                    totalTasksCompleted = 0
                )
            )
            
            val now = System.currentTimeMillis()
            val reminder = Reminder(
                id = 100 + currentRep,
                title = "Nag Test $description",
                priority = PriorityLevel.MEDIUM,
                startTimeInMillis = now,
                recurrenceType = RecurrenceType.ONE_TIME,
                isNagModeEnabled = true,
                nagIntervalInMillis = 15 * 60 * 1000L,
                nagTotalRepetitions = totalReps,
                currentRepetitionIndex = currentRep,
                isCompleted = false,
                isEnabled = true,
                category = ReminderCategory.GENERAL,
                originalStartTimeInMillis = now
            )
            
            // Setup: Insert reminder
            reminderRepository.insertReminder(reminder)
            
            // Action: Simulate ACTION_COMPLETE
            val intent = Intent("com.nami.peace.ACTION_COMPLETE").apply {
                putExtra("REMINDER_ID", reminder.id)
            }
            alarmReceiver.onReceive(context, intent)
            
            // Give async operations time to complete
            kotlinx.coroutines.delay(500)
            
            // Verify: Reminder is NOT marked complete (sequence continues)
            val updatedReminder = reminderRepository.getReminderById(reminder.id)
            assertNotNull("Updated reminder should not be null for $description", updatedReminder)
            assertFalse("Reminder should NOT be complete for $description", updatedReminder!!.isCompleted)
            
            // Verify: Current repetition index advanced
            assertEquals(
                "Repetition index should advance for $description",
                currentRep + 1,
                updatedReminder.currentRepetitionIndex
            )
            
            // Verify: Not in panic loop anymore
            assertFalse("Should not be in panic loop for $description", updatedReminder.isInNestedSnoozeLoop)
        }
    }
    
    /**
     * Property 28: Nag mode progression - final repetition
     * For any nag mode reminder completion on the final repetition, the reminder 
     * should be marked complete.
     */
    @Test
    fun `Property 28 - Nag mode progression - final repetition`() = runTest {
        // Test with various nag mode configurations on final rep
        val testCases = listOf(2, 3, 5, 10)
        
        testCases.forEach { totalReps ->
            database.clearAllTables()
            
            // Re-initialize garden state
            gardenRepository.insertGardenState(
                GardenState(
                    theme = GardenTheme.ZEN,
                    growthStage = 0,
                    currentStreak = 0,
                    longestStreak = 0,
                    lastCompletionDate = null,
                    totalTasksCompleted = 0
                )
            )
            
            val now = System.currentTimeMillis()
            val reminder = Reminder(
                id = 200 + totalReps,
                title = "Final Rep Test $totalReps",
                priority = PriorityLevel.HIGH,
                startTimeInMillis = now,
                recurrenceType = RecurrenceType.ONE_TIME,
                isNagModeEnabled = true,
                nagIntervalInMillis = 15 * 60 * 1000L,
                nagTotalRepetitions = totalReps,
                currentRepetitionIndex = totalReps - 1, // Final repetition
                isCompleted = false,
                isEnabled = true,
                category = ReminderCategory.GENERAL,
                originalStartTimeInMillis = now
            )
            
            // Setup: Insert reminder
            reminderRepository.insertReminder(reminder)
            
            // Action: Simulate ACTION_COMPLETE
            val intent = Intent("com.nami.peace.ACTION_COMPLETE").apply {
                putExtra("REMINDER_ID", reminder.id)
            }
            alarmReceiver.onReceive(context, intent)
            
            // Give async operations time to complete
            kotlinx.coroutines.delay(500)
            
            // Verify: Reminder IS marked complete (sequence finished)
            val updatedReminder = reminderRepository.getReminderById(reminder.id)
            assertNotNull("Updated reminder should not be null for $totalReps reps", updatedReminder)
            assertTrue("Reminder should be complete for final rep of $totalReps", updatedReminder!!.isCompleted)
        }
    }
    
    /**
     * Property 29: Panic loop activation
     * For any snooze action during nag mode, the panic loop should activate with 
     * the next alarm scheduled in exactly 2 minutes.
     */
    @Test
    fun `Property 29 - Panic loop activation`() = runTest {
        // Test with 10 different nag mode reminders
        repeat(10) { iteration ->
            database.clearAllTables()
            
            // Re-initialize garden state
            gardenRepository.insertGardenState(
                GardenState(
                    theme = GardenTheme.ZEN,
                    growthStage = 0,
                    currentStreak = 0,
                    longestStreak = 0,
                    lastCompletionDate = null,
                    totalTasksCompleted = 0
                )
            )
            
            val now = System.currentTimeMillis()
            val reminder = Reminder(
                id = 300 + iteration,
                title = "Snooze Test $iteration",
                priority = PriorityLevel.values().random(),
                startTimeInMillis = now,
                recurrenceType = RecurrenceType.ONE_TIME,
                isNagModeEnabled = true,
                nagIntervalInMillis = 15 * 60 * 1000L,
                nagTotalRepetitions = 3,
                currentRepetitionIndex = 1,
                isCompleted = false,
                isEnabled = true,
                category = ReminderCategory.GENERAL,
                originalStartTimeInMillis = now
            )
            
            // Setup: Insert reminder
            reminderRepository.insertReminder(reminder)
            
            val beforeSnoozeTime = System.currentTimeMillis()
            
            // Action: Simulate ACTION_SNOOZE
            val intent = Intent("com.nami.peace.ACTION_SNOOZE").apply {
                putExtra("REMINDER_ID", reminder.id)
            }
            alarmReceiver.onReceive(context, intent)
            
            // Give async operations time to complete
            kotlinx.coroutines.delay(500)
            
            // Verify: Reminder is in panic loop
            val updatedReminder = reminderRepository.getReminderById(reminder.id)
            assertNotNull("Updated reminder should not be null", updatedReminder)
            assertTrue("Reminder should be in panic loop", updatedReminder!!.isInNestedSnoozeLoop)
            
            // Verify: Nested snooze start time is set
            assertNotNull("Nested snooze start time should be set", updatedReminder.nestedSnoozeStartTime)
            
            // Verify: Start time is approximately 2 minutes in the future
            val expectedSnoozeTime = beforeSnoozeTime + (2 * 60 * 1000L)
            val actualSnoozeTime = updatedReminder.startTimeInMillis
            
            // Allow 2 second tolerance for test execution time
            val timeDifference = kotlin.math.abs(actualSnoozeTime - expectedSnoozeTime)
            assertTrue(
                "Snooze time should be approximately 2 minutes (diff: $timeDifference ms)",
                timeDifference < 2000
            )
        }
    }
}
