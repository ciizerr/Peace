package com.nami.peace.domain.usecase

import com.nami.peace.domain.model.GardenState
import com.nami.peace.domain.repository.GardenRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Use case for updating the streak when a task is completed.
 * 
 * Handles:
 * - Incrementing streak on consecutive day completions
 * - Resetting streak when a day is missed
 * - Tracking longest streak
 * - Updating last completion date
 */
class UpdateStreakUseCase @Inject constructor(
    private val gardenRepository: GardenRepository
) {
    /**
     * Updates the streak based on task completion.
     * 
     * @param completionTime The timestamp of the task completion (defaults to current time)
     * @return Pair of (streakIncremented: Boolean, newStreak: Int)
     */
    suspend operator fun invoke(completionTime: Long = System.currentTimeMillis()): Pair<Boolean, Int> {
        val currentState = gardenRepository.getGardenStateOnce() ?: GardenState()
        
        val (newStreak, streakIncremented) = calculateNewStreak(
            currentStreak = currentState.currentStreak,
            lastCompletionDate = currentState.lastCompletionDate,
            completionTime = completionTime
        )
        
        val newLongestStreak = maxOf(currentState.longestStreak, newStreak)
        
        val updatedState = currentState.copy(
            currentStreak = newStreak,
            longestStreak = newLongestStreak,
            lastCompletionDate = completionTime
        )
        
        gardenRepository.updateGardenState(updatedState)
        
        return Pair(streakIncremented, newStreak)
    }
    
    /**
     * Calculates the new streak based on the last completion date.
     * 
     * Rules:
     * - If no previous completion, start streak at 1
     * - If completion is on the same day, maintain current streak
     * - If completion is on the next consecutive day, increment streak
     * - If completion is after a gap of 1+ days, reset streak to 1
     * 
     * @param currentStreak The current streak count
     * @param lastCompletionDate The timestamp of the last completion (null if no previous completion)
     * @param completionTime The timestamp of the current completion
     * @return Pair of (newStreak: Int, streakIncremented: Boolean)
     */
    private fun calculateNewStreak(
        currentStreak: Int,
        lastCompletionDate: Long?,
        completionTime: Long
    ): Pair<Int, Boolean> {
        // First completion ever
        if (lastCompletionDate == null) {
            return Pair(1, true)
        }
        
        val daysSinceLastCompletion = calculateDaysBetween(lastCompletionDate, completionTime)
        
        return when {
            // Same day - maintain streak
            daysSinceLastCompletion == 0 -> Pair(currentStreak, false)
            
            // Next consecutive day - increment streak
            daysSinceLastCompletion == 1 -> Pair(currentStreak + 1, true)
            
            // Gap of 2+ days - reset streak
            else -> Pair(1, true)
        }
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
        val fromDay = toTime / TimeUnit.DAYS.toMillis(1)
        val toDay = fromTime / TimeUnit.DAYS.toMillis(1)
        return (fromDay - toDay).toInt()
    }
}
