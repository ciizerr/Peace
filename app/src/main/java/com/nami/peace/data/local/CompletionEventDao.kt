package com.nami.peace.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for accessing completion events for ML pattern analysis.
 */
@Dao
interface CompletionEventDao {
    
    /**
     * Insert a new completion event
     */
    @Insert
    suspend fun insert(event: CompletionEventEntity): Long
    
    /**
     * Get all completion events from the last 90 days
     */
    @Query("""
        SELECT * FROM completion_events 
        WHERE completedTimeInMillis >= :ninetyDaysAgo 
        ORDER BY completedTimeInMillis DESC
    """)
    fun getRecentEvents(ninetyDaysAgo: Long): Flow<List<CompletionEventEntity>>
    
    /**
     * Get completion events for a specific reminder
     */
    @Query("""
        SELECT * FROM completion_events 
        WHERE reminderId = :reminderId 
        AND completedTimeInMillis >= :ninetyDaysAgo
        ORDER BY completedTimeInMillis DESC
    """)
    fun getEventsForReminder(reminderId: Int, ninetyDaysAgo: Long): Flow<List<CompletionEventEntity>>
    
    /**
     * Get completion events by category
     */
    @Query("""
        SELECT * FROM completion_events 
        WHERE category = :category 
        AND completedTimeInMillis >= :ninetyDaysAgo
        ORDER BY completedTimeInMillis DESC
    """)
    fun getEventsByCategory(category: String, ninetyDaysAgo: Long): Flow<List<CompletionEventEntity>>
    
    /**
     * Get completion events by priority
     */
    @Query("""
        SELECT * FROM completion_events 
        WHERE priority = :priority 
        AND completedTimeInMillis >= :ninetyDaysAgo
        ORDER BY completedTimeInMillis DESC
    """)
    fun getEventsByPriority(priority: String, ninetyDaysAgo: Long): Flow<List<CompletionEventEntity>>
    
    /**
     * Get completion events by hour of day
     */
    @Query("""
        SELECT * FROM completion_events 
        WHERE hourOfDay = :hour 
        AND completedTimeInMillis >= :ninetyDaysAgo
        ORDER BY completedTimeInMillis DESC
    """)
    fun getEventsByHour(hour: Int, ninetyDaysAgo: Long): Flow<List<CompletionEventEntity>>
    
    /**
     * Get completion events by day of week
     */
    @Query("""
        SELECT * FROM completion_events 
        WHERE dayOfWeek = :dayOfWeek 
        AND completedTimeInMillis >= :ninetyDaysAgo
        ORDER BY completedTimeInMillis DESC
    """)
    fun getEventsByDayOfWeek(dayOfWeek: Int, ninetyDaysAgo: Long): Flow<List<CompletionEventEntity>>
    
    /**
     * Get count of completion events
     */
    @Query("""
        SELECT COUNT(*) FROM completion_events 
        WHERE completedTimeInMillis >= :ninetyDaysAgo
    """)
    suspend fun getEventCount(ninetyDaysAgo: Long): Int
    
    /**
     * Delete events older than 90 days (cleanup)
     */
    @Query("DELETE FROM completion_events WHERE completedTimeInMillis < :ninetyDaysAgo")
    suspend fun deleteOldEvents(ninetyDaysAgo: Long): Int
    
    /**
     * Get all events (for testing/debugging)
     */
    @Query("SELECT * FROM completion_events ORDER BY completedTimeInMillis DESC")
    fun getAllEvents(): Flow<List<CompletionEventEntity>>
    
    /**
     * Delete all events (for testing)
     */
    @Query("DELETE FROM completion_events")
    suspend fun deleteAll()
}
