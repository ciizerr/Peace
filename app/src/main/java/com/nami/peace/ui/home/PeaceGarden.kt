package com.nami.peace.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.FilterVintage
import androidx.compose.material.icons.rounded.Grass
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import com.nami.peace.ui.components.PeaceCard

@Composable
fun PeaceGarden(
    progress: Float, // 0.0 to 1.0
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "GardenProgress"
    )

    PeaceCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Peace Garden",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Daily Bloom",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Box(contentAlignment = Alignment.Center) {
                CircularProgress(
                    progress = animatedProgress,
                    size = 120.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
                
                // Icon changes based on progress
                val icon = when {
                    progress >= 1.0f -> Icons.Rounded.LocalFlorist // Full bloom
                    progress >= 0.66f -> Icons.Rounded.FilterVintage // Flower bud
                    progress >= 0.33f -> Icons.Rounded.Eco // Sprout
                    else -> Icons.Rounded.Grass // Seed/Grass
                }
                
                Icon(
                    imageVector = icon,
                    contentDescription = "Garden Status",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun CircularProgress(
    progress: Float,
    size: Dp,
    color: Color,
    trackColor: Color,
    strokeWidth: Dp = 8.dp
) {
    Canvas(modifier = Modifier.size(size)) {
        // Draw track
        drawCircle(
            color = trackColor,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
        
        // Draw progress
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360 * progress,
            useCenter = false,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
    }
}
