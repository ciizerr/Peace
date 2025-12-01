package com.nami.peace.domain.model

import android.net.Uri

/**
 * Represents an alarm sound that can be used for reminders.
 *
 * @property id Unique identifier for the sound
 * @property name Display name of the sound
 * @property uri URI pointing to the sound file
 * @property isSystem True if this is a system sound, false if custom
 */
data class AlarmSound(
    val id: String,
    val name: String,
    val uri: Uri,
    val isSystem: Boolean
) {
    companion object {
        /**
         * Creates a default system alarm sound.
         */
        fun default(): AlarmSound {
            return AlarmSound(
                id = "default",
                name = "Default Alarm",
                uri = Uri.EMPTY,
                isSystem = true
            )
        }
    }
}
