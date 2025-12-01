package com.nami.peace.domain.usecase

import com.nami.peace.domain.model.Reminder
import com.nami.peace.domain.repository.ReminderRepository
import com.nami.peace.scheduler.AlarmScheduler
import javax.inject.Inject

/**
 * Use case for importing a reminder from a deep link.
 * 
 * This use case handles:
 * - Validating the imported reminder data
 * - Creating a new reminder in the database
 * - Scheduling the alarm for the new reminder
 * 
 * Requirements: 9.3, 9.5
 */
class ImportReminderUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler
) {
    /**
     * Imports a reminder from a deep link.
     * 
     * @param reminder The reminder to import (with id = 0)
     * @return The ID of the newly created reminder
     * @throws IllegalArgumentException if the reminder data is invalid
     */
    suspend operator fun invoke(reminder: Reminder): Long {
        // Validate reminder data
        require(reminder.title.isNotBlank()) { "Reminder title cannot be blank" }
        require(reminder.startTimeInMillis > 0) { "Invalid start time" }
        
        // Ensure this is a new reminder (id should be 0)
        val newReminder = reminder.copy(
            id = 0,
            isCompleted = false,
            isEnabled = true,
            currentRepetitionIndex = 0,
            isInNestedSnoozeLoop = false,
            nestedSnoozeStartTime = null,
            originalStartTimeInMillis = reminder.startTimeInMillis
        )
        
        // Insert the reminder into the database
        val reminderId = reminderRepository.insertReminder(newReminder)
        
        // Schedule the alarm if the reminder is in the future
        val currentTime = System.currentTimeMillis()
        if (newReminder.startTimeInMillis > currentTime) {
            val reminderWithId = newReminder.copy(id = reminderId.toInt())
            alarmScheduler.schedule(reminderWithId)
        }
        
        return reminderId
    }
}
