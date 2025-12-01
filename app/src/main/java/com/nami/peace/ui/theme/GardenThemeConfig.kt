package com.nami.peace.ui.theme

import com.nami.peace.data.local.GardenTheme

/**
 * Complete configuration for a garden theme including colors, icons, and metadata.
 * This provides all the visual elements needed to render a themed Peace Garden.
 */
data class GardenThemeConfig(
    val theme: GardenTheme,
    val displayName: String,
    val description: String,
    val colors: GardenThemeColors,
    val icons: GardenThemeIcons
)

/**
 * Get the complete configuration for a specific garden theme.
 * 
 * @param theme The garden theme to get configuration for
 * @return The complete theme configuration including colors, icons, and metadata
 */
fun getGardenThemeConfig(theme: GardenTheme): GardenThemeConfig {
    return GardenThemeConfig(
        theme = theme,
        displayName = getThemeDisplayName(theme),
        description = getThemeDescription(theme),
        colors = getGardenThemeColors(theme),
        icons = getGardenThemeIcons(theme)
    )
}

/**
 * Get all available garden theme configurations.
 * 
 * @return List of all garden theme configurations
 */
fun getAllGardenThemeConfigs(): List<GardenThemeConfig> {
    return GardenTheme.values().map { getGardenThemeConfig(it) }
}

/**
 * Get the display name for a garden theme.
 * 
 * @param theme The garden theme
 * @return The human-readable display name
 */
private fun getThemeDisplayName(theme: GardenTheme): String {
    return when (theme) {
        GardenTheme.ZEN -> "Zen Garden"
        GardenTheme.FOREST -> "Forest Garden"
        GardenTheme.DESERT -> "Desert Garden"
        GardenTheme.OCEAN -> "Ocean Garden"
    }
}

/**
 * Get the description for a garden theme.
 * 
 * @param theme The garden theme
 * @return A brief description of the theme
 */
private fun getThemeDescription(theme: GardenTheme): String {
    return when (theme) {
        GardenTheme.ZEN -> "Find peace in simplicity with calming grays and minimalist design"
        GardenTheme.FOREST -> "Grow through lush greens and vibrant forest life"
        GardenTheme.DESERT -> "Thrive in warm sands and resilient desert beauty"
        GardenTheme.OCEAN -> "Flow with cool blues and the depths of the ocean"
    }
}
