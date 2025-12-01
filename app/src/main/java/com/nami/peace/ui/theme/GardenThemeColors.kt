package com.nami.peace.ui.theme

import androidx.compose.ui.graphics.Color
import com.nami.peace.data.local.GardenTheme

/**
 * Color palette for a specific garden theme.
 * Each theme has unique colors for primary, secondary, accent, and background elements.
 */
data class GardenThemeColors(
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val background: Color,
    val surface: Color,
    val onPrimary: Color,
    val onSecondary: Color,
    val onBackground: Color
)

/**
 * Zen Garden Theme Colors
 * Inspired by Japanese zen gardens with calming grays, whites, and subtle greens
 */
private val ZenColors = GardenThemeColors(
    primary = Color(0xFF8E9AAF),      // Soft blue-gray
    secondary = Color(0xFFCBC0D3),    // Light lavender
    accent = Color(0xFF9DB4AB),       // Sage green
    background = Color(0xFFF5F5F5),   // Off-white
    surface = Color(0xFFFFFFFF),      // Pure white
    onPrimary = Color(0xFFFFFFFF),    // White text
    onSecondary = Color(0xFF2D2D2D),  // Dark gray text
    onBackground = Color(0xFF2D2D2D)  // Dark gray text
)

/**
 * Forest Garden Theme Colors
 * Rich greens and earth tones inspired by lush forests
 */
private val ForestColors = GardenThemeColors(
    primary = Color(0xFF2D6A4F),      // Deep forest green
    secondary = Color(0xFF52B788),    // Vibrant green
    accent = Color(0xFF95D5B2),       // Light mint green
    background = Color(0xFFD8F3DC),   // Very light green
    surface = Color(0xFFFFFFFF),      // White
    onPrimary = Color(0xFFFFFFFF),    // White text
    onSecondary = Color(0xFF1B4332),  // Dark green text
    onBackground = Color(0xFF1B4332)  // Dark green text
)

/**
 * Desert Garden Theme Colors
 * Warm sandy tones and terracotta inspired by desert landscapes
 */
private val DesertColors = GardenThemeColors(
    primary = Color(0xFFE76F51),      // Terracotta
    secondary = Color(0xFFF4A261),    // Sandy orange
    accent = Color(0xFFE9C46A),       // Golden sand
    background = Color(0xFFFFF8E7),   // Cream
    surface = Color(0xFFFFFFFF),      // White
    onPrimary = Color(0xFFFFFFFF),    // White text
    onSecondary = Color(0xFF5C3D2E),  // Dark brown text
    onBackground = Color(0xFF5C3D2E)  // Dark brown text
)

/**
 * Ocean Garden Theme Colors
 * Cool blues and aqua tones inspired by the ocean
 */
private val OceanColors = GardenThemeColors(
    primary = Color(0xFF0077B6),      // Deep ocean blue
    secondary = Color(0xFF00B4D8),    // Bright cyan
    accent = Color(0xFF90E0EF),       // Light aqua
    background = Color(0xFFCAF0F8),   // Very light blue
    surface = Color(0xFFFFFFFF),      // White
    onPrimary = Color(0xFFFFFFFF),    // White text
    onSecondary = Color(0xFF03045E),  // Dark navy text
    onBackground = Color(0xFF03045E)  // Dark navy text
)

/**
 * Get the color palette for a specific garden theme.
 * 
 * @param theme The garden theme to get colors for
 * @return The color palette for the specified theme
 */
fun getGardenThemeColors(theme: GardenTheme): GardenThemeColors {
    return when (theme) {
        GardenTheme.ZEN -> ZenColors
        GardenTheme.FOREST -> ForestColors
        GardenTheme.DESERT -> DesertColors
        GardenTheme.OCEAN -> OceanColors
    }
}
