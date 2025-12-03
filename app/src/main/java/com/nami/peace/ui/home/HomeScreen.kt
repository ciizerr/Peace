package com.nami.peace.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.Reminder
import com.nami.peace.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddReminder: () -> Unit,
    onEditReminder: (Int) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToPeaceGarden: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToFocus: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val reminders = uiState.sections.values.flatten()
    
    // Pre-calculate lists outside LazyColumn
    val activeReminders = reminders.filter { it.isEnabled && !it.isCompleted }
    val nextUp = activeReminders.firstOrNull()
    val upcomingList = reminders.filter { it.id != nextUp?.id && it.isEnabled && !it.isCompleted }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GradientLightStart,
                        GradientLightEnd
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                // Premium Top Bar with blur effect
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Logo and Title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(SerenityBlue, CalmTeal)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Spa,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    "Peace", 
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.W600
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "Your calm companion",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Settings Button
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                Icons.Default.Settings, 
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                // Premium FAB with glow effect
                FloatingActionButton(
                    onClick = onAddReminder,
                    containerColor = SerenityBlue,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = CircleShape,
                            ambientColor = SerenityBlue.copy(alpha = 0.3f),
                            spotColor = SerenityBlue.copy(alpha = 0.3f)
                        )
                ) {
                    Icon(
                        Icons.Default.Add, 
                        contentDescription = "Add Reminder",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Quick Stats Row
                item {
                    QuickStatsRow(
                        totalTasks = reminders.size,
                        completedToday = reminders.count { reminder: Reminder -> reminder.isCompleted },
                        activeReminders = reminders.count { reminder: Reminder -> reminder.isEnabled && !reminder.isCompleted },
                        streak = 7 // TODO: Get from Peace Garden
                    )
                }

                // Today's Focus Section
                item {
                    TodaysFocusCard(
                        focusTime = "2h 30m", // TODO: Get from focus timer
                        tasksCompleted = reminders.count { reminder: Reminder -> reminder.isCompleted },
                        goalProgress = 0.65f
                    )
                }

                // Peace Garden Card
                item {
                    PremiumPeaceGardenCard(completedCount = reminders.count { reminder: Reminder -> reminder.isCompleted })
                }

                // Quick Actions
                item {
                    QuickActionsRow(
                        onViewGarden = onNavigateToPeaceGarden,
                        onViewHistory = onNavigateToHistory,
                        onStartFocus = onNavigateToFocus
                    )
                }

                // Motivational Quote
                item {
                    MotivationalQuoteCard()
                }

                // Weekly Progress
                item {
                    WeeklyProgressCard(
                        completedThisWeek = reminders.count { reminder: Reminder -> reminder.isCompleted },
                        totalThisWeek = reminders.size
                    )
                }

                // Section: Next Up
                if (nextUp != null) {
                    item {
                        Text(
                            "Next Up", 
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.W600
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    item {
                        PremiumHeroReminderCard(
                            reminder = nextUp,
                            onClick = { onEditReminder(nextUp.id) }
                        )
                    }
                }

                // Section: Upcoming
                if (upcomingList.isNotEmpty()) {
                    item {
                        Text(
                            "Upcoming", 
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.W600
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                        )
                    }
                    items(upcomingList, key = { reminder: Reminder -> reminder.id }) { reminder ->
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
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            AccentError.copy(alpha = 0.12f), 
                                            MaterialTheme.shapes.medium
                                        )
                                        .padding(horizontal = 28.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = AccentError,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            content = {
                                PremiumUpcomingReminderCard(
                                    reminder = reminder,
                                    onToggle = { viewModel.toggleReminder(reminder) },
                                    onClick = { onEditReminder(reminder.id) }
                                )
                            },
                            enableDismissFromStartToEnd = false
                        )
                    }
                } else if (nextUp == null) {
                    item {
                        PremiumEmptyStateCard()
                    }
                }
                
                // Bottom spacing for FAB
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
fun PremiumPeaceGardenCard(completedCount: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = MaterialTheme.shapes.large,
                ambientColor = ShadowLight,
                spotColor = ShadowMedium
            ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            GlowMint,
                            GlowTeal
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Your Peace Garden",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.W600
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "$completedCount tasks completed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MintGlow,
                                    CalmTeal
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "🌱",
                        style = MaterialTheme.typography.displaySmall
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumHeroReminderCard(
    reminder: Reminder,
    onClick: () -> Unit
) {
    val gradientColors = when (reminder.priority) {
        PriorityLevel.HIGH -> listOf(PriorityHigh, PriorityHigh.copy(alpha = 0.85f))
        PriorityLevel.MEDIUM -> listOf(PriorityMedium, PriorityMedium.copy(alpha = 0.85f))
        PriorityLevel.LOW -> listOf(PriorityLow, PriorityLow.copy(alpha = 0.85f))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .shadow(
                elevation = 8.dp,
                shape = MaterialTheme.shapes.extraLarge,
                ambientColor = ShadowMedium,
                spotColor = ShadowDark
            )
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradientColors))
                .padding(32.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    formatTime(reminder.startTimeInMillis),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.W300
                    ),
                    color = Color.White
                )
                Text(
                    reminder.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.W600
                    ),
                    color = Color.White.copy(alpha = 0.98f)
                )
                if (reminder.isNagModeEnabled) {
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.extraSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            "Repetition ${reminder.currentRepetitionIndex + 1}/${reminder.nagTotalRepetitions}",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }
                }
            }
            
            if (reminder.isInNestedSnoozeLoop) {
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("💤", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumUpcomingReminderCard(
    reminder: Reminder,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    val stripColor = when (reminder.priority) {
        PriorityLevel.HIGH -> PriorityHigh
        PriorityLevel.MEDIUM -> PriorityMedium
        PriorityLevel.LOW -> PriorityLow
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = MaterialTheme.shapes.medium,
                ambientColor = ShadowLight,
                spotColor = ShadowLight
            )
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(stripColor)
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    reminder.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.W600
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    formatTime(reminder.startTimeInMillis),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (reminder.isInNestedSnoozeLoop) {
                    Text(
                        "💤 Snoozed",
                        style = MaterialTheme.typography.labelSmall,
                        color = SerenityBlue
                    )
                }
            }
            
            Switch(
                checked = reminder.isEnabled,
                onCheckedChange = { onToggle(it) },
                modifier = Modifier.padding(end = 20.dp),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = SerenityBlue,
                    checkedTrackColor = SerenityBlue.copy(alpha = 0.5f),
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

@Composable
fun PremiumEmptyStateCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp)
            .shadow(
                elevation = 2.dp,
                shape = MaterialTheme.shapes.large,
                ambientColor = ShadowLight,
                spotColor = ShadowLight
            ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(GlowBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "🌿",
                    style = MaterialTheme.typography.displayMedium
                )
            }
            Text(
                "All Clear",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.W600
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "No reminders scheduled.\nEnjoy your peace.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun QuickStatsRow(
    totalTasks: Int,
    completedToday: Int,
    activeReminders: Int,
    streak: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Completed Today
        StatCard(
            modifier = Modifier.weight(1f),
            icon = "✓",
            value = completedToday.toString(),
            label = "Completed",
            color = AccentSuccess
        )
        
        // Active Tasks
        StatCard(
            modifier = Modifier.weight(1f),
            icon = "⏰",
            value = activeReminders.toString(),
            label = "Active",
            color = SerenityBlue
        )
        
        // Streak
        StatCard(
            modifier = Modifier.weight(1f),
            icon = "🔥",
            value = streak.toString(),
            label = "Day Streak",
            color = PriorityMedium
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: String,
    value: String,
    label: String,
    color: Color
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = MaterialTheme.shapes.medium,
                ambientColor = ShadowLight,
                spotColor = ShadowLight
            ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    icon,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.W600
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TodaysFocusCard(
    focusTime: String,
    tasksCompleted: Int,
    goalProgress: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = MaterialTheme.shapes.large,
                ambientColor = ShadowLight,
                spotColor = ShadowMedium
            ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Today's Focus",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.W600
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "$tasksCompleted tasks completed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Focus Time Badge
                Surface(
                    color = SerenityBlue.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⏱️", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            focusTime,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.W600
                            ),
                            color = SerenityBlue
                        )
                    }
                }
            }
            
            // Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Daily Goal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${(goalProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.W600
                        ),
                        color = SerenityBlue
                    )
                }
                
                LinearProgressIndicator(
                    progress = goalProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(MaterialTheme.shapes.extraSmall),
                    color = SerenityBlue,
                    trackColor = SerenityBlue.copy(alpha = 0.15f)
                )
            }
        }
    }
}

