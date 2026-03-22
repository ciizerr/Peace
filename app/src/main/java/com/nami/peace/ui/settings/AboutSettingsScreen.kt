package com.nami.peace.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nami.peace.BuildConfig
import com.nami.peace.R
import com.nami.peace.data.updater.UpdateState
import com.nami.peace.ui.settings.components.*
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSettingsScreen(
    onNavigateToHistory: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    viewModel: SettingsViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    hazeState: HazeState? = null
) {
    val effectiveHazeState = hazeState ?: remember { HazeState() }
    val updateStatus by viewModel.updateStatus.collectAsState()

    // Update Dialogs
    UpdateDialogs(updateStatus, viewModel, effectiveHazeState)

    val blurEnabled by viewModel.blurEnabled.collectAsState()
    val blurStrength by viewModel.blurStrength.collectAsState()
    val shadowsEnabled by viewModel.shadowsEnabled.collectAsState()
    val shadowStrength by viewModel.shadowStrength.collectAsState()
    val blurTintAlpha by viewModel.blurTintAlpha.collectAsState()

    val shadowStyle = when {
        shadowStrength == 0f -> "None"
        shadowStrength <= 0.33f -> "Subtle"
        shadowStrength <= 0.66f -> "Medium"
        else -> "Heavy"
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .haze(effectiveHazeState)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        top = padding.calculateTopPadding() + 80.dp,
                        bottom = padding.calculateBottomPadding() + 16.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // App Header Section
                AppHeaderSection()

                // App Information Section
                AppInfoSection(updateStatus, viewModel)

                // Features Section
                FeaturesSection(onNavigateToHistory)

                // Support Section
                SupportSection()

                // Legal Section
                LegalSection()

                // Developer Section
                DeveloperSection()
            }

            // Floating Glassy Top Bar
            com.nami.peace.ui.components.GlassyTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.title_wisdom),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToDashboard) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
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
private fun UpdateDialogs(updateStatus: UpdateState, viewModel: SettingsViewModel, hazeState: HazeState?) {
    val availableState = updateStatus as? UpdateState.Available
    val errorState = updateStatus as? UpdateState.Error
    
    val lastUpdateInfo = remember { androidx.compose.runtime.mutableStateOf(availableState?.updateInfo) }
    if (availableState != null) lastUpdateInfo.value = availableState.updateInfo
    
    val lastErrorMsg = remember { androidx.compose.runtime.mutableStateOf(errorState?.message) }
    if (errorState != null) lastErrorMsg.value = errorState.message

    com.nami.peace.ui.components.GlassyAlertDialog(
        show = updateStatus is UpdateState.Available,
        hazeState = hazeState,
        onDismissRequest = { viewModel.resetUpdateState() },
        title = { Text(stringResource(R.string.update_available_title, lastUpdateInfo.value?.version ?: "")) },
        text = { 
            Column {
                Text(stringResource(R.string.update_available_message))
                Spacer(modifier = Modifier.height(8.dp))
                Text(lastUpdateInfo.value?.releaseNotes ?: "", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = { lastUpdateInfo.value?.downloadUrl?.let { viewModel.startUpdate(it) } }) {
                Text(stringResource(R.string.update_now))
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.resetUpdateState() }) {
                Text(stringResource(R.string.later))
            }
        }
    )

    com.nami.peace.ui.components.GlassyAlertDialog(
        show = updateStatus is UpdateState.Error,
        hazeState = hazeState,
        onDismissRequest = { viewModel.resetUpdateState() },
        title = { Text(stringResource(R.string.update_failed_title)) },
        text = { Text(lastErrorMsg.value ?: "") },
        confirmButton = {
            TextButton(onClick = { viewModel.resetUpdateState() }) {
                Text(stringResource(R.string.ok))
            }
        }
    )

    com.nami.peace.ui.components.GlassyAlertDialog(
        show = updateStatus is UpdateState.UpToDate,
        hazeState = hazeState,
        onDismissRequest = { viewModel.resetUpdateState() },
        title = { Text(stringResource(R.string.up_to_date_title)) },
        text = { Text(stringResource(R.string.up_to_date_message)) },
        confirmButton = {
            TextButton(onClick = { viewModel.resetUpdateState() }) {
                Text(stringResource(R.string.ok))
            }
        }
    )
}

@Composable
private fun AppHeaderSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // App Icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Find your inner peace through mindful productivity",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AppInfoSection(updateStatus: UpdateState, viewModel: SettingsViewModel) {
    GlassySettingSection(
        title = "App Information"
    ) {
        InfoRow("Version", stringResource(R.string.version_format, BuildConfig.VERSION_NAME))
        InfoRow("Build", BuildConfig.VERSION_CODE.toString())
        InfoRow("Target SDK", "34 (Android 14)")
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Update Button
        when (updateStatus) {
            is UpdateState.Downloading -> {
                val progress = updateStatus.progress
                Column {
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.downloading_format, progress),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            is UpdateState.Checking -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Text("Checking for updates...")
                }
            }
            else -> {
                Button(
                    onClick = { viewModel.checkForUpdates() },
                    enabled = updateStatus is UpdateState.Idle,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Update, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.check_for_updates))
                }
            }
        }
    }
}

@Composable
private fun FeaturesSection(onNavigateToHistory: () -> Unit) {
    GlassySettingSection(
        title = "Features"
    ) {
        GlassyButtonRow(
            title = "View History Log",
            subtitle = "Browse your completed tasks and activity",
            icon = Icons.Default.Info,
            onClick = onNavigateToHistory
        )
        
        FeatureItem(
            title = "Smart Reminders",
            description = "Intelligent notification system with nag mode"
        )
        
        FeatureItem(
            title = "Peaceful Design",
            description = "Glassmorphism UI with customizable themes"
        )
        
        FeatureItem(
            title = "Privacy First",
            description = "All data stored locally on your device"
        )
    }
}

@Composable
private fun SupportSection() {
    GlassySettingSection(
        title = "Support"
    ) {
        GlassyButtonRow(
            title = "Rate Peace",
            subtitle = "Help us improve by rating the app",
            icon = Icons.Default.Star,
            onClick = { /* TODO: Open Play Store */ }
        )
        
        GlassyButtonRow(
            title = "Share Peace",
            subtitle = "Share this app with friends and family",
            icon = Icons.Default.Share,
            onClick = { /* TODO: Share app */ }
        )
        
        GlassyButtonRow(
            title = "Report Bug",
            subtitle = "Found an issue? Let us know",
            icon = Icons.Default.BugReport,
            onClick = { /* TODO: Open bug report */ }
        )
    }
}

@Composable
private fun LegalSection() {
    GlassySettingSection(
        title = "Legal"
    ) {
        GlassyButtonRow(
            title = "Privacy Policy",
            subtitle = "How we handle your data",
            icon = Icons.Default.Security,
            onClick = { /* TODO: Open privacy policy */ }
        )
        
        GlassyButtonRow(
            title = "Terms of Service",
            subtitle = "Terms and conditions of use",
            icon = Icons.Default.Info,
            onClick = { /* TODO: Open terms */ }
        )
        
        GlassyButtonRow(
            title = "Open Source Licenses",
            subtitle = "Third-party libraries and licenses",
            icon = Icons.Default.Code,
            onClick = { /* TODO: Open licenses */ }
        )
    }
}

@Composable
private fun DeveloperSection() {
    GlassySettingSection(
        title = "Developer"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Made with ❤️ by the Peace Team",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "© 2024 Peace App. All rights reserved.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun FeatureItem(title: String, description: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
