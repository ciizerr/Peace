package com.nami.peace.ui.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nami.peace.R
import com.nami.peace.domain.model.Reminder
import com.nami.peace.ui.components.GlassyTopAppBar
import com.nami.peace.ui.home.UpcomingReminderCard
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars

@Composable
fun AlarmsListScreen(
    viewModel: AlarmsListViewModel = hiltViewModel(),
    hazeState: HazeState? = null,
    blurEnabled: Boolean = true,
    blurStrength: Float = 12f,
    blurTintAlpha: Float = 0.5f,
    shadowsEnabled: Boolean = true,
    shadowStyle: String = "Medium",
    onEditReminder: (Int) -> Unit = {},
    onAddReminder: () -> Unit = {},
    isFABVisible: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Dialog State
    var showNapDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var selectedNapLabel by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var selectedNapDuration by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(0) }

    if (showNapDialog) {
        NapCustomizationDialog(
            initialDuration = selectedNapDuration,
            label = selectedNapLabel,
            onDismiss = { showNapDialog = false },
            onConfirm = { duration ->
                viewModel.addQuickNap(duration, selectedNapLabel)
                showNapDialog = false
            }
        )
    }
    
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    // Selection Mode State
    var selectedIds by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(emptySet<Int>()) }
    val isSelectionMode = selectedIds.isNotEmpty()
    var showDeleteDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    // Toggle Selection Helper
    val toggleSelection: (Int) -> Unit = { id ->
        selectedIds = if (selectedIds.contains(id)) {
            selectedIds - id
        } else {
            selectedIds + id
        }
    }

    // Exit Selection Mode on Back Press
    androidx.activity.compose.BackHandler(enabled = isSelectionMode) {
        selectedIds = emptySet()
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_bulk_title)) },
            text = { Text(stringResource(R.string.delete_bulk_message, selectedIds.size)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val toDelete = uiState.activeAlarms.filter { selectedIds.contains(it.id) }
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
        modifier = Modifier,
        containerColor = Color.Transparent,
        floatingActionButton = {
            val fabContainerColor by androidx.compose.animation.animateColorAsState(
                if (isSelectionMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                label = "fab_color"
            )
            
            val fabIcon = if (isSelectionMode) Icons.Default.Delete else Icons.Default.Add
            val fabContentDescription = if (isSelectionMode) stringResource(R.string.cd_delete) else stringResource(R.string.cd_add_reminder)

            val fabContentColor by androidx.compose.animation.animateColorAsState(
                if (isSelectionMode) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary,
                label = "fab_content_color"
            )

            com.nami.peace.ui.components.GlassyFloatingActionButton(
                onClick = { 
                    if (isSelectionMode) {
                        showDeleteDialog = true
                    } else {
                        onAddReminder()
                    }
                },
                modifier = Modifier.padding(bottom = 100.dp),
                containerColor = fabContainerColor,
                contentColor = fabContentColor,
                icon = fabIcon,
                contentDescription = fabContentDescription,
                hazeState = hazeState,
                isVisible = isFABVisible,
                blurEnabled = blurEnabled,
                blurStrength = blurStrength,
                blurTintAlpha = blurTintAlpha
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
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
                    top = 100.dp + WindowInsets.statusBars.asPaddingValues().calculateTopPadding(), 
                    bottom = 100.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(), 
                    start = 16.dp, 
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 1. Next Alarm Hero
                item {
                    NextAlarmHero(reminder = uiState.nextAlarm)
                }
    
                // 2. Quick Rest
                item {
                    Column {
                        Text(
                            stringResource(R.string.nap_section_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        // Disable naps selection for now or allow user to select them?
                        // Quick Nap row creates new naps. It's not a list of items to select.
                        QuickNapRow(
                            onNap20 = { 
                                if (!viewModel.isNapActive("Power Nap")) {
                                    selectedNapLabel = "Power Nap"
                                    selectedNapDuration = 20
                                    showNapDialog = true
                                }
                            },
                            onNap90 = { 
                                if (!viewModel.isNapActive("Sleep Cycle")) {
                                    selectedNapLabel = "Sleep Cycle"
                                    selectedNapDuration = 90
                                    showNapDialog = true
                                }
                            }
                        )
                    }
                }
    
                // 3. List Header
                item {
                    Text(
                        stringResource(R.string.rhythms_list_header),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
    
                // 4. List Items
                items(uiState.activeAlarms, key = { it.id }) { reminder ->
                     UpcomingReminderCard(
                        reminder = reminder,
                        isNextUp = false,
                        isSelected = selectedIds.contains(reminder.id),
                        onToggle = { viewModel.toggleReminder(reminder) },
                        onLongClick = { toggleSelection(reminder.id) },
                        onClick = { 
                            if (isSelectionMode) toggleSelection(reminder.id) else onEditReminder(reminder.id)
                        }
                    )
                }
            }
            
            // Glassy Top Bar
            GlassyTopAppBar(
                title = { 
                    if (isSelectionMode) {
                        Text(
                            stringResource(R.string.selected_count, selectedIds.size),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    } else {
                        Text(
                            stringResource(R.string.rhythms_title), 
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                navigationIcon = {
                    if (isSelectionMode) {
                        IconButton(onClick = { selectedIds = emptySet() }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close_selection))
                        }
                    }
                },
                modifier = Modifier.align(Alignment.TopCenter),
                hazeState = hazeState,
                blurEnabled = blurEnabled,
                blurStrength = blurStrength,
                blurTintAlpha = blurTintAlpha,
                shadowsEnabled = shadowsEnabled,
                shadowStyle = shadowStyle
            )
        }
    }
}

@Composable
fun NextAlarmHero(reminder: Reminder?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                if (reminder != null) {
                    Text(
                        stringResource(R.string.rhythms_next_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha=0.7f)
                    )
                    Text(
                        formatTime(reminder.startTimeInMillis),
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    val timeDiff = reminder.startTimeInMillis - System.currentTimeMillis()
                    val relativeTime = formatRelativeTime(timeDiff)
                    
                    Text(
                        stringResource(R.string.rhythms_next_countdown, relativeTime),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha=0.8f)
                    )
                } else {
                    Text(
                        stringResource(R.string.rhythms_next_empty),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha=0.8f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun QuickNapRow(onNap20: () -> Unit, onNap90: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NapCard(
            title = stringResource(R.string.nap_20_min),
            icon = Icons.Default.Bolt,
            color = Color(0xFFFFD54F), // Amber/Yellowish
            onClick = onNap20,
            modifier = Modifier.weight(1f)
        )
        
        NapCard(
            title = stringResource(R.string.nap_90_min),
            icon = Icons.Default.Bedtime,
            color = Color(0xFF9575CD), // Deep Purple
            onClick = onNap90,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun NapCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f)),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun NapCustomizationDialog(
    initialDuration: Int,
    label: String,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var duration by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(initialDuration) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.nap_dialog_title, label)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.nap_duration_label), style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = { if (duration > 5) duration -= 5 }) {
                        Text("-", style = MaterialTheme.typography.headlineMedium)
                    }
                    Text(
                        "$duration", 
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    IconButton(onClick = { duration += 5 }) {
                        Text("+", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(duration) }) {
                Text(stringResource(R.string.start_nap))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

private fun formatTime(millis: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(millis))
}

private fun formatRelativeTime(diffMillis: Long): String {
    if (diffMillis < 0) return "now"
    
    val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis) % 60
    
    return if (hours > 0) {
        "${hours}h ${minutes}m"
    } else {
        "${minutes}m"
    }
}
