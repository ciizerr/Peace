package com.nami.peace.ui.settings.rhythms

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nami.peace.R
import com.nami.peace.ui.settings.components.*
import com.nami.peace.ui.settings.SettingsViewModel
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
    val quietHoursEnabled by viewModel.quietHoursEnabled.collectAsState()
    val quietHoursStart by viewModel.quietHoursStart.collectAsState()
    val quietHoursEnd by viewModel.quietHoursEnd.collectAsState()
    val nagModeEnabled by viewModel.nagModeEnabled.collectAsState()
    val nagModeInterval by viewModel.nagModeInterval.collectAsState()
    val nagModeMaxRepetitions by viewModel.nagModeMaxRepetitions.collectAsState()

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
                            
                            GlassyDropdownRow(
                                title = "Soundscape",
                                subtitle = "Choose your reminder sound",
                                selectedValue = selectedSoundscape,
                                options = listOf("Default", "Gentle Bell", "Nature", "Chime", "Soft Tone"),
                                onValueSelected = { viewModel.setSelectedSoundscape(it) }
                            )
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
                GlassySettingSection(title = "Nag Mode") {
                    GlassySwitchRow(
                        label = "Enable Nag Mode",
                        subtitle = "Repeat reminders until completed",
                        imageVector = Icons.Default.Schedule,
                        checked = nagModeEnabled,
                        onCheckedChange = { viewModel.setNagModeEnabled(it) }
                    )
                    
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