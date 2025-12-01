package com.nami.peace.domain.usecase

import android.app.Activity
import com.nami.peace.domain.model.Language
import com.nami.peace.util.language.LanguageManager
import javax.inject.Inject

/**
 * Use case to set the app language
 */
class SetLanguageUseCase @Inject constructor(
    private val languageManager: LanguageManager
) {
    /**
     * Set the app language and apply it immediately
     * 
     * @param language The language to set
     * @param activity Optional activity to recreate for immediate effect
     */
    suspend operator fun invoke(language: Language, activity: Activity? = null) {
        languageManager.setLanguage(language, activity)
    }
}
