package com.nami.peace.util.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import com.nami.peace.domain.model.AlarmSound
import com.nami.peace.util.DebugLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages alarm sounds including loading system sounds, custom sounds, and sound preview.
 */
@Singleton
class AlarmSoundManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var previewPlayer: MediaPlayer? = null
    private var alarmPlayer: MediaPlayer? = null
    private var wakeLock: android.os.PowerManager.WakeLock? = null
    
    // Volume control (0.0 to 1.0)
    private var alarmVolume: Float = 1.0f
    private var previewVolume: Float = 0.7f // Preview at 70% volume by default

    /**
     * Gets all available system alarm sounds.
     */
    suspend fun getSystemAlarmSounds(): List<AlarmSound> = withContext(Dispatchers.IO) {
        val sounds = mutableListOf<AlarmSound>()
        
        try {
            val ringtoneManager = RingtoneManager(context)
            ringtoneManager.setType(RingtoneManager.TYPE_ALARM)
            
            val cursor = ringtoneManager.cursor
            
            while (cursor.moveToNext()) {
                val id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX)
                val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                val uri = ringtoneManager.getRingtoneUri(cursor.position)
                
                sounds.add(
                    AlarmSound(
                        id = "system_$id",
                        name = title,
                        uri = uri,
                        isSystem = true
                    )
                )
            }
            
            cursor.close()
            
            DebugLogger.log("AlarmSoundManager: Loaded ${sounds.size} system alarm sounds")
        } catch (e: Exception) {
            DebugLogger.log("AlarmSoundManager: Error loading system sounds: ${e.message}")
            e.printStackTrace()
        }
        
        // Add default alarm sound at the beginning
        val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        
        sounds.add(
            0,
            AlarmSound(
                id = "default",
                name = "Default Alarm",
                uri = defaultUri,
                isSystem = true
            )
        )
        
        sounds
    }

    /**
     * Gets all available notification sounds (as alternative alarm sounds).
     */
    suspend fun getSystemNotificationSounds(): List<AlarmSound> = withContext(Dispatchers.IO) {
        val sounds = mutableListOf<AlarmSound>()
        
        try {
            val ringtoneManager = RingtoneManager(context)
            ringtoneManager.setType(RingtoneManager.TYPE_NOTIFICATION)
            
            val cursor = ringtoneManager.cursor
            
            while (cursor.moveToNext()) {
                val id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX)
                val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                val uri = ringtoneManager.getRingtoneUri(cursor.position)
                
                sounds.add(
                    AlarmSound(
                        id = "notification_$id",
                        name = title,
                        uri = uri,
                        isSystem = true
                    )
                )
            }
            
            cursor.close()
            
            DebugLogger.log("AlarmSoundManager: Loaded ${sounds.size} notification sounds")
        } catch (e: Exception) {
            DebugLogger.log("AlarmSoundManager: Error loading notification sounds: ${e.message}")
            e.printStackTrace()
        }
        
        sounds
    }

    /**
     * Gets all available sounds (system alarms + notifications).
     */
    suspend fun getAllSystemSounds(): List<AlarmSound> = withContext(Dispatchers.IO) {
        val alarmSounds = getSystemAlarmSounds()
        val notificationSounds = getSystemNotificationSounds()
        alarmSounds + notificationSounds
    }

    /**
     * Creates an AlarmSound from a custom URI (e.g., from file picker).
     */
    fun createCustomSound(uri: Uri, name: String): AlarmSound {
        return AlarmSound(
            id = "custom_${uri.hashCode()}",
            name = name,
            uri = uri,
            isSystem = false
        )
    }

    /**
     * Validates if a URI is a valid audio file.
     */
    fun isValidAudioUri(uri: Uri): Boolean {
        return try {
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(context, uri)
            mediaPlayer.prepare()
            mediaPlayer.release()
            true
        } catch (e: Exception) {
            DebugLogger.log("AlarmSoundManager: Invalid audio URI: ${e.message}")
            false
        }
    }

    /**
     * Sets the alarm volume (0.0 to 1.0).
     */
    fun setAlarmVolume(volume: Float) {
        alarmVolume = volume.coerceIn(0.0f, 1.0f)
        alarmPlayer?.setVolume(alarmVolume, alarmVolume)
        DebugLogger.log("AlarmSoundManager: Set alarm volume to $alarmVolume")
    }
    
    /**
     * Gets the current alarm volume (0.0 to 1.0).
     */
    fun getAlarmVolume(): Float = alarmVolume
    
    /**
     * Sets the preview volume (0.0 to 1.0).
     */
    fun setPreviewVolume(volume: Float) {
        previewVolume = volume.coerceIn(0.0f, 1.0f)
        previewPlayer?.setVolume(previewVolume, previewVolume)
        DebugLogger.log("AlarmSoundManager: Set preview volume to $previewVolume")
    }
    
    /**
     * Gets the current preview volume (0.0 to 1.0).
     */
    fun getPreviewVolume(): Float = previewVolume

    /**
     * Plays a preview of the alarm sound (short duration, not looping).
     */
    fun playPreview(alarmSound: AlarmSound) {
        stopPreview() // Stop any existing preview
        
        try {
            val uri = if (alarmSound.uri == Uri.EMPTY) {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            } else {
                alarmSound.uri
            }
            
            previewPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setVolume(previewVolume, previewVolume)
                isLooping = false
                setOnCompletionListener {
                    stopPreview()
                }
                prepare()
                start()
            }
            
            DebugLogger.log("AlarmSoundManager: Playing preview for ${alarmSound.name} at volume $previewVolume")
        } catch (e: Exception) {
            DebugLogger.log("AlarmSoundManager: Error playing preview: ${e.message}")
            e.printStackTrace()
            stopPreview()
        }
    }

    /**
     * Stops the preview sound.
     */
    fun stopPreview() {
        previewPlayer?.let {
            try {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        previewPlayer = null
        DebugLogger.log("AlarmSoundManager: Stopped preview")
    }

    /**
     * Plays the alarm sound (looping, for actual alarms).
     */
    fun playAlarmSound(alarmSound: AlarmSound?) {
        if (alarmPlayer?.isPlaying == true) {
            DebugLogger.log("AlarmSoundManager: Already playing alarm. Ignoring request.")
            return
        }
        stopAlarmSound() // Stop any existing alarm sound
        
        try {
            val uri = if (alarmSound == null || alarmSound.uri == Uri.EMPTY) {
                // Use default alarm sound
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            } else {
                alarmSound.uri
            }
            
            alarmPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setVolume(alarmVolume, alarmVolume)
                isLooping = true
                prepare()
                start()
            }
            
            DebugLogger.log("AlarmSoundManager: Playing alarm sound: ${alarmSound?.name ?: "Default"} at volume $alarmVolume")
        } catch (e: Exception) {
            DebugLogger.log("AlarmSoundManager: Error playing alarm sound: ${e.message}")
            e.printStackTrace()
            // Fallback to default sound if custom sound fails
            if (alarmSound != null && !alarmSound.isSystem) {
                DebugLogger.log("AlarmSoundManager: Custom sound failed, falling back to default")
                playAlarmSound(null)
            }
        }
    }

    /**
     * Plays alarm sound from URI string (for backward compatibility).
     * Automatically falls back to default sound if custom sound fails.
     */
    fun playAlarmSoundFromUri(uriString: String?) {
        if (uriString.isNullOrEmpty()) {
            DebugLogger.log("AlarmSoundManager: No custom sound URI, using default")
            playAlarmSound(null)
            return
        }
        
        try {
            val uri = Uri.parse(uriString)
            
            // Validate URI before attempting to play
            if (!isValidAudioUri(uri)) {
                DebugLogger.log("AlarmSoundManager: Invalid audio URI, falling back to default")
                playAlarmSound(null)
                return
            }
            
            val alarmSound = AlarmSound(
                id = "custom",
                name = "Custom",
                uri = uri,
                isSystem = false
            )
            playAlarmSound(alarmSound)
        } catch (e: Exception) {
            DebugLogger.log("AlarmSoundManager: Error parsing URI, falling back to default: ${e.message}")
            e.printStackTrace()
            playAlarmSound(null)
        }
    }

    /**
     * Stops the alarm sound.
     */
    fun stopAlarmSound() {
        alarmPlayer?.let {
            try {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        alarmPlayer = null
        
        // Release WakeLock
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
                DebugLogger.log("AlarmSoundManager: Released WakeLock.")
            }
        }
        wakeLock = null
        
        DebugLogger.log("AlarmSoundManager: Stopped alarm sound.")
    }

    /**
     * Sets the wake lock for alarm playback.
     */
    fun setWakeLock(lock: android.os.PowerManager.WakeLock) {
        wakeLock = lock
    }

    /**
     * Releases all resources.
     */
    fun release() {
        stopPreview()
        stopAlarmSound()
    }
}
