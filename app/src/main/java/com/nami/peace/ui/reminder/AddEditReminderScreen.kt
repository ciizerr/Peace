package com.nami.peace.ui.reminder

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.RecurrenceType
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditReminderScreen(
    onNavigateUp: () -> Unit,
    viewModel: AddEditReminderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Reminder") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.onEvent(AddEditReminderEvent.SaveReminder)
                        onNavigateUp()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.onEvent(AddEditReminderEvent.TitleChanged(it)) },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            // Priority
            Text("Priority", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PriorityLevel.values().forEach { priority ->
                    FilterChip(
                        selected = uiState.priority == priority,
                        onClick = { viewModel.onEvent(AddEditReminderEvent.PriorityChanged(priority)) },
                        label = { Text(priority.name) }
                    )
                }
            }

            // Start Time
            Text("Start Time", style = MaterialTheme.typography.titleMedium)
            Button(onClick = {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = uiState.startTimeInMillis
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        viewModel.onEvent(AddEditReminderEvent.StartTimeChanged(calendar.timeInMillis))
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()
            }) {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = uiState.startTimeInMillis
                Text(String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)))
            }

            // Recurrence
            Text("Recurrence", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RecurrenceType.values().forEach { type ->
                    FilterChip(
                        selected = uiState.recurrenceType == type,
                        onClick = { viewModel.onEvent(AddEditReminderEvent.RecurrenceChanged(type)) },
                        label = { Text(type.name) }
                    )
                }
            }

            // Nag Mode
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Nag Mode", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Switch(
                    checked = uiState.isNagModeEnabled,
                    onCheckedChange = { viewModel.onEvent(AddEditReminderEvent.NagModeToggled(it)) }
                )
            }

            // Soft Warning Dialog
            if (uiState.showSoftWarningDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.onEvent(AddEditReminderEvent.DismissWarningDialog) },
                    title = { Text("Limited Repetitions Warning") },
                    text = {
                        Text(
                            "You are scheduling a Daily sequence late at night.\n\n" +
                            "To prevent conflicts with tomorrow's schedule, 'Nag Mode' sequences are reset at midnight (11:59 PM).\n\n" +
                            "Result: You may not get as many repetitions as you expect before the day ends."
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { viewModel.onEvent(AddEditReminderEvent.ConfirmWarningDialog) }) {
                            Text("I Understand")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.onEvent(AddEditReminderEvent.DismissWarningDialog) }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Permission Banner
            // We need to check permission on resume or launch
            val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val hasPermission = alarmManager.canScheduleExactAlarms()
                LaunchedEffect(hasPermission) {
                    viewModel.onEvent(AddEditReminderEvent.PermissionStateChanged(hasPermission))
                }
            }

            if (uiState.showPermissionBanner) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Exact Alarm Permission Required",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            "To ensure your reminders fire on time, please grant the 'Alarms & Reminders' permission.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                    val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                    context.startActivity(intent)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
            }

            // Nag Interval & Repetitions
            if (uiState.isNagModeEnabled) {
                Text("Nag Interval", style = MaterialTheme.typography.titleMedium)
                
                // Interval Selector (Dropdown-like using exposed dropdown or just a list for now, let's use a better list)
                // User requested "Every 15 min", "Every 30 min", "Every 1 Hour"
                // Let's use a FlowRow or similar if possible, or just a scrollable Row
                // Interval Input Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.nagIntervalValue,
                        onValueChange = { 
                            // Only allow numeric input
                            if (it.all { char -> char.isDigit() }) {
                                viewModel.onEvent(AddEditReminderEvent.NagIntervalValueChanged(it))
                            }
                        },
                        label = { Text("Interval") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        singleLine = true
                    )
                    
                    // Unit Selector (Simple Toggle for now)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        TimeUnit.values().forEach { unit ->
                            FilterChip(
                                selected = uiState.nagIntervalUnit == unit,
                                onClick = { viewModel.onEvent(AddEditReminderEvent.NagIntervalUnitChanged(unit)) },
                                label = { Text(unit.name.lowercase().capitalize()) },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                }

                // Presets
                Text("Presets", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(
                        15 to TimeUnit.MINUTES,
                        30 to TimeUnit.MINUTES,
                        1 to TimeUnit.HOURS,
                        2 to TimeUnit.HOURS
                    )
                    presets.forEach { (valNum, unit) ->
                        FilterChip(
                            selected = uiState.nagIntervalValue == valNum.toString() && uiState.nagIntervalUnit == unit,
                            onClick = { 
                                viewModel.onEvent(AddEditReminderEvent.NagIntervalUnitChanged(unit))
                                viewModel.onEvent(AddEditReminderEvent.NagIntervalValueChanged(valNum.toString()))
                            },
                            label = { 
                                val unitLabel = if (unit == TimeUnit.MINUTES) "m" else "h"
                                Text("$valNum$unitLabel") 
                            }
                        )
                    }
                }

                if (uiState.nagIntervalInMillis != null) {
                    Text(
                        "Repetitions: ${uiState.nagTotalRepetitions}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    if (uiState.maxAllowedRepetitions > 0) {
                        Slider(
                            value = uiState.nagTotalRepetitions.toFloat(),
                            onValueChange = { viewModel.onEvent(AddEditReminderEvent.NagRepetitionsChanged(it.toInt())) },
                            valueRange = 0f..uiState.maxAllowedRepetitions.toFloat(),
                            steps = if (uiState.maxAllowedRepetitions > 1) uiState.maxAllowedRepetitions - 1 else 0
                        )
                        Text(
                            "Max allowed: ${uiState.maxAllowedRepetitions} (Stops at midnight)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    } else {
                        Text(
                            "No repetitions possible today (Too close to midnight)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
