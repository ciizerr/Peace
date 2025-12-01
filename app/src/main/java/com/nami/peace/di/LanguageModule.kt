package com.nami.peace.di

import com.nami.peace.data.repository.UserPreferencesRepository
import com.nami.peace.util.language.LanguageManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LanguageModule {
    
    @Provides
    @Singleton
    fun provideLanguageManager(
        userPreferencesRepository: UserPreferencesRepository
    ): LanguageManager {
        return LanguageManager(userPreferencesRepository)
    }
}
