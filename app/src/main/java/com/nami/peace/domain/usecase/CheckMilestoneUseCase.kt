package com.nami.peace.domain.usecase

import com.nami.peace.domain.repository.GardenRepository
import javax.inject.Inject

/**
 * Use case for checking if a milestone has been reached.
 * 
 * Milestones are significant streak achievements at 7, 30, 100, and 365 consecutive days.
 * This use case detects when a user reaches these milestones.
 */
class CheckMilestoneUseCase @Inject constructor(
    private val gardenRepository: GardenRepository
) {
    /**
     * Checks if the current streak has reached a milestone.
     * 
     * @return The milestone value if reached (7, 30, 100, or 365), null otherwise
     */
    suspend operator fun invoke(): Int? {
        val currentState = gardenRepository.getGardenStateOnce() ?: return null
        
        val currentStreak = currentState.currentStreak
        val milestones = currentState.milestones
        
        // Check if current streak matches any milestone
        return if (milestones.contains(currentStreak)) {
            currentStreak
        } else {
            null
        }
    }
    
    /**
     * Gets the next milestone the user is working towards.
     * 
     * @return The next milestone value, or null if all milestones are achieved
     */
    suspend fun getNextMilestone(): Int? {
        val currentState = gardenRepository.getGardenStateOnce() ?: return 7
        
        val currentStreak = currentState.currentStreak
        val milestones = currentState.milestones.sorted()
        
        // Find the first milestone greater than current streak
        return milestones.firstOrNull { it > currentStreak }
    }
    
    /**
     * Gets all achieved milestones.
     * 
     * @return List of milestone values that have been achieved
     */
    suspend fun getAchievedMilestones(): List<Int> {
        val currentState = gardenRepository.getGardenStateOnce() ?: return emptyList()
        
        val longestStreak = currentState.longestStreak
        val milestones = currentState.milestones
        
        // Return all milestones that are less than or equal to longest streak
        return milestones.filter { it <= longestStreak }
    }
}
