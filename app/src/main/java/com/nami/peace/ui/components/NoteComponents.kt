package com.nami.peace.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.nami.peace.domain.model.Note
import com.nami.peace.util.icon.IconManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A single note item with timestamp and delete button.
 * Displays note content with formatted timestamp.
 *
 * @param note The note to display
 * @param onDelete Callback when delete button is clicked
 * @param iconManager IconManager for loading icons
 * @param modifier Modifier for the item
 */
@Composable
fun NoteItem(
    note: Note,
    onDelete: () -> Unit,
    iconManager: IconManager,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with timestamp and delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timestamp
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PeaceIcon(
                        iconName = "time",
                        contentDescription = "Note timestamp",
                        iconManager = iconManager,
                        size = 16.dp,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = formatTimestamp(note.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                
                // Delete button
                IconButton(
                    onClick = { showDeleteConfirmation = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    PeaceIcon(
                        iconName = "trash",
                        contentDescription = "Delete note",
                        iconManager = iconManager,
                        size = 20.dp,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Note content
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        DeleteNoteConfirmationDialog(
            onDismiss = { showDeleteConfirmation = false },
            onConfirm = {
                showDeleteConfirmation = false
                onDelete()
            },
            iconManager = iconManager
        )
    }
}

/**
 * A list of notes displayed in chronological order.
 * Shows all notes for a reminder with timestamps.
 *
 * @param notes List of notes to display (should be sorted chronologically)
 * @param onDelete Callback when a note is deleted
 * @param iconManager IconManager for loading icons
 * @param modifier Modifier for the list
 */
@Composable
fun NoteList(
    notes: List<Note>,
    onDelete: (Note) -> Unit,
    iconManager: IconManager,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (notes.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    PeaceIcon(
                        iconName = "document_text",
                        contentDescription = "No notes",
                        iconManager = iconManager,
                        size = 48.dp,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "No notes yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = notes,
                    key = { note -> note.id }
                ) { note ->
                    AnimatedVisibility(
                        visible = true,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        NoteItem(
                            note = note,
                            onDelete = { onDelete(note) },
                            iconManager = iconManager
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog for adding a new note.
 * Provides a multi-line text input field and action buttons.
 *
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when note is confirmed with the entered content
 * @param iconManager IconManager for loading icons
 */
@Composable
fun AddNoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    iconManager: IconManager
) {
    var noteContent by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Dialog title with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PeaceIcon(
                        iconName = "create",
                        contentDescription = "Add note",
                        iconManager = iconManager,
                        size = 24.dp,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Add Note",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Multi-line text input field
                androidx.compose.material3.OutlinedTextField(
                    value = noteContent,
                    onValueChange = {
                        noteContent = it
                        showError = false
                    },
                    label = { Text("Note content") },
                    placeholder = { Text("Enter your note here...") },
                    isError = showError,
                    supportingText = if (showError) {
                        { Text("Note cannot be empty") }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    minLines = 5,
                    maxLines = 8,
                    colors = androidx.compose.material3.TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    TextButton(
                        onClick = {
                            if (noteContent.isBlank()) {
                                showError = true
                            } else {
                                onConfirm(noteContent.trim())
                            }
                        }
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

/**
 * Confirmation dialog for deleting a note.
 * Displays a warning message and action buttons.
 *
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when deletion is confirmed
 * @param iconManager IconManager for loading icons
 */
@Composable
private fun DeleteNoteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    iconManager: IconManager
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Dialog title with warning icon
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PeaceIcon(
                        iconName = "alert_circle",
                        contentDescription = "Warning",
                        iconManager = iconManager,
                        size = 24.dp,
                        tint = MaterialTheme.colorScheme.error
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Delete Note",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Warning message
                Text(
                    text = "Are you sure you want to delete this note? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    TextButton(
                        onClick = onConfirm,
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}

/**
 * Format a timestamp to a human-readable string.
 * Shows relative time for recent notes, absolute time for older notes.
 *
 * @param timestamp The timestamp in milliseconds
 * @return Formatted timestamp string
 */
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        diff < 604800_000 -> "${diff / 86400_000}d ago"
        else -> {
            val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            dateFormat.format(Date(timestamp))
        }
    }
}
