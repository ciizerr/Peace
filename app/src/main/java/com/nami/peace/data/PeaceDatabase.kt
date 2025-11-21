package com.nami.peace.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Reminder::class, Category::class], version = 2, exportSchema = false)
abstract class PeaceDatabase : RoomDatabase() {

    abstract fun reminderDao(): ReminderDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var Instance: PeaceDatabase? = null

        fun getDatabase(context: Context): PeaceDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, PeaceDatabase::class.java, "peace_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
