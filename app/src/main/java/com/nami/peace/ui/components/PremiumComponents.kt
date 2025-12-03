package com.nami.peace.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nami.peace.ui.theme.*

/**
 * Premium floating action button with glow effect
 */
@Composable
fun PremiumFAB(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    containerColor: Color = SerenityBlue
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = containerColor,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = modifier
            .size(64.dp)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = containerColor.copy(alpha = 0.3f),
                spotColor = containerColor.copy(alpha = 0.3f)
            )
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(28.dp)
        )
    }
}

/**
 * Premium card with soft shadow
 */
@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevation: Int = 2,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = elevation.dp,
                shape = MaterialTheme.shapes.medium,
                ambientColor = ShadowLight,
                spotColor = if (elevation > 4) ShadowMedium else ShadowLight
            )
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        content = content
    )
}

/**
 * Premium section header with icon
 */
@Composable
fun SectionHeader(
    title: String,
    icon: String? = null,
    action: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Text(
                    icon,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.W600
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        action?.invoke()
    }
}

/**
 * Premium badge with icon and text
 */
@Composable
fun PremiumBadge(
    text: String,
    icon: String? = null,
    color: Color = SerenityBlue,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Text(icon, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.W600
                ),
                color = color
            )
        }
    }
}

/**
 * Premium circular icon button
 */
@Composable
fun CircularIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Int = 44,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    iconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(backgroundColor)
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size((size * 0.5).dp)
        )
    }
}

/**
 * Premium progress indicator with label
 */
@Composable
fun LabeledProgressIndicator(
    progress: Float,
    label: String,
    progressText: String,
    color: Color = SerenityBlue,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                progressText,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.W600
                ),
                color = color
            )
        }
        
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(MaterialTheme.shapes.extraSmall),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}

/**
 * Premium chip with selection state
 */
@Composable
fun PremiumChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: String? = null,
    color: Color = SerenityBlue
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        leadingIcon = if (selected && leadingIcon != null) {
            {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        } else null,
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.2f),
            selectedLabelColor = color
        ),
        shape = MaterialTheme.shapes.extraSmall
    )
}

/**
 * Premium divider with text
 */
@Composable
fun DividerWithText(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

/**
 * Premium info card with icon
 */
@Composable
fun InfoCard(
    title: String,
    description: String,
    icon: String,
    color: Color = SerenityBlue,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = MaterialTheme.shapes.medium,
                ambientColor = ShadowLight,
                spotColor = ShadowLight
            ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    icon,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.W600
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Premium gradient background
 */
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(GradientLightStart, GradientLightEnd),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(colors = colors)
            ),
        content = content
    )
}

/**
 * Premium loading indicator
 */
@Composable
fun PremiumLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = SerenityBlue
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = color,
            strokeWidth = 3.dp,
            modifier = Modifier.size(48.dp)
        )
    }
}

/**
 * Premium empty state
 */
@Composable
fun EmptyState(
    icon: String,
    title: String,
    description: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(GlowBlue),
            contentAlignment = Alignment.Center
        ) {
            Text(
                icon,
                style = MaterialTheme.typography.displayMedium
            )
        }
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.W600
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SerenityBlue
                ),
                shape = MaterialTheme.shapes.small
            ) {
                Text(actionText)
            }
        }
    }
}

/**
 * Premium time picker display
 */
@Composable
fun TimeDisplay(
    time: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            time,
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.W300
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Premium metric card
 */
@Composable
fun MetricCard(
    value: String,
    label: String,
    icon: String,
    color: Color,
    trend: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = MaterialTheme.shapes.medium,
                ambientColor = ShadowLight,
                spotColor = ShadowLight
            ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        icon,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                
                if (trend != null) {
                    PremiumBadge(
                        text = trend,
                        color = AccentSuccess
                    )
                }
            }
            
            Text(
                value,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.W600
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
