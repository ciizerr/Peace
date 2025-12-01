package com.nami.peace.di

import com.nami.peace.util.attachment.AttachmentManager
import com.nami.peace.util.attachment.AttachmentManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AttachmentModule {
    
    @Binds
    @Singleton
    abstract fun bindAttachmentManager(
        impl: AttachmentManagerImpl
    ): AttachmentManager
}
