package com.nami.peace.widget

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.LocalContext
import com.nami.peace.MainActivity
import com.nami.peace.R
import com.nami.peace.data.local.GardenTheme
import com.nami.peace.domain.model.GardenState
import com.nami.peace.ui.theme.getGardenThemeIcons
import com.nami.peace.ui.theme.getGrowthStageIcon

/**
 * Main content composable for the Peace Garden widget.
 * Displays garden state, current streak, and growth stage.
 * Implements Requirements 17.4, 17.5, 17.9
 */
@Composable
fun GardenWidgetContent() {
    val context = LocalContext.current
    val repository = WidgetDataProvider.getGardenRepository(context)
    val gardenState by repository.getGardenState().collectAsState(initial = null)
    
    val intent = Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_VIEW
        putExtra("navigate_to", "garden")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .padding(16.dp)
            .cornerRadius(16.dp)
            .clickable(actionStartActivity(intent))
    ) {
        if (gardenState == null) {
            LoadingStateContent()
        } else {
            GardenStateContent(gardenState!!)
        }
    }
}

/**
 * Displays the garden state with theme, growth stage, and streak information.
 */
@Composable
private fun GardenStateContent(gardenState: GardenState) {
    val themeIcons = getGardenThemeIcons(gardenState.theme)
    val growthStageIcon = getGrowthStageIcon(gardenState.theme, gardenState.growthStage)
    
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with theme name
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(getThemeIconResource(gardenState.theme)),
                contentDescription = "Garden Theme",
                modifier = GlanceModifier.size(24.dp)
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = getThemeName(gardenState.theme),
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                )
            )
        }
        
        Spacer(modifier = GlanceModifier.height(16.dp))
        
        // Growth stage visualization
        Image(
            provider = ImageProvider(getIconResource(growthStageIcon)),
            contentDescription = "Growth Stage ${gardenState.growthStage}",
            modifier = GlanceModifier.size(80.dp)
        )
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        // Growth stage text
        Text(
            text = "Stage ${gardenState.growthStage + 1}/10",
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = GlanceTheme.colors.onSurface
            )
        )
        
        Spacer(modifier = GlanceModifier.height(16.dp))
        
        // Streak information
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(GlanceTheme.colors.surfaceVariant)
                .cornerRadius(8.dp)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(getIconResource(themeIcons.streakIcon)),
                contentDescription = "Streak",
                modifier = GlanceModifier.size(24.dp)
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Column(
                modifier = GlanceModifier.defaultWeight()
            ) {
                Text(
                    text = "Current Streak",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = GlanceTheme.colors.onSurfaceVariant
                    )
                )
                Text(
                    text = "${gardenState.currentStreak} days",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onSurface
                    )
                )
            }
        }
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        // Stats row
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Longest streak
            Column(
                modifier = GlanceModifier
                    .defaultWeight()
                    .background(GlanceTheme.colors.surfaceVariant)
                    .cornerRadius(8.dp)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Best",
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = GlanceTheme.colors.onSurfaceVariant
                    )
                )
                Text(
                    text = "${gardenState.longestStreak}",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onSurface
                    )
                )
            }
            
            Spacer(modifier = GlanceModifier.width(8.dp))
            
            // Total tasks
            Column(
                modifier = GlanceModifier
                    .defaultWeight()
                    .background(GlanceTheme.colors.surfaceVariant)
                    .cornerRadius(8.dp)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tasks",
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = GlanceTheme.colors.onSurfaceVariant
                    )
                )
                Text(
                    text = "${gardenState.totalTasksCompleted}",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onSurface
                    )
                )
            }
            
            Spacer(modifier = GlanceModifier.width(8.dp))
            
            // Next milestone
            Column(
                modifier = GlanceModifier
                    .defaultWeight()
                    .background(GlanceTheme.colors.surfaceVariant)
                    .cornerRadius(8.dp)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Next",
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = GlanceTheme.colors.onSurfaceVariant
                    )
                )
                Text(
                    text = "${getNextMilestone(gardenState)}",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onSurface
                    )
                )
            }
        }
    }
}

/**
 * Displays loading state while garden data is being fetched.
 */
