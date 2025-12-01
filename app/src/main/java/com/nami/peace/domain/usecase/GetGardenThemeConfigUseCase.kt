package com.nami.peace.domain.usecase

import com.nami.peace.domain.repository.GardenRepository
import com.nami.peace.ui.theme.GardenThemeConfig
import com.nami.peace.ui.theme.getGardenThemeConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for getting the current garden theme configuration.
 * 
 * This use case:
 * - Observes the current garden state
 * - Maps the theme to its complete configuration (colors, icons, metadata)
 * - Returns a Flow that updates when the theme changes
 * 
 * Requirements: 18.1, 18.2
 */
class GetGardenThemeConfigUseCase @Inject constructor(
    private val gardenRepository: GardenRepository
) {
    /**
     * Get the current garden theme configuration as a Flow.
     * 
     * @return Flow of GardenThemeConfig, or null if garden state doesn't exist
     */
    operator fun invoke(): Flow<GardenThemeConfig?> {
        return gardenRepository.getGardenState().map { gardenState ->
            gardenState?.let { getGardenThemeConfig(it.theme) }
        }
    }
}
