package com.nami.peace.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nami.peace.domain.model.Reminder
import com.nami.peace.ui.theme.GlassyBlack
import com.nami.peace.ui.theme.GlassyWhite
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale

@Composable
fun PeaceCalendar(
    isExpanded: Boolean,
    historyDates: Set<LocalDate>,
    selectedDate: LocalDate?,
    currentMonth: YearMonth,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    RoundedCornerShape(24.dp)
                )
                .padding(16.dp)
        ) {
            val days = remember(currentMonth) {
                val start = currentMonth.atDay(1)
                val end = currentMonth.atEndOfMonth()
                val dayList = mutableListOf<LocalDate?>()
                
                // Pad empty days at start
                repeat(start.dayOfWeek.value % 7) { dayList.add(null) }
                
                for (i in 1..end.dayOfMonth) {
                    dayList.add(currentMonth.atDay(i))
                }
                dayList
            }

            // Month Header with Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, start = 8.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onMonthChange(-1) }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Previous Month",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + currentMonth.year,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = { onMonthChange(1) }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = "Next Month",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Day Labels Row
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 8.dp)
                    )
                }
            }
            
            // Calendar Rows
            val weeks = days.chunked(7)
            Column(modifier = Modifier.fillMaxWidth()) {
                weeks.forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        week.forEach { date ->
                            Box(modifier = Modifier.weight(1f)) {
                                if (date != null) {
                                    CalendarDay(
                                        date = date,
                                        isSelected = date == selectedDate,
                                        hasHistory = historyDates.contains(date),
                                        onSelect = { onDateSelected(date) }
                                    )
                                } else {
                                    Spacer(modifier = Modifier.aspectRatio(1f))
                                }
                            }
                        }
                        // Handle incomplete last week
                        if (week.size < 7) {
                             repeat(7 - week.size) {
                                 Spacer(modifier = Modifier.weight(1f))
                             }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDay(
    date: LocalDate,
    isSelected: Boolean,
    hasHistory: Boolean,
    onSelect: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary 
                else Color.Transparent
            )
            .clickable(onClick = onSelect)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected || hasHistory) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary 
                        else if (hasHistory) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
            )
            
            if (hasHistory && !isSelected) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
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
    blurTintAlpha: Float = 0.5f,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onLongClick: () -> Unit = {},
    onToggleSelection: (Boolean) -> Unit = {}
) {
    if (isCompact) {
        CompactHistoryRow(reminder, onClick, hazeState, blurEnabled, blurStrength, blurTintAlpha, isSelectionMode, isSelected, onLongClick, onToggleSelection)
    } else {
        StandardHistoryRow(reminder, onClick, hazeState, blurEnabled, blurStrength, blurTintAlpha, isSelectionMode, isSelected, onLongClick, onToggleSelection)
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun StandardHistoryRow(
    reminder: Reminder,
    onClick: () -> Unit,
    hazeState: HazeState? = null,
    blurEnabled: Boolean = true,
    blurStrength: Float = 12f,
    blurTintAlpha: Float = 0.5f,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onLongClick: () -> Unit,
    onToggleSelection: (Boolean) -> Unit
) {
    val containerModifier = Modifier
        .padding(horizontal = 16.dp, vertical = 6.dp)
        .combinedClickable(
            onClick = { if (isSelectionMode) onToggleSelection(!isSelected) else onClick() },
            onLongClick = onLongClick
        )

    GlassyItemContainer(
        onClick = { if (isSelectionMode) onToggleSelection(!isSelected) else onClick() },
        onLongClick = onLongClick,
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
            if (isSelectionMode) {
                RadioButton(
                    selected = isSelected,
                    onClick = { onToggleSelection(!isSelected) },
                    modifier = Modifier.size(24.dp)
                )
            } else {
                val icon = if (reminder.isAbandoned) Icons.Default.Cancel else Icons.Default.CheckCircle
                val iconTint = if (reminder.isAbandoned) MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                               else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            
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
                    text = com.nami.peace.ui.components.formatTime(androidx.compose.ui.platform.LocalContext.current, reminder.completedTime ?: 0),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Category & Priority Indicators
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                     painter = androidx.compose.ui.res.painterResource(id = reminder.category.iconResId),
                     contentDescription = null,
                     tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                     modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(com.nami.peace.ui.components.getPriorityColor(reminder.priority), CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape)
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CompactHistoryRow(
    reminder: Reminder,
    onClick: () -> Unit,
    hazeState: HazeState? = null,
    blurEnabled: Boolean = true,
    blurStrength: Float = 12f,
    blurTintAlpha: Float = 0.5f,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onLongClick: () -> Unit,
    onToggleSelection: (Boolean) -> Unit
) {
    GlassyItemContainer(
        onClick = { if (isSelectionMode) onToggleSelection(!isSelected) else onClick() },
        onLongClick = onLongClick,
        hazeState = hazeState,
        blurEnabled = blurEnabled,
        blurStrength = blurStrength,
        blurTintAlpha = blurTintAlpha,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                RadioButton(
                    selected = isSelected,
                    onClick = { onToggleSelection(!isSelected) },
                    modifier = Modifier.size(16.dp)
                )
            } else {
                val icon = if (reminder.isAbandoned) Icons.Default.Cancel else Icons.Default.CheckCircle
                val iconTint = if (reminder.isAbandoned) MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                               else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }
            
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

            Spacer(modifier = Modifier.width(8.dp))
            
            // Compact Category/Priority
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                     painter = androidx.compose.ui.res.painterResource(id = reminder.category.iconResId),
                     contentDescription = null,
                     tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                     modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(com.nami.peace.ui.components.getPriorityColor(reminder.priority), CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GlassyItemContainer(
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    hazeState: HazeState?,
    blurEnabled: Boolean,
    blurStrength: Float,
    blurTintAlpha: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val isDark = isSystemInDarkTheme()
    val borderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, borderColor, shape)
            .background(
                if (isDark) GlassyBlack.copy(alpha = 0.3f) 
                else GlassyWhite.copy(alpha = 0.3f)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
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
                                 GlassyBlack.copy(alpha = blurTintAlpha) 
                             else 
                                 GlassyWhite.copy(alpha = blurTintAlpha),
                             blurRadius = blurStrength.dp
                        )
                    )
            )
        }
        content()
    }
}
