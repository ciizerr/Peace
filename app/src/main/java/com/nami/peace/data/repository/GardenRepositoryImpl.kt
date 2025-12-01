package com.nami.peace.data.repository

import com.nami.peace.data.local.GardenDao
import com.nami.peace.domain.model.GardenState
import com.nami.peace.domain.repository.GardenRepository
import com.nami.peace.widget.WidgetUpdateManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GardenRepositoryImpl @Inject constructor(
    private val dao: GardenDao,
    private val widgetUpdateManager: WidgetUpdateManager
) : GardenRepository {

    override fun getGardenState(): Flow<GardenState?> {
        return dao.getGardenState().map { entity ->
            entity?.let { GardenState.fromEntity(it) }
        }
    }

    override suspend fun getGardenStateOnce(): GardenState? {
        return dao.getGardenStateOnce()?.let { GardenState.fromEntity(it) }
    }

    override suspend fun insertGardenState(gardenState: GardenState) {
        dao.insert(gardenState.toEntity())
        // Trigger widget update when garden state changes
        widgetUpdateManager.onGardenStateChanged()
    }

    override suspend fun updateGardenState(gardenState: GardenState) {
        dao.update(gardenState.toEntity())
        // Trigger widget update when garden state changes
        widgetUpdateManager.onGardenStateChanged()
    }
}
