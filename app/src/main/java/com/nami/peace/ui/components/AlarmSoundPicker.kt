package com.nami.peace.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.nami.peace.R
import com.nami.peace.domain.model.AlarmSound
import com.nami.peace.util.icon.IoniconsManager

/**
 * Dialog for selecting an alarm sound.
 *
 * @param currentSound The currently selected alarm sound (null for default)
 * @param availableSounds List of available system sounds
 * @param onSoundSelected Callback when a sound is selected
 * @param onDismiss Callback when dialog is dismissed
 * @param onPlayPreview Callback to play a preview of the sound
 * @param onStopPreview Callback to stop the preview
 * @param onPickCustomSound Callback to pick a custom sound file
 */
@Composable
fun AlarmSoundPickerDialog(
    currentSound: AlarmSound?,
    availableSounds: List<AlarmSound>,
    onSoundSelected: (AlarmSound?) -> Unit,
    onDismiss: () -> Unit,
    onPlayPreview: (AlarmSound) -> Unit,
    onStopPreview: () -> Unit,
    onPickCustomSound: (Uri, String) -> Unit,
    iconManager: IoniconsManager
) {
    var playingSound by remember { mutableStateOf<AlarmSound?>(null) }
    
    // File picker for custom sounds
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Extract filename from URI
            val fileName = uri.lastPathSegment ?: "Custom Sound"
            onPickCustomSound(uri, fileName)
        }
    }
    
    Dialog(onDismissRequest = {
        onStopPreview()
        onDismiss()
    }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Text(
                    text = stringResource(R.string.select_alarm_sound),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Sound list
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Default sound option
                    item {
                        AlarmSoundItem(
                            sound = null,
                            isSelected = currentSound == null,
                            isPlaying = playingSound == null && playingSound != null,
                            onSelect = {
                                onStopPreview()
                                playingSound = null
                                onSoundSelected(null)
                            },
                            onPlayPreview = {
                                // Can't preview null, so do nothing
                            },
                            iconManager = iconManager
                        )
                    }
                    
                    // Available system sounds
                    items(availableSounds) { sound ->
                        AlarmSoundItem(
                            sound = sound,
                            isSelected = currentSound?.id == sound.id,
                            isPlaying = playingSound?.id == sound.id,
                            onSelect = {
                                onStopPreview()
                                playingSound = null
                                onSoundSelected(sound)
                            },
                            onPlayPreview = {
                                if (playingSound?.id == sound.id) {
                                    onStopPreview()
                                    playingSound = null
                                } else {
                                    onStopPreview()
                                    onPlayPreview(sound)
                                    playingSound = sound
                                }
                            },
                            iconManager = iconManager
                        )
                    }
                    
                    // Custom sound picker button
                    item {
                        OutlinedButton(
                            onClick = {
                                onStopPreview()
                                playingSound = null
                                filePicker.launch("audio/*")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(
                                    iconManager.getIcon("folder-open") ?: iconManager.getFallbackIcon("folder-open")
                                ),
                                contentDescription = stringResource(R.string.cd_pick_custom_sound)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.pick_custom_sound))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Close button
                Button(
                    onClick = {
                        onStopPreview()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.close))
                }
            }
        }
    }
}

/**
 * Individual alarm sound item in the picker.
 */
@Composable
private fun AlarmSoundItem(
    sound: AlarmSound?,
    isSelected: Boolean,
    isPlaying: Boolean,
    onSelect: () -> Unit,
    onPlayPreview: () -> Unit,
    iconManager: IoniconsManager
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection indicator
            RadioButton(
                selected = isSelected,
                onClick = { onSelect() }
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Sound name
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sound?.name ?: "Default Alarm",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                if (sound != null && !sound.isSystem) {
                    Text(
                        text = "Custom",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Play/Stop preview button
            if (sound != null) {
                IconButton(onClick = onPlayPreview) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(
                            if (isPlaying) {
                                iconManager.getIcon("stop_circle") ?: iconManager.getFallbackIcon("stop_circle")
                            } else {
                                iconManager.getIcon("play_circle") ?: iconManager.getFallbackIcon("play_circle")
                            }
                        ),
                        contentDescription = if (isPlaying) {
                            "Stop preview"
                        } else {
                            "Play preview"
                        },
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
