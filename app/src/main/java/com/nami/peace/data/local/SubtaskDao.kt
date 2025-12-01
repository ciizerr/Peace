package com.nami.peace.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SubtaskDao {
    @Query("SELECT * FROM subtasks WHERE reminderId = :reminderId ORDER BY `order` ASC")
    fun getSubtasksForReminder(reminderId: Int): Flow<List<SubtaskEntity>>
    
    @Insert
    suspend fun insert(subtask: SubtaskEntity): Long
    
    @Update
    suspend fun update(subtask: SubtaskEntity)
    
    @Delete
    suspend fun delete(subtask: SubtaskEntity)
    
    @Query("SELECT COUNT(*) FROM subtasks WHERE reminderId = :reminderId")
    suspend fun getSubtaskCount(reminderId: Int): Int
    
    @Query("SELECT COUNT(*) FROM subtasks WHERE reminderId = :reminderId AND isCompleted = 1")
    suspend fun getCompletedSubtaskCount(reminderId: Int): Int
}
