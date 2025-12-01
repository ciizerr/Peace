package com.nami.peace.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nami.peace.domain.model.Attachment
import com.nami.peace.util.icon.IconManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A single attachment item displaying a thumbnail with delete button.
 * Clicking the thumbnail opens the full-screen image viewer.
 *
 * @param attachment The attachment to display
 * @param onDelete Callback when delete button is clicked
 * @param onClick Callback when thumbnail is clicked
 * @param iconManager IconManager for loading icons
 * @param modifier Modifier for the item
 */
@Composable
fun AttachmentItem(
    attachment: Attachment,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    iconManager: IconManager,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Thumbnail image
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(File(attachment.thumbnailPath))
                    .crossfade(true)
                    .build(),
                contentDescription = "Attachment thumbnail",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Delete button overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(
                    onClick = { showDeleteConfirmation = true },
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    PeaceIcon(
                        iconName = "trash",
                        contentDescription = "Delete attachment",
                        iconManager = iconManager,
                        size = 18.dp,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Timestamp overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    text = formatTimestamp(attachment.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        DeleteAttachmentConfirmationDialog(
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
 * A grid of attachment thumbnails displayed in chronological order.
 * Shows all attachments for a reminder in a 2-column grid.
 *
 * @param attachments List of attachments to display (should be sorted chronologically)
 * @param onDelete Callback when an attachment is deleted
 * @param onAttachmentClick Callback when an attachment is clicked
 * @param iconManager IconManager for loading icons
 * @param modifier Modifier for the grid
 */
@Composable
fun AttachmentGrid(
    attachments: List<Attachment>,
    onDelete: (Attachment) -> Unit,
    onAttachmentClick: (Attachment) -> Unit,
    iconManager: IconManager,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (attachments.isEmpty()) {
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
                        iconName = "image",
                        contentDescription = "No attachments",
                        iconManager = iconManager,
                        size = 48.dp,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "No attachments yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(
                    items = attachments,
                    key = { attachment -> attachment.id }
                ) { attachment ->
                    AnimatedVisibility(
                        visible = true,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        AttachmentItem(
                            attachment = attachment,
                            onDelete = { onDelete(attachment) },
                            onClick = { onAttachmentClick(attachment) },
                            iconManager = iconManager
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog for picking an image from the device.
 * Uses the system image picker to select an image.
 *
 * @param onDismiss Callback when dialog is dismissed
 * @param onImageSelected Callback when an image is selected with the URI
 * @param iconManager IconManager for loading icons
 */
@Composable
fun ImagePickerDialog(
    onDismiss: () -> Unit,
    onImageSelected: (Uri) -> Unit,
    iconManager: IconManager
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onImageSelected(uri)
        }
        onDismiss()
    }
    
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
                        iconName = "image",
                        contentDescription = "Add image",
                        iconManager = iconManager,
                        size = 24.dp,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Add Image",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Description
                Text(
                    text = "Select an image from your device to attach to this reminder.",
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
                        onClick = {
                            launcher.launch("image/*")
                        }
                    ) {
                        Text("Select Image")
                    }
                }
            }
        }
    }
}

/**
 * Full-screen image viewer dialog.
 * Displays the full-resolution image with close button.
 *
 * @param attachment The attachment to display
 * @param onDismiss Callback when dialog is dismissed
 * @param iconManager IconManager for loading icons
 */
@Composable
fun FullScreenImageViewer(
    attachment: Attachment,
    onDismiss: () -> Unit,
    iconManager: IconManager
) {
    val context = LocalContext.current
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Full-resolution image
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(File(attachment.filePath))
                    .crossfade(true)
                    .build(),
                contentDescription = "Full-screen attachment",
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onDismiss),
                contentScale = ContentScale.Fit
            )
            
            // Close button
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    PeaceIcon(
                        iconName = "close",
                        contentDescription = "Close",
                        iconManager = iconManager,
                        size = 24.dp,
                        tint = Color.White
                    )
                }
            }
            
            // Timestamp info
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    text = formatTimestamp(attachment.timestamp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

/**
 * Confirmation dialog for deleting an attachment.
 * Displays a warning message and action buttons.
 *
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when deletion is confirmed
 * @param iconManager IconManager for loading icons
 */
@Composable
private fun DeleteAttachmentConfirmationDialog(
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
                        text = "Delete Attachment",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Warning message
                Text(
                    text = "Are you sure you want to delete this attachment? This action cannot be undone.",
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
 * Shows relative time for recent attachments, absolute time for older attachments.
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
