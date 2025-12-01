package com.nami.peace.domain.model

import com.nami.peace.data.local.GardenEntity
import com.nami.peace.data.local.GardenTheme

data class GardenState(
    val theme: GardenTheme = GardenTheme.ZEN,
    val growthStage: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCompletionDate: Long? = null,
    val totalTasksCompleted: Int = 0,
    val milestones: List<Int> = listOf(7, 30, 100, 365)
) {
    fun toEntity(): GardenEntity {
        return GardenEntity(
            id = 1,
            theme = theme,
            growthStage = growthStage,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            lastCompletionDate = lastCompletionDate,
            totalTasksCompleted = totalTasksCompleted
        )
    }

    companion object {
        fun fromEntity(entity: GardenEntity): GardenState {
            return GardenState(
                theme = entity.theme,
                growthStage = entity.growthStage,
                currentStreak = entity.currentStreak,
                longestStreak = entity.longestStreak,
                lastCompletionDate = entity.lastCompletionDate,
                totalTasksCompleted = entity.totalTasksCompleted
            )
        }
    }
}
