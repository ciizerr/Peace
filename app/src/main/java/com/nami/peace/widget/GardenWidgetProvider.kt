package com.nami.peace.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Widget provider for displaying Peace Garden state and streak.
 * Implements Requirements 17.4, 17.5, 17.9
 */
class GardenWidget : GlanceAppWidget() {
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GardenWidgetContent()
        }
    }
}

/**
 * Receiver for the Peace Garden widget.
 * Handles widget lifecycle events and updates.
 */
@AndroidEntryPoint
class GardenWidgetReceiver : GlanceAppWidgetReceiver() {
    
    @Inject
    lateinit var widgetUpdateManager: WidgetUpdateManager
    
    override val glanceAppWidget: GlanceAppWidget = GardenWidget()
    
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
