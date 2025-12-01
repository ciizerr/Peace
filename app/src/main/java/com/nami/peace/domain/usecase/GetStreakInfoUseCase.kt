package com.nami.peace.domain.usecase

import com.nami.peace.domain.repository.GardenRepository
import javax.inject.Inject

/**
 * Use case for retrieving streak information.
 * 
 * Provides:
 * - Current streak count
 * - Longest streak achieved
 * - Last completion date
 */
class GetStreakInfoUseCase @Inject constructor(
    private val gardenRepository: GardenRepository
) {
    /**
     * Gets the current streak information.
     * 
     * @return StreakInfo containing current and longest streak
     */
    suspend operator fun invoke(): StreakInfo {
        val currentState = gardenRepository.getGardenStateOnce()
        
        return if (currentState != null) {
            StreakInfo(
                currentStreak = currentState.currentStreak,
                longestStreak = currentState.longestStreak,
                lastCompletionDate = currentState.lastCompletionDate
            )
        } else {
            StreakInfo(
                currentStreak = 0,
                longestStreak = 0,
                lastCompletionDate = null
            )
        }
    }
}

/**
 * Data class containing streak information.
 */
data class StreakInfo(
    val currentStreak: Int,
    val longestStreak: Int,
    val lastCompletionDate: Long?
)
