package com.nami.peace.ui.reminder

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nami.peace.R
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.Reminder
import com.nami.peace.ui.components.GlassyFloatingActionButton
import com.nami.peace.ui.components.GlassyTopAppBar
import com.nami.peace.ui.settings.SettingsViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDetailScreen(
    onNavigateUp: () -> Unit,
    onEditReminder: (Int) -> Unit,
    viewModel: ReminderDetailViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Global Settings
    val blurEnabled by settingsViewModel.blurEnabled.collectAsState()
    val blurStrength by settingsViewModel.blurStrength.collectAsState()
    val blurTintAlpha by settingsViewModel.blurTintAlpha.collectAsState()
    val shadowsEnabled by settingsViewModel.shadowsEnabled.collectAsState()
    val shadowStyleString by settingsViewModel.shadowStyle.collectAsState()
    
    val hazeState = remember { HazeState() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            GlassyTopAppBar(
                title = { Text(stringResource(R.string.reminder_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                // Suggestion: Remove edit button from topappbar (Removed actions block)
                hazeState = hazeState,
                blurEnabled = blurEnabled,
                blurStrength = blurStrength,
                blurTintAlpha = blurTintAlpha,
                shadowsEnabled = shadowsEnabled,
                shadowStyle = shadowStyleString
            )
        },
        floatingActionButton = {
           val reminder = uiState.reminder
           if (reminder != null) {
               GlassyFloatingActionButton(
                   onClick = { onEditReminder(reminder.id) },
                   icon = Icons.Default.Edit,
                   contentDescription = stringResource(R.string.reminder_action_edit),
                   hazeState = hazeState,
                   containerColor = MaterialTheme.colorScheme.primaryContainer,
                   contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                   blurEnabled = blurEnabled,
                   blurStrength = blurStrength,
                   blurTintAlpha = blurTintAlpha
               )
           }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.reminder == null) {
                 EmptyState(
                     message = stringResource(R.string.reminder_not_found),
                     modifier = Modifier.align(Alignment.Center)
                 )
            } else {
                val reminder = uiState.reminder!!
                
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = paddingValues.calculateTopPadding() + 8.dp,
                        bottom = 120.dp,
                        start = 16.dp, 
                        end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .haze(
                            state = hazeState,
                            style = HazeStyle(blurRadius = if (blurEnabled) blurStrength.dp else 0.dp, tint = Color.Transparent)
                        )
                ) {
                    // Changed from stickyHeader to item to fix overlapping/behind issues
                    item {
                        HeaderSummaryCard(
                            reminder = reminder,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Original Time
                    item {
                        DetailCard(
                            label = stringResource(R.string.reminder_label_original_time),
                            value = formatTime(context, reminder.originalStartTimeInMillis)
                        )
                    }

                    // Recurrence
                    item {
                        val recurrenceValue = when (reminder.recurrenceType) {
                            com.nami.peace.domain.model.RecurrenceType.ONE_TIME -> {
                                if (reminder.dateInMillis != null) {
                                    stringResource(R.string.reminder_recurrence_one_time) + ": " + SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(reminder.dateInMillis))
                                } else {
                                    stringResource(R.string.reminder_recurrence_one_time)
                                }
                            }
                            com.nami.peace.domain.model.RecurrenceType.WEEKLY -> {
                                val sortedDays = reminder.daysOfWeek.sorted()
                                val dayStrings = mutableListOf<String>()
                                for (day in sortedDays) {
                                    dayStrings.add(
                                        when (day) {
                                            1 -> stringResource(R.string.day_sunday_short)
                                            2 -> stringResource(R.string.day_monday_short)
                                            3 -> stringResource(R.string.day_tuesday_short)
                                            4 -> stringResource(R.string.day_wednesday_short)
                                            5 -> stringResource(R.string.day_thursday_short)
                                            6 -> stringResource(R.string.day_friday_short)
                                            7 -> stringResource(R.string.day_saturday_short)
                                            else -> ""
                                        }
                                    )
                                }
                                val days = dayStrings.joinToString(", ")
                                stringResource(R.string.reminder_recurrence_weekly_prefix) + " $days"
                            }
                            else -> reminder.recurrenceType.name
                        }
                        
                        DetailCard(
                            label = stringResource(R.string.reminder_label_recurrence),
                            value = recurrenceValue
                        )
                    }

                    // Schedule Mode
                    item {
                        DetailCard(
                            label = stringResource(R.string.reminder_label_schedule_mode),
                            value = if (reminder.isStrictSchedulingEnabled) stringResource(R.string.strict_anchored) else stringResource(R.string.flexible_drift)
                        )
                    }

                    // Nag Mode Summary
                    item {
                         DetailCard(
                            label = stringResource(R.string.reminder_label_nag_mode),
                            value = if (reminder.isNagModeEnabled) stringResource(R.string.reminder_status_enabled) else stringResource(R.string.reminder_status_disabled)
                        )
                    }

                    // Nag Sequence Section
                    if (reminder.isNagModeEnabled) {
                        item {
                            NagSequenceSection(reminder = reminder, context = context)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSummaryCard(
    reminder: Reminder,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    // Removed hazeChild because it was causing content to be unreadable.
    // Using simple semi-transparent background.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)) 
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
    ) {
         Row(
             modifier = Modifier.padding(16.dp),
             verticalAlignment = Alignment.CenterVertically
         ) {
             Column(modifier = Modifier.weight(1f)) {
                 Text(
                     text = reminder.title,
                     style = MaterialTheme.typography.headlineSmall,
                     fontWeight = FontWeight.SemiBold,
                     maxLines = 2,
                     overflow = TextOverflow.Ellipsis,
                     color = MaterialTheme.colorScheme.onSurface
                 )
                 
                 Spacer(modifier = Modifier.height(8.dp))
                 
                 // Updated Next Occurrence Logic
                 val nextTimeText = if (reminder.isNagModeEnabled) {
                     val interval = reminder.nagIntervalInMillis ?: 0L
                     val nextTime = reminder.originalStartTimeInMillis + (reminder.currentRepetitionIndex * interval)
                     formatTime(context, nextTime)
                 } else {
                     formatTime(context, reminder.startTimeInMillis)
                 }

                 Row(verticalAlignment = Alignment.CenterVertically) {
                     Text(
                         text = "Next: $nextTimeText", 
                         style = MaterialTheme.typography.bodySmall,
                         color = MaterialTheme.colorScheme.primary
                     )
                 }
             }
             
             Spacer(modifier = Modifier.width(16.dp))
             
             // Dynamic Icon Cluster
             Column(horizontalAlignment = Alignment.CenterHorizontally) {
                 Surface(
                     shape = CircleShape,
                     color = MaterialTheme.colorScheme.surfaceVariant,
                     modifier = Modifier.size(40.dp)
                 ) {
                     Box(contentAlignment = Alignment.Center) {
                         Icon(
                             painter = painterResource(id = reminder.category.iconResId),
                             contentDescription = reminder.category.name,
                             tint = MaterialTheme.colorScheme.primary,
                             modifier = Modifier.size(20.dp)
                         )
                     }
                 }
                 Spacer(modifier = Modifier.height(8.dp))
                 
                 val priorityColor = getPriorityColor(reminder.priority)
                 Box(
                     modifier = Modifier
                         .size(12.dp)
                         .background(priorityColor, CircleShape)
                         .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape)
                 )
             }
         }
    }
}

@Composable
fun DetailCard(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
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
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun NagSequenceSection(reminder: Reminder, context: android.content.Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stringResource(R.string.reminder_label_sequence),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            val interval = reminder.nagIntervalInMillis ?: (15 * 60 * 1000L)
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) { 
                for (i in 0 until reminder.nagTotalRepetitions) {
                    val repTime = reminder.originalStartTimeInMillis + (i * interval)
                    
                    val isDone = i < reminder.currentRepetitionIndex
                    val isNext = i == reminder.currentRepetitionIndex
                    
                    val statusText = when {
                        isDone -> stringResource(R.string.done)
                        isNext -> stringResource(R.string.reminder_status_next)
                        else -> stringResource(R.string.reminder_status_upcoming)
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isNext) MaterialTheme.colorScheme.primary else if (isDone) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${i + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isNext) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = formatTime(context, repTime),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                            color = if (isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    
                    if (i < reminder.nagTotalRepetitions - 1) {
                        Divider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), 
                            modifier = Modifier.padding(start = 36.dp, top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

fun getPriorityColor(priority: PriorityLevel): Color {
    return when (priority) {
        PriorityLevel.HIGH -> Color(0xFFFFCDD2) 
        PriorityLevel.MEDIUM -> Color(0xFFFFF9C4) 
        PriorityLevel.LOW -> Color(0xFFC8E6C9) 
    }
}

fun formatTime(context: android.content.Context, timeInMillis: Long): String {
    return android.text.format.DateFormat.getTimeFormat(context).format(Date(timeInMillis))
}
