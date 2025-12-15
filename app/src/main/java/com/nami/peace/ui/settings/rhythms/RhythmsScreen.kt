package com.nami.peace.ui.settings.rhythms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nami.peace.R
import com.nami.peace.ui.components.SettingsSection
import com.nami.peace.ui.components.SettingsSlider
import com.nami.peace.ui.components.SettingsSwitch
import com.nami.peace.ui.components.SettingsDropdown
import com.nami.peace.ui.settings.SettingsViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RhythmsScreen(
    onNavigateBack: () -> Unit,
    hazeState: HazeState,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    
    // Settings states
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()
    val soundVolume by viewModel.soundVolume.collectAsState()
    val selectedSoundscape by viewModel.selectedSoundscape.collectAsState()
    val quietHoursEnabled by viewModel.quietHoursEnabled.collectAsState()
    val quietHoursStart by viewModel.quietHoursStart.collectAsState()
    val quietHoursEnd by viewModel.quietHoursEnd.collectAsState()
    val nagModeEnabled by viewModel.nagModeEnabled.collectAsState()
    val nagModeInterval by viewModel.nagModeInterval.collectAsState()
    val nagModeMaxRepetitions by viewModel.nagModeMaxRepetitions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_rhythms)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        modifier = Modifier.haze(hazeState)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Notifications Section
            SettingsSection(
                title = "Notifications",
                icon = Icons.Default.Notifications
            ) {
                SettingsSwitch(
                    title = "Enable Notifications",
                    subtitle = "Allow Peace to send reminder notifications",
                    checked = notificationsEnabled,
                    onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                )
            }

            // Sound & Vibration Section
            SettingsSection(
                title = "Sound & Vibration",
                icon = Icons.Default.VolumeUp
            ) {
                SettingsSwitch(
                    title = "Sound",
                    subtitle = "Play sound for reminders",
                    checked = soundEnabled,
                    onCheckedChange = { viewModel.setSoundEnabled(it) }
                )
                
                if (soundEnabled) {
                    SettingsSlider(
                        title = "Volume",
                        value = soundVolume,
                        onValueChange = { viewModel.setSoundVolume(it) },
                        valueRange = 0f..1f,
                        steps = 10
                    )
                    
                    SettingsDropdown(
                        title = "Soundscape",
                        subtitle = "Choose your reminder sound",
                        selectedValue = selectedSoundscape,
                        options = listOf("Default", "Gentle Bell", "Nature", "Chime", "Soft Tone"),
                        onValueSelected = { viewModel.setSelectedSoundscape(it) }
                    )
                }
                
                SettingsSwitch(
                    title = "Vibration",
                    subtitle = "Vibrate device for reminders",
                    checked = vibrationEnabled,
                    onCheckedChange = { viewModel.setVibrationEnabled(it) }
                )
            }

            // Quiet Hours Section
            SettingsSection(
                title = "Quiet Hours",
                icon = Icons.Default.DoNotDisturb
            ) {
                SettingsSwitch(
                    title = "Enable Quiet Hours",
                    subtitle = "Reduce notification intensity during specified hours",
                    checked = quietHoursEnabled,
                    onCheckedChange = { viewModel.setQuietHoursEnabled(it) }
                )
                
                if (quietHoursEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Start Time",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = quietHoursStart,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "End Time",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = quietHoursEnd,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Nag Mode Section
            SettingsSection(
                title = "Nag Mode",
                icon = Icons.Default.Schedule
            ) {
                SettingsSwitch(
                    title = "Enable Nag Mode",
                    subtitle = "Repeat reminders until completed",
                    checked = nagModeEnabled,
                    onCheckedChange = { viewModel.setNagModeEnabled(it) }
                )
                
                if (nagModeEnabled) {
                    SettingsSlider(
                        title = "Interval (minutes)",
                        value = nagModeInterval.toFloat(),
                        onValueChange = { viewModel.setNagModeInterval(it.toInt()) },
                        valueRange = 1f..60f,
                        steps = 59
                    )
                    
                    SettingsSlider(
                        title = "Max Repetitions",
                        value = nagModeMaxRepetitions.toFloat(),
                        onValueChange = { viewModel.setNagModeMaxRepetitions(it.toInt()) },
                        valueRange = 1f..20f,
                        steps = 19
                    )
                }
            }
        }
    }
}