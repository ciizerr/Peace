package com.nami.peace.ui.settings

import android.app.Activity
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nami.peace.R
import com.nami.peace.domain.model.Language
import com.nami.peace.ui.components.PeaceIcon
import com.nami.peace.util.icon.IconManager
import com.nami.peace.util.icon.IoniconsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettingsScreen(
    onNavigateUp: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val iconManager: IconManager = remember { IoniconsManager(context) }
    
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val availableLanguages by viewModel.availableLanguages.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Language Settings") },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Section Header
            item {
                Text(
                    text = "Select Language",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Language Options
            items(availableLanguages) { language ->
                LanguageOptionItem(
                    language = language,
                    isSelected = currentLanguage.code == language.code,
                    onClick = {
                        viewModel.setLanguage(language)
                        // Recreate activity for immediate language application
                        activity?.recreate()
                    }
                )
            }
        }
    }
}

@Composable
private fun LanguageOptionItem(
    language: Language,
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
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = language.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                if (language.nativeName != language.displayName) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = language.nativeName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
