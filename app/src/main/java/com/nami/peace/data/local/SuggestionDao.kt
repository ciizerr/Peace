package com.nami.peace.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SuggestionDao {
    @Query("SELECT * FROM suggestions WHERE status = 'PENDING' ORDER BY confidenceScore DESC")
    fun getPendingSuggestions(): Flow<List<SuggestionEntity>>
    
    @Query("SELECT * FROM suggestions WHERE id = :id")
    suspend fun getSuggestionById(id: Int): SuggestionEntity?
    
    @Insert
    suspend fun insert(suggestion: SuggestionEntity): Long
    
    @Update
    suspend fun update(suggestion: SuggestionEntity)
    
    @Delete
    suspend fun delete(suggestion: SuggestionEntity)
}
