package com.nami.peace.ui.theme

import androidx.compose.ui.graphics.Color
import com.nami.peace.data.local.GardenTheme
import com.nami.peace.domain.model.GrowthStage

/**
 * Visual configuration for a growth stage in a specific theme
 */
data class GrowthStageVisual(
    val iconName: String,
    val color: Color,
    val description: String
)

/**
 * Provides theme-specific visualizations for each growth stage
 */
object GrowthStageVisuals {
    
    /**
     * Get the visual configuration for a specific growth stage and theme
     */
    fun getVisual(stage: GrowthStage, theme: GardenTheme): GrowthStageVisual {
        return when (theme) {
            GardenTheme.ZEN -> getZenVisual(stage)
            GardenTheme.FOREST -> getForestVisual(stage)
            GardenTheme.DESERT -> getDesertVisual(stage)
            GardenTheme.OCEAN -> getOceanVisual(stage)
        }
    }

    private fun getZenVisual(stage: GrowthStage): GrowthStageVisual {
        return when (stage) {
            GrowthStage.SEED -> GrowthStageVisual(
                iconName = "ionicons_leaf_outline",
                color = Color(0xFFE8E8E8),
                description = "A single stone in the garden"
            )
            GrowthStage.SPROUT -> GrowthStageVisual(
                iconName = "ionicons_leaf",
                color = Color(0xFFD4D4D4),
                description = "Bamboo shoots emerging"
            )
            GrowthStage.SEEDLING -> GrowthStageVisual(
                iconName = "ionicons_flower_outline",
                color = Color(0xFFC0C0C0),
                description = "Young bamboo stalks"
            )
            GrowthStage.YOUNG_PLANT -> GrowthStageVisual(
                iconName = "ionicons_flower",
                color = Color(0xFFB0B0B0),
                description = "Growing bamboo grove"
            )
            GrowthStage.MATURE_PLANT -> GrowthStageVisual(
                iconName = "ionicons_rose_outline",
                color = Color(0xFFA0A0A0),
                description = "Mature bamboo forest"
            )
            GrowthStage.BUDDING -> GrowthStageVisual(
                iconName = "ionicons_rose",
                color = Color(0xFF909090),
                description = "Cherry blossoms budding"
            )
            GrowthStage.FLOWERING -> GrowthStageVisual(
                iconName = "ionicons_sparkles_outline",
                color = Color(0xFFFFB7C5),
                description = "Cherry blossoms in full bloom"
            )
            GrowthStage.FRUITING -> GrowthStageVisual(
                iconName = "ionicons_sparkles",
                color = Color(0xFFFF9DB5),
                description = "Lotus flowers blooming"
            )
            GrowthStage.ABUNDANT -> GrowthStageVisual(
                iconName = "ionicons_star_outline",
                color = Color(0xFFFF83A5),
                description = "Perfect zen harmony"
            )
            GrowthStage.TRANSCENDENT -> GrowthStageVisual(
                iconName = "ionicons_star",
                color = Color(0xFFFF6995),
                description = "Enlightenment achieved"
            )
        }
    }

    private fun getForestVisual(stage: GrowthStage): GrowthStageVisual {
        return when (stage) {
            GrowthStage.SEED -> GrowthStageVisual(
                iconName = "ionicons_leaf_outline",
                color = Color(0xFF8B7355),
                description = "Acorn in the soil"
            )
            GrowthStage.SPROUT -> GrowthStageVisual(
                iconName = "ionicons_leaf",
                color = Color(0xFF9ACD32),
                description = "First green shoot"
            )
            GrowthStage.SEEDLING -> GrowthStageVisual(
                iconName = "ionicons_flower_outline",
                color = Color(0xFF7CFC00),
                description = "Young sapling"
            )
            GrowthStage.YOUNG_PLANT -> GrowthStageVisual(
                iconName = "ionicons_flower",
                color = Color(0xFF32CD32),
                description = "Growing tree"
            )
            GrowthStage.MATURE_PLANT -> GrowthStageVisual(
                iconName = "ionicons_rose_outline",
                color = Color(0xFF228B22),
                description = "Mature oak tree"
            )
            GrowthStage.BUDDING -> GrowthStageVisual(
                iconName = "ionicons_rose",
                color = Color(0xFF006400),
                description = "Wildflowers appearing"
            )
            GrowthStage.FLOWERING -> GrowthStageVisual(
                iconName = "ionicons_sparkles_outline",
                color = Color(0xFFFFD700),
                description = "Forest in bloom"
            )
            GrowthStage.FRUITING -> GrowthStageVisual(
                iconName = "ionicons_sparkles",
                color = Color(0xFFFF8C00),
                description = "Abundant harvest"
            )
            GrowthStage.ABUNDANT -> GrowthStageVisual(
                iconName = "ionicons_star_outline",
                color = Color(0xFFFF6347),
                description = "Thriving ecosystem"
            )
            GrowthStage.TRANSCENDENT -> GrowthStageVisual(
                iconName = "ionicons_star",
                color = Color(0xFFDC143C),
                description = "Ancient forest wisdom"
            )
        }
    }

