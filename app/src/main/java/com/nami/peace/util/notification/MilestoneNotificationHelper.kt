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
 * Helper class for creating and displaying milestone achievement notifications.
 */
@Singleton
class MilestoneNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val CHANNEL_ID = "milestone_achievements"
        private const val CHANNEL_NAME = "Milestone Achievements"
        private const val CHANNEL_DESCRIPTION = "Notifications for streak milestones"
        private const val NOTIFICATION_ID_BASE = 5000
    }
    
    init {
        createNotificationChannel()
    }
    
    /**
     * Creates the notification channel for milestone achievements.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 100, 50, 150, 50, 200, 50, 250)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Shows a notification for a milestone achievement.
     * 
     * @param milestone The milestone value (7, 30, 100, or 365)
     */
    fun showMilestoneNotification(milestone: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create intent to open the app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_peace_garden", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.star_on) // TODO: Replace with Peace icon
            .setContentTitle(getMilestoneTitle(milestone))
            .setContentText(getMilestoneMessage(milestone))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(getMilestoneMessage(milestone))
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 100, 50, 150, 50, 200, 50, 250))
            .build()
        
        // Show notification with unique ID based on milestone
        notificationManager.notify(NOTIFICATION_ID_BASE + milestone, notification)
    }
    
    /**
     * Gets the notification title for a milestone.
     */
    private fun getMilestoneTitle(milestone: Int): String {
        return when (milestone) {
            7 -> "🏆 7 Day Streak!"
            30 -> "🏆 30 Day Streak!"
            100 -> "🏆 100 Day Streak!"
            365 -> "🏆 365 Day Streak!"
            else -> "🏆 $milestone Day Streak!"
        }
    }
    
    /**
     * Gets the notification message for a milestone.
     */
    private fun getMilestoneMessage(milestone: Int): String {
        return when (milestone) {
            7 -> "Amazing! You've completed tasks for 7 consecutive days. Keep building that habit!"
            30 -> "Incredible! A full month of consistency. You're unstoppable!"
            100 -> "Outstanding! 100 days of dedication. You're a productivity champion!"
            365 -> "LEGENDARY! A full year of consistency. You've achieved something truly remarkable!"
            else -> "Congratulations on reaching $milestone consecutive days!"
        }
    }
}
