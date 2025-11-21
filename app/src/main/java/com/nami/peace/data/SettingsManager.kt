package com.nami.peace.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 1. Create the extension for DataStore
private val Context.dataStore by preferencesDataStore(name = "user_settings")

class SettingsManager(private val context: Context) {

    companion object {
        val USER_API_KEY = stringPreferencesKey("user_api_key")
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val HAPTICS_KEY = booleanPreferencesKey("haptics_enabled")
        val SOUND_KEY = stringPreferencesKey("sound_selection")
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val USER_PROFILE_URI_KEY = stringPreferencesKey("user_profile_uri")
        val PRIVACY_MODE_KEY = booleanPreferencesKey("privacy_mode")
        val THEME_ACCENT_KEY = stringPreferencesKey("theme_accent")
        
        // Profile Keys
        val WAKE_UP_TIME_KEY = stringPreferencesKey("wake_up_time")
        val BED_TIME_KEY = stringPreferencesKey("bed_time")
        val FOCUS_AREAS_KEY = stringPreferencesKey("focus_areas") // Stored as comma-separated string
        val DIETARY_CONTEXT_KEY = stringPreferencesKey("dietary_context") // Stored as comma-separated string
        val DIETARY_NOTES_KEY = stringPreferencesKey("dietary_notes")
    }

    val userApiKeyFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_API_KEY]
        }

    val isDarkModeFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: true // Default to Dark Mode
        }

    val isHapticsEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[HAPTICS_KEY] ?: true
        }

    val soundFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[SOUND_KEY] ?: "Calm Breeze"
        }

    val userNameFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[USER_NAME_KEY] ?: "User"
        }

    val userProfileUriFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_PROFILE_URI_KEY]
        }

    val isPrivacyModeEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PRIVACY_MODE_KEY] ?: false
        }

    val themeAccentFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[THEME_ACCENT_KEY] ?: "Purple"
        }

    val wakeUpTimeFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[WAKE_UP_TIME_KEY] ?: "07:00"
        }

    val bedTimeFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[BED_TIME_KEY] ?: "22:00"
        }

    val focusAreasFlow: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[FOCUS_AREAS_KEY]?.split(",")?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()
        }

    val dietaryContextFlow: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[DIETARY_CONTEXT_KEY]?.split(",")?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()
        }

    val dietaryNotesFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[DIETARY_NOTES_KEY] ?: ""
        }

    suspend fun saveUserApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_API_KEY] = key
        }
    }

    suspend fun saveUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name
        }
    }

    suspend fun saveProfileUri(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_PROFILE_URI_KEY] = uri
        }
    }

    suspend fun savePrivacyMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PRIVACY_MODE_KEY] = enabled
        }
    }

    suspend fun saveThemeAccent(colorName: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_ACCENT_KEY] = colorName
        }
    }

    suspend fun saveDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }

    suspend fun saveHaptics(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAPTICS_KEY] = enabled
        }
    }

    suspend fun saveSound(soundName: String) {
        context.dataStore.edit { preferences ->
            preferences[SOUND_KEY] = soundName
        }
    }

    suspend fun saveWakeUpTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[WAKE_UP_TIME_KEY] = time
        }
    }

    suspend fun saveBedTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[BED_TIME_KEY] = time
        }
    }

    suspend fun saveFocusAreas(areas: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[FOCUS_AREAS_KEY] = areas.joinToString(",")
        }
    }

    suspend fun saveDietaryContext(context: Set<String>) {
        this.context.dataStore.edit { preferences ->
            preferences[DIETARY_CONTEXT_KEY] = context.joinToString(",")
        }
    }

    suspend fun saveDietaryNotes(notes: String) {
        context.dataStore.edit { preferences ->
            preferences[DIETARY_NOTES_KEY] = notes
        }
    }
}