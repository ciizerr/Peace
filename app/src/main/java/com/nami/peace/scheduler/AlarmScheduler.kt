package com.nami.peace.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.nami.peace.domain.model.RecurrenceType
import com.nami.peace.domain.model.Reminder
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject

class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun calculateNextTriggerTime(reminder: Reminder): Long {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = reminder.startTimeInMillis
        
        // Extract Time Components
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        
        return when (reminder.recurrenceType) {
            RecurrenceType.ONE_TIME -> {
                if (reminder.dateInMillis != null) {
                    // Specific Date
                    val dateCalendar = Calendar.getInstance()
                    dateCalendar.timeInMillis = reminder.dateInMillis
                    dateCalendar.set(Calendar.HOUR_OF_DAY, hour)
                    dateCalendar.set(Calendar.MINUTE, minute)
                    dateCalendar.set(Calendar.SECOND, 0)
                    dateCalendar.set(Calendar.MILLISECOND, 0)
                    
                    if (dateCalendar.timeInMillis <= now) {
                        // If specific date is in past, it's in past.
                        dateCalendar.timeInMillis
                    } else {
                        dateCalendar.timeInMillis
                    }
                } else {
                    // Legacy One-Time (Today or Tomorrow)
                    val todayCalendar = Calendar.getInstance()
                    todayCalendar.set(Calendar.HOUR_OF_DAY, hour)
                    todayCalendar.set(Calendar.MINUTE, minute)
                    todayCalendar.set(Calendar.SECOND, 0)
                    todayCalendar.set(Calendar.MILLISECOND, 0)
                    
                    if (todayCalendar.timeInMillis <= now) {
                        todayCalendar.add(Calendar.DAY_OF_YEAR, 1)
                    }
                    todayCalendar.timeInMillis
                }
            }
            RecurrenceType.DAILY -> {
                val todayCalendar = Calendar.getInstance()
                todayCalendar.set(Calendar.HOUR_OF_DAY, hour)
                todayCalendar.set(Calendar.MINUTE, minute)
                todayCalendar.set(Calendar.SECOND, 0)
                todayCalendar.set(Calendar.MILLISECOND, 0)
                
                if (todayCalendar.timeInMillis <= now) {
                    todayCalendar.add(Calendar.DAY_OF_YEAR, 1)
                }
                todayCalendar.timeInMillis
            }
            RecurrenceType.WEEKLY -> {
                // Find next matching day
                val todayCalendar = Calendar.getInstance()
                todayCalendar.set(Calendar.HOUR_OF_DAY, hour)
                todayCalendar.set(Calendar.MINUTE, minute)
                todayCalendar.set(Calendar.SECOND, 0)
                todayCalendar.set(Calendar.MILLISECOND, 0)
                
                val currentDay = todayCalendar.get(Calendar.DAY_OF_WEEK) // 1=Sun, 7=Sat
                val targetDays = if (reminder.daysOfWeek.isNotEmpty()) reminder.daysOfWeek.sorted() else listOf(currentDay)
                
                // Find next day in list
                var daysToAdd = 0
                var found = false
                
                // Check today first if time is future
                if (targetDays.contains(currentDay) && todayCalendar.timeInMillis > now) {
                    daysToAdd = 0
                    found = true
                } else {
                    // Look for next day
                    for (i in 1..7) {
                        val nextDay = (currentDay + i - 1) % 7 + 1
                        if (targetDays.contains(nextDay)) {
                            daysToAdd = i
                            found = true
                            break
                        }
                    }
                }
                
                if (!found) {
                    // Fallback (shouldn't happen if list not empty)
                    if (todayCalendar.timeInMillis <= now) daysToAdd = 7 else 0
                }
                
                todayCalendar.add(Calendar.DAY_OF_YEAR, daysToAdd)
                todayCalendar.timeInMillis
            }
            else -> {
                 // Default fallback
                 if (calendar.timeInMillis <= now) calendar.add(Calendar.DAY_OF_YEAR, 1)
                 calendar.timeInMillis
            }
        }
    }

    fun schedule(reminder: Reminder, manualTime: Long? = null) {
        val triggerTime = if (manualTime != null) {
            manualTime
        } else {
            calculateNextTriggerTime(reminder)
        }

        // Zeroing Logic
        val calendar = Calendar.getInstance().apply {
            timeInMillis = triggerTime
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val alarmTime = calendar.timeInMillis
        val now = System.currentTimeMillis()

        com.nami.peace.util.DebugLogger.log("Scheduling Alarm for Reminder: ${reminder.title} (ID: ${reminder.id}) at $alarmTime")

        if (alarmTime <= now) {
            val diff = now - alarmTime
            if (diff < 60 * 1000L) {
                com.nami.peace.util.DebugLogger.log("Alarm time is slightly in the past ($diff ms). Scheduling immediately.")
                scheduleExact(now + 100, reminder)
            } else {
                com.nami.peace.util.DebugLogger.log("Alarm time is too far in the past ($diff ms). Skipping.")
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } else {
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
