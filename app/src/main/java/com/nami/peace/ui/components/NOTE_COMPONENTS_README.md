# Note Components

This document describes the note UI components created for the Peace app enhancement project.

## Overview

The note components provide a complete UI solution for displaying, adding, and deleting notes associated with reminders. Notes are displayed in chronological order with timestamps and support deletion with confirmation dialogs.

## Components

### 1. NoteItem

A single note card that displays note content with a timestamp and delete button.

**Features:**
- Displays note content in a card layout
- Shows formatted timestamp (relative for recent notes, absolute for older ones)
- Delete button with confirmation dialog
- Material Design 3 styling with elevation and rounded corners

**Usage:**
```kotlin
NoteItem(
    note = Note(
        id = 1,
        reminderId = 1,
        content = "This is a note",
        timestamp = System.currentTimeMillis()
    ),
    onDelete = { /* Handle deletion */ },
    iconManager = iconManager
)
```

**Timestamp Formatting:**
- "Just now" - Less than 1 minute ago
- "Xm ago" - Less than 1 hour ago
- "Xh ago" - Less than 1 day ago
- "Xd ago" - Less than 1 week ago
- "MMM d, yyyy" - Older than 1 week

### 2. NoteList

A scrollable list of notes with empty state support.

**Features:**
- Displays all notes in a LazyColumn
- Chronological ordering (should be sorted before passing to component)
- Empty state with icon and message
- Smooth animations for item appearance/disappearance
- Automatic spacing between items

**Usage:**
```kotlin
NoteList(
    notes = notes.sortedByDescending { it.timestamp }, // Most recent first
    onDelete = { note -> /* Handle deletion */ },
    iconManager = iconManager
)
```

**Empty State:**
When no notes are present, displays:
- Document icon
- "No notes yet" message

### 3. AddNoteDialog

A dialog for creating new notes with multi-line text input.

**Features:**
- Multi-line text input (5-8 lines)
- Input validation (prevents empty notes)
- Error message display
- Cancel and Add buttons
- Material Design 3 dialog styling

**Usage:**
```kotlin
if (showAddDialog) {
    AddNoteDialog(
        onDismiss = { showAddDialog = false },
        onConfirm = { content ->
            // Create note with content
            showAddDialog = false
        },
        iconManager = iconManager
    )
}
```

**Validation:**
- Prevents submission of empty or whitespace-only content
- Shows error message: "Note cannot be empty"
- Trims whitespace from submitted content

### 4. DeleteNoteConfirmationDialog

An internal confirmation dialog for note deletion.

**Features:**
- Warning icon and title
- Confirmation message
- Cancel and Delete buttons
- Delete button styled in error color

**Behavior:**
- Automatically shown when delete button is clicked in NoteItem
- Prevents accidental deletions
- Message: "Are you sure you want to delete this note? This action cannot be undone."

## Integration Example

Here's a complete example of integrating note components in a reminder detail screen:

```kotlin
@Composable
fun ReminderDetailScreen(
    reminderId: Int,
    noteRepository: NoteRepository,
    iconManager: IconManager
) {
    val notes by noteRepository.getNotesForReminder(reminderId)
        .collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Notes",
                style = MaterialTheme.typography.titleLarge
            )
            
            IconButton(onClick = { showAddDialog = true }) {
                PeaceIcon(
                    iconName = "add_circle",
                    contentDescription = "Add note",
                    iconManager = iconManager
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Note list
        NoteList(
            notes = notes.sortedByDescending { it.timestamp },
            onDelete = { note ->
                scope.launch {
                    noteRepository.deleteNote(note)
                }
            },
            iconManager = iconManager
        )
    }
    
    // Add note dialog
    if (showAddDialog) {
        AddNoteDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { content ->
                scope.launch {
                    noteRepository.insertNote(
                        Note(
                            reminderId = reminderId,
                            content = content,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    showAddDialog = false
                }
            },
            iconManager = iconManager
        )
    }
}
```

## Design Decisions

### Chronological Ordering
Notes are displayed in chronological order (most recent first) to show the latest updates at the top. The component expects the list to be pre-sorted, giving the caller flexibility in ordering.

### Timestamp Display
Timestamps use relative time for recent notes (e.g., "2h ago") and absolute dates for older notes (e.g., "Jan 15, 2024"). This provides context without cluttering the UI.

### Delete Confirmation
All note deletions require confirmation to prevent accidental data loss. The confirmation dialog clearly states that the action cannot be undone.

### Multi-line Input
The AddNoteDialog uses a multi-line text field (5-8 lines) to accommodate longer notes while maintaining a reasonable dialog size.

### Empty State
The empty state uses a document icon and friendly message to guide users when no notes exist.

## Accessibility

All components include:
- Content descriptions for icons
- Proper semantic structure
- Touch target sizes meeting minimum requirements (48dp)
- Color contrast ratios meeting WCAG guidelines

## Testing

Comprehensive tests are provided in `NoteComponentsTest.kt`:
- Note content display
- Timestamp formatting
- Delete confirmation flow
- Empty state rendering
- Dialog validation
- Callback invocation

## Requirements Validation

This implementation satisfies the following requirements:

**Requirement 5.1:** Notes are stored with timestamps
- ✅ NoteItem displays timestamp
- ✅ Timestamp is part of Note model

**Requirement 5.3:** Notes displayed in chronological order
- ✅ NoteList supports chronological ordering
- ✅ Example shows sorting by timestamp

**Requirement 5.4:** Note deletion with confirmation
- ✅ Delete button in NoteItem
- ✅ DeleteNoteConfirmationDialog prevents accidental deletion
- ✅ Clear warning message

## Icons Used

The following Ionicons are used in the note components:
- `time` - Timestamp indicator
- `trash` - Delete button
- `document_text` - Empty state icon
- `create` - Add note dialog title
- `alert_circle` - Delete confirmation warning

## Future Enhancements

Potential improvements for future iterations:
- Rich text formatting (bold, italic, lists)
- Note editing capability
- Note search/filter
- Note categories or tags
- Markdown support
- Image attachments within notes
- Voice-to-text input
