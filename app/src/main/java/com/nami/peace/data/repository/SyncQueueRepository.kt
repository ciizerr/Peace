package com.nami.peace.data.repository

import com.nami.peace.data.local.SyncOperationType
import com.nami.peace.data.local.SyncQueueDao
import com.nami.peace.data.local.SyncQueueEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing the calendar sync queue.
 */
@Singleton
class SyncQueueRepository @Inject constructor(
    private val syncQueueDao: SyncQueueDao
) {
    
    /**
     * Add a sync operation to the queue.
     */
    suspend fun queueSync(
        reminderId: Int,
        operationType: SyncOperationType,
        eventId: String? = null
    ): Long {
        val syncEntity = SyncQueueEntity(
            reminderId = reminderId,
            operationType = operationType,
            eventId = eventId,
            retryCount = 0,
            queuedAt = System.currentTimeMillis()
        )
        return syncQueueDao.insert(syncEntity)
    }
    
    /**
     * Get all pending sync operations.
     */
    suspend fun getPendingSyncs(): List<SyncQueueEntity> {
        return syncQueueDao.getPendingSyncs()
    }
    
    /**
     * Observe pending sync operations.
     */
    fun observePendingSyncs(): Flow<List<SyncQueueEntity>> {
        return syncQueueDao.observePendingSyncs()
    }
    
    /**
     * Get sync operations for a specific reminder.
     */
    suspend fun getSyncsForReminder(reminderId: Int): List<SyncQueueEntity> {
        return syncQueueDao.getSyncsForReminder(reminderId)
    }
    
    /**
     * Mark a sync operation as processing.
     */
    suspend fun markAsProcessing(syncId: Int) {
        syncQueueDao.markAsProcessing(syncId)
    }
    
    /**
     * Update a sync operation after a retry attempt.
     */
    suspend fun updateAfterRetry(
        syncId: Int,
        success: Boolean,
        error: String? = null
    ) {
        val sync = syncQueueDao.getSyncById(syncId) ?: return
        
        if (success) {
            // Remove from queue on success
            syncQueueDao.deleteById(syncId)
        } else {
            // Update retry count and error
            val updatedSync = sync.copy(
                retryCount = sync.retryCount + 1,
                lastRetryAt = System.currentTimeMillis(),
                lastError = error,
                isProcessing = false
            )
            syncQueueDao.update(updatedSync)
        }
    }
    
    /**
     * Remove a sync operation from the queue.
     */
    suspend fun removeSync(syncId: Int) {
        syncQueueDao.deleteById(syncId)
    }
    
    /**
     * Remove all sync operations for a specific reminder.
     */
    suspend fun removeSyncsForReminder(reminderId: Int) {
        syncQueueDao.deleteSyncsForReminder(reminderId)
    }
    
    /**
     * Get count of pending syncs.
     */
    suspend fun getPendingSyncCount(): Int {
        return syncQueueDao.getPendingSyncCount()
    }
    
    /**
     * Observe count of pending syncs.
     */
    fun observePendingSyncCount(): Flow<Int> {
        return syncQueueDao.observePendingSyncCount()
    }
    
    /**
     * Clear failed syncs that have exceeded max retry attempts.
     */
    suspend fun clearFailedSyncs() {
        syncQueueDao.clearFailedSyncs()
    }
}
