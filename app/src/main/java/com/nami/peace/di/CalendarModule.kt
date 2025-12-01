package com.nami.peace.di

import android.content.Context
import com.nami.peace.data.repository.SyncQueueRepository
import com.nami.peace.data.repository.UserPreferencesRepository
import com.nami.peace.util.calendar.CalendarManager
import com.nami.peace.util.calendar.CalendarManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CalendarModule {
    
    @Provides
    @Singleton
    fun provideCalendarManager(
        @ApplicationContext context: Context,
        preferencesRepository: UserPreferencesRepository,
        syncQueueRepository: SyncQueueRepository
    ): CalendarManager {
        return CalendarManagerImpl(context, preferencesRepository, syncQueueRepository)
    }
}