@Composable
fun QuickActionsRow(
    onViewGarden: () -> Unit,
    onViewHistory: () -> Unit,
    onStartFocus: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionButton(
            modifier = Modifier.weight(1f),
            icon = "🌱",
            label = "Garden",
            onClick = onViewGarden,
            color = MintGlow
        )
        
        QuickActionButton(
            modifier = Modifier.weight(1f),
            icon = "📊",
            label = "History",
            onClick = onViewHistory,
            color = CalmTeal
        )
        
        QuickActionButton(
            modifier = Modifier.weight(1f),
            icon = "🎯",
            label = "Focus",
            onClick = onStartFocus,
            color = SerenityBlue
        )
    }
}

@Composable
fun QuickActionButton(
    modifier: Modifier = Modifier,
    icon: String,
    label: String,
    onClick: () -> Unit,
    color: Color
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = MaterialTheme.shapes.small,
                ambientColor = ShadowLight,
                spotColor = ShadowLight
            )
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    icon,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun SuggestedActionsCard(
    suggestions: List<String>
) {
    if (suggestions.isEmpty()) return
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = MaterialTheme.shapes.large,
                ambientColor = ShadowLight,
                spotColor = ShadowLight
            ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("💡", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Suggested Actions",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.W600
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            suggestions.forEach { suggestion ->
                SuggestionItem(text = suggestion)
            }
        }
    }
}

