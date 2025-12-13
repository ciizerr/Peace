package com.nami.peace.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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





@Composable
fun GlassyDialogSurface(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    blurEnabled: Boolean = true,
    blurStrength: Int = 15,
    blurTintAlpha: Float = 0.2f,
    shadowsEnabled: Boolean = true,
    shadowStyle: Int = 1,
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
    blurEnabled: Boolean = true,
    blurStrength: Int = 25,
    blurTintAlpha: Float = 0.4f,
    shadowsEnabled: Boolean = true,
    shadowStyle: Int = 1,
    content: @Composable () -> Unit
) {
    if (show) {
        androidx.activity.compose.BackHandler(onBack = onDismissRequest)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(100f),
        contentAlignment = Alignment.Center
    ) {
        // Scrim
        androidx.compose.animation.AnimatedVisibility(
            visible = show,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismissRequest
                    )
            )
        }

        // Dialog Surface
        androidx.compose.animation.AnimatedVisibility(
            visible = show,
            enter = fadeIn() + scaleIn(initialScale = 0.95f),
            exit = fadeOut() + scaleOut(targetScale = 0.95f)
        ) {
            GlassyDialogSurface(
                modifier = modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
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
