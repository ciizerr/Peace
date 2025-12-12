package com.nami.peace.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")



class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val BLUR_ENABLED = androidx.datastore.preferences.core.booleanPreferencesKey("blur_enabled")
    private val SHADOWS_ENABLED = androidx.datastore.preferences.core.booleanPreferencesKey("shadows_enabled")
    private val BLUR_STRENGTH = androidx.datastore.preferences.core.floatPreferencesKey("blur_strength")
    private val BLUR_TINT_ALPHA = androidx.datastore.preferences.core.floatPreferencesKey("blur_tint_alpha")
    private val SHADOW_STYLE = androidx.datastore.preferences.core.stringPreferencesKey("shadow_style")

    // Profile Keys
    private val USER_NAME = stringPreferencesKey("user_name")
    private val USER_PHOTO_URI = stringPreferencesKey("user_photo_uri")
    private val USER_BIO = stringPreferencesKey("user_bio")
    private val USER_OCCUPATION = stringPreferencesKey("user_occupation")
    private val USER_WAKE_TIME = stringPreferencesKey("user_wake_time")
    private val USER_BED_TIME = stringPreferencesKey("user_bed_time")

    val userProfile: Flow<UserProfile> = dataStore.data
        .map { preferences ->
            UserProfile(
                name = preferences[USER_NAME] ?: "",
                photoUri = preferences[USER_PHOTO_URI],
                bio = preferences[USER_BIO] ?: "",
                occupation = preferences[USER_OCCUPATION] ?: "",
                wakeTime = preferences[USER_WAKE_TIME] ?: "",
                bedTime = preferences[USER_BED_TIME] ?: ""
            )
        }


    val blurEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[BLUR_ENABLED] ?: (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
        }

    val shadowsEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[SHADOWS_ENABLED] ?: true
        }

    val blurStrength: Flow<Float> = dataStore.data
        .map { preferences ->
            preferences[BLUR_STRENGTH] ?: 12f
        }

    val blurTintAlpha: Flow<Float> = dataStore.data
        .map { preferences ->
            preferences[BLUR_TINT_ALPHA] ?: 0.5f
        }

    val shadowStyle: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[SHADOW_STYLE] ?: "Subtle"
        }

    suspend fun setBlurEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[BLUR_ENABLED] = enabled
        }
    }

    suspend fun setShadowsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHADOWS_ENABLED] = enabled
        }
    }

    suspend fun setBlurStrength(strength: Float) {
        dataStore.edit { preferences ->
            preferences[BLUR_STRENGTH] = strength
        }
    }

    suspend fun setBlurTintAlpha(alpha: Float) {
        dataStore.edit { preferences ->
            preferences[BLUR_TINT_ALPHA] = alpha
        }
    }

    suspend fun setShadowStyle(style: String) {
        dataStore.edit { preferences ->
            preferences[SHADOW_STYLE] = style
        }
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        dataStore.edit { preferences ->
            preferences[USER_NAME] = profile.name
            if (profile.photoUri != null) {
                preferences[USER_PHOTO_URI] = profile.photoUri
            } else {
                preferences.remove(USER_PHOTO_URI)
            }
            preferences[USER_BIO] = profile.bio
            preferences[USER_OCCUPATION] = profile.occupation
            preferences[USER_WAKE_TIME] = profile.wakeTime
            preferences[USER_BED_TIME] = profile.bedTime
        }
    }
}
