package com.nami.peace.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nami.peace.data.local.GardenTheme
import com.nami.peace.domain.model.GrowthStage
import com.nami.peace.ui.theme.GrowthStageVisuals
import kotlinx.coroutines.delay

/**
 * Celebration animation displayed when a growth stage is reached.
 * 
 * Features:
 * - Animated icon scaling and pulsing
 * - Haptic feedback
 * - Theme-specific colors
 * - Auto-dismisses after 3 seconds
 */
@Composable
fun GrowthStageCelebration(
    stage: GrowthStage,
    theme: GardenTheme,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val visual = GrowthStageVisuals.getVisual(stage, theme)
    
    // Trigger haptic feedback on first composition
    LaunchedEffect(stage) {
        triggerHapticFeedback(context)
        // Auto-dismiss after 3 seconds
        delay(3000)
        onDismiss()
    }

    // Animation values
    val infiniteTransition = rememberInfiniteTransition(label = "celebration")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(300)) + scaleIn(
            initialScale = 0.3f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ),
        exit = fadeOut(animationSpec = tween(300)) + scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(300)
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                // Animated icon
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale),
                    contentAlignment = Alignment.Center
                ) {
                    // Placeholder for icon - in real implementation, use IconManager
                    Icon(
                        painter = painterResource(id = android.R.drawable.star_big_on),
                        contentDescription = "Growth stage achieved",
                        tint = visual.color,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Stage name
                Text(
                    text = "🎉 ${stage.displayName} 🎉",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Stage description
                Text(
                    text = stage.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Theme-specific description
                Text(
                    text = visual.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = visual.color,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Tasks completed
                Text(
                    text = "${stage.tasksRequired} tasks completed!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Trigger haptic feedback for celebration
 */
private fun triggerHapticFeedback(context: Context) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create a celebration pattern: short-long-short
                val timings = longArrayOf(0, 100, 50, 200, 50, 100)
                val amplitudes = intArrayOf(0, 128, 0, 255, 0, 128)
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                it.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(longArrayOf(0, 100, 50, 200, 50, 100), -1)
            }
        }
    } catch (e: Exception) {
        // Silently fail if haptic feedback is not available
    }
}
