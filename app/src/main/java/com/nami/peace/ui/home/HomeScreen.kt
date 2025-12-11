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
    shadowsEnabled: Boolean = true,
    shadowStyle: String = "Medium",
    isFABVisible: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsState()

    
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
                        val toDelete = (uiState.morningTasks + uiState.afternoonTasks + uiState.eveningTasks + listOfNotNull(uiState.focusTask))
                            .filter { selectedIds.contains(it.id) }
                        
                        // We use a helper from VM to delete multiple (ensure VM has it)
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

    // Toast Observation
    val context = androidx.compose.ui.platform.LocalContext.current
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
        }
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

            val fabContentColor by animateColorAsState(
                if (isSelectionMode) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary,
                label = "fab_content_color"
            )

            GlassyFloatingActionButton(
                onClick = { 
                    if (isSelectionMode) {
                        showDeleteDialog = true
                    } else {
                        onAddReminder()
                    }
                },
                modifier = Modifier.padding(bottom = 100.dp), // Lift above bottom bar
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
                // Item 1: Dashboard Header (Greeting)
                item {
                    DashboardHeader(
                        greetingRes = uiState.greetingRes,
                        userName = uiState.userName
                    )
                }

                // Item 2: Progress Card
                item {
                    PeaceProgressCard(
                        completed = uiState.completedCount,
                        total = uiState.totalCount
                    )
                }

                // Item 3: Filter Row (Still useful to keep, maybe?)
                // User prompt implies "Rhythm & Focus", focus hero is key.
                // Keeping filters as it's a "Dashboard".
                item {
                    FilterChipsRow(
                        categories = uiState.availableCategories,
                        selectedCategory = uiState.selectedFilter,
                        onSelectCategory = viewModel::onFilterSelected
                    )
                }

                // Item 4: Focus Hero Section
                item {
                    Text(
                        stringResource(R.string.header_focus),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                    
                    FocusHeroCard(
                         reminder = uiState.focusTask,
                         onDone = { 
                             if (uiState.focusTask != null) {
                                 viewModel.markAsDone(uiState.focusTask!!)
                             }
                         }
                    )
                }

                // Item 5: Time Buckets
                
                // Morning
                if (uiState.morningTasks.isNotEmpty()) {
                    item { TimeBucketSection(stringResource(R.string.bucket_morning), uiState.morningTasks, selectedIds, isSelectionMode, viewModel::toggleReminder, toggleSelection, onEditReminder) }
                }

                // Afternoon
                if (uiState.afternoonTasks.isNotEmpty()) {
                    item { TimeBucketSection(stringResource(R.string.bucket_afternoon), uiState.afternoonTasks, selectedIds, isSelectionMode, viewModel::toggleReminder, toggleSelection, onEditReminder) }
                }

                // Evening
                if (uiState.eveningTasks.isNotEmpty()) {
                    item { TimeBucketSection(stringResource(R.string.bucket_evening), uiState.eveningTasks, selectedIds, isSelectionMode, viewModel::toggleReminder, toggleSelection, onEditReminder) }
                }
                
                // Empty State handled by FocusHero's empty state + empty buckets essentially.
                // If everything is empty (focusTask is null AND buckets empty), Focus Hero shows "No immediate focus".
            }
            
            // Overlay: Glassy Top App Bar
            GlassyTopAppBar(
                title = { 
                    if (isSelectionMode) {
                        Text(
                            stringResource(R.string.selected_count, selectedIds.size),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    } else {
                        Text(
                            stringResource(R.string.app_name),
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
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = stringResource(R.string.cd_profile),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
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

// -----------------------------------------------------------------------------
// Dashboard Components
// -----------------------------------------------------------------------------

@Composable
fun DashboardHeader(greetingRes: Int, userName: String?) {
    val baseGreeting = stringResource(greetingRes)
    val text = if (!userName.isNullOrBlank()) {
         stringResource(R.string.greeting_with_name_format, baseGreeting, userName)
    } else {
         baseGreeting
    }
    
    Text(
        text,
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun PeaceProgressCard(completed: Int, total: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.header_progress),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.progress_label, completed, total),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

@Composable
fun FocusHeroCard(reminder: Reminder?, onDone: () -> Unit) {
    if (reminder != null) {
        val stripColor = when (reminder.priority) {
            PriorityLevel.HIGH -> Color(0xFFEF5350)
            PriorityLevel.MEDIUM -> Color(0xFF42A5F5)
            PriorityLevel.LOW -> Color(0xFF66BB6A)
        }
    
        Card(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background Gradient or Effect?
                
                Column(
                    modifier = Modifier.padding(24.dp).align(Alignment.TopStart)
                ) {
                    // Priority Chip
                    Surface(
                        color = stripColor,
                        shape = CircleShape
                    ) {
                        Text(
                             reminder.priority.name,
                             modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                             style = MaterialTheme.typography.labelSmall,
                             color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        reminder.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )
                    Text(
                         formatTime(reminder.startTimeInMillis),
                         style = MaterialTheme.typography.titleMedium,
                         color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha=0.7f)
                    )
                }
                
                Button(
                    onClick = onDone,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.done))
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Eco, null)
                }
            }
        }
    } else {
        // Empty State
        Card(
            modifier = Modifier.fillMaxWidth().height(150.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f))
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.focus_empty),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TimeBucketSection(
    title: String, 
    reminders: List<Reminder>,
    selectedIds: Set<Int>,
    isSelectionMode: Boolean,
    onToggle: (Reminder) -> Unit,
    onToggleSelection: (Int) -> Unit,
    onEdit: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
             title,
             style = MaterialTheme.typography.titleSmall,
             color = MaterialTheme.colorScheme.primary,
             fontWeight = FontWeight.Bold,
             modifier = Modifier.padding(bottom = 8.dp)
        )
        
        reminders.forEach { reminder ->
            UpcomingReminderCard(
                reminder = reminder,
                isNextUp = false,
                isSelected = selectedIds.contains(reminder.id),
                onToggle = { onToggle(reminder) },
                onLongClick = { onToggleSelection(reminder.id) },
                onClick = {
                    if (isSelectionMode) onToggleSelection(reminder.id) else onEdit(reminder.id)
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipsRow(
    categories: List<com.nami.peace.domain.model.ReminderCategory>,
    selectedCategory: com.nami.peace.domain.model.ReminderCategory?,
    onSelectCategory: (com.nami.peace.domain.model.ReminderCategory?) -> Unit
) {
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
