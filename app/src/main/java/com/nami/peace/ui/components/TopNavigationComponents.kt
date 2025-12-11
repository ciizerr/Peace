package com.nami.peace.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nami.peace.ui.theme.GlassyBlack
import com.nami.peace.ui.theme.GlassyWhite
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import androidx.compose.foundation.border
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.zIndex
import dev.chrisbanes.haze.hazeChild
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassyTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    hazeState: HazeState? = null,
    blurEnabled: Boolean = true,
    blurStrength: Float = 12f,
    blurTintAlpha: Float = 0.5f,
    shadowsEnabled: Boolean = true,
    shadowStyle: String = "Medium"
) {
    val isDark = isSystemInDarkTheme()
    val shape = RoundedCornerShape(24.dp)
    
        // Shadow Logic
    val (baseElevation, baseAlpha) = when (shadowStyle) {
        "None" -> 0.dp to 0f
        "Subtle" -> 4.dp to 0.1f
        "Medium" -> 8.dp to 0.25f
        "Heavy" -> 16.dp to 0.6f
        else -> 8.dp to 0.25f
    }
    
    // If blur is enabled, we DISABLE the system shadow to prevent artifacts.
    // Instead, we use a Border to define the edges (Glassmorphism).
    val effectiveElevation = if (shadowsEnabled && shadowStyle != "None" && !blurEnabled) baseElevation else 0.dp
    
    val shadowColor = com.nami.peace.ui.theme.SoftShadow.copy(alpha = baseAlpha)
    
    // Border Logic for Glass Mode
    val showBorder = blurEnabled && shadowsEnabled && shadowStyle != "None"
    val borderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)
    val borderModifier = if (showBorder) Modifier.border(1.dp, borderColor, shape) else Modifier

    Box(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(top = 8.dp, start = 16.dp, end = 16.dp)
            .shadow(elevation = effectiveElevation, shape = shape, spotColor = shadowColor, ambientColor = shadowColor)
            .then(borderModifier)
            .fillMaxWidth()
    ) {
        // Layer 1: Background and Blur
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
                            MaterialTheme.colorScheme.surfaceContainer
                        )
                    }
                )
        )

        // Layer 2: Content (TopAppBar)
        TopAppBar(
            title = title,
            navigationIcon = navigationIcon,
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .zIndex(1f) // Ensure text is above blur
        )
    }
}
