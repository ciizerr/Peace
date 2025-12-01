package com.nami.peace.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nami.peace.R
import com.nami.peace.ui.components.BackgroundWrapper
import com.nami.peace.ui.components.PeaceIcon
import com.nami.peace.ui.home.HomeViewModel
import com.nami.peace.util.background.BackgroundImageManager
import com.nami.peace.util.feature.FeatureToggleManager
import com.nami.peace.util.icon.IconManager
import com.nami.peace.util.icon.IoniconsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureSettingsScreen(
    onNavigateUp: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    backgroundImageManager: BackgroundImageManager? = null
) {
    val context = LocalContext.current
    val iconManager: IconManager = remember { IoniconsManager(context) }
    
    // Background settings
    val blurIntensity by viewModel.blurIntensity.collectAsState()
    val slideshowEnabled by viewModel.slideshowEnabled.collectAsState()
    val allAttachments by homeViewModel.allAttachments.collectAsState()
    
    // Feature toggle states
    val subtasksEnabled by viewModel.subtasksEnabled.collectAsState()
    val attachmentsEnabled by viewModel.attachmentsEnabled.collectAsState()
    val widgetsEnabled by viewModel.widgetsEnabled.collectAsState()
    val mlSuggestionsEnabled by viewModel.mlSuggestionsEnabled.collectAsState()
    val calendarSyncEnabled by viewModel.calendarSyncEnabled.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.feature_settings)) },
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
        BackgroundWrapper(
            attachments = allAttachments,
            blurIntensity = blurIntensity,
            slideshowEnabled = slideshowEnabled,
            backgroundImageManager = backgroundImageManager,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.features_section),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "Enable or disable features to customize your Peace experience. Changes take effect immediately.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Subtasks Feature
                FeatureToggleItem(
                    iconName = "checkbox",
                    title = stringResource(R.string.feature_subtasks),
                    description = stringResource(R.string.feature_subtasks_desc),
                    enabled = subtasksEnabled,
                    onToggle = { viewModel.setFeatureEnabled(FeatureToggleManager.Feature.SUBTASKS, it) },
                    iconManager = iconManager
                )
                
                HorizontalDivider()
                
                // Attachments Feature
                FeatureToggleItem(
                    iconName = "attach",
                    title = stringResource(R.string.feature_attachments),
                    description = stringResource(R.string.feature_attachments_desc),
                    enabled = attachmentsEnabled,
                    onToggle = { viewModel.setFeatureEnabled(FeatureToggleManager.Feature.ATTACHMENTS, it) },
                    iconManager = iconManager
                )
                
                HorizontalDivider()
                
                // Widgets Feature
                FeatureToggleItem(
                    iconName = "apps",
                    title = stringResource(R.string.feature_widgets),
                    description = stringResource(R.string.feature_widgets_desc),
                    enabled = widgetsEnabled,
                    onToggle = { viewModel.setFeatureEnabled(FeatureToggleManager.Feature.WIDGETS, it) },
                    iconManager = iconManager
                )
                
                HorizontalDivider()
                
                // ML Suggestions Feature
                FeatureToggleItem(
                    iconName = "bulb",
                    title = stringResource(R.string.feature_ml_suggestions),
                    description = stringResource(R.string.feature_ml_suggestions_desc),
                    enabled = mlSuggestionsEnabled,
                    onToggle = { viewModel.setFeatureEnabled(FeatureToggleManager.Feature.ML_SUGGESTIONS, it) },
                    iconManager = iconManager
                )
                
                HorizontalDivider()
                
                // Calendar Sync Feature
                FeatureToggleItem(
                    iconName = "calendar",
                    title = stringResource(R.string.feature_calendar_sync),
                    description = stringResource(R.string.feature_calendar_sync_desc),
                    enabled = calendarSyncEnabled,
                    onToggle = { viewModel.setFeatureEnabled(FeatureToggleManager.Feature.CALENDAR_SYNC, it) },
                    iconManager = iconManager
                )
            }
        }
    }
}

@Composable
private fun FeatureToggleItem(
    iconName: String,
    title: String,
    description: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    iconManager: IconManager
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PeaceIcon(
                iconName = iconName,
                contentDescription = title,
                iconManager = iconManager,
                modifier = Modifier.size(32.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Switch(
            checked = enabled,
            onCheckedChange = onToggle
        )
    }
}
