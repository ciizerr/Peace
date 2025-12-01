package com.nami.peace.domain.usecase

import com.nami.peace.domain.model.Language
import com.nami.peace.util.language.LanguageManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get the currently selected language
 */
class GetCurrentLanguageUseCase @Inject constructor(
    private val languageManager: LanguageManager
) {
    /**
     * Get the current language as a Flow
     */
    operator fun invoke(): Flow<Language> {
        return languageManager.currentLanguage
    }
    
    /**
     * Get the current language synchronously
     */
    suspend fun getCurrent(): Language {
        return languageManager.getCurrentLanguage()
    }
}
