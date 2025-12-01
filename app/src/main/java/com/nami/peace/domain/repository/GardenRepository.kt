package com.nami.peace.domain.repository

import com.nami.peace.domain.model.GardenState
import kotlinx.coroutines.flow.Flow

interface GardenRepository {
    fun getGardenState(): Flow<GardenState?>
    suspend fun getGardenStateOnce(): GardenState?
    suspend fun insertGardenState(gardenState: GardenState)
    suspend fun updateGardenState(gardenState: GardenState)
}
