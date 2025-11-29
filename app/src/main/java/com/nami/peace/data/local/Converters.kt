package com.nami.peace.data.local

import androidx.room.TypeConverter
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.RecurrenceType
import com.nami.peace.domain.model.ReminderCategory

class Converters {

    @TypeConverter
    fun fromPriorityLevel(priority: PriorityLevel): String {
        return priority.name
    }

    @TypeConverter
    fun toPriorityLevel(priority: String): PriorityLevel {
        return PriorityLevel.valueOf(priority)
    }

    @TypeConverter
    fun fromRecurrenceType(recurrence: RecurrenceType): String {
        return recurrence.name
    }

    @TypeConverter
    fun toRecurrenceType(recurrence: String): RecurrenceType {
        return RecurrenceType.valueOf(recurrence)
    }

    @TypeConverter
    fun fromReminderCategory(category: ReminderCategory): String {
        return category.name
    }

    @TypeConverter
    fun toReminderCategory(category: String): ReminderCategory {
        return ReminderCategory.valueOf(category)
    }
    @TypeConverter
    fun fromStringToListInt(value: String?): List<Int> {
        return if (value.isNullOrEmpty()) {
            emptyList()
        } else {
            value.split(",").map { it.toInt() }
        }
    }

    @TypeConverter
    fun fromListIntToString(list: List<Int>?): String {
        return list?.joinToString(",") ?: ""
    }
}
