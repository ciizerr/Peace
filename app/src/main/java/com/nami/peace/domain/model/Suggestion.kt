package com.nami.peace.domain.model

import com.nami.peace.data.local.SuggestionEntity
import com.nami.peace.data.local.SuggestionStatus
import com.nami.peace.data.local.SuggestionType

data class Suggestion(
    val id: Int = 0,
    val type: SuggestionType,
    val reminderId: Int?,
    val title: String,
    val description: String,
    val confidenceScore: Int, // 0-100
    val suggestedValue: String, // JSON-encoded suggestion data
    val createdAt: Long = System.currentTimeMillis(),
    val status: SuggestionStatus = SuggestionStatus.PENDING
) {
    fun toEntity(): SuggestionEntity {
        return SuggestionEntity(
            id = id,
            type = type,
            reminderId = reminderId,
            title = title,
            description = description,
            confidenceScore = confidenceScore,
            suggestedValue = suggestedValue,
            createdAt = createdAt,
            status = status
        )
    }

    companion object {
        fun fromEntity(entity: SuggestionEntity): Suggestion {
            return Suggestion(
                id = entity.id,
                type = entity.type,
                reminderId = entity.reminderId,
                title = entity.title,
                description = entity.description,
                confidenceScore = entity.confidenceScore,
                suggestedValue = entity.suggestedValue,
                createdAt = entity.createdAt,
                status = entity.status
            )
        }
    }
}
