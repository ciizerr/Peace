package com.nami.peace.util.feature

import com.nami.peace.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages feature toggles for the Peace app.
 * Provides a centralized interface for checking and managing feature flags.
 */
@Singleton
class FeatureToggleManager @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    /**
     * Enum representing all toggleable features in the app
     */
    enum class Feature {
        SUBTASKS,
        ATTACHMENTS,
        WIDGETS,
        ML_SUGGESTIONS,
        CALENDAR_SYNC
    }

    // Flow-based feature state accessors
    val subtasksEnabled: Flow<Boolean> = userPreferencesRepository.subtasksEnabled
    val attachmentsEnabled: Flow<Boolean> = userPreferencesRepository.attachmentsEnabled
    val widgetsEnabled: Flow<Boolean> = userPreferencesRepository.widgetsEnabled
    val mlSuggestionsEnabled: Flow<Boolean> = userPreferencesRepository.mlSuggestionsEnabled
    val calendarSyncEnabled: Flow<Boolean> = userPreferencesRepository.calendarSyncEnabled

    /**
     * Check if a feature is enabled (suspend function for one-time checks)
     */
    suspend fun isFeatureEnabled(feature: Feature): Boolean {
        return when (feature) {
            Feature.SUBTASKS -> userPreferencesRepository.subtasksEnabled.first()
            Feature.ATTACHMENTS -> userPreferencesRepository.attachmentsEnabled.first()
            Feature.WIDGETS -> userPreferencesRepository.widgetsEnabled.first()
            Feature.ML_SUGGESTIONS -> userPreferencesRepository.mlSuggestionsEnabled.first()
            Feature.CALENDAR_SYNC -> userPreferencesRepository.calendarSyncEnabled.first()
        }
    }

    /**
     * Get a Flow for a specific feature's enabled state
     */
    fun getFeatureFlow(feature: Feature): Flow<Boolean> {
        return when (feature) {
            Feature.SUBTASKS -> subtasksEnabled
            Feature.ATTACHMENTS -> attachmentsEnabled
            Feature.WIDGETS -> widgetsEnabled
            Feature.ML_SUGGESTIONS -> mlSuggestionsEnabled
            Feature.CALENDAR_SYNC -> calendarSyncEnabled
        }
    }

    /**
     * Enable or disable a feature
     */
    suspend fun setFeatureEnabled(feature: Feature, enabled: Boolean) {
        when (feature) {
            Feature.SUBTASKS -> userPreferencesRepository.setSubtasksEnabled(enabled)
            Feature.ATTACHMENTS -> userPreferencesRepository.setAttachmentsEnabled(enabled)
            Feature.WIDGETS -> userPreferencesRepository.setWidgetsEnabled(enabled)
            Feature.ML_SUGGESTIONS -> userPreferencesRepository.setMlSuggestionsEnabled(enabled)
            Feature.CALENDAR_SYNC -> userPreferencesRepository.setCalendarSyncEnabled(enabled)
        }
    }

    /**
     * Get all features with their current states
     */
    suspend fun getAllFeatureStates(): Map<Feature, Boolean> {
        return Feature.values().associateWith { feature ->
            isFeatureEnabled(feature)
        }
    }

    /**
     * Reset all features to their default states
     */
    suspend fun resetToDefaults() {
        setFeatureEnabled(Feature.SUBTASKS, true)
        setFeatureEnabled(Feature.ATTACHMENTS, true)
        setFeatureEnabled(Feature.WIDGETS, true)
        setFeatureEnabled(Feature.ML_SUGGESTIONS, true)
        setFeatureEnabled(Feature.CALENDAR_SYNC, false)
    }
}
