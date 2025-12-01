package com.nami.peace.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages widget updates with intelligent throttling to prevent excessive updates.
 * Implements Requirements 17.2, 17.5, 17.10
 * 
 * Performance optimizations:
 * - Throttling to maximum once per minute for battery efficiency
 * - Debouncing to batch rapid updates within a short window
 * - Separate throttling for different widget types
 * - Immediate updates for critical changes (theme)
 * 
 * Updates are throttled to maximum once per minute to balance
 * freshness with battery efficiency. Provides methods to trigger
 * updates on data changes and supports theme updates.
 */
@Singleton
class WidgetUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Separate throttling for different update types
    private var lastReminderUpdateTime = 0L
    private var lastGardenUpdateTime = 0L
    private val minUpdateInterval = 60_000L // 1 minute in milliseconds
    
    // Debouncing to batch rapid updates
    private var pendingReminderUpdate: kotlinx.coroutines.Job? = null
    private var pendingGardenUpdate: kotlinx.coroutines.Job? = null
    private val debounceDelay = 2_000L // 2 seconds
    
    /**
     * Triggers widget update when reminder data changes.
     * This is called when reminders are created, updated, or deleted.
     * Updates are debounced and throttled to prevent excessive updates.
     */
    fun onReminderDataChanged() {
        // Cancel any pending update
        pendingReminderUpdate?.cancel()
        
        // Schedule a new debounced update
        pendingReminderUpdate = scope.launch {
            delay(debounceDelay)
            
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastReminderUpdateTime >= minUpdateInterval) {
                lastReminderUpdateTime = currentTime
                updateTodayWidget()
            }
        }
    }
    
    /**
     * Triggers widget update when garden state changes.
     * This is called when tasks are completed or garden theme changes.
     * Updates are debounced and throttled to prevent excessive updates.
     */
    fun onGardenStateChanged() {
        // Cancel any pending update
        pendingGardenUpdate?.cancel()
        
        // Schedule a new debounced update
        pendingGardenUpdate = scope.launch {
            delay(debounceDelay)
            
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastGardenUpdateTime >= minUpdateInterval) {
                lastGardenUpdateTime = currentTime
                updateGardenWidget()
            }
        }
    }
    
    /**
     * Triggers widget update when theme changes.
     * This bypasses throttling to ensure immediate theme application.
     */
    fun onThemeChanged() {
        // Cancel any pending updates
        pendingReminderUpdate?.cancel()
        pendingGardenUpdate?.cancel()
        
        // Bypass throttling for theme changes to ensure immediate update
        lastReminderUpdateTime = System.currentTimeMillis()
        lastGardenUpdateTime = System.currentTimeMillis()
        updateAllWidgets()
    }
    
    /**
     * Forces an immediate widget update, bypassing throttling.
     * Use sparingly to avoid battery drain.
     */
    fun forceUpdate() {
        // Cancel any pending updates
        pendingReminderUpdate?.cancel()
        pendingGardenUpdate?.cancel()
        
        lastReminderUpdateTime = System.currentTimeMillis()
        lastGardenUpdateTime = System.currentTimeMillis()
        updateAllWidgets()
    }
    
    /**
     * Internal method to update all widgets.
     */
    private fun updateAllWidgets() {
        scope.launch {
            try {
                // Update both widgets in parallel for better performance
                val todayJob = async { TodayWidget().updateAll(context) }
                val gardenJob = async { GardenWidget().updateAll(context) }
                todayJob.await()
                gardenJob.await()
            } catch (e: Exception) {
                // Log error but don't crash
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Update only the Today widget.
     */
    private fun updateTodayWidget() {
        scope.launch {
            try {
                TodayWidget().updateAll(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Update only the Garden widget.
     */
    private fun updateGardenWidget() {
        scope.launch {
            try {
                GardenWidget().updateAll(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Schedules periodic widget updates using WorkManager.
     * Updates run every 15 minutes to keep widget data fresh.
     */
    fun scheduleWidgetUpdates() {
        val workRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WIDGET_UPDATE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
    
    /**
     * Cancels scheduled widget updates.
     */
    fun cancelWidgetUpdates() {
        WorkManager.getInstance(context).cancelUniqueWork(WIDGET_UPDATE_WORK_NAME)
    }
    
    companion object {
        private const val WIDGET_UPDATE_WORK_NAME = "widget_update_work"
    }
}
