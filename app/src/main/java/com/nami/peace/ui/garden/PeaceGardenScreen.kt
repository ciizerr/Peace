package com.nami.peace.ui.garden

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nami.peace.R
import com.nami.peace.data.local.GardenTheme
import com.nami.peace.ui.components.PeaceIcon
import com.nami.peace.ui.theme.GardenThemeConfig
import com.nami.peace.ui.theme.getGrowthStageIcon
import com.nami.peace.util.icon.IconManager
import com.nami.peace.util.icon.IoniconsManager

/**
 * Peace Garden Screen
 * 
 * Displays:
 * - Theme selector tabs
 * - Garden visualization with growth stage
 * - Streak display
 * - Milestone progress
 * - Recent achievements list
 * 
 * Requirements: 18.1, 18.2, 18.3, 18.5, 18.6, 18.7
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeaceGardenScreen(
    onNavigateUp: () -> Unit,
    viewModel: PeaceGardenViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val iconManager: IconManager = remember { IoniconsManager(context) }

    val currentThemeConfig by viewModel.currentThemeConfig.collectAsState()
    val growthStageInfo by viewModel.growthStageInfo.collectAsState()
    val streakInfo by viewModel.streakInfo.collectAsState()
    val nextMilestone by viewModel.nextMilestone.collectAsState()
    val achievedMilestones by viewModel.achievedMilestones.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.peace_garden),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        PeaceIcon(
                            iconName = "arrow_back",
                            contentDescription = stringResource(R.string.cd_back),
                            iconManager = iconManager
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Theme Selector Tabs
                item {
                    ThemeSelectorTabs(
                        availableThemes = viewModel.availableThemes,
                        currentTheme = currentThemeConfig?.theme,
                        onThemeSelected = { theme -> viewModel.updateTheme(theme) },
                        iconManager = iconManager
                    )
                }

                // Garden Visualization
                item {
                    currentThemeConfig?.let { config ->
                        growthStageInfo?.let { info ->
                            GardenVisualization(
                                themeConfig = config,
                                growthStageInfo = info,
                                iconManager = iconManager
                            )
                        }
                    }
                }

                // Streak Display
                item {
                    streakInfo?.let { info ->
                        currentThemeConfig?.let { config ->
                            StreakDisplay(
                                streakInfo = info,
                                themeConfig = config,
                                iconManager = iconManager
                            )
                        }
                    }
                }

                // Milestone Progress
                item {
                    nextMilestone?.let { milestone ->
                        streakInfo?.let { info ->
                            currentThemeConfig?.let { config ->
                                MilestoneProgress(
                                    currentStreak = info.currentStreak,
                                    nextMilestone = milestone,
                                    themeConfig = config,
                                    iconManager = iconManager
                                )
                            }
                        }
                    }
                }

                // Recent Achievements
                item {
                    if (achievedMilestones.isNotEmpty()) {
                        currentThemeConfig?.let { config ->
                            RecentAchievements(
                                achievedMilestones = achievedMilestones,
                                themeConfig = config,
                                iconManager = iconManager
                            )
                        }
                    }
                }

                // Bottom padding
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

/**
 * Theme selector tabs component
 */
