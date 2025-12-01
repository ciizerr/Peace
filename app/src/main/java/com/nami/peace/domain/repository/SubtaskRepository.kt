package com.nami.peace.domain.repository

import com.nami.peace.domain.model.Subtask
import kotlinx.coroutines.flow.Flow

interface SubtaskRepository {
    fun getSubtasksForReminder(reminderId: Int): Flow<List<Subtask>>
    suspend fun insertSubtask(subtask: Subtask): Long
    suspend fun updateSubtask(subtask: Subtask)
    suspend fun deleteSubtask(subtask: Subtask)
    suspend fun getSubtaskCount(reminderId: Int): Int
    suspend fun getCompletedSubtaskCount(reminderId: Int): Int
}
