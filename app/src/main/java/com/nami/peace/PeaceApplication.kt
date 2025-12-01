package com.nami.peace

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.nami.peace.util.language.LanguageManager
import com.nami.peace.util.performance.StartupProfiler
import com.nami.peace.worker.CleanupCompletionEventsWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
open class PeaceApplication : Application() {
    
    @Inject
    lateinit var languageManager: LanguageManager
    
    @Inject
    lateinit var startupProfiler: StartupProfiler
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        
        // Start profiling app startup (after super.onCreate so Hilt injection is complete)
        startupProfiler.markAppStart()
        startupProfiler.startMeasurement(StartupProfiler.PHASE_APP_INIT)
        
        // Initialize language on app startup
        startupProfiler.startMeasurement("language_initialization")
        applicationScope.launch {
            languageManager.initializeLanguage(this@PeaceApplication)
        }
        startupProfiler.endMeasurement("language_initialization")
        
        // Skip WorkManager initialization during tests
        if (!isRunningTest()) {
            startupProfiler.startMeasurement("worker_scheduling")
            
            // Schedule periodic cleanup of old completion events (daily)
            scheduleCompletionEventsCleanup()
            
            // Schedule daily ML analysis
            scheduleMLAnalysis()
            
            startupProfiler.endMeasurement("worker_scheduling")
        }
        
        // End app initialization measurement
        startupProfiler.endMeasurement(StartupProfiler.PHASE_APP_INIT)
        startupProfiler.endMeasurement(StartupProfiler.PHASE_TOTAL_STARTUP)
        
        // Log startup metrics in debug builds
        if (BuildConfig.DEBUG) {
            startupProfiler.logStartupMetrics()
        }
    }
    
    /**
     * Checks if the app is running in a test environment.
     * This prevents WorkManager initialization issues during unit tests.
     */
    private fun isRunningTest(): Boolean {
        return try {
            Class.forName("org.robolectric.RobolectricTestRunner")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }
    
    private fun scheduleCompletionEventsCleanup() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
        
        val cleanupRequest = PeriodicWorkRequestBuilder<CleanupCompletionEventsWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            CleanupCompletionEventsWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupRequest
        )
        
        // Schedule periodic cleanup of old suggestion feedback (daily)
        scheduleSuggestionFeedbackCleanup()
    }
    
    private fun scheduleSuggestionFeedbackCleanup() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
        
        val cleanupRequest = PeriodicWorkRequestBuilder<com.nami.peace.worker.CleanupSuggestionFeedbackWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "cleanup_suggestion_feedback",
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupRequest
        )
    }
    
    private fun scheduleMLAnalysis() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
        
        val analysisRequest = PeriodicWorkRequestBuilder<com.nami.peace.worker.AnalysisWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            com.nami.peace.worker.AnalysisWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            analysisRequest
        )
    }
}
