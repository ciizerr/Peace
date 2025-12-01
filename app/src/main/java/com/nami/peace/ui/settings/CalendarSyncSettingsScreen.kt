package com.nami.peace.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nami.peace.R
import com.nami.peace.ui.components.BackgroundWrapper
import com.nami.peace.ui.components.PeaceIcon
import com.nami.peace.ui.home.HomeViewModel
import com.nami.peace.util.background.BackgroundImageManager
import com.nami.peace.util.icon.IconManager
import com.nami.peace.util.icon.IoniconsManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarSyncSettingsScreen(
    onNavigateUp: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    backgroundImageManager: BackgroundImageManager? = null
) {
    val context = LocalContext.current
    val iconManager: IconManager = remember { IoniconsManager(context) }
    
    // State
    val calendarSyncEnabled by viewModel.calendarSyncEnabled.collectAsState()
    val isAuthenticated by viewModel.isCalendarAuthenticated.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val syncedCount by viewModel.syncedCount.collectAsState()
    val syncError by viewModel.syncError.collectAsState()
    
    // Background settings
    val blurIntensity by viewModel.blurIntensity.collectAsState()
    val slideshowEnabled by viewModel.slideshowEnabled.collectAsState()
    val allAttachments by homeViewModel.allAttachments.collectAsState()
    
    // Show authentication dialog if needed
    var showAuthDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.calendar_sync_settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        PeaceIcon(
                            iconName = "arrow_back",
                            contentDescription = stringResource(R.string.cd_back),
                            iconManager = iconManager
                        )
                    }
                }
            )
        }
    ) { padding ->
        BackgroundWrapper(
            attachments = allAttachments,
            blurIntensity = blurIntensity,
            slideshowEnabled = slideshowEnabled,
            backgroundImageManager = backgroundImageManager,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Calendar Sync Toggle
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.enable_calendar_sync),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = stringResource(R.string.calendar_sync_description),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = calendarSyncEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled && !isAuthenticated) {
                                        showAuthDialog = true
                                    } else {
                                        viewModel.setCalendarSyncEnabled(enabled)
                                    }
                                }
                            )
                        }
                    }
                }
                
                // Authentication Status
                if (calendarSyncEnabled) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = if (isAuthenticated) {
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        } else {
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PeaceIcon(
                                iconName = if (isAuthenticated) "checkmark_circle" else "alert_circle",
                                contentDescription = "Authentication status",
                                iconManager = iconManager,
                                tint = if (isAuthenticated) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onErrorContainer
                                }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isAuthenticated) {
                                        "Authenticated"
                                    } else {
                                        stringResource(R.string.not_authenticated)
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (isAuthenticated) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onErrorContainer
                                    }
                                )
                            }
                            if (!isAuthenticated) {
                                TextButton(
                                    onClick = { showAuthDialog = true }
                                ) {
                                    Text(stringResource(R.string.sign_in))
                                }
                            }
                        }
                    }
                }
                
                // Manual Sync Button
                if (calendarSyncEnabled && isAuthenticated) {
                    Button(
                        onClick = { viewModel.syncNow() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSyncing
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.syncing))
                        } else {
                            PeaceIcon(
                                iconName = "sync",
                                contentDescription = "Sync",
                                iconManager = iconManager
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.sync_now))
                        }
                    }
                }
                
                // Sync Statistics
                if (calendarSyncEnabled && isAuthenticated) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.sync_statistics),
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            HorizontalDivider()
                            
                            // Last Sync Time
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.last_sync),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = lastSyncTime?.let { 
                                        formatSyncTime(it)
                                    } ?: stringResource(R.string.never_synced),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            // Synced Count
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.synced_reminders),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = stringResource(R.string.reminders_synced_format, syncedCount),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // Error Display
                syncError?.let { error ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PeaceIcon(
                                    iconName = "alert_circle",
                                    contentDescription = "Error",
                                    iconManager = iconManager,
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = stringResource(R.string.sync_error),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            TextButton(
                                onClick = { viewModel.clearSyncError() }
                            ) {
                                Text(stringResource(R.string.close))
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Authentication Dialog
    if (showAuthDialog) {
        AlertDialog(
            onDismissRequest = { showAuthDialog = false },
            icon = {
                PeaceIcon(
                    iconName = "logo_google",
                    contentDescription = "Google",
                    iconManager = iconManager
                )
            },
            title = {
                Text(stringResource(R.string.authentication_required))
            },
            text = {
                Text(stringResource(R.string.authentication_required_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAuthDialog = false
                        viewModel.requestGoogleAuthentication()
                    }
                ) {
                    Text(stringResource(R.string.sign_in))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAuthDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun formatSyncTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000} minutes ago"
        diff < 86400_000 -> "${diff / 3600_000} hours ago"
        else -> {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            dateFormat.format(Date(timestamp))
        }
    }
}
