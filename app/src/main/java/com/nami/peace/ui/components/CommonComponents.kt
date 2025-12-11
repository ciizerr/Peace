package com.nami.peace.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nami.peace.ui.theme.AccentBlue
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeChild
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.fillMaxWidth

@Composable
fun PlaceholderScreen(
    title: String = "404 — Not Found Yet",
    subtitle: String = "This screen isn’t implemented yet. Check back soon!",
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "🚧",
                fontSize = 64.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onBackClick,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Back to Previous Screen", color = Color.White)
            }
        }
    }
}

@Composable
fun GlassyFloatingActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    isVisible: Boolean = true
) {
    val shape = androidx.compose.foundation.shape.CircleShape
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    
    androidx.compose.animation.AnimatedVisibility(
        visible = isVisible,
        enter = androidx.compose.animation.scaleIn() + androidx.compose.animation.fadeIn(),
        exit = androidx.compose.animation.scaleOut() + androidx.compose.animation.fadeOut()
    ) {
        // Root Container
        Box(
            modifier = modifier
                .size(56.dp)
                .shadow(
                    elevation = 8.dp, 
                    shape = shape,
                    spotColor = if (isDark) Color.Black.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.2f),
                    ambientColor = if (isDark) Color.Black.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.2f)
                )
                .clip(shape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = androidx.compose.material3.ripple(color = MaterialTheme.colorScheme.onPrimaryContainer),
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            // Layer 1: Pure Blur (Behind Tint)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .then(
                        if (hazeState != null) {
                            Modifier.hazeChild(
                                state = hazeState,
                                shape = shape,
                                style = HazeStyle(
                                    blurRadius = 12.dp,
                                    tint = Color.Transparent // Clear blur, tint handled by overlay
                                )
                            )
                        } else {
                            Modifier
                        }
                    )
                    .background(
                        if (hazeState == null) MaterialTheme.colorScheme.surface.copy(alpha = 0.5f) else Color.Transparent
                    )
            )
    
            // Layer 2: Tint Overlay (Consistent Color)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(containerColor.copy(alpha = 0.45f))
            )
    
            // Layer 3: Border Ring (Hides Edge Artifacts)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(
                        width = 1.dp, 
                        color = Color.White.copy(alpha = 0.3f), // Subtle highlight
                        shape = shape
                    )
            )
    
            // Layer 4: Content Icon
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun GlassyBottomSheet(
    onDismissRequest: () -> Unit,
    show: Boolean,
    hazeState: HazeState? = null,
    content: @Composable () -> Unit
) {
    // Back Handler
    if (show) {
        androidx.activity.compose.BackHandler(onBack = onDismissRequest)
    }

    // Container Layer
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(100f) // Ensure it sits on top
    ) {
        // Scrim
        androidx.compose.animation.AnimatedVisibility(
            visible = show,
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.32f))
                    .clickable(
                        interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = onDismissRequest
                    )
            )
        }

        // Sheet Content
        androidx.compose.animation.AnimatedVisibility(
            visible = show,
            enter = androidx.compose.animation.slideInVertically { it } + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.slideOutVertically { it } + androidx.compose.animation.fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .clickable(
                        interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Capture clicks to prevent dismissing
                    )
            ) {
                GlassySheetSurface(hazeState = hazeState, content = content)
            }
        }
    }
}

@Composable
fun GlassySheetSurface(
    hazeState: HazeState?,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val borderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, borderColor, shape)
            .background(Color.Transparent) // Remove duplicate background layer
    ) {
        if (hazeState != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .hazeChild(
                        state = hazeState,
                        shape = shape,
                        style = HazeStyle(
                             tint = if (isDark) 
                                 com.nami.peace.ui.theme.GlassyBlack.copy(alpha = 0.2f) // Reduced alpha
                             else 
                                 com.nami.peace.ui.theme.GlassyWhite.copy(alpha = 0.2f), // Reduced alpha
                             blurRadius = 20.dp
                        )
                    )
                     .background(
                        if (isDark) com.nami.peace.ui.theme.GlassyBlack.copy(alpha = 0.1f) 
                        else com.nami.peace.ui.theme.GlassyWhite.copy(alpha = 0.1f)
                    )
            )
        }
        content()
    }
}
