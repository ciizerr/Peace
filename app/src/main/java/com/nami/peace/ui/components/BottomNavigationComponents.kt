package com.nami.peace.ui.components

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.zIndex
import com.nami.peace.ui.theme.AccentBlue
import com.nami.peace.ui.theme.GlassyBlack
import com.nami.peace.ui.theme.GlassyWhite
import com.nami.peace.ui.theme.SoftShadow
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import com.nami.peace.R
import dev.chrisbanes.haze.hazeChild

enum class MainTab(@StringRes val titleRes: Int, val icon: ImageVector, val route: String) {
    Dashboard(R.string.tab_dashboard, Icons.Filled.Dashboard, "dashboard"),
    Alarms(R.string.tab_alarms, Icons.Filled.Notifications, "alarms"),
    Tasks(R.string.tab_tasks, Icons.Filled.DoneAll, "tasks"),
    Settings(R.string.tab_settings, Icons.Filled.Settings, "settings")
}

enum class SettingsCategory(@StringRes val titleRes: Int, val route: String) {
    Overview(R.string.settings_overview, "settings_overview"),
    Appearance(R.string.settings_appearance, "settings_appearance"),
    NavStyle(R.string.settings_nav_style, "settings_nav_style"),
    ShadowBlur(R.string.settings_shadow_blur, "settings_shadow_blur"),
    About(R.string.settings_about, "settings_about")
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
                    
                    val tabWeight by animateFloatAsState(
                        targetValue = if (isSelected) 2f else 1f,
                        label = "weight",
                        animationSpec = tween(300)
                    )
                    
                    Box(
                        modifier = Modifier
                            .weight(tabWeight)
                            .height(72.dp)
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(24.dp))
                                .background(if (isSelected) AccentBlue.copy(alpha = 0.1f) else Color.Transparent)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    onClick = { onTabSelected(tab) }
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = stringResource(tab.titleRes),
                                tint = if (isSelected) AccentBlue else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(24.dp)
                            )
                            
                            AnimatedVisibility(
                                visible = isSelected,
                                enter = androidx.compose.animation.fadeIn(tween(150, delayMillis = 150)) + androidx.compose.animation.expandHorizontally(tween(300)),
                                exit = androidx.compose.animation.fadeOut(tween(150)) + androidx.compose.animation.shrinkHorizontally(tween(300))
                            ) {
                                Row {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(tab.titleRes),
                                        color = AccentBlue,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelLarge,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
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
                                    tint = if (isDark) GlassyBlack.copy(alpha = blurTintAlpha) else GlassyWhite.copy(alpha = blurTintAlpha)
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
                            text = stringResource(category.titleRes),
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
