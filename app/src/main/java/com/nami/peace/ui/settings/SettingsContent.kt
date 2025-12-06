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

@Composable
fun SettingsContent(
    category: SettingsCategory,
    onNavigateToHistory: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToCategory: (SettingsCategory) -> Unit
) {
    when (category) {
        SettingsCategory.Appearance -> PlaceholderScreen(
            title = stringResource(com.nami.peace.R.string.settings_appearance_title),
            subtitle = stringResource(com.nami.peace.R.string.settings_appearance_subtitle),
            onBack = onNavigateToDashboard
        )
        SettingsCategory.NavStyle -> PlaceholderScreen(
            title = stringResource(com.nami.peace.R.string.settings_nav_style_title),
            subtitle = stringResource(com.nami.peace.R.string.settings_nav_style_subtitle),
            onBack = onNavigateToDashboard
        )
        SettingsCategory.ShadowBlur -> ShadowBlurSettingsScreen(
            onBack = onNavigateToDashboard
        )
        SettingsCategory.About -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.layout.Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    Text(stringResource(com.nami.peace.R.string.pref_about), style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                    androidx.compose.material3.Button(onClick = onNavigateToHistory) {
                        Text(stringResource(com.nami.peace.R.string.view_history_log))
                    }
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                    androidx.compose.material3.OutlinedButton(onClick = onNavigateToDashboard) {
                        Text(stringResource(com.nami.peace.R.string.settings_back_to_main))
                    }
                }
            }
        }
    }
}
