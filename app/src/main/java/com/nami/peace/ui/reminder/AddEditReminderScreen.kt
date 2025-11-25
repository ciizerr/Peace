package com.nami.peace.ui.reminder

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
                    onCheckedChange = { viewModel.onEvent(AddEditReminderEvent.NagModeToggled(it)) },
                    enabled = uiState.recurrenceType != RecurrenceType.DAILY
                )
            }
            if (uiState.recurrenceType == RecurrenceType.DAILY) {
                Text(
                    "Nag Mode is disabled for Daily reminders.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Nag Interval & Repetitions
            if (uiState.isNagModeEnabled) {
                Text("Nag Interval", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val intervals = listOf(
                        30 * 60 * 1000L to "30m",
                        60 * 60 * 1000L to "1h",
                        2 * 60 * 60 * 1000L to "2h"
                    )
                    intervals.forEach { (duration, label) ->
                        FilterChip(
                            selected = uiState.nagIntervalInMillis == duration,
                            onClick = { viewModel.onEvent(AddEditReminderEvent.NagIntervalChanged(duration)) },
                            label = { Text(label) }
                        )
                    }
                }

                if (uiState.nagIntervalInMillis != null) {
                    Text(
                        "Max Repetitions today: ${uiState.nagTotalRepetitions}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Repetitions stop at midnight.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
