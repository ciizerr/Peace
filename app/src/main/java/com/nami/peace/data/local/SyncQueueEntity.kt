package com.nami.peace.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a pending calendar sync operation.
 * Used for offline sync queue and retry mechanism.
 */
@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    /** ID of the reminder to sync */
    val reminderId: Int,
    
    /** Type of sync operation */
    val operationType: SyncOperationType,
    
    /** Calendar event ID (for update/delete operations) */
    val eventId: String? = null,
    
    /** Number of retry attempts */
    val retryCount: Int = 0,
    
    /** Timestamp when the sync was queued */
    val queuedAt: Long = System.currentTimeMillis(),
    
    /** Timestamp of last retry attempt */
    val lastRetryAt: Long? = null,
    
    /** Error message from last attempt */
    val lastError: String? = null,
    
    /** Whether this sync is currently being processed */
    val isProcessing: Boolean = false
)

/**
 * Types of calendar sync operations.
 */
enum class SyncOperationType {
    CREATE,
    UPDATE,
    DELETE
}
