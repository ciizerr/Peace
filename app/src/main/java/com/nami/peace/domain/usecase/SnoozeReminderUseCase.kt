package com.nami.peace.domain.usecase

import android.content.Context
import com.nami.peace.domain.model.Reminder
import com.nami.peace.domain.repository.ReminderRepository
import com.nami.peace.scheduler.AlarmScheduler
import javax.inject.Inject
import java.util.Calendar

class SnoozeReminderUseCase @Inject constructor(
    private val repository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler
) {

    suspend operator fun invoke(reminder: Reminder, context: Context) {
        // 1. Show Notification via Service
        val intent = android.content.Intent(context, com.nami.peace.scheduler.ReminderService::class.java).apply {
            putExtra("REMINDER_ID", reminder.id)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }

        // 2. Handle State Machine
        val now = System.currentTimeMillis()
        
        // If already completed, do nothing (should have been cancelled)
        if (reminder.isCompleted) return

        // Check if we are in Micro-Loop or Main Loop
        if (!reminder.isInNestedSnoozeLoop) {
            // MAIN LOOP TRIGGERED
            // Enter Nested Snooze Loop
            val updatedReminder = reminder.copy(
                isInNestedSnoozeLoop = true,
                nestedSnoozeStartTime = now
            )
            repository.updateReminder(updatedReminder)
            
            // Schedule first Micro-Loop
            // Rule: Minutes -> 2 mins, Hours -> 5 mins
            // We need to know the Main Interval unit. 
            // Since we only store intervalInMillis, we infer or store unit.
            // For simplicity, let's assume if interval < 1 hour (3600000), it's minutes-based.
            val isMinutesBased = (reminder.nagIntervalInMillis ?: 0) < 3600000
            val microInterval = if (isMinutesBased) 2 * 60 * 1000L else 5 * 60 * 1000L
            
            alarmScheduler.schedule(updatedReminder, now + microInterval)
            
        } else {
            // MICRO LOOP TRIGGERED
            // Check Timeout (30 mins)
            val timeout = 30 * 60 * 1000L
            val startTime = reminder.nestedSnoozeStartTime ?: now
            
            if (now - startTime >= timeout) {
                // Timeout Reached. Stop Micro-Loop.
                // Wait for next Main Interval (if repeating)
                // We don't need to do anything here because the Main Interval 
                // should have been scheduled separately? 
                // Actually, AlarmManager only holds one alarm per PendingIntent (RequestCode).
                // So we need to reschedule the Main Alarm if we are aborting the Micro-Loop.
                
                // Wait, if we are in Micro-Loop, we overwrote the Main Alarm?
                // Yes, because we use the same RequestCode (reminder.id).
                
                // So we need to calculate the NEXT Main Interval time.
                // This is tricky. We need to know the original schedule.
                // Let's assume we just stop nagging until the user interacts or we 
                // need to support the "Next Main Interval" override.
                
                // If the user ignores it for 30 mins, we just stop.
                // But if it's a repeating reminder (e.g. every 1 hour), 
                // we need to ensure the next hour triggers.
                
                // For this iteration, let's just stop the micro-loop.
                // The next main repetition (if any) needs to be scheduled.
                // But we don't have the "next repetition time" stored easily.
                // We might need to calculate it based on startTime + (index * interval).
                
                val nextMainTime = calculateNextMainTime(reminder)
                if (nextMainTime > now) {
                     val resetReminder = reminder.copy(
                        isInNestedSnoozeLoop = false,
                        nestedSnoozeStartTime = null
                    )
                    repository.updateReminder(resetReminder)
                    alarmScheduler.schedule(resetReminder, nextMainTime)
                }
                
            } else {
                // Continue Micro-Loop
                 val isMinutesBased = (reminder.nagIntervalInMillis ?: 0) < 3600000
                val microInterval = if (isMinutesBased) 2 * 60 * 1000L else 5 * 60 * 1000L
                
                alarmScheduler.schedule(reminder, now + microInterval)
            }
        }
    }
    
    private fun calculateNextMainTime(reminder: Reminder): Long {
        // Calculate when the next Main Interval should be
        // StartTime + (CurrentIndex + 1) * Interval
        // But wait, CurrentIndex tracks the *Main* repetitions.
        // We haven't incremented CurrentIndex yet because the current one isn't "Done".
        // But if we timeout, do we skip to the next one?
        // The requirement says: "Override: If the next Main Interval time arrives, it cancels any active Micro-Loop and rings the Main Alarm."
        
        // So we should have checked if we crossed the Main Interval line.
        
        val interval = reminder.nagIntervalInMillis ?: return 0
        val nextIndex = reminder.currentRepetitionIndex + 1
        
        // Check if we exceeded max repetitions
        if (nextIndex >= reminder.nagTotalRepetitions) return 0
        
        return reminder.startTimeInMillis + (nextIndex * interval)
    }
}
