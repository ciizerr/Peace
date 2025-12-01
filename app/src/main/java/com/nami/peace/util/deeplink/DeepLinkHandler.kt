package com.nami.peace.util.deeplink

import android.net.Uri
import android.util.Base64
import com.nami.peace.domain.model.Reminder
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.RecurrenceType
import com.nami.peace.domain.model.ReminderCategory
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles encoding and decoding of reminders for deep link sharing.
 * Deep links follow the format: peace://share?data=<base64_encoded_json>
 */
@Singleton
class DeepLinkHandler @Inject constructor() {
    
    companion object {
        private const val SCHEME = "peace"
        private const val HOST = "share"
        private const val PARAM_DATA = "data"
        private const val MAX_DATA_SIZE = 8192 // 8KB limit
    }
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    /**
     * Creates a deep link URI from a reminder.
     * @param reminder The reminder to encode
     * @return Deep link URI string
     * @throws IllegalArgumentException if the encoded data exceeds size limit
     */
    fun createShareLink(reminder: Reminder): String {
        val shareableReminder = reminder.toShareableReminder()
        val encodedData = encodeReminderData(shareableReminder)
        
        if (encodedData.length > MAX_DATA_SIZE) {
            throw IllegalArgumentException("Reminder data exceeds maximum size limit of $MAX_DATA_SIZE bytes")
        }
        
        return "$SCHEME://$HOST?$PARAM_DATA=$encodedData"
    }
    
    /**
     * Parses a deep link URI and extracts the reminder data.
     * @param uri The deep link URI to parse
     * @return Reminder object if valid, null otherwise
     */
    fun parseShareLink(uri: Uri): Reminder? {
        if (uri.scheme != SCHEME || uri.host != HOST) {
            return null
        }
        
        val data = uri.getQueryParameter(PARAM_DATA) ?: return null
        return decodeReminderData(data)
    }
    
    /**
     * Parses a deep link string and extracts the reminder data.
     * @param uriString The deep link URI string to parse
     * @return Reminder object if valid, null otherwise
     */
    fun parseShareLink(uriString: String): Reminder? {
        return try {
            val uri = Uri.parse(uriString)
            parseShareLink(uri)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Validates if a URI is a valid Peace deep link.
     * @param uri The URI to validate
     * @return true if valid Peace deep link format, false otherwise
     */
    fun isValidDeepLink(uri: Uri): Boolean {
        return uri.scheme == SCHEME && 
               uri.host == HOST && 
               uri.getQueryParameter(PARAM_DATA) != null
    }
    
    private fun encodeReminderData(reminder: ShareableReminder): String {
        val jsonString = json.encodeToString(reminder)
        return Base64.encodeToString(
            jsonString.toByteArray(Charsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_WRAP
        )
    }
    
    private fun decodeReminderData(data: String): Reminder? {
        return try {
            val jsonString = String(
                Base64.decode(data, Base64.URL_SAFE),
                Charsets.UTF_8
            )
            val shareableReminder = json.decodeFromString<ShareableReminder>(jsonString)
            shareableReminder.toReminder()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Converts a Reminder to a ShareableReminder (excludes id and runtime state).
     */
    private fun Reminder.toShareableReminder(): ShareableReminder {
        return ShareableReminder(
            title = title,
            priority = priority.name,
            startTimeInMillis = startTimeInMillis,
            recurrenceType = recurrenceType.name,
            isNagModeEnabled = isNagModeEnabled,
            nagIntervalInMillis = nagIntervalInMillis,
            nagTotalRepetitions = nagTotalRepetitions,
            category = category.name,
            isStrictSchedulingEnabled = isStrictSchedulingEnabled,
            dateInMillis = dateInMillis,
            daysOfWeek = daysOfWeek,
            customAlarmSoundUri = customAlarmSoundUri,
            customAlarmSoundName = customAlarmSoundName
        )
    }
    
    /**
     * Converts a ShareableReminder back to a Reminder (with default values for excluded fields).
     */
    private fun ShareableReminder.toReminder(): Reminder {
        return Reminder(
            id = 0, // New reminder will get auto-generated ID
            title = title,
            priority = PriorityLevel.valueOf(priority),
            startTimeInMillis = startTimeInMillis,
            recurrenceType = RecurrenceType.valueOf(recurrenceType),
            isNagModeEnabled = isNagModeEnabled,
            nagIntervalInMillis = nagIntervalInMillis,
            nagTotalRepetitions = nagTotalRepetitions,
            currentRepetitionIndex = 0, // Reset to 0 for new reminder
            isCompleted = false, // New reminder starts incomplete
            isEnabled = true, // New reminder starts enabled
            isInNestedSnoozeLoop = false, // Reset snooze state
            nestedSnoozeStartTime = null, // Reset snooze state
            category = ReminderCategory.valueOf(category),
            isStrictSchedulingEnabled = isStrictSchedulingEnabled,
            dateInMillis = dateInMillis,
            daysOfWeek = daysOfWeek,
            originalStartTimeInMillis = startTimeInMillis, // Set to start time for new reminder
            customAlarmSoundUri = customAlarmSoundUri,
            customAlarmSoundName = customAlarmSoundName
        )
    }
}

/**
 * Serializable version of Reminder for deep link sharing.
 * Excludes id and runtime state fields that shouldn't be shared.
 */
@Serializable
data class ShareableReminder(
    val title: String,
    val priority: String,
    val startTimeInMillis: Long,
    val recurrenceType: String,
    val isNagModeEnabled: Boolean,
    val nagIntervalInMillis: Long?,
    val nagTotalRepetitions: Int,
    val category: String,
    val isStrictSchedulingEnabled: Boolean,
    val dateInMillis: Long?,
    val daysOfWeek: List<Int>,
    val customAlarmSoundUri: String?,
    val customAlarmSoundName: String?
)
