package com.nami.peace.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ReminderEntity::class,
        HistoryEntity::class,
        SubtaskEntity::class,
        NoteEntity::class,
        AttachmentEntity::class,
        GardenEntity::class,
        SuggestionEntity::class,
        SyncQueueEntity::class,
        CompletionEventEntity::class,
        SuggestionFeedbackEntity::class
    ],
    version = 11,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    abstract fun historyDao(): HistoryDao
    abstract fun subtaskDao(): SubtaskDao
    abstract fun noteDao(): NoteDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun gardenDao(): GardenDao
    abstract fun suggestionDao(): SuggestionDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun completionEventDao(): CompletionEventDao
    abstract fun suggestionFeedbackDao(): SuggestionFeedbackDao
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add new columns to reminders table
        database.execSQL("ALTER TABLE reminders ADD COLUMN customAlarmSoundUri TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE reminders ADD COLUMN customAlarmSoundName TEXT DEFAULT NULL")
        
        // Create subtasks table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS subtasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                reminderId INTEGER NOT NULL,
                title TEXT NOT NULL,
                isCompleted INTEGER NOT NULL DEFAULT 0,
                `order` INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                FOREIGN KEY(reminderId) REFERENCES reminders(id) ON DELETE CASCADE
            )
        """)
        database.execSQL("CREATE INDEX IF NOT EXISTS index_subtasks_reminderId ON subtasks(reminderId)")
        
        // Create notes table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS notes (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                reminderId INTEGER NOT NULL,
                content TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                FOREIGN KEY(reminderId) REFERENCES reminders(id) ON DELETE CASCADE
            )
        """)
        database.execSQL("CREATE INDEX IF NOT EXISTS index_notes_reminderId ON notes(reminderId)")
        
        // Create attachments table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS attachments (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                reminderId INTEGER NOT NULL,
                filePath TEXT NOT NULL,
                thumbnailPath TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                mimeType TEXT NOT NULL,
                FOREIGN KEY(reminderId) REFERENCES reminders(id) ON DELETE CASCADE
            )
        """)
        database.execSQL("CREATE INDEX IF NOT EXISTS index_attachments_reminderId ON attachments(reminderId)")
        
        // Create garden_state table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS garden_state (
                id INTEGER PRIMARY KEY NOT NULL,
                theme TEXT NOT NULL,
                growthStage INTEGER NOT NULL,
                currentStreak INTEGER NOT NULL,
                longestStreak INTEGER NOT NULL,
                lastCompletionDate INTEGER,
                totalTasksCompleted INTEGER NOT NULL
            )
        """)
        
        // Initialize garden_state with default values
        database.execSQL("""
            INSERT INTO garden_state (id, theme, growthStage, currentStreak, longestStreak, lastCompletionDate, totalTasksCompleted)
            VALUES (1, 'ZEN', 0, 0, 0, NULL, 0)
        """)
        
        // Create suggestions table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS suggestions (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                type TEXT NOT NULL,
                reminderId INTEGER,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                confidenceScore INTEGER NOT NULL,
                suggestedValue TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                status TEXT NOT NULL
            )
        """)
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create sync_queue table for offline sync and retry mechanism
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS sync_queue (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                reminderId INTEGER NOT NULL,
                operationType TEXT NOT NULL,
                eventId TEXT,
                retryCount INTEGER NOT NULL DEFAULT 0,
                queuedAt INTEGER NOT NULL,
                lastRetryAt INTEGER,
                lastError TEXT,
                isProcessing INTEGER NOT NULL DEFAULT 0
            )
        """)
        database.execSQL("CREATE INDEX IF NOT EXISTS index_sync_queue_reminderId ON sync_queue(reminderId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_sync_queue_isProcessing ON sync_queue(isProcessing)")
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create completion_events table for ML pattern analysis
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS completion_events (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                reminderId INTEGER NOT NULL,
                title TEXT NOT NULL,
                priority TEXT NOT NULL,
                category TEXT NOT NULL,
                scheduledTimeInMillis INTEGER NOT NULL,
                completedTimeInMillis INTEGER NOT NULL,
                completionDelayInMillis INTEGER NOT NULL,
                wasNagMode INTEGER NOT NULL,
                nagRepetitionIndex INTEGER,
                nagTotalRepetitions INTEGER,
                dayOfWeek INTEGER NOT NULL,
                hourOfDay INTEGER NOT NULL,
                wasRecurring INTEGER NOT NULL,
                recurrenceType TEXT NOT NULL
            )
        """)
        
        // Create indexes for efficient querying
        database.execSQL("CREATE INDEX IF NOT EXISTS index_completion_events_reminderId ON completion_events(reminderId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_completion_events_completedTime ON completion_events(completedTimeInMillis)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_completion_events_category ON completion_events(category)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_completion_events_priority ON completion_events(priority)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_completion_events_hourOfDay ON completion_events(hourOfDay)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_completion_events_dayOfWeek ON completion_events(dayOfWeek)")
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create suggestion_feedback table for ML learning
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS suggestion_feedback (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                suggestionId INTEGER NOT NULL,
                suggestionType TEXT NOT NULL,
                wasAccepted INTEGER NOT NULL,
                feedbackTimestamp INTEGER NOT NULL,
                reminderId INTEGER,
                confidenceScore INTEGER NOT NULL
            )
        """)
        
        // Create indexes for efficient querying
        database.execSQL("CREATE INDEX IF NOT EXISTS index_suggestion_feedback_suggestionType ON suggestion_feedback(suggestionType)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_suggestion_feedback_feedbackTimestamp ON suggestion_feedback(feedbackTimestamp)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_suggestion_feedback_wasAccepted ON suggestion_feedback(wasAccepted)")
        
        // Add performance-critical indexes to existing tables
        // These indexes optimize common query patterns
        
        // Reminders table indexes for faster queries
        database.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_isCompleted ON reminders(isCompleted)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_startTime ON reminders(startTimeInMillis)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_category ON reminders(category)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_priority ON reminders(priority)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_isCompleted_startTime ON reminders(isCompleted, startTimeInMillis)")
        
        // Subtasks table composite index for progress calculation
        database.execSQL("CREATE INDEX IF NOT EXISTS index_subtasks_reminderId_isCompleted ON subtasks(reminderId, isCompleted)")
        
        // Notes table index for chronological ordering
        database.execSQL("CREATE INDEX IF NOT EXISTS index_notes_reminderId_timestamp ON notes(reminderId, timestamp)")
        
        // Attachments table index for chronological ordering
        database.execSQL("CREATE INDEX IF NOT EXISTS index_attachments_reminderId_timestamp ON attachments(reminderId, timestamp)")
        
        // Suggestions table indexes for filtering
        database.execSQL("CREATE INDEX IF NOT EXISTS index_suggestions_status ON suggestions(status)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_suggestions_type_status ON suggestions(type, status)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_suggestions_createdAt ON suggestions(createdAt)")
    }
}
