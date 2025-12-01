package com.nami.peace.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SuggestionType {
    OPTIMAL_TIME,
    PRIORITY_ADJUSTMENT,
    RECURRING_PATTERN,
    BREAK_REMINDER,
    HABIT_FORMATION,
    TEMPLATE_CREATION,
    FOCUS_SESSION
}

enum class SuggestionStatus {
    PENDING, APPLIED, DISMISSED
}

@Entity(tableName = "suggestions")
data class SuggestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: SuggestionType,
    val reminderId: Int?,
    val title: String,
    val description: String,
    val confidenceScore: Int, // 0-100
    val suggestedValue: String, // JSON-encoded suggestion data
    val createdAt: Long,
    val status: SuggestionStatus
)
