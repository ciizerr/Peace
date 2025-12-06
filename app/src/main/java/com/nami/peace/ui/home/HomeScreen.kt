package com.nami.peace.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.Reminder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.res.stringResource
import com.nami.peace.R

import com.nami.peace.ui.components.GlassyTopAppBar
import dev.chrisbanes.haze.HazeState

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onAddReminder: () -> Unit,
    onEditReminder: (Int) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    viewModel: HomeViewModel = hiltViewModel(),
    hazeState: HazeState? = null,
    blurEnabled: Boolean = true,
    blurStrength: Float = 12f,
    blurTintAlpha: Float = 0.5f,
) {
    val uiState by viewModel.uiState.collectAsState()
    val nextUp = uiState.nextUp
    val sections = uiState.sections

    Scaffold(
        topBar = {
            GlassyTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.my_schedule), 
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.cd_settings))
                    }
                },
                hazeState = hazeState,
                blurEnabled = blurEnabled,
                blurStrength = blurStrength,
                blurTintAlpha = blurTintAlpha
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddReminder,
                modifier = Modifier.padding(bottom = bottomPadding),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add_reminder))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Next Up (Hero Card)
            if (nextUp != null) {
                item {
                    Text(
                        stringResource(R.string.next_up), 
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                    )
                    HeroReminderCard(
                        reminder = nextUp,
                        onClick = { onEditReminder(nextUp.id) }
                    )
                }
            }

            // Section 2: Grouped Lists
            sections.forEach { (header, reminders) ->
                stickyHeader {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Text(
                            header,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                items(reminders, key = { it.id }) { reminder ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.deleteReminder(reminder)
                                true
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val color = Color(0xFFE57373) // Soft Red
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.cd_delete),
                                    tint = Color.White
                                )
                            }
                        },
                        content = {
                            UpcomingReminderCard(
                                reminder = reminder,
                                isNextUp = (reminder.id == nextUp?.id),
                                onToggle = { viewModel.toggleReminder(reminder) },
                                onClick = { onEditReminder(reminder.id) }
                            )
                        },
                        enableDismissFromStartToEnd = false
                    )
                }
            }
            
            if (nextUp == null && sections.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.no_reminders_message),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
            
            // Add some bottom padding for FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun HeroReminderCard(
    reminder: Reminder,
    onClick: () -> Unit
) {
    val gradientColors = when (reminder.priority) {
        PriorityLevel.HIGH -> listOf(Color(0xFFEF5350), Color(0xFFB71C1C)) // Red
        PriorityLevel.MEDIUM -> listOf(Color(0xFF42A5F5), Color(0xFF1565C0)) // Blue
        PriorityLevel.LOW -> listOf(Color(0xFF66BB6A), Color(0xFF2E7D32)) // Green
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(gradientColors))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                Text(
                    formatTime(reminder.startTimeInMillis),
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    reminder.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
                if (reminder.isNagModeEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            stringResource(R.string.repetition_format, reminder.currentRepetitionIndex + 1, reminder.nagTotalRepetitions),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }
                }
            }
            
            if (reminder.isInNestedSnoozeLoop) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = stringResource(R.string.cd_snoozed),
                    tint = Color.White,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }
    }
}

@Composable
fun UpcomingReminderCard(
    reminder: Reminder,
    isNextUp: Boolean = false,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    val stripColor = when (reminder.priority) {
        PriorityLevel.HIGH -> Color(0xFFEF5350)
        PriorityLevel.MEDIUM -> Color(0xFF42A5F5)
        PriorityLevel.LOW -> Color(0xFF66BB6A)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Priority Strip
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(stripColor)
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                if (isNextUp) {
                    Text(
                        stringResource(R.string.next_reminder),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Text(
                    reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    formatTime(reminder.startTimeInMillis),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (reminder.isInNestedSnoozeLoop) {
                    Text(
                        stringResource(R.string.snoozed_nag_mode),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Switch(
                checked = reminder.isEnabled,
                onCheckedChange = { onToggle(it) },
                modifier = Modifier.padding(end = 16.dp)
            )
        }
    }
}

private fun formatTime(millis: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(millis))
}
