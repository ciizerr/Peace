package com.nami.peace.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GardenDao {
    @Query("SELECT * FROM garden_state WHERE id = 1")
    fun getGardenState(): Flow<GardenEntity?>
    
    @Query("SELECT * FROM garden_state WHERE id = 1")
    suspend fun getGardenStateOnce(): GardenEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(gardenState: GardenEntity)
    
    @Update
    suspend fun update(gardenState: GardenEntity)
}
