package com.nami.peace.domain.model

import com.nami.peace.data.local.SubtaskEntity

data class Subtask(
    val id: Int = 0,
    val reminderId: Int,
    val title: String,
    val isCompleted: Boolean = false,
    val order: Int,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toEntity(): SubtaskEntity {
        return SubtaskEntity(
            id = id,
            reminderId = reminderId,
            title = title,
            isCompleted = isCompleted,
            order = order,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromEntity(entity: SubtaskEntity): Subtask {
            return Subtask(
                id = entity.id,
                reminderId = entity.reminderId,
                title = entity.title,
                isCompleted = entity.isCompleted,
                order = entity.order,
                createdAt = entity.createdAt
            )
        }
    }
}
