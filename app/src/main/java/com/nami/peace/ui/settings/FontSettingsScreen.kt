package com.nami.peace.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nami.peace.R
import com.nami.peace.ui.components.PeaceIcon
import com.nami.peace.util.font.CustomFont
import com.nami.peace.util.icon.IconManager
import com.nami.peace.util.icon.IoniconsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontSettingsScreen(
    onNavigateUp: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val iconManager: IconManager = remember { IoniconsManager(context) }
    
    val selectedFont by viewModel.selectedFont.collectAsState()
    val fontPadding by viewModel.fontPadding.collectAsState()
    val availableFonts by viewModel.availableFonts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Font Settings") },
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
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Font Padding Slider Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Font Padding",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Adjust spacing around text: ${fontPadding}dp",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Slider(
                            value = fontPadding.toFloat(),
                            onValueChange = { viewModel.setFontPadding(it.toInt()) },
                            valueRange = 0f..20f,
                            steps = 19,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Section Header
            item {
                Text(
                    text = "Select Font",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // System Font Option
            item {
                FontOptionItem(
                    fontName = "System Font",
                    fontFamily = FontFamily.Default,
                    previewText = "The quick brown fox jumps over the lazy dog",
                    isSelected = selectedFont == null,
                    onClick = { viewModel.setSelectedFont(null) }
                )
            }

            // Custom Fonts
            items(availableFonts) { customFont ->
                FontOptionItem(
                    fontName = customFont.name,
                    fontFamily = customFont.fontFamily,
                    previewText = customFont.previewText,
                    isSelected = selectedFont == customFont.name,
                    onClick = { viewModel.setSelectedFont(customFont.name) }
                )
            }
        }
    }
}

@Composable
private fun FontOptionItem(
    fontName: String,
    fontFamily: FontFamily,
    previewText: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fontName,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = previewText,
                fontFamily = fontFamily,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
