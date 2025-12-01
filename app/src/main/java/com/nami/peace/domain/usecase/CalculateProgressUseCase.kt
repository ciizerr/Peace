package com.nami.peace.domain.usecase

import com.nami.peace.domain.repository.SubtaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for calculating progress percentage for reminders with subtasks.
 * 
 * This use case handles:
 * - Calculating progress percentage based on completed vs total subtasks
 * - Providing real-time progress updates via Flow
 * - Handling edge cases (no subtasks, all completed, etc.)
 * 
 * Requirements: 4.3, 4.5, 4.6
 */
class CalculateProgressUseCase @Inject constructor(
    private val subtaskRepository: SubtaskRepository
) {
    /**
     * Calculates the progress percentage for a reminder's subtasks.
     * 
     * Formula: (completed subtasks / total subtasks) * 100
     * 
     * @param reminderId The ID of the parent reminder
     * @return Progress percentage (0-100), or 0 if no subtasks exist
     */
    suspend fun calculateProgress(reminderId: Int): Int {
        require(reminderId > 0) { "Invalid reminder ID: $reminderId" }
        
        val total = subtaskRepository.getSubtaskCount(reminderId)
        if (total == 0) return 0
        
        val completed = subtaskRepository.getCompletedSubtaskCount(reminderId)
        return (completed * 100) / total
    }
    
    /**
     * Gets a Flow that emits progress percentage updates whenever subtasks change.
     * 
     * This is useful for UI components that need to react to progress changes in real-time.
     * 
     * @param reminderId The ID of the parent reminder
     * @return A Flow of progress percentages (0-100)
     */
    fun observeProgress(reminderId: Int): Flow<Int> {
        require(reminderId > 0) { "Invalid reminder ID: $reminderId" }
        
        return subtaskRepository.getSubtasksForReminder(reminderId).map { subtasks ->
            if (subtasks.isEmpty()) {
                0
            } else {
                val completed = subtasks.count { it.isCompleted }
                (completed * 100) / subtasks.size
            }
        }
    }
    
    /**
     * Gets the completion counts for a reminder.
     * 
     * @param reminderId The ID of the parent reminder
     * @return Pair of (completed count, total count)
     */
    suspend fun getCompletionCounts(reminderId: Int): Pair<Int, Int> {
        require(reminderId > 0) { "Invalid reminder ID: $reminderId" }
        
        val total = subtaskRepository.getSubtaskCount(reminderId)
        val completed = subtaskRepository.getCompletedSubtaskCount(reminderId)
        
        return Pair(completed, total)
    }
    
    /**
     * Checks if all subtasks are completed.
     * 
     * @param reminderId The ID of the parent reminder
     * @return true if all subtasks are completed, false otherwise
     */
    suspend fun isFullyCompleted(reminderId: Int): Boolean {
        require(reminderId > 0) { "Invalid reminder ID: $reminderId" }
        
        val total = subtaskRepository.getSubtaskCount(reminderId)
        if (total == 0) return false
        
        val completed = subtaskRepository.getCompletedSubtaskCount(reminderId)
        return completed == total
    }
}
