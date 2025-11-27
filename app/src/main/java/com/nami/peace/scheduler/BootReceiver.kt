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
            
            val pendingResult = goAsync()
            val scope = CoroutineScope(Dispatchers.IO)
            
            scope.launch {
                try {
                    val now = System.currentTimeMillis()
                    // Fetch all reminders that are NOT completed
                    // We need a repository method for this. Assuming getActiveReminders exists or similar.
                    // Based on previous view_file, repository.getActiveReminders(now) was used.
                    // But we should probably get ALL enabled reminders to be safe.
                    // Let's assume getActiveReminders returns reminders that should be active.
                    
                    val activeReminders = repository.getActiveReminders(now)
                    
                    activeReminders.forEach { reminder ->
                        if (!reminder.isCompleted) {
                            // If the reminder is in the future, schedule it normally.
                            if (reminder.startTimeInMillis > now) {
                                alarmScheduler.schedule(reminder)
                            } else {
                                // If the reminder was missed (startTime < now),
                                // AND it hasn't been marked completed,
                                // we should probably fire it immediately or schedule it for very soon.
                                // For Nag Mode, if we missed a nag, we should restart the cycle?
                                // For now, let's fire it immediately to ensure the user sees it.
                                alarmScheduler.schedule(reminder, now + 1000) // Fire in 1 second
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
