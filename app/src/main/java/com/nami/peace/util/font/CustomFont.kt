package com.nami.peace.util.font

import androidx.compose.ui.text.font.FontFamily

/**
 * Represents a custom font available in the application.
 *
 * @property name The display name of the font (e.g., "Poppins", "Lato")
 * @property fontFamily The Compose FontFamily object for this font
 * @property previewText Sample text to display when previewing this font
 */
data class CustomFont(
    val name: String,
    val fontFamily: FontFamily,
    val previewText: String = "The quick brown fox jumps over the lazy dog"
)
