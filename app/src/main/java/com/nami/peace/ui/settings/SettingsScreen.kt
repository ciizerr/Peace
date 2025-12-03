package com.nami.peace.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nami.peace.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToFontSettings: () -> Unit = {},
    onNavigateToBackgroundSettings: () -> Unit = {},
    onNavigateToLanguageSettings: () -> Unit = {},
    onNavigateToCalendarSync: () -> Unit = {},
    onNavigateToPeaceGarden: () -> Unit = {},
    onNavigateToMLSuggestions: () -> Unit = {},
    onNavigateToFeatureSettings: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    // Scheduling mode state (can be added to ViewModel later)
    var schedulingMode by remember { mutableStateOf("FLEXIBLE") }

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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            IconButton(
                                onClick = onNavigateUp,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Text(
                                "Settings",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.W600
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
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
                // Alarm Behavior Section
                item {
                    SettingsSectionCard(
                        icon = Icons.Outlined.Schedule,
                        title = "Alarm Behavior",
                        iconColor = SerenityBlue
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                "Scheduling Mode",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.W500
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            SchedulingModeOption(
                                title = "Flexible (Drift)",
                                description = "Next alarm is calculated from when you respond. Good for workouts.",
                                isSelected = schedulingMode == "FLEXIBLE",
                                onClick = { schedulingMode = "FLEXIBLE" }
                            )

                            SchedulingModeOption(
                                title = "Strict (Anchored)",
                                description = "Alarms lock to the original schedule (e.g. 6:00, 6:15). Good for meds.",
                                isSelected = schedulingMode == "STRICT",
                                onClick = { schedulingMode = "STRICT" }
                            )
                        }
                    }
                }

                // Appearance Section
                item {
                    SettingsSectionCard(
                        icon = Icons.Outlined.Palette,
                        title = "Appearance",
                        iconColor = MintGlow
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SettingsItem(
                                icon = "🌓",
                                title = "Theme",
                                subtitle = "Light / Dark / Auto",
                                onClick = { /* TODO: Theme settings */ },
                                showArrow = true
                            )
                            
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            
                            SettingsItem(
                                icon = "✍️",
                                title = "Font",
                                subtitle = "Customize typography",
                                onClick = onNavigateToFontSettings,
                                showArrow = true
                            )
                            
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            
                            SettingsItem(
                                icon = "🎨",
                                title = "Background",
                                subtitle = "Set custom backgrounds",
                                onClick = onNavigateToBackgroundSettings,
                                showArrow = true
                            )
                            
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            
                            SettingsItem(
                                icon = "🌍",
                                title = "Language",
                                subtitle = "Change app language",
                                onClick = onNavigateToLanguageSettings,
                                showArrow = true
                            )
                        }
                    }
                }

                // Data & Privacy Section
                item {
                    SettingsSectionCard(
                        icon = Icons.Outlined.Security,
                        title = "Data & Privacy",
                        iconColor = CalmTeal
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SettingsItem(
                                icon = "📊",
                                title = "History Log",
                                subtitle = "View completed tasks",
                                onClick = onNavigateToHistory,
                                showArrow = true
                            )
                            
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            
                            SettingsItem(
                                icon = "🔑",
                                title = "API Key",
                                subtitle = "Manage Gemini API key",
                                onClick = { /* TODO */ }
                            )
                            
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            
                            SettingsItem(
                                icon = "📅",
                                title = "Calendar Sync",
                                subtitle = "Google Calendar integration",
                                onClick = onNavigateToCalendarSync,
                                showArrow = true
                            )
                        }
                    }
                }

                // Features Section
                item {
                    SettingsSectionCard(
                        icon = Icons.Outlined.Tune,
                        title = "Features",
                        iconColor = PriorityMedium
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SettingsItem(
                                icon = "🤖",
                                title = "ML Suggestions",
                                subtitle = "Intelligent recommendations",
                                onClick = onNavigateToMLSuggestions,
                                showArrow = true
                            )
                            
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            
                            SettingsItem(
                                icon = "🌱",
                                title = "Peace Garden",
                                subtitle = "View your progress",
                                onClick = onNavigateToPeaceGarden,
                                showArrow = true
                            )
                            
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            
                            SettingsItem(
                                icon = "⚙️",
                                title = "Feature Settings",
                                subtitle = "Toggle advanced features",
                                onClick = onNavigateToFeatureSettings,
                                showArrow = true
                            )
                        }
                    }
                }

                // About Section
                item {
                    SettingsSectionCard(
                        icon = Icons.Outlined.Info,
                        title = "About",
                        iconColor = SerenityBlue
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SettingsItem(
                                icon = "ℹ️",
                                title = "Version",
                                subtitle = "2.0.0 (Alpha)",
                                onClick = { }
                            )
                            
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            
                            SettingsItem(
                                icon = "📖",
                                title = "User Guide",
                                subtitle = "Learn how to use Peace",
                                onClick = { /* TODO: Open user guide */ },
                                showArrow = true
                            )
                            
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            
                            SettingsItem(
                                icon = "💜",
                                title = "Rate Us",
                                subtitle = "Share your feedback",
                                onClick = { /* TODO: Open Play Store */ },
                                showArrow = true
                            )
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun SettingsSectionCard(
    icon: ImageVector,
    title: String,
    iconColor: Color,
    content: @Composable ColumnScope.() -> Unit
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.W600
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            content()
        }
    }
}

@Composable
fun SchedulingModeOption(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                SerenityBlue.copy(alpha = 0.12f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(2.dp, SerenityBlue)
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = SerenityBlue
                )
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isSelected) FontWeight.W600 else FontWeight.W500
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showArrow: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            icon,
            style = MaterialTheme.typography.headlineSmall
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.W500
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (showArrow) {
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SettingsToggleItem(
    icon: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            icon,
            style = MaterialTheme.typography.headlineSmall
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.W500
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SerenityBlue,
                checkedTrackColor = SerenityBlue.copy(alpha = 0.5f),
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}
