package com.nami.peace.domain.usecase

import com.nami.peace.util.feature.FeatureToggleManager
import javax.inject.Inject

/**
 * Use case for enabling or disabling a feature toggle
 */
class SetFeatureStateUseCase @Inject constructor(
    private val featureToggleManager: FeatureToggleManager
) {
    /**
     * Enable or disable a feature
     */
    suspend operator fun invoke(feature: FeatureToggleManager.Feature, enabled: Boolean) {
        featureToggleManager.setFeatureEnabled(feature, enabled)
    }
}
