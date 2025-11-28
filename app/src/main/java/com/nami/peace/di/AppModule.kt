package com.nami.peace.di

import android.app.Application
import androidx.room.Room
import com.nami.peace.data.local.AppDatabase
import com.nami.peace.data.local.HistoryDao
import com.nami.peace.data.local.ReminderDao
import com.nami.peace.data.repository.ReminderRepositoryImpl
import com.nami.peace.domain.repository.ReminderRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

import com.nami.peace.data.repository.dataStore

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "peace_db"
        ).fallbackToDestructiveMigration() // For development simplicity
         .build()
    }

    @Provides
    @Singleton
    fun provideReminderDao(db: AppDatabase): ReminderDao {
        return db.reminderDao()
    }

    @Provides
    @Singleton
    fun provideReminderRepository(dao: ReminderDao): ReminderRepository {
        return ReminderRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideHistoryDao(db: AppDatabase): HistoryDao {
        return db.historyDao()
    }

    @Provides
    @Singleton
    fun provideDataStore(app: Application): androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences> {
        return app.dataStore
    }
}
