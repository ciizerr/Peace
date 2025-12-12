package com.nami.peace.ui.reminder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nami.peace.R
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.RecurrenceType
import com.nami.peace.domain.model.Reminder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer

import com.nami.peace.ui.components.formatTime
import com.nami.peace.ui.components.getPriorityColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReminderDetailSheet(
    reminder: Reminder,
    onEdit: () -> Unit,
    onClose: () -> Unit
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

        // Content Area (Scrollable)
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Checkbox/Icon Area (Visual only for now, or match History style)
            // Using Category Icon + Priority Dot as the main visual anchor
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = reminder.category.iconResId),
                        contentDescription = reminder.category.name,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = reminder.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            // "Next: 10:00 AM"
            val context = androidx.compose.ui.platform.LocalContext.current
            val nextTimeText = if (reminder.isNagModeEnabled) {
                 val interval = reminder.nagIntervalInMillis ?: 0L
                 val nextTime = reminder.originalStartTimeInMillis + (reminder.currentRepetitionIndex * interval)
                 formatTime(context, nextTime)
             } else {
                 formatTime(context, reminder.startTimeInMillis)
             }
             
            Text(
                text = "Next: $nextTimeText",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Compact Details Grid
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Row 1: Category & Priority
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) {
                        DetailItem(
                            label = stringResource(R.string.label_category),
                            value = reminder.category.name,
                            icon = {
                                Icon(
                                    painter = painterResource(id = reminder.category.iconResId),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                    
                    Box(modifier = Modifier.weight(1f)) {
                        DetailItem(
                            label = stringResource(R.string.label_priority),
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
                
                // Row 2: Original Time & Recurrence
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) {
                        DetailItem(
                            label = stringResource(R.string.reminder_label_original_time),
                            value = formatTime(context, reminder.originalStartTimeInMillis)
                        )
                    }
                    
                    Box(modifier = Modifier.weight(1f)) {
                         val recurrenceValue = when (reminder.recurrenceType) {
                            RecurrenceType.ONE_TIME -> {
                                 if (reminder.dateInMillis != null) {
                                      SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(reminder.dateInMillis))
                                 } else {
                                      stringResource(R.string.reminder_recurrence_one_time)
                                 }
                            }
                            RecurrenceType.WEEKLY -> {
                                if (reminder.daysOfWeek.isNotEmpty()) {
                                    stringResource(R.string.reminder_recurrence_weekly_prefix) + " " + formatDaysOfWeek(reminder.daysOfWeek)
                                } else {
                                    stringResource(R.string.reminder_recurrence_weekly_prefix)
                                }
                            }
                            else -> reminder.recurrenceType.name
                        }
                        DetailItem(
                            label = stringResource(R.string.reminder_label_recurrence),
                            value = recurrenceValue
                        )
                    }
                }
                    // Row 3: Scheduling Mode & (Empty/Future)

                
                // Row 4: Nag Mode Sequence
                if (reminder.isNagModeEnabled) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    NagSequence(
                       startTime = reminder.originalStartTimeInMillis,
                       interval = reminder.nagIntervalInMillis ?: 0L,
                       totalRepetitions = reminder.nagTotalRepetitions,
                       currentIndex = reminder.currentRepetitionIndex,
                       isStrict = reminder.isStrictSchedulingEnabled
                    )
                }
            } // End Grid

            // Notes
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
            
            // Spacer at bottom of scrollable content to separate from pinned buttons (which have their own top spacer)
             Spacer(modifier = Modifier.height(24.dp))
        }

        // Fixed Actions Footer
        Column(modifier = Modifier.fillMaxWidth()) {
             // Optional: Gradient or divider above actions
             // HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
             // Spacer(modifier = Modifier.height(16.dp))
             
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onClose,
                    modifier = Modifier.weight(1f)
                ) {
                     Text(stringResource(R.string.cancel))
                }
                
                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.reminder_action_edit))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}



@Composable
fun NagSequence(
    startTime: Long,
    interval: Long,
    totalRepetitions: Int,
    currentIndex: Int,
    isStrict: Boolean
) {
    val listState = rememberLazyListState()
    
    // Animation for Current Item (Slow Bounce)
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f, // Move up 6dp
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounceOffset"
    )

    // Auto-scroll to center context around current index
    androidx.compose.runtime.LaunchedEffect(currentIndex) {
        if (totalRepetitions > 0) {
            // Scroll to have the current item somewhat centered or visible (e.g. show one previous item for context)
            listState.animateScrollToItem(kotlin.math.max(0, currentIndex - 1))
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val modeText = if (isStrict) stringResource(R.string.reminder_scheduling_strict) else stringResource(R.string.reminder_scheduling_flexible)
            Text(
                text = "Nag Sequence • $modeText",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Stats: "15m × 30"
            val intervalMinutes = interval / 60000L
            Text(
                text = "${intervalMinutes}m × $totalRepetitions",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(24.dp), // More space for horizontal timeline
            contentPadding = PaddingValues(horizontal = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(totalRepetitions) { i ->
                val time = startTime + (i * interval)
                val isCurrent = i == currentIndex
                val isPast = i < currentIndex
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Circular Indicator
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                translationY = if (isCurrent) bounceOffset else 0f
                            }
                            .size(32.dp) // Slightly larger for touch target/visuals
                            .background(
                                color = if (isCurrent) MaterialTheme.colorScheme.primary else Color.Transparent, 
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = if (isCurrent) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha=0.5f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${i + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Time text
                    Text(
                        text = formatTime(androidx.compose.ui.platform.LocalContext.current, time),
                        style = MaterialTheme.typography.bodySmall, // Smaller font for timeline labels
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = if (isPast) MaterialTheme.colorScheme.onSurface.copy(alpha=0.5f) else MaterialTheme.colorScheme.onSurface
                    )

                    // Optional "Active" label for horizontal view
                    if (isCurrent) {
                        Text(
                            text = "Now",
                            style = MaterialTheme.typography.labelSmall, // Assuming labelSmall or custom
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                         // Empty spacer to align heights roughly if needed, or just let it be
                         Spacer(modifier = Modifier.height(12.dp)) // Approximate height of "Now" text to keep alignment
                    }
                }
            }
        }
    }
}

private fun formatDaysOfWeek(days: List<Int>): String {
    val symbols = java.text.DateFormatSymbols.getInstance().shortWeekdays
    // symbols is 1-based array where 1=Sunday, same as our model
    return days.sorted().joinToString(", ") { day ->
        if (day in 1..7) symbols[day] else ""
    }
}

@Composable
private fun DetailItem(
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
