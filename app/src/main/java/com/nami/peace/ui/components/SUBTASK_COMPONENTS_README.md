# Subtask UI Components

This document describes the subtask UI components created for the Peace app.

## Components

### 1. SubtaskItem

A single subtask item with checkbox and title. Supports completion state with animations and drag-to-reorder functionality.

**Features:**
- Animated checkbox that scales when checked
- Text color animation when completed
- Strikethrough text decoration for completed subtasks
- Drag handle for reordering
- Background elevation animation when dragging
- Long-press to initiate drag gesture

**Usage:**
```kotlin
SubtaskItem(
    subtask = subtask,
    onCheckedChange = { checked -> 
        // Handle checkbox state change
    },
    onDragStart = { 
        // Handle drag start
    },
    onDragEnd = { 
        // Handle drag end
    },
    onDrag = { offset -> 
        // Handle drag offset
    },
    isDragging = false,
    iconManager = iconManager
)
```

**Parameters:**
- `subtask`: The Subtask model to display
- `onCheckedChange`: Callback when checkbox state changes
- `onDragStart`: Callback when drag gesture starts (optional)
- `onDragEnd`: Callback when drag gesture ends (optional)
- `onDrag`: Callback during drag with vertical offset (optional)
- `isDragging`: Whether this item is currently being dragged
- `iconManager`: IconManager for loading Ionicons
- `modifier`: Modifier for the item (optional)

### 2. SubtaskList

A list of subtasks with drag-to-reorder support. Displays all subtasks for a reminder with animations.

**Features:**
- LazyColumn for efficient rendering
- Empty state message when no subtasks exist
- Expand/fade animations for items
- Automatic drag-to-reorder calculation
- Sorted by order field

**Usage:**
```kotlin
SubtaskList(
    subtasks = subtasks,
    onCheckedChange = { subtask, checked ->
        // Handle checkbox state change for specific subtask
    },
    onReorder = { fromIndex, toIndex ->
        // Handle reordering from one index to another
    },
    iconManager = iconManager
)
```

**Parameters:**
- `subtasks`: List of Subtask models to display
- `onCheckedChange`: Callback when a subtask's checkbox state changes
- `onReorder`: Callback when subtasks are reordered (fromIndex, toIndex)
- `iconManager`: IconManager for loading Ionicons
- `modifier`: Modifier for the list (optional)

**Reordering Logic:**
The component automatically calculates the new index based on drag offset. The approximate item height is 64dp (card + spacing). When the user drags an item, the component calculates how many positions to move based on the vertical offset.

### 3. AddSubtaskDialog

Dialog for adding a new subtask. Provides a text input field and action buttons.

**Features:**
- Material Design 3 dialog styling
- Text input validation
- Error message for empty titles
- Automatic whitespace trimming
- Cancel and Add buttons

**Usage:**
```kotlin
AddSubtaskDialog(
    onDismiss = {
        // Handle dialog dismissal
    },
    onConfirm = { title ->
        // Handle subtask creation with the entered title
    },
    iconManager = iconManager
)
```

**Parameters:**
- `onDismiss`: Callback when dialog is dismissed
- `onConfirm`: Callback when subtask is confirmed with the entered title
- `iconManager`: IconManager for loading Ionicons

**Validation:**
- Title cannot be empty or blank
- Whitespace is automatically trimmed
- Error message displayed if validation fails

## Animations

All components include smooth animations:

1. **Checkbox Scale**: Scales to 1.1x when checked (200ms duration)
2. **Text Color**: Fades to 60% opacity when completed (300ms duration)
3. **Text Decoration**: Adds strikethrough when completed
4. **Background Color**: Changes to surface variant when dragging (200ms duration)
5. **Elevation**: Increases to 8dp when dragging (200ms duration)
6. **List Items**: Expand/fade in when added, shrink/fade out when removed

## Accessibility

- All icons have content descriptions
- Checkbox states are properly announced
- Touch targets meet minimum size requirements (48dp)
- Color contrast ratios meet WCAG guidelines

## Integration

To integrate these components into a screen:

```kotlin
@Composable
fun ReminderDetailScreen(
    reminderId: Int,
    iconManager: IconManager,
    viewModel: ReminderDetailViewModel = hiltViewModel()
) {
    val subtasks by viewModel.getSubtasksForReminder(reminderId).collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    
    Column {
        // ... other content ...
        
        SubtaskList(
            subtasks = subtasks,
            onCheckedChange = { subtask, checked ->
                viewModel.updateSubtask(subtask.copy(isCompleted = checked))
            },
            onReorder = { fromIndex, toIndex ->
                viewModel.reorderSubtasks(fromIndex, toIndex)
            },
            iconManager = iconManager
        )
        
        Button(onClick = { showAddDialog = true }) {
            Text("Add Subtask")
        }
        
        if (showAddDialog) {
            AddSubtaskDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { title ->
                    viewModel.addSubtask(reminderId, title)
                    showAddDialog = false
                },
                iconManager = iconManager
            )
        }
    }
}
```

## Requirements Satisfied

This implementation satisfies the following requirements from the spec:

- **Requirement 4.1**: Subtasks are linked to parent reminders
- **Requirement 4.2**: Checkbox state updates in real-time
- **Requirement 4.4**: Drag-to-reorder support for subtasks

## Testing

Unit tests are provided in `SubtaskComponentsTest.kt` covering:
- Subtask model properties
- Completion state toggling
- Reordering logic
- Progress calculation
- Title validation and trimming
- Empty list handling

## Dependencies

- Jetpack Compose Material 3
- Compose Foundation (for drag gestures)
- Compose Animation
- IconManager (for Ionicons)
- Subtask domain model

## Future Enhancements

Potential improvements for future iterations:
- Swipe-to-delete gesture
- Bulk operations (complete all, delete all)
- Subtask templates
- Due dates for individual subtasks
- Priority levels for subtasks
