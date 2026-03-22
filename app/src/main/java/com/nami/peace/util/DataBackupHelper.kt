package com.nami.peace.util

import com.nami.peace.domain.model.Reminder
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.RecurrenceType
import com.nami.peace.domain.model.ReminderCategory
import org.json.JSONArray
import org.json.JSONObject

class DataBackupHelper {
    companion object {
        fun exportToJson(reminders: List<Reminder>, history: List<Reminder>): String {
            val root = JSONObject()
            
            val remindersArray = JSONArray()
            reminders.forEach { r ->
                val obj = JSONObject().apply {
                    put("id", r.id)
                    put("title", r.title)
                    put("priority", r.priority.name)
                    put("startTimeInMillis", r.startTimeInMillis)
                    put("recurrenceType", r.recurrenceType.name)
                    put("isNagModeEnabled", r.isNagModeEnabled)
                    put("nagIntervalInMillis", r.nagIntervalInMillis ?: JSONObject.NULL)
                    put("nagTotalRepetitions", r.nagTotalRepetitions)
                    put("currentRepetitionIndex", r.currentRepetitionIndex)
                    put("isCompleted", r.isCompleted)
                    put("isEnabled", r.isEnabled)
                    put("category", r.category.name)
                    put("originalStartTimeInMillis", r.originalStartTimeInMillis)
                    put("completedTime", r.completedTime ?: JSONObject.NULL)
                    put("isAbandoned", r.isAbandoned)
                    put("isInNestedSnoozeLoop", r.isInNestedSnoozeLoop)
                    put("nestedSnoozeStartTime", r.nestedSnoozeStartTime ?: JSONObject.NULL)
                    put("isStrictSchedulingEnabled", r.isStrictSchedulingEnabled)
                    put("dateInMillis", r.dateInMillis ?: JSONObject.NULL)
                    put("notes", r.notes ?: JSONObject.NULL)
                    
                    val daysArray = JSONArray()
                    r.daysOfWeek.forEach { daysArray.put(it) }
                    put("daysOfWeek", daysArray)
                }
                remindersArray.put(obj)
            }
            root.put("reminders", remindersArray)
            
            val historyArray = JSONArray()
            history.forEach { h ->
                val obj = JSONObject().apply {
                    put("id", h.id)
                    put("title", h.title)
                    put("completedTime", h.completedTime ?: JSONObject.NULL)
                    put("isAbandoned", h.isAbandoned)
                }
                historyArray.put(obj)
            }
            root.put("history", historyArray)
            
            return root.toString(4)
        }

        fun parseJson(json: String): Pair<List<Reminder>, List<Reminder>> {
            val root = JSONObject(json)
            val reminders = mutableListOf<Reminder>()
            val history = mutableListOf<Reminder>()
            
            root.optJSONArray("reminders")?.let { array ->
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    reminders.add(parseReminder(obj, false))
                }
            }
            
            root.optJSONArray("history")?.let { array ->
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    history.add(parseReminder(obj, true))
                }
            }
            
            return reminders to history
        }

        private fun parseReminder(obj: JSONObject, isHistory: Boolean): Reminder {
            return Reminder(
                id = if (isHistory) 0 else obj.optInt("id", 0),
                title = obj.getString("title"),
                priority = if (obj.has("priority")) PriorityLevel.valueOf(obj.getString("priority")) else PriorityLevel.MEDIUM,
                startTimeInMillis = obj.optLong("startTimeInMillis", 0),
                recurrenceType = if (obj.has("recurrenceType")) RecurrenceType.valueOf(obj.getString("recurrenceType")) else RecurrenceType.ONE_TIME,
                isNagModeEnabled = obj.optBoolean("isNagModeEnabled", false),
                nagIntervalInMillis = if (obj.isNull("nagIntervalInMillis")) null else obj.getLong("nagIntervalInMillis"),
                nagTotalRepetitions = obj.optInt("nagTotalRepetitions", 0),
                currentRepetitionIndex = obj.optInt("currentRepetitionIndex", 0),
                isCompleted = obj.optBoolean("isCompleted", isHistory),
                isEnabled = obj.optBoolean("isEnabled", !isHistory),
                category = if (obj.has("category")) ReminderCategory.valueOf(obj.getString("category")) else ReminderCategory.GENERAL,
                originalStartTimeInMillis = obj.optLong("originalStartTimeInMillis", 0),
                completedTime = if (obj.isNull("completedTime")) null else obj.getLong("completedTime"),
                isAbandoned = obj.optBoolean("isAbandoned", false),
                isInNestedSnoozeLoop = obj.optBoolean("isInNestedSnoozeLoop", false),
                nestedSnoozeStartTime = if (obj.isNull("nestedSnoozeStartTime")) null else obj.getLong("nestedSnoozeStartTime"),
                isStrictSchedulingEnabled = obj.optBoolean("isStrictSchedulingEnabled", false),
                dateInMillis = if (obj.isNull("dateInMillis")) null else obj.getLong("dateInMillis"),
                notes = if (obj.isNull("notes")) null else obj.getString("notes"),
                daysOfWeek = if (obj.has("daysOfWeek")) {
                    val dArray = obj.getJSONArray("daysOfWeek")
                    List(dArray.length()) { dArray.getInt(it) }
                } else emptyList()
            )
        }
    }
}
