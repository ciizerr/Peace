package com.nami.peace.domain.usecase

import com.nami.peace.domain.model.Subtask
import com.nami.peace.domain.repository.SubtaskRepository
import javax.inject.Inject

/**
 * Use case for deleting a subtask.
 * 
 * This use case handles:
 * - Removing a subtask from the database
 * - Triggering progress recalculation (handled by the UI layer)
 * 
 * Requirements: 4.5
 */
class DeleteSubtaskUseCase @Inject constructor(
    private val subtaskRepository: SubtaskRepository
) {
    /**
     * Deletes a subtask.
     * 
     * @param subtask The subtask to delete
     */
    suspend operator fun invoke(subtask: Subtask) {
        require(subtask.id > 0) { "Invalid subtask ID" }
        
        subtaskRepository.deleteSubtask(subtask)
    }
    
    /**
     * Deletes a subtask by ID.
     * 
     * @param subtaskId The ID of the subtask to delete
     * @param reminderId The ID of the parent reminder (for validation)
     */
    suspend fun deleteById(subtaskId: Int, reminderId: Int) {
        require(subtaskId > 0) { "Invalid subtask ID" }
        require(reminderId > 0) { "Invalid reminder ID" }
        
        // Create a minimal subtask object for deletion
        val subtask = Subtask(
            id = subtaskId,
            reminderId = reminderId,
            title = "", // Not needed for deletion
            order = 0   // Not needed for deletion
        )
        
        subtaskRepository.deleteSubtask(subtask)
    }
}
