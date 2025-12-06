package com.nami.peace.ui.reminder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nami.peace.domain.model.PriorityLevel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDetailScreen(
    onNavigateUp: () -> Unit,
    onEditReminder: (Int) -> Unit,
    viewModel: ReminderDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(androidx.compose.ui.res.stringResource(com.nami.peace.R.string.reminder_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = androidx.compose.ui.res.stringResource(com.nami.peace.R.string.back))
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.reminder != null) {
                FloatingActionButton(onClick = { onEditReminder(uiState.reminder!!.id) }) {
                    Icon(Icons.Default.Edit, contentDescription = androidx.compose.ui.res.stringResource(com.nami.peace.R.string.reminder_action_edit))
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                val reminder = uiState.reminder
                if (reminder != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header Box
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = getPriorityColor(reminder.priority)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text(
                                    text = reminder.title,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(id = reminder.category.iconResId),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = reminder.category.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        // Info Cards
                        DetailCard(
                            label = androidx.compose.ui.res.stringResource(com.nami.peace.R.string.reminder_label_original_time),
                            value = formatTime(reminder.originalStartTimeInMillis)
                        )

                        DetailCard(
                            label = androidx.compose.ui.res.stringResource(com.nami.peace.R.string.reminder_label_recurrence),
                            value = when (reminder.recurrenceType) {
                                com.nami.peace.domain.model.RecurrenceType.ONE_TIME -> {
                                    if (reminder.dateInMillis != null) {
                                        androidx.compose.ui.res.stringResource(com.nami.peace.R.string.reminder_recurrence_one_time) + ": " + SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(reminder.dateInMillis))
                                    } else {
                                        androidx.compose.ui.res.stringResource(com.nami.peace.R.string.reminder_recurrence_one_time)
                                    }
                                }
                                com.nami.peace.domain.model.RecurrenceType.WEEKLY -> {
                                    val sortedDays = reminder.daysOfWeek.sorted()
                                    val dayStrings = mutableListOf<String>()
                                    for (day in sortedDays) {
                                        dayStrings.add(
                                            when (day) {
                                                1 -> androidx.compose.ui.res.stringResource(com.nami.peace.R.string.day_sunday_short)
                                                2 -> androidx.compose.ui.res.stringResource(com.nami.peace.R.string.day_monday_short)
                                                3 -> androidx.compose.ui.res.stringResource(com.nami.peace.R.string.day_tuesday_short)
                                                4 -> androidx.compose.ui.res.stringResource(com.nami.peace.R.string.day_wednesday_short)
                                                5 -> androidx.compose.ui.res.stringResource(com.nami.peace.R.string.day_thursday_short)
                                                6 -> androidx.compose.ui.res.stringResource(com.nami.peace.R.string.day_friday_short)
                                                7 -> androidx.compose.ui.res.stringResource(com.nami.peace.R.string.day_saturday_short)
                                                else -> ""
                                            }
                                        )
                                    }
                                    val days = dayStrings.joinToString(", ")
                                    androidx.compose.ui.res.stringResource(com.nami.peace.R.string.reminder_recurrence_weekly_prefix) + " $days"
                                }
                                else -> reminder.recurrenceType.name
                            }
                        )

                        DetailCard(
                            label = androidx.compose.ui.res.stringResource(com.nami.peace.R.string.reminder_label_schedule_mode),
                            value = if (reminder.isStrictSchedulingEnabled) androidx.compose.ui.res.stringResource(com.nami.peace.R.string.strict_anchored) else androidx.compose.ui.res.stringResource(com.nami.peace.R.string.flexible_drift)
                        )

                        DetailCard(
                            label = androidx.compose.ui.res.stringResource(com.nami.peace.R.string.reminder_label_nag_mode),
                            value = if (reminder.isNagModeEnabled) androidx.compose.ui.res.stringResource(com.nami.peace.R.string.reminder_status_enabled) else androidx.compose.ui.res.stringResource(com.nami.peace.R.string.reminder_status_disabled)
                        )

                        if (reminder.isNagModeEnabled) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(com.nami.peace.R.string.reminder_label_sequence),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    val interval = reminder.nagIntervalInMillis ?: (15 * 60 * 1000L)
                                    
                                    for (i in 0 until reminder.nagTotalRepetitions) {
                                        val repTime = reminder.originalStartTimeInMillis + (i * interval)
                                        val status = when {
                                            i < reminder.currentRepetitionIndex -> androidx.compose.ui.res.stringResource(com.nami.peace.R.string.done)
                                            i == reminder.currentRepetitionIndex -> androidx.compose.ui.res.stringResource(com.nami.peace.R.string.reminder_status_next)
                                            else -> androidx.compose.ui.res.stringResource(com.nami.peace.R.string.reminder_status_upcoming)
                                        }
                                        
                                        val color = when (status) {
                                            androidx.compose.ui.res.stringResource(com.nami.peace.R.string.done) -> Color.Gray
                                            androidx.compose.ui.res.stringResource(com.nami.peace.R.string.reminder_status_next) -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                        
                                        val fontWeight = if (status == androidx.compose.ui.res.stringResource(com.nami.peace.R.string.reminder_status_next)) FontWeight.Bold else FontWeight.Normal
                                        
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${androidx.compose.ui.res.stringResource(com.nami.peace.R.string.reminder_rep_prefix)} ${i + 1}: ${formatTime(repTime)}",
                                                color = color,
                                                fontWeight = fontWeight
                                            )
                                            Text(
                                                text = status,
                                                color = color,
                                                fontWeight = fontWeight,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        if (i < reminder.nagTotalRepetitions - 1) {
                                            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text(androidx.compose.ui.res.stringResource(com.nami.peace.R.string.reminder_not_found), modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun DetailCard(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun getPriorityColor(priority: PriorityLevel): Color {
    return when (priority) {
        PriorityLevel.HIGH -> Color(0xFFFFCDD2) // Red-ish
        PriorityLevel.MEDIUM -> Color(0xFFFFF9C4) // Yellow-ish
        PriorityLevel.LOW -> Color(0xFFC8E6C9) // Green-ish
    }
}

fun formatTime(timeInMillis: Long): String {
    val calendar = Calendar.getInstance().apply { this.timeInMillis = timeInMillis }
    return String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
}
