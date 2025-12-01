package com.nami.peace.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE reminderId = :reminderId ORDER BY timestamp ASC")
    fun getNotesForReminder(reminderId: Int): Flow<List<NoteEntity>>
    
    @Insert
    suspend fun insert(note: NoteEntity): Long
    
    @Delete
    suspend fun delete(note: NoteEntity)
}
