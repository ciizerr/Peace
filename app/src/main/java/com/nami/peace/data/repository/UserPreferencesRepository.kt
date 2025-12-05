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
}
