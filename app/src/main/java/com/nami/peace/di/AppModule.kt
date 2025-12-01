package com.nami.peace.di

import android.app.Application
import androidx.room.Room
import com.nami.peace.data.local.AppDatabase
import com.nami.peace.data.local.AttachmentDao
import com.nami.peace.data.local.GardenDao
import com.nami.peace.data.local.HistoryDao
import com.nami.peace.data.local.NoteDao
import com.nami.peace.data.local.ReminderDao
import com.nami.peace.data.local.SubtaskDao
import com.nami.peace.data.local.SuggestionDao
import com.nami.peace.data.local.SyncQueueDao
import com.nami.peace.data.repository.AttachmentRepositoryImpl
import com.nami.peace.data.repository.GardenRepositoryImpl
import com.nami.peace.data.repository.NoteRepositoryImpl
import com.nami.peace.data.repository.ReminderRepositoryImpl
import com.nami.peace.data.repository.SubtaskRepositoryImpl
import com.nami.peace.data.repository.SuggestionRepositoryImpl
import com.nami.peace.domain.repository.AttachmentRepository
import com.nami.peace.domain.repository.GardenRepository
import com.nami.peace.domain.repository.NoteRepository
import com.nami.peace.domain.repository.ReminderRepository
import com.nami.peace.domain.repository.SubtaskRepository
import com.nami.peace.domain.repository.SuggestionRepository
import com.nami.peace.util.icon.IconManager
import com.nami.peace.util.icon.IoniconsManager
import com.nami.peace.util.font.FontManager
import com.nami.peace.util.font.FontManagerImpl
import dagger.Binds
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
        )
        .addMigrations(
            com.nami.peace.data.local.MIGRATION_7_8,
            com.nami.peace.data.local.MIGRATION_8_9,
            com.nami.peace.data.local.MIGRATION_9_10,
            com.nami.peace.data.local.MIGRATION_10_11
        )
        .fallbackToDestructiveMigration() // For development simplicity
         .build()
    }

    @Provides
    @Singleton
    fun provideReminderDao(db: AppDatabase): ReminderDao {
        return db.reminderDao()
    }

    @Provides
    @Singleton
    fun provideReminderRepository(
        dao: ReminderDao,
        widgetUpdateManager: com.nami.peace.widget.WidgetUpdateManager
    ): ReminderRepository {
        return ReminderRepositoryImpl(dao, widgetUpdateManager)
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

    @Provides
    @Singleton
    fun provideSubtaskDao(db: AppDatabase): SubtaskDao {
        return db.subtaskDao()
    }

    @Provides
    @Singleton
    fun provideSubtaskRepository(dao: SubtaskDao): SubtaskRepository {
        return SubtaskRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideNoteDao(db: AppDatabase): NoteDao {
        return db.noteDao()
    }

    @Provides
    @Singleton
    fun provideNoteRepository(dao: NoteDao): NoteRepository {
        return NoteRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideAttachmentDao(db: AppDatabase): AttachmentDao {
        return db.attachmentDao()
    }

    @Provides
    @Singleton
    fun provideAttachmentRepository(dao: AttachmentDao): AttachmentRepository {
        return AttachmentRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideGardenDao(db: AppDatabase): GardenDao {
        return db.gardenDao()
    }

    @Provides
    @Singleton
    fun provideGardenRepository(
        dao: GardenDao,
        widgetUpdateManager: com.nami.peace.widget.WidgetUpdateManager
    ): GardenRepository {
        return GardenRepositoryImpl(dao, widgetUpdateManager)
    }

    @Provides
    @Singleton
    fun provideSuggestionDao(db: AppDatabase): SuggestionDao {
        return db.suggestionDao()
    }

    @Provides
    @Singleton
    fun provideSuggestionRepository(dao: SuggestionDao): SuggestionRepository {
        return SuggestionRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideSyncQueueDao(db: AppDatabase): com.nami.peace.data.local.SyncQueueDao {
        return db.syncQueueDao()
    }

    @Provides
    @Singleton
    fun provideCompletionEventDao(db: AppDatabase): com.nami.peace.data.local.CompletionEventDao {
        return db.completionEventDao()
    }

    @Provides
    @Singleton
    fun provideSuggestionFeedbackDao(db: AppDatabase): com.nami.peace.data.local.SuggestionFeedbackDao {
        return db.suggestionFeedbackDao()
    }

    @Provides
    @Singleton
    fun provideLearningRepository(dao: com.nami.peace.data.local.SuggestionFeedbackDao): com.nami.peace.domain.repository.LearningRepository {
        return com.nami.peace.data.repository.LearningRepositoryImpl(dao)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class IconModule {
    
    @Binds
    @Singleton
    abstract fun bindIconManager(
        ioniconsManager: IoniconsManager
    ): IconManager
}

@Module
@InstallIn(SingletonComponent::class)
abstract class FontModule {
    
    @Binds
    @Singleton
    abstract fun bindFontManager(
        fontManagerImpl: FontManagerImpl
    ): FontManager
}

@Module
@InstallIn(SingletonComponent::class)
abstract class MLModule {
    
    @Binds
    @Singleton
    abstract fun bindPatternAnalyzer(
        patternAnalyzerImpl: com.nami.peace.domain.ml.PatternAnalyzerImpl
    ): com.nami.peace.domain.ml.PatternAnalyzer
    
    @Binds
    @Singleton
    abstract fun bindSuggestionGenerator(
        suggestionGeneratorImpl: com.nami.peace.domain.ml.SuggestionGeneratorImpl
    ): com.nami.peace.domain.ml.SuggestionGenerator
}
