package com.nami.peace.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker that periodically updates all widgets.
 * Runs every 15 minutes to keep widget data fresh.
 * 
 * Implements Requirements 17.2, 17.5, 17.10
 */
@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            // Update all widget instances
            TodayWidget().updateAll(applicationContext)
            GardenWidget().updateAll(applicationContext)
            
            Result.success()
        } catch (e: Exception) {
            // Log error but don't retry to avoid battery drain
            e.printStackTrace()
            Result.failure()
        }
    }
}
