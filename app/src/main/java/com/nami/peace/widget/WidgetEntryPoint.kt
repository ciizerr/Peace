package com.nami.peace.widget

import com.nami.peace.domain.repository.GardenRepository
import com.nami.peace.domain.repository.ReminderRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt EntryPoint for accessing dependencies in widgets.
 * Widgets cannot use @Inject directly, so we define an entry point.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun reminderRepository(): ReminderRepository
    fun gardenRepository(): GardenRepository
    fun widgetUpdateManager(): WidgetUpdateManager
}
