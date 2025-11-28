package com.nami.peace.domain.repository

import com.nami.peace.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getReminders(): Flow<List<Reminder>>
    suspend fun getReminderById(id: Int): Reminder?
    suspend fun insertReminder(reminder: Reminder): Long
    suspend fun updateReminder(reminder: Reminder)
    suspend fun deleteReminder(reminder: Reminder)
    suspend fun getActiveReminders(currentTime: Long): List<Reminder>
    suspend fun getIncompleteReminders(): List<Reminder>
    suspend fun setTaskCompleted(id: Int, isCompleted: Boolean)
}
