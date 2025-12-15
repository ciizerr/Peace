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
        SettingsCategory.Identity -> com.nami.peace.ui.settings.identity.IdentityScreen(
            onNavigateBack = onNavigateToDashboard,
            hazeState = hazeState ?: dev.chrisbanes.haze.HazeState()
        )
        SettingsCategory.Rhythms -> com.nami.peace.ui.settings.rhythms.RhythmsScreen(
            onNavigateBack = onNavigateToDashboard,
            hazeState = hazeState ?: dev.chrisbanes.haze.HazeState()
        )
        SettingsCategory.Sanctuary -> com.nami.peace.ui.settings.sanctuary.SanctuaryScreen(
            onNavigateBack = onNavigateToDashboard,
            onNavigateToHistory = onNavigateToHistory,
            hazeState = hazeState ?: dev.chrisbanes.haze.HazeState()
        )
        SettingsCategory.Wisdom -> AboutSettingsScreen(
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToDashboard = onNavigateToDashboard,
            hazeState = hazeState
        )
    }
}
