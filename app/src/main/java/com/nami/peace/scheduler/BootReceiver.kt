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
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            val scope = CoroutineScope(Dispatchers.IO)
            
            scope.launch {
                try {
                    val now = System.currentTimeMillis()
                    val activeReminders = repository.getActiveReminders(now)
                    
                    activeReminders.forEach { reminder ->
                        // Reschedule future alarms
                        // If alarm was missed during downtime, it should fire immediately or handled
                        // Logic: If startTime > now, schedule at startTime
                        // If startTime <= now, and not completed, it's missed. Fire immediately?
                        // For Nag Mode, we should probably fire immediately if it was due.
                        
                        val triggerTime = if (reminder.startTimeInMillis > now) {
                            reminder.startTimeInMillis
                        } else {
                            // Missed. Fire now.
                            now
                        }
                        
                        alarmScheduler.schedule(reminder, triggerTime)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
