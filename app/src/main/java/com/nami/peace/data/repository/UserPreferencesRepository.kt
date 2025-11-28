package com.nami.peace.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val SCHEDULING_MODE = stringPreferencesKey("scheduling_mode")

    val schedulingMode: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[SCHEDULING_MODE] ?: "FLEXIBLE"
        }

    suspend fun setSchedulingMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[SCHEDULING_MODE] = mode
        }
    }
}
