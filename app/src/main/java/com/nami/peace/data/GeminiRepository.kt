package com.nami.peace.data

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiRepository {

    suspend fun parseReminder(input: String, apiKey: String): ParsedReminder? = withContext(Dispatchers.IO) {
        try {
            val generativeModel = GenerativeModel(
                modelName = "gemini-pro",
                apiKey = apiKey
            )
            
            val prompt = """
                Parse the following user input into a structured reminder.
                Input: "$input"
                
                Return ONLY a JSON object with the following fields:
                - title: String (The core task)
                - time: String (Time in HH:mm format, 24h. If not specified, guess a reasonable time or use null)
                - frequency: String (ONCE, DAILY, WEEKLY)
                - category: String (Health, Work, Personal, Errands, Other)
                
                Example output:
                {
                    "title": "Drink water",
                    "time": "14:00",
                    "frequency": "DAILY",
                    "category": "Health"
                }
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            val text = response.text ?: return@withContext null
            
            // Basic JSON parsing (In a real app, use Gson/Moshi)
            // This is a simplified manual parser for the prototype
            parseJsonToReminder(text)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getCoachQuote(userName: String, focusAreas: Set<String>, apiKey: String): String = withContext(Dispatchers.IO) {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val timeOfDay = when (hour) {
            in 5..11 -> "Morning"
            in 12..17 -> "Afternoon"
            in 18..22 -> "Evening"
            else -> "Night"
        }

        try {
            val generativeModel = GenerativeModel(
                modelName = "gemini-pro",
                apiKey = apiKey
            )
            
            val prompt = """
                You are a gentle, encouraging life coach named "Peace".
                User: $userName
                Focus Areas: ${focusAreas.joinToString(", ")}
                Time of Day: $timeOfDay
                
                Generate a short, calming, 1-sentence greeting or tip for the user based on their focus areas and the current time of day ($timeOfDay).
                Do not be bossy. Be soft and poetic.
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            response.text ?: "Breathe in, breathe out."
        } catch (e: Exception) {
            "Welcome to your moment of peace."
        }
    }

    private fun parseJsonToReminder(json: String): ParsedReminder? {
        // Very naive parser for the prototype to avoid adding Gson dependency right now if not needed
        // In production, definitely use a library.
        try {
            val cleanJson = json.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            
            val title = cleanJson.substringAfter("\"title\": \"").substringBefore("\"")
            val time = cleanJson.substringAfter("\"time\": \"").substringBefore("\"")
            val frequency = cleanJson.substringAfter("\"frequency\": \"").substringBefore("\"")
            val category = cleanJson.substringAfter("\"category\": \"").substringBefore("\"")

            return ParsedReminder(
                title = title,
                time = if (time == "null") null else time,
                frequency = frequency,
                category = category
            )
        } catch (e: Exception) {
            return null
        }
    }
}

data class ParsedReminder(
    val title: String,
    val time: String?,
    val frequency: String,
    val category: String
)
