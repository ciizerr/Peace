package com.nami.peace.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nami.peace.data.local.ReminderEntity

@Database(entities = [ReminderEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
}
