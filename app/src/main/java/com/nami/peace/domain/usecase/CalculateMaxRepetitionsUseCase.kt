package com.nami.peace.domain.usecase

import java.util.Calendar
import javax.inject.Inject

class CalculateMaxRepetitionsUseCase @Inject constructor() {

    /**
     * Calculates the maximum number of repetitions allowed before the end of the day.
     * Rule: All repetitions must complete within the same calendar day.
     *
     * @param startTimeInMillis The start time of the reminder.
     * @param intervalInMillis The interval between repetitions.
     * @return The maximum number of repetitions allowed (0 means no repetitions, just the initial alarm).
     */
    operator fun invoke(startTimeInMillis: Long, intervalInMillis: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startTimeInMillis

        val startDay = calendar.get(Calendar.DAY_OF_YEAR)
        val startYear = calendar.get(Calendar.YEAR)

        // Calculate the end of the day (23:59:59.999)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDayInMillis = calendar.timeInMillis

        // If start time is somehow after end of day (shouldn't happen if logic is correct elsewhere), return 0
        if (startTimeInMillis > endOfDayInMillis) return 0

        val remainingTime = endOfDayInMillis - startTimeInMillis
        
        // If interval is 0 or negative, avoid division by zero
        if (intervalInMillis <= 0) return 0

        // Calculate how many intervals fit in the remaining time
        // Example: 10 PM (22:00), Interval 1h. End of day 23:59.
        // Remaining: ~2h.
        // 22:00 (Initial) -> 23:00 (1st Rep) -> 24:00 (Next day, invalid).
        // So max repetitions = 1.
        
        return (remainingTime / intervalInMillis).toInt()
    }
}
