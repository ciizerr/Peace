package com.nami.peace.ui.settings.sanctuary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nami.peace.R
import com.nami.peace.ui.components.SettingsSection
import com.nami.peace.ui.components.SettingsSwitch
import com.nami.peace.ui.components.SettingsButton
import com.nami.peace.ui.settings.SettingsViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SanctuaryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHistory: () -> Unit,
    hazeState: HazeState,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    
    // Settings states
    val autoBackupEnabled by viewModel.autoBackupEnabled.collectAsState()
    val analyticsEnabled by viewModel.analyticsEnabled.collectAsState()
    val crashReportingEnabled by viewModel.crashReportingEnabled.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_sanctuary)) },
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
            // Data Management Section
            SettingsSection(
                title = "Data Management",
                icon = Icons.Default.Storage
            ) {
                SettingsButton(
                    title = "View History Log",
                    subtitle = "Browse your completed tasks and activity",
                    icon = Icons.Default.Storage,
                    onClick = onNavigateToHistory
                )
                
                SettingsButton(
                    title = "Export Data",
                    subtitle = "Download your data as JSON file",
                    icon = Icons.Default.Download,
                    onClick = { showExportDialog = true }
                )
                
                SettingsButton(
                    title = "Import Data",
                    subtitle = "Restore data from backup file",
                    icon = Icons.Default.Upload,
                    onClick = { /* TODO: Implement import */ }
                )
            }

            // Backup & Sync Section
            SettingsSection(
                title = "Backup & Sync",
                icon = Icons.Default.Backup
            ) {
                SettingsSwitch(
                    title = "Auto Backup",
                    subtitle = "Automatically backup your data locally",
                    checked = autoBackupEnabled,
                    onCheckedChange = { viewModel.setAutoBackupEnabled(it) }
                )
                
                if (autoBackupEnabled) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
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
            }

            // Privacy & Security Section
            SettingsSection(
                title = "Privacy & Security",
                icon = Icons.Default.Security
            ) {
                SettingsSwitch(
                    title = "Anonymous Analytics",
                    subtitle = "Help improve Peace by sharing anonymous usage data",
                    checked = analyticsEnabled,
                    onCheckedChange = { viewModel.setAnalyticsEnabled(it) }
                )
                
                SettingsSwitch(
                    title = "Crash Reporting",
                    subtitle = "Automatically send crash reports to help fix bugs",
                    checked = crashReportingEnabled,
                    onCheckedChange = { viewModel.setCrashReportingEnabled(it) }
                )
            }

            // Danger Zone Section
            SettingsSection(
                title = "Danger Zone",
                icon = Icons.Default.Delete
            ) {
                SettingsButton(
                    title = "Clear All Data",
                    subtitle = "Permanently delete all reminders and history",
                    icon = Icons.Default.Delete,
                    onClick = { showClearDataDialog = true },
                    isDestructive = true
                )
            }
        }
    }

    // Clear Data Confirmation Dialog
    if (showClearDataDialog) {
        AlertDialog(
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
    }

    // Export Data Dialog
    if (showExportDialog) {
        AlertDialog(
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
}