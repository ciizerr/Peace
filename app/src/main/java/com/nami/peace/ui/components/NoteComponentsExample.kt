package com.nami.peace.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nami.peace.domain.model.Note
import com.nami.peace.ui.theme.PeaceTheme
import com.nami.peace.util.icon.IconManager

/**
 * Example screen demonstrating the usage of note components.
 * This shows how to integrate NoteList and AddNoteDialog in a real screen.
 */
@Composable
fun NoteComponentsExampleScreen(
    iconManager: IconManager,
    modifier: Modifier = Modifier
) {
    var notes by remember {
        mutableStateOf(
            listOf(
                Note(
                    id = 1,
                    reminderId = 1,
                    content = "Remember to review the design document before the meeting",
                    timestamp = System.currentTimeMillis() - 3600_000 // 1 hour ago
                ),
                Note(
                    id = 2,
                    reminderId = 1,
                    content = "Updated the deadline to next Friday",
                    timestamp = System.currentTimeMillis() - 7200_000 // 2 hours ago
                ),
                Note(
                    id = 3,
                    reminderId = 1,
                    content = "Initial note: This task requires coordination with the design team",
                    timestamp = System.currentTimeMillis() - 86400_000 // 1 day ago
                )
            )
        )
    }
    var showAddDialog by remember { mutableStateOf(false) }
    var nextId by remember { mutableStateOf(4) }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Notes Example",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Demonstrating note components with chronological ordering",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Add note button
            Button(
                onClick = { showAddDialog = true }
            ) {
                Text("Add Note")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Note list
            NoteList(
                notes = notes.sortedByDescending { it.timestamp }, // Most recent first
                onDelete = { note ->
                    notes = notes.filter { it.id != note.id }
                },
                iconManager = iconManager
            )
        }
    }

    // Add note dialog
    if (showAddDialog) {
        AddNoteDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { content ->
                val newNote = Note(
                    id = nextId++,
                    reminderId = 1,
                    content = content,
                    timestamp = System.currentTimeMillis()
                )
                notes = notes + newNote
                showAddDialog = false
            },
            iconManager = iconManager
        )
    }
}

/**
 * Preview for the note components example screen.
 */
@Preview(showBackground = true)
@Composable
private fun NoteComponentsExampleScreenPreview() {
    PeaceTheme {
        // Mock IconManager for preview
        val mockIconManager = object : IconManager {
            override fun getIcon(iconName: String): Int? = null
            override fun getAllIcons(): Map<String, Int> = emptyMap()
            override fun getFallbackIcon(iconName: String): Int = 0
            override fun hasIcon(iconName: String): Boolean = false
        }

        NoteComponentsExampleScreen(
            iconManager = mockIconManager
        )
    }
}

/**
 * Example of a single note item in isolation.
 */
@Preview(showBackground = true)
@Composable
private fun NoteItemPreview() {
    PeaceTheme {
        val mockIconManager = object : IconManager {
            override fun getIcon(iconName: String): Int? = null
            override fun getAllIcons(): Map<String, Int> = emptyMap()
            override fun getFallbackIcon(iconName: String): Int = 0
            override fun hasIcon(iconName: String): Boolean = false
        }

        NoteItem(
            note = Note(
                id = 1,
                reminderId = 1,
                content = "This is a sample note with some content to demonstrate how it looks in the UI",
                timestamp = System.currentTimeMillis() - 1800_000 // 30 minutes ago
            ),
            onDelete = {},
            iconManager = mockIconManager,
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Example of an empty note list.
 */
@Preview(showBackground = true)
@Composable
private fun EmptyNoteListPreview() {
    PeaceTheme {
        val mockIconManager = object : IconManager {
            override fun getIcon(iconName: String): Int? = null
            override fun getAllIcons(): Map<String, Int> = emptyMap()
            override fun getFallbackIcon(iconName: String): Int = 0
            override fun hasIcon(iconName: String): Boolean = false
        }

        NoteList(
            notes = emptyList(),
            onDelete = {},
            iconManager = mockIconManager,
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Example of the add note dialog.
 */
@Preview(showBackground = true)
@Composable
private fun AddNoteDialogPreview() {
    PeaceTheme {
        val mockIconManager = object : IconManager {
            override fun getIcon(iconName: String): Int? = null
            override fun getAllIcons(): Map<String, Int> = emptyMap()
            override fun getFallbackIcon(iconName: String): Int = 0
            override fun hasIcon(iconName: String): Boolean = false
        }

        AddNoteDialog(
            onDismiss = {},
            onConfirm = {},
            iconManager = mockIconManager
        )
    }
}
