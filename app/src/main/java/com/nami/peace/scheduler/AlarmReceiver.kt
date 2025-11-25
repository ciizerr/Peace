package com.nami.peace.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nami.peace.domain.repository.ReminderRepository
import com.nami.peace.domain.usecase.SnoozeReminderUseCase
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
        val reminderId = intent.getIntExtra("REMINDER_ID", -1)
        if (reminderId == -1) return

        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            try {
                val reminder = repository.getReminderById(reminderId) ?: return@launch
                
                // Trigger the snooze/nag logic
                // This UseCase will handle:
                // 1. Showing Notification
                // 2. Updating State (entering nested loop)
                // 3. Scheduling next micro-loop
                snoozeReminderUseCase(reminder, context)
                
            } finally {
                pendingResult.finish()
            }
        }
    }
}
