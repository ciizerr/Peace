package com.nami.peace.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    fun isToday(timeInMillis: Long): Boolean {
        val calendar = Calendar.getInstance()
        val todayYear = calendar.get(Calendar.YEAR)
        val todayDay = calendar.get(Calendar.DAY_OF_YEAR)

        calendar.timeInMillis = timeInMillis
        return calendar.get(Calendar.YEAR) == todayYear &&
                calendar.get(Calendar.DAY_OF_YEAR) == todayDay
    }

    fun isTomorrow(timeInMillis: Long): Boolean {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrowYear = calendar.get(Calendar.YEAR)
        val tomorrowDay = calendar.get(Calendar.DAY_OF_YEAR)

        calendar.timeInMillis = timeInMillis
        return calendar.get(Calendar.YEAR) == tomorrowYear &&
                calendar.get(Calendar.DAY_OF_YEAR) == tomorrowDay
    }

    fun isYesterday(timeInMillis: Long): Boolean {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayYear = calendar.get(Calendar.YEAR)
        val yesterdayDay = calendar.get(Calendar.DAY_OF_YEAR)

        calendar.timeInMillis = timeInMillis
        return calendar.get(Calendar.YEAR) == yesterdayYear &&
                calendar.get(Calendar.DAY_OF_YEAR) == yesterdayDay
    }

    fun formatDateHeader(timeInMillis: Long): String {
        if (isToday(timeInMillis)) return "Today"
        if (isTomorrow(timeInMillis)) return "Tomorrow"
        
        val sdf = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
        return sdf.format(Date(timeInMillis))
    }

    fun getNextOccurrence(currentStartTime: Long, recurrenceType: com.nami.peace.domain.model.RecurrenceType, daysOfWeek: List<Int>): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentStartTime
        val now = Calendar.getInstance()

        // Ensure we advance from the original time
        when (recurrenceType) {
            com.nami.peace.domain.model.RecurrenceType.DAILY -> {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            com.nami.peace.domain.model.RecurrenceType.WEEKLY -> {
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
            }
            com.nami.peace.domain.model.RecurrenceType.CUSTOM -> {
                if (daysOfWeek.isEmpty()) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                } else {
                    // Find next selected day
                    var count = 0
                    while (count < 7) {
                        calendar.add(Calendar.DAY_OF_YEAR, 1)
                        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                        if (daysOfWeek.contains(dayOfWeek)) break
                        count++
                    }
                }
            }
            com.nami.peace.domain.model.RecurrenceType.ONE_TIME -> return currentStartTime
        }

        // If after incrementing it's still in the past (e.g. user missed many days),
        // we should probably sync it to the next valid date from "now"
        if (calendar.before(now)) {
            // Set to same time today or tomorrow if needed
            val timeCal = Calendar.getInstance()
            timeCal.timeInMillis = currentStartTime
            
            calendar.timeInMillis = now.timeInMillis
            calendar.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
            calendar.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            
            if (calendar.before(now)) {
                return getNextOccurrence(calendar.timeInMillis, recurrenceType, daysOfWeek)
            }
        }

        return calendar.timeInMillis
    }
}
