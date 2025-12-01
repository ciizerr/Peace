package com.nami.peace.domain.usecase

import com.nami.peace.domain.model.Subtask
import com.nami.peace.domain.repository.SubtaskRepository
import javax.inject.Inject

/**
 * Use case for reordering subtasks within a reminder.
 * 
 * This use case handles:
 * - Updating the order field of multiple subtasks
 * - Ensuring consistent ordering after drag-and-drop operations
 * - Batch updating subtasks efficiently
 * 
 * Requirements: 4.4
 */
class ReorderSubtasksUseCase @Inject constructor(
    private val subtaskRepository: SubtaskRepository
) {
    /**
     * Reorders subtasks based on a new list order.
     * 
     * @param subtasks The list of subtasks in their new order
     */
    suspend operator fun invoke(subtasks: List<Subtask>) {
        require(subtasks.isNotEmpty()) { "Subtask list cannot be empty" }
        
        // Update each subtask with its new order index
        subtasks.forEachIndexed { index, subtask ->
            val reorderedSubtask = subtask.copy(order = index)
            subtaskRepository.updateSubtask(reorderedSubtask)
        }
    }
    
    /**
     * Moves a subtask from one position to another.
     * 
     * @param subtasks The current list of subtasks
     * @param fromIndex The current index of the subtask
     * @param toIndex The target index for the subtask
     * @return The reordered list of subtasks
     */
    suspend fun moveSubtask(
        subtasks: List<Subtask>,
        fromIndex: Int,
        toIndex: Int
    ): List<Subtask> {
        require(fromIndex in subtasks.indices) { "Invalid fromIndex" }
        require(toIndex in subtasks.indices) { "Invalid toIndex" }
        
        if (fromIndex == toIndex) return subtasks
        
        val mutableList = subtasks.toMutableList()
        val item = mutableList.removeAt(fromIndex)
        mutableList.add(toIndex, item)
        
        // Update order for all affected subtasks
        val reorderedList = mutableList.mapIndexed { index, subtask ->
            subtask.copy(order = index)
        }
        
        // Persist the changes
        reorderedList.forEach { subtask ->
            subtaskRepository.updateSubtask(subtask)
        }
        
        return reorderedList
    }
}
