package com.nami.peace.ui.components

import com.nami.peace.domain.model.Note
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Note UI components logic.
 * Tests the helper functions and data transformations.
 */
class NoteComponentsTest {

    @Test
    fun note_hasCorrectProperties() {
        // Given
        val note = Note(
            id = 1,
            reminderId = 100,
            content = "This is a test note",
            timestamp = 1234567890L
        )

        // Then
        assertEquals(1, note.id)
        assertEquals(100, note.reminderId)
        assertEquals("This is a test note", note.content)
        assertEquals(1234567890L, note.timestamp)
    }

    @Test
    fun note_canBeCreatedWithDefaultTimestamp() {
        // Given
        val beforeCreation = System.currentTimeMillis()
        
        // When
        val note = Note(
            id = 1,
            reminderId = 1,
            content = "Test note"
        )
        
        val afterCreation = System.currentTimeMillis()

        // Then
        assertTrue(note.timestamp >= beforeCreation)
        assertTrue(note.timestamp <= afterCreation)
    }

    @Test
    fun noteList_canBeFiltered() {
        // Given
        val notes = listOf(
            Note(id = 1, reminderId = 1, content = "Note 1", timestamp = 1000L),
            Note(id = 2, reminderId = 2, content = "Note 2", timestamp = 2000L),
            Note(id = 3, reminderId = 1, content = "Note 3", timestamp = 3000L)
        )

        // When
        val reminder1Notes = notes.filter { it.reminderId == 1 }
        val reminder2Notes = notes.filter { it.reminderId == 2 }

        // Then
        assertEquals(2, reminder1Notes.size)
        assertEquals(1, reminder2Notes.size)
        assertEquals("Note 1", reminder1Notes.first().content)
        assertEquals("Note 2", reminder2Notes.first().content)
    }

    @Test
    fun noteList_canBeSortedByTimestamp() {
        // Given
        val notes = listOf(
            Note(id = 1, reminderId = 1, content = "Note 1", timestamp = 3000L),
            Note(id = 2, reminderId = 1, content = "Note 2", timestamp = 1000L),
            Note(id = 3, reminderId = 1, content = "Note 3", timestamp = 2000L)
        )

        // When - Sort by timestamp ascending (oldest first)
        val sortedAscending = notes.sortedBy { it.timestamp }
        
        // When - Sort by timestamp descending (newest first)
        val sortedDescending = notes.sortedByDescending { it.timestamp }

        // Then
        assertEquals("Note 2", sortedAscending[0].content)
        assertEquals("Note 3", sortedAscending[1].content)
        assertEquals("Note 1", sortedAscending[2].content)
        
        assertEquals("Note 1", sortedDescending[0].content)
        assertEquals("Note 3", sortedDescending[1].content)
        assertEquals("Note 2", sortedDescending[2].content)
    }

    @Test
    fun noteContent_canBeTrimmed() {
        // Given
        val content = "  Test note content  "

        // When
        val trimmedContent = content.trim()

        // Then
        assertEquals("Test note content", trimmedContent)
    }

    @Test
    fun noteContent_canBeValidated() {
        // Given
        val validContent = "Valid note content"
        val emptyContent = ""
        val blankContent = "   "

        // When/Then
        assertTrue(validContent.isNotBlank())
        assertFalse(emptyContent.isNotBlank())
        assertFalse(blankContent.isNotBlank())
    }

    @Test
    fun noteTimestamp_canBeCompared() {
        // Given
        val olderNote = Note(id = 1, reminderId = 1, content = "Older", timestamp = 1000L)
        val newerNote = Note(id = 2, reminderId = 1, content = "Newer", timestamp = 2000L)

        // When/Then
        assertTrue(olderNote.timestamp < newerNote.timestamp)
        assertTrue(newerNote.timestamp > olderNote.timestamp)
    }

    @Test
    fun noteList_handlesEmptyList() {
        // Given
        val notes = emptyList<Note>()

        // When
        val count = notes.size
        val filtered = notes.filter { it.reminderId == 1 }

        // Then
        assertEquals(0, count)
        assertEquals(0, filtered.size)
    }

    @Test
    fun noteList_canFindNoteById() {
        // Given
        val notes = listOf(
            Note(id = 1, reminderId = 1, content = "Note 1", timestamp = 1000L),
            Note(id = 2, reminderId = 1, content = "Note 2", timestamp = 2000L),
            Note(id = 3, reminderId = 1, content = "Note 3", timestamp = 3000L)
        )

        // When
        val foundNote = notes.find { it.id == 2 }
        val notFoundNote = notes.find { it.id == 99 }

        // Then
        assertNotNull(foundNote)
        assertEquals("Note 2", foundNote?.content)
        assertNull(notFoundNote)
    }

    @Test
    fun noteList_canRemoveNote() {
        // Given
        val notes = listOf(
            Note(id = 1, reminderId = 1, content = "Note 1", timestamp = 1000L),
            Note(id = 2, reminderId = 1, content = "Note 2", timestamp = 2000L),
            Note(id = 3, reminderId = 1, content = "Note 3", timestamp = 3000L)
        )

        // When
        val notesAfterRemoval = notes.filter { it.id != 2 }

        // Then
        assertEquals(2, notesAfterRemoval.size)
        assertFalse(notesAfterRemoval.any { it.id == 2 })
        assertTrue(notesAfterRemoval.any { it.id == 1 })
        assertTrue(notesAfterRemoval.any { it.id == 3 })
    }
}
