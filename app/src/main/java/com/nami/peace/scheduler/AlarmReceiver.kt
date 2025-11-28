package com.nami.peace.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nami.peace.data.local.HistoryDao
import com.nami.peace.data.local.HistoryEntity
import com.nami.peace.data.repository.UserPreferencesRepository
import com.nami.peace.domain.repository.ReminderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: ReminderRepository
    
    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var historyDao: HistoryDao

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onReceive(context: Context, intent: Intent) {
        com.nami.peace.util.DebugLogger.log("Receiver Woke Up! Action: ${intent.action}")

        val reminderId = intent.getIntExtra("REMINDER_ID", -1)
        if (reminderId == -1) return

        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO)

        when (intent.action) {
            "com.nami.peace.ACTION_STOP_SOUND" -> {
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
                            historyDao.insert(
                                HistoryEntity(
                                    originalTitle = reminder.title,
                                    completedTime = System.currentTimeMillis(),
                                    status = "Done"
                                )
                            )

                            if (reminder.nagTotalRepetitions > 1 && 
                                reminder.currentRepetitionIndex < (reminder.nagTotalRepetitions - 1)) {
                                
                                // CASE A: Sequence NOT Finished
                                val interval = if (reminder.nagIntervalInMillis != null && reminder.nagIntervalInMillis > 0) {
                                    reminder.nagIntervalInMillis
                                } else {
                                    com.nami.peace.util.DebugLogger.log("Warning: Interval is null or 0. Defaulting to 15 mins.")
                                    15 * 60 * 1000L
                                }

                                val schedulingMode = userPreferencesRepository.schedulingMode.first()
                                val now = System.currentTimeMillis()
                                var nextTime: Long
                                var nextRepIndex = reminder.currentRepetitionIndex + 1

                                if (schedulingMode == "STRICT") {
                                    // STRICT (Anchored)
                                    nextTime = reminder.startTimeInMillis + interval
                                    
                                    // Catch-up loop
                                    while (nextTime <= now && nextRepIndex < reminder.nagTotalRepetitions) {
                                        nextTime += interval
                                        nextRepIndex++
                                    }
                                    
                                    if (nextRepIndex >= reminder.nagTotalRepetitions) {
                                        com.nami.peace.util.DebugLogger.log("Strict Mode: Skipped all remaining repetitions due to delay.")
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
                                    startTimeInMillis = nextTime
                                )
                                repository.updateReminder(updatedReminder)
                                
                                alarmScheduler.schedule(updatedReminder, nextTime)
                                com.nami.peace.util.DebugLogger.log("Scheduled Next Repetition ($schedulingMode) at $nextTime")
                                
                            } else {
                                // CASE B: Sequence Finished
                                com.nami.peace.util.DebugLogger.log("Sequence Finished. Marking Task Complete.")
                                repository.setTaskCompleted(reminderId, true)
                            }
                        }
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
                                    com.nami.peace.util.DebugLogger.log("Nag Loop Timeout on Last Repetition. Marking Complete.")
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
                                        com.nami.peace.util.DebugLogger.log("Nag Loop Timeout on Last Repetition (Trigger). Marking Complete.")
                                        repository.setTaskCompleted(reminderId, true)
                                    }
                                }
                            }
                            
                            if (shouldPlayAlarm) {
                                val serviceIntent = Intent(context, com.nami.peace.scheduler.ReminderService::class.java).apply {
                                    putExtra("REMINDER_ID", reminderId)
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

    private fun stopService(context: Context) {
        val serviceIntent = Intent(context, com.nami.peace.scheduler.ReminderService::class.java)
        context.stopService(serviceIntent)
    }
}