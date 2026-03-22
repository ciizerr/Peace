package com.nami.peace.ui.settings.sanctuary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
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
fun SanctuaryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHistory: () -> Unit,
    hazeState: HazeState? = null,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val effectiveHazeState = hazeState ?: remember { HazeState() }
    
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    
    // Immersion Settings
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
    val autoBackupEnabled by viewModel.autoBackupEnabled.collectAsState()
    val analyticsEnabled by viewModel.analyticsEnabled.collectAsState()
    val crashReportingEnabled by viewModel.crashReportingEnabled.collectAsState()

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
                // Data Management Section
                GlassySettingSection(title = "Data Management") {
                    GlassyButtonRow(
                        title = "View History Log",
                        subtitle = "Browse your completed tasks and activity",
                        icon = Icons.Default.Storage,
                        onClick = onNavigateToHistory
                    )
                    
                    GlassyButtonRow(
                        title = "Export Data",
                        subtitle = "Download your data as JSON file",
                        icon = Icons.Default.Download,
                        onClick = { showExportDialog = true }
                    )
                    
                    GlassyButtonRow(
                        title = "Import Data",
                        subtitle = "Restore data from backup file",
                        icon = Icons.Default.Upload,
                        onClick = { /* TODO: Implement import */ }
                    )
                }

                // Backup & Sync Section
                GlassySettingSection(title = "Backup & Sync") {
                    GlassySwitchRow(
                        label = "Auto Backup",
                        subtitle = "Automatically backup your data locally",
                        imageVector = Icons.Default.Backup,
                        checked = autoBackupEnabled,
                        onCheckedChange = { viewModel.setAutoBackupEnabled(it) }
                    )
                    
                    if (autoBackupEnabled) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Last Backup",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Today at 3:42 PM",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Privacy & Security Section
                GlassySettingSection(title = "Privacy & Security") {
                    GlassySwitchRow(
                        label = "Anonymous Analytics",
                        subtitle = "Help improve Peace by sharing anonymous usage data",
                        imageVector = Icons.Default.Security,
                        checked = analyticsEnabled,
                        onCheckedChange = { viewModel.setAnalyticsEnabled(it) }
                    )
                    
                    GlassySwitchRow(
                        label = "Crash Reporting",
                        subtitle = "Automatically send crash reports to help fix bugs",
                        imageVector = Icons.Default.Security,
                        checked = crashReportingEnabled,
                        onCheckedChange = { viewModel.setCrashReportingEnabled(it) }
                    )
                }

                // Danger Zone Section
                GlassySettingSection(title = "Danger Zone") {
                    GlassyButtonRow(
                        title = "Clear All Data",
                        subtitle = "Permanently delete all reminders and history",
                        icon = Icons.Default.Delete,
                        onClick = { showClearDataDialog = true },
                        isDestructive = true
                    )
                }
                
                Spacer(modifier = Modifier.height(100.dp))
            }

            // Floating Glassy Top Bar
            com.nami.peace.ui.components.GlassyTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.title_sanctuary),
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

    // Clear Data Confirmation Dialog
    com.nami.peace.ui.components.GlassyAlertDialog(
        show = showClearDataDialog,
        hazeState = effectiveHazeState,
        onDismissRequest = { showClearDataDialog = false },
        title = { Text("Clear All Data?") },
        text = { 
            Text("This will permanently delete all your reminders, history, and settings. This action cannot be undone.")
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // TODO: Implement clear all data
                    showClearDataDialog = false
                }
            ) {
                Text("Clear All", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = { showClearDataDialog = false }) {
                Text("Cancel")
            }
        }
    )

    // Export Data Dialog
    com.nami.peace.ui.components.GlassyAlertDialog(
        show = showExportDialog,
        hazeState = effectiveHazeState,
        onDismissRequest = { showExportDialog = false },
        title = { Text("Export Data") },
        text = { 
            Text("Your data will be exported as a JSON file that you can save or share.")
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // TODO: Implement data export
                    showExportDialog = false
                }
            ) {
                Text("Export")
            }
        },
        dismissButton = {
            TextButton(onClick = { showExportDialog = false }) {
                Text("Cancel")
            }
        }
    )
}