package com.nami.peace.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class GardenTheme {
    ZEN, FOREST, DESERT, OCEAN
}

@Entity(tableName = "garden_state")
data class GardenEntity(
    @PrimaryKey val id: Int = 1,
    val theme: GardenTheme,
    val growthStage: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastCompletionDate: Long?,
    val totalTasksCompleted: Int
)
