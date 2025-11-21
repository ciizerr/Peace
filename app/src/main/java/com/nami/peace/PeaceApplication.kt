package com.nami.peace

import android.app.Application
import com.nami.peace.data.PeaceDatabase
import com.nami.peace.data.PeaceRepository

class PeaceApplication : Application() {
    // Lazy initialization of the database and repository
    val database by lazy { PeaceDatabase.getDatabase(this) }
    val repository by lazy { PeaceRepository(database.reminderDao(), database.categoryDao()) }
}
