package com.nami.peace.domain.usecase

import com.nami.peace.domain.model.Subtask
import com.nami.peace.domain.repository.SubtaskRepository
import javax.inject.Inject

/**
 * Use case for adding a new subtask to a reminder.
 * 
 * This use case handles:
 * - Creating a new subtask with proper ordering
 * - Linking the subtask to its parent reminder
 * - Persisting the subtask to the database
 * 
 * Requirements: 4.1, 4.2
 */
class AddSubtaskUseCase @Inject constructor(
    private val subtaskRepository: SubtaskRepository
) {
    /**
     * Adds a new subtask to a reminder.
     * 
     * @param reminderId The ID of the parent reminder
     * @param title The title/description of the subtask
     * @param order The position of the subtask in the list (for ordering)
     * @return The ID of the newly created subtask
     */
    suspend operator fun invoke(
        reminderId: Int,
        title: String,
        order: Int
    ): Long {
        require(title.isNotBlank()) { "Subtask title cannot be blank" }
        require(reminderId > 0) { "Invalid reminder ID" }
        require(order >= 0) { "Order must be non-negative" }
        
        val subtask = Subtask(
            reminderId = reminderId,
            title = title.trim(),
            isCompleted = false,
            order = order,
            createdAt = System.currentTimeMillis()
        )
        
        return subtaskRepository.insertSubtask(subtask)
    }
}