    private fun getDesertVisual(stage: GrowthStage): GrowthStageVisual {
        return when (stage) {
            GrowthStage.SEED -> GrowthStageVisual(
                iconName = "ionicons_leaf_outline",
                color = Color(0xFFD2B48C),
                description = "Seed in the sand"
            )
            GrowthStage.SPROUT -> GrowthStageVisual(
                iconName = "ionicons_leaf",
                color = Color(0xFFDEB887),
                description = "Cactus emerging"
            )
            GrowthStage.SEEDLING -> GrowthStageVisual(
                iconName = "ionicons_flower_outline",
                color = Color(0xFFF4A460),
                description = "Young cactus"
            )
            GrowthStage.YOUNG_PLANT -> GrowthStageVisual(
                iconName = "ionicons_flower",
                color = Color(0xFFCD853F),
                description = "Growing saguaro"
            )
            GrowthStage.MATURE_PLANT -> GrowthStageVisual(
                iconName = "ionicons_rose_outline",
                color = Color(0xFF8B4513),
                description = "Mature cactus"
            )
            GrowthStage.BUDDING -> GrowthStageVisual(
                iconName = "ionicons_rose",
                color = Color(0xFFFF69B4),
                description = "Cactus flower buds"
            )
            GrowthStage.FLOWERING -> GrowthStageVisual(
                iconName = "ionicons_sparkles_outline",
                color = Color(0xFFFF1493),
                description = "Desert bloom"
            )
            GrowthStage.FRUITING -> GrowthStageVisual(
                iconName = "ionicons_sparkles",
                color = Color(0xFFC71585),
                description = "Prickly pear fruit"
            )
            GrowthStage.ABUNDANT -> GrowthStageVisual(
                iconName = "ionicons_star_outline",
                color = Color(0xFF8B008B),
                description = "Oasis of life"
            )
            GrowthStage.TRANSCENDENT -> GrowthStageVisual(
                iconName = "ionicons_star",
                color = Color(0xFF4B0082),
                description = "Desert resilience mastered"
            )
        }
    }

    private fun getOceanVisual(stage: GrowthStage): GrowthStageVisual {
        return when (stage) {
            GrowthStage.SEED -> GrowthStageVisual(
                iconName = "ionicons_leaf_outline",
                color = Color(0xFFE0F7FA),
                description = "Coral polyp"
            )
            GrowthStage.SPROUT -> GrowthStageVisual(
                iconName = "ionicons_leaf",
                color = Color(0xFFB2EBF2),
                description = "Young coral"
            )
            GrowthStage.SEEDLING -> GrowthStageVisual(
                iconName = "ionicons_flower_outline",
                color = Color(0xFF80DEEA),
                description = "Growing reef"
            )
            GrowthStage.YOUNG_PLANT -> GrowthStageVisual(
                iconName = "ionicons_flower",
                color = Color(0xFF4DD0E1),
                description = "Kelp forest starting"
            )
            GrowthStage.MATURE_PLANT -> GrowthStageVisual(
                iconName = "ionicons_rose_outline",
                color = Color(0xFF26C6DA),
                description = "Thriving kelp forest"
            )
            GrowthStage.BUDDING -> GrowthStageVisual(
                iconName = "ionicons_rose",
                color = Color(0xFF00BCD4),
                description = "Sea anemones blooming"
            )
            GrowthStage.FLOWERING -> GrowthStageVisual(
                iconName = "ionicons_sparkles_outline",
                color = Color(0xFF00ACC1),
                description = "Bioluminescent display"
            )
            GrowthStage.FRUITING -> GrowthStageVisual(
                iconName = "ionicons_sparkles",
                color = Color(0xFF0097A7),
                description = "Abundant marine life"
            )
            GrowthStage.ABUNDANT -> GrowthStageVisual(
                iconName = "ionicons_star_outline",
                color = Color(0xFF00838F),
                description = "Vibrant coral reef"
            )
            GrowthStage.TRANSCENDENT -> GrowthStageVisual(
                iconName = "ionicons_star",
                color = Color(0xFF006064),
                description = "Ocean harmony achieved"
            )
        }
    }
}
