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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
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
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import com.nami.peace.ui.theme.White
import com.nami.peace.ui.theme.AccentRed
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.clip

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
    val hazeState = remember { HazeState() }
    var isSnoozed by remember { mutableStateOf(false) }

    // Delayed Exit for Snooze
    LaunchedEffect(isSnoozed) {
        if (isSnoozed) {
            delay(2000)
            onSnooze()
        }
    }
    
    // Clock Logic
    val currentTime by produceState(initialValue = LocalTime.now()) {
        while (true) {
            value = LocalTime.now()
            delay(1000)
        }
    }
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Serene Background Image
        AsyncImage(
            model = R.drawable.peace_alarm_bg,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Haze Blur Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .haze(
                    state = hazeState,
                    style = HazeStyle(blurRadius = 40.dp, tint = Color.Black.copy(alpha = 0.15f))
                )
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.1f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.4f)
                        )
                    )
                )
        )

        // 3. Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Upper Section: Clock & Greeting
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = currentTime.format(timeFormatter),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Light,
                    fontSize = 84.sp,
                    letterSpacing = (-2).sp
                ),
                color = White
            )
            
            Text(
                text = stringResource(R.string.alarm_hero_title).uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Center Section: Primary Reminder
            reminders.firstOrNull()?.let { hero ->
                com.nami.peace.ui.alarm.GlassyCard(
                    hazeState = hazeState,
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(AccentRed.copy(alpha = 0.2f), CircleShape)
                                .border(1.dp, AccentRed.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = AccentRed,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = hero.title,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = White,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        
                        if (!hero.notes.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = hero.notes,
                                style = MaterialTheme.typography.bodyMedium,
                                color = White.copy(alpha = 0.8f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                        
                        // Priority Badge
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            color = White.copy(alpha = 0.1f),
                            shape = CircleShape,
                            modifier = Modifier.border(0.5.dp, White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Text(
                                text = hero.priority.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = White.copy(alpha = 0.8f),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1.2f))

            // Lower Section: Bundled Reminders (if any)
            if (reminders.size > 1) {
                Text(
                    text = stringResource(R.string.alarm_bundle_header),
                    style = MaterialTheme.typography.labelMedium,
                    color = White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(reminders.drop(1)) { reminder ->
                        com.nami.peace.ui.alarm.GlassySmallCard(hazeState = hazeState, title = reminder.title)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            if (isSnoozed) {
                // Snooze Confirmation Overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(White.copy(alpha = 0.9f))
                        .hazeChild(state = hazeState),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Snoozed for 2 minutes",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // Bottom Section: Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    com.nami.peace.ui.alarm.GlassyActionButton(
                        onClick = onStop,
                        icon = Icons.Default.Check,
                        label = stringResource(R.string.im_doing_it_stop_all),
                        containerColor = White.copy(alpha = 0.9f),
                        contentColor = Color.Black,
                        hazeState = hazeState,
                        modifier = Modifier.weight(1.5f)
                    )
                    
                    com.nami.peace.ui.alarm.GlassyActionButton(
                        onClick = { isSnoozed = true },
                        icon = Icons.Default.Snooze,
                        label = stringResource(R.string.snooze_all),
                        containerColor = White.copy(alpha = 0.15f),
                        contentColor = White,
                        hazeState = hazeState,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
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