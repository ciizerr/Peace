package com.nami.peace.ui.settings

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.TopAppBar
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShadowBlurSettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val blurEnabled by viewModel.blurEnabled.collectAsState()
    val shadowsEnabled by viewModel.shadowsEnabled.collectAsState()
    val blurStrength by viewModel.blurStrength.collectAsState()
    val blurTintAlpha by viewModel.blurTintAlpha.collectAsState()
    val shadowStyle by viewModel.shadowStyle.collectAsState()
    val isBlurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shadow & Blur") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Blur Section
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Blur Effect",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable Blur (Android 12+)",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = if (isBlurSupported) {
                                "Frosted glass effect behind navigation bars"
                            } else {
                                "Requires Android 12+. Using semi-transparent fallback."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isBlurSupported) {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                    Switch(
                        checked = blurEnabled && isBlurSupported,
                        onCheckedChange = { viewModel.setBlurEnabled(it) },
                        enabled = isBlurSupported
                    )
                }

                if (blurEnabled && isBlurSupported) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Blur Strength",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${blurStrength.toInt()}px",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = blurStrength,
                            onValueChange = { viewModel.setBlurStrength(it) },
                            valueRange = 5f..30f,
                            steps = 24
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tint Opacity",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${(blurTintAlpha * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = blurTintAlpha,
                            onValueChange = { viewModel.setBlurTintAlpha(it) },
                            valueRange = 0f..1f,
                            steps = 19 // 5% increments
                        )
                        Text(
                            text = "Adjusts the opacity of the glass tint",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            HorizontalDivider()

            // Shadow Section
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Shadow Style",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Enable Shadows",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = shadowsEnabled,
                        onCheckedChange = { viewModel.setShadowsEnabled(it) }
                    )
                }

                if (shadowsEnabled) {
                    Column {
                        Text("Intensity", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        val options = listOf("None", "Subtle", "Medium", "Heavy")
                        options.forEach { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setShadowStyle(option) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = shadowStyle == option,
                                    onClick = { viewModel.setShadowStyle(option) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = option)
                            }
                        }
                    }
                }
            }
        }
    }
}
