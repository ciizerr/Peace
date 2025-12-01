package com.nami.peace.util.font

import androidx.compose.ui.text.font.FontFamily

/**
 * Interface for managing custom fonts in the application.
 * Provides access to bundled custom fonts and system font fallback.
 */
interface FontManager {
    /**
     * Get all available custom fonts.
     *
     * @return List of all custom fonts bundled with the application
     */
    fun getAllFonts(): List<CustomFont>

    /**
     * Get a specific font by name.
     *
     * @param fontName The name of the font to retrieve
     * @return The FontFamily for the specified font, or null if not found
     */
    fun getFont(fontName: String): FontFamily?

    /**
     * Get the system default font.
     *
     * @return The Android system default FontFamily
     */
    fun getSystemFont(): FontFamily
}
