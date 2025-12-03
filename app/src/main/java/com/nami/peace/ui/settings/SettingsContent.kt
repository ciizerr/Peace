package com.nami.peace.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        SettingsCategory.Overview -> SettingsScreen(
            onNavigateUp = onNavigateToDashboard,
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToCategory = onNavigateToCategory
        )
        SettingsCategory.Appearance -> PlaceholderScreen(
            title = "Appearance Settings",
            subtitle = "Customize the look and feel of the app.",
            onBack = { onNavigateToCategory(SettingsCategory.Overview) }
        )
        SettingsCategory.NavStyle -> PlaceholderScreen(
            title = "Navigation Style",
            subtitle = "Choose your preferred navigation bar style.",
            onBack = { onNavigateToCategory(SettingsCategory.Overview) }
        )
        SettingsCategory.ShadowBlur -> ShadowBlurSettingsScreen(
            onBack = { onNavigateToCategory(SettingsCategory.Overview) }
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
                    Text("About Peace", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                    androidx.compose.material3.Button(onClick = onNavigateToHistory) {
                        Text("View History Log")
                    }
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                    androidx.compose.material3.OutlinedButton(onClick = { onNavigateToCategory(SettingsCategory.Overview) }) {
                        Text("Back to Settings")
                    }
                }
            }
        }
    }
}
