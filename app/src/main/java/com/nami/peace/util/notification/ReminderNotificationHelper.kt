package com.nami.peace.util.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.nami.peace.MainActivity
import com.nami.peace.R
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.Reminder
import com.nami.peace.domain.model.ReminderCategory
import com.nami.peace.domain.repository.SubtaskRepository
import com.nami.peace.scheduler.AlarmReceiver
import com.nami.peace.util.icon.IoniconsManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for creating custom notification layouts for reminders.
 * Implements Requirements 14.1, 14.6, 19.6
 */
@Singleton
class ReminderNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val iconManager: IoniconsManager,
    private val subtaskRepository: SubtaskRepository
) {
    companion object {
        private const val CHANNEL_ID = "reminder_channel"
        private const val CHANNEL_NAME = "Reminders"
        private const val CHANNEL_DESCRIPTION = "Peace Reminder Notifications"
        private const val BUNDLED_NOTIFICATION_ID = 999999
        const val BUNDLED_GROUP_KEY = "com.nami.peace.BUNDLED_REMINDERS"
    }
    
    init {
        createNotificationChannel()
    }
    
    /**
     * Creates the notification channel for reminders.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI, audioAttributes)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Creates a custom notification for a reminder with enhanced layout.
     * 
     * @param reminder The reminder to create notification for
     * @param subtaskProgress Optional subtask progress (completed/total), null if no subtasks
     * @return NotificationCompat.Builder configured with custom layout
     */
    suspend fun createReminderNotification(
        reminder: Reminder,
        subtaskProgress: Pair<Int, Int>? = null
    ): NotificationCompat.Builder {
        // Get subtask progress if not provided
        val progress = subtaskProgress ?: getSubtaskProgress(reminder.id)
        
        // Create custom layout
        val customView = createCustomNotificationLayout(reminder, progress)
        val expandedView = createExpandedNotificationLayout(reminder, progress)
        
        // Create pending intents for actions
        val fullScreenIntent = createFullScreenIntent(reminder)
        val completeIntent = createActionIntent(reminder, "com.nami.peace.ACTION_COMPLETE")
        val snoozeIntent = createActionIntent(reminder, "com.nami.peace.ACTION_SNOOZE")
        val dismissIntent = createActionIntent(reminder, "com.nami.peace.ACTION_STOP_SOUND")
        
        // Get action icons
        val completeIconResId = iconManager.getIcon("checkmark_circle") 
            ?: iconManager.getFallbackIcon("checkmark_circle")
        val snoozeIconResId = iconManager.getIcon("time") 
            ?: iconManager.getFallbackIcon("time")
        val dismissIconResId = iconManager.getIcon("close_circle") 
            ?: iconManager.getFallbackIcon("close_circle")
        
        // Build notification
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(getCategoryIcon(reminder.category))
            .setCustomContentView(customView)
            .setCustomBigContentView(expandedView)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenIntent, true)
            .setAutoCancel(true)
            .setOngoing(true)
            .addAction(completeIconResId, "Complete", completeIntent)
            .addAction(snoozeIconResId, "Snooze", snoozeIntent)
            .addAction(dismissIconResId, "Dismiss", dismissIntent)
            .setDeleteIntent(dismissIntent)
    }
    
    /**
     * Creates the collapsed custom notification layout.
     * Implements Requirement 14.8 - Distinct panic loop notification style
     */
    private fun createCustomNotificationLayout(
        reminder: Reminder,
        subtaskProgress: Pair<Int, Int>?
    ): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.notification_reminder_collapsed)
        
        // Set title with panic loop indication
        val titleText = if (reminder.isInNestedSnoozeLoop) {
            "⚠️ ${reminder.title}"
        } else {
            reminder.title
        }
        remoteViews.setTextViewText(R.id.notification_title, titleText)
        
        // Set priority indicator with panic loop color override
        remoteViews.setTextViewText(R.id.notification_priority, getPriorityText(reminder.priority))
        val priorityColor = if (reminder.isInNestedSnoozeLoop) {
            android.graphics.Color.parseColor("#DC2626") // Darker red for panic loop
        } else {
            getPriorityColor(reminder.priority)
        }
        remoteViews.setInt(
            R.id.notification_priority,
            "setBackgroundColor",
            priorityColor
        )
        
        // Set category icon
        remoteViews.setImageViewResource(
            R.id.notification_category_icon,
            getCategoryIcon(reminder.category)
        )
        
        // Show panic loop indicator if in nested snooze
        if (reminder.isInNestedSnoozeLoop) {
            remoteViews.setTextViewText(
                R.id.notification_panic_indicator_collapsed,
                context.getString(R.string.notification_panic_loop_indicator)
            )
            remoteViews.setViewVisibility(R.id.notification_panic_indicator_collapsed, android.view.View.VISIBLE)
        } else {
            remoteViews.setViewVisibility(R.id.notification_panic_indicator_collapsed, android.view.View.GONE)
        }
        
        // Set nag mode progress if enabled
        if (reminder.isNagModeEnabled && reminder.nagTotalRepetitions > 1) {
            val nagProgress = "${reminder.currentRepetitionIndex + 1}/${reminder.nagTotalRepetitions}"
            remoteViews.setTextViewText(R.id.notification_nag_progress, nagProgress)
            remoteViews.setViewVisibility(R.id.notification_nag_progress, android.view.View.VISIBLE)
        } else {
            remoteViews.setViewVisibility(R.id.notification_nag_progress, android.view.View.GONE)
        }
        
        // Set subtask progress if available
        if (subtaskProgress != null && subtaskProgress.second > 0) {
            val (completed, total) = subtaskProgress
            val progressText = "$completed/$total subtasks"
            remoteViews.setTextViewText(R.id.notification_subtask_progress, progressText)
            remoteViews.setViewVisibility(R.id.notification_subtask_progress, android.view.View.VISIBLE)
            
            // Set progress bar
            remoteViews.setProgressBar(
                R.id.notification_progress_bar,
                total,
                completed,
                false
            )
            remoteViews.setViewVisibility(R.id.notification_progress_bar, android.view.View.VISIBLE)
        } else {
            remoteViews.setViewVisibility(R.id.notification_subtask_progress, android.view.View.GONE)
            remoteViews.setViewVisibility(R.id.notification_progress_bar, android.view.View.GONE)
        }
        
        // Set Peace branding
        remoteViews.setTextViewText(R.id.notification_branding, "Peace")
        
        return remoteViews
    }
    
    /**
     * Creates the expanded custom notification layout.
     * Implements Requirement 14.8 - Distinct panic loop notification style
     */
    private fun createExpandedNotificationLayout(
        reminder: Reminder,
        subtaskProgress: Pair<Int, Int>?
    ): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.notification_reminder_expanded)
        
        // Set title with panic loop indication
        val titleText = if (reminder.isInNestedSnoozeLoop) {
            "⚠️ ${reminder.title}"
        } else {
            reminder.title
        }
        remoteViews.setTextViewText(R.id.notification_title, titleText)
        
        // Set priority indicator with panic loop color override
        remoteViews.setTextViewText(R.id.notification_priority, getPriorityText(reminder.priority))
        val priorityColor = if (reminder.isInNestedSnoozeLoop) {
            android.graphics.Color.parseColor("#DC2626") // Darker red for panic loop
        } else {
            getPriorityColor(reminder.priority)
        }
        remoteViews.setInt(
            R.id.notification_priority,
            "setBackgroundColor",
            priorityColor
        )
        
        // Set category icon
        remoteViews.setImageViewResource(
            R.id.notification_category_icon,
            getCategoryIcon(reminder.category)
        )
        
        // Set nag mode progress if enabled
        if (reminder.isNagModeEnabled && reminder.nagTotalRepetitions > 1) {
            val nagProgress = "Repetition ${reminder.currentRepetitionIndex + 1} of ${reminder.nagTotalRepetitions}"
            remoteViews.setTextViewText(R.id.notification_nag_progress, nagProgress)
            remoteViews.setViewVisibility(R.id.notification_nag_progress, android.view.View.VISIBLE)
            
            // Show panic loop indicator if in nested snooze
            if (reminder.isInNestedSnoozeLoop) {
                val panicMessage = context.getString(R.string.notification_panic_loop_indicator) + 
                    "\n" + context.getString(R.string.notification_panic_loop_message)
                remoteViews.setTextViewText(
                    R.id.notification_panic_indicator,
                    panicMessage
                )
                remoteViews.setViewVisibility(R.id.notification_panic_indicator, android.view.View.VISIBLE)
            } else {
                remoteViews.setViewVisibility(R.id.notification_panic_indicator, android.view.View.GONE)
            }
        } else {
            remoteViews.setViewVisibility(R.id.notification_nag_progress, android.view.View.GONE)
            remoteViews.setViewVisibility(R.id.notification_panic_indicator, android.view.View.GONE)
        }
        
        // Set subtask progress if available
        if (subtaskProgress != null && subtaskProgress.second > 0) {
            val (completed, total) = subtaskProgress
            val progressText = "Subtasks: $completed of $total completed"
            val progressPercentage = (completed * 100) / total
            
            remoteViews.setTextViewText(R.id.notification_subtask_progress, progressText)
            remoteViews.setTextViewText(
                R.id.notification_subtask_percentage,
                "$progressPercentage%"
            )
            remoteViews.setViewVisibility(R.id.notification_subtask_progress, android.view.View.VISIBLE)
            remoteViews.setViewVisibility(R.id.notification_subtask_percentage, android.view.View.VISIBLE)
            
            // Set progress bar
            remoteViews.setProgressBar(
                R.id.notification_progress_bar,
                100,
                progressPercentage,
                false
            )
            remoteViews.setViewVisibility(R.id.notification_progress_bar, android.view.View.VISIBLE)
        } else {
            remoteViews.setViewVisibility(R.id.notification_subtask_progress, android.view.View.GONE)
            remoteViews.setViewVisibility(R.id.notification_subtask_percentage, android.view.View.GONE)
            remoteViews.setViewVisibility(R.id.notification_progress_bar, android.view.View.GONE)
        }
        
        // Set Peace branding
        remoteViews.setTextViewText(R.id.notification_branding, "Peace")
        
        return remoteViews
    }
    
    /**
     * Gets subtask progress for a reminder.
     * Returns null if no subtasks exist.
     */
    private suspend fun getSubtaskProgress(reminderId: Int): Pair<Int, Int>? {
        val total = subtaskRepository.getSubtaskCount(reminderId)
        if (total == 0) return null
        
        val completed = subtaskRepository.getCompletedSubtaskCount(reminderId)
        return Pair(completed, total)
    }
    
    /**
     * Creates a full screen intent for the notification.
     */
    private fun createFullScreenIntent(reminder: Reminder): PendingIntent {
        val intent = Intent(context, com.nami.peace.ui.alarm.AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION or 
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("REMINDER_ID", reminder.id)
            putExtra("REMINDER_TITLE", reminder.title)
            putExtra("REMINDER_PRIORITY", reminder.priority.name)
        }
        
        return PendingIntent.getActivity(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    /**
     * Creates an action intent for notification buttons.
     */
    private fun createActionIntent(reminder: Reminder, action: String): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = action
            putExtra("REMINDER_ID", reminder.id)
        }
        
        return PendingIntent.getBroadcast(
            context,
            reminder.id + action.hashCode(), // Unique request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    /**
     * Gets the priority text for display.
     */
    private fun getPriorityText(priority: PriorityLevel): String {
        return when (priority) {
            PriorityLevel.HIGH -> "HIGH"
            PriorityLevel.MEDIUM -> "MED"
            PriorityLevel.LOW -> "LOW"
        }
    }
    
    /**
     * Gets the priority color.
     */
    private fun getPriorityColor(priority: PriorityLevel): Int {
        return when (priority) {
            PriorityLevel.HIGH -> android.graphics.Color.parseColor("#EF4444") // Red
            PriorityLevel.MEDIUM -> android.graphics.Color.parseColor("#F59E0B") // Orange
            PriorityLevel.LOW -> android.graphics.Color.parseColor("#10B981") // Green
        }
    }
    
    /**
     * Gets the category icon resource ID.
     */
    private fun getCategoryIcon(category: ReminderCategory): Int {
        return category.iconResId
    }
    
    /**
     * Creates a bundled notification for multiple simultaneous reminders.
     * Implements Requirement 14.5
     * 
     * @param reminders List of reminders to bundle (sorted by priority)
     * @return NotificationCompat.Builder configured as a bundled notification
     */
    suspend fun createBundledNotification(
        reminders: List<Reminder>
    ): NotificationCompat.Builder {
        require(reminders.isNotEmpty()) { "Cannot create bundled notification with empty list" }
        
        // Sort by priority (HIGH first)
        val sortedReminders = reminders.sortedBy { it.priority.ordinal }
        
        // Create summary text
        val summaryText = when (sortedReminders.size) {
            1 -> sortedReminders[0].title
            2 -> "${sortedReminders[0].title} and 1 other"
            else -> "${sortedReminders[0].title} and ${sortedReminders.size - 1} others"
        }
        
        // Create inbox style for expanded view
        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle("${sortedReminders.size} Reminders")
            .setSummaryText("Tap to view all")
        
        // Add each reminder to the inbox
        sortedReminders.forEach { reminder ->
            val priorityBadge = when (reminder.priority) {
                PriorityLevel.HIGH -> "🔴"
                PriorityLevel.MEDIUM -> "🟠"
                PriorityLevel.LOW -> "🟢"
            }
            inboxStyle.addLine("$priorityBadge ${reminder.title}")
        }
        
        // Create pending intent to open app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            BUNDLED_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Get the highest priority reminder for the icon
        val highestPriorityReminder = sortedReminders.first()
        
        // Create action intents for the highest priority reminder
        val completeIntent = createActionIntent(highestPriorityReminder, "com.nami.peace.ACTION_COMPLETE")
        val snoozeIntent = createActionIntent(highestPriorityReminder, "com.nami.peace.ACTION_SNOOZE")
        val dismissIntent = createActionIntent(highestPriorityReminder, "com.nami.peace.ACTION_STOP_SOUND")
        
        // Get action icons
        val completeIconResId = iconManager.getIcon("checkmark_circle") 
            ?: iconManager.getFallbackIcon("checkmark_circle")
        val snoozeIconResId = iconManager.getIcon("time") 
            ?: iconManager.getFallbackIcon("time")
        val dismissIconResId = iconManager.getIcon("close_circle") 
            ?: iconManager.getFallbackIcon("close_circle")
        
        // Build bundled notification
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(getCategoryIcon(highestPriorityReminder.category))
            .setContentTitle("${sortedReminders.size} Reminders")
            .setContentText(summaryText)
            .setStyle(inboxStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOngoing(true)
            .setGroup(BUNDLED_GROUP_KEY)
            .setGroupSummary(true)
            .addAction(completeIconResId, "Complete", completeIntent)
            .addAction(snoozeIconResId, "Snooze", snoozeIntent)
            .addAction(dismissIconResId, "Dismiss", dismissIntent)
            .setDeleteIntent(dismissIntent)
    }
    
    /**
     * Checks if reminders should be bundled based on their trigger times.
     * Reminders within a 1-minute window should be bundled.
     * Implements Requirement 14.5
     * 
     * @param reminders List of all active reminders
     * @param currentTime Current time in milliseconds
     * @return List of reminder IDs that should be bundled, or empty if no bundling needed
     */
    fun detectSimultaneousReminders(
        reminders: List<Reminder>,
        currentTime: Long
    ): List<Int> {
        val timeWindow = 60 * 1000L // 1 minute
        
        val simultaneousReminders = reminders.filter { reminder ->
            !reminder.isCompleted && 
            reminder.isEnabled &&
            kotlin.math.abs(reminder.startTimeInMillis - currentTime) < timeWindow
        }
        
        // Only bundle if there are 2 or more reminders
        return if (simultaneousReminders.size >= 2) {
            simultaneousReminders
                .sortedBy { it.priority.ordinal } // Sort by priority (HIGH first)
                .map { it.id }
        } else {
            emptyList()
        }
    }
}
