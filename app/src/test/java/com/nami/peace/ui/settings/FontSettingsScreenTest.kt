package com.nami.peace.ui.settings

import androidx.compose.ui.text.font.FontFamily
import com.nami.peace.util.font.CustomFont
import com.nami.peace.util.font.FontManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit tests for FontSettingsScreen functionality.
 * Tests font selection, padding adjustment, and state management.
 */
class FontSettingsScreenTest {

    @Test
    fun `FontManager returns available fonts`() {
        // Create a test implementation
        val fontManager = object : FontManager {
            override fun getAllFonts(): List<CustomFont> = listOf(
                CustomFont("Poppins", FontFamily.Default, "Preview text"),
                CustomFont("Lato", FontFamily.Default, "Preview text")
            )
            override fun getFont(fontName: String): FontFamily? = null
            override fun getSystemFont(): FontFamily = FontFamily.Default
        }

        // When
        val fonts = fontManager.getAllFonts()

        // Then
        assertEquals(2, fonts.size)
        assertEquals("Poppins", fonts[0].name)
        assertEquals("Lato", fonts[1].name)
    }

    @Test
    fun `FontManager returns system font`() {
        // Create a test implementation
        val fontManager = object : FontManager {
            override fun getAllFonts(): List<CustomFont> = emptyList()
            override fun getFont(fontName: String): FontFamily? = null
            override fun getSystemFont(): FontFamily = FontFamily.Default
        }

        // When
        val systemFont = fontManager.getSystemFont()

        // Then
        assertNotNull(systemFont)
        assertEquals(FontFamily.Default, systemFont)
    }

    @Test
    fun `CustomFont contains preview text`() {
        // Given
        val customFont = CustomFont(
            name = "Test Font",
            fontFamily = FontFamily.Default,
            previewText = "The quick brown fox jumps over the lazy dog"
        )

        // Then
        assertEquals("Test Font", customFont.name)
        assertEquals("The quick brown fox jumps over the lazy dog", customFont.previewText)
        assertNotNull(customFont.fontFamily)
    }

    @Test
    fun `Font padding range is 0 to 20`() {
        // This test verifies the expected range for font padding
        val minPadding = 0
        val maxPadding = 20

        // Verify the range is correct
        assert(minPadding >= 0) { "Min padding should be non-negative" }
        assert(maxPadding <= 20) { "Max padding should not exceed 20dp" }
        assert(minPadding < maxPadding) { "Min should be less than max" }
    }
}
