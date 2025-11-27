package com.nami.peace.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.nami.peace.domain.model.Reminder
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(reminder: Reminder, triggerTime: Long? = null) {
        // Use provided triggerTime or the reminder's startTime
        // Fix: Zeroing Logic for Precision
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = triggerTime ?: reminder.startTimeInMillis
            set(java.util.Calendar.SECOND, 0)      // Force to :00
            set(java.util.Calendar.MILLISECOND, 0) // Force to .000
        }
        val alarmTime = calendar.timeInMillis
        val now = System.currentTimeMillis()

        com.nami.peace.util.DebugLogger.log("Scheduling Alarm for Reminder: ${reminder.title} (ID: ${reminder.id}) at $alarmTime")

        // Fix A: Millisecond Precision & Buffer
        // If the time is "in the past" by less than 1 minute, schedule it immediately.
        if (alarmTime <= now) {
            val diff = now - alarmTime
            if (diff < 60 * 1000L) {
                com.nami.peace.util.DebugLogger.log("Alarm time is slightly in the past ($diff ms). Scheduling immediately.")
                // Schedule for now + 100ms to ensure it fires
                scheduleExact(now + 100, reminder)
                return
            } else {
                com.nami.peace.util.DebugLogger.log("Alarm time is too far in the past ($diff ms). Skipping or handling as missed.")
                // If it's too far in the past, we might want to skip or fire immediately depending on logic.
                // For now, let's fire immediately if it's within reason, or just let the standard logic handle it.
                // But the requirement says "If the time is 'in the past' by less than 1 minute, schedule it immediately instead of rejecting it."
                // So we handled that. If it's MORE than 1 minute, we probably shouldn't schedule it for the past.
                // However, for testing, let's assume we want to fire it if it's not completed.
                // But let's stick to the strict "1 minute buffer" rule for the "Fix".
            }
        } else {
             scheduleExact(alarmTime, reminder)
        }
    }

    private fun scheduleExact(triggerAtMillis: Long, reminder: Reminder) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.nami.peace.ACTION_ALARM_TRIGGER"
            putExtra("REMINDER_ID", reminder.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Fix B: The "Can Schedule" Check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                com.nami.peace.util.DebugLogger.log("Permission Granted. Scheduling Exact Alarm at $triggerAtMillis")
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                com.nami.peace.util.DebugLogger.log("Permission DENIED. Cannot schedule exact alarm.")
                // We should probably notify the user or UI somehow.
                // The UI will check this permission separately to show the banner.
                // Here we can fallback to inexact or just fail/log.
                // Let's try setAndAllowWhileIdle as fallback (it's not exact but better than nothing)
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } else {
            com.nami.peace.util.DebugLogger.log("Scheduling Exact Alarm (Pre-Android 12) at $triggerAtMillis")
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancel(reminder: Reminder) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
