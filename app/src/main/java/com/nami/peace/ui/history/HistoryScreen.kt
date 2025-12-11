package com.nami.peace.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
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
import androidx.compose.foundation.ExperimentalFoundationApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    hazeState: HazeState? = null,
    blurEnabled: Boolean = true,
    blurStrength: Float = 12f,
    blurTintAlpha: Float = 0.5f,
    onEditReminder: (Int) -> Unit = {}, // Default for preview/unused
    onSheetStateChange: (Boolean) -> Unit = {} // New callback
) {
    val uiState by viewModel.uiState.collectAsState()
    val state = hazeState ?: remember { HazeState() }
    val snackbarHostState = remember { SnackbarHostState() }

    // Notify MainScreen of sheet state
    androidx.compose.runtime.LaunchedEffect(uiState.selectedReceipt) {
        onSheetStateChange(uiState.selectedReceipt != null)
    }

    // Handle User Feedback Messages
    androidx.compose.runtime.LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.messageShown()
        }
    }
    
    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                contentPadding = PaddingValues(top = 100.dp, bottom = 100.dp),
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
                            HistoryItemRow(
                                reminder = reminder,
                                isCompact = header != "Today" && header != "Yesterday",
                                onClick = { viewModel.openReceipt(reminder) },
                                hazeState = state,
                                blurEnabled = false,
                                blurTintAlpha = blurTintAlpha,
                                blurStrength = blurStrength
                            )
                        }
                    }
                }
            }
            
            // Overlay Layer: Top App Bar
            GlassyTopAppBar(
                title = { 
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
                },
                actions = {
                   TextButton(onClick = { viewModel.toggleCalendar() }) {
                       Text(stringResource(R.string.history_timeline_label))
                   }
                },
                hazeState = state,
                blurEnabled = blurEnabled,
                blurStrength = blurStrength,
                blurTintAlpha = blurTintAlpha,
                modifier = Modifier.align(Alignment.TopCenter),
                shadowsEnabled = true,
                shadowStyle = "Medium"
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
             val (baseElevation, baseAlpha) = 8.dp to 0.25f // Medium style hardcoded for now or fetched from settings
             
             val shadowColor = com.nami.peace.ui.theme.SoftShadow.copy(alpha = baseAlpha)
             
             // Border Logic for Glass Mode
             val borderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)
             val borderModifier = Modifier.border(1.dp, borderColor, shape)
        
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 0.dp, shape = shape, spotColor = shadowColor, ambientColor = shadowColor) // Elevation handled by sheet usually, but adding for completeness if needed
                    .then(borderModifier)
                    .hazeChild(
                        state = state,
                        shape = shape,
                        style = HazeStyle(
                            blurRadius = blurStrength.dp, 
                            tint = if (isDark) com.nami.peace.ui.theme.GlassyBlack.copy(alpha = blurTintAlpha) else com.nami.peace.ui.theme.GlassyWhite.copy(alpha = blurTintAlpha)
                        )
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

        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
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
            text = stringResource(
                R.string.receipt_completed_on,
                SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(reminder.completedTime ?: 0))
            ) + " " + stringResource(
                R.string.receipt_completed_at,
                SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(reminder.completedTime ?: 0))
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
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

