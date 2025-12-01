package com.nami.peace.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO for managing the calendar sync queue.
 */
@Dao
interface SyncQueueDao {
    
    /**
     * Insert a new sync operation into the queue.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(syncQueueEntity: SyncQueueEntity): Long
    
    /**
     * Get all pending sync operations.
     */
    @Query("SELECT * FROM sync_queue WHERE isProcessing = 0 ORDER BY queuedAt ASC")
    suspend fun getPendingSyncs(): List<SyncQueueEntity>
    
    /**
     * Get pending syncs as a Flow for observing changes.
     */
    @Query("SELECT * FROM sync_queue WHERE isProcessing = 0 ORDER BY queuedAt ASC")
    fun observePendingSyncs(): Flow<List<SyncQueueEntity>>
    
    /**
     * Get a specific sync operation by ID.
     */
    @Query("SELECT * FROM sync_queue WHERE id = :id")
    suspend fun getSyncById(id: Int): SyncQueueEntity?
    
    /**
     * Get sync operations for a specific reminder.
     */
    @Query("SELECT * FROM sync_queue WHERE reminderId = :reminderId")
    suspend fun getSyncsForReminder(reminderId: Int): List<SyncQueueEntity>
    
    /**
     * Update a sync operation.
     */
    @Update
    suspend fun update(syncQueueEntity: SyncQueueEntity)
    
    /**
     * Delete a sync operation.
     */
    @Delete
    suspend fun delete(syncQueueEntity: SyncQueueEntity)
    
    /**
     * Delete a sync operation by ID.
     */
    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteById(id: Int)
    
    /**
     * Delete all sync operations for a specific reminder.
     */
    @Query("DELETE FROM sync_queue WHERE reminderId = :reminderId")
    suspend fun deleteSyncsForReminder(reminderId: Int)
    
    /**
     * Mark a sync as processing.
     */
    @Query("UPDATE sync_queue SET isProcessing = 1 WHERE id = :id")
    suspend fun markAsProcessing(id: Int)
    
    /**
     * Mark a sync as not processing.
     */
    @Query("UPDATE sync_queue SET isProcessing = 0 WHERE id = :id")
    suspend fun markAsNotProcessing(id: Int)
    
    /**
     * Get count of pending syncs.
     */
    @Query("SELECT COUNT(*) FROM sync_queue WHERE isProcessing = 0")
    suspend fun getPendingSyncCount(): Int
    
    /**
     * Get count of pending syncs as Flow.
     */
    @Query("SELECT COUNT(*) FROM sync_queue WHERE isProcessing = 0")
    fun observePendingSyncCount(): Flow<Int>
    
    /**
     * Clear all completed syncs (for cleanup).
     */
    @Query("DELETE FROM sync_queue WHERE isProcessing = 0 AND retryCount >= 5")
    suspend fun clearFailedSyncs()
}
