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
import androidx.compose.ui.graphics.luminance
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
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    isVisible: Boolean = true,
    blurEnabled: Boolean = true,
    blurStrength: Float = 12f,
    blurTintAlpha: Float = 0.5f
) {
    val shape = androidx.compose.foundation.shape.CircleShape
    // Use luminance to detect if the *App Theme* is dark, not the System Theme
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
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
                    indication = androidx.compose.material3.ripple(color = contentColor),
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            
            // Background Logic:
            val activeContainerColor = if (blurEnabled) {
                containerColor.copy(alpha = 0.45f) // Glassy tint
            } else {
                containerColor // Solid opaque
            }

            // Layer 1: Haze (Only if enabled)
            if (blurEnabled && hazeState != null) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .hazeChild(
                            state = hazeState,
                            shape = shape,
                            style = HazeStyle(
                                blurRadius = blurStrength.dp,
                                tint = Color.Transparent
                            )
                        )
                )
            } else if (!blurEnabled) {
                // If blur disabled, activeContainerColor handles the solid part.
            }
    
            // Layer 2: Tint/Background Overlay and Border
             val borderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.1f)
             Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(activeContainerColor)
                    .then(
                         // Only show borders in Glassy mode to define edges
                        if (blurEnabled) {
                            Modifier.border(
                                width = 1.dp, 
                                color = borderColor,
                                shape = shape
                            )
                        } else Modifier
                    )
            )
    
            // Layer 3: Content Icon
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (blurEnabled) contentColor.copy(alpha = 0.9f) else contentColor
            )
        }
    }
}

@Composable
fun GlassyBottomSheet(
    onDismissRequest: () -> Unit,
    show: Boolean,
    hazeState: HazeState? = null,
    blurEnabled: Boolean = true,
    blurStrength: Int = 15,
    blurTintAlpha: Float = 0.5f,
    shadowsEnabled: Boolean = true,
    shadowStyle: Int = 1,
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
                GlassySheetSurface(
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

@Composable
fun GlassySheetSurface(
    hazeState: HazeState?,
    blurEnabled: Boolean = true,
    blurStrength: Int = 15,
    blurTintAlpha: Float = 0.5f,
    shadowsEnabled: Boolean = true,
    shadowStyle: Int = 1,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val borderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)
    
    // Background Color Logic
    // If Blur is ENABLED: Transparent (Haze fills it)
    // If Blur is DISABLED: Solid Surface Color (User Request: "Solid but semi-transparent? or Full Solid?")
    // Decision: Full Solid Surface Container for better readability when blur is off.
    val containerColor = if (blurEnabled) {
        Color.Transparent 
    } else {
        // Fallback to a solid surface color. 
        // SurfaceContainer is a good default for sheets in M3.
        MaterialTheme.colorScheme.surfaceContainer
    }

    // Shadow Logic based on Settings
    val shadowModifier = if (shadowsEnabled) {
        when (shadowStyle) {
            0 -> Modifier // None
            1 -> Modifier.shadow(8.dp, shape, spotColor = Color.Black.copy(alpha = 0.1f)) // Soft
            2 -> Modifier.shadow(16.dp, shape, spotColor = Color.Black.copy(alpha = 0.2f)) // Medium
            3 -> Modifier.shadow(24.dp, shape, spotColor = Color.Black.copy(alpha = 0.3f)) // Hard
            else -> Modifier
        }
    } else {
        Modifier
    }

    val borderModifier = if (shadowsEnabled && shadowStyle > 0) {
         Modifier.border(1.dp, borderColor, shape)
    } else if (!shadowsEnabled) {
        // If shadows disabled, user might still want a border, but strictly speaking "Glassy" implies border + blur.
        // We keep the border for definition unless specifically removed, but let's stick to the main pattern.
        Modifier.border(1.dp, borderColor, shape)
    } else {
        Modifier
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(shadowModifier)
            .clip(shape)
            .then(borderModifier)
            .background(containerColor)
    ) {
        if (blurEnabled && hazeState != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .hazeChild(
                        state = hazeState,
                        shape = shape,
                        style = HazeStyle(
                             tint = if (isDark) 
                                 com.nami.peace.ui.theme.GlassyBlack.copy(alpha = if (blurEnabled) 0.2f else 0.8f) 
                             else 
                                 com.nami.peace.ui.theme.GlassyWhite.copy(alpha = if (blurEnabled) 0.2f else 0.8f),
                             blurRadius = if (blurEnabled) blurStrength.dp else 0.dp
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



@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

fun formatTime(context: android.content.Context, timeInMillis: Long): String {
    return android.text.format.DateFormat.getTimeFormat(context).format(java.util.Date(timeInMillis))
}

@Composable
fun DetailCard(label: String, value: String) {
    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

fun getPriorityColor(priority: com.nami.peace.domain.model.PriorityLevel): Color {
    return when (priority) {
        com.nami.peace.domain.model.PriorityLevel.HIGH -> androidx.compose.ui.graphics.Color(0xFFE57373) // Soft Red
        com.nami.peace.domain.model.PriorityLevel.MEDIUM -> androidx.compose.ui.graphics.Color(0xFF64B5F6) // Soft Blue
        com.nami.peace.domain.model.PriorityLevel.LOW -> androidx.compose.ui.graphics.Color(0xFF81C784) // Soft Green
    }
}
@Composable
fun GlassySection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                content()
            }
        }
    }
}
