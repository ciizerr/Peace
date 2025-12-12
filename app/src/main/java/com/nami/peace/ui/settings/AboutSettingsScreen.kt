package com.nami.peace.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nami.peace.BuildConfig
import com.nami.peace.R
import com.nami.peace.data.updater.UpdateState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun AboutSettingsScreen(
    onNavigateToHistory: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    viewModel: SettingsViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val updateStatus by viewModel.updateStatus.collectAsState()

    // Manage Dialog State based on updateStatus
    // We can also have local state if we want to dismiss it without resetting VM state,
    // but resetting VM state is cleaner for "done" actions.
    
    if (updateStatus is UpdateState.Available) {
        val info = (updateStatus as UpdateState.Available).updateInfo
        AlertDialog(
            onDismissRequest = { viewModel.resetUpdateState() },
            title = { Text(stringResource(R.string.update_available_title, info.version)) },
            text = { 
                Column {
                    Text(stringResource(R.string.update_available_message))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(info.releaseNotes, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.startUpdate(info.downloadUrl) }) {
                    Text(stringResource(R.string.update_now))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.resetUpdateState() }) {
                    Text(stringResource(R.string.later))
                }
            }
        )
    }

    if (updateStatus is UpdateState.Error) {
        AlertDialog(
            onDismissRequest = { viewModel.resetUpdateState() },
            title = { Text(stringResource(R.string.update_failed_title)) },
            text = { Text((updateStatus as UpdateState.Error).message) },
            confirmButton = {
                TextButton(onClick = { viewModel.resetUpdateState() }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
    
    if (updateStatus is UpdateState.UpToDate) {
         AlertDialog(
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = stringResource(R.string.version_format, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Check for Update Button
        if (updateStatus is UpdateState.Downloading) {
            val progress = (updateStatus as UpdateState.Downloading).progress
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LinearProgressIndicator(
                    progress = progress / 100f,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.downloading_format, progress))
            }
        } else if (updateStatus is UpdateState.Checking) {
             CircularProgressIndicator()
        } else {
            Button(
                onClick = { viewModel.checkForUpdates() },
                enabled = updateStatus is UpdateState.Idle // Prevent multiple clicks
            ) {
                Text(stringResource(R.string.check_for_updates))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        HorizontalDivider(modifier = Modifier.padding(horizontal = 32.dp))

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(onClick = onNavigateToHistory) {
            Text(stringResource(R.string.view_history_log))
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToDashboard) {
            Text(stringResource(R.string.settings_back_to_main))
        }
    }
}
