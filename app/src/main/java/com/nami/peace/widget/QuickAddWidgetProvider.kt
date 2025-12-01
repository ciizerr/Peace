package com.nami.peace.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Widget provider for quick reminder creation.
 * Implements Requirements 17.6, 17.7, 17.8
 */
class QuickAddWidget : GlanceAppWidget() {
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            QuickAddWidgetContent()
        }
    }
}

/**
 * Receiver for the Quick-Add widget.
 * Handles widget lifecycle events and updates.
 */
@AndroidEntryPoint
class QuickAddWidgetReceiver : GlanceAppWidgetReceiver() {
    
    @Inject
    lateinit var widgetUpdateManager: WidgetUpdateManager
    
    override val glanceAppWidget: GlanceAppWidget = QuickAddWidget()
    
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
