package com.nami.peace.domain.usecase

import com.nami.peace.domain.model.Subtask
import com.nami.peace.domain.repository.SubtaskRepository
import javax.inject.Inject

/**
 * Use case for updating an existing subtask.
 * 
 * This use case handles:
 * - Updating subtask completion state
 * - Updating subtask title
 * - Updating subtask order (for reordering)
 * - Persisting changes to the database
 * 
 * Requirements: 4.2, 4.5
 */
class UpdateSubtaskUseCase @Inject constructor(
    private val subtaskRepository: SubtaskRepository
) {
    /**
     * Updates an existing subtask.
     * 
     * @param subtask The subtask with updated values
     */
    suspend operator fun invoke(subtask: Subtask) {
        require(subtask.id > 0) { "Invalid subtask ID" }
        require(subtask.title.isNotBlank()) { "Subtask title cannot be blank" }
        require(subtask.reminderId > 0) { "Invalid reminder ID" }
        require(subtask.order >= 0) { "Order must be non-negative" }
        
        val updatedSubtask = subtask.copy(
            title = subtask.title.trim()
        )
        
        subtaskRepository.updateSubtask(updatedSubtask)
    }
    
    /**
     * Toggles the completion state of a subtask.
     * 
     * @param subtask The subtask to toggle
     */
    suspend fun toggleCompletion(subtask: Subtask) {
        val toggledSubtask = subtask.copy(
            isCompleted = !subtask.isCompleted
        )
        subtaskRepository.updateSubtask(toggledSubtask)
    }
}
