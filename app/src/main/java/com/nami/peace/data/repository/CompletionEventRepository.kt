package com.nami.peace.data.repository

import com.nami.peace.data.local.CompletionEventDao
import com.nami.peace.data.local.toDomain
import com.nami.peace.data.local.toEntity
import com.nami.peace.domain.model.CompletionEvent
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.ReminderCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing completion events for ML pattern analysis.
 * Handles storing and retrieving completion history (last 90 days).
 */
@Singleton
class CompletionEventRepository @Inject constructor(
    private val completionEventDao: CompletionEventDao
) {
    companion object {
        private val NINETY_DAYS_IN_MILLIS = TimeUnit.DAYS.toMillis(90)
    }
    
    /**
     * Record a new completion event
     */
    suspend fun recordCompletionEvent(event: CompletionEvent): Long {
        return completionEventDao.insert(event.toEntity())
    }
    
    /**
     * Get all completion events from the last 90 days
     */
    fun getRecentEvents(): Flow<List<CompletionEvent>> {
        val ninetyDaysAgo = System.currentTimeMillis() - NINETY_DAYS_IN_MILLIS
        return completionEventDao.getRecentEvents(ninetyDaysAgo)
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    /**
     * Get completion events for a specific reminder
     */
    fun getEventsForReminder(reminderId: Int): Flow<List<CompletionEvent>> {
        val ninetyDaysAgo = System.currentTimeMillis() - NINETY_DAYS_IN_MILLIS
        return completionEventDao.getEventsForReminder(reminderId, ninetyDaysAgo)
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    /**
     * Get completion events by category
     */
    fun getEventsByCategory(category: ReminderCategory): Flow<List<CompletionEvent>> {
        val ninetyDaysAgo = System.currentTimeMillis() - NINETY_DAYS_IN_MILLIS
        return completionEventDao.getEventsByCategory(category.name, ninetyDaysAgo)
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    /**
     * Get completion events by priority
     */
    fun getEventsByPriority(priority: PriorityLevel): Flow<List<CompletionEvent>> {
        val ninetyDaysAgo = System.currentTimeMillis() - NINETY_DAYS_IN_MILLIS
        return completionEventDao.getEventsByPriority(priority.name, ninetyDaysAgo)
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    /**
     * Get completion events by hour of day (0-23)
     */
    fun getEventsByHour(hour: Int): Flow<List<CompletionEvent>> {
        val ninetyDaysAgo = System.currentTimeMillis() - NINETY_DAYS_IN_MILLIS
        return completionEventDao.getEventsByHour(hour, ninetyDaysAgo)
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    /**
     * Get completion events by day of week (1=Sunday, 7=Saturday)
     */
    fun getEventsByDayOfWeek(dayOfWeek: Int): Flow<List<CompletionEvent>> {
        val ninetyDaysAgo = System.currentTimeMillis() - NINETY_DAYS_IN_MILLIS
        return completionEventDao.getEventsByDayOfWeek(dayOfWeek, ninetyDaysAgo)
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    /**
     * Get count of completion events in the last 90 days
     */
    suspend fun getEventCount(): Int {
        val ninetyDaysAgo = System.currentTimeMillis() - NINETY_DAYS_IN_MILLIS
        return completionEventDao.getEventCount(ninetyDaysAgo)
    }
    
    /**
     * Clean up events older than 90 days
     * Should be called periodically (e.g., daily)
     */
    suspend fun cleanupOldEvents(): Int {
        val ninetyDaysAgo = System.currentTimeMillis() - NINETY_DAYS_IN_MILLIS
        return completionEventDao.deleteOldEvents(ninetyDaysAgo)
    }
    
    /**
     * Get all events (for testing/debugging)
     */
    fun getAllEvents(): Flow<List<CompletionEvent>> {
        return completionEventDao.getAllEvents()
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    /**
     * Delete all events (for testing)
     */
    suspend fun deleteAll() {
        completionEventDao.deleteAll()
    }
}
