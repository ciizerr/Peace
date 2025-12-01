package com.nami.peace.di

import android.content.Context
import com.nami.peace.util.alarm.AlarmSoundManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AlarmSoundModule {

    @Provides
    @Singleton
    fun provideAlarmSoundManager(
        @ApplicationContext context: Context
    ): AlarmSoundManager {
        return AlarmSoundManager(context)
    }
}
