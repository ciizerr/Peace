package com.nami.peace.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nami.peace.data.local.HistoryDao
import com.nami.peace.data.local.HistoryEntity
import com.nami.peace.data.repository.UserPreferencesRepository
import com.nami.peace.domain.model.Reminder
import com.nami.peace.domain.model.RecurrenceType
import com.nami.peace.domain.repository.ReminderRepository
import com.nami.peace.ui.alarm.AlarmActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var userPrefs: UserPreferencesRepository
    
    @Inject
    lateinit var repository: ReminderRepository
    
    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var historyDao: HistoryDao

    override fun onReceive(context: Context, intent: Intent) {
        com.nami.peace.util.DebugLogger.log("Receiver Woke Up! Action: ${intent.action}")

        val reminderId = intent.getIntExtra("REMINDER_ID", -1)
        if (reminderId == -1) return

        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO)

        when (intent.action) {
            "com.nami.peace.ACTION_STOP_SOUND" -> {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                notificationManager.cancel(reminderId)
                stopService(context)
                pendingResult.finish()
            }
            "com.nami.peace.ACTION_COMPLETE" -> {
                scope.launch {
                    try {
                        val reminder = repository.getReminderById(reminderId)
                        if (reminder != null) {
                            com.nami.peace.util.DebugLogger.log("User clicked COMPLETE. Current Rep: ${reminder.currentRepetitionIndex + 1}/${reminder.nagTotalRepetitions}")
                            
                            // Archive to History
                            repository.insertHistory(reminder)

                            if (reminder.nagTotalRepetitions > 1 && 
                                reminder.currentRepetitionIndex < (reminder.nagTotalRepetitions - 1)) {
                                
                                // CASE A: Sequence NOT Finished
                                val interval = if (reminder.nagIntervalInMillis != null && reminder.nagIntervalInMillis > 0) {
                                    reminder.nagIntervalInMillis
                                } else {
                                    com.nami.peace.util.DebugLogger.log("Warning: Interval is null or 0. Defaulting to 15 mins.")
                                    15 * 60 * 1000L
                                }

                                val now = System.currentTimeMillis()
                                var nextTime: Long
                                var nextRepIndex = reminder.currentRepetitionIndex + 1

                                if (reminder.isStrictSchedulingEnabled) {
                                    // STRICT (Anchored)
                                    // Calculate based on Original Start Time + (RepIndex * Interval)
                                    // RepIndex is 0-based. Next rep is 'nextRepIndex'.
                                    nextTime = reminder.originalStartTimeInMillis + (nextRepIndex * interval)
                                    
                                    // Catch-up loop
                                    // If the calculated nextTime is already in the past, skip it and move to the next rep.
                                    while (nextTime <= now && nextRepIndex < reminder.nagTotalRepetitions) {
                                        com.nami.peace.util.DebugLogger.log("Strict Mode: Catching up. Skipping rep $nextRepIndex at $nextTime")
                                        nextRepIndex++
                                        nextTime = reminder.originalStartTimeInMillis + (nextRepIndex * interval)
                                    }
                                    
                                    if (nextRepIndex >= reminder.nagTotalRepetitions) {
                                        com.nami.peace.util.DebugLogger.log("Strict Mode: Skipped all remaining repetitions due to delay. Tracking as Missed.")
                                        repository.insertHistory(reminder.copy(isAbandoned = true))
                                        repository.setTaskCompleted(reminderId, true)
                                        stopService(context)
                                        pendingResult.finish()
                                        return@launch
                                    }

                                } else {
                                    // FLEXIBLE (Drift)
                                    nextTime = now + interval
                                }
                                
                                val updatedReminder = reminder.copy(
                                    currentRepetitionIndex = nextRepIndex,
                                    isInNestedSnoozeLoop = false,
                                    nestedSnoozeStartTime = null,
                                    startTimeInMillis = nextTime,
                                    originalStartTimeInMillis = reminder.originalStartTimeInMillis
                                )
                                repository.updateReminder(updatedReminder)
                                
                                alarmScheduler.schedule(updatedReminder, nextTime)
                                com.nami.peace.util.DebugLogger.log("Scheduled Next Repetition (Strict=${reminder.isStrictSchedulingEnabled}) at $nextTime")
                                
                            } else {
                                // CASE B: Sequence Finished OR Standard Stop
                                com.nami.peace.util.DebugLogger.log("Sequence Finished for this instance. Checking Re-scheduling...")
                                
                                val isOneTime = reminder.recurrenceType == com.nami.peace.domain.model.RecurrenceType.ONE_TIME
                                
                                val updatedReminder = if (isOneTime) {
                                    reminder.copy(
                                        isCompleted = true,
                                        isEnabled = false,
                                        completedTime = System.currentTimeMillis()
                                    )
                                } else {
                                    // RECURRING: Reset nag counters and move to next day/time
                                    val nextFullOccurrence = com.nami.peace.util.DateUtils.getNextOccurrence(
                                        reminder.originalStartTimeInMillis, 
                                        reminder.recurrenceType, 
                                        reminder.daysOfWeek
                                    )
                                    reminder.copy(
                                        currentRepetitionIndex = 0,
                                        isInNestedSnoozeLoop = false,
                                        nestedSnoozeStartTime = null,
                                        startTimeInMillis = nextFullOccurrence,
                                        originalStartTimeInMillis = nextFullOccurrence // Move anchor too
                                    )
                                }
                                
                                repository.updateReminder(updatedReminder)
                                
                                if (isOneTime) {
                                    alarmScheduler.cancel(reminder)
                                } else {
                                    alarmScheduler.schedule(updatedReminder, updatedReminder.startTimeInMillis)
                                    com.nami.peace.util.DebugLogger.log("Re-scheduled Recurring Alarm: ${reminder.title} at ${updatedReminder.startTimeInMillis}")
                                }
                            }
                        }
                        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                        notificationManager.cancel(reminderId)
                        stopService(context)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            "com.nami.peace.ACTION_SNOOZE" -> {
                scope.launch {
                    try {
                        val reminder = repository.getReminderById(reminderId)
                        if (reminder != null) {
                            val now = System.currentTimeMillis()
                            val snoozeStart = reminder.nestedSnoozeStartTime ?: now
                            
                            // Check Timeout (30 Minutes)
                            if ((now - snoozeStart) > (30 * 60 * 1000L)) {
                                com.nami.peace.util.DebugLogger.log("Nag Loop Timeout. User unresponsive. Breaking Loop.")
                                
                                if (reminder.nagTotalRepetitions > 1 && 
                                    reminder.currentRepetitionIndex < (reminder.nagTotalRepetitions - 1)) {
                                    
                                    val interval = if (reminder.nagIntervalInMillis != null && reminder.nagIntervalInMillis > 0) {
                                        reminder.nagIntervalInMillis
                                    } else {
                                        15 * 60 * 1000L
                                    }
                                    
                                    val nextTime = reminder.startTimeInMillis + interval
                                    
                                    val updatedReminder = reminder.copy(
                                        currentRepetitionIndex = reminder.currentRepetitionIndex + 1,
                                        isInNestedSnoozeLoop = false,
                                        nestedSnoozeStartTime = null,
                                        startTimeInMillis = nextTime
                                    )
                                    repository.updateReminder(updatedReminder)
                                    alarmScheduler.schedule(updatedReminder, nextTime)
                                    com.nami.peace.util.DebugLogger.log("Scheduled Next Repetition (Timeout Recovery) at $nextTime")
                                    
                                } else {
                                    com.nami.peace.util.DebugLogger.log("Nag Loop Timeout on Last Repetition. Marking Missed.")
                                    repository.insertHistory(reminder.copy(isAbandoned = true))
                                    repository.setTaskCompleted(reminderId, true)
                                }
                                
                            } else {
                                // NO TIMEOUT - Continue Panic Loop
                                val snoozeTime = now + (2 * 60 * 1000L) // 120,000ms
                                
                                val updatedReminder = reminder.copy(
                                    isInNestedSnoozeLoop = true,
                                    nestedSnoozeStartTime = snoozeStart
                                )
                                repository.updateReminder(updatedReminder)
                                
                                alarmScheduler.schedule(updatedReminder, snoozeTime)
                                com.nami.peace.util.DebugLogger.log("Snoozed (Nag Mode). Next panic in 2 mins.")
                            }
                        }
                        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                        nm.cancel(reminderId)
                        stopService(context)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            "com.nami.peace.ACTION_ALARM_TRIGGER" -> {
                scope.launch {
                    try {
                        val reminder = repository.getReminderById(reminderId)
                        if (reminder != null) {
                            val now = System.currentTimeMillis()
                            var shouldPlayAlarm = true
                            
                            if (reminder.isInNestedSnoozeLoop) {
                                val snoozeStart = reminder.nestedSnoozeStartTime ?: now
                                if ((now - snoozeStart) > (30 * 60 * 1000L)) {
                                    com.nami.peace.util.DebugLogger.log("Nag Loop Timeout inside Trigger. User unresponsive.")
                                    shouldPlayAlarm = false
                                    
                                    if (reminder.nagTotalRepetitions > 1 && 
                                        reminder.currentRepetitionIndex < (reminder.nagTotalRepetitions - 1)) {
                                        
                                        val interval = if (reminder.nagIntervalInMillis != null && reminder.nagIntervalInMillis > 0) {
                                            reminder.nagIntervalInMillis
                                        } else {
                                            15 * 60 * 1000L
                                        }
                                        
                                        val nextTime = reminder.startTimeInMillis + interval
                                        
                                        val updatedReminder = reminder.copy(
                                            currentRepetitionIndex = reminder.currentRepetitionIndex + 1,
                                            isInNestedSnoozeLoop = false,
                                            nestedSnoozeStartTime = null,
                                            startTimeInMillis = nextTime
                                        )
                                        repository.updateReminder(updatedReminder)
                                        alarmScheduler.schedule(updatedReminder, nextTime)
                                        com.nami.peace.util.DebugLogger.log("Scheduled Next Repetition (Timeout Trigger) at $nextTime")
                                        
                                    } else {
                                        com.nami.peace.util.DebugLogger.log("Nag Loop Timeout on Last Repetition (Trigger). Marking Missed.")
                                        repository.insertHistory(reminder.copy(isAbandoned = true))
                                        repository.setTaskCompleted(reminderId, true)
                                    }
                                }
                            }
                            
                            if (shouldPlayAlarm) {
                                // Find all reminders due at the same time (within 1 minute window)
                                val allReminders = repository.getIncompleteReminders()
                                val timeWindow = 60 * 1000L // 1 minute
                                val bundledReminderIds = allReminders
                                    .filter { 
                                        kotlin.math.abs(it.startTimeInMillis - reminder.startTimeInMillis) < timeWindow &&
                                        !it.isCompleted && it.isEnabled
                                    }
                                    .sortedBy { it.priority.ordinal } // HIGH=0, MEDIUM=1, LOW=2
                                    .map { it.id }
                                
                                com.nami.peace.util.DebugLogger.log("Bundled ${bundledReminderIds.size} reminders for simultaneous alarm")
                                
                                // --- LOGIC FOR NOTIFICATION vs FULL SCREEN ---
                                val notifOn = userPrefs.notificationsEnabled.first()
                                val qhOn = userPrefs.quietHoursEnabled.first()
                                val qhStart = userPrefs.quietHoursStart.first()
                                val qhEnd = userPrefs.quietHoursEnd.first()
                                
                                val inQuietHours = qhOn && isNowInQuietHours(qhStart, qhEnd)
                                
                                if (!notifOn || inQuietHours) {
                                    com.nami.peace.util.DebugLogger.log("Direct Full Screen Trigger (Notif Off: ${!notifOn}, QuietHours: $inQuietHours)")
                                    val activityIntent = Intent(context, AlarmActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        putExtra("REMINDER_ID", reminderId)
                                        putIntegerArrayListExtra("BUNDLED_REMINDER_IDS", ArrayList(bundledReminderIds))
                                    }
                                    context.startActivity(activityIntent)
                                }
                                
                                val serviceIntent = Intent(context, com.nami.peace.scheduler.ReminderService::class.java).apply {
                                    putExtra("REMINDER_ID", reminderId)
                                    putIntegerArrayListExtra("BUNDLED_REMINDER_IDS", ArrayList(bundledReminderIds))
                                    putExtra("SILENT_NOTIF", !notifOn || inQuietHours)
                                }
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    context.startForegroundService(serviceIntent)
                                } else {
                                    context.startService(serviceIntent)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    private fun isNowInQuietHours(start: String, end: String): Boolean {
        try {
            val now = LocalTime.now()
            val startTime = LocalTime.parse(start)
            val endTime = LocalTime.parse(end)
            return if (startTime <= endTime) {
                now in startTime..endTime
            } else {
                now >= startTime || now <= endTime
            }
        } catch (e: Exception) {
            return false
        }
    }

    private fun stopService(context: Context) {
        val serviceIntent = Intent(context, com.nami.peace.scheduler.ReminderService::class.java)
        context.stopService(serviceIntent)
    }
}