@Composable
fun SuggestionItem(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(SerenityBlue)
            )
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MotivationalQuoteCard() {
    val quotes = listOf(
        "One step at a time brings peace of mind.",
        "Progress, not perfection.",
        "Small actions create big changes.",
        "You're doing better than you think.",
        "Consistency is the key to growth."
    )
    
    val quote = remember { quotes.random() }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = MaterialTheme.shapes.large,
                ambientColor = ShadowLight,
                spotColor = ShadowLight
            ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            GlowBlue,
                            GlowTeal
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "✨",
                    style = MaterialTheme.typography.displaySmall
                )
                Text(
                    quote,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.W500
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun UpcomingEventsPreview(
    events: List<Reminder>
) {
    if (events.isEmpty()) return
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = MaterialTheme.shapes.large,
                ambientColor = ShadowLight,
                spotColor = ShadowLight
            ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Coming Up Next",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.W600
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            events.take(3).forEach { event ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when (event.priority) {
                                    PriorityLevel.HIGH -> PriorityHigh
                                    PriorityLevel.MEDIUM -> PriorityMedium
                                    PriorityLevel.LOW -> PriorityLow
                                }
                            )
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            event.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.W500
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            formatTime(event.startTimeInMillis),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyProgressCard(
    completedThisWeek: Int,
    totalThisWeek: Int
) {
    val progress = if (totalThisWeek > 0) completedThisWeek.toFloat() / totalThisWeek else 0f
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = MaterialTheme.shapes.large,
                ambientColor = ShadowLight,
                spotColor = ShadowLight
            ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "This Week",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.W600
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "$completedThisWeek of $totalThisWeek completed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(CalmTeal.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.W600
                        ),
                        color = CalmTeal
                    )
                }
            }
            
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(MaterialTheme.shapes.extraSmall),
                color = CalmTeal,
                trackColor = CalmTeal.copy(alpha = 0.15f)
            )
        }
    }
}

private fun formatTime(millis: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(millis))
}
