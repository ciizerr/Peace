package com.nami.peace.domain.usecase

import com.nami.peace.domain.model.GardenState
import com.nami.peace.domain.model.GrowthStage
import com.nami.peace.domain.repository.GardenRepository
import javax.inject.Inject

/**
 * Use case for advancing the Peace Garden growth stage when tasks are completed.
 * 
 * This use case:
 * 1. Retrieves the current garden state
 * 2. Increments the total tasks completed
 * 3. Calculates the new growth stage based on task count
 * 4. Updates the garden state if the stage has advanced
 * 5. Returns whether a stage advancement occurred
 */
class AdvanceGrowthStageUseCase @Inject constructor(
    private val gardenRepository: GardenRepository
) {
    /**
     * Advance the growth stage by completing a task.
     * 
     * @return Pair of (stageAdvanced: Boolean, newStage: GrowthStage?)
     *         stageAdvanced is true if the growth stage increased
     *         newStage is the new growth stage if advancement occurred, null otherwise
     */
    suspend operator fun invoke(): Pair<Boolean, GrowthStage?> {
        // Get current garden state or create default
        val currentState = gardenRepository.getGardenStateOnce() ?: GardenState()
        
        // Calculate new task count
        val newTaskCount = currentState.totalTasksCompleted + 1
        
        // Determine current and new growth stages
        val currentStage = GrowthStage.fromTaskCount(currentState.totalTasksCompleted)
        val newStage = GrowthStage.fromTaskCount(newTaskCount)
        
        // Update garden state with new task count
        val updatedState = currentState.copy(
            totalTasksCompleted = newTaskCount,
            growthStage = newStage.stage
        )
        
        // Save updated state
        if (currentState.totalTasksCompleted == 0) {
            gardenRepository.insertGardenState(updatedState)
        } else {
            gardenRepository.updateGardenState(updatedState)
        }
        
        // Return whether stage advanced and the new stage
        val stageAdvanced = newStage.stage > currentStage.stage
        return Pair(stageAdvanced, if (stageAdvanced) newStage else null)
    }
}