@Composable
fun ThemeSelectorTabs(
    availableThemes: List<GardenThemeConfig>,
    currentTheme: GardenTheme?,
    onThemeSelected: (GardenTheme) -> Unit,
    iconManager: IconManager
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            stringResource(R.string.garden_theme),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableThemes.forEach { themeConfig ->
                val isSelected = themeConfig.theme == currentTheme
                ThemeTab(
                    themeConfig = themeConfig,
                    isSelected = isSelected,
                    onClick = { onThemeSelected(themeConfig.theme) },
                    iconManager = iconManager,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Individual theme tab
 */
@Composable
fun ThemeTab(
    themeConfig: GardenThemeConfig,
    isSelected: Boolean,
    onClick: () -> Unit,
    iconManager: IconManager,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                themeConfig.colors.primary.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, themeConfig.colors.primary)
        } else {
            null
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            PeaceIcon(
                iconName = themeConfig.icons.themeIcon,
                contentDescription = themeConfig.displayName,
                tint = if (isSelected) themeConfig.colors.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                iconManager = iconManager,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                themeConfig.displayName.split(" ").first(), // Show first word only
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) themeConfig.colors.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Garden visualization component showing growth stage
 */
@Composable
fun GardenVisualization(
    themeConfig: GardenThemeConfig,
    growthStageInfo: com.nami.peace.domain.usecase.GrowthStageInfo,
    iconManager: IconManager
) {
    // Animated scale for the main icon
    val infiniteTransition = rememberInfiniteTransition(label = "garden_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = themeConfig.colors.background
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Growth stage icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                themeConfig.colors.primary.copy(alpha = 0.3f),
                                themeConfig.colors.primary.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                PeaceIcon(
                    iconName = getGrowthStageIcon(themeConfig.theme, growthStageInfo.currentStage.stage),
                    contentDescription = growthStageInfo.currentStage.displayName,
                    tint = themeConfig.colors.primary,
                    iconManager = iconManager,
                    modifier = Modifier
                        .size(64.dp)
                        .scale(scale)
                )
            }

            // Growth stage name
            Text(
                growthStageInfo.currentStage.displayName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = themeConfig.colors.primary
            )

            // Growth stage description
            Text(
                growthStageInfo.currentStage.description,
                style = MaterialTheme.typography.bodyMedium,
                color = themeConfig.colors.onBackground,
                textAlign = TextAlign.Center
            )

            // Tasks completed
            Text(
                stringResource(R.string.tasks_completed_format, growthStageInfo.tasksCompleted),
                style = MaterialTheme.typography.titleMedium,
                color = themeConfig.colors.primary
            )

            // Progress to next stage
            growthStageInfo.nextStage?.let { nextStage ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            stringResource(R.string.next_stage_format, nextStage.displayName),
                            style = MaterialTheme.typography.labelMedium,
                            color = themeConfig.colors.onBackground
                        )
                        Text(
                            "${growthStageInfo.progressToNextStage}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = themeConfig.colors.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LinearProgressIndicator(
                        progress = { growthStageInfo.progressToNextStage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = themeConfig.colors.primary,
                        trackColor = themeConfig.colors.primary.copy(alpha = 0.2f)
                    )

                    growthStageInfo.tasksNeededForNextStage?.let { tasksNeeded ->
                        Text(
                            stringResource(
                                R.string.tasks_until_next_stage_format,
                                tasksNeeded - growthStageInfo.tasksCompleted
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = themeConfig.colors.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Streak display component
 */
@Composable
fun StreakDisplay(
    streakInfo: com.nami.peace.domain.usecase.StreakInfo,
    themeConfig: GardenThemeConfig,
    iconManager: IconManager
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Current streak
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PeaceIcon(
                    iconName = themeConfig.icons.streakIcon,
                    contentDescription = stringResource(R.string.current_streak),
                    tint = themeConfig.colors.accent,
                    iconManager = iconManager,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        stringResource(R.string.current_streak),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        stringResource(R.string.days_format, streakInfo.currentStreak),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = themeConfig.colors.primary
                    )
                }
            }

            // Longest streak
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    stringResource(R.string.longest_streak),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    stringResource(R.string.days_format, streakInfo.longestStreak),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Milestone progress component
 */
@Composable
fun MilestoneProgress(
    currentStreak: Int,
    nextMilestone: Int,
    themeConfig: GardenThemeConfig,
    iconManager: IconManager
) {
    val progress = (currentStreak.toFloat() / nextMilestone).coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PeaceIcon(
                    iconName = themeConfig.icons.milestoneIcon,
                    contentDescription = stringResource(R.string.next_milestone),
                    tint = themeConfig.colors.accent,
                    iconManager = iconManager,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    stringResource(R.string.next_milestone),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.days_format, currentStreak),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    stringResource(R.string.days_format, nextMilestone),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = themeConfig.colors.primary
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = themeConfig.colors.accent,
                trackColor = themeConfig.colors.accent.copy(alpha = 0.2f)
            )

            Text(
                stringResource(R.string.days_until_milestone_format, nextMilestone - currentStreak),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Recent achievements component
 */
@Composable
fun RecentAchievements(
    achievedMilestones: List<Int>,
    themeConfig: GardenThemeConfig,
    iconManager: IconManager
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            stringResource(R.string.achievements),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                achievedMilestones.sortedDescending().forEach { milestone ->
                    AchievementItem(
                        milestone = milestone,
                        themeConfig = themeConfig,
                        iconManager = iconManager
                    )
                }
            }
        }
    }
}

/**
 * Individual achievement item
 */
@Composable
fun AchievementItem(
    milestone: Int,
    themeConfig: GardenThemeConfig,
    iconManager: IconManager
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(themeConfig.colors.primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            PeaceIcon(
                iconName = themeConfig.icons.milestoneIcon,
                contentDescription = stringResource(R.string.milestone_achieved),
                tint = themeConfig.colors.primary,
                iconManager = iconManager,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                stringResource(R.string.milestone_days_format, milestone),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                getMilestoneDescription(milestone),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        PeaceIcon(
            iconName = "checkmark_circle",
            contentDescription = stringResource(R.string.completed),
            tint = themeConfig.colors.accent,
            iconManager = iconManager,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Get a description for a milestone
 */
private fun getMilestoneDescription(milestone: Int): String {
    return when (milestone) {
        7 -> "One week of consistency"
        30 -> "A full month of dedication"
        100 -> "Century of commitment"
        365 -> "A full year of mastery"
        else -> "Milestone achieved"
    }
}
