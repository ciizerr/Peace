package com.nami.peace.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.nami.peace.domain.repository.ReminderRepository
import com.nami.peace.domain.usecase.SnoozeReminderUseCase
import com.nami.peace.ui.alarm.AlarmActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: ReminderRepository
    
    @Inject
    lateinit var snoozeReminderUseCase: SnoozeReminderUseCase

    override fun onReceive(context: Context, intent: Intent) {
        com.nami.peace.util.DebugLogger.log("Receiver Woke Up! Action: ${intent.action}")

        if (intent.action == "com.nami.peace.ACTION_STOP_SOUND") {
            // Stop the Service
            val serviceIntent = Intent(context, com.nami.peace.scheduler.ReminderService::class.java)
            context.stopService(serviceIntent)
            return
        }
        
        // Verify Action for Alarm Trigger
        if (intent.action != "com.nami.peace.ACTION_ALARM_TRIGGER") {
            com.nami.peace.util.DebugLogger.log("Warning: Unknown action received: ${intent.action}")
        }

        val reminderId = intent.getIntExtra("REMINDER_ID", -1)
        if (reminderId == -1) {
            com.nami.peace.util.DebugLogger.log("Error: No Reminder ID found in intent.")
            return
        }

        // Start Foreground Service to handle Alarm
        val serviceIntent = Intent(context, com.nami.peace.scheduler.ReminderService::class.java).apply {
            putExtra("REMINDER_ID", reminderId)
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
        com.nami.peace.util.DebugLogger.log("AlarmReceiver: Started ReminderService.")
    }
}
