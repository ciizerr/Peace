# Attachment System

The attachment system provides comprehensive image attachment management for reminders in the Peace app.

## Features

- **Image Storage**: Saves images to app-private directory for security
- **Thumbnail Generation**: Automatically creates 200x200 thumbnails
- **File Size Validation**: Enforces 5MB maximum file size
- **Complete Cleanup**: Deletes both full images and thumbnails on removal

## Architecture

### AttachmentManager Interface

The `AttachmentManager` interface defines the contract for file operations:

```kotlin
interface AttachmentManager {
    suspend fun saveImage(uri: Uri, reminderId: Int): AttachmentPaths
    suspend fun deleteAttachment(filePath: String, thumbnailPath: String)
    suspend fun validateFileSize(uri: Uri): Boolean
    suspend fun getFileSize(uri: Uri): Long
}
```

### AttachmentManagerImpl

The implementation handles:
- Image compression (JPEG, 90% quality)
- Thumbnail generation with aspect ratio preservation
- File naming with timestamp uniqueness
- Error handling and cleanup

### Storage Structure

```
/data/data/com.nami.peace/files/
├── attachments/
│   ├── attachment_1_1234567890.jpg
│   ├── attachment_1_1234567891.jpg
│   └── ...
└── thumbnails/
    ├── thumb_1_1234567890.jpg
    ├── thumb_1_1234567891.jpg
    └── ...
```

## Use Cases

### AddAttachmentUseCase

Adds an image attachment to a reminder:

```kotlin
class AddAttachmentUseCase @Inject constructor(
    private val attachmentRepository: AttachmentRepository,
    private val attachmentManager: AttachmentManager
)
```

**Process:**
1. Validates file size (max 5MB)
2. Saves image to storage
3. Generates thumbnail
4. Stores metadata in database

**Throws:**
- `FileSizeExceededException` if image exceeds 5MB
- `IOException` if file operations fail

### DeleteAttachmentUseCase

Deletes an attachment and its files:

```kotlin
class DeleteAttachmentUseCase @Inject constructor(
    private val attachmentRepository: AttachmentRepository,
    private val attachmentManager: AttachmentManager
)
```

**Process:**
1. Removes database record
2. Deletes full image file
3. Deletes thumbnail file

### GetAttachmentsForReminderUseCase

Retrieves attachments for a reminder:

```kotlin
class GetAttachmentsForReminderUseCase @Inject constructor(
    private val attachmentRepository: AttachmentRepository
)
```

Returns a Flow of attachments sorted chronologically.

## Usage Example

```kotlin
// In ViewModel
class ReminderDetailViewModel @Inject constructor(
    private val addAttachment: AddAttachmentUseCase,
    private val deleteAttachment: DeleteAttachmentUseCase,
    private val getAttachments: GetAttachmentsForReminderUseCase
) : ViewModel() {
    
    fun addImage(uri: Uri, reminderId: Int) {
        viewModelScope.launch {
            try {
                addAttachment(uri, reminderId)
            } catch (e: FileSizeExceededException) {
                // Show error: "Image too large (max 5MB)"
            } catch (e: IOException) {
                // Show error: "Failed to save image"
            }
        }
    }
    
    fun deleteImage(attachment: Attachment) {
        viewModelScope.launch {
            deleteAttachment(attachment)
        }
    }
    
    val attachments = getAttachments(reminderId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
}
```

## Image Picker Integration

Use Accompanist Permissions for runtime permission handling:

```kotlin
@Composable
fun ImagePickerButton(onImageSelected: (Uri) -> Unit) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }
    
    Button(onClick = { launcher.launch("image/*") }) {
        Text("Add Image")
    }
}
```

## File Size Validation

The system enforces a 5MB maximum file size:

```kotlin
// Validation happens automatically in AddAttachmentUseCase
try {
    addAttachment(uri, reminderId)
} catch (e: FileSizeExceededException) {
    // Image exceeds 5MB - show error to user
}
```

## Thumbnail Generation

Thumbnails are automatically generated with:
- Maximum dimension: 200x200 pixels
- Aspect ratio: Preserved
- Format: JPEG
- Quality: 90%

## Error Handling

### FileSizeExceededException
Thrown when an image exceeds 5MB. Show user-friendly error message.

### IOException
Thrown when file operations fail. Possible causes:
- Insufficient storage space
- Permission issues
- Corrupted image file

## Testing

See `AttachmentManagerTest.kt` for unit tests covering:
- Image saving and thumbnail generation
- File size validation
- Attachment deletion
- Error scenarios

## Performance Considerations

- All file operations run on `Dispatchers.IO`
- Bitmaps are recycled after use to prevent memory leaks
- Thumbnails reduce memory usage for list displays
- LRU cache recommended for thumbnail loading in UI (use Coil)

## Security

- Images stored in app-private directory (`context.filesDir`)
- No external storage access required
- Files automatically deleted when app is uninstalled
- Foreign key cascade ensures orphaned files are cleaned up
