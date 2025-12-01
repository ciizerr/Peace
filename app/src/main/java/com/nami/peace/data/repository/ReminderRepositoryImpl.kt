package com.nami.peace.data.repository

import com.nami.peace.data.local.ReminderDao
import com.nami.peace.data.local.ReminderEntity
import com.nami.peace.domain.model.Reminder
import com.nami.peace.domain.repository.ReminderRepository
import com.nami.peace.widget.WidgetUpdateManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ReminderRepositoryImpl @Inject constructor(
    private val dao: ReminderDao,
    private val widgetUpdateManager: WidgetUpdateManager
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
        val result = dao.insertReminder(ReminderEntity.fromDomain(reminder))
        // Trigger widget update when reminder data changes
        widgetUpdateManager.onReminderDataChanged()
        return result
    }

    override suspend fun updateReminder(reminder: Reminder) {
        dao.updateReminder(ReminderEntity.fromDomain(reminder))
        // Trigger widget update when reminder data changes
        widgetUpdateManager.onReminderDataChanged()
    }

    override suspend fun deleteReminder(reminder: Reminder) {
        dao.deleteReminder(ReminderEntity.fromDomain(reminder))
        // Trigger widget update when reminder data changes
        widgetUpdateManager.onReminderDataChanged()
    }

    override suspend fun getActiveReminders(currentTime: Long): List<Reminder> {
        return dao.getActiveReminders(currentTime).map { it.toDomain() }
    }

    override suspend fun getIncompleteReminders(): List<Reminder> {
        return dao.getIncompleteReminders().map { it.toDomain() }
    }

    override suspend fun setTaskCompleted(id: Int, isCompleted: Boolean) {
        dao.setTaskCompleted(id, isCompleted)
        // Trigger widget update when reminder completion status changes
        widgetUpdateManager.onReminderDataChanged()
    }
}
