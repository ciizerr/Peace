# Attachment Components

This file contains reusable Compose UI components for displaying and managing image attachments in the Peace app.

## Components

### AttachmentItem
A single attachment item displaying a thumbnail with delete button and timestamp overlay.

**Features:**
- Displays thumbnail image using Coil
- Delete button with confirmation dialog
- Timestamp overlay showing when the attachment was added
- Clickable to open full-screen viewer
- Smooth animations

**Usage:**
```kotlin
AttachmentItem(
    attachment = attachment,
    onDelete = { /* Handle deletion */ },
    onClick = { /* Open full-screen viewer */ },
    iconManager = iconManager
)
```

### AttachmentGrid
A grid of attachment thumbnails displayed in chronological order.

**Features:**
- 2-column grid layout
- Empty state when no attachments
- Smooth animations for add/remove
- Chronological ordering (oldest to newest)

**Usage:**
```kotlin
AttachmentGrid(
    attachments = attachmentList,
    onDelete = { attachment -> /* Handle deletion */ },
    onAttachmentClick = { attachment -> /* Open viewer */ },
    iconManager = iconManager
)
```

### ImagePickerDialog
Dialog for picking an image from the device using the system image picker.

**Features:**
- Uses Android's built-in image picker
- Filters to show only images
- Returns selected image URI
- Clean, simple UI

**Usage:**
```kotlin
var showImagePicker by remember { mutableStateOf(false) }

if (showImagePicker) {
    ImagePickerDialog(
        onDismiss = { showImagePicker = false },
        onImageSelected = { uri ->
            // Handle selected image URI
            showImagePicker = false
        },
        iconManager = iconManager
    )
}
```

### FullScreenImageViewer
Full-screen image viewer dialog for viewing attachments at full resolution.

**Features:**
- Displays full-resolution image
- Black background for better viewing
- Close button overlay
- Timestamp info overlay
- Tap anywhere to close
- Uses Coil for efficient image loading

**Usage:**
```kotlin
var selectedAttachment by remember { mutableStateOf<Attachment?>(null) }

selectedAttachment?.let { attachment ->
    FullScreenImageViewer(
        attachment = attachment,
        onDismiss = { selectedAttachment = null },
        iconManager = iconManager
    )
}
```

## Integration Example

Here's a complete example of integrating attachment components into a screen:

```kotlin
@Composable
fun ReminderDetailScreen(
    reminderId: Int,
    viewModel: ReminderViewModel,
    iconManager: IconManager
) {
    val attachments by viewModel.getAttachmentsForReminder(reminderId).collectAsState(initial = emptyList())
    var showImagePicker by remember { mutableStateOf(false) }
    var selectedAttachment by remember { mutableStateOf<Attachment?>(null) }
    
    Column {
        // Header with add button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Attachments", style = MaterialTheme.typography.titleMedium)
            
            IconButton(onClick = { showImagePicker = true }) {
                PeaceIcon(
                    iconName = "add_circle",
                    contentDescription = "Add attachment",
                    iconManager = iconManager
                )
            }
        }
        
        // Attachment grid
        AttachmentGrid(
            attachments = attachments,
            onDelete = { attachment ->
                viewModel.deleteAttachment(attachment)
            },
            onAttachmentClick = { attachment ->
                selectedAttachment = attachment
            },
            iconManager = iconManager
        )
    }
    
    // Image picker dialog
    if (showImagePicker) {
        ImagePickerDialog(
            onDismiss = { showImagePicker = false },
            onImageSelected = { uri ->
                viewModel.addAttachment(reminderId, uri)
                showImagePicker = false
            },
            iconManager = iconManager
        )
    }
    
    // Full-screen viewer
    selectedAttachment?.let { attachment ->
        FullScreenImageViewer(
            attachment = attachment,
            onDismiss = { selectedAttachment = null },
            iconManager = iconManager
        )
    }
}
```

## Design Decisions

1. **Grid Layout**: Uses a 2-column grid for optimal thumbnail viewing on mobile devices
2. **Coil Integration**: Uses Coil for efficient image loading with crossfade animations
3. **Overlay UI**: Delete button and timestamp are overlaid on thumbnails to maximize image visibility
4. **Confirmation Dialogs**: All destructive actions require confirmation to prevent accidental deletions
5. **Full-Screen Viewer**: Provides immersive viewing experience with minimal UI
6. **Chronological Ordering**: Attachments are displayed in the order they were added
7. **Empty States**: Clear messaging when no attachments exist

## Dependencies

- Coil for image loading
- Material3 for UI components
- Compose Foundation for layouts
- Activity Compose for image picker integration

## Testing

See `AttachmentComponentsTest.kt` for UI tests covering:
- Attachment item rendering
- Grid layout behavior
- Dialog interactions
- Image picker integration
- Full-screen viewer functionality
