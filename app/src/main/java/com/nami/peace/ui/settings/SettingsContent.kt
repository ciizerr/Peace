package com.nami.peace.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.nami.peace.ui.components.PlaceholderScreen
import com.nami.peace.ui.components.SettingsCategory

import dev.chrisbanes.haze.HazeState

@Composable
fun SettingsContent(
    category: SettingsCategory,
    onNavigateToHistory: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToCategory: (SettingsCategory) -> Unit,
    hazeState: HazeState? = null
) {
    when (category) {
        SettingsCategory.Appearance -> com.nami.peace.ui.settings.appearance.AppearanceScreen(
            onNavigateBack = onNavigateToDashboard,
            hazeState = hazeState
        )
        SettingsCategory.Identity -> PlaceholderScreen(
            title = stringResource(com.nami.peace.R.string.title_identity),
            subtitle = stringResource(com.nami.peace.R.string.subtitle_identity),
            onBack = onNavigateToDashboard
        )
        SettingsCategory.Rhythms -> PlaceholderScreen(
            title = stringResource(com.nami.peace.R.string.title_rhythms),
            subtitle = stringResource(com.nami.peace.R.string.subtitle_rhythms),
            onBack = onNavigateToDashboard
        )
        SettingsCategory.Sanctuary -> PlaceholderScreen(
            title = stringResource(com.nami.peace.R.string.title_sanctuary),
            subtitle = stringResource(com.nami.peace.R.string.subtitle_sanctuary),
            onBack = onNavigateToDashboard
        )
        SettingsCategory.Wisdom -> AboutSettingsScreen(
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToDashboard = onNavigateToDashboard,
            hazeState = hazeState
        )
    }
}
