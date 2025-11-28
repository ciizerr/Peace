package com.nami.peace.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY startTimeInMillis ASC")
    fun getReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Int): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    // Helper to get active reminders for collision handling or rescheduling
    @Query("SELECT * FROM reminders WHERE isCompleted = 0 AND startTimeInMillis <= :currentTime")
    suspend fun getActiveReminders(currentTime: Long): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE isCompleted = 0")
    suspend fun getIncompleteReminders(): List<ReminderEntity>

    @Query("UPDATE reminders SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun setTaskCompleted(id: Int, isCompleted: Boolean)
}
