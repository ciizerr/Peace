package com.nami.peace.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val schedulingMode by viewModel.schedulingMode.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Alarm Behavior Section
            Text(
                "Alarm Behavior",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Scheduling Mode", style = MaterialTheme.typography.titleSmall)
                
                // Flexible Option
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setSchedulingMode("FLEXIBLE") }
                        .padding(vertical = 8.dp)
                ) {
                    RadioButton(
                        selected = schedulingMode == "FLEXIBLE",
                        onClick = { viewModel.setSchedulingMode("FLEXIBLE") }
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text("Flexible (Drift)", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Next alarm is calculated from when you respond. Good for workouts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Strict Option
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setSchedulingMode("STRICT") }
                        .padding(vertical = 8.dp)
                ) {
                    RadioButton(
                        selected = schedulingMode == "STRICT",
                        onClick = { viewModel.setSchedulingMode("STRICT") }
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text("Strict (Anchored)", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Alarms lock to the original schedule (e.g. 6:00, 6:15). Good for meds.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider()

            // Data Section
            Text(
                "Data",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Button(
                onClick = onNavigateToHistory,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.History, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("View History Log")
            }
        }
    }
}
