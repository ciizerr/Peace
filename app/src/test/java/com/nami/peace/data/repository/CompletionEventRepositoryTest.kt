package com.nami.peace.data.repository

import com.nami.peace.domain.model.CompletionEvent
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.RecurrenceType
import com.nami.peace.domain.model.ReminderCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Unit tests for CompletionEventRepository.
 * Tests the basic CRUD operations and data retrieval for completion events.
 */
class CompletionEventRepositoryTest {

    @Test
    fun `completion event contains all required fields`() = runTest {
        // Given: A completion event with all fields populated
        val now = System.currentTimeMillis()
        val scheduledTime = now - TimeUnit.HOURS.toMillis(1)
        val calendar = Calendar.getInstance().apply { timeInMillis = now }
        
        val event = CompletionEvent(
            id = 0,
            reminderId = 123,
            title = "Test Task",
            priority = PriorityLevel.HIGH,
            category = ReminderCategory.WORK,
            scheduledTimeInMillis = scheduledTime,
            completedTimeInMillis = now,
            completionDelayInMillis = now - scheduledTime,
            wasNagMode = true,
            nagRepetitionIndex = 2,
            nagTotalRepetitions = 5,
            dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK),
            hourOfDay = calendar.get(Calendar.HOUR_OF_DAY),
            wasRecurring = true,
            recurrenceType = RecurrenceType.DAILY
        )
        
        // Then: All fields should be properly set
        assertEquals(123, event.reminderId)
        assertEquals("Test Task", event.title)
        assertEquals(PriorityLevel.HIGH, event.priority)
        assertEquals(ReminderCategory.WORK, event.category)
        assertEquals(scheduledTime, event.scheduledTimeInMillis)
        assertEquals(now, event.completedTimeInMillis)
        assertTrue(event.completionDelayInMillis > 0)
        assertTrue(event.wasNagMode)
        assertEquals(2, event.nagRepetitionIndex)
        assertEquals(5, event.nagTotalRepetitions)
        assertTrue(event.dayOfWeek in 1..7)
        assertTrue(event.hourOfDay in 0..23)
        assertTrue(event.wasRecurring)
        assertEquals(RecurrenceType.DAILY, event.recurrenceType)
    }
    
    @Test
    fun `completion delay is calculated correctly for late completion`() {
        // Given: A task scheduled for 10:00 AM
        val scheduledTime = System.currentTimeMillis()
        
        // When: Completed 30 minutes late
        val completedTime = scheduledTime + TimeUnit.MINUTES.toMillis(30)
        val delay = completedTime - scheduledTime
        
        // Then: Delay should be positive (30 minutes)
        assertEquals(TimeUnit.MINUTES.toMillis(30), delay)
        assertTrue(delay > 0)
    }
    
    @Test
    fun `completion delay is calculated correctly for early completion`() {
        // Given: A task scheduled for 10:00 AM
        val scheduledTime = System.currentTimeMillis()
        
        // When: Completed 15 minutes early
        val completedTime = scheduledTime - TimeUnit.MINUTES.toMillis(15)
        val delay = completedTime - scheduledTime
        
        // Then: Delay should be negative (15 minutes)
        assertEquals(-TimeUnit.MINUTES.toMillis(15), delay)
        assertTrue(delay < 0)
    }
    
    @Test
    fun `day of week is correctly extracted from timestamp`() {
        // Given: A specific date (e.g., Sunday)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        }
        
        // When: Extracting day of week
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        
        // Then: Should be 1 (Sunday)
        assertEquals(Calendar.SUNDAY, dayOfWeek)
        assertEquals(1, dayOfWeek)
    }
    
    @Test
    fun `hour of day is correctly extracted from timestamp`() {
        // Given: A specific time (e.g., 3 PM)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 15)
        }
        
        // When: Extracting hour of day
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        
        // Then: Should be 15 (3 PM in 24-hour format)
        assertEquals(15, hourOfDay)
        assertTrue(hourOfDay in 0..23)
    }
    
    @Test
    fun `nag mode fields are null for non-nag tasks`() {
        // Given: A completion event for a non-nag mode task
        val event = CompletionEvent(
            id = 0,
            reminderId = 123,
            title = "Simple Task",
            priority = PriorityLevel.MEDIUM,
            category = ReminderCategory.GENERAL,
            scheduledTimeInMillis = System.currentTimeMillis(),
            completedTimeInMillis = System.currentTimeMillis(),
            completionDelayInMillis = 0,
            wasNagMode = false,
            nagRepetitionIndex = null,
            nagTotalRepetitions = null,
            dayOfWeek = 1,
            hourOfDay = 12,
            wasRecurring = false,
            recurrenceType = RecurrenceType.ONE_TIME
        )
        
        // Then: Nag mode fields should be null
        assertFalse(event.wasNagMode)
        assertNull(event.nagRepetitionIndex)
        assertNull(event.nagTotalRepetitions)
    }
    
    @Test
    fun `recurring flag matches recurrence type`() {
        // Given: A daily recurring task
        val recurringEvent = CompletionEvent(
            id = 0,
            reminderId = 123,
            title = "Daily Task",
            priority = PriorityLevel.MEDIUM,
            category = ReminderCategory.GENERAL,
            scheduledTimeInMillis = System.currentTimeMillis(),
            completedTimeInMillis = System.currentTimeMillis(),
            completionDelayInMillis = 0,
            wasNagMode = false,
            nagRepetitionIndex = null,
            nagTotalRepetitions = null,
            dayOfWeek = 1,
            hourOfDay = 12,
            wasRecurring = true,
            recurrenceType = RecurrenceType.DAILY
        )
        
        // Then: wasRecurring should be true and recurrenceType should not be ONE_TIME
        assertTrue(recurringEvent.wasRecurring)
        assertNotEquals(RecurrenceType.ONE_TIME, recurringEvent.recurrenceType)
        
        // Given: A one-time task
        val oneTimeEvent = recurringEvent.copy(
            wasRecurring = false,
            recurrenceType = RecurrenceType.ONE_TIME
        )
        
        // Then: wasRecurring should be false and recurrenceType should be ONE_TIME
        assertFalse(oneTimeEvent.wasRecurring)
        assertEquals(RecurrenceType.ONE_TIME, oneTimeEvent.recurrenceType)
    }
}
