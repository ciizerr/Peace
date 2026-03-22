package com.nami.peace.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri

object SoundManager {
    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: android.os.PowerManager.WakeLock? = null
    private var vibrator: android.os.Vibrator? = null

    fun setWakeLock(lock: android.os.PowerManager.WakeLock) {
        wakeLock = lock
    }

    fun playAlarmSound(
        context: Context, 
        volume: Float = 0.8f, 
        enableVibration: Boolean = true,
        selectedSoundscape: String = "Default",
        customUri: String? = null
    ) {
        if (mediaPlayer?.isPlaying == true) {
            DebugLogger.log("SoundManager: Already playing. Ignoring request.")
            return
        }
        stopAlarmSound()

        try {
            // Choose Sound URI
            val alert: Uri? = when (selectedSoundscape) {
                "Custom" -> customUri?.let { Uri.parse(it) }
                else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            } ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, alert!!)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = true
                setVolume(volume, volume)
                prepare()
                start()
            }

            // 2. Start Vibration
            if (enableVibration) {
                vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
                val pattern = longArrayOf(0, 1500, 500) // 1.5s ON, 0.5s OFF
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        android.os.VibrationEffect.createWaveform(pattern, 0)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern, 0)
                }
            }

            DebugLogger.log("SoundManager: Playing alarm (Vol: $volume, Vibrate: $enableVibration)")
        } catch (e: Exception) {
            DebugLogger.log("SoundManager: Error: ${e.message}")
            e.printStackTrace()
        }
    }

    fun stopAlarmSound() {
        mediaPlayer?.let {
            try {
                if (it.isPlaying) it.stop()
                it.release()
            } catch (e: Exception) { e.printStackTrace() }
        }
        mediaPlayer = null

        vibrator?.cancel()
        vibrator = null
        
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
        
        DebugLogger.log("SoundManager: Stopped all.")
    }
}
