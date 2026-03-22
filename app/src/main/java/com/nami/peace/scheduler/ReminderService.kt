package com.nami.peace.scheduler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.nami.peace.MainActivity
import com.nami.peace.R
import com.nami.peace.data.repository.UserPreferencesRepository
import com.nami.peace.domain.repository.ReminderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderService : Service() {

    @Inject
    lateinit var repository: ReminderRepository

    @Inject
    lateinit var userPrefs: UserPreferencesRepository

    private var wakeLock: android.os.PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 1. Acquire WakeLock immediately
        val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        wakeLock = powerManager.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "Peace:ServiceWakeLock")
        wakeLock?.acquire(10 * 60 * 1000L /*10 minutes*/)
        
        val reminderId = intent?.getIntExtra("REMINDER_ID", -1) ?: -1
        val bundledIds = intent?.getIntegerArrayListExtra("BUNDLED_REMINDER_IDS") ?: arrayListOf(reminderId)
        val silentMode = intent?.getBooleanExtra("SILENT_NOTIF", false) ?: false
        
        if (reminderId != -1) {
            CoroutineScope(Dispatchers.IO).launch {
                val reminder = repository.getReminderById(reminderId)
                if (reminder != null) {
                    // Get User Preferences
                    val vol = userPrefs.soundVolume.first()
                    val soundOn = userPrefs.soundEnabled.first()
                    val vibOn = userPrefs.vibrationEnabled.first()
                    val soundscape = userPrefs.selectedSoundscape.first()
                    val customSoundUri = userPrefs.selectedSoundUri.first()

                    if (soundOn) {
                        com.nami.peace.util.SoundManager.playAlarmSound(
                            this@ReminderService, 
                            vol, 
                            vibOn,
                            soundscape,
                            customSoundUri
                        )
                    }
                    
                    // 3. Show Notification (Start Foreground) with bundled IDs
                    showNotification(reminder, bundledIds, silentMode)

                    // 4. Timeout Logic (1 Minute)
                    kotlinx.coroutines.delay(60 * 1000L)
                    com.nami.peace.util.SoundManager.stopAlarmSound()
                    com.nami.peace.util.DebugLogger.log("Ringtone Timeout: Sound stopped after 1 minute.")
                } else {
                    stopSelf()
                }
            }
        } else {
            stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // 4. Stop Sound and Release WakeLock
        com.nami.peace.util.SoundManager.stopAlarmSound()
        
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
    }

    private fun showNotification(reminder: com.nami.peace.domain.model.Reminder, bundledIds: ArrayList<Int> = arrayListOf(reminder.id), silentMode: Boolean = false) {
        val channelId = if (silentMode) "peace_silent_service" else "peace_reminder_silent_v10"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                channelId,
                if (silentMode) "Silent Alarm Service" else "Reminders",
                if (silentMode) NotificationManager.IMPORTANCE_LOW else NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notif_channel_name)
                setSound(null, null)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val fullScreenIntent = Intent(this, com.nami.peace.ui.alarm.AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("REMINDER_ID", reminder.id)
            putExtra("REMINDER_TITLE", reminder.title)
            putExtra("REMINDER_PRIORITY", reminder.priority.name)
            putIntegerArrayListExtra("BUNDLED_REMINDER_IDS", bundledIds)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            reminder.id,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val titleText = if (reminder.isInNestedSnoozeLoop) {
            getString(R.string.notif_time_to_snoozed, reminder.title)
        } else {
            getString(R.string.notif_time_to, reminder.title)
        }
            
        // Action: DONE (Stops and archives)
        val doneIntent = Intent(this, com.nami.peace.scheduler.AlarmReceiver::class.java).apply {
            action = "com.nami.peace.ACTION_COMPLETE"
            putExtra("REMINDER_ID", reminder.id)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            this,
            reminder.id + 1000, // Different request code from others
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: SNOOZE
        val snoozeIntent = Intent(this, com.nami.peace.scheduler.AlarmReceiver::class.java).apply {
            action = "com.nami.peace.ACTION_SNOOZE"
            putExtra("REMINDER_ID", reminder.id)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            this,
            reminder.id + 2000,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(reminder.category.iconResId)
            .setContentTitle(titleText)
            .setContentText(reminder.notes ?: getString(R.string.notif_tap_to_view))
            .setPriority(NotificationCompat.PRIORITY_MAX) // Use MAX for heads-up
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setStyle(NotificationCompat.BigTextStyle()
                .setBigContentTitle(titleText)
                .bigText(reminder.notes ?: getString(R.string.notif_tap_to_view))
            )
            .setAutoCancel(true)
            .setOngoing(true)
            .addAction(R.drawable.ic_check_circle, getString(R.string.done), donePendingIntent)
            .addAction(R.drawable.ic_snooze, getString(R.string.snooze_mixed), snoozePendingIntent)
            .setDeleteIntent(donePendingIntent) // Treat swipe as Done/Dismiss? Or just have actions.
            .build()

        startForeground(reminder.id, notification)
    }
}