package com.nami.peace.domain.usecase

import com.nami.peace.domain.model.Language
import com.nami.peace.util.language.LanguageManager
import javax.inject.Inject

/**
 * Use case to get all available languages
 */
class GetAvailableLanguagesUseCase @Inject constructor(
    private val languageManager: LanguageManager
) {
    /**
     * Get all available languages including system default
     */
    operator fun invoke(): List<Language> {
        return languageManager.getAvailableLanguages()
    }
}
