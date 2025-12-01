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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Celebration animation displayed when a milestone is reached.
 * 
 * Features:
 * - Animated trophy/star icon with rotation and scaling
 * - Haptic feedback
 * - Milestone-specific messages
 * - Auto-dismisses after 4 seconds
 */
@Composable
fun MilestoneCelebration(
    milestone: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Trigger haptic feedback on first composition
    LaunchedEffect(milestone) {
        triggerMilestoneHapticFeedback(context)
        // Auto-dismiss after 4 seconds
        delay(4000)
        onDismiss()
    }

    // Animation values
    val infiniteTransition = rememberInfiniteTransition(label = "milestone_celebration")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(400)) + scaleIn(
            initialScale = 0.2f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ),
        exit = fadeOut(animationSpec = tween(400)) + scaleOut(
            targetScale = 0.7f,
            animationSpec = tween(400)
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                // Animated trophy icon
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(scale)
                        .rotate(rotation),
                    contentAlignment = Alignment.Center
                ) {
                    // Trophy icon
                    Icon(
                        painter = painterResource(id = android.R.drawable.star_big_on),
                        contentDescription = "Milestone achieved",
                        tint = getMilestoneColor(milestone),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Milestone title
                Text(
                    text = "🏆 MILESTONE ACHIEVED! 🏆",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Milestone value
                Text(
                    text = "$milestone Day Streak!",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 48.sp
                    ),
                    color = getMilestoneColor(milestone),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Milestone message
                Text(
                    text = getMilestoneMessage(milestone),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp
                    ),
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Encouragement
                Text(
                    text = "Keep up the amazing work!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Get color based on milestone tier
 */
private fun getMilestoneColor(milestone: Int): Color {
    return when (milestone) {
        7 -> Color(0xFFFFD700) // Gold
        30 -> Color(0xFF00CED1) // Turquoise
        100 -> Color(0xFFFF69B4) // Hot Pink
        365 -> Color(0xFF9370DB) // Medium Purple
        else -> Color(0xFFFFD700) // Default gold
    }
}

/**
 * Get message based on milestone
 */
private fun getMilestoneMessage(milestone: Int): String {
    return when (milestone) {
        7 -> "One week of consistency! You're building a strong habit."
        30 -> "A full month of dedication! You're unstoppable."
        100 -> "100 days of excellence! You're a productivity champion."
        365 -> "A FULL YEAR! You've achieved something truly remarkable."
        else -> "You've reached an incredible milestone!"
    }
}

/**
 * Trigger enhanced haptic feedback for milestone celebration
 */
private fun triggerMilestoneHapticFeedback(context: Context) {
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
                // Create a special milestone pattern: crescendo effect
                val timings = longArrayOf(0, 100, 50, 150, 50, 200, 50, 250, 50, 300)
                val amplitudes = intArrayOf(0, 100, 0, 150, 0, 200, 0, 250, 0, 255)
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                it.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(longArrayOf(0, 100, 50, 150, 50, 200, 50, 250, 50, 300), -1)
            }
        }
    } catch (e: Exception) {
        // Silently fail if haptic feedback is not available
    }
}
