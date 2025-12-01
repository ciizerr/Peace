package com.nami.peace.di

import com.nami.peace.util.background.BackgroundImageManager
import com.nami.peace.util.background.BackgroundImageManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BackgroundModule {
    
    @Binds
    @Singleton
    abstract fun bindBackgroundImageManager(
        impl: BackgroundImageManagerImpl
    ): BackgroundImageManager
}
