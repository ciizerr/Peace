package com.nami.peace.data.repository

import com.nami.peace.data.local.SubtaskDao
import com.nami.peace.domain.model.Subtask
import com.nami.peace.domain.repository.SubtaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SubtaskRepositoryImpl @Inject constructor(
    private val dao: SubtaskDao
) : SubtaskRepository {

    override fun getSubtasksForReminder(reminderId: Int): Flow<List<Subtask>> {
        return dao.getSubtasksForReminder(reminderId).map { entities ->
            entities.map { Subtask.fromEntity(it) }
        }
    }

    override suspend fun insertSubtask(subtask: Subtask): Long {
        return dao.insert(subtask.toEntity())
    }

    override suspend fun updateSubtask(subtask: Subtask) {
        dao.update(subtask.toEntity())
    }

    override suspend fun deleteSubtask(subtask: Subtask) {
        dao.delete(subtask.toEntity())
    }

    override suspend fun getSubtaskCount(reminderId: Int): Int {
        return dao.getSubtaskCount(reminderId)
    }

    override suspend fun getCompletedSubtaskCount(reminderId: Int): Int {
        return dao.getCompletedSubtaskCount(reminderId)
    }
}
