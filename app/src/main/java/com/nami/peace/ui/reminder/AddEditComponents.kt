package com.nami.peace.ui.reminder

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.nami.peace.R
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.RecurrenceType
import com.nami.peace.domain.model.ReminderCategory
import com.nami.peace.ui.theme.GlassyWhite
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeChild
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.absoluteValue
import java.util.Locale

// --- VERTICAL SWIPE SELECTOR ---
// --- VERTICAL SWIPE SELECTOR (COMPACT) ---
// --- DROPDOWN SELECTOR ---
@Composable
fun <T> DropdownSelector(
    value: T,
    options: List<T>,
    onValueChanged: (T) -> Unit,
    labelMapper: (T) -> String,
    modifier: Modifier = Modifier,
    colorMapper: ((T) -> Color)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        // Tappable Area
        Row(
            modifier = Modifier
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val label = labelMapper(value)
            val color = colorMapper?.invoke(value) ?: MaterialTheme.colorScheme.onSurface
            
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
        
        // Dropdown Menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            content = {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            val optionLabel = labelMapper(option)
                            val optionColor = colorMapper?.invoke(option) ?: MaterialTheme.colorScheme.onSurface
                            Text(text = optionLabel, color = optionColor, fontWeight = FontWeight.Medium)
                        },
                        onClick = {
                            onValueChanged(option)
                            expanded = false
                        }
                    )
                }
            }
        )
    }
}

// --- NEW TITLE SECTION ---
@Composable
fun TitleSection(
    title: String,
    onTitleChanged: (String) -> Unit,
    categoryName: String,
    recurrenceName: String,
    timeText: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChanged,
                label = { Text(stringResource(R.string.title)) },
                placeholder = { Text(stringResource(R.string.input_hint_title)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true,
                shape = RoundedCornerShape(24.dp), // Slightly less than card for inner nesting or match it? Let's match or use Circle. 
                // Actually to look like "bottom nav bar" (floating pill), the card is the container. 
                // The text field inside should probably be cleaner.
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Read-only summary row
            Text(
                text = "$categoryName   $recurrenceName   $timeText",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// --- NEW DATE/TIME CARD (2-COLUMN) ---
@Composable
fun DateTimeCard(
    dateText: String,
    timeText: String,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date Component
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.label_date), style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = onDateClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text(dateText, maxLines = 1)
                }
            }
            
            // Time Component
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.label_time), style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = onTimeClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text(timeText, maxLines = 1)
                }
            }
        }
    }
}

// --- NEW CATEGORY/PRIORITY CARD (STACKED ROWS) ---
@Composable
fun CategoryPriorityCard(
    category: ReminderCategory,
    priority: PriorityLevel,
    onCategoryChanged: (ReminderCategory) -> Unit,
    onPriorityChanged: (PriorityLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(stringResource(R.string.label_category), style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            // Single Row: Category Scroll (Left) + Priority Dropdown (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category List (Scrollable, takes available space)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReminderCategory.values().forEach { cat ->
                        val isSelected = category == cat
                        Surface(
                            onClick = { onCategoryChanged(cat) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = cat.iconResId),
                                    contentDescription = cat.name,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // Vertical Divider
                Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier.height(40.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)))
                Spacer(modifier = Modifier.width(12.dp))
                
                // Priority Dropdown (Fixed Width)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.label_priority), style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    DropdownSelector(
                        value = priority,
                        options = PriorityLevel.values().toList(),
                        onValueChanged = onPriorityChanged,
                        labelMapper = { it.name },
                        colorMapper = { com.nami.peace.ui.components.getPriorityColor(it) },
                        modifier = Modifier.width(100.dp) // Slightly narrower for side-by-side
                    )
                }
            }
        }
    }
}

