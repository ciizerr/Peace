package com.nami.peace.ui.components

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nami.peace.domain.model.Attachment
import com.nami.peace.ui.theme.PeaceTheme
import com.nami.peace.util.icon.IconManager

/**
 * Example screen demonstrating the usage of attachment components.
 * Shows how to integrate AttachmentGrid, ImagePickerDialog, and FullScreenImageViewer.
 */
@Composable
fun AttachmentComponentsExampleScreen(
    iconManager: IconManager,
    modifier: Modifier = Modifier
) {
    // Sample attachments for demonstration
    var attachments by remember {
        mutableStateOf(
            listOf(
                Attachment(
                    id = 1,
                    reminderId = 1,
                    filePath = "/path/to/image1.jpg",
                    thumbnailPath = "/path/to/thumb1.jpg",
                    timestamp = System.currentTimeMillis() - 3600000 // 1 hour ago
                ),
                Attachment(
                    id = 2,
                    reminderId = 1,
                    filePath = "/path/to/image2.jpg",
                    thumbnailPath = "/path/to/thumb2.jpg",
                    timestamp = System.currentTimeMillis() - 7200000 // 2 hours ago
                ),
                Attachment(
                    id = 3,
                    reminderId = 1,
                    filePath = "/path/to/image3.jpg",
                    thumbnailPath = "/path/to/thumb3.jpg",
                    timestamp = System.currentTimeMillis() - 86400000 // 1 day ago
                )
            )
        )
    }
    
    var showImagePicker by remember { mutableStateOf(false) }
    var selectedAttachment by remember { mutableStateOf<Attachment?>(null) }
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showImagePicker = true }
            ) {
                PeaceIcon(
                    iconName = "add",
                    contentDescription = "Add attachment",
                    iconManager = iconManager,
                    size = 24.dp
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Attachments",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "${attachments.size} images",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Attachment grid
            AttachmentGrid(
                attachments = attachments,
                onDelete = { attachment ->
                    // Remove attachment from list
                    attachments = attachments.filter { it.id != attachment.id }
                },
                onAttachmentClick = { attachment ->
                    // Open full-screen viewer
                    selectedAttachment = attachment
                },
                iconManager = iconManager
            )
        }
    }
    
    // Image picker dialog
    if (showImagePicker) {
        ImagePickerDialog(
            onDismiss = { showImagePicker = false },
            onImageSelected = { uri ->
                // In a real app, this would call the ViewModel to save the attachment
                // For demo purposes, we'll just add a new attachment to the list
                val newAttachment = Attachment(
                    id = attachments.size + 1,
                    reminderId = 1,
                    filePath = uri.toString(),
                    thumbnailPath = uri.toString(),
                    timestamp = System.currentTimeMillis()
                )
                attachments = attachments + newAttachment
                showImagePicker = false
            },
            iconManager = iconManager
        )
    }
    
    // Full-screen image viewer
    selectedAttachment?.let { attachment ->
        FullScreenImageViewer(
            attachment = attachment,
            onDismiss = { selectedAttachment = null },
            iconManager = iconManager
        )
    }
}

/**
 * Example showing empty state when no attachments exist.
 */
@Composable
fun EmptyAttachmentGridExample(
    iconManager: IconManager,
    modifier: Modifier = Modifier
) {
    var showImagePicker by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "No Attachments",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Empty attachment grid
        AttachmentGrid(
            attachments = emptyList(),
            onDelete = { },
            onAttachmentClick = { },
            iconManager = iconManager
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Add button
        FloatingActionButton(
            onClick = { showImagePicker = true }
        ) {
            PeaceIcon(
                iconName = "add",
                contentDescription = "Add first attachment",
                iconManager = iconManager,
                size = 24.dp
            )
        }
    }
    
    if (showImagePicker) {
        ImagePickerDialog(
            onDismiss = { showImagePicker = false },
            onImageSelected = { uri ->
                // Handle image selection
                showImagePicker = false
            },
            iconManager = iconManager
        )
    }
}

/**
 * Example showing a single attachment item.
 */
@Composable
fun SingleAttachmentItemExample(
    iconManager: IconManager,
    modifier: Modifier = Modifier
) {
    val sampleAttachment = Attachment(
        id = 1,
        reminderId = 1,
        filePath = "/path/to/image.jpg",
        thumbnailPath = "/path/to/thumb.jpg",
        timestamp = System.currentTimeMillis() - 1800000 // 30 minutes ago
    )
    
    var selectedAttachment by remember { mutableStateOf<Attachment?>(null) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Single Attachment",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AttachmentItem(
            attachment = sampleAttachment,
            onDelete = {
                // Handle deletion
            },
            onClick = {
                selectedAttachment = sampleAttachment
            },
            iconManager = iconManager,
            modifier = Modifier.fillMaxWidth(0.5f)
        )
    }
    
    selectedAttachment?.let { attachment ->
        FullScreenImageViewer(
            attachment = attachment,
            onDismiss = { selectedAttachment = null },
            iconManager = iconManager
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AttachmentComponentsExamplePreview() {
    PeaceTheme {
        // Preview requires a mock IconManager
        // In actual usage, inject the real IconManager
    }
}
