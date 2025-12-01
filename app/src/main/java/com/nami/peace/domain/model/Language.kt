package com.nami.peace.domain.model

import java.util.Locale

/**
 * Represents a language option available in the app
 */
data class Language(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val locale: Locale
) {
    companion object {
        /**
         * System default language option
         */
        val SYSTEM_DEFAULT = Language(
            code = "system",
            displayName = "System Default",
            nativeName = "System Default",
            locale = Locale.getDefault()
        )
        
        /**
         * All available languages in the app
         */
        val AVAILABLE_LANGUAGES = listOf(
            SYSTEM_DEFAULT,
            Language(
                code = "en",
                displayName = "English",
                nativeName = "English",
                locale = Locale.ENGLISH
            ),
            Language(
                code = "es",
                displayName = "Spanish",
                nativeName = "Español",
                locale = Locale("es")
            ),
            Language(
                code = "fr",
                displayName = "French",
                nativeName = "Français",
                locale = Locale.FRENCH
            ),
            Language(
                code = "de",
                displayName = "German",
                nativeName = "Deutsch",
                locale = Locale.GERMAN
            ),
            Language(
                code = "pt",
                displayName = "Portuguese",
                nativeName = "Português",
                locale = Locale("pt")
            ),
            Language(
                code = "hi",
                displayName = "Hindi",
                nativeName = "हिन्दी",
                locale = Locale("hi")
            ),
            Language(
                code = "ja",
                displayName = "Japanese",
                nativeName = "日本語",
                locale = Locale.JAPANESE
            ),
            Language(
                code = "zh",
                displayName = "Chinese",
                nativeName = "中文",
                locale = Locale.CHINESE
            )
        )
        
        /**
         * Get a language by its code
         */
        fun getByCode(code: String): Language {
            return AVAILABLE_LANGUAGES.find { it.code == code } ?: SYSTEM_DEFAULT
        }
    }
}
