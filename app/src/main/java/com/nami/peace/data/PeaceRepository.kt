package com.nami.peace.data

import kotlinx.coroutines.flow.Flow

class PeaceRepository(
    private val reminderDao: ReminderDao,
    private val categoryDao: CategoryDao
) {
    val allReminders: Flow<List<Reminder>> = reminderDao.getAllReminders()
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun insertReminder(reminder: Reminder) {
        reminderDao.insert(reminder)
    }

    suspend fun deleteReminder(reminder: Reminder) {
        reminderDao.delete(reminder)
    }

    suspend fun insertCategory(category: Category) {
        categoryDao.insert(category)
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.delete(category)
    }

    suspend fun nukeData() {
        reminderDao.deleteAll()
        categoryDao.deleteAll()
    }
}
