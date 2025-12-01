package com.nami.peace.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Widget provider for displaying today's reminders.
 * Implements Requirements 17.1, 17.2, 17.3, 17.9
 */
class TodayWidget : GlanceAppWidget() {
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            TodayWidgetContent()
        }
    }
}

/**
 * Receiver for the Today's Reminders widget.
 * Handles widget lifecycle events and updates.
 */
@AndroidEntryPoint
class TodayWidgetReceiver : GlanceAppWidgetReceiver() {
    
    @Inject
    lateinit var widgetUpdateManager: WidgetUpdateManager
    
    override val glanceAppWidget: GlanceAppWidget = TodayWidget()
    
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // Widget is added to home screen
        widgetUpdateManager.scheduleWidgetUpdates()
    }
    
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // Last widget is removed from home screen
        widgetUpdateManager.cancelWidgetUpdates()
    }
}
