package com.nami.peace.ui.settings

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nami.peace.R
import com.nami.peace.ui.components.BlurredBackground
import com.nami.peace.ui.components.PeaceIcon
import com.nami.peace.util.icon.IconManager
import com.nami.peace.util.icon.IoniconsManager
import com.nami.peace.util.background.BackgroundImageManager
import kotlinx.coroutines.launch

/**
 * BackgroundSettingsScreen allows users to configure background image settings.
 * 
 * Features:
 * - Background image toggle (currently informational, as images come from attachments)
 * - Blur intensity slider (0-100)
 * - Slideshow toggle
 * - Real-time blur preview
 * 
 * Requirements: 6.1, 6.2, 6.3, 6.5, 6.6
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundSettingsScreen(
    onNavigateUp: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    backgroundImageManager: BackgroundImageManager? = null
) {
    val context = LocalContext.current
    val iconManager: IconManager = remember { IoniconsManager(context) }
    
    val blurIntensity by viewModel.blurIntensity.collectAsState()
    val slideshowEnabled by viewModel.slideshowEnabled.collectAsState()
    
    // For preview purposes, we'll use a sample bitmap if available
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Background Settings") },
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
        // Use BlurredBackground for real-time preview
        BlurredBackground(
            bitmap = previewBitmap,
            blurIntensity = blurIntensity,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PeaceIcon(
                                iconName = "information_circle",
                                contentDescription = "Info",
                                iconManager = iconManager,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Background Images",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            "Background images come from reminder attachments. Add images to your reminders to use them as backgrounds.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Blur Intensity Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PeaceIcon(
                                    iconName = "color_filter",
                                    contentDescription = "Blur",
                                    iconManager = iconManager
                                )
                                Text(
                                    "Blur Intensity",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Text(
                                "$blurIntensity",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Slider(
                            value = blurIntensity.toFloat(),
                            onValueChange = { viewModel.setBlurIntensity(it.toInt()) },
                            valueRange = 0f..100f,
                            steps = 99,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "0 (Clear)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "100 (Maximum)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Text(
                            "Adjust how blurred background images appear. Higher values create more blur effect.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Slideshow Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            PeaceIcon(
                                iconName = "images",
                                contentDescription = "Slideshow",
                                iconManager = iconManager
                            )
                            Column {
                                Text(
                                    "Slideshow Mode",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "Cycle through images every 5 seconds",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Switch(
                            checked = slideshowEnabled,
                            onCheckedChange = { viewModel.setSlideshowEnabled(it) }
                        )
                    }
                }
                
                // Preview Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PeaceIcon(
                                iconName = "eye",
                                contentDescription = "Preview",
                                iconManager = iconManager,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                "Live Preview",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Text(
                            "The blur effect you see on this screen is a live preview. Adjust the slider above to see changes in real-time.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
