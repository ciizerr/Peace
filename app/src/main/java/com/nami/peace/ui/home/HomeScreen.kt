package com.nami.peace.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.AutoAwesome
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
import com.nami.peace.ui.components.GlassyFloatingActionButton
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
    onProfileClick: () -> Unit = {},
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
        modifier = Modifier,
        containerColor = Color.Transparent, 
        // We do NOT use the topBar slot here to allow content to scroll BEHIND it for the glassy effect.
        // The TopBar is manually overlayed in the Box below.
        floatingActionButton = {
            val fabContainerColor by animateColorAsState(
                if (isSelectionMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                label = "fab_color"
            )
            
            val fabIcon = if (isSelectionMode) Icons.Default.Delete else Icons.Default.Add
            val fabContentDescription = if (isSelectionMode) stringResource(R.string.cd_delete) else stringResource(R.string.cd_add_reminder)

            GlassyFloatingActionButton(
                onClick = { 
                    if (isSelectionMode) {
                        showDeleteDialog = true
                    } else {
                        onAddReminder()
                    }
                },
                modifier = Modifier.padding(bottom = bottomPadding),
                containerColor = fabContainerColor,
                icon = fabIcon,
                contentDescription = fabContentDescription,
                hazeState = hazeState
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Main Content: Single Fluid LazyColumn
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
                    top = 100.dp, // Space for Top bar
                    bottom = 100.dp + bottomPadding, // Space for Bottom bar(s) + FAB
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Item 1: Garden Hero
                item {
                    PeaceGardenHero(
                        streakDays = uiState.streakDays,
                        activeCount = uiState.activeCount,
                        hasCompleted = uiState.streakDays > 0
                    )
                }

                // Item 2: Peace Coach
                item {
                    val messageFormat = stringResource(uiState.coachMessage)
                    val formattedMessage = if (uiState.coachMessage == R.string.coach_morning || uiState.coachMessage == R.string.coach_evening) {
                         String.format(messageFormat, uiState.activeCount)
                    } else {
                         messageFormat
                    }
                    PeaceCoachCard(message = formattedMessage)
                }

                // Item 3: Filter Row
                item {
                    FilterChipsRow(
                        selectedCategory = uiState.selectedFilter,
                        onSelectCategory = viewModel::onFilterSelected
                    )
                }

                // Section 4: Next Up (Hero Card)
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

                // Section 5: Grouped Lists
                sections.forEach { (header, reminders) ->
                    stickyHeader {
                        // Glassy Header Background to prevent overlapping text readability issues
                         Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f)) 
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                header,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    items(reminders, key = { it.id }) { reminder ->
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
                    }
                }
                
                // Empty State
                if (nextUp == null && sections.isEmpty()) {
                    item {
                        val emptyMessage = if (uiState.selectedFilter != null) {
                            stringResource(R.string.dashboard_list_empty_filter)
                        } else {
                            stringResource(R.string.no_reminders_message)
                        }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                emptyMessage,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            // Overlay: Glassy Top App Bar
            // We place it here manually so the list scrolls BEHIND it.
            GlassyTopAppBar(
                title = { 
                    if (isSelectionMode) {
                        Text(
                            "${selectedIds.size} Selected", // Will use interpolation
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    } else {
                        Text(
                            stringResource(R.string.my_schedule), 
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        IconButton(onClick = { selectedIds = emptySet() }) {
                            Icon(
                                imageVector = Icons.Default.Close, 
                                contentDescription = stringResource(R.string.cd_close_selection)
                            )
                        }
                    } else {
                        IconButton(onClick = onProfileClick) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle, 
                                contentDescription = stringResource(R.string.cd_profile),
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }
                },
                modifier = Modifier.align(Alignment.TopCenter), // Overlay at top
                hazeState = hazeState,
                blurEnabled = blurEnabled,
                blurStrength = blurStrength,
                blurTintAlpha = blurTintAlpha
            )
        }
    }
}

// -----------------------------------------------------------------------------
// Dashboard Components
// -----------------------------------------------------------------------------

@Composable
fun PeaceGardenHero(streakDays: Int, activeCount: Int, hasCompleted: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp), // Compacter Hero
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Centered Garden
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp), // Large Eco Icon
                    tint = Color(0xFF81C784) // Soft Green
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.dashboard_garden_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                // New Subtitle Logic
                val subtitle = when {
                    activeCount > 0 -> stringResource(R.string.garden_subtitle_active, activeCount)
                    streakDays > 0 -> stringResource(R.string.garden_subtitle_done)
                    else -> stringResource(R.string.garden_subtitle_empty)
                }
                
                Text(
                     text = subtitle,
                     style = MaterialTheme.typography.bodyMedium,
                     color = MaterialTheme.colorScheme.onSurfaceVariant
                 )
            }
            
            // Streak Pill (Top Right)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.background.copy(alpha=0.8f) // Subtle pill
            ) {
                val label = if (streakDays > 0) {
                    stringResource(R.string.dashboard_streak_active, streakDays)
                } else {
                    stringResource(R.string.dashboard_streak_empty)
                }
                
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun PeaceCoachCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CircleShape, // Pill Shape
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipsRow(
    selectedCategory: com.nami.peace.domain.model.ReminderCategory?,
    onSelectCategory: (com.nami.peace.domain.model.ReminderCategory?) -> Unit
) {
    val categories = com.nami.peace.domain.model.ReminderCategory.values()
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
        modifier = Modifier.fillMaxWidth() // Ensure it takes width
    ) {
        // "All" Chip
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onSelectCategory(null) },
                label = { Text(stringResource(R.string.dashboard_filter_all)) },
                leadingIcon = if (selectedCategory == null) {
                    { Icon(Icons.Default.Eco, null, modifier = Modifier.size(16.dp)) }
                } else null
            )
        }
        
        // Category Chips
        items(categories) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onSelectCategory(category) },
                label = { Text(category.name.lowercase().replaceFirstChar { it.uppercase() }) },
                leadingIcon = {
                     Icon(
                         painter = androidx.compose.ui.res.painterResource(id = category.iconResId),
                         contentDescription = null,
                         modifier = Modifier.size(18.dp)
                     )
                }
            )
        }
    }
}

@Composable
fun EmptyStateComponent() {
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

// -----------------------------------------------------------------------------
// Existing Components (Helper methods kept at bottom)
// -----------------------------------------------------------------------------

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HeroReminderCard(
    reminder: Reminder,
    isSelected: Boolean = false,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit
) {
    // ... [Content unchanged from previous step, ensuring formatting matches]
    // Re-paste the HeroReminderCard and UpcomingReminderCard code to ensure they are present
    // Since I'm using replace_file_content with a large range logic, 
    // I will include their implementations here for safety.
    
    val gradientColors = if (isSelected) {
        listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), MaterialTheme.colorScheme.primary)
    } else {
        when (reminder.priority) {
            PriorityLevel.HIGH -> listOf(Color(0xFFEF5350), Color(0xFFB71C1C)) 
            PriorityLevel.MEDIUM -> listOf(Color(0xFF42A5F5), Color(0xFF1565C0))
            PriorityLevel.LOW -> listOf(Color(0xFF66BB6A), Color(0xFF2E7D32)) 
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
