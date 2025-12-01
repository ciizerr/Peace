package com.nami.peace.ui.reminder

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import com.nami.peace.ui.components.PeaceIcon
import com.nami.peace.util.icon.IconManager
import com.nami.peace.util.icon.IoniconsManager
import androidx.compose.runtime.remember
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
import com.nami.peace.domain.model.ReminderCategory
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.res.stringResource
import com.nami.peace.R
import com.nami.peace.ui.components.AlarmSoundPickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditReminderScreen(
    onNavigateUp: () -> Unit,
    viewModel: AddEditReminderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val iconManager: IconManager = remember { IoniconsManager(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_reminder)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        PeaceIcon(
                            iconName = "arrow_back",
                            contentDescription = stringResource(R.string.cd_back),
                            iconManager = iconManager
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.onEvent(AddEditReminderEvent.SaveReminder)
                        onNavigateUp()
                    }) {
                        PeaceIcon(
                            iconName = "checkmark",
                            contentDescription = stringResource(R.string.cd_save),
                            iconManager = iconManager
                        )
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
                label = { Text(stringResource(R.string.title)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            // Category
            Text(stringResource(R.string.category), style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReminderCategory.values().forEach { category ->
                    val isSelected = uiState.category == category
                    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    val iconColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    
                    Surface(
                        onClick = { viewModel.onEvent(AddEditReminderEvent.CategoryChanged(category)) },
                        shape = MaterialTheme.shapes.medium,
                        color = backgroundColor,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(id = category.iconResId),
                                contentDescription = category.name,
                                tint = iconColor
                            )
                        }
                    }
                }
            }

            // Priority
            Text(stringResource(R.string.priority), style = MaterialTheme.typography.titleMedium)
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
            Text(stringResource(R.string.start_time), style = MaterialTheme.typography.titleMedium)
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
            Text(stringResource(R.string.recurrence), style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RecurrenceType.values().forEach { type ->
                    FilterChip(
                        selected = uiState.recurrenceType == type,
                        onClick = { viewModel.onEvent(AddEditReminderEvent.RecurrenceChanged(type)) },
                        label = { Text(type.name.replace("_", " ")) }
                    )
                }
            }

            // Date Picker (One Time)
            if (uiState.recurrenceType == RecurrenceType.ONE_TIME) {
                val dateText = if (uiState.dateInMillis != null) {
                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(uiState.dateInMillis!!))
                } else {
                    stringResource(R.string.select_date)
                }
                
                val calendar = Calendar.getInstance()
                
                val datePickerDialog = android.app.DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val selectedCalendar = Calendar.getInstance()
                        selectedCalendar.set(year, month, dayOfMonth)
                        viewModel.onEvent(AddEditReminderEvent.DateChanged(selectedCalendar.timeInMillis))
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )

                OutlinedButton(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(dateText)
                }
            }

            // Day Selector (Weekly)
            if (uiState.recurrenceType == RecurrenceType.WEEKLY) {
                Text(stringResource(R.string.repeat_on), style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val days = listOf("S", "M", "T", "W", "T", "F", "S")
                    days.forEachIndexed { index, dayLabel ->
                        val dayValue = index + 1 // Calendar.SUNDAY = 1
                        val isSelected = uiState.daysOfWeek.contains(dayValue)
                        
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onEvent(AddEditReminderEvent.DayToggled(dayValue)) },
                            label = { Text(dayLabel) },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            // Nag Mode
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.nag_mode), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = uiState.isNagModeEnabled,
                    onCheckedChange = { viewModel.onEvent(AddEditReminderEvent.NagModeToggled(it)) }
                )
            }

            if (uiState.isNagModeEnabled) {
                // Interval
                OutlinedTextField(
                    value = uiState.nagIntervalValue,
                    onValueChange = { 
                        if (it.all { char -> char.isDigit() }) {
                            viewModel.onEvent(AddEditReminderEvent.NagIntervalValueChanged(it))
                        }
                    },
                    label = { Text(stringResource(R.string.interval)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    singleLine = true
                )
                
                // Unit Selector
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

                // Presets
                Text(stringResource(R.string.presets), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
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
                        stringResource(R.string.repetitions_format, uiState.nagTotalRepetitions),
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
                            stringResource(R.string.max_allowed_format, uiState.maxAllowedRepetitions),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    } else {
                        Text(
                            stringResource(R.string.no_repetitions_possible),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Strict Mode (Radio Buttons)
                Text(stringResource(R.string.scheduling_mode), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = !uiState.isStrictSchedulingEnabled,
                        onClick = { viewModel.onEvent(AddEditReminderEvent.StrictModeToggled(false)) }
                    )
                    Text(stringResource(R.string.flexible_drift))
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = uiState.isStrictSchedulingEnabled,
                        onClick = { viewModel.onEvent(AddEditReminderEvent.StrictModeToggled(true)) }
                    )
                    Text(stringResource(R.string.strict_anchored))
                }
            }

            // Soft Warning Dialog
            if (uiState.showSoftWarningDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.onEvent(AddEditReminderEvent.DismissWarningDialog) },
                    title = { Text(stringResource(R.string.limited_repetitions_warning)) },
                    text = {
                        Text(stringResource(R.string.limited_repetitions_message))
                    },
                    confirmButton = {
                        TextButton(onClick = { viewModel.onEvent(AddEditReminderEvent.ConfirmWarningDialog) }) {
                            Text(stringResource(R.string.i_understand))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.onEvent(AddEditReminderEvent.DismissWarningDialog) }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            // Alarm Sound Selection
            Text(stringResource(R.string.alarm_sound), style = MaterialTheme.typography.titleMedium)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            val currentSound = uiState.selectedAlarmSound
                            Text(
                                text = currentSound?.name ?: stringResource(R.string.default_alarm),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (currentSound != null && !currentSound.isSystem) {
                                Text(
                                    text = stringResource(R.string.custom_sound),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Row {
                            // Test sound button
                            val currentSound = uiState.selectedAlarmSound
                            if (currentSound != null) {
                                IconButton(onClick = {
                                    viewModel.onEvent(AddEditReminderEvent.PlayAlarmSoundPreview(currentSound))
                                }) {
                                    PeaceIcon(
                                        iconName = "play-circle",
                                        contentDescription = stringResource(R.string.cd_test_sound),
                                        iconManager = iconManager
                                    )
                                }
                            }
                            
                            // Change sound button
                            IconButton(onClick = {
                                viewModel.onEvent(AddEditReminderEvent.ShowAlarmSoundPicker(true))
                            }) {
                                PeaceIcon(
                                    iconName = "musical-notes",
                                    contentDescription = stringResource(R.string.cd_change_alarm_sound),
                                    iconManager = iconManager
                                )
                            }
                        }
                    }
                }
            }

            // Permission Banner
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
                            stringResource(R.string.exact_alarm_permission_required),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            stringResource(R.string.exact_alarm_permission_message),
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
                            Text(stringResource(R.string.grant_permission))
                        }
                    }
                }
            }
        }
        
        // Alarm Sound Picker Dialog
        if (uiState.showAlarmSoundPicker) {
            AlarmSoundPickerDialog(
                currentSound = uiState.selectedAlarmSound,
                availableSounds = uiState.availableAlarmSounds,
                onSoundSelected = { sound ->
                    viewModel.onEvent(AddEditReminderEvent.AlarmSoundSelected(sound))
                    viewModel.onEvent(AddEditReminderEvent.ShowAlarmSoundPicker(false))
                },
                onDismiss = {
                    viewModel.onEvent(AddEditReminderEvent.StopAlarmSoundPreview)
                    viewModel.onEvent(AddEditReminderEvent.ShowAlarmSoundPicker(false))
                },
                onPlayPreview = { sound ->
                    viewModel.onEvent(AddEditReminderEvent.PlayAlarmSoundPreview(sound))
                },
                onStopPreview = {
                    viewModel.onEvent(AddEditReminderEvent.StopAlarmSoundPreview)
                },
                onPickCustomSound = { uri, name ->
                    viewModel.onEvent(AddEditReminderEvent.CustomAlarmSoundPicked(uri, name))
                    viewModel.onEvent(AddEditReminderEvent.ShowAlarmSoundPicker(false))
                },
                iconManager = iconManager as IoniconsManager
            )
        }
    }
}
