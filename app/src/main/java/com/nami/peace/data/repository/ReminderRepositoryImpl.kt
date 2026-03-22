package com.nami.peace.data.repository

import com.nami.peace.data.local.HistoryDao
import com.nami.peace.data.local.HistoryEntity
import com.nami.peace.data.local.ReminderDao
import com.nami.peace.data.local.ReminderEntity
import com.nami.peace.domain.model.Reminder
import com.nami.peace.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ReminderRepositoryImpl @Inject constructor(
    private val dao: ReminderDao,
    private val historyDao: HistoryDao
) : ReminderRepository {

    override fun getReminders(): Flow<List<Reminder>> {
        return dao.getReminders().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getReminderById(id: Int): Reminder? {
        return dao.getReminderById(id)?.toDomain()
    }

    override suspend fun insertReminder(reminder: Reminder): Long {
        return dao.insertReminder(ReminderEntity.fromDomain(reminder))
    }

    override suspend fun updateReminder(reminder: Reminder) {
        dao.updateReminder(ReminderEntity.fromDomain(reminder))
    }

    override suspend fun deleteReminder(reminder: Reminder) {
        dao.deleteReminder(ReminderEntity.fromDomain(reminder))
    }

    override suspend fun getActiveReminders(currentTime: Long): List<Reminder> {
        return dao.getActiveReminders(currentTime).map { it.toDomain() }
    }

    override suspend fun getIncompleteReminders(): List<Reminder> {
        return dao.getIncompleteReminders().map { it.toDomain() }
    }

    override suspend fun setTaskCompleted(id: Int, isCompleted: Boolean) {
        dao.setTaskCompleted(id, isCompleted)
    }

    override suspend fun getAllRemindersList(): List<Reminder> {
        return dao.getAllRemindersList().map { it.toDomain() }
    }

    override fun getAllHistory(): Flow<List<Reminder>> {
        return historyDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getAllHistoryList(): List<Reminder> {
        return historyDao.getAllList().map { it.toDomain() }
    }

    override suspend fun clearAllReminders() {
        dao.clearAll()
    }

    override suspend fun clearAllHistory() {
        historyDao.clearAll()
    }

    override suspend fun insertHistory(reminder: Reminder) {
        historyDao.insert(HistoryEntity.fromDomain(reminder))
    }
}
