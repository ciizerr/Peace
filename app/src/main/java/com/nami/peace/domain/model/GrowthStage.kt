package com.nami.peace.domain.model

/**
 * Represents the 10 growth stages in the Peace Garden.
 * Each stage requires a certain number of completed tasks to unlock.
 */
enum class GrowthStage(
    val stage: Int,
    val displayName: String,
    val tasksRequired: Int,
    val description: String
) {
    SEED(0, "Seed", 0, "The beginning of your journey"),
    SPROUT(1, "Sprout", 5, "First signs of growth"),
    SEEDLING(2, "Seedling", 15, "Taking root"),
    YOUNG_PLANT(3, "Young Plant", 30, "Growing stronger"),
    MATURE_PLANT(4, "Mature Plant", 50, "Established and thriving"),
    BUDDING(5, "Budding", 75, "Preparing to bloom"),
    FLOWERING(6, "Flowering", 100, "In full bloom"),
    FRUITING(7, "Fruiting", 150, "Bearing fruit"),
    ABUNDANT(8, "Abundant", 250, "Overflowing with life"),
    TRANSCENDENT(9, "Transcendent", 500, "Mastery achieved");

    companion object {
        /**
         * Get the growth stage for a given number of completed tasks
         */
        fun fromTaskCount(taskCount: Int): GrowthStage {
            return values()
                .lastOrNull { it.tasksRequired <= taskCount }
                ?: SEED
        }

        /**
         * Get the next growth stage, or null if at max stage
         */
        fun GrowthStage.next(): GrowthStage? {
            val nextStage = stage + 1
            return values().getOrNull(nextStage)
        }

        /**
         * Get the previous growth stage, or null if at first stage
         */
        fun GrowthStage.previous(): GrowthStage? {
            val prevStage = stage - 1
            return values().getOrNull(prevStage)
        }

        /**
         * Calculate progress to next stage (0-100)
         */
        fun calculateProgressToNextStage(currentTaskCount: Int): Int {
            val currentStage = fromTaskCount(currentTaskCount)
            val nextStage = currentStage.next() ?: return 100
            
            val tasksInCurrentStage = currentTaskCount - currentStage.tasksRequired
            val tasksNeededForNextStage = nextStage.tasksRequired - currentStage.tasksRequired
            
            return if (tasksNeededForNextStage > 0) {
                ((tasksInCurrentStage.toFloat() / tasksNeededForNextStage) * 100).toInt().coerceIn(0, 100)
            } else {
                100
            }
        }
    }
}
