package com.nami.peace.domain.usecase

import com.nami.peace.domain.repository.GardenRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Use case for checking if the streak should be reset due to missed days.
 * 
 * This is useful for:
 * - Displaying accurate streak information
 * - Resetting streak when user opens app after missing days
 * - Validating streak status before displaying
 */
class CheckStreakStatusUseCase @Inject constructor(
    private val gardenRepository: GardenRepository
) {
    /**
     * Checks if the current streak is still valid or should be reset.
     * 
     * @param currentTime The current timestamp (defaults to System.currentTimeMillis())
     * @return Pair of (streakIsValid: Boolean, currentStreak: Int)
     */
    suspend operator fun invoke(currentTime: Long = System.currentTimeMillis()): Pair<Boolean, Int> {
        val currentState = gardenRepository.getGardenStateOnce()
        
        // No state or no previous completion - streak is 0
        if (currentState == null || currentState.lastCompletionDate == null) {
            return Pair(true, 0)
        }
        
        val daysSinceLastCompletion = calculateDaysBetween(
            currentState.lastCompletionDate,
            currentTime
        )
        
        // Streak is valid if last completion was today or yesterday
        val streakIsValid = daysSinceLastCompletion <= 1
        
        // If streak is invalid, reset it
        if (!streakIsValid && currentState.currentStreak > 0) {
            val updatedState = currentState.copy(currentStreak = 0)
            gardenRepository.updateGardenState(updatedState)
            return Pair(false, 0)
        }
        
        return Pair(streakIsValid, currentState.currentStreak)
    }
    
    /**
     * Calculates the number of days between two timestamps.
     * 
     * @param fromTime The earlier timestamp
     * @param toTime The later timestamp
     * @return The number of complete days between the timestamps
     */
    private fun calculateDaysBetween(fromTime: Long, toTime: Long): Int {
        // Convert to start of day for accurate day counting
        val fromDay = fromTime / TimeUnit.DAYS.toMillis(1)
        val toDay = toTime / TimeUnit.DAYS.toMillis(1)
        return (toDay - fromDay).toInt()
    }
}
