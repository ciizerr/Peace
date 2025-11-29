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
import com.nami.peace.domain.repository.ReminderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderService : Service() {

    @Inject
    lateinit var repository: ReminderRepository

    private var wakeLock: android.os.PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 1. Acquire WakeLock immediately
        val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        wakeLock = powerManager.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "Peace:ServiceWakeLock")
        wakeLock?.acquire(10 * 60 * 1000L /*10 minutes*/)
        
        val reminderId = intent?.getIntExtra("REMINDER_ID", -1) ?: -1
        if (reminderId != -1) {
            CoroutineScope(Dispatchers.IO).launch {
                val reminder = repository.getReminderById(reminderId)
                if (reminder != null) {
                    // 2. Play Sound
                    com.nami.peace.util.SoundManager.playAlarmSound(this@ReminderService)
                    
                    // 3. Show Notification (Start Foreground)
                    showNotification(reminder)

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

    private fun showNotification(reminder: com.nami.peace.domain.model.Reminder) {
        val channelId = "reminder_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                channelId,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Nag Mode Reminders"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI, audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val fullScreenIntent = Intent(this, com.nami.peace.ui.alarm.AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("REMINDER_ID", reminder.id)
            putExtra("REMINDER_TITLE", reminder.title)
            putExtra("REMINDER_PRIORITY", reminder.priority.name)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            reminder.id,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val titleText = if (reminder.isInNestedSnoozeLoop) {
            "Time to ${reminder.title} (Snoozed)"
        } else {
            "Time to ${reminder.title}"
        }
            
        // Action to stop service via AlarmReceiver
        val stopSoundIntent = Intent(this, com.nami.peace.scheduler.AlarmReceiver::class.java).apply {
            action = "com.nami.peace.ACTION_STOP_SOUND"
        }
        val stopSoundPendingIntent = PendingIntent.getBroadcast(
            this,
            reminder.id, 
            stopSoundIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(reminder.category.iconResId)
            .setContentTitle(titleText)
            .setContentText("Tap to view")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .setOngoing(true) // Make it ongoing so it can't be swiped away easily
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", stopSoundPendingIntent)
            .setDeleteIntent(stopSoundPendingIntent)
            .build()

        startForeground(reminder.id, notification)
    }
}