package com.nami.peace.util.font

import androidx.compose.ui.text.font.FontFamily
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit tests for FontManager interface and CustomFont data class.
 * Tests the contract and basic functionality of the FontManager system.
 * 
 * Note: Full integration tests with actual font loading from resources
 * are performed in instrumented tests.
 */
class FontManagerTest {

    @Test
    fun `CustomFont data class holds correct properties`() {
        val testFont = CustomFont(
            name = "Test Font",
            fontFamily = FontFamily.Default,
            previewText = "Sample text"
        )
        
        assertEquals("Test Font", testFont.name)
        assertEquals(FontFamily.Default, testFont.fontFamily)
        assertEquals("Sample text", testFont.previewText)
    }

    @Test
    fun `CustomFont has default preview text`() {
        val testFont = CustomFont(
            name = "Test Font",
            fontFamily = FontFamily.Default
        )
        
        assertEquals("The quick brown fox jumps over the lazy dog", testFont.previewText)
    }

    @Test
    fun `FontManager getSystemFont returns default FontFamily`() {
        val fontManager = object : FontManager {
            override fun getAllFonts(): List<CustomFont> = emptyList()
            override fun getFont(fontName: String): FontFamily? = null
            override fun getSystemFont(): FontFamily = FontFamily.Default
        }
        
        val systemFont = fontManager.getSystemFont()
        assertNotNull("System font should not be null", systemFont)
        assertEquals("System font should be FontFamily.Default", FontFamily.Default, systemFont)
    }

    @Test
    fun `FontManager interface contract is satisfied`() {
        // Create a simple implementation to verify the interface works
        val fontManager = object : FontManager {
            private val fonts = mapOf(
                "TestFont" to CustomFont("TestFont", FontFamily.Default)
            )
            
            override fun getAllFonts(): List<CustomFont> = fonts.values.toList()
            override fun getFont(fontName: String): FontFamily? = fonts[fontName]?.fontFamily
            override fun getSystemFont(): FontFamily = FontFamily.Default
        }
        
        // Verify interface methods work
        val allFonts = fontManager.getAllFonts()
        assertEquals(1, allFonts.size)
        assertEquals("TestFont", allFonts[0].name)
        
        val testFont = fontManager.getFont("TestFont")
        assertNotNull(testFont)
        
        val nonExistent = fontManager.getFont("NonExistent")
        assertEquals(null, nonExistent)
        
        val systemFont = fontManager.getSystemFont()
        assertEquals(FontFamily.Default, systemFont)
    }
}
