package com.nami.peace.ui.components

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.zIndex
import com.nami.peace.ui.theme.AccentBlue
import com.nami.peace.ui.theme.GlassyBlack
import com.nami.peace.ui.theme.GlassyWhite
import com.nami.peace.ui.theme.SoftShadow
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeChild

enum class MainTab(val title: String, val icon: ImageVector, val route: String) {
    Dashboard("Dashboard", Icons.Filled.Dashboard, "dashboard"),
    Alarms("Alarms", Icons.Filled.Notifications, "alarms"),
    Tasks("Tasks", Icons.Filled.DoneAll, "tasks"),
    Settings("Settings", Icons.Filled.Settings, "settings")
}

enum class SettingsCategory(val title: String, val route: String) {
    Overview("Overview", "settings_overview"),
    Appearance("Appearance", "settings_appearance"),
    NavStyle("Navigation Style", "settings_nav_style"),
    ShadowBlur("Shadows & Blur", "settings_shadow_blur"),
    About("About", "settings_about")
}

@Composable
fun FloatingBottomBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    isVisible: Boolean,
    hazeState: HazeState? = null,
    blurEnabled: Boolean = true,
    blurStrength: Float = 12f,
    blurTintAlpha: Float = 0.5f,
    modifier: Modifier = Modifier,
    shadowsEnabled: Boolean = true
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(200)),
        exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(200))
    ) {
        val shape = RoundedCornerShape(24.dp)
        val shadowElevation = if (shadowsEnabled) 8.dp else 0.dp
        val isDark = isSystemInDarkTheme()
        
        // Z-Index Fix: Root Box contains content
        Box(
            modifier = Modifier
                .padding(bottom = 24.dp, start = 20.dp, end = 20.dp)
                .shadow(elevation = shadowElevation, shape = shape, spotColor = SoftShadow)
                .height(72.dp)
                .fillMaxWidth()
                .then(modifier)
        ) {
            // Background Layer: Applies Haze OR Fallback Color
            // This is the first child, so it renders BEHIND the Row (zIndex 0 by default)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(shape)
                    .then(
                        if (blurEnabled && hazeState != null) {
                            Modifier.hazeChild(
                                state = hazeState,
                                shape = shape,
                                style = HazeStyle(
                                    blurRadius = blurStrength.dp,
                                    tint = if (isDark) Color.Black.copy(alpha = blurTintAlpha) else Color.White.copy(alpha = blurTintAlpha)
                                )
                            )
                        } else {
                            Modifier.background(
                                (if (isDark) GlassyBlack else GlassyWhite).copy(alpha = 0.9f)
                            )
                        }
                    )
            )
            
            // Foreground Content Layer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(1f), // Ensure content is above background
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MainTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    val interactionSource = remember { MutableInteractionSource() }
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = { onTabSelected(tab) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(AccentBlue.copy(alpha = 0.1f))
                            )
                        }
                        
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = if (isSelected) AccentBlue else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryCarouselBar(
    selectedCategory: SettingsCategory,
    onCategorySelected: (SettingsCategory) -> Unit,
    isVisible: Boolean,
    hazeState: HazeState? = null,
    blurEnabled: Boolean = true,
    blurStrength: Float = 12f,
    blurTintAlpha: Float = 0.5f,
    modifier: Modifier = Modifier,
    shadowsEnabled: Boolean = true
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(200)),
        exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(200)),
        modifier = modifier
    ) {
        val shape = RoundedCornerShape(24.dp)
        val shadowElevation = if (shadowsEnabled) 8.dp else 0.dp
        val isDark = isSystemInDarkTheme()
        
        Box(
            modifier = Modifier
                .padding(bottom = 24.dp, start = 20.dp, end = 20.dp)
                .shadow(elevation = shadowElevation, shape = shape, spotColor = SoftShadow)
                .height(72.dp)
                .fillMaxWidth()
        ) {
            // Background Layer: Applies Haze OR Fallback Color
            // This is the first child, so it renders BEHIND the Row (zIndex 0 by default)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(shape)
                    .then(
                        if (blurEnabled && hazeState != null) {
                            Modifier.hazeChild(
                                state = hazeState,
                                shape = shape,
                                style = HazeStyle(
                                    blurRadius = blurStrength.dp,
                                    tint = if (isDark) Color.Black.copy(alpha = blurTintAlpha) else Color.White.copy(alpha = blurTintAlpha)
                                )
                            )
                        } else {
                            Modifier.background(
                                (if (isDark) GlassyBlack else GlassyWhite).copy(alpha = 0.9f)
                            )
                        }
                    )
            )
            
            // Foreground Content Layer (Sharp text - NO blur applied)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(1f), // Ensure content is above background
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(SettingsCategory.values()) { category ->
                    val isSelected = selectedCategory == category
                    
                    Box(
                        modifier = Modifier
                            .height(72.dp)
                            .clickable { onCategorySelected(category) }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) AccentBlue else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun isSystemInDarkTheme(): Boolean {
    return androidx.compose.foundation.isSystemInDarkTheme()
}
