package com.nami.peace.util.language

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nami.peace.domain.model.Language
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for LanguageManager and Language model
 * 
 * These tests verify the language selection system without requiring
 * a full UserPreferencesRepository implementation.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class LanguageManagerTest {
    
    private lateinit var context: Context
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }
    
    @Test
    fun `AVAILABLE_LANGUAGES contains all expected languages`() {
        // When
        val languages = Language.AVAILABLE_LANGUAGES
        
        // Then
        assertEquals(9, languages.size) // 8 languages + system default
        assertEquals(Language.SYSTEM_DEFAULT, languages[0])
        
        // Verify all expected languages are present
        val languageCodes = languages.map { it.code }
        assertTrue(languageCodes.contains("system"))
        assertTrue(languageCodes.contains("en"))
        assertTrue(languageCodes.contains("es"))
        assertTrue(languageCodes.contains("fr"))
        assertTrue(languageCodes.contains("de"))
        assertTrue(languageCodes.contains("pt"))
        assertTrue(languageCodes.contains("hi"))
        assertTrue(languageCodes.contains("ja"))
        assertTrue(languageCodes.contains("zh"))
    }
    
    @Test
    fun `Language model has correct properties`() {
        // Test English language
        val english = Language.getByCode("en")
        assertEquals("en", english.code)
        assertEquals("English", english.displayName)
        assertEquals("English", english.nativeName)
        assertNotNull(english.locale)
        
        // Test Spanish language
        val spanish = Language.getByCode("es")
        assertEquals("es", spanish.code)
        assertEquals("Spanish", spanish.displayName)
        assertEquals("Español", spanish.nativeName)
        
        // Test French language
        val french = Language.getByCode("fr")
        assertEquals("fr", french.code)
        assertEquals("French", french.displayName)
        assertEquals("Français", french.nativeName)
    }
    
    @Test
    fun `Language getByCode returns system default for unknown code`() {
        // When
        val result = Language.getByCode("unknown")
        
        // Then
        assertEquals(Language.SYSTEM_DEFAULT, result)
    }
    
    @Test
    fun `Language getByCode returns correct language for valid code`() {
        // Test all valid language codes
        val testCases = mapOf(
            "en" to "English",
            "es" to "Spanish",
            "fr" to "French",
            "de" to "German",
            "pt" to "Portuguese",
            "hi" to "Hindi",
            "ja" to "Japanese",
            "zh" to "Chinese"
        )
        
        testCases.forEach { (code, displayName) ->
            val language = Language.getByCode(code)
            assertEquals(code, language.code)
            assertEquals(displayName, language.displayName)
        }
    }
    
    @Test
    fun `Language SYSTEM_DEFAULT has correct properties`() {
        // When
        val systemDefault = Language.SYSTEM_DEFAULT
        
        // Then
        assertEquals("system", systemDefault.code)
        assertEquals("System Default", systemDefault.displayName)
        assertEquals("System Default", systemDefault.nativeName)
        assertNotNull(systemDefault.locale)
    }
    
    @Test
    fun `All languages have unique codes`() {
        // When
        val languages = Language.AVAILABLE_LANGUAGES
        val codes = languages.map { it.code }
        
        // Then
        assertEquals(codes.size, codes.toSet().size) // No duplicates
    }
    
    @Test
    fun `All languages have non-empty display names`() {
        // When
        val languages = Language.AVAILABLE_LANGUAGES
        
        // Then
        languages.forEach { language ->
            assertTrue("Language ${language.code} has empty display name", 
                language.displayName.isNotEmpty())
            assertTrue("Language ${language.code} has empty native name", 
                language.nativeName.isNotEmpty())
        }
    }
    
    @Test
    fun `All languages have valid locales`() {
        // When
        val languages = Language.AVAILABLE_LANGUAGES
        
        // Then
        languages.forEach { language ->
            assertNotNull("Language ${language.code} has null locale", language.locale)
            assertNotNull("Language ${language.code} has null locale language", 
                language.locale.language)
        }
    }
}
