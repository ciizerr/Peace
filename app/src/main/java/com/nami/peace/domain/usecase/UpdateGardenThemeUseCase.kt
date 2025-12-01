package com.nami.peace.domain.usecase

import com.nami.peace.data.local.GardenTheme
import com.nami.peace.domain.repository.GardenRepository
import javax.inject.Inject

/**
 * Use case for updating the Peace Garden theme.
 * 
 * This use case:
 * - Retrieves the current garden state
 * - Updates the theme while preserving all other state
 * - Persists the updated state to the database
 * 
 * Requirements: 18.1, 18.2, 18.9
 */
class UpdateGardenThemeUseCase @Inject constructor(
    private val gardenRepository: GardenRepository
) {
    /**
     * Update the garden theme.
     * 
     * @param newTheme The new theme to apply
     * @throws IllegalStateException if garden state doesn't exist
     */
    suspend operator fun invoke(newTheme: GardenTheme) {
        val currentState = gardenRepository.getGardenStateOnce()
            ?: throw IllegalStateException("Garden state must be initialized before updating theme")
        
        val updatedState = currentState.copy(theme = newTheme)
        gardenRepository.updateGardenState(updatedState)
    }
}
