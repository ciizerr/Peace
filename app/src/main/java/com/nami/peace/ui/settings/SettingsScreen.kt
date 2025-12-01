package com.nami.peace.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import com.nami.peace.ui.components.BackgroundWrapper
import com.nami.peace.ui.components.PeaceIcon
import com.nami.peace.util.background.BackgroundImageManager
import com.nami.peace.util.icon.IconManager
import com.nami.peace.util.icon.IoniconsManager
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.nami.peace.R
import com.nami.peace.ui.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToFontSettings: () -> Unit,
    onNavigateToBackgroundSettings: () -> Unit,
    onNavigateToLanguageSettings: () -> Unit,
    onNavigateToCalendarSync: () -> Unit = {},
    onNavigateToPeaceGarden: () -> Unit = {},
    onNavigateToMLSuggestions: () -> Unit = {},
    onNavigateToFeatureSettings: () -> Unit = {},
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
    
    // Feature toggles - get from settings view model
    val mlSuggestionsEnabled by viewModel.mlSuggestionsEnabled.collectAsState()
    val calendarSyncEnabled by viewModel.calendarSyncEnabled.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
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
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
            // Appearance Section
            Text(
                "Appearance",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Button(
                onClick = onNavigateToLanguageSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                PeaceIcon(
                    iconName = "language",
                    contentDescription = "Language Settings",
                    iconManager = iconManager
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Language Settings")
            }

            Button(
                onClick = onNavigateToFontSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                PeaceIcon(
                    iconName = "text",
                    contentDescription = "Font Settings",
                    iconManager = iconManager
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Font Settings")
            }

            Button(
                onClick = onNavigateToBackgroundSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                PeaceIcon(
                    iconName = "image",
                    contentDescription = "Background Settings",
                    iconManager = iconManager
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Background Settings")
            }

            HorizontalDivider()

            // Garden Section
            Text(
                "Garden",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Button(
                onClick = onNavigateToPeaceGarden,
                modifier = Modifier.fillMaxWidth()
            ) {
                PeaceIcon(
                    iconName = "leaf",
                    contentDescription = stringResource(R.string.peace_garden),
                    iconManager = iconManager
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.peace_garden))
            }

            HorizontalDivider()

            // Integration Section (only show if calendar sync is enabled)
            if (calendarSyncEnabled) {
                Text(
                    "Integration",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Button(
                    onClick = onNavigateToCalendarSync,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PeaceIcon(
                        iconName = "calendar",
                        contentDescription = stringResource(R.string.cd_calendar_sync),
                        iconManager = iconManager
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.calendar_sync))
                }

                HorizontalDivider()
            }

            // Intelligence Section (only show if ML suggestions is enabled)
            if (mlSuggestionsEnabled) {
                Text(
                    "Intelligence",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Button(
                    onClick = onNavigateToMLSuggestions,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PeaceIcon(
                        iconName = "bulb",
                        contentDescription = stringResource(R.string.cd_ml_suggestions),
                        iconManager = iconManager
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.ml_suggestions))
                }

                HorizontalDivider()
            }

            // Features Section
            Text(
                stringResource(R.string.features_section),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Button(
                onClick = onNavigateToFeatureSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                PeaceIcon(
                    iconName = "toggle",
                    contentDescription = stringResource(R.string.cd_feature_settings),
                    iconManager = iconManager
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.feature_settings))
            }

            HorizontalDivider()

            // Data Section
            Text(
                stringResource(R.string.data),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Button(
                onClick = onNavigateToHistory,
                modifier = Modifier.fillMaxWidth()
            ) {
                PeaceIcon(
                    iconName = "time",
                    contentDescription = stringResource(R.string.cd_history),
                    iconManager = iconManager
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.view_history_log))
            }
            }
        }
    }
}
