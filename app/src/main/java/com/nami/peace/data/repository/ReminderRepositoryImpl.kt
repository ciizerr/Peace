package com.nami.peace.data.repository

import com.nami.peace.data.local.ReminderDao
import com.nami.peace.data.local.ReminderEntity
import com.nami.peace.domain.model.Reminder
import com.nami.peace.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ReminderRepositoryImpl @Inject constructor(
    private val dao: ReminderDao
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
}
