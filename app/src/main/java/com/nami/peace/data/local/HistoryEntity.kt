package com.nami.peace.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val originalTitle: String,
    val completedTime: Long,
    val status: String, // "Done" or "Missed"
    val priority: com.nami.peace.domain.model.PriorityLevel = com.nami.peace.domain.model.PriorityLevel.MEDIUM,
    val category: com.nami.peace.domain.model.ReminderCategory = com.nami.peace.domain.model.ReminderCategory.GENERAL,
    val nagInfo: String? = null
)
