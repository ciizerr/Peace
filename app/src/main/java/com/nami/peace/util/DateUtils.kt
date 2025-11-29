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

    fun formatDateHeader(timeInMillis: Long): String {
        if (isToday(timeInMillis)) return "Today"
        if (isTomorrow(timeInMillis)) return "Tomorrow"
        
        val sdf = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
        return sdf.format(Date(timeInMillis))
    }
}
