package com.nami.peace.domain.usecase

import com.nami.peace.domain.model.GardenState
import com.nami.peace.domain.model.GrowthStage
import com.nami.peace.domain.repository.GardenRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Information about the current growth stage
 */
data class GrowthStageInfo(
    val currentStage: GrowthStage,
    val nextStage: GrowthStage?,
    val progressToNextStage: Int, // 0-100
    val tasksCompleted: Int,
    val tasksNeededForNextStage: Int?
)

/**
 * Use case for retrieving detailed information about the current growth stage.
 */
class GetGrowthStageInfoUseCase @Inject constructor(
    private val gardenRepository: GardenRepository
) {
    /**
     * Get a flow of growth stage information that updates when the garden state changes.
     */
    operator fun invoke(): Flow<GrowthStageInfo> {
        return gardenRepository.getGardenState().map { gardenState ->
            val state = gardenState ?: GardenState()
            calculateGrowthStageInfo(state)
        }
    }

    /**
     * Get the current growth stage information once.
     */
    suspend fun getOnce(): GrowthStageInfo {
        val state = gardenRepository.getGardenStateOnce() ?: GardenState()
        return calculateGrowthStageInfo(state)
    }

    private fun calculateGrowthStageInfo(state: GardenState): GrowthStageInfo {
        val currentStage = GrowthStage.fromTaskCount(state.totalTasksCompleted)
        val nextStage = GrowthStage.values().getOrNull(currentStage.stage + 1)
        val progressToNextStage = GrowthStage.calculateProgressToNextStage(state.totalTasksCompleted)
        val tasksNeededForNextStage = nextStage?.tasksRequired

        return GrowthStageInfo(
            currentStage = currentStage,
            nextStage = nextStage,
            progressToNextStage = progressToNextStage,
            tasksCompleted = state.totalTasksCompleted,
            tasksNeededForNextStage = tasksNeededForNextStage
        )
    }
}
