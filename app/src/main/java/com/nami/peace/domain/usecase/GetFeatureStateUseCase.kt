package com.nami.peace.domain.usecase

import com.nami.peace.util.feature.FeatureToggleManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting the state of a specific feature toggle
 */
class GetFeatureStateUseCase @Inject constructor(
    private val featureToggleManager: FeatureToggleManager
) {
    /**
     * Get a Flow of the feature's enabled state
     */
    operator fun invoke(feature: FeatureToggleManager.Feature): Flow<Boolean> {
        return featureToggleManager.getFeatureFlow(feature)
    }

    /**
     * Get the current state of a feature (one-time check)
     */
    suspend fun check(feature: FeatureToggleManager.Feature): Boolean {
        return featureToggleManager.isFeatureEnabled(feature)
    }
}
