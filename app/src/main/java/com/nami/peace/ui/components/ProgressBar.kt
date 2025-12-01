package com.nami.peace.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A progress bar component that displays completion percentage for subtasks.
 * 
 * Features:
 * - Animated progress updates
 * - Customizable colors
 * - Optional percentage text display
 * - Smooth transitions
 * 
 * Requirements: 4.3, 4.5, 4.6
 * 
 * @param progress The progress percentage (0-100)
 * @param modifier Modifier for the progress bar container
 * @param showPercentage Whether to display the percentage text
 * @param backgroundColor The background color of the progress bar track
 * @param progressColor The color of the progress indicator
 * @param height The height of the progress bar
 * @param animationDuration Duration of the progress animation in milliseconds
 */
@Composable
fun ProgressBar(
    progress: Int,
    modifier: Modifier = Modifier,
    showPercentage: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    height: Int = 8,
    animationDuration: Int = 300
) {
    // Clamp progress to 0-100 range
    val clampedProgress = progress.coerceIn(0, 100)
    
    // Animate progress changes
    val animatedProgress by animateFloatAsState(
        targetValue = clampedProgress / 100f,
        animationSpec = tween(durationMillis = animationDuration),
        label = "progress_animation"
    )
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
                .clip(RoundedCornerShape(height.dp / 2))
                .background(backgroundColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(RoundedCornerShape(height.dp / 2))
                    .background(progressColor)
            )
        }
        
        // Optional percentage text
        if (showPercentage) {
            Text(
                text = "$clampedProgress%",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

/**
 * A progress bar with completion counts displayed.
 * 
 * Shows "X of Y completed" text along with the progress bar.
 * 
 * @param completedCount Number of completed subtasks
 * @param totalCount Total number of subtasks
 * @param modifier Modifier for the component
 * @param backgroundColor The background color of the progress bar track
 * @param progressColor The color of the progress indicator
 */
@Composable
fun ProgressBarWithCounts(
    completedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColor: Color = MaterialTheme.colorScheme.primary
) {
    val progress = if (totalCount > 0) {
        (completedCount * 100) / totalCount
    } else {
        0
    }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Completion text
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Progress",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "$completedCount of $totalCount completed",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Progress bar
        ProgressBar(
            progress = progress,
            showPercentage = false,
            backgroundColor = backgroundColor,
            progressColor = progressColor
        )
    }
}

/**
 * A compact progress indicator for list items.
 * 
 * Displays a small progress bar without text, suitable for compact layouts.
 * 
 * @param progress The progress percentage (0-100)
 * @param modifier Modifier for the component
 * @param backgroundColor The background color of the progress bar track
 * @param progressColor The color of the progress indicator
 */
@Composable
fun CompactProgressBar(
    progress: Int,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColor: Color = MaterialTheme.colorScheme.primary
) {
    ProgressBar(
        progress = progress,
        modifier = modifier,
        showPercentage = false,
        backgroundColor = backgroundColor,
        progressColor = progressColor,
        height = 4,
        animationDuration = 200
    )
}
