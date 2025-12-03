package com.nami.peace.ui.reminder

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.RecurrenceType
import com.nami.peace.ui.theme.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditReminderScreen(
    onNavigateUp: () -> Unit,
    viewModel: AddEditReminderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Check permission status
    val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        val hasPermission = alarmManager.canScheduleExactAlarms()
        LaunchedEffect(hasPermission) {
            viewModel.onEvent(AddEditReminderEvent.PermissionStateChanged(hasPermission))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GradientLightStart,
                        GradientLightEnd
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            IconButton(
                                onClick = onNavigateUp,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Text(
                                if (uiState.id != 0) "Edit Reminder" else "New Reminder",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.W600
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        FilledTonalButton(
                            onClick = {
                                viewModel.onEvent(AddEditReminderEvent.SaveReminder)
                                onNavigateUp()
                            },
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = SerenityBlue,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Save",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save")
                        }
                    }
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Title Input
                item {
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = { viewModel.onEvent(AddEditReminderEvent.TitleChanged(it)) },
                        label = { Text("What do you want to remember?") },
                        placeholder = { Text("e.g., Morning meditation") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SerenityBlue,
                            focusedLabelColor = SerenityBlue,
                            cursorColor = SerenityBlue
                        )
                    )
                }

                // Priority Section
                item {
                    PremiumSectionCard(
                        icon = "🎯",
                        title = "Priority"
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            PriorityLevel.values().forEach { priority ->
                                val isSelected = uiState.priority == priority
                                val priorityColor = when (priority) {
                                    PriorityLevel.HIGH -> PriorityHigh
                                    PriorityLevel.MEDIUM -> PriorityMedium
                                    PriorityLevel.LOW -> PriorityLow
                                }
                                
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.onEvent(AddEditReminderEvent.PriorityChanged(priority)) },
                                    label = { Text(priority.name.lowercase().capitalize()) },
                                    leadingIcon = if (isSelected) {
                                        {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(priorityColor)
                                            )
                                        }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = priorityColor.copy(alpha = 0.2f),
                                        selectedLabelColor = priorityColor
                                    ),
                                    shape = MaterialTheme.shapes.extraSmall
                                )
                            }
                        }
                    }
                }

                // Time Section
                item {
                    PremiumSectionCard(
                        icon = "⏰",
                        title = "When?"
                    ) {
                        FilledTonalButton(
                            onClick = {
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
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = SerenityBlue.copy(alpha = 0.12f),
                                contentColor = SerenityBlue
                            )
                        ) {
                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = uiState.startTimeInMillis
                            Text(
                                String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)),
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.W300
                                )
                            )
                        }
                    }
                }

                // Recurrence Section
                item {
                    PremiumSectionCard(
                        icon = "🔁",
                        title = "Repeat"
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            RecurrenceType.values().forEach { type ->
                                FilterChip(
                                    selected = uiState.recurrenceType == type,
                                    onClick = { viewModel.onEvent(AddEditReminderEvent.RecurrenceChanged(type)) },
                                    label = { Text(type.name.replace("_", " ").lowercase().capitalize()) },
                                    shape = MaterialTheme.shapes.extraSmall
                                )
                            }
                        }
                    }
                }

                // Nag Mode Section
                item {
                    PremiumSectionCard(
                        icon = "🔔",
                        title = "Nag Mode"
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Enable Nag Mode",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.W500
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "Gentle repetitions until complete",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = uiState.isNagModeEnabled,
                                    onCheckedChange = { viewModel.onEvent(AddEditReminderEvent.NagModeToggled(it)) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = SerenityBlue,
                                        checkedTrackColor = SerenityBlue.copy(alpha = 0.5f)
                                    )
                                )
                            }

                            if (uiState.isNagModeEnabled) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                
                                // Interval Configuration
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        "Interval",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = uiState.nagIntervalValue,
                                            onValueChange = { 
                                                if (it.all { char -> char.isDigit() }) {
                                                    viewModel.onEvent(AddEditReminderEvent.NagIntervalValueChanged(it))
                                                }
                                            },
                                            label = { Text("Every") },
                                            modifier = Modifier.weight(1f),
                                            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                            singleLine = true,
                                            shape = MaterialTheme.shapes.small
                                        )
                                        
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            TimeUnit.values().forEach { unit ->
                                                FilterChip(
                                                    selected = uiState.nagIntervalUnit == unit,
                                                    onClick = { viewModel.onEvent(AddEditReminderEvent.NagIntervalUnitChanged(unit)) },
                                                    label = { 
                                                        Text(
                                                            if (unit == TimeUnit.MINUTES) "min" else "hr",
                                                            style = MaterialTheme.typography.labelMedium
                                                        ) 
                                                    },
                                                    modifier = Modifier.weight(1f),
                                                    shape = MaterialTheme.shapes.extraSmall
                                                )
                                            }
                                        }
                                    }

                                    // Presets
                                    Text(
                                        "Quick presets",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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
                                                },
                                                shape = MaterialTheme.shapes.extraSmall
                                            )
                                        }
                                    }

                                    // Repetitions Slider
                                    if (uiState.nagIntervalInMillis != null) {
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                        
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    "Repetitions",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    "${uiState.nagTotalRepetitions}",
                                                    style = MaterialTheme.typography.titleSmall.copy(
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = SerenityBlue
                                                )
                                            }
                                            
                                            if (uiState.maxAllowedRepetitions > 0) {
                                                Slider(
                                                    value = uiState.nagTotalRepetitions.toFloat(),
                                                    onValueChange = { viewModel.onEvent(AddEditReminderEvent.NagRepetitionsChanged(it.toInt())) },
                                                    valueRange = 0f..uiState.maxAllowedRepetitions.toFloat(),
                                                    steps = if (uiState.maxAllowedRepetitions > 1) uiState.maxAllowedRepetitions - 1 else 0,
                                                    colors = SliderDefaults.colors(
                                                        thumbColor = SerenityBlue,
                                                        activeTrackColor = SerenityBlue
                                                    )
                                                )
                                                Text(
                                                    "Max ${uiState.maxAllowedRepetitions} (resets at midnight)",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            } else {
                                                Text(
                                                    "⚠️ Too close to midnight for repetitions",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Permission Banner
                if (uiState.showPermissionBanner) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "⚠️ Permission Required",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.W600
                                    ),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    "To ensure your reminders fire on time, please grant the 'Alarms & Reminders' permission.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Button(
                                    onClick = {
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                            val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                            context.startActivity(intent)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    ),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text("Grant Permission")
                                }
                            }
                        }
                    }
                }

                // Warning Dialog
                if (uiState.showSoftWarningDialog) {
                    item {
                        AlertDialog(
                            onDismissRequest = { viewModel.onEvent(AddEditReminderEvent.DismissWarningDialog) },
                            title = { 
                                Text(
                                    "Limited Repetitions",
                                    style = MaterialTheme.typography.titleLarge
                                ) 
                            },
                            text = {
                                Text(
                                    "You are scheduling a Daily sequence late at night.\n\n" +
                                    "To prevent conflicts with tomorrow's schedule, 'Nag Mode' sequences are reset at midnight (11:59 PM).\n\n" +
                                    "Result: You may not get as many repetitions as you expect before the day ends.",
                                    style = MaterialTheme.typography.bodyMedium
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
                            },
                            shape = MaterialTheme.shapes.large
                        )
                    }
                }
                
                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun PremiumSectionCard(
    icon: String,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = MaterialTheme.shapes.large,
                ambientColor = ShadowLight,
                spotColor = ShadowLight
            ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    icon,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.W600
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            content()
        }
    }
}
