package com.nami.peace.domain.usecase

import com.nami.peace.domain.model.Subtask
import com.nami.peace.domain.repository.SubtaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving all subtasks for a specific reminder.
 * 
 * This use case handles:
 * - Fetching subtasks from the database
 * - Ordering subtasks by their order field
 * - Providing real-time updates via Flow
 * 
 * Requirements: 4.1, 4.4
 */
class GetSubtasksForReminderUseCase @Inject constructor(
    private val subtaskRepository: SubtaskRepository
) {
    /**
     * Gets all subtasks for a reminder as a Flow.
     * The subtasks are ordered by their order field.
     * 
     * @param reminderId The ID of the parent reminder
     * @return A Flow of subtask lists that updates when subtasks change
     */
    operator fun invoke(reminderId: Int): Flow<List<Subtask>> {
        require(reminderId > 0) { "Invalid reminder ID" }
        
        return subtaskRepository.getSubtasksForReminder(reminderId)
    }
    
    /**
     * Gets the count of subtasks for a reminder.
     * 
     * @param reminderId The ID of the parent reminder
     * @return The total number of subtasks
     */
    suspend fun getCount(reminderId: Int): Int {
        require(reminderId > 0) { "Invalid reminder ID" }
        
        return subtaskRepository.getSubtaskCount(reminderId)
    }
    
    /**
     * Gets the count of completed subtasks for a reminder.
     * 
     * @param reminderId The ID of the parent reminder
     * @return The number of completed subtasks
     */
    suspend fun getCompletedCount(reminderId: Int): Int {
        require(reminderId > 0) { "Invalid reminder ID" }
        
        return subtaskRepository.getCompletedSubtaskCount(reminderId)
    }
    
    /**
     * Calculates the progress percentage for a reminder's subtasks.
     * 
     * @param reminderId The ID of the parent reminder
     * @return Progress percentage (0-100), or 0 if no subtasks exist
     */
    suspend fun calculateProgress(reminderId: Int): Int {
        require(reminderId > 0) { "Invalid reminder ID" }
        
        val total = subtaskRepository.getSubtaskCount(reminderId)
        if (total == 0) return 0
        
        val completed = subtaskRepository.getCompletedSubtaskCount(reminderId)
        return (completed * 100) / total
    }
}
