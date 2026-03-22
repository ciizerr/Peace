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
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.launch
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
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    val json = stream.bufferedReader().use { reader -> reader.readText() }
                    viewModel.importDataFromJson(json)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    val json = viewModel.exportDataToJson()
                    context.contentResolver.openOutputStream(it)?.use { stream ->
                        stream.write(json.toByteArray())
                    }
                    android.widget.Toast.makeText(context, "Data exported successfully", android.widget.Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    android.widget.Toast.makeText(context, "Export failed", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
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
    val autoBackupFrequency by viewModel.autoBackupFrequency.collectAsState()
    val lastBackupTime by viewModel.lastBackupTime.collectAsState()
    val analyticsEnabled by viewModel.analyticsEnabled.collectAsState()
    val crashReportingEnabled by viewModel.crashReportingEnabled.collectAsState()

    val lastBackupString = remember(lastBackupTime) {
        if (lastBackupTime != null) {
            val sdf = java.text.SimpleDateFormat("MMM dd, yyyy • hh:mm a", java.util.Locale.getDefault())
            sdf.format(java.util.Date(lastBackupTime!!))
        } else {
            "Never"
        }
    }

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
                        subtitle = "Save your data as a .json file",
                        icon = Icons.Default.Download,
                        onClick = { exportLauncher.launch("Peace_Backup_${System.currentTimeMillis()}.json") }
                    )
                    
                    GlassyButtonRow(
                        title = "Import Data",
                        subtitle = "Restore data from backup file",
                        icon = Icons.Default.Upload,
                        onClick = { importLauncher.launch("application/json") }
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
                        GlassyDropdownRow(
                            title = "Backup Frequency",
                            subtitle = "How often to update the backup",
                            selectedValue = autoBackupFrequency,
                            options = listOf("Daily", "Weekly", "Monthly"),
                            onValueSelected = { viewModel.setAutoBackupFrequency(it) }
                        )

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
                                text = lastBackupString,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
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
                    viewModel.clearAllData()
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