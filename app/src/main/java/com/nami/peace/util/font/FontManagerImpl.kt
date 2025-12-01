package com.nami.peace.util.font

import android.content.Context
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.nami.peace.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of FontManager that loads custom fonts from the res/font directory.
 * Provides access to bundled fonts and system font fallback.
 */
@Singleton
class FontManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : FontManager {

    private val fonts: Map<String, CustomFont> by lazy {
        loadCustomFonts()
    }

    /**
     * Load all custom fonts from the res/font directory.
     * Currently supports: Poppins, Lato, Bodoni Moda, and Loves.
     */
    private fun loadCustomFonts(): Map<String, CustomFont> {
        val fontMap = mutableMapOf<String, CustomFont>()

        try {
            // Poppins font family
            val poppins = FontFamily(
                Font(R.font.poppins_regular, FontWeight.Normal, FontStyle.Normal),
                Font(R.font.poppins_medium, FontWeight.Medium, FontStyle.Normal),
                Font(R.font.poppins_bold, FontWeight.Bold, FontStyle.Normal)
            )
            fontMap["Poppins"] = CustomFont(
                name = "Poppins",
                fontFamily = poppins,
                previewText = "The quick brown fox jumps over the lazy dog"
            )

            // Lato font family
            val lato = FontFamily(
                Font(R.font.lato_regular, FontWeight.Normal, FontStyle.Normal),
                Font(R.font.lato_bold, FontWeight.Bold, FontStyle.Normal),
                Font(R.font.lato_italic, FontWeight.Normal, FontStyle.Italic)
            )
            fontMap["Lato"] = CustomFont(
                name = "Lato",
                fontFamily = lato,
                previewText = "The quick brown fox jumps over the lazy dog"
            )

            // Bodoni Moda font family
            val bodoniModa = FontFamily(
                Font(R.font.bodoni_moda_regular, FontWeight.Normal, FontStyle.Normal),
                Font(R.font.bodoni_moda_italic, FontWeight.Normal, FontStyle.Italic)
            )
            fontMap["Bodoni Moda"] = CustomFont(
                name = "Bodoni Moda",
                fontFamily = bodoniModa,
                previewText = "The quick brown fox jumps over the lazy dog"
            )

            // Loves font family
            val loves = FontFamily(
                Font(R.font.loves_regular, FontWeight.Normal, FontStyle.Normal)
            )
            fontMap["Loves"] = CustomFont(
                name = "Loves",
                fontFamily = loves,
                previewText = "The quick brown fox jumps over the lazy dog"
            )

        } catch (e: Exception) {
            // Log error but don't crash - will fall back to system font
            android.util.Log.e("FontManagerImpl", "Error loading custom fonts", e)
        }

        return fontMap
    }

    override fun getAllFonts(): List<CustomFont> {
        return fonts.values.toList()
    }

    override fun getFont(fontName: String): FontFamily? {
        return fonts[fontName]?.fontFamily
    }

    override fun getSystemFont(): FontFamily {
        return FontFamily.Default
    }
}
