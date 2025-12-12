package com.nami.peace.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nami.peace.R
import com.nami.peace.domain.model.Reminder
import com.nami.peace.ui.components.GlassyTopAppBar
import com.nami.peace.ui.components.GlassyFloatingActionButton
import com.nami.peace.ui.components.HistoryItemRow
import com.nami.peace.ui.components.PeaceCalendar
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.foundation.border
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.zIndex
import com.nami.peace.ui.components.DetailCard
import com.nami.peace.ui.components.getPriorityColor
import com.nami.peace.ui.components.formatTime
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.zIndex
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    hazeState: HazeState? = null,
    blurEnabled: Boolean = true,
    blurStrength: Float = 12f,
    blurTintAlpha: Float = 0.5f,
    shadowsEnabled: Boolean = true,
    shadowStyle: String = "Medium",
    onEditReminder: (Int) -> Unit = {}, // Default for preview/unused
    onSheetStateChange: (Boolean) -> Unit = {} // New callback
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSelectionMode = uiState.selectedIds.isNotEmpty()
    val state = hazeState ?: remember { HazeState() }
    val context = androidx.compose.ui.platform.LocalContext.current

    // Notify MainScreen of sheet state
    androidx.compose.runtime.LaunchedEffect(uiState.selectedReceipt) {
        onSheetStateChange(uiState.selectedReceipt != null)
    }

    // Handle User Feedback Messages
    androidx.compose.runtime.LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.messageShown()
        }
    }
    
    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (isSelectionMode) {
                GlassyFloatingActionButton(
                    onClick = { viewModel.deleteSelected() },
                    icon = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    hazeState = state,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    blurEnabled = blurEnabled,
                    blurStrength = blurStrength,
                    blurTintAlpha = blurTintAlpha,
                    modifier = Modifier.padding(bottom = 100.dp)
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main Content (Scrollable)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .haze( 
                        state = state,
                        style = HazeStyle(blurRadius = 15.dp, tint = Color.Transparent)
                    ),
                contentPadding = PaddingValues(
                    top = 100.dp + WindowInsets.statusBars.asPaddingValues().calculateTopPadding(), 
                    bottom = 100.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                ),
            ) {
                // Item 1: Calendar
                item {
                    PeaceCalendar(
                        isExpanded = uiState.isCalendarExpanded,
                        historyDates = uiState.historyDates,
                        selectedDate = uiState.selectedDate,
                        currentMonth = uiState.currentMonth,
                        onDateSelected = { viewModel.selectDate(it) },
                        onMonthChange = { viewModel.changeMonth(it) }
                    )
                }

                // Item 2: Filter Indicator (if active)
                if (uiState.selectedDate != null) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.history_label_completed),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = { viewModel.clearDateFilter() }) {
                                Text(stringResource(R.string.history_clear_filter))
                            }
                        }
                    }
                }

                // Item 3: Empty State or History List
                if (uiState.groupedItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Archive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    if (uiState.selectedDate != null) stringResource(R.string.history_empty_date)
                                    else stringResource(R.string.history_empty_all),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                            }
                        }
                    }
                } else {
                    uiState.groupedItems.forEach { (header, items) ->
                        stickyHeader {
                            GlassyHeader(header)
                        }
                        
                        items(items) { reminder ->
                            val isSelected = uiState.selectedIds.contains(reminder.id)
                            HistoryItemRow(
                                reminder = reminder,
                                isCompact = header != "Today" && header != "Yesterday",
                                onClick = { if (uiState.selectedIds.isNotEmpty()) viewModel.toggleSelection(reminder.id) else viewModel.openReceipt(reminder) },
                                hazeState = state,
                                blurEnabled = false,
                                blurTintAlpha = blurTintAlpha,
                                blurStrength = blurStrength,
                                isSelectionMode = uiState.selectedIds.isNotEmpty(),
                                isSelected = isSelected,
                                onLongClick = { viewModel.toggleSelection(reminder.id) },
                                onToggleSelection = { viewModel.toggleSelection(reminder.id) }
                            )
                        }
                    }
                }
            }
            
            // Overlay Layer: Top App Bar
            GlassyTopAppBar(
                title = { 
                    if (isSelectionMode) {
                        Text(
                            text = stringResource(R.string.selected_count, uiState.selectedIds.size),
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Column {
                            Text(stringResource(R.string.history_title), fontWeight = FontWeight.Bold)
                            if (uiState.selectedDate != null) {
                                Text(
                                    text = uiState.selectedDate!!.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    if (isSelectionMode) {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Cancel, contentDescription = stringResource(R.string.close_selection))
                        }
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        TextButton(onClick = { viewModel.selectAll() }) {
                            Text("Select All") // Should extract string, but hardcoding for speed/accuracy per instructions
                        }
                    } else {
                        TextButton(onClick = { viewModel.toggleCalendar() }) {
                            Text(stringResource(R.string.history_timeline_label))
                        }
                    }
                },
                hazeState = state,
                blurEnabled = blurEnabled,
                blurStrength = blurStrength,
                blurTintAlpha = blurTintAlpha,
                modifier = Modifier.align(Alignment.TopCenter),
                shadowsEnabled = shadowsEnabled,
                shadowStyle = shadowStyle
            )
            

        }
    }

    // Custom Glassy Bottom Sheet
    if (uiState.selectedReceipt != null) {
        val isDark = androidx.compose.foundation.isSystemInDarkTheme()
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissReceipt() },
            containerColor = Color.Transparent,
            scrimColor = Color.Transparent, // Removed dimming
            dragHandle = null // We draw our own or none for glassy effect
        ) {
             val shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
             
             // Reuse consistency logic
             // Shadow Logic
             val showShadow = blurEnabled && shadowsEnabled && shadowStyle != "None"
             val baseAlpha = if (showShadow) {
                 when (shadowStyle) {
                     "Soft" -> 0.15f
                     "Medium" -> 0.25f
                     "Sharp" -> 0.4f
                     else -> 0f
                 }
             } else 0f
             
             val shadowColor = com.nami.peace.ui.theme.SoftShadow.copy(alpha = baseAlpha)
             val elevation = if (showShadow) {
                    when (shadowStyle) {
                        "Soft" -> 4.dp
                        "Medium" -> 8.dp
                        "Sharp" -> 12.dp
                        else -> 0.dp
                    }
             } else 0.dp
             
             // Border Logic for Glass Mode
             val showBorder = blurEnabled && shadowsEnabled && shadowStyle != "None"
             val borderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)
             val borderModifier = if (showBorder) Modifier.border(1.dp, borderColor, shape) else Modifier
        
            val containerColor = if (blurEnabled) Color.Transparent else MaterialTheme.colorScheme.surfaceContainer

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = elevation, shape = shape, spotColor = shadowColor, ambientColor = shadowColor)
                    .then(borderModifier)
                    .background(containerColor)
                    .then(
                        if (blurEnabled) {
                            Modifier.hazeChild(
                                state = state,
                                shape = shape,
                                style = HazeStyle(
                                    blurRadius = blurStrength.dp, 
                                    tint = if (isDark) com.nami.peace.ui.theme.GlassyBlack.copy(alpha = blurTintAlpha) else com.nami.peace.ui.theme.GlassyWhite.copy(alpha = blurTintAlpha)
                                )
                            )
                        } else {
                            Modifier
                        }
                    )
            ) {
                HistoryReceiptSheet(
                    reminder = uiState.selectedReceipt!!,
                    onRestore = { viewModel.restoreTask(uiState.selectedReceipt!!) },
                    onRepeat = { viewModel.repeatTask(uiState.selectedReceipt!!) },
                    onDelete = { viewModel.deleteTask(uiState.selectedReceipt!!) }
                )
            }
        }
    }
}

