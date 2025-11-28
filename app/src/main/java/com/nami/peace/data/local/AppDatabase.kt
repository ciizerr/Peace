package com.nami.peace.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nami.peace.data.local.HistoryDao
import com.nami.peace.data.local.HistoryEntity
import com.nami.peace.data.local.ReminderEntity

@Database(entities = [ReminderEntity::class, HistoryEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    abstract fun historyDao(): HistoryDao
}
