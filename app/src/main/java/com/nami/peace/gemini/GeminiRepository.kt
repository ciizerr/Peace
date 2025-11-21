package com.nami.peace.gemini

import com.google.ai.client.generativeai.GenerativeModel
import com.nami.peace.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

class GeminiRepository {

    // The "Backup" Key (Your secure one)
    private val defaultApiKey = BuildConfig.GEMINI_API_KEY

    // Helper to create the model dynamically based on which key we use
    private fun createModel(userKey: String?): GenerativeModel {
        // LOGIC: Use User Key if it exists and isn't empty; otherwise use Default
        val finalKey = if (!userKey.isNullOrBlank()) userKey else defaultApiKey

        Log.d("GEMINI_DEBUG", "Using Key: ${if (finalKey == defaultApiKey) "DEFAULT" else "USER CUSTOM"}")

        return GenerativeModel(
            modelName = "gemini-2.5-flash", // Your working model
            apiKey = finalKey
        )
    }

    suspend fun generateSmartReminder(userInput: String, userCustomKey: String?): String {
        return withContext(Dispatchers.IO) {
            try {
                // Create the model on the fly with the right key
                val model = createModel(userCustomKey)

                val prompt = """
                    You are an assistant for a reminder app.
                    Extract data from this user input: "$userInput"
                    Return ONLY a raw string in this exact format:
                    Title|HH:mm|Frequency
                    Example: Drink Water|08:00|DAILY
                """.trimIndent()

                val response = model.generateContent(prompt)
                response.text?.trim() ?: "Error"
            } catch (e: Exception) {
                Log.e("GEMINI_DEBUG", "Error: ${e.message}")
                "Error"
            }
        }
    }
}