package com.nami.peace.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri

object SoundManager {
    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: android.os.PowerManager.WakeLock? = null

    fun setWakeLock(lock: android.os.PowerManager.WakeLock) {
        wakeLock = lock
    }

    fun playAlarmSound(context: Context) {
        if (mediaPlayer?.isPlaying == true) {
            DebugLogger.log("SoundManager: Already playing. Ignoring request.")
            return
        }
        stopAlarmSound() // Stop any existing sound (cleanup)

        try {
            val alert: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, alert)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
            DebugLogger.log("SoundManager: Playing alarm sound.")
        } catch (e: Exception) {
            DebugLogger.log("SoundManager: Error playing sound: ${e.message}")
            e.printStackTrace()
        }
    }

    fun stopAlarmSound() {
        mediaPlayer?.let {
            try {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mediaPlayer = null
        
        // Release WakeLock
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
                DebugLogger.log("SoundManager: Released WakeLock.")
            }
        }
        wakeLock = null
        
        DebugLogger.log("SoundManager: Stopped alarm sound.")
    }
}
