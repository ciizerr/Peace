package com.nami.peace.ui.settings.rhythms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import java.util.Calendar
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nami.peace.R
import com.nami.peace.ui.settings.components.*
import com.nami.peace.ui.settings.SettingsViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.media.RingtoneManager
import android.net.Uri
import android.content.Intent
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RhythmsScreen(
    onNavigateBack: () -> Unit,
    hazeState: HazeState? = null,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val effectiveHazeState = hazeState ?: remember { HazeState() }
    
    // Immersion Settings (needed for the glassy top bar)
    val blurEnabled by viewModel.blurEnabled.collectAsState()
    val shadowsEnabled by viewModel.shadowsEnabled.collectAsState()
    val blurStrength by viewModel.blurStrength.collectAsState()
    val blurTintAlpha by viewModel.blurTintAlpha.collectAsState()
    val shadowStrength by viewModel.shadowStrength.collectAsState()

    val shadowStyle = when {
        shadowStrength == 0f -> "None"
        shadowStrength <= 0.33f -> "Subtle"
        shadowStrength <= 0.66f -> "Medium"
        else -> "Heavy"
    }
    
    // Settings states
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()
    val soundVolume by viewModel.soundVolume.collectAsState()
    val selectedSoundscape by viewModel.selectedSoundscape.collectAsState()
    val selectedSoundUri by viewModel.selectedSoundUri.collectAsState()
    val quietHoursEnabled by viewModel.quietHoursEnabled.collectAsState()
    val quietHoursStart by viewModel.quietHoursStart.collectAsState()
    val quietHoursEnd by viewModel.quietHoursEnd.collectAsState()
    val nagModeEnabled by viewModel.nagModeEnabled.collectAsState()
    val nagModeInterval by viewModel.nagModeInterval.collectAsState()
    val nagModeMaxRepetitions by viewModel.nagModeMaxRepetitions.collectAsState()
    
    // Ringtone Picker
    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            viewModel.setSelectedSoundUri(uri?.toString())
        }
    }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .haze(effectiveHazeState)
                    .verticalScroll(scrollState)
                    .padding(
                        top = paddingValues.calculateTopPadding() + 80.dp,
                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
                        start = 0.dp,
                        end = 0.dp
                    )
            ) {
                // Notifications Section
                GlassySettingSection(title = "Notifications") {
                    GlassySwitchRow(
                        label = "Enable Notifications",
                        subtitle = "Allow Peace to send reminder notifications",
                        imageVector = Icons.Default.Notifications,
                        checked = notificationsEnabled,
                        onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                    )
                }

                // Sound & Vibration Section
                GlassySettingSection(title = "Sound & Vibration") {
                    GlassySwitchRow(
                        label = "Sound",
                        subtitle = "Play sound for reminders",
                        imageVector = Icons.Default.VolumeUp,
                        checked = soundEnabled,
                        onCheckedChange = { viewModel.setSoundEnabled(it) }
                    )
                    
                    AnimatedVisibility(visible = soundEnabled) {
                        Column {
                            GlassySliderRow(
                                label = "Volume",
                                value = soundVolume,
                                onValueChange = { viewModel.setSoundVolume(it) },
                                valueRange = 0f..1f,
                                steps = 10
                            )
                            
                            // Dedicated Pick Sound Button
                            Button(
                                onClick = {
                                    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Reminder Sound")
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, selectedSoundUri?.let { Uri.parse(it) })
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                                    }
                                    ringtonePickerLauncher.launch(intent)
                                },
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Icon(
                                    Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pick Sound...")
                            }
                        }
                    }
                    
                    GlassySwitchRow(
                        label = "Vibration",
                        subtitle = "Vibrate device for reminders",
                        checked = vibrationEnabled,
                        onCheckedChange = { viewModel.setVibrationEnabled(it) }
                    )
                }

                // Quiet Hours Section
                GlassySettingSection(title = "Quiet Hours") {
                    GlassySwitchRow(
                        label = "Enable Quiet Hours",
                        subtitle = "Reduce notification intensity during specified hours",
                        imageVector = Icons.Default.DoNotDisturb,
                        checked = quietHoursEnabled,
                        onCheckedChange = { viewModel.setQuietHoursEnabled(it) }
                    )
                    
                    AnimatedVisibility(visible = quietHoursEnabled) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showStartPicker = true }
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "Start Time",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = quietHoursStart,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showEndPicker = true }
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "End Time",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = quietHoursEnd,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Start Time Picker
                val startParts = remember(quietHoursStart) { quietHoursStart.split(":") }
                val startH = startParts.getOrNull(0)?.toIntOrNull() ?: 22
                val startM = startParts.getOrNull(1)?.toIntOrNull() ?: 0
                val startPickerState = rememberTimePickerState(initialHour = startH, initialMinute = startM)
                
                com.nami.peace.ui.components.GlassyTimePicker(
                    show = showStartPicker,
                    state = startPickerState,
                    onDismiss = { showStartPicker = false },
                    onConfirm = {
                        val timeStr = String.format("%02d:%02d", startPickerState.hour, startPickerState.minute)
                        viewModel.setQuietHoursStart(timeStr)
                        showStartPicker = false
                    },
                    hazeState = effectiveHazeState
                )

                // End Time Picker
                val endParts = remember(quietHoursEnd) { quietHoursEnd.split(":") }
                val endH = endParts.getOrNull(0)?.toIntOrNull() ?: 7
                val endM = endParts.getOrNull(1)?.toIntOrNull() ?: 0
                val endPickerState = rememberTimePickerState(initialHour = endH, initialMinute = endM)
                
                com.nami.peace.ui.components.GlassyTimePicker(
                    show = showEndPicker,
                    state = endPickerState,
                    onDismiss = { showEndPicker = false },
                    onConfirm = {
                        val timeStr = String.format("%02d:%02d", endPickerState.hour, endPickerState.minute)
                        viewModel.setQuietHoursEnd(timeStr)
                        showEndPicker = false
                    },
                    hazeState = effectiveHazeState
                )

                // Nag Mode Section
                GlassySettingSection(title = "Nag Mode") {
                    val nagModeInterval by viewModel.nagModeInterval.collectAsState()
                    val nagModeMaxRepetitions by viewModel.nagModeMaxRepetitions.collectAsState()

                    GlassySwitchRow(
                        label = "Enable Nag Mode",
                        subtitle = "Repeat rhythm until explicitly completed",
                        imageVector = Icons.Default.Schedule,
                        checked = nagModeEnabled,
                        onCheckedChange = { viewModel.setNagModeEnabled(it) }
                    )
                    
                    // Detailed Description as requested
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "UNDERSTANDING RHYTHMS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        HelpEntry(
                            title = "Nag Mode",
                            desc = "The rhythm will repeat at intervals until you mark it as 'Done'. Never miss a moment again."
                        )
                        HelpEntry(
                            title = "Strict (Anchored)",
                            desc = "Repetitions stay fixed to their original times. If you're late, it skips to the current slot to keep you on schedule."
                        )
                        HelpEntry(
                            title = "Flexible (Drift)",
                            desc = "Each repetition waits for the previous one. If you're delayed, the whole sequence shifts to give you space."
                        )
                    }
                    
                    AnimatedVisibility(visible = nagModeEnabled) {
                        Column {
                            GlassySliderRow(
                                label = "Interval (minutes)",
                                value = nagModeInterval.toFloat(),
                                onValueChange = { viewModel.setNagModeInterval(it.toInt()) },
                                valueRange = 1f..60f,
                                steps = 59
                            )
                            
                            GlassySliderRow(
                                label = "Max Repetitions",
                                value = nagModeMaxRepetitions.toFloat(),
                                onValueChange = { viewModel.setNagModeMaxRepetitions(it.toInt()) },
                                valueRange = 1f..20f,
                                steps = 19
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(100.dp))
            }

            // Floating Glassy Top Bar
            com.nami.peace.ui.components.GlassyTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.title_rhythms),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                modifier = Modifier.align(Alignment.TopCenter),
                hazeState = effectiveHazeState,
                blurEnabled = blurEnabled,
                blurStrength = blurStrength,
                blurTintAlpha = blurTintAlpha,
                shadowsEnabled = shadowsEnabled,
                shadowStyle = shadowStyle
            )
        }
    }
}

@Composable
fun HelpEntry(title: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
