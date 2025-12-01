package com.nami.peace.util.performance

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * StartupProfiler tracks app startup performance metrics.
 * 
 * Measures:
 * - Application initialization time
 * - Database initialization time
 * - Icon cache initialization time
 * - Total cold start time
 * 
 * Usage:
 * 1. Call startMeasurement() at the beginning of each phase
 * 2. Call endMeasurement() at the end of each phase
 * 3. Call logStartupMetrics() to output all measurements
 */
@Singleton
class StartupProfiler @Inject constructor() {
    
    private val measurements = mutableMapOf<String, Long>()
    private val startTimes = mutableMapOf<String, Long>()
    private var appStartTime = 0L
    
    companion object {
        private const val TAG = "StartupProfiler"
        const val PHASE_APP_INIT = "app_initialization"
        const val PHASE_DATABASE_INIT = "database_initialization"
        const val PHASE_ICON_CACHE_INIT = "icon_cache_initialization"
        const val PHASE_FONT_INIT = "font_initialization"
        const val PHASE_PREFERENCES_INIT = "preferences_initialization"
        const val PHASE_TOTAL_STARTUP = "total_startup"
    }
    
    /**
     * Mark the start of app initialization.
     * Should be called in Application.onCreate()
     */
    fun markAppStart() {
        appStartTime = System.currentTimeMillis()
        startMeasurement(PHASE_TOTAL_STARTUP)
    }
    
    /**
     * Start measuring a specific phase.
     */
    fun startMeasurement(phase: String) {
        startTimes[phase] = System.currentTimeMillis()
    }
    
    /**
     * End measuring a specific phase and record the duration.
     */
    fun endMeasurement(phase: String) {
        val startTime = startTimes[phase] ?: return
        val duration = System.currentTimeMillis() - startTime
        measurements[phase] = duration
        startTimes.remove(phase)
    }
    
    /**
     * Log all startup metrics to Logcat.
     */
    fun logStartupMetrics() {
        Log.d(TAG, "=== Startup Performance Metrics ===")
        
        measurements.forEach { (phase, duration) ->
            Log.d(TAG, "$phase: ${duration}ms")
        }
        
        val totalTime = measurements[PHASE_TOTAL_STARTUP] ?: 0L
        Log.d(TAG, "Total startup time: ${totalTime}ms")
        
        // Warn if startup is slow
        if (totalTime > 1000) {
            Log.w(TAG, "Slow startup detected! Total time: ${totalTime}ms")
        }
        
        Log.d(TAG, "===================================")
    }
    
    /**
     * Get the duration of a specific phase.
     */
    fun getMeasurement(phase: String): Long? {
        return measurements[phase]
    }
    
    /**
     * Get all measurements as a map.
     */
    fun getAllMeasurements(): Map<String, Long> {
        return measurements.toMap()
    }
    
    /**
     * Clear all measurements.
     */
    fun clear() {
        measurements.clear()
        startTimes.clear()
        appStartTime = 0L
    }
}
