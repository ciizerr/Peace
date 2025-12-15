package com.nami.peace.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nami.peace.domain.repository.ReminderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: ReminderRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            // Acquire Partial WakeLock
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            val wakeLock = powerManager.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "Peace:BootReceiverWakeLock")
            wakeLock.acquire(60 * 1000L) // 1 Minute Timeout

            val pendingResult = goAsync()
            val scope = CoroutineScope(Dispatchers.IO)
            
            scope.launch {
                try {
                    val now = System.currentTimeMillis()
                    val gracePeriod = 15 * 60 * 1000L // 15 Minutes

                    // Fetch all reminders that are NOT completed
                    val incompleteReminders = repository.getIncompleteReminders()
                    
                    incompleteReminders.forEach { reminder ->
                        if (reminder.startTimeInMillis > now) {
                            // Future: Schedule normally
                            alarmScheduler.schedule(reminder)
                        } else {
                            // Past: Check if it's "Fresh" or "Stale"
                            val diff = now - reminder.startTimeInMillis
                            if (diff < gracePeriod) {
                                // Fresh: Fire immediately (Phone just rebooted in time)
                                // Schedule for now + 100ms to ensure it fires
                                alarmScheduler.schedule(reminder, now + 100)
                            } else {
                                // Stale: Ignore (Don't annoy user with old alarms)
                                com.nami.peace.util.DebugLogger.log("Skipping stale alarm: ${reminder.title}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    if (wakeLock.isHeld) wakeLock.release()
                    pendingResult.finish()
                }
            }
        }
    }
}
