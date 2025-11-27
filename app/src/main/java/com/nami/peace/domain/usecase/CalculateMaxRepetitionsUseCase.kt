package com.nami.peace.domain.usecase

import javax.inject.Inject
import java.util.Calendar

class CalculateMaxRepetitionsUseCase @Inject constructor() {
    operator fun invoke(startTimeInMillis: Long, intervalInMillis: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startTimeInMillis
        
        // Calculate midnight of the same day (or next day if start time is late?)
        // The rule is "Midnight Wall". Repetitions stop at midnight.
        // So we need to calculate time from startTime until midnight.
        
        val startHour = calendar.get(Calendar.HOUR_OF_DAY)
        val startMinute = calendar.get(Calendar.MINUTE)
        
        // Midnight is 24:00 (or 00:00 of next day)
        // Let's calculate remaining milliseconds in the day.
        
        val midnightCalendar = Calendar.getInstance()
        midnightCalendar.timeInMillis = startTimeInMillis
        midnightCalendar.set(Calendar.HOUR_OF_DAY, 0)
        midnightCalendar.set(Calendar.MINUTE, 0)
        midnightCalendar.set(Calendar.SECOND, 0)
        midnightCalendar.set(Calendar.MILLISECOND, 0)
        midnightCalendar.add(Calendar.DAY_OF_YEAR, 1) // Next day 00:00
        
        val midnightMillis = midnightCalendar.timeInMillis
        val diff = midnightMillis - startTimeInMillis
        
        if (diff <= 0) return 0
        
        // Max repetitions = diff / interval
        return (diff / intervalInMillis).toInt()
    }
}
