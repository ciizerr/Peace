package com.nami.peace.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    // Preference Keys
    private object PreferencesKeys {
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        val SELECTED_FONT = stringPreferencesKey("selected_font")
        val FONT_PADDING = intPreferencesKey("font_padding")
        val BLUR_INTENSITY = intPreferencesKey("blur_intensity")
        val SLIDESHOW_ENABLED = booleanPreferencesKey("slideshow_enabled")
        val CALENDAR_SYNC_ENABLED = booleanPreferencesKey("calendar_sync_enabled")
        val ML_SUGGESTIONS_ENABLED = booleanPreferencesKey("ml_suggestions_enabled")
        val SUBTASKS_ENABLED = booleanPreferencesKey("subtasks_enabled")
        val ATTACHMENTS_ENABLED = booleanPreferencesKey("attachments_enabled")
        val WIDGETS_ENABLED = booleanPreferencesKey("widgets_enabled")
    }

    // First Launch Detection
    val isFirstLaunch: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_FIRST_LAUNCH] ?: true
    }

    suspend fun setFirstLaunchComplete() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_FIRST_LAUNCH] = false
        }
    }

    // Language Selection
    val selectedLanguage: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SELECTED_LANGUAGE]
    }

    suspend fun setSelectedLanguage(language: String?) {
        dataStore.edit { preferences ->
            if (language != null) {
                preferences[PreferencesKeys.SELECTED_LANGUAGE] = language
            } else {
                preferences.remove(PreferencesKeys.SELECTED_LANGUAGE)
            }
        }
    }

    // Font Selection
    val selectedFont: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SELECTED_FONT]
    }

    suspend fun setSelectedFont(font: String?) {
        dataStore.edit { preferences ->
            if (font != null) {
                preferences[PreferencesKeys.SELECTED_FONT] = font
            } else {
                preferences.remove(PreferencesKeys.SELECTED_FONT)
            }
        }
    }

    // Font Padding (0-20dp)
    val fontPadding: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FONT_PADDING] ?: 0
    }

    suspend fun setFontPadding(padding: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_PADDING] = padding.coerceIn(0, 20)
        }
    }

    // Blur Intensity (0-100)
    val blurIntensity: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BLUR_INTENSITY] ?: 0
    }

    suspend fun setBlurIntensity(intensity: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BLUR_INTENSITY] = intensity.coerceIn(0, 100)
        }
    }

    // Slideshow Enabled
    val slideshowEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SLIDESHOW_ENABLED] ?: false
    }

    suspend fun setSlideshowEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SLIDESHOW_ENABLED] = enabled
        }
    }

    // Calendar Sync Enabled
    val calendarSyncEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CALENDAR_SYNC_ENABLED] ?: false
    }

    suspend fun setCalendarSyncEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CALENDAR_SYNC_ENABLED] = enabled
        }
    }

    // ML Suggestions Enabled
    val mlSuggestionsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ML_SUGGESTIONS_ENABLED] ?: true
    }

    suspend fun setMlSuggestionsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ML_SUGGESTIONS_ENABLED] = enabled
        }
    }

    // Subtasks Enabled
    val subtasksEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SUBTASKS_ENABLED] ?: true
    }

    suspend fun setSubtasksEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SUBTASKS_ENABLED] = enabled
        }
    }

    // Attachments Enabled
    val attachmentsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ATTACHMENTS_ENABLED] ?: true
    }

    suspend fun setAttachmentsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ATTACHMENTS_ENABLED] = enabled
        }
    }

    // Widgets Enabled
    val widgetsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.WIDGETS_ENABLED] ?: true
    }

    suspend fun setWidgetsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.WIDGETS_ENABLED] = enabled
        }
    }
    
    // Generic methods for calendar and other features
    suspend fun saveString(key: String, value: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(key)] = value
        }
    }
    
    suspend fun getString(key: String): String? {
        return dataStore.data.map { preferences ->
            preferences[stringPreferencesKey(key)]
        }.first()
    }
    
    suspend fun saveInt(key: String, value: Int) {
        dataStore.edit { preferences ->
            preferences[intPreferencesKey(key)] = value
        }
    }
    
    suspend fun getInt(key: String): Int? {
        return dataStore.data.map { preferences ->
            preferences[intPreferencesKey(key)]
        }.first()
    }
    
    suspend fun saveLong(key: String, value: Long) {
        dataStore.edit { preferences ->
            preferences[longPreferencesKey(key)] = value
        }
    }
    
    suspend fun getLong(key: String): Long? {
        return dataStore.data.map { preferences ->
            preferences[longPreferencesKey(key)]
        }.first()
    }
}
