package com.nami.peace.ui.theme

import com.nami.peace.data.local.GardenTheme

/**
 * Icon configuration for a specific garden theme.
 * Each theme uses different Ionicons to represent growth stages and theme elements.
 */
data class GardenThemeIcons(
    val themeIcon: String,           // Main icon representing the theme
    val growthStage0: String,        // Seed/beginning stage
    val growthStage1: String,        // Early growth
    val growthStage2: String,        // Small plant
    val growthStage3: String,        // Growing plant
    val growthStage4: String,        // Medium plant
    val growthStage5: String,        // Large plant
    val growthStage6: String,        // Flowering
    val growthStage7: String,        // Full bloom
    val growthStage8: String,        // Mature
    val growthStage9: String,        // Complete/mastery
    val streakIcon: String,          // Icon for streak display
    val milestoneIcon: String        // Icon for milestone achievements
)

/**
 * Zen Garden Theme Icons
 * Uses minimalist, nature-inspired icons
 */
private val ZenIcons = GardenThemeIcons(
    themeIcon = "leaf",
    growthStage0 = "ellipse",              // Seed
    growthStage1 = "radio_button_on",      // Sprouting
    growthStage2 = "leaf_outline",         // Small leaf
    growthStage3 = "leaf",                 // Growing leaf
    growthStage4 = "flower_outline",       // Budding
    growthStage5 = "flower",               // Small flower
    growthStage6 = "rose_outline",         // Blooming
    growthStage7 = "rose",                 // Full bloom
    growthStage8 = "sparkles_outline",     // Radiant
    growthStage9 = "sparkles",             // Enlightenment
    streakIcon = "flame",
    milestoneIcon = "trophy"
)

/**
 * Forest Garden Theme Icons
 * Uses tree and forest-related icons
 */
private val ForestIcons = GardenThemeIcons(
    themeIcon = "leaf",
    growthStage0 = "ellipse",              // Seed
    growthStage1 = "water_outline",        // Watered seed
    growthStage2 = "leaf_outline",         // Sprout
    growthStage3 = "leaf",                 // Seedling
    growthStage4 = "git_branch_outline",   // Young tree
    growthStage5 = "git_branch",           // Growing tree
    growthStage6 = "git_network_outline",  // Branching tree
    growthStage7 = "git_network",          // Full tree
    growthStage8 = "bonfire_outline",      // Forest
    growthStage9 = "bonfire",              // Ancient forest
    streakIcon = "flame",
    milestoneIcon = "trophy"
)

/**
 * Desert Garden Theme Icons
 * Uses cactus and desert-related icons
 */
private val DesertIcons = GardenThemeIcons(
    themeIcon = "sunny",
    growthStage0 = "ellipse",              // Seed
    growthStage1 = "water_outline",        // Watered
    growthStage2 = "triangle_outline",     // Small cactus
    growthStage3 = "triangle",             // Growing cactus
    growthStage4 = "caret_up_outline",     // Tall cactus
    growthStage5 = "caret_up",             // Mature cactus
    growthStage6 = "star_outline",         // Flowering cactus
    growthStage7 = "star",                 // Blooming cactus
    growthStage8 = "sunny_outline",        // Desert oasis
    growthStage9 = "sunny",                // Thriving oasis
    streakIcon = "flame",
    milestoneIcon = "trophy"
)

/**
 * Ocean Garden Theme Icons
 * Uses water and marine-related icons
 */
private val OceanIcons = GardenThemeIcons(
    themeIcon = "water",
    growthStage0 = "ellipse",              // Seed/pearl
    growthStage1 = "water_outline",        // Bubble
    growthStage2 = "fish_outline",         // Small fish
    growthStage3 = "fish",                 // Growing fish
    growthStage4 = "boat_outline",         // Small coral
    growthStage5 = "boat",                 // Growing coral
    growthStage6 = "navigate_outline",     // Reef
    growthStage7 = "navigate",             // Thriving reef
    growthStage8 = "planet_outline",       // Ocean ecosystem
    growthStage9 = "planet",               // Complete ocean
    streakIcon = "flame",
    milestoneIcon = "trophy"
)

/**
 * Get the icon configuration for a specific garden theme.
 * 
 * @param theme The garden theme to get icons for
 * @return The icon configuration for the specified theme
 */
fun getGardenThemeIcons(theme: GardenTheme): GardenThemeIcons {
    return when (theme) {
        GardenTheme.ZEN -> ZenIcons
        GardenTheme.FOREST -> ForestIcons
        GardenTheme.DESERT -> DesertIcons
        GardenTheme.OCEAN -> OceanIcons
    }
}

/**
 * Get the icon name for a specific growth stage in a theme.
 * 
 * @param theme The garden theme
 * @param stage The growth stage (0-9)
 * @return The icon name for the specified stage, or the theme icon if stage is out of range
 */
fun getGrowthStageIcon(theme: GardenTheme, stage: Int): String {
    val icons = getGardenThemeIcons(theme)
    return when (stage) {
        0 -> icons.growthStage0
        1 -> icons.growthStage1
        2 -> icons.growthStage2
        3 -> icons.growthStage3
        4 -> icons.growthStage4
        5 -> icons.growthStage5
        6 -> icons.growthStage6
        7 -> icons.growthStage7
        8 -> icons.growthStage8
        9 -> icons.growthStage9
        else -> icons.themeIcon
    }
}