@Composable
fun GlassyHeader(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Translate Header Strings
        val displayText = when(text) {
            "Today" -> stringResource(R.string.history_header_today)
            "Yesterday" -> stringResource(R.string.history_header_yesterday)
            else -> text
        }
        
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
            modifier = Modifier.height(32.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun HistoryReceiptSheet(
    reminder: Reminder,
    onRestore: () -> Unit,
    onRepeat: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Drag Handle
        Box(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .width(32.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
        )

        val isAbandoned = reminder.isAbandoned
        val icon = if (isAbandoned) Icons.Default.Cancel else Icons.Default.CheckCircle
        val iconTint = if (isAbandoned) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        val dateText = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(reminder.completedTime ?: 0))
        val timeText = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(reminder.completedTime ?: 0))
        
        val fullStatusText = if (isAbandoned) {
            "Abandoned on $dateText at $timeText"
        } else {
            stringResource(R.string.receipt_completed_on, dateText) + " " + stringResource(R.string.receipt_completed_at, timeText)
        }

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = reminder.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = fullStatusText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        
        // Compact Details Grid
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Row 1: Category & Priority
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    CompactDetailItem(
                        label = stringResource(R.string.history_label_category),
                        value = reminder.category.name,
                        icon = {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(id = reminder.category.iconResId),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
                
                Box(modifier = Modifier.weight(1f)) {
                     CompactDetailItem(
                        label = stringResource(R.string.history_label_priority),
                        value = reminder.priority.name,
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(getPriorityColor(reminder.priority), CircleShape)
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape)
                            )
                        }
                    )
                }
            }
            
            // Row 2: Time & Recurrence
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    CompactDetailItem(
                        label = stringResource(R.string.reminder_label_original_time),
                        value = formatTime(androidx.compose.ui.platform.LocalContext.current, reminder.originalStartTimeInMillis)
                    )
                }
                
                val recurrenceValue = when (reminder.recurrenceType) {
                    com.nami.peace.domain.model.RecurrenceType.ONE_TIME -> stringResource(R.string.reminder_recurrence_one_time)
                    com.nami.peace.domain.model.RecurrenceType.WEEKLY -> stringResource(R.string.reminder_recurrence_weekly_prefix)
                    else -> reminder.recurrenceType.name
                }
                
                Box(modifier = Modifier.weight(1f)) {
                    CompactDetailItem(
                        label = stringResource(R.string.reminder_label_recurrence),
                        value = recurrenceValue
                    )
                }
            }
            
            // Row 3: Nag Mode (If Enabled)
            if (reminder.isNagModeEnabled) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) {
                        CompactDetailItem(
                            label = stringResource(R.string.reminder_label_nag_mode),
                            value = stringResource(R.string.reminder_status_enabled),
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Refresh, // Using Refresh as a proxy for "Cycle/Repeat" nature of Nag
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                    
                    Box(modifier = Modifier.weight(1f)) {
                         val intervalMinutes = (reminder.nagIntervalInMillis ?: 0L) / 60000L
                         val stats = "${intervalMinutes}m • ${reminder.nagTotalRepetitions}x"
                         CompactDetailItem(
                            label = stringResource(R.string.history_label_nag_stats),
                            value = stats
                        )
                    }
                }
            }
        }
        

        
        if (!reminder.notes.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = reminder.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.receipt_action_delete), tint = MaterialTheme.colorScheme.error)
            }
            
            OutlinedButton(onClick = onRestore) {
                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.receipt_action_restore))
            }
            
            Button(onClick = onRepeat) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.receipt_action_repeat))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CompactDetailItem(
    label: String,
    value: String,
    icon: (@Composable () -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

