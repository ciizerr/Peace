package com.nami.peace.ui.alarm

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nami.peace.scheduler.ReminderService
import com.nami.peace.ui.theme.PeaceTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class AlarmActivity : ComponentActivity() {

    private var wakeLock: android.os.PowerManager.WakeLock? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Lock Orientation
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // 2. Acquire WakeLock immediately to keep CPU running
        val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        wakeLock = powerManager.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "Peace:AlarmActivityWakeLock")
        wakeLock?.acquire(10 * 60 * 1000L /*10 minutes*/)
        com.nami.peace.util.DebugLogger.log("AlarmActivity: Acquired WakeLock.")

        // 3. Turn Screen On and Keyguard Handling
        turnScreenOnAndKeyguard()
        
        val reminderTitle = intent.getStringExtra("REMINDER_TITLE") ?: "Reminder"
        val reminderPriority = intent.getStringExtra("REMINDER_PRIORITY") ?: "MEDIUM"

        setContent {
            PeaceTheme {
                AlarmScreen(
                    title = reminderTitle,
                    priority = reminderPriority,
                    onStop = {
                        stopService()
                        finish()
                    },
                    onSnooze = {
                        // For now, snooze behaves like stop (as per requirements)
                        stopService()
                        finish()
                    }
                )
            }
        }
    }

    private fun stopService() {
        val serviceIntent = Intent(this, ReminderService::class.java)
        stopService(serviceIntent)
    }

    private fun turnScreenOnAndKeyguard() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        keyguardManager.requestDismissKeyguard(this, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release WakeLock
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
                com.nami.peace.util.DebugLogger.log("AlarmActivity: Released WakeLock.")
            }
        }
        wakeLock = null
    }
}

@Composable
fun AlarmScreen(
    title: String,
    priority: String,
    onStop: () -> Unit,
    onSnooze: () -> Unit
) {
    // Dynamic Background Gradient
    val gradientColors = if (priority == "HIGH") {
        listOf(Color(0xFFB71C1C), Color(0xFF212121)) // Deep Red -> Dark Grey
    } else {
        listOf(Color(0xFF1976D2), Color(0xFF212121)) // Ocean Blue -> Dark Grey
    }

    // Clock Logic
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(1000)
        }
    }
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    // Animation Logic
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black // Fallback
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradientColors)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                // Top: Clock
                Text(
                    text = currentTime.format(timeFormatter),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 80.sp,
                        color = Color.White
                    )
                )
                
                Spacer(modifier = Modifier.height(48.dp))

                // Middle: Pulsing Icon & Title
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(120.dp)
                ) {
                    // Pulsing Background Circle
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .scale(scale)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    )
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = "Alarm",
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(64.dp))

                // Bottom: Buttons
                Button(
                    onClick = onStop,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "I'M DOING IT (STOP)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onSnooze,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color.White)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "SNOOZE",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}