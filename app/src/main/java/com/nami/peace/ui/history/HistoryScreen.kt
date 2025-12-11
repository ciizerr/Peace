package com.nami.peace.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nami.peace.R
import com.nami.peace.domain.model.Reminder
import com.nami.peace.ui.components.GlassyTopAppBar
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    // Notify MainScreen of sheet state
    androidx.compose.runtime.LaunchedEffect(uiState.selectedItem) {
        onSheetStateChange(uiState.selectedItem != null)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // 1. Solid background for Haze to blur
            .haze( // 2. Move Haze source to root
                state = hazeState ?: remember { HazeState() },
                style = dev.chrisbanes.haze.HazeStyle(blurRadius = 15.dp, tint = Color.Transparent)
            )
    ) {
        Scaffold(
            topBar = {
                GlassyTopAppBar(
                    title = { Text(stringResource(R.string.history_log), fontWeight = FontWeight.Bold) },
                    hazeState = hazeState,
                    blurEnabled = blurEnabled
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            if (uiState.groupedItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                    stringResource(R.string.history_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(top = padding.calculateTopPadding() + 16.dp, bottom = 100.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                uiState.groupedItems.forEach { (header, items) ->
                    stickyHeader {
                        GlassyHeader(header)
                    }
                    
                    items(items) { reminder ->
                        HistoryItemRow(
                            reminder = reminder,
                            isCompact = header != "Today" && header != "Yesterday",
                            onClick = { viewModel.selectItem(reminder) },
                            hazeState = hazeState,
                            blurEnabled = false, // 3. Disable blur for items to improve readability
                            blurTintAlpha = blurTintAlpha,
                            blurStrength = blurStrength
                        )
                    }
                }
            }
        }
    }

        // Custom Glassy Bottom Sheet
        com.nami.peace.ui.components.GlassyBottomSheet(
            show = uiState.selectedItem != null,
            onDismissRequest = { viewModel.dismissSheet() },
            hazeState = hazeState
        ) {
            if (uiState.selectedItem != null) {
                HistoryReceiptSheet(
                    reminder = uiState.selectedItem!!,
                    onRestore = { viewModel.restoreItem(uiState.selectedItem!!) },
                    onRepeat = { viewModel.repeatItem(uiState.selectedItem!!) },
                    onDelete = { viewModel.deleteItem(uiState.selectedItem!!) }
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
fun HistoryItemRow(
    reminder: Reminder,
    isCompact: Boolean,
    onClick: () -> Unit,
    hazeState: HazeState? = null,
    blurEnabled: Boolean = true,
    blurStrength: Float = 12f,
    blurTintAlpha: Float = 0.5f
) {
    if (isCompact) {
        CompactHistoryRow(reminder, onClick, hazeState, blurEnabled, blurStrength, blurTintAlpha)
    } else {
        StandardHistoryRow(reminder, onClick, hazeState, blurEnabled, blurStrength, blurTintAlpha)
    }
}

@Composable
fun StandardHistoryRow(
    reminder: Reminder,
    onClick: () -> Unit,
    hazeState: HazeState? = null,
    blurEnabled: Boolean = true,
    blurStrength: Float = 12f,
    blurTintAlpha: Float = 0.5f
) {
    GlassyItemContainer(
        onClick = onClick,
        hazeState = hazeState,
        blurEnabled = blurEnabled,
        blurStrength = blurStrength,
        blurTintAlpha = blurTintAlpha,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(reminder.completedTime ?: 0)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CompactHistoryRow(
    reminder: Reminder,
    onClick: () -> Unit,
    hazeState: HazeState? = null,
    blurEnabled: Boolean = true,
    blurStrength: Float = 12f,
    blurTintAlpha: Float = 0.5f
) {
    GlassyItemContainer(
        onClick = onClick,
        hazeState = hazeState,
        blurEnabled = blurEnabled,
        blurStrength = blurStrength,
        blurTintAlpha = blurTintAlpha,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp) // Slightly tighter vertical padding
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = reminder.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(reminder.completedTime ?: 0)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun GlassyItemContainer(
    onClick: () -> Unit,
    hazeState: HazeState?,
    blurEnabled: Boolean,
    blurStrength: Float,
    blurTintAlpha: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val borderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, borderColor, shape)
            .background(
                if (isDark) com.nami.peace.ui.theme.GlassyBlack.copy(alpha = 0.3f) 
                else com.nami.peace.ui.theme.GlassyWhite.copy(alpha = 0.3f)
            )
            .clickable(onClick = onClick)
    ) {
         if (hazeState != null && blurEnabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .hazeChild(
                        state = hazeState,
                        shape = shape,
                        style = dev.chrisbanes.haze.HazeStyle(
                             tint = if (isDark) 
                                 com.nami.peace.ui.theme.GlassyBlack.copy(alpha = blurTintAlpha) 
                             else 
                                 com.nami.peace.ui.theme.GlassyWhite.copy(alpha = blurTintAlpha),
                             blurRadius = blurStrength.dp
                        )
                    )
            )
        }
        content()
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
                R.string.history_completed_on,
                SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(reminder.completedTime ?: 0)),
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
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.history_action_delete), tint = MaterialTheme.colorScheme.error)
            }
            
            OutlinedButton(onClick = onRestore) {
                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.history_action_restore))
            }
            
            Button(onClick = onRepeat) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.history_action_repeat))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

