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
import dev.chrisbanes.haze.haze
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable

import androidx.compose.ui.draw.clip
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
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
    
    // Selection Mode State
    var selectedIds by remember { mutableStateOf(emptySet<Int>()) }
    val isSelectionMode = selectedIds.isNotEmpty()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Toggle Selection Helper
    val toggleSelection: (Int) -> Unit = { id ->
        selectedIds = if (selectedIds.contains(id)) {
            selectedIds - id
        } else {
            selectedIds + id
        }
    }

    // Exit Selection Mode on Back Press
    BackHandler(enabled = isSelectionMode) {
        selectedIds = emptySet()
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_bulk_title)) },
            text = { Text(stringResource(R.string.delete_bulk_message, selectedIds.size)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val allReminders = (uiState.nextUp?.let { listOf(it) } ?: emptyList()) + uiState.sections.values.flatten()
                        val toDelete = allReminders.filter { selectedIds.contains(it.id) }
                        viewModel.deleteReminders(toDelete)
                        selectedIds = emptySet()
                        showDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

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
                    if (!isSelectionMode) {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.cd_settings))
                        }
                    }
                },
                hazeState = hazeState,
                blurEnabled = blurEnabled,
                blurStrength = blurStrength,
                blurTintAlpha = blurTintAlpha
            )
        },
        floatingActionButton = {
            val fabContainerColor by animateColorAsState(
                if (isSelectionMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            val fabContentColor by animateColorAsState(
                if (isSelectionMode) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
            )

            FloatingActionButton(
                onClick = { 
                    if (isSelectionMode) {
                        showDeleteDialog = true
                    } else {
                        onAddReminder()
                    }
                },
                modifier = Modifier.padding(bottom = bottomPadding),
                containerColor = fabContainerColor,
                contentColor = fabContentColor
            ) {
                AnimatedContent(
                    targetState = isSelectionMode,
                    label = "fab_icon"
                ) { selectionMode ->
                    if (selectionMode) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.cd_delete))
                    } else {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add_reminder))
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (hazeState != null) {
                        Modifier.haze(
                            state = hazeState,
                            style = dev.chrisbanes.haze.HazeStyle(
                                 tint = if (androidx.compose.foundation.isSystemInDarkTheme()) 
                                     com.nami.peace.ui.theme.GlassyBlack.copy(alpha = blurTintAlpha) 
                                 else 
                                     com.nami.peace.ui.theme.GlassyWhite.copy(alpha = blurTintAlpha),
                                 blurRadius = blurStrength.dp
                            )    
                        )
                    } else Modifier
                ),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 8.dp, // Add some extra top padding if needed, or just use scaffold padding
                bottom = padding.calculateBottomPadding() + 80.dp + bottomPadding, // Combine scaffold + manual bottom padding
                start = 16.dp,
                end = 16.dp
            ),
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
                        isSelected = selectedIds.contains(nextUp.id),
                        onLongClick = { toggleSelection(nextUp.id) },
                        onClick = { 
                            if (isSelectionMode) {
                                toggleSelection(nextUp.id)
                            } else {
                                onEditReminder(nextUp.id) 
                            }
                        }
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
                                isSelected = selectedIds.contains(reminder.id),
                                onToggle = { viewModel.toggleReminder(reminder) },
                                onLongClick = { toggleSelection(reminder.id) },
                                onClick = { 
                                    if (isSelectionMode) {
                                        toggleSelection(reminder.id)
                                    } else {
                                        onEditReminder(reminder.id) 
                                    }
                                }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HeroReminderCard(
    reminder: Reminder,
    isSelected: Boolean = false,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit
) {
    val gradientColors = if (isSelected) {
        listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), MaterialTheme.colorScheme.primary)
    } else {
        when (reminder.priority) {
            PriorityLevel.HIGH -> listOf(Color(0xFFEF5350), Color(0xFFB71C1C)) // Red
            PriorityLevel.MEDIUM -> listOf(Color(0xFF42A5F5), Color(0xFF1565C0)) // Blue
            PriorityLevel.LOW -> listOf(Color(0xFF66BB6A), Color(0xFF2E7D32)) // Green
        }
    }

    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val borderWidth = if (isSelected) 3.dp else 0.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(24.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = androidx.compose.foundation.BorderStroke(borderWidth, borderColor)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UpcomingReminderCard(
    reminder: Reminder,
    isNextUp: Boolean = false,
    isSelected: Boolean = false,
    onToggle: (Boolean) -> Unit,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit
) {
    val stripColor = when (reminder.priority) {
        PriorityLevel.HIGH -> Color(0xFFEF5350)
        PriorityLevel.MEDIUM -> Color(0xFF42A5F5)
        PriorityLevel.LOW -> Color(0xFF66BB6A)
    }

    val containerColor = if (isSelected) 
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) 
    else 
        MaterialTheme.colorScheme.surfaceVariant

    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val borderWidth = if (isSelected) 2.dp else 0.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardDefaults.shape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(borderWidth, borderColor)
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
