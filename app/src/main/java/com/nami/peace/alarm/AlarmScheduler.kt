package com.nami.peace.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.nami.peace.data.Reminder
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

interface AlarmScheduler {
    fun schedule(reminder: Reminder)
    fun cancel(reminder: Reminder)
}

class AndroidAlarmScheduler(
    private val context: Context
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(reminder: Reminder) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("EXTRA_MESSAGE", reminder.title)
            putExtra("EXTRA_ID", reminder.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(), // ID is Long now, need to cast to Int for PendingIntent
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calculate trigger time
        val calendar = Calendar.getInstance().apply {
            timeInMillis = reminder.timeInMillis
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // If the time is just a time of day (e.g. created with today's date but for 8am and it's now 9am),
        // we need to make sure it's for tomorrow if it's already passed.
        // However, if timeInMillis is a full timestamp (Date + Time), we should respect it.
        // For this prototype, let's assume timeInMillis is the next occurrence.
        
        // Logic adjustment: If we are just storing "Time of Day", we need to adjust the date.
        // But since we switched to timeInMillis, let's assume the ViewModel sets the correct next occurrence.
        // If it's in the past, maybe add a day? 
        // Let's stick to the previous logic: if it's in the past, add a day.
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        Log.d("AlarmScheduler", "Scheduling alarm for: ${calendar.time}")

        try {
            // Use setAlarmClock for high reliability and visibility (shows icon)
            val alarmClockInfo = AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "SecurityException: ${e.message}")
            // Fallback
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    override fun cancel(reminder: Reminder) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun parseTime(timeString: String): Pair<Int, Int>? {
        val cleanedTime = timeString.trim().uppercase()
        return try {
            // Try parsing "08:00 AM" or "8:00 AM"
            val formatter = DateTimeFormatter.ofPattern("h:mm a")
            val time = LocalTime.parse(cleanedTime, formatter)
            Pair(time.hour, time.minute)
        } catch (e: Exception) {
            try {
                // Try parsing "HH:mm"
                val formatter = DateTimeFormatter.ofPattern("H:mm")
                val time = LocalTime.parse(cleanedTime, formatter)
                Pair(time.hour, time.minute)
            } catch (e2: Exception) {
                Log.e("AlarmScheduler", "Failed to parse time: $timeString")
                null
            }
        }
    }
}
