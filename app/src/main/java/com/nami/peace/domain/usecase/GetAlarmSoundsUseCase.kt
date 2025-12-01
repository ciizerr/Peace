package com.nami.peace.domain.usecase

import com.nami.peace.domain.model.AlarmSound
import com.nami.peace.util.alarm.AlarmSoundManager
import javax.inject.Inject

/**
 * Use case for retrieving available alarm sounds.
 */
class GetAlarmSoundsUseCase @Inject constructor(
    private val alarmSoundManager: AlarmSoundManager
) {
    /**
     * Gets all available system alarm sounds.
     */
    suspend fun getSystemAlarmSounds(): List<AlarmSound> {
        return alarmSoundManager.getSystemAlarmSounds()
    }

    /**
     * Gets all available system notification sounds.
     */
    suspend fun getSystemNotificationSounds(): List<AlarmSound> {
        return alarmSoundManager.getSystemNotificationSounds()
    }

    /**
     * Gets all available system sounds (alarms + notifications).
     */
    suspend fun getAllSystemSounds(): List<AlarmSound> {
        return alarmSoundManager.getAllSystemSounds()
    }
}
