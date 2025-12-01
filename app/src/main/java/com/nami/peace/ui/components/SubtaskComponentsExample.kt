package com.nami.peace.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nami.peace.domain.model.Subtask
import com.nami.peace.util.icon.IconManager

/**
 * Example screen demonstrating the usage of Subtask UI components.
 * This is for reference and testing purposes only.
 */
@Composable
fun SubtaskComponentsExampleScreen(
    iconManager: IconManager
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val subtasks = remember {
        mutableStateListOf(
            Subtask(
                id = 1,
                reminderId = 1,
                title = "Research topic",
                isCompleted = true,
                order = 0
            ),
            Subtask(
                id = 2,
                reminderId = 1,
                title = "Create outline",
                isCompleted = true,
                order = 1
            ),
            Subtask(
                id = 3,
                reminderId = 1,
                title = "Write first draft",
                isCompleted = false,
                order = 2
            ),
            Subtask(
                id = 4,
                reminderId = 1,
                title = "Review and edit",
                isCompleted = false,
                order = 3
            ),
            Subtask(
                id = 5,
                reminderId = 1,
                title = "Submit final version",
                isCompleted = false,
                order = 4
            )
        )
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "Subtask Components Example",
                style = MaterialTheme.typography.headlineMedium
            )

            // Progress indicator
            val completedCount = subtasks.count { it.isCompleted }
            val totalCount = subtasks.size
            val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

            Column {
                Text(
                    text = "Progress: $completedCount / $totalCount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subtask list
            SubtaskList(
                subtasks = subtasks,
                onCheckedChange = { subtask, checked ->
                    val index = subtasks.indexOfFirst { it.id == subtask.id }
                    if (index != -1) {
                        subtasks[index] = subtask.copy(isCompleted = checked)
                    }
                },
                onReorder = { fromIndex, toIndex ->
                    if (fromIndex != toIndex) {
                        val item = subtasks.removeAt(fromIndex)
                        subtasks.add(toIndex, item)
                        // Update order values
                        subtasks.forEachIndexed { index, subtask ->
                            subtasks[index] = subtask.copy(order = index)
                        }
                    }
                },
                iconManager = iconManager,
                modifier = Modifier.weight(1f)
            )

            // Add button
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                PeaceIcon(
                    iconName = "add",
                    contentDescription = "Add",
                    iconManager = iconManager,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = "Add Subtask",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Add dialog
            if (showAddDialog) {
                AddSubtaskDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { title ->
                        val newSubtask = Subtask(
                            id = (subtasks.maxOfOrNull { it.id } ?: 0) + 1,
                            reminderId = 1,
                            title = title,
                            isCompleted = false,
                            order = subtasks.size
                        )
                        subtasks.add(newSubtask)
                        showAddDialog = false
                    },
                    iconManager = iconManager
                )
            }
        }
    }
}

/**
 * Example showing a single SubtaskItem in isolation.
 */
@Composable
fun SubtaskItemExample(
    iconManager: IconManager
) {
    var isCompleted by remember { mutableStateOf(false) }
    val subtask = Subtask(
        id = 1,
        reminderId = 1,
        title = "Example subtask with a longer title to show text wrapping",
        isCompleted = isCompleted,
        order = 0
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Single SubtaskItem Example",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        SubtaskItem(
            subtask = subtask,
            onCheckedChange = { checked ->
                isCompleted = checked
            },
            iconManager = iconManager
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Status: ${if (isCompleted) "Completed" else "Incomplete"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

/**
 * Example showing the AddSubtaskDialog in isolation.
 */
@Composable
fun AddSubtaskDialogExample(
    iconManager: IconManager
) {
    var showDialog by remember { mutableStateOf(false) }
    var lastAddedTitle by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "AddSubtaskDialog Example",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { showDialog = true }) {
            Text("Show Add Dialog")
        }

        lastAddedTitle?.let { title ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Last added: $title",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (showDialog) {
            AddSubtaskDialog(
                onDismiss = { showDialog = false },
                onConfirm = { title ->
                    lastAddedTitle = title
                    showDialog = false
                },
                iconManager = iconManager
            )
        }
    }
}
