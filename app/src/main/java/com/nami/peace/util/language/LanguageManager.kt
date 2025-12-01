package com.nami.peace.util.language

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import com.nami.peace.data.repository.UserPreferencesRepository
import com.nami.peace.domain.model.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages language selection and locale changes for the Peace app.
 * 
 * Supports:
 * - Language change without app restart
 * - System default language option
 * - Language persistence across app restarts
 * - Android 13+ per-app language preferences
 */
@Singleton
class LanguageManager @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    /**
     * Get the currently selected language as a Flow
     */
    val currentLanguage: Flow<Language> = userPreferencesRepository.selectedLanguage.map { code ->
        if (code == null) {
            Language.SYSTEM_DEFAULT
        } else {
            Language.getByCode(code)
        }
    }
    
    /**
     * Get the currently selected language synchronously
     */
    suspend fun getCurrentLanguage(): Language {
        return currentLanguage.first()
    }
    
    /**
     * Set the app language and apply it immediately
     * 
     * @param language The language to set (use Language.SYSTEM_DEFAULT for system language)
     * @param activity Optional activity to recreate for immediate effect
     */
    suspend fun setLanguage(language: Language, activity: Activity? = null) {
        // Save to preferences
        if (language.code == "system") {
            userPreferencesRepository.setSelectedLanguage(null)
        } else {
            userPreferencesRepository.setSelectedLanguage(language.code)
        }
        
        // Apply language change
        applyLanguage(language, activity)
    }
    
    /**
     * Apply the language change to the app
     * 
     * On Android 13+ (API 33+), uses per-app language preferences
     * On older versions, recreates the activity with the new locale
     */
    private fun applyLanguage(language: Language, activity: Activity?) {
        val locale = if (language.code == "system") {
            Locale.getDefault()
        } else {
            language.locale
        }
        
        // Set the default locale
        Locale.setDefault(locale)
        
        // Update configuration for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ per-app language preferences
            // The system handles this automatically via locales_config.xml
            // We just need to set the locale and recreate the activity
            activity?.recreate()
        } else {
            // Pre-Android 13: Recreate activity with new locale
            activity?.recreate()
        }
    }
    
    /**
     * Initialize language on app startup
     * Should be called from Application.onCreate() or MainActivity.onCreate()
     */
    suspend fun initializeLanguage(context: Context) {
        val savedLanguage = getCurrentLanguage()
        
        if (savedLanguage.code != "system") {
            // Apply saved language by setting default locale
            Locale.setDefault(savedLanguage.locale)
            
            // Update configuration
            val configuration = Configuration(context.resources.configuration)
            configuration.setLocale(savedLanguage.locale)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                configuration.setLocales(LocaleList(savedLanguage.locale))
            }
            
            // Apply configuration
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
        }
    }
    
    /**
     * Get the current locale being used by the app
     */
    fun getCurrentLocale(context: Context): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
    }
    
    /**
     * Update configuration with the selected language
     * Used for creating a context with the correct locale
     */
    fun updateConfiguration(context: Context, language: Language): Context {
        val locale = if (language.code == "system") {
            Locale.getDefault()
        } else {
            language.locale
        }
        
        Locale.setDefault(locale)
        
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        
        return context.createConfigurationContext(configuration)
    }
    
    /**
     * Get all available languages
     */
    fun getAvailableLanguages(): List<Language> {
        return Language.AVAILABLE_LANGUAGES
    }
}