@Composable
private fun LoadingStateContent() {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_ionicons_leaf),
            contentDescription = "Loading",
            modifier = GlanceModifier.size(48.dp)
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(
            text = "Peace Garden",
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.onSurface
            )
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = "Loading...",
            style = TextStyle(
                fontSize = 12.sp,
                color = GlanceTheme.colors.onSurfaceVariant
            )
        )
    }
}

/**
 * Gets the theme name for display.
 */
private fun getThemeName(theme: GardenTheme): String {
    return when (theme) {
        GardenTheme.ZEN -> "Zen Garden"
        GardenTheme.FOREST -> "Forest Garden"
        GardenTheme.DESERT -> "Desert Garden"
        GardenTheme.OCEAN -> "Ocean Garden"
    }
}

/**
 * Gets the drawable resource ID for a theme icon.
 */
private fun getThemeIconResource(theme: GardenTheme): Int {
    return when (theme) {
        GardenTheme.ZEN -> R.drawable.ic_ionicons_leaf
        GardenTheme.FOREST -> R.drawable.ic_ionicons_leaf
        GardenTheme.DESERT -> R.drawable.ic_ionicons_sunny
        GardenTheme.OCEAN -> R.drawable.ic_ionicons_water
    }
}

/**
 * Gets the drawable resource ID for an icon name.
 */
private fun getIconResource(iconName: String): Int {
    return when (iconName) {
        "leaf" -> R.drawable.ic_ionicons_leaf
        "ellipse" -> R.drawable.ic_ionicons_ellipse
        "radio_button_on" -> R.drawable.ic_ionicons_radio_button_on
        "leaf_outline" -> R.drawable.ic_ionicons_leaf_outline
        "flower_outline" -> R.drawable.ic_ionicons_flower_outline
        "flower" -> R.drawable.ic_ionicons_flower
        "rose_outline" -> R.drawable.ic_ionicons_rose_outline
        "rose" -> R.drawable.ic_ionicons_rose
        "sparkles_outline" -> R.drawable.ic_ionicons_sparkles_outline
        "sparkles" -> R.drawable.ic_ionicons_sparkles
        "water_outline" -> R.drawable.ic_ionicons_water_outline
        "git_branch_outline" -> R.drawable.ic_ionicons_git_branch_outline
        "git_branch" -> R.drawable.ic_ionicons_git_branch
        "git_network_outline" -> R.drawable.ic_ionicons_git_network_outline
        "git_network" -> R.drawable.ic_ionicons_git_network
        "bonfire_outline" -> R.drawable.ic_ionicons_bonfire_outline
        "bonfire" -> R.drawable.ic_ionicons_bonfire
        "sunny" -> R.drawable.ic_ionicons_sunny
        "water" -> R.drawable.ic_ionicons_water
        "triangle_outline" -> R.drawable.ic_ionicons_triangle_outline
        "triangle" -> R.drawable.ic_ionicons_triangle
        "caret_up_outline" -> R.drawable.ic_ionicons_caret_up_outline
        "caret_up" -> R.drawable.ic_ionicons_caret_up
        "star_outline" -> R.drawable.ic_ionicons_star_outline
        "star" -> R.drawable.ic_ionicons_star
        "sunny_outline" -> R.drawable.ic_ionicons_sunny_outline
        "fish_outline" -> R.drawable.ic_ionicons_fish_outline
        "fish" -> R.drawable.ic_ionicons_fish
        "boat_outline" -> R.drawable.ic_ionicons_boat_outline
        "boat" -> R.drawable.ic_ionicons_boat
        "navigate_outline" -> R.drawable.ic_ionicons_navigate_outline
        "navigate" -> R.drawable.ic_ionicons_navigate
        "planet_outline" -> R.drawable.ic_ionicons_planet_outline
        "planet" -> R.drawable.ic_ionicons_planet
        "flame" -> R.drawable.ic_ionicons_flame
        "trophy" -> R.drawable.ic_ionicons_trophy
        else -> R.drawable.ic_ionicons_leaf // Fallback
    }
}

/**
 * Gets the next milestone for the current streak.
 */
private fun getNextMilestone(gardenState: GardenState): Int {
    val milestones = gardenState.milestones
    return milestones.firstOrNull { it > gardenState.currentStreak } ?: milestones.last()
}
