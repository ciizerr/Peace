package com.nami.peace.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.nami.peace.domain.model.Subtask
import com.nami.peace.util.icon.IconManager

/**
 * A single subtask item with checkbox and title.
 * Supports completion state with animations and drag-to-reorder functionality.
 *
 * @param subtask The subtask to display
 * @param onCheckedChange Callback when checkbox state changes
 * @param onDragStart Callback when drag gesture starts
 * @param onDragEnd Callback when drag gesture ends
 * @param onDrag Callback during drag with offset
 * @param isDragging Whether this item is currently being dragged
 * @param iconManager IconManager for loading icons
 * @param modifier Modifier for the item
 */
@Composable
fun SubtaskItem(
    subtask: Subtask,
    onCheckedChange: (Boolean) -> Unit,
    onDragStart: () -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDrag: (Float) -> Unit = {},
    isDragging: Boolean = false,
    iconManager: IconManager,
    modifier: Modifier = Modifier
) {
    // Animate checkbox scale when checked
    val checkboxScale by animateFloatAsState(
        targetValue = if (subtask.isCompleted) 1.1f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "checkbox_scale"
    )
    
    // Animate text color when completed
    val textColor by animateColorAsState(
        targetValue = if (subtask.isCompleted) {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(durationMillis = 300),
        label = "text_color"
    )
    
    // Animate background when dragging
    val backgroundColor by animateColorAsState(
        targetValue = if (isDragging) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = 200),
        label = "background_color"
    )
    
    // Animate elevation when dragging
    val elevation by animateFloatAsState(
        targetValue = if (isDragging) 8f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "elevation"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { onDragStart() },
                    onDragEnd = { onDragEnd() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.y)
                    }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation.dp
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drag handle icon
            PeaceIcon(
                iconName = "reorder_three",
                contentDescription = "Drag to reorder",
                iconManager = iconManager,
                size = 20.dp,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.alpha(if (isDragging) 1f else 0.6f)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Checkbox with scale animation
            Checkbox(
                checked = subtask.isCompleted,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.scale(checkboxScale),
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Subtask title with strikethrough when completed
            Text(
                text = subtask.title,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                textDecoration = if (subtask.isCompleted) {
                    TextDecoration.LineThrough
                } else {
                    TextDecoration.None
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * A list of subtasks with drag-to-reorder support.
 * Displays all subtasks for a reminder with animations.
 *
 * @param subtasks List of subtasks to display
 * @param onCheckedChange Callback when a subtask's checkbox state changes
 * @param onReorder Callback when subtasks are reordered (fromIndex, toIndex)
 * @param iconManager IconManager for loading icons
 * @param modifier Modifier for the list
 */
@Composable
fun SubtaskList(
    subtasks: List<Subtask>,
    onCheckedChange: (Subtask, Boolean) -> Unit,
    onReorder: (Int, Int) -> Unit,
    iconManager: IconManager,
    modifier: Modifier = Modifier
) {
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }
    
    Column(modifier = modifier) {
        if (subtasks.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No subtasks yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    items = subtasks,
                    key = { _, subtask -> subtask.id }
                ) { index, subtask ->
                    AnimatedVisibility(
                        visible = true,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        SubtaskItem(
                            subtask = subtask,
                            onCheckedChange = { checked ->
                                onCheckedChange(subtask, checked)
                            },
                            onDragStart = {
                                draggedIndex = index
                                dragOffset = 0f
                            },
                            onDragEnd = {
                                draggedIndex?.let { fromIndex ->
                                    val toIndex = calculateNewIndex(
                                        fromIndex,
                                        dragOffset,
                                        subtasks.size
                                    )
                                    if (fromIndex != toIndex) {
                                        onReorder(fromIndex, toIndex)
                                    }
                                }
                                draggedIndex = null
                                dragOffset = 0f
                            },
                            onDrag = { offset ->
                                dragOffset += offset
                            },
                            isDragging = draggedIndex == index,
                            iconManager = iconManager
                        )
                    }
                }
            }
        }
    }
}

/**
 * Calculate the new index for a dragged item based on drag offset.
 *
 * @param currentIndex Current index of the dragged item
 * @param dragOffset Total vertical drag offset in pixels
 * @param listSize Total number of items in the list
 * @return The new index where the item should be placed
 */
private fun calculateNewIndex(
    currentIndex: Int,
    dragOffset: Float,
    listSize: Int
): Int {
    // Approximate item height (card + spacing)
    val itemHeight = 64f
    val indexChange = (dragOffset / itemHeight).toInt()
    val newIndex = currentIndex + indexChange
    
    return newIndex.coerceIn(0, listSize - 1)
}

/**
 * Dialog for adding a new subtask.
 * Provides a text input field and action buttons.
 *
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when subtask is confirmed with the entered title
 * @param iconManager IconManager for loading icons
 */
@Composable
fun AddSubtaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    iconManager: IconManager
) {
    var subtaskTitle by remember { mutableStateOf("") }
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
                // Dialog title
                Text(
                    text = "Add Subtask",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Text input field
                androidx.compose.material3.OutlinedTextField(
                    value = subtaskTitle,
                    onValueChange = {
                        subtaskTitle = it
                        showError = false
                    },
                    label = { Text("Subtask title") },
                    placeholder = { Text("Enter subtask description") },
                    isError = showError,
                    supportingText = if (showError) {
                        { Text("Title cannot be empty") }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
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
                            if (subtaskTitle.isBlank()) {
                                showError = true
                            } else {
                                onConfirm(subtaskTitle.trim())
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
