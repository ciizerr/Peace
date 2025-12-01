package com.nami.peace.widget

import android.content.Context
import com.nami.peace.domain.repository.GardenRepository
import com.nami.peace.domain.repository.ReminderRepository
import dagger.hilt.android.EntryPointAccessors

/**
 * Provides data access for widgets.
 * This is a helper object to access Hilt-injected repositories from widget composables.
 * 
 * Implements Requirements 17.2, 17.5
 */
object WidgetDataProvider {
    
    /**
     * Gets the ReminderRepository instance from Hilt.
     */
    fun getReminderRepository(context: Context): ReminderRepository {
        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            WidgetDataEntryPoint::class.java
        )
        return entryPoint.reminderRepository()
    }
    
    /**
     * Gets the GardenRepository instance from Hilt.
     */
    fun getGardenRepository(context: Context): GardenRepository {
        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            WidgetDataEntryPoint::class.java
        )
        return entryPoint.gardenRepository()
    }
}

/**
 * Hilt EntryPoint for accessing repositories from widgets.
 */
@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface WidgetDataEntryPoint {
    fun reminderRepository(): ReminderRepository
    fun gardenRepository(): GardenRepository
}
