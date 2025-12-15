package com.nami.peace.ui.alarm

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.hilt.navigation.compose.hiltViewModel
import com.nami.peace.domain.model.Reminder
import com.nami.peace.domain.repository.ReminderRepository
import com.nami.peace.scheduler.AlarmReceiver
import com.nami.peace.scheduler.ReminderService
import com.nami.peace.ui.theme.PeaceTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import com.nami.peace.R

@AndroidEntryPoint
class AlarmActivity : ComponentActivity() {

    private var wakeLock: android.os.PowerManager.WakeLock? = null
    private var bundledReminderIds: ArrayList<Int> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Lock Orientation
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        
        // Fix: Set window background to black to avoid white flash
        window.decorView.setBackgroundColor(android.graphics.Color.BLACK)

        // 2. Acquire WakeLock
        val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        wakeLock = powerManager.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "Peace:AlarmActivityWakeLock")
        wakeLock?.acquire(10 * 60 * 1000L)

        // 3. Turn Screen On
        turnScreenOnAndKeyguard()
        
        // 4. Get Data - now includes bundled reminder IDs
        val reminderId = intent.getIntExtra("REMINDER_ID", -1)
        bundledReminderIds = intent.getIntegerArrayListExtra("BUNDLED_REMINDER_IDS") ?: arrayListOf(reminderId)
        val reminderTitle = intent.getStringExtra("REMINDER_TITLE") ?: "Reminder"
        val reminderPriority = intent.getStringExtra("REMINDER_PRIORITY") ?: "MEDIUM"

        setContent {
            PeaceTheme {
                AlarmScreenWithViewModel(
                    bundledReminderIds = bundledReminderIds,
                    onStop = {
                        // SEND "COMPLETE" SIGNAL for ALL bundled reminders
                        sendActionForAll("com.nami.peace.ACTION_COMPLETE")
                    },
                    onSnooze = {
                        // SEND "SNOOZE" SIGNAL for ALL bundled reminders
                        sendActionForAll("com.nami.peace.ACTION_SNOOZE")
                    }
                )
            }
        }
    }

    // --- UPDATED HELPER FUNCTION to handle multiple reminders ---
    private fun sendActionForAll(actionName: String) {
        // Send action for each bundled reminder
        bundledReminderIds.forEach { id ->
            val intent = Intent(this, AlarmReceiver::class.java).apply {
                action = actionName
                putExtra("REMINDER_ID", id)
            }
            sendBroadcast(intent)
        }
        
        // We still finish the UI, but we let the Receiver stop the Service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask()
        } else {
            finish()
        }
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
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }
}

// ViewModel for loading bundled reminders
@HiltViewModel
class BundledAlarmViewModel @Inject constructor(
    private val repository: ReminderRepository
) : ViewModel() {
    
    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()
    
    fun loadReminders(ids: List<Int>) {
        viewModelScope.launch {
            val loaded = ids.mapNotNull { repository.getReminderById(it) }
                .sortedBy { it.priority.ordinal } // HIGH=0, MEDIUM=1, LOW=2
            _reminders.value = loaded
        }
    }
}

@Composable
fun AlarmScreenWithViewModel(
    bundledReminderIds: List<Int>,
    onStop: () -> Unit,
    onSnooze: () -> Unit,
    viewModel: BundledAlarmViewModel = hiltViewModel()
) {
    val reminders by viewModel.reminders.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Prevent back navigation from destroying the activity, instead minimize it
    androidx.activity.compose.BackHandler {
        (context as? android.app.Activity)?.moveTaskToBack(true)
    }
    
    LaunchedEffect(bundledReminderIds) {
        viewModel.loadReminders(bundledReminderIds)
    }
    
    if (reminders.isEmpty()) {
        // Loading state
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    } else {
        AlarmScreenMultiple(
            reminders = reminders,
            onStop = onStop,
            onSnooze = onSnooze
        )
    }
}

@Composable
fun AlarmScreenMultiple(
    reminders: List<Reminder>,
    onStop: () -> Unit,
    onSnooze: () -> Unit
) {
    // Use the highest priority for gradient
    val highestPriority = reminders.firstOrNull()?.priority?.name ?: "MEDIUM"
    val gradientColors = when (highestPriority) {
        "HIGH" -> listOf(Color(0xFFB71C1C), Color(0xFF212121))
        "MEDIUM" -> listOf(Color(0xFF1976D2), Color(0xFF212121))
        else -> listOf(Color(0xFF2E7D32), Color(0xFF212121))
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
        color = Color.Black 
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradientColors))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Top: Clock
                Text(
                    text = currentTime.format(timeFormatter),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 64.sp,
                        color = Color.White
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                // Middle: Pulsing Icon
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(100.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .scale(scale)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    )
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = stringResource(R.string.cd_alarm),
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Show count if multiple
                if (reminders.size > 1) {
                    Text(
                        text = stringResource(R.string.reminders_due_format, reminders.size),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // List of reminders (sorted by priority)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(reminders) { reminder ->
                        ReminderCard(reminder = reminder)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

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
                        text = stringResource(R.string.im_doing_it_stop_all),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                        text = stringResource(R.string.snooze_all),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun ReminderCard(reminder: Reminder) {
    val priorityColor = when (reminder.priority.name) {
        "HIGH" -> Color(0xFFEF5350)
        "MEDIUM" -> Color(0xFF42A5F5)
        else -> Color(0xFF66BB6A)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Priority indicator
            Box(
                modifier = Modifier
                    .size(8.dp, 40.dp)
                    .background(priorityColor, RoundedCornerShape(4.dp))
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = reminder.priority.name,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.7f)
                    )
                )
            }
        }
    }
}