package com.nami.peace.domain.usecase

import com.nami.peace.util.feature.FeatureToggleManager
import javax.inject.Inject

/**
 * Use case for getting all feature toggle states
 */
class GetAllFeatureStatesUseCase @Inject constructor(
    private val featureToggleManager: FeatureToggleManager
) {
    /**
     * Get a map of all features and their current states
     */
    suspend operator fun invoke(): Map<FeatureToggleManager.Feature, Boolean> {
        return featureToggleManager.getAllFeatureStates()
    }
}
