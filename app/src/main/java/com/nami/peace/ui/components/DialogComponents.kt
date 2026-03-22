package com.nami.peace.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.zIndex
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nami.peace.ui.theme.SoftShadow
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import com.nami.peace.ui.theme.GlassyBlack
import com.nami.peace.ui.theme.GlassyWhite
import dev.chrisbanes.haze.hazeChild
import com.nami.peace.ui.theme.LocalGlassSettings





@Composable
fun GlassyDialogSurface(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    blurEnabled: Boolean = LocalGlassSettings.current.blurEnabled,
    blurStrength: Int = LocalGlassSettings.current.blurStrength.toInt(),
    blurTintAlpha: Float = LocalGlassSettings.current.blurTintAlpha,
    shadowsEnabled: Boolean = LocalGlassSettings.current.shadowsEnabled,
    shadowStyle: Int = when(LocalGlassSettings.current.shadowStyle) {
        "None" -> 0
        "Subtle" -> 1
        "Medium" -> 2
        "Heavy" -> 3
        else -> 1
    },
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    
    // Border Logic
    val runBorderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)
    
    // Shadow Logic (using shadowStyle like BottomSheet)
    val shadowModifier = if (shadowsEnabled) {
        when (shadowStyle) {
            0 -> Modifier // None
            1 -> Modifier.shadow(8.dp, shape, spotColor = Color.Black.copy(alpha = 0.1f), ambientColor = Color.Black.copy(alpha = 0.1f)) // Soft
            2 -> Modifier.shadow(16.dp, shape, spotColor = Color.Black.copy(alpha = 0.2f), ambientColor = Color.Black.copy(alpha = 0.2f)) // Medium
            3 -> Modifier.shadow(24.dp, shape, spotColor = Color.Black.copy(alpha = 0.3f), ambientColor = Color.Black.copy(alpha = 0.3f)) // Hard
            else -> Modifier.shadow(8.dp, shape, spotColor = Color.Black.copy(alpha = 0.1f), ambientColor = Color.Black.copy(alpha = 0.1f)) // Default
        }
    } else {
        Modifier
    }

    // Border Logic (Conditional)
    val borderModifier = if (shadowsEnabled && shadowStyle > 0) {
        Modifier.border(1.dp, runBorderColor, shape)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(shadowModifier)
            .then(borderModifier)
            .clip(shape)
    ) {
        // Background Layer (Haze or Solid Fallback)
        if (hazeState != null && blurEnabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .hazeChild(
                        state = hazeState,
                        shape = shape,
                        style = HazeStyle(
                            blurRadius = blurStrength.dp,
                            tint = if (isDark) GlassyBlack.copy(alpha = blurTintAlpha) else GlassyWhite.copy(alpha = blurTintAlpha)
                        )
                    )
                    .background(
                        if (isDark) GlassyBlack.copy(alpha = 0.1f) 
                        else GlassyWhite.copy(alpha = 0.1f)
                    )
            )
        } else {
             // Fallback if no haze state provided OR blur disabled
             Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
             )
        }
        
        // Content Layer
        Box(
            modifier = Modifier.padding(24.dp)
        ) {
            content()
        }
    }
}

@Composable
fun GlassyDialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    blurEnabled: Boolean = LocalGlassSettings.current.blurEnabled,
    blurStrength: Int = LocalGlassSettings.current.blurStrength.toInt(),
    blurTintAlpha: Float = LocalGlassSettings.current.blurTintAlpha,
    shadowsEnabled: Boolean = LocalGlassSettings.current.shadowsEnabled,
    shadowStyle: Int = when(LocalGlassSettings.current.shadowStyle) {
        "None" -> 0
        "Subtle" -> 1
        "Medium" -> 2
        "Heavy" -> 3
        else -> 1
    },
    content: @Composable () -> Unit
) {
    // Manage internal state to keep Dialog attached during exit animation
    val isAnimating = remember { androidx.compose.runtime.mutableStateOf(false) }
    val isVisible = remember { androidx.compose.runtime.mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(show) {
        if (show) {
            isVisible.value = true
            isAnimating.value = true
        } else {
            // Delay hide to allow exit animation
             kotlinx.coroutines.delay(300) // Match longest animation duration
             if (!show) { // Double check in case of rapid toggle
                 isVisible.value = false
                 isAnimating.value = false
             }
        }
    }

    if (isVisible.value || show) {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Scrim
                androidx.compose.animation.AnimatedVisibility(
                    visible = show,
                    enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(300)),
                    exit = fadeOut(animationSpec = androidx.compose.animation.core.tween(300)),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.01f)) // Reduced dim intensity
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onDismissRequest
                            )
                    )
                }

                // Scrollable Content Container
                // This wrapper ensures that if the dialog is too tall (e.g. landscape), we can scroll.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars) // Respect system bars
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDismissRequest // Dismiss on clicking outside content
                        )
                        .verticalScroll(androidx.compose.foundation.rememberScrollState())
                        .padding(vertical = 16.dp), // Add vertical padding
                    contentAlignment = Alignment.Center
                ) {
                    // Dialog Surface
                    androidx.compose.animation.AnimatedVisibility(
                        visible = show,
                        enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) + 
                                scaleIn(initialScale = 0.95f, animationSpec = androidx.compose.animation.core.tween(300, easing = androidx.compose.animation.core.FastOutSlowInEasing)),
                        exit = fadeOut(animationSpec = androidx.compose.animation.core.tween(200)) + 
                               scaleOut(targetScale = 0.95f, animationSpec = androidx.compose.animation.core.tween(200)),
                        modifier = Modifier.zIndex(1f) // Ensure above scrim
                    ) {
                        GlassyDialogSurface(
                            modifier = modifier
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {} // Swallow clicks inside dialog
                                ),
                            hazeState = hazeState,
                            blurEnabled = blurEnabled,
                            blurStrength = blurStrength,
                            blurTintAlpha = blurTintAlpha,
                            shadowsEnabled = shadowsEnabled,
                            shadowStyle = shadowStyle,
                            content = content
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GlassyAlertDialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    hazeState: HazeState? = null
) {
    GlassyDialog(
        show = show,
        onDismissRequest = onDismissRequest,
        modifier = modifier.padding(horizontal = 24.dp, vertical = 24.dp).fillMaxWidth(0.9f),
        hazeState = hazeState
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            // Optional Icon Centered at top
            if (icon != null) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
            }

            // Optional Title
            if (title != null) {
                androidx.compose.material3.ProvideTextStyle(
                    MaterialTheme.typography.headlineSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                ) {
                    title()
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
            }

            // Optional Descriptive Text
            if (text != null) {
                androidx.compose.material3.ProvideTextStyle(
                    MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    text()
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))
            }

            // Buttons aligned to end
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (dismissButton != null) {
                    dismissButton()
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                }
                confirmButton()
            }
        }
    }
}