// --- RECURRENCE CARD (WITH INLINE PREVIEW) ---
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RecurrenceCard(
    recurrenceType: RecurrenceType,
    daysOfWeek: List<Int>,
    onRecurrenceChanged: (RecurrenceType) -> Unit,
    onDayToggled: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.recurrence), style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
            
                DropdownSelector(
                    value = recurrenceType,
                    options = RecurrenceType.values().toList(),
                    onValueChanged = onRecurrenceChanged,
                    labelMapper = { type ->
                        type.name.lowercase().split("_").joinToString(" ") { it.capitalize(Locale.getDefault()) }
                    }
                )
            }

            AnimatedVisibility(visible = recurrenceType == RecurrenceType.WEEKLY) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(stringResource(R.string.select_days), style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("S", "M", "T", "W", "T", "F", "S").forEachIndexed { index, label ->
                            val dayVal = index + 1
                            FilterChip(
                                selected = daysOfWeek.contains(dayVal),
                                onClick = { onDayToggled(dayVal) },
                                label = { Text(label) },
                                shape = CircleShape,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- ADVANCED BOTTOM SHEET CONTENT ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedBottomSheetContent(
    isNagModeEnabled: Boolean,
    onNagToggled: (Boolean) -> Unit,
    nagIntervalValue: String,
    onNagIntervalValueChanged: (String) -> Unit,
    nagIntervalUnit: TimeUnit,
    onNagIntervalUnitChanged: (TimeUnit) -> Unit,
    nagTotalRepetitions: Int,
    maxAllowedRepetitions: Int,
    onNagRepetitionsChanged: (Int) -> Unit,
    isStrictSchedulingEnabled: Boolean,
    onStrictModeToggled: (Boolean) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp) // Matched HistoryScreen padding
            .navigationBarsPadding() 
    ) {
        // Drag Handle (Matched HistoryScreen)
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .width(32.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            )
        }

        Text("Advanced Settings", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // Nag Switch
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clickable { onNagToggled(!isNagModeEnabled) }
        ) {
            Text(stringResource(R.string.nag_mode), modifier = Modifier.weight(1f))
            Switch(checked = isNagModeEnabled, onCheckedChange = onNagToggled)
        }
        
        if (isNagModeEnabled) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Interval
            Text(stringResource(R.string.interval), style = MaterialTheme.typography.labelMedium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nagIntervalValue,
                    onValueChange = { if (it.all(Char::isDigit)) onNagIntervalValueChanged(it) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    shape = RoundedCornerShape(32.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )
                TimeUnit.values().forEach { unit ->
                    FilterChip(
                        selected = nagIntervalUnit == unit,
                        onClick = { onNagIntervalUnitChanged(unit) },
                        label = { Text(unit.name.lowercase().capitalize(Locale.getDefault())) }
                    )
                }
            }
            
            // Presets
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "15m" to ("15" to TimeUnit.MINUTES),
                    "30m" to ("30" to TimeUnit.MINUTES),
                    "1h" to ("1" to TimeUnit.HOURS)
                ).forEach { (label, data) ->
                    SuggestionChip(
                        onClick = {
                            onNagIntervalValueChanged(data.first)
                            onNagIntervalUnitChanged(data.second)
                        },
                        label = { Text(label) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Repetitions
            Text("Repetitions: $nagTotalRepetitions", style = MaterialTheme.typography.labelMedium)
            Slider(
                value = nagTotalRepetitions.toFloat(),
                onValueChange = { onNagRepetitionsChanged(it.toInt()) },
                valueRange = 0f..maxAllowedRepetitions.toFloat(),
                steps = if (maxAllowedRepetitions > 0) maxAllowedRepetitions - 1 else 0
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Strict Mode
             Column {
                Text(stringResource(R.string.scheduling_mode), style = MaterialTheme.typography.labelMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = !isStrictSchedulingEnabled,
                        onClick = { onStrictModeToggled(false) }
                    )
                    Text("Flexible", style = MaterialTheme.typography.bodyMedium)
                    
                    Spacer(modifier = Modifier.width(16.dp))

                    RadioButton(
                        selected = isStrictSchedulingEnabled,
                        onClick = { onStrictModeToggled(true) }
                    )
                    Text("Strict", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
        ) {
            Text("Done")
        }
    }
}

// --- GLASSY ACTION BAR (Sticky Bottom) ---
@Composable
fun GlassyActionBar(
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            .padding(vertical = 12.dp, horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel))
            }
            
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}



@Composable
fun AdvancedBottomSheetTrigger(
    onClick: () -> Unit,
    previewText: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        FilledTonalButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ExpandMore, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.advanced_settings))
        }
        
        if (!previewText.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = previewText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
    }


// --- NEW NOTES SECTION ---
@Composable
fun NotesSection(
    notes: String,
    onNotesChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChanged,
                label = { Text("Notes") },
                placeholder = { Text("Add details...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                     unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )
        }
    }
}
