package com.nami.peace.util.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.nami.peace.MainActivity
import com.nami.peace.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for creating and displaying ML suggestion notifications.
 * Notifies users when new productivity suggestions are available.
 */
@Singleton
class SuggestionNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val CHANNEL_ID = "ml_suggestions"
        private const val CHANNEL_NAME = "ML Suggestions"
        private const val CHANNEL_DESCRIPTION = "Notifications for new productivity suggestions"
        private const val NOTIFICATION_ID = 6000
    }
    
    init {
        createNotificationChannel()
    }
    
    /**
     * Creates the notification channel for ML suggestions.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Shows a notification when new suggestions are available.
     * 
     * @param count The number of new suggestions
     */
    fun showNewSuggestionsNotification(count: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create intent to open the suggestions screen
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_suggestions", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification
        val title = if (count == 1) {
            "New Productivity Suggestion"
        } else {
            "$count New Productivity Suggestions"
        }
        
        val message = if (count == 1) {
            "We've found a way to help you be more productive. Tap to view."
        } else {
            "We've found $count ways to help you be more productive. Tap to view."
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: Replace with Peace icon
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        // Show notification
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
