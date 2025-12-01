package com.nami.peace.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import com.nami.peace.util.alarm.AlarmSoundManager

/**
 * Legacy SoundManager for backward compatibility.
 * Delegates to AlarmSoundManager for actual sound management.
 * 
 * @deprecated Use AlarmSoundManager directly instead.
 */
@Deprecated("Use AlarmSoundManager instead", ReplaceWith("AlarmSoundManager"))
object SoundManager {
    private var alarmSoundManager: AlarmSoundManager? = null
    
    fun initialize(manager: AlarmSoundManager) {
        alarmSoundManager = manager
    }

    fun setWakeLock(lock: android.os.PowerManager.WakeLock) {
        alarmSoundManager?.setWakeLock(lock)
    }

    fun playAlarmSound(context: Context) {
        alarmSoundManager?.playAlarmSound(null)
            ?: DebugLogger.log("SoundManager: AlarmSoundManager not initialized")
    }
    
    fun playAlarmSound(context: Context, customSoundUri: String?) {
        alarmSoundManager?.playAlarmSoundFromUri(customSoundUri)
            ?: DebugLogger.log("SoundManager: AlarmSoundManager not initialized")
    }

    fun stopAlarmSound() {
        alarmSoundManager?.stopAlarmSound()
            ?: DebugLogger.log("SoundManager: AlarmSoundManager not initialized")
    }
}